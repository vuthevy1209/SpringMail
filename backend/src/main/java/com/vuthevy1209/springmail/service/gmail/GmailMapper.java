package com.vuthevy1209.springmail.service.gmail;

import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.model.Thread;
import com.vuthevy1209.springmail.dto.response.mail.MailAttachmentResponse;
import com.vuthevy1209.springmail.dto.response.mail.MailResponse;
import com.vuthevy1209.springmail.dto.response.mail.MailThreadResponse;
import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentBodyDto;
import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.*;
import com.vuthevy1209.springmail.service.gmail.dto.message.*;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailListThreadsResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailThreadDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Base64;
import java.util.stream.Collectors;

@Component
public class GmailMapper {

	public GmailListThreadsResponseDto toDto(ListThreadsResponse response) {
		if (response == null) return null;
		return enrich(GmailListThreadsResponseDto.builder()
				.threads(response.getThreads() != null ? response.getThreads().stream().map(this::toDto).collect(Collectors.toList()) : null)
				.nextPageToken(response.getNextPageToken())
				.resultSizeEstimate(response.getResultSizeEstimate() != null ? response.getResultSizeEstimate().longValue() : null)
				.build());
	}

	public GmailThreadDto toDto(Thread thread) {
		if (thread == null) return null;
		return enrich(GmailThreadDto.builder()
				.id(thread.getId())
				.snippet(thread.getSnippet())
				.historyId(thread.getHistoryId() != null ? thread.getHistoryId().longValue() : null)
				.messages(thread.getMessages() != null ? thread.getMessages().stream().map(this::toDto).collect(Collectors.toList()) : null)
				.build());
	}

	public GmailMessageDto toDto(Message message) {
		if (message == null) return null;
		GmailMessageDto dto = GmailMessageDto.builder()
				.id(message.getId())
				.threadId(message.getThreadId())
				.labelIds(message.getLabelIds())
				.snippet(message.getSnippet())
				.historyId(message.getHistoryId() != null ? message.getHistoryId().longValue() : null)
				.internalDate(message.getInternalDate() != null ? message.getInternalDate() : null)
				.payload(toDto(message.getPayload()))
				.sizeEstimate(message.getSizeEstimate() != null ? message.getSizeEstimate().longValue() : null)
				.raw(message.getRaw())
				.build();
		return enrich(dto);
	}

	public GmailMessagePartDto toDto(MessagePart part) {
		if (part == null) return null;
		return GmailMessagePartDto.builder()
				.partId(part.getPartId())
				.mimeType(part.getMimeType())
				.filename(part.getFilename())
				.headers(part.getHeaders() != null ? part.getHeaders().stream().map(this::toDto).collect(Collectors.toList()) : null)
				.body(toDto(part.getBody()))
				.parts(part.getParts() != null ? part.getParts().stream().map(this::toDto).collect(Collectors.toList()) : null)
				.build();
	}

	public GmailHeaderDto toDto(MessagePartHeader header) {
		if (header == null) return null;
		return GmailHeaderDto.builder()
				.name(header.getName())
				.value(header.getValue())
				.build();
	}

	public GmailMessageBodyDto toDto(MessagePartBody body) {
		if (body == null) return null;
		return GmailMessageBodyDto.builder()
				.attachmentId(body.getAttachmentId())
				.size(body.getSize())
				.data(body.getData())
				.build();
	}

	public GmailAttachmentBodyDto toDto(MessagePartBody body, boolean isAttachment) {
		if (body == null) return null;
		return GmailAttachmentBodyDto.builder()
				.attachmentId(body.getAttachmentId())
				.size(body.getSize())
				.data(body.getData())
				.build();
	}

	public GmailListHistoryResponseDto toDto(ListHistoryResponse response) {
		if (response == null) return null;
		return enrich(GmailListHistoryResponseDto.builder()
				.history(response.getHistory() != null ? response.getHistory().stream().map(this::toDto).collect(Collectors.toList()) : null)
				.nextPageToken(response.getNextPageToken())
				.historyId(response.getHistoryId() != null ? response.getHistoryId().longValue() : null)
				.build());
	}

