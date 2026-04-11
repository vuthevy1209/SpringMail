package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.converters.MailMessageConverter;
import com.vuthevy1209.springmail.converters.MailThreadConverter;
import com.vuthevy1209.springmail.dto.mail.request.FetchOlderRequest;
import com.vuthevy1209.springmail.dto.mail.response.FetchOlderResponse;
import com.vuthevy1209.springmail.entity.MailMessage;
import com.vuthevy1209.springmail.entity.MailThread;
import com.vuthevy1209.springmail.entity.User;
import com.vuthevy1209.springmail.enums.MailLabel;
import com.vuthevy1209.springmail.enums.SyncStatus;
import com.vuthevy1209.springmail.exception.AppException;
import com.vuthevy1209.springmail.exception.ErrorCode;
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
import com.vuthevy1209.springmail.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailSyncServiceImpl implements MailSyncService {

	private final GmailService gmailService;

	private final UserRepository userRepository;
	private final MailThreadRepository mailThreadRepository;
	private final MailMessageRepository mailMessageRepository;

	private final MailThreadConverter mailThreadConverter;
	private final MailMessageConverter mailMessageConverter;

	private final String FORMAT = "full";
	private final Long QUANTITY_OF_THREAD_NEEDED_TO_SYNC = 200L;


	@Override
	public void syncMail(User user, String accessToken) throws IOException {
		boolean isFirstSync = user.getLastHistoryId() == null;
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
		GmailProfileDto profile = gmailService.getProfile(accessToken);
		Long lastHistoryId = profile.getHistoryId();

		// Step 2: Fetch the first page of threads
		GmailListThreadsResponseDto threadsResponse = gmailService.listThreads(accessToken, null, QUANTITY_OF_THREAD_NEEDED_TO_SYNC, null);

		if (threadsResponse.getThreads() != null) {
			for (GmailThreadDto threadMetadata : threadsResponse.getThreads()) {
				fetchAndProcessThread(user, accessToken, threadMetadata.getId());
			}
		}

		// Step 3: Update user state - save progress for pagination and anchor for incremental sync
		user.setLastHistoryId(lastHistoryId);
		user.setNextPageToken(threadsResponse.getNextPageToken());
		user.setSyncStatus(SyncStatus.COMPLETED);
		userRepository.save(user);

		log.info("Initial sync completed for user {} with historyId {}", user.getEmail(), lastHistoryId);
	}

	private void executeIncrementalSyncSequence(User user, String accessToken) throws IOException {
		log.info("Executing incremental sync for user {} from historyId {}", user.getEmail(), user.getLastHistoryId());

		try {
			String pageToken = null;
			Long newLastHistoryId = user.getLastHistoryId();

			do {
				GmailListHistoryResponseDto gmailListHistory = gmailService.listHistory(accessToken, user.getLastHistoryId().toString(), 100L, pageToken);

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

	private void fetchAndProcessThread(User user, String accessToken, String threadId) throws IOException {
		// Fetch full thread details
		GmailThreadDto fullThread = null;
		try {
			fullThread = gmailService.getThread(accessToken, threadId, FORMAT, null);
		} catch (IOException e) {
			if (e.getMessage() != null && e.getMessage().contains("404")) {
				log.info("Thread {} not found in Gmail. Deleting locally.", threadId);
				mailThreadRepository.deleteById(threadId);
				mailMessageRepository.deleteByThreadId(threadId);
				return;
			}
			throw e;
		}

		if (fullThread == null || fullThread.getMessages() == null) {
			return;
		}

		String subject = null;
		java.util.List<String> senderNames = new java.util.ArrayList<>();
		int messageCount = 0;
		Long lastMessageTimestamp = null;

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

			// Convert and save message
			MailMessage mailMessageEntity = mailMessageConverter.toMailMessage(msg);
			mailMessageEntity.setUserId(user.getId());
			mailMessageRepository.save(mailMessageEntity);
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
	}


	@Override
	public FetchOlderResponse fetchOlderThreads(FetchOlderRequest request) throws IOException {
		OAuth2User oauthUser = SecurityUtils.getCurrentOAuth2User();
		if (oauthUser == null) {
			throw new RuntimeException("Current user not found");
		}
		String userId = oauthUser.getAttribute("googleId");
		User user = userRepository.findById(userId).orElseThrow();
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

		log.info("Fetching older threads for user {}, query {}, pageToken {}", user.getEmail(), query, request.getPageToken());

		GmailListThreadsResponseDto response;
		try {
				response = gmailService.listThreads(accessToken, query, (long) maxResults, request.getPageToken());
		} catch (IOException e) {
				if (e.getMessage() != null && (e.getMessage().contains("401 Unauthorized") || e.getMessage().contains("401"))) {
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
}
