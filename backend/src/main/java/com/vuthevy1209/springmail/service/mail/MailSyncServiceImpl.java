package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.converters.GmailMessageConverter;
import com.vuthevy1209.springmail.converters.GmailThreadConverter;
import com.vuthevy1209.springmail.entity.MailMessage;
import com.vuthevy1209.springmail.entity.MailThread;
import com.vuthevy1209.springmail.entity.User;
import com.vuthevy1209.springmail.repository.MailMessageRepository;
import com.vuthevy1209.springmail.repository.MailThreadRepository;
import com.vuthevy1209.springmail.repository.UserRepository;
import com.vuthevy1209.springmail.service.gmail.GmailService;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailHistoryDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailListHistoryResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailHistoryMessageAddedDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailHistoryMessageDeletedDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailHistoryLabelAddedDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailHistoryLabelRemovedDto;
import com.vuthevy1209.springmail.service.gmail.dto.message.GmailMessageDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailListThreadsResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailThreadDto;
import com.vuthevy1209.springmail.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailSyncServiceImpl implements MailSyncService {

	private final GmailService gmailService;
	private final MailThreadRepository threadRepository;
	private final MailMessageRepository messageRepository;
	private final UserRepository userRepository;
	private final GmailThreadConverter threadConverter;
	private final GmailMessageConverter messageConverter;

	// -------------------------------------------------------------------------
	// Full-sync throttle configuration
	// -------------------------------------------------------------------------

	/** Số trang tối đa được fetch trong một lần full sync (500 threads/trang → 50 * 500 = 25.000 threads max) */
	private static final int FULL_SYNC_MAX_PAGES = 50;

	/** Số milli-giây nghỉ giữa 2 request getThread() liên tiếp để tránh Gmail rate-limit (429) */
	private static final long THREAD_REQUEST_DELAY_MS = 50;

	/** Số milli-giây nghỉ sau mỗi batch (1 page list + toàn bộ getThread của batch đó) */
	private static final long BATCH_DELAY_MS = 500;

	@Override
	public void syncForUser() throws IOException {
		OAuth2User oauth2User = SecurityUtils.getCurrentOAuth2User();
		if (oauth2User == null) {
			throw new IOException("User not authenticated");
		}

		String email = oauth2User.getAttribute("email");
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IOException("User not found in database for email: " + email));

		syncForUser(user);
	}

	@Override
	public void syncForUser(User user) throws IOException {
		String accessToken = SecurityUtils.getAccessToken("google");
		if (accessToken == null) {
			log.error("No access token for user {}", user.getEmail());
			return;
		}

		syncMail(user, accessToken);
	}

	@Override
	public void syncMail(User user, String accessToken) throws IOException {
		if (user.getLastHistoryId() == null) {
			performFullSync(user, accessToken);
		} else {
			performIncrementalSync(user, accessToken);
		}
	}

	// -------------------------------------------------------------------------
	// Sync strategies
	// -------------------------------------------------------------------------

	private void performFullSync(User user, String accessToken) throws IOException {
		log.info("Performing full sync for user {} (max {} pages, ~{} threads)",
				user.getEmail(), FULL_SYNC_MAX_PAGES, FULL_SYNC_MAX_PAGES * 500);

		Long maxHistoryId = 0L;
		String pageToken = null;
		int totalThreads = 0;
		int page = 0;

		do {
			// --- Page limit guard ---
			if (page >= FULL_SYNC_MAX_PAGES) {
				log.warn("Full sync reached max page limit ({}) for user {}. Stopping early.",
						FULL_SYNC_MAX_PAGES, user.getEmail());
				break;
			}
			page++;

			// --- Fetch one page of thread IDs ---
			GmailListThreadsResponseDto response = gmailService.listThreads(accessToken, "in:inbox", 500L, pageToken);

			if (response.getThreads() == null || response.getThreads().isEmpty()) {
				break;
			}

			// --- Fetch full details for each thread in this page ---
			for (GmailThreadDto tSnippet : response.getThreads()) {
				try {
					GmailThreadDto fullThread = gmailService.getThread(accessToken, tSnippet.getId(), "full", null);
					saveThread(fullThread, user.getId());

					if (fullThread.getHistoryId() != null) {
						maxHistoryId = Math.max(maxHistoryId, fullThread.getHistoryId());
					}
				} catch (IOException e) {
					log.error("Failed to fetch thread {} during full sync, skipping", tSnippet.getId(), e);
				}

				// --- Rate limit: sleep between individual thread requests ---
				sleepQuietly(THREAD_REQUEST_DELAY_MS);
			}

			totalThreads += response.getThreads().size();
			pageToken = response.getNextPageToken();
			log.info("Full sync page {}/{} for user {}: {} threads fetched so far",
					page, FULL_SYNC_MAX_PAGES, user.getEmail(), totalThreads);

			// --- Rate limit: sleep between page batches ---
			if (pageToken != null) {
				sleepQuietly(BATCH_DELAY_MS);
			}

		} while (pageToken != null);

		log.info("Full sync completed for user {}: {} threads synced across {} pages",
				user.getEmail(), totalThreads, page);

		if (maxHistoryId > 0) {
			user.setLastHistoryId(maxHistoryId.toString());
			userRepository.save(user);
		}
	}


	private void performIncrementalSync(User user, String accessToken) throws IOException {
		log.info("Performing incremental sync for user {} starting from historyId {}", user.getEmail(), user.getLastHistoryId());
		try {
			GmailListHistoryResponseDto historyResponse = gmailService.listHistory(accessToken, user.getLastHistoryId(), 100L, null);

			if (historyResponse.getHistory() != null) {
				Set<String> threadIdsToUpdate = new HashSet<>();
				Set<String> messageIdsToDelete = new HashSet<>();

				for (GmailHistoryDto history : historyResponse.getHistory()) {
					if (history.getMessagesAdded() != null) {
						history.getMessagesAdded().forEach(m -> threadIdsToUpdate.add(m.getMessage().getThreadId()));
					}
					if (history.getLabelsAdded() != null) {
						history.getLabelsAdded().forEach(m -> threadIdsToUpdate.add(m.getMessage().getThreadId()));
					}
					if (history.getLabelsRemoved() != null) {
						history.getLabelsRemoved().forEach(m -> threadIdsToUpdate.add(m.getMessage().getThreadId()));
					}
					if (history.getMessagesDeleted() != null) {
						history.getMessagesDeleted().forEach(m -> {
							messageIdsToDelete.add(m.getMessage().getId());
							threadIdsToUpdate.add(m.getMessage().getThreadId());
						});
					}
				}

				// 1. Delete messages
				for (String msgId : messageIdsToDelete) {
					messageRepository.deleteByGoogleId(msgId);
				}

				// 2. Update/Sync threads
				for (String threadId : threadIdsToUpdate) {
					try {
						GmailThreadDto fullThread = gmailService.getThread(accessToken, threadId, "full", null);
						saveThread(fullThread, user.getId());
					} catch (IOException e) {
						// If thread is not found (deleted from Gmail), we should remove it locally
						log.warn("Thread {} not found or error during sync, cleaning up local records", threadId);
						messageRepository.deleteByThreadId(threadId);
						threadRepository.deleteById(threadId);
					}
				}

				// 3. Clean up empty threads (in case some messages remain but are not part of the thread anymore or were missed)
				for (String threadId : threadIdsToUpdate) {
					if (messageRepository.countByThreadId(threadId) == 0) {
						threadRepository.deleteById(threadId);
					}
				}

				log.info("Incremental sync processed {} threads and deleted {} messages for user {}",
						threadIdsToUpdate.size(), messageIdsToDelete.size(), user.getEmail());
			}

			if (historyResponse.getHistoryId() != null) {
				user.setLastHistoryId(historyResponse.getHistoryId().toString());
				userRepository.save(user);
			}
		} catch (IOException e) {
			if (e.getMessage().contains("404") || e.getMessage().contains("expired")) {
				log.warn("History expired for user {}, falling back to full sync", user.getEmail());
				performFullSync(user, accessToken);
			} else {
				throw e;
			}
		}
	}

	// -------------------------------------------------------------------------
	// Persistence
	// -------------------------------------------------------------------------

	private void saveThread(GmailThreadDto fullThread, String userId) {
		Optional<MailThread> existingThread = threadRepository.findById(fullThread.getId());

		MailThread mailThread;
		if (existingThread.isPresent()) {
			mailThread = existingThread.get();
			threadConverter.updateMailThread(mailThread, fullThread);
		} else {
			mailThread = threadConverter.toNewMailThread(fullThread, userId);
		}

		List<MailMessage> messages = new ArrayList<>();
		long lastTimestamp = 0;

		if (fullThread.getMessages() != null) {
			for (GmailMessageDto msg : fullThread.getMessages()) {
				messages.add(messageConverter.toMailMessage(msg, userId));
				if (msg.getInternalDate() != null && msg.getInternalDate() > lastTimestamp) {
					lastTimestamp = msg.getInternalDate();
				}
			}
		}

		mailThread.setLastMessageTimestamp(lastTimestamp);
		threadRepository.save(mailThread);
		messageRepository.saveAll(messages);
	}

	// -------------------------------------------------------------------------
	// Utilities
	// -------------------------------------------------------------------------

	/**
	 * Ngủ {@code ms} milli-giây; nếu bị interrupt thì restore flag và thoát sớm.
	 */
	private void sleepQuietly(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // restore interrupt flag
			log.debug("Full sync sleep interrupted");
		}
	}
}

