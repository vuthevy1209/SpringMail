package com.vuthevy1209.springmail.converters;

import com.vuthevy1209.springmail.entity.MailMessage;
import com.vuthevy1209.springmail.entity.MessageAttachment;
import com.vuthevy1209.springmail.service.gmail.dto.message.GmailMessageDto;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class GmailMessageConverter {

	/**
	 * Converts a {@link GmailMessageDto} to a {@link MailMessage} entity,
	 * including nested {@link MessageAttachment} entities.
	 */
	public MailMessage toMailMessage(GmailMessageDto dto, String userId) {
		Set<MessageAttachment> attachments = convertAttachments(dto);

		String from = buildDisplayAddress(dto.getFromName(), dto.getFromEmail());
		String to = buildDisplayAddress(dto.getToName(), dto.getToEmail());

		return MailMessage.builder()
				.id(dto.getId())
				.threadId(dto.getThreadId())
				.userId(userId)
				.labelIds(dto.getLabelIds() != null ? new HashSet<>(dto.getLabelIds()) : new HashSet<>())
				.snippet(dto.getSnippet())
				.subject(dto.getSubject())
				.from(from)
				.to(to)
				.internalDate(dto.getInternalDate())
				.historyId(dto.getHistoryId() != null ? dto.getHistoryId().toString() : null)
				.bodyHtml(dto.getBodyHtml())
				.attachments(attachments)
				.build();
	}

	// -------------------------------------------------------------------------
	// Private helpers
	// -------------------------------------------------------------------------

	private Set<MessageAttachment> convertAttachments(GmailMessageDto dto) {
		Set<MessageAttachment> attachments = new HashSet<>();
		if (dto.getAttachments() == null) {
			return attachments;
		}
		for (var att : dto.getAttachments()) {
			attachments.add(MessageAttachment.builder()
					.id(att.getAttachmentId())
					.messageId(dto.getId())
					.filename(att.getFilename())
					.mimeType(att.getMimeType())
					.size(att.getSize() != null ? att.getSize() : 0L)
					.contentId(att.getContentId())
					.build());
		}
		return attachments;
	}

	/**
	 * Returns {@code "Display Name <email>"} if name is present, otherwise just {@code "email"}.
	 */
	private String buildDisplayAddress(String name, String email) {
		if (name != null && !name.isEmpty()) {
			return name + " <" + email + ">";
		}
		return email;
	}
}
