package com.vuthevy1209.springmail.converters;

import com.vuthevy1209.springmail.entity.MailThread;
import com.vuthevy1209.springmail.service.gmail.dto.message.GmailMessageDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailThreadDto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class GmailThreadConverter {

	/**
	 * Creates a new {@link MailThread} entity from a full {@link GmailThreadDto}.
	 */
	public MailThread toNewMailThread(GmailThreadDto dto, String userId) {
		MailThread thread = MailThread.builder()
				.id(dto.getId())
				.userId(userId)
				.historyId(dto.getHistoryId() != null ? dto.getHistoryId().toString() : null)
				.snippet(dto.getSnippet())
				.createdAt(Instant.now())
				.updatedAt(Instant.now())
				.build();

		applyDenormalizedFields(thread, dto.getMessages());
		return thread;
	}

	/**
	 * Applies updated fields from a {@link GmailThreadDto} onto an existing {@link MailThread} entity.
	 */
	public void updateMailThread(MailThread existing, GmailThreadDto dto) {
		existing.setSnippet(dto.getSnippet());
		if (dto.getHistoryId() != null) {
			existing.setHistoryId(dto.getHistoryId().toString());
		}
		existing.setUpdatedAt(Instant.now());

		applyDenormalizedFields(existing, dto.getMessages());
	}

	// -------------------------------------------------------------------------
	// Private helpers
	// -------------------------------------------------------------------------

	/**
	 * Populates denormalized display fields on the thread from its message list.
	 * Safe to call with a null or empty message list.
	 */
	private void applyDenormalizedFields(MailThread thread, List<GmailMessageDto> messages) {
		if (messages == null || messages.isEmpty()) {
			thread.setMessageCount(0);
			thread.setLabelIds(new HashSet<>());
			return;
		}

		thread.setMessageCount(messages.size());

		// Subject: from the first message that has a non-blank subject
		String subject = messages.stream()
				.filter(m -> m.getSubject() != null && !m.getSubject().isBlank())
				.map(GmailMessageDto::getSubject)
				.findFirst()
				.orElse("(No Subject)");
		thread.setSubject(subject);

		// Latest sender: from the message with the largest internalDate
		GmailMessageDto latest = messages.stream()
				.filter(m -> m.getInternalDate() != null)
				.max((a, b) -> Long.compare(a.getInternalDate(), b.getInternalDate()))
				.orElse(messages.get(messages.size() - 1));

		thread.setLatestSenderName(latest.getFromName());
		thread.setLatestSenderEmail(latest.getFromEmail());

		// LabelIds: union of all message labelIds
		Set<String> allLabels = new HashSet<>();
		for (GmailMessageDto msg : messages) {
			if (msg.getLabelIds() != null) {
				allLabels.addAll(msg.getLabelIds());
			}
		}
		thread.setLabelIds(allLabels);
	}
}