	public GmailHistoryDto toDto(History history) {
		if (history == null) return null;
		return GmailHistoryDto.builder()
				.id(history.getId() != null ? history.getId().toString() : null)
				.messages(history.getMessages() != null ? history.getMessages().stream().map(this::toDto).collect(Collectors.toList()) : null)
				.messagesAdded(history.getMessagesAdded() != null ? history.getMessagesAdded().stream().map(this::toDto).collect(Collectors.toList()) : null)
				.messagesDeleted(history.getMessagesDeleted() != null ? history.getMessagesDeleted().stream().map(this::toDto).collect(Collectors.toList()) : null)
				.labelsAdded(history.getLabelsAdded() != null ? history.getLabelsAdded().stream().map(this::toDto).collect(Collectors.toList()) : null)
				.labelsRemoved(history.getLabelsRemoved() != null ? history.getLabelsRemoved().stream().map(this::toDto).collect(Collectors.toList()) : null)
				.build();
	}

	public GmailHistoryMessageAddedDto toDto(HistoryMessageAdded hma) {
		if (hma == null) return null;
		return GmailHistoryMessageAddedDto.builder()
				.message(toDto(hma.getMessage()))
				.build();
	}

	public GmailHistoryMessageDeletedDto toDto(HistoryMessageDeleted hmd) {
		if (hmd == null) return null;
		return GmailHistoryMessageDeletedDto.builder()
				.message(toDto(hmd.getMessage()))
				.build();
	}

	public GmailHistoryLabelAddedDto toDto(HistoryLabelAdded hla) {
		if (hla == null) return null;
		return GmailHistoryLabelAddedDto.builder()
				.message(toDto(hla.getMessage()))
				.labelIds(hla.getLabelIds())
				.build();
	}

	public GmailHistoryLabelRemovedDto toDto(HistoryLabelRemoved hlr) {
		if (hlr == null) return null;
		return GmailHistoryLabelRemovedDto.builder()
				.message(toDto(hlr.getMessage()))
				.labelIds(hlr.getLabelIds())
				.build();
	}

	public GmailListThreadsResponseDto enrich(GmailListThreadsResponseDto response) {
		if (response == null || response.getThreads() == null) return response;

		response.getThreads().forEach(this::enrich);

		return response;
	}

	public GmailThreadDto enrich(GmailThreadDto thread) {
		if (thread == null || thread.getMessages() == null) return thread;

		thread.getMessages().forEach(this::enrich);

		return thread;
	}

	public GmailMessageDto enrich(GmailMessageDto message) {
		if (message == null) return null;

		if (message.getPayload() != null) {
			mapPayload(message.getPayload(), message);
		}

		return message;
	}


	public GmailListHistoryResponseDto enrich(GmailListHistoryResponseDto response) {
		if (response == null || response.getHistory() == null) return response;

		response.getHistory().forEach(h -> {
			if (h.getMessages() != null) {
				h.getMessages().forEach(this::enrich);
			}
			if (h.getMessagesAdded() != null) {
				h.getMessagesAdded().forEach(ma -> enrich(ma.getMessage()));
			}
			if (h.getMessagesDeleted() != null) {
				h.getMessagesDeleted().forEach(md -> enrich(md.getMessage()));
			}
			if (h.getLabelsAdded() != null) {
				h.getLabelsAdded().forEach(la -> enrich(la.getMessage()));
			}
			if (h.getLabelsRemoved() != null) {
				h.getLabelsRemoved().forEach(lr -> enrich(lr.getMessage()));
			}
		});

		return response;
	}

	private void mapPayload(GmailMessagePartDto payload, GmailMessageDto message) {
		if (payload.getHeaders() != null) {
			for (GmailHeaderDto header : payload.getHeaders()) {
				String name = header.getName();
				String value = header.getValue();

				if ("From".equalsIgnoreCase(name)) {
					message.setFromEmail(extractEmail(value));
					message.setFromName(extractName(value));
				} else if ("To".equalsIgnoreCase(name)) {
					message.setToEmail(extractEmail(value));
					message.setToName(extractName(value));
				} else if ("Subject".equalsIgnoreCase(name)) {
					message.setSubject(value);
				} else if ("Date".equalsIgnoreCase(name)) {
					message.setDateString(value);
				} else if ("Cc".equalsIgnoreCase(name)) {
					message.setCc(parseEmailList(value));
				} else if ("Bcc".equalsIgnoreCase(name)) {
					message.setBcc(parseEmailList(value));
				}
			}
		}

		message.setBodyHtml(extractContent(payload, "text/html"));
		message.setBodyText(extractContent(payload, "text/plain"));

		List<GmailAttachmentDto> attachments = new ArrayList<>();
		collectAttachments(payload, attachments);
		message.setAttachments(attachments);
	}

