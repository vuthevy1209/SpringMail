package com.vuthevy1209.springmail.service.mail.impl;

import com.vuthevy1209.springmail.converters.MailMessageConverter;
import com.vuthevy1209.springmail.converters.MailThreadConverter;
import com.vuthevy1209.springmail.dto.mail.request.FetchOlderRequest;
import com.vuthevy1209.springmail.dto.mail.response.FetchOlderResponse;
import com.vuthevy1209.springmail.entity.MailChunkElasticSearch;
import com.vuthevy1209.springmail.entity.MailElasticSearch;
import com.vuthevy1209.springmail.entity.MailMessage;
import com.vuthevy1209.springmail.entity.MailThread;
import com.vuthevy1209.springmail.entity.User;
import com.vuthevy1209.springmail.enums.MailLabel;
import com.vuthevy1209.springmail.enums.SyncStatus;
import com.vuthevy1209.springmail.exception.AppException;
import com.vuthevy1209.springmail.exception.ErrorCode;
import com.vuthevy1209.springmail.repository.MailChunkElasticSearchRepository;
import com.vuthevy1209.springmail.repository.MailElasticSearchRepository;
import com.vuthevy1209.springmail.repository.MailMessageRepository;
import com.vuthevy1209.springmail.repository.MailThreadRepository;
import com.vuthevy1209.springmail.repository.UserRepository;
import com.vuthevy1209.springmail.service.gmail.GmailService;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailHistoryDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailHistoryLabelAddedDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailHistoryLabelRemovedDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailHistoryMessageAddedDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailHistoryMessageDeletedDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailListHistoryResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.message.GmailMessageDto;
import com.vuthevy1209.springmail.service.gmail.dto.profile.GmailProfileDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailListThreadsResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailThreadDto;
import com.vuthevy1209.springmail.service.mail.MailSyncService;
import com.vuthevy1209.springmail.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.context.ApplicationEventPublisher;
import com.vuthevy1209.springmail.event.SyncMailMessageEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailSyncServiceImpl implements MailSyncService {

	private final GmailService gmailService;

	private final UserRepository userRepository;
	private final MailThreadRepository mailThreadRepository;
	private final MailMessageRepository mailMessageRepository;
	private final MailElasticSearchRepository mailElasticSearchRepository;
	private final MailChunkElasticSearchRepository mailChunkElasticSearchRepository;


	private final MailThreadConverter mailThreadConverter;
	private final MailMessageConverter mailMessageConverter;

	private final AuthorizedClientServiceOAuth2AuthorizedClientManager backgroundAuthorizedClientManager;
	private final ApplicationEventPublisher applicationEventPublisher;

	private final String FORMAT = "full";
	private final Long QUANTITY_OF_THREAD_NEEDED_TO_SYNC = 20L;
	private final String INITIAL_SYNC_QUERY = MailLabel.toGmailQuery(MailLabel.INBOX.getId())
			+ " " + MailLabel.toGmailQuery(MailLabel.CATEGORY_PERSONAL.getId());

	@Override
	public void syncMail(User user, String accessToken) throws IOException {
		boolean isFirstSync = user.getSyncStatus() != SyncStatus.COMPLETED;
		try {
			if (isFirstSync) {
				executeInitialSyncSequence(user, accessToken);
			} else {
				executeIncrementalSyncSequence(user, accessToken);
			}
		} catch (Exception e) {
			log.error("Mail synchronization failed for user {}", user.getEmail(), e);
			throw new IOException("Mail synchronization failed for user " + user.getEmail(), e);
		}
	}

	private void executeInitialSyncSequence(User user, String accessToken) throws IOException {
		log.info("Executing initial sync for user {}", user.getEmail());

		// Step 1: Capture the current state of the mailbox (anchor)
		//         With the first, history is the newest
		GmailProfileDto profile = gmailService.getProfile(accessToken);
		Long lastHistoryId = profile.getHistoryId();

		// Step 2: Fetch the first page of threads (chỉ lấy INBOX + CATEGORY_PERSONAL)
		GmailListThreadsResponseDto threadsResponse = gmailService.listThreads(accessToken, INITIAL_SYNC_QUERY,
				QUANTITY_OF_THREAD_NEEDED_TO_SYNC, null);

		if (threadsResponse.getThreads() != null && !threadsResponse.getThreads().isEmpty()) {
			List<GmailThreadDto> threads = threadsResponse.getThreads();
			List<CompletableFuture<Void>> allFutures = new ArrayList<>();

			for (GmailThreadDto threadMetadata : threads) {
				List<CompletableFuture<Void>> threadFutures = fetchAndProcessThread(user, accessToken, threadMetadata.getId());
				allFutures.addAll(threadFutures);
			}

			// Track progress dựa trên số message thực sự được xử lý xong (MongoDB & ES)
			int totalMessages = allFutures.size();
			AtomicInteger completedMessages = new AtomicInteger(0);

			for (CompletableFuture<Void> future : allFutures) {
				future.whenComplete((result, ex) -> {
					int count = completedMessages.incrementAndGet();
					if (count % 3 == 0 || count == totalMessages) {
						int percent = Math.min(99, (int) Math.round((double) count / totalMessages * 99));
						user.setInitialSyncProgress(percent);
						userRepository.save(user);
					}
				});
			}

			// Chờ tất cả messages được xử lý xong thực sự (lưu xong MongoDB & ES)
			CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();
		}

		// Step 3: Update user state - save progress for pagination and anchor for
		// incremental sync
		user.setLastHistoryId(lastHistoryId);
		user.setNextPageToken(threadsResponse.getNextPageToken());
		user.setSyncStatus(SyncStatus.COMPLETED);
		user.setInitialSyncProgress(100);
		userRepository.save(user);

		log.info("Initial sync completed for user {} with historyId {}", user.getEmail(), lastHistoryId);

		// Ngay sau khi initial sync xong, gọi incremental sync một lần
		// (những webhooks bị bỏ qua vì status đang là INITIAL_SYNC_IN_PROGRESS)
		log.info("Triggering follow-up incremental sync to catch up emails that arrived during initial sync...");
		executeIncrementalSyncSequence(user, accessToken);
	}

	private void executeIncrementalSyncSequence(User user, String accessToken) throws IOException {
		log.info("Executing incremental sync for user {} from historyId {}", user.getEmail(), user.getLastHistoryId());

		try {
			String pageToken = null;
			Long newLastHistoryId = user.getLastHistoryId();

			do {
				GmailListHistoryResponseDto gmailListHistory = gmailService.listHistory(accessToken,
						user.getLastHistoryId().toString(), 100L, pageToken);

				if (gmailListHistory.getHistory() != null) {
					Set<String> threadIdsToUpdate = new HashSet<>();

					for (GmailHistoryDto history : gmailListHistory.getHistory()) {
						// Case 1: Messages added
						if (history.getMessagesAdded() != null) {
							for (GmailHistoryMessageAddedDto added : history.getMessagesAdded()) {
								threadIdsToUpdate.add(added.getMessage().getThreadId());
							}
						}

						// Case 2: Label changes (Mark as read/unread, Starred, Trash, etc.)
						if (history.getLabelsAdded() != null) {
							for (GmailHistoryLabelAddedDto added : history.getLabelsAdded()) {
								threadIdsToUpdate.add(added.getMessage().getThreadId());
							}
						}
						if (history.getLabelsRemoved() != null) {
							for (GmailHistoryLabelRemovedDto removed : history.getLabelsRemoved()) {
								threadIdsToUpdate.add(removed.getMessage().getThreadId());
							}
						}

						// Case 3: Permanent deletions
						if (history.getMessagesDeleted() != null) {
							for (GmailHistoryMessageDeletedDto deleted : history.getMessagesDeleted()) {
								String messageId = deleted.getMessage().getId();
								log.info("Handling message deletion: {}", messageId);

								// Try to get threadId to update metadata later
								if (deleted.getMessage().getThreadId() != null) {
									threadIdsToUpdate.add(deleted.getMessage().getThreadId());
								} else {
									mailMessageRepository.findById(messageId)
											.ifPresent(msg -> threadIdsToUpdate.add(msg.getThreadId()));
								}

								mailMessageRepository.deleteById(messageId);
							}
						}
					}

					for (String threadId : threadIdsToUpdate) {
						fetchAndProcessThread(user, accessToken, threadId);
					}
				}

				if (gmailListHistory.getHistoryId() != null) {
					newLastHistoryId = gmailListHistory.getHistoryId();
				}

				pageToken = gmailListHistory.getNextPageToken();
			} while (pageToken != null);

			// Update lastHistoryId for next incremental sync
			if (!newLastHistoryId.equals(user.getLastHistoryId())) {
				user.setLastHistoryId(newLastHistoryId);
				userRepository.save(user);
			}

			log.info("Incremental sync completed for user {}. New historyId: {}", user.getEmail(), newLastHistoryId);

		} catch (IOException e) {
			if (e.getMessage() != null && (e.getMessage().contains("404") || e.getMessage().contains("410"))) {
				log.warn("History expired for user {}. Falling back to initial sync sequence.", user.getEmail());
				executeInitialSyncSequence(user, accessToken);
			} else {
				throw e;
			}
		}
	}

	@Override
	public FetchOlderResponse fetchOlderThreads(FetchOlderRequest request) throws IOException {
		OAuth2User oauthUser = SecurityUtils.getCurrentOAuth2User();
		if (oauthUser == null) {
			throw new RuntimeException("Current user not found");
		}
		
		String email = oauthUser.getAttribute("email");
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User not found in database: " + email));

		// Chặn Fetch Older nếu đang trong quá trình đồng bộ lần đầu
		if (user.getSyncStatus() == SyncStatus.INITIAL_SYNC_IN_PROGRESS) {
			log.info("Initial sync in progress, blocking fetch-older for user {}", email);
			return FetchOlderResponse.builder()
					.fetchedCount(0)
					.nextPageToken(request.getPageToken())
					.build();
		}

		String accessToken = SecurityUtils.getAccessToken("google");
		if (accessToken == null) {
			throw new IOException("Missing access token");
		}

		String query = null;
		if (request.getLabelIds() != null && !request.getLabelIds().isEmpty()) {
			query = request.getLabelIds().stream()
					.map(MailLabel::toGmailQuery)
					.collect(Collectors.joining(" "));
		}

		if (request.getBeforeTimestamp() != null && request.getBeforeTimestamp() > 0) {
			String beforeQuery = "before:" + (request.getBeforeTimestamp() / 1000);
			query = (query == null || query.isEmpty()) ? beforeQuery : query + " " + beforeQuery;
		}

		int maxResults = request.getMaxResults() > 0 ? request.getMaxResults() : 50;

		log.info("Fetching older threads for user {}, query {}, pageToken {}", user.getEmail(), query,
				request.getPageToken());

		GmailListThreadsResponseDto response;
		try {
			response = gmailService.listThreads(accessToken, query, (long) maxResults, request.getPageToken());
		} catch (IOException e) {
			if (e.getMessage() != null
					&& (e.getMessage().contains("401 Unauthorized") || e.getMessage().contains("401"))) {
				throw new AppException(ErrorCode.GMAIL_UNAUTHENTICATED);
			}
			throw e;
		}

		int count = 0;

		if (response.getThreads() != null) {
			for (GmailThreadDto metadata : response.getThreads()) {
				fetchAndProcessThread(user, accessToken, metadata.getId());
				count++;
			}
		}

		return FetchOlderResponse.builder()
				.nextPageToken(response.getNextPageToken())
				.fetchedCount(count)
				.build();
	}

	@Override
	public void processNewEmails(String email, String historyId) {
		log.info("Processing new emails for {} triggered by webhook", email);

		User user = userRepository.findByEmail(email).orElse(null);
		if (user == null) {
				log.warn("User {} not found in DB. Cannot process webhook.", email);
				return;
		}

		// Nếu người dùng đang trong quá trình đồng bộ lần đầu (INITIAL_SYNC_IN_PROGRESS hoặc PENDING)
		// thì KHÔNG chạy đè đồng bộ Incremental, tránh đụng độ và phá hỏng dữ liệu Thread.
		if (user.getSyncStatus() != SyncStatus.COMPLETED) {
				log.info("User {} is currently in initial sync or not fully synced (Status: {}). Skipping webhook processing.", email, user.getSyncStatus());
				return;
		}

		// Tạo một proxy Authentication dựa trên email (cùng định dạng principal đã lưu)
		Authentication principal =
				new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

		OAuth2AuthorizeRequest authorizeRequest =
				OAuth2AuthorizeRequest.withClientRegistrationId("google")
						.principal(principal)
						.build();

		OAuth2AuthorizedClient client = backgroundAuthorizedClientManager.authorize(authorizeRequest);
		if (client == null || client.getAccessToken() == null) {
				log.warn("OAuth2AuthorizedClient not found or missing access token for user {}", email);
				return;
		}

		try {
				String accessToken = client.getAccessToken().getTokenValue();
				this.syncMail(user, accessToken);
		} catch (Exception e) {
				log.error("Failed to sync new emails for user {}", email, e);
		}
	}

    private List<CompletableFuture<Void>> fetchAndProcessThread(User user, String accessToken, String threadId) throws IOException {
		// Fetch full thread details
		GmailThreadDto fullThread = null;

		try {
			fullThread = gmailService.getThread(accessToken, threadId, FORMAT, null);
		} catch (IOException e) {
			if (e.getMessage() != null && e.getMessage().contains("404")) {
				log.info("Thread {} not found in Gmail. Deleting locally.", threadId);
				mailThreadRepository.deleteById(threadId);
				mailMessageRepository.deleteByThreadId(threadId);
				return Collections.emptyList();
			}
			throw e;
		}

		if (fullThread == null || fullThread.getMessages() == null) {
			return Collections.emptyList();
		}

		String subject = null;
		List<String> senderNames = new ArrayList<>();
		int messageCount = 0;
		Long lastMessageTimestamp = null;
		List<CompletableFuture<Void>> futures = new ArrayList<>();

		for (GmailMessageDto msg : fullThread.getMessages()) {
			messageCount++;
			if (subject == null && msg.getSubject() != null && !msg.getSubject().isBlank()) {
				subject = msg.getSubject();
			}
			if (msg.getFromName() != null && !msg.getFromName().isBlank()) {
				senderNames.add(msg.getFromName());
			}
			if (lastMessageTimestamp == null || msg.getInternalDate() > lastMessageTimestamp) {
				lastMessageTimestamp = msg.getInternalDate();
			}

			// create entity and publish event to save to mongodb and elasticsearch asynchronously
			MailMessage mailMessageEntity = mailMessageConverter.toMailMessage(msg);
			mailMessageEntity.setUserId(user.getId());
			SyncMailMessageEvent event = new SyncMailMessageEvent(this, mailMessageEntity);
			applicationEventPublisher.publishEvent(event);
			futures.add(event.getProcessingFuture());
		}

		Set<String> threadLabelIds = fullThread.getMessages().stream()
				.flatMap(m -> m.getLabelIds().stream())
				.collect(Collectors.toSet());

		// Convert and save thread
		MailThread mailThreadEntity = mailThreadConverter.toMailThread(fullThread);
		mailThreadEntity.setUserId(user.getId());
		mailThreadEntity.setLabelIds(threadLabelIds);
		mailThreadEntity.setSubject(subject);
		mailThreadEntity.setSenderNames(senderNames);
		mailThreadEntity.setMessageCount(messageCount);
		mailThreadEntity.setLastMessageTimestamp(lastMessageTimestamp);
		mailThreadRepository.save(mailThreadEntity);

		return futures;
	}

}
