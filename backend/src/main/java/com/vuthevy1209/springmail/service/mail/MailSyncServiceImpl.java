package com.vuthevy1209.springmail.service.mail;

import com.google.api.services.gmail.model.*;
import com.vuthevy1209.springmail.entity.MailMessage;
import com.vuthevy1209.springmail.entity.MailThread;
import com.vuthevy1209.springmail.entity.User;
import com.vuthevy1209.springmail.repository.MailMessageRepository;
import com.vuthevy1209.springmail.repository.MailThreadRepository;
import com.vuthevy1209.springmail.repository.UserRepository;
import com.vuthevy1209.springmail.service.gmail.GmailClient;
import com.vuthevy1209.springmail.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailSyncServiceImpl implements MailSyncService {

	private final GmailClient gmailClient;
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
		ListThreadsResponse response = gmailClient.listThreads(accessToken, "in:inbox", 50L, null);
		
		if (response.getThreads() == null) {
			return;
		}

		BigInteger maxHistoryId = BigInteger.ZERO;

		for (com.google.api.services.gmail.model.Thread tSnippet : response.getThreads()) {
			com.google.api.services.gmail.model.Thread fullThread = gmailClient.getThread(accessToken, tSnippet.getId(), "full", null);
			
			saveThread(fullThread, user.getId());
			
			if (fullThread.getHistoryId() != null) {
				maxHistoryId = maxHistoryId.max(fullThread.getHistoryId());
			}
		}

		if (!maxHistoryId.equals(BigInteger.ZERO)) {
			user.setLastHistoryId(maxHistoryId.toString());
			userRepository.save(user);
		}
	}

	private void performIncrementalSync(User user, String accessToken) throws IOException {
		log.info("Performing incremental sync for user {} starting from historyId {}", user.getEmail(), user.getLastHistoryId());
		try {
			ListHistoryResponse historyResponse = gmailClient.listHistory(accessToken, user.getLastHistoryId(), 100L, null);
			
			if (historyResponse.getHistory() != null) {
				Set<String> threadIdsToUpdate = new HashSet<>();
				for (History history : historyResponse.getHistory()) {
					if (history.getMessagesAdded() != null) {
						history.getMessagesAdded().forEach(m -> threadIdsToUpdate.add(m.getMessage().getThreadId()));
					}
					if (history.getLabelsAdded() != null) {
						history.getLabelsAdded().forEach(m -> threadIdsToUpdate.add(m.getMessage().getThreadId()));
					}
					if (history.getLabelsRemoved() != null) {
						history.getLabelsRemoved().forEach(m -> threadIdsToUpdate.add(m.getMessage().getThreadId()));
					}
				}
				
				for (String threadId : threadIdsToUpdate) {
					try {
						com.google.api.services.gmail.model.Thread fullThread = gmailClient.getThread(accessToken, threadId, "full", null);
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

	private void saveThread(com.google.api.services.gmail.model.Thread fullThread, String userId) {
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
			for (Message msg : fullThread.getMessages()) {
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

	private MailMessage mapToEntity(Message msg, String userId) {
		String from = "";
		String to = "";
		String subject = "";
		
		if (msg.getPayload() != null && msg.getPayload().getHeaders() != null) {
			for (MessagePartHeader header : msg.getPayload().getHeaders()) {
				if ("From".equalsIgnoreCase(header.getName())) {
					from = header.getValue();
				} else if ("To".equalsIgnoreCase(header.getName())) {
					to = header.getValue();
				} else if ("Subject".equalsIgnoreCase(header.getName())) {
					subject = header.getValue();
				}
			}
		}

		return MailMessage.builder()
				.id(msg.getId())
				.threadId(msg.getThreadId())
				.userId(userId)
				.labelIds(msg.getLabelIds() != null ? new HashSet<>(msg.getLabelIds()) : new HashSet<>())
				.snippet(msg.getSnippet())
				.subject(subject)
				.from(from)
				.to(to)
				.internalDate(msg.getInternalDate())
				.historyId(msg.getHistoryId() != null ? msg.getHistoryId().toString() : null)
				.bodyHtml(getMessageBody(msg.getPayload()))
				.build();
	}

	private String getMessageBody(MessagePart part) {
		if (part == null) return "";
		
		String htmlContent = extractMimeTypeString(part, "text/html");
		if (htmlContent != null && !htmlContent.isEmpty()) {
			return htmlContent;
		}

		String plainContent = extractMimeTypeString(part, "text/plain");
		if (plainContent != null && !plainContent.isEmpty()) {
			return plainContent;
		}

		return "";
	}

	private String extractMimeTypeString(MessagePart part, String mimeType) {
		if (part.getMimeType() != null && part.getMimeType().contains(mimeType) && part.getBody() != null && part.getBody().getData() != null) {
			return new String(Base64.getUrlDecoder().decode(part.getBody().getData()));
		}

		if (part.getParts() != null) {
			for (MessagePart subPart : part.getParts()) {
				String result = extractMimeTypeString(subPart, mimeType);
				if (result != null && !result.isEmpty()) {
					return result;
				}
			}
		}
		return null;
	}
}