	private String extractContent(GmailMessagePartDto part, String mimeType) {
		if (part.getMimeType().equalsIgnoreCase(mimeType) && part.getBody() != null && part.getBody().getData() != null) {
			return decodeBase64(part.getBody().getData());
		}

		if (part.getParts() != null) {
			for (GmailMessagePartDto subPart : part.getParts()) {
				String content = extractContent(subPart, mimeType);
				if (content != null) return content;
			}
		}
		return null;
	}

	private void collectAttachments(GmailMessagePartDto part, List<GmailAttachmentDto> attachments) {
		if (part.getFilename() != null && !part.getFilename().isEmpty()) {
			attachments.add(GmailAttachmentDto.builder()
					.id(part.getBody() != null ? part.getBody().getAttachmentId() : null)
					.filename(part.getFilename())
					.mimeType(part.getMimeType())
					.size(part.getBody() != null && part.getBody().getSize() != null ? part.getBody().getSize().longValue() : null)
					.build());
		}

		if (part.getParts() != null) {
			for (GmailMessagePartDto subPart : part.getParts()) {
				collectAttachments(subPart, attachments);
			}
		}
	}

	private String decodeBase64(String data) {
		try {
			return new String(Base64.getUrlDecoder().decode(data));
		} catch (Exception e) {
			return data; // Return as is if failed
		}
	}

	public MailResponse toMailResponse(GmailMessageDto message) {
		if (message == null) return null;
		
		boolean isUnread = message.getLabelIds() != null && message.getLabelIds().contains("UNREAD");
		
		return new MailResponse(
				message.getId(),
				message.getFromEmail(), // Or combine with name if needed
				message.getToEmail(),
				message.getFromName(),
				message.getFromEmail(),
				message.getSubject(),
				message.getDateString(),
				message.getSnippet(),
				message.getBodyHtml() != null ? message.getBodyHtml() : message.getBodyText(),
				isUnread,
				message.getInternalDate(),
				message.getAttachments() != null ? 
						message.getAttachments().stream().map(this::toMailAttachmentResponse).collect(Collectors.toList()) : 
						Collections.emptyList()
		);
	}

	public MailThreadResponse toMailThreadResponse(GmailThreadDto thread) {
		if (thread == null) return null;

		String subject = "(No Subject)";
		String latestDate = "";
		String latestSenderName = "";
		Long internalDate = 0L;
		boolean isUnread = false;
		int messageCount = 0;
		List<MailResponse> messages = Collections.emptyList();

		if (thread.getMessages() != null && !thread.getMessages().isEmpty()) {
			messageCount = thread.getMessages().size();
			
			// Subject usually comes from the first message
			GmailMessageDto firstMsg = thread.getMessages().get(0);
			if (firstMsg.getSubject() != null && !firstMsg.getSubject().isEmpty()) {
				subject = firstMsg.getSubject();
			}

			// Latest info comes from the last message
			GmailMessageDto lastMsg = thread.getMessages().get(messageCount - 1);
			latestDate = lastMsg.getDateString();
			latestSenderName = lastMsg.getFromName();
			internalDate = lastMsg.getInternalDate();

			// Thread is unread if any message is unread
			isUnread = thread.getMessages().stream()
					.anyMatch(m -> m.getLabelIds() != null && m.getLabelIds().contains("UNREAD"));

			messages = thread.getMessages().stream()
					.map(this::toMailResponse)
					.collect(Collectors.toList());
		}

		return new MailThreadResponse(
				thread.getId(),
				subject,
				thread.getSnippet(),
				latestDate,
				latestSenderName,
				isUnread,
				messageCount,
				internalDate,
				messages
		);
	}

	public MailAttachmentResponse toMailAttachmentResponse(GmailAttachmentDto attachment) {
		if (attachment == null) return null;
		return new MailAttachmentResponse(
				attachment.getId(),
				attachment.getFilename(),
				attachment.getMimeType(),
				null // contentId if available
		);
	}

	private String extractEmail(String headerValue) {
		if (headerValue == null) return null;
		int start = headerValue.indexOf('<');
		int end = headerValue.indexOf('>');
		if (start != -1 && end != -1) {
			return headerValue.substring(start + 1, end).trim();
		}
		return headerValue.trim();
	}

	private String extractName(String headerValue) {
		if (headerValue == null) return null;
		int start = headerValue.indexOf('<');
		if (start != -1) {
			return headerValue.substring(0, start).replace("\"", "").trim();
		}
		return null;
	}

	private List<String> parseEmailList(String headerValue) {
		if (headerValue == null) return Collections.emptyList();
		List<String> emails = new ArrayList<>();
		String[] parts = headerValue.split(",");
		for (String part : parts) {
			emails.add(extractEmail(part.trim()));
		}
		return emails;
	}
}
