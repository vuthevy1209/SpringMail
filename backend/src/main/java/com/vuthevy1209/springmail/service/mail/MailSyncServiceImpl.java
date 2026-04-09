package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.entity.*;
import com.vuthevy1209.springmail.repository.MailMessageRepository;
import com.vuthevy1209.springmail.repository.MailThreadRepository;
import com.vuthevy1209.springmail.repository.UserRepository;
import com.vuthevy1209.springmail.service.gmail.GmailService;
import com.vuthevy1209.springmail.service.gmail.GmailMapper;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailListHistoryResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.message.GmailMessageDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailListThreadsResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailThreadDto;
import com.vuthevy1209.springmail.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailSyncServiceImpl implements MailSyncService {

	private final GmailService gmailClient;
	private final MailThreadRepository threadRepository;
	private final MailMessageRepository messageRepository;
	private final UserRepository userRepository;

	@Override
	public void sync(User user) throws IOException {
		String accessToken = SecurityUtils.getAccessToken("google");
		if (accessToken == null) {
			log.error("No access token for user {}", user.getEmail());
			return;
		}

		if (user.getLastHistoryId() == null) {
			performFullSync(user, accessToken);
		} else {
			performIncrementalSync(user, accessToken);
		}
	}

	private void performFullSync(User user, String accessToken) throws IOException {
		log.info("Performing full sync for user {}", user.getEmail());
		GmailListThreadsResponseDto response = gmailClient.listThreads(accessToken, "in:inbox", 50L, null);
		
		if (response.getThreads() == null) {
			return;
		}

		Long maxHistoryId = 0L;

		for (GmailThreadDto tSnippet : response.getThreads()) {
			GmailThreadDto fullThread = gmailClient.getThread(accessToken, tSnippet.getId(), "full", null);
			
			saveThread(fullThread, user.getId());
			
			if (fullThread.getHistoryId() != null) {
				maxHistoryId = Math.max(maxHistoryId, fullThread.getHistoryId());
			}
		}

		if (maxHistoryId > 0) {
			user.setLastHistoryId(maxHistoryId.toString());
			userRepository.save(user);
		}
	}

	private void performIncrementalSync(User user, String accessToken) throws IOException {
		log.info("Performing incremental sync for user {} starting from historyId {}", user.getEmail(), user.getLastHistoryId());
		try {
			GmailListHistoryResponseDto historyResponse = gmailClient.listHistory(accessToken, user.getLastHistoryId(), 100L, null);
			
			if (historyResponse.getHistory() != null) {
				Set<String> threadIdsToUpdate = historyResponse.getHistory().stream()
						.flatMap(history -> {
							Set<String> ids = new HashSet<>();
							if (history.getMessagesAdded() != null) {
								history.getMessagesAdded().forEach(m -> ids.add(m.getMessage().getThreadId()));
							}
							if (history.getLabelsAdded() != null) {
								history.getLabelsAdded().forEach(m -> ids.add(m.getMessage().getThreadId()));
							}
							if (history.getLabelsRemoved() != null) {
								history.getLabelsRemoved().forEach(m -> ids.add(m.getMessage().getThreadId()));
							}
							return ids.stream();
						})
						.collect(Collectors.toSet());
				
				for (String threadId : threadIdsToUpdate) {
					try {
						GmailThreadDto fullThread = gmailClient.getThread(accessToken, threadId, "full", null);
						saveThread(fullThread, user.getId());
					} catch (IOException e) {
						log.error("Error fetching thread {} during history sync", threadId, e);
					}
				}
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

	private void saveThread(GmailThreadDto fullThread, String userId) {
		Optional<MailThread> existingThread = threadRepository.findById(fullThread.getId());
		
		MailThread mailThread;
		if (existingThread.isPresent()) {
			mailThread = existingThread.get();
			mailThread.setSnippet(fullThread.getSnippet());
			mailThread.setHistoryId(fullThread.getHistoryId() != null ? fullThread.getHistoryId().toString() : mailThread.getHistoryId());
			mailThread.setUpdatedAt(Instant.now());
		} else {
			mailThread = MailThread.builder()
					.id(fullThread.getId())
					.userId(userId)
					.historyId(fullThread.getHistoryId() != null ? fullThread.getHistoryId().toString() : null)
					.snippet(fullThread.getSnippet())
					.createdAt(Instant.now())
					.updatedAt(Instant.now())
					.build();
		}

		List<MailMessage> messages = new ArrayList<>();
		long lastTimestamp = 0;

		if (fullThread.getMessages() != null) {
			for (GmailMessageDto msg : fullThread.getMessages()) {
				MailMessage mailMsg = mapToEntity(msg, userId);
				messages.add(mailMsg);
				if (msg.getInternalDate() != null && msg.getInternalDate() > lastTimestamp) {
					lastTimestamp = msg.getInternalDate();
				}
			}
		}

		mailThread.setLastMessageTimestamp(lastTimestamp);
		threadRepository.save(mailThread);
		messageRepository.saveAll(messages);
	}

	private MailMessage mapToEntity(GmailMessageDto msg, String userId) {
		Set<MessageAttachment> attachments = new HashSet<>();
		if (msg.getAttachments() != null) {
			for (var attDto : msg.getAttachments()) {
				attachments.add(MessageAttachment.builder()
						.id(attDto.getAttachmentId())
						.messageId(msg.getId())
						.filename(attDto.getFilename())
						.mimeType(attDto.getMimeType())
						.size(attDto.getSize() != null ? attDto.getSize() : 0L)
						.contentId(attDto.getContentId())
						.build());
			}
		}

		return MailMessage.builder()
				.id(msg.getId())
				.threadId(msg.getThreadId())
				.userId(userId)
				.labelIds(msg.getLabelIds() != null ? new HashSet<>(msg.getLabelIds()) : new HashSet<>())
				.snippet(msg.getSnippet())
				.subject(msg.getSubject())
				.from(msg.getFromName() != null && !msg.getFromName().isEmpty()
						? msg.getFromName() + " <" + msg.getFromEmail() + ">"
						: msg.getFromEmail())
				.to(msg.getToName() != null && !msg.getToName().isEmpty()
						? msg.getToName() + " <" + msg.getToEmail() + ">"
						: msg.getToEmail())
				.internalDate(msg.getInternalDate())
				.historyId(msg.getHistoryId() != null ? msg.getHistoryId().toString() : null)
				.bodyHtml(msg.getBodyHtml())
				.attachments(attachments)
				.build();
	}
}
