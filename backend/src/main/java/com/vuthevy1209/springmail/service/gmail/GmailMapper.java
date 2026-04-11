package com.vuthevy1209.springmail.service.gmail;

import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.HistoryLabelAdded;
import com.google.api.services.gmail.model.HistoryLabelRemoved;
import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.HistoryMessageDeleted;
import com.google.api.services.gmail.model.ListHistoryResponse;
import com.google.api.services.gmail.model.ListThreadsResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.Profile;
import com.google.api.services.gmail.model.Thread;
import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentDto;
import com.vuthevy1209.springmail.service.gmail.dto.profile.GmailProfileDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailHistoryDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailHistoryLabelAddedDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailHistoryLabelRemovedDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailHistoryMessageAddedDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailHistoryMessageDeletedDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailListHistoryResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.message.GmailMessageDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailListThreadsResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailThreadDto;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GmailMapper {
 
	public static GmailProfileDto toGmailProfileDto(Profile profile) {
		if (profile == null) {
			return null;
		}
		GmailProfileDto dto = new GmailProfileDto();
		dto.setEmailAddress(profile.getEmailAddress());
		dto.setMessagesTotal(profile.getMessagesTotal() != null ? profile.getMessagesTotal().longValue() : null);
		dto.setThreadsTotal(profile.getThreadsTotal() != null ? profile.getThreadsTotal().longValue() : null);
		dto.setHistoryId(profile.getHistoryId() != null ? profile.getHistoryId().longValue() : null);
		return dto;
	}

	// verified
	public static GmailListThreadsResponseDto toGmailListThreadsResponseDto(ListThreadsResponse response) {
		if (response == null) {
			return null;
		}
		return GmailListThreadsResponseDto.builder()
				.threads(response.getThreads() != null ? response.getThreads().stream()
						.map(GmailMapper::toGmailThreadDto)
						.collect(Collectors.toList()) : null)
				.nextPageToken(response.getNextPageToken())
				.resultSizeEstimate(response.getResultSizeEstimate())
				.build();
	}

	// verified
	public static GmailThreadDto toGmailThreadDto(Thread thread) {
		if (thread == null) {
			return null;
		}
		return GmailThreadDto.builder()
				.id(thread.getId())
				.snippet(thread.getSnippet())
				.historyId(thread.getHistoryId() != null ? thread.getHistoryId().longValue() : null)
				.messages(thread.getMessages() != null ? thread.getMessages().stream()
						.map(GmailMapper::toGmailMessageDto)
						.collect(Collectors.toList()) : null)
				.build();
	}

	// verified
	public static GmailMessageDto toGmailMessageDto(Message message) {
		if (message == null) {
			return null;
		}
		GmailMessageDto dto = GmailMessageDto.builder()
				.id(message.getId())
				.threadId(message.getThreadId())
				.snippet(message.getSnippet())
				.internalDate(message.getInternalDate())
				.historyId(message.getHistoryId() != null ? message.getHistoryId().longValue() : null)
				.labelIds(message.getLabelIds())
				.sizeEstimate(message.getSizeEstimate() != null ? message.getSizeEstimate().longValue() : null)
				.raw(message.getRaw())
				.build();

		if (message.getPayload() == null) {
			return dto;
		}

		// extract headers, body, and attachments
		StringBuilder htmlBuilder = new StringBuilder();
		StringBuilder textBuilder = new StringBuilder();
		List<GmailAttachmentDto> attachments = new ArrayList<>();

		extractHeader(message, dto);
		extractBodyAndAttachment(message.getPayload(), htmlBuilder, textBuilder, attachments);

		dto.setBodyHtml(htmlBuilder.toString());
		dto.setBodyText(textBuilder.toString());
		dto.setAttachments(attachments);

		return dto;
	}

	// verified
	private static void extractHeader(Message message, GmailMessageDto dto) {
		if (message.getPayload().getHeaders() == null) {
			return;
		}

		for (MessagePartHeader header : message.getPayload().getHeaders()) {
			String name = header.getName();
			String value = header.getValue();

			if ("From".equalsIgnoreCase(name)) {
				dto.setFromName(extractName(value));
				dto.setFromEmail(extractEmail(value));
			} else if ("To".equalsIgnoreCase(name)) {
				dto.setToName(extractName(value));
				dto.setToEmail(extractEmail(value));
			} else if ("Cc".equalsIgnoreCase(name)) {
				dto.setCc(extractEmailList(value));
			} else if ("Bcc".equalsIgnoreCase(name)) {
				dto.setBcc(extractEmailList(value));
			} else if ("Subject".equalsIgnoreCase(name)) {
				dto.setSubject(value);
			} else if ("Date".equalsIgnoreCase(name)) {
				dto.setDateString(value);
			}
		}
	}

	private static String extractName(String raw) {
		if (raw == null) return null;
		int bracketIndex = raw.indexOf('<');
		if (bracketIndex != -1) {
			return raw.substring(0, bracketIndex).trim().replace("\"", "");
		}
		return raw;
	}

	private static String extractEmail(String raw) {
		if (raw == null) return null;
		Pattern pattern = Pattern.compile("<(.*?)>");
		Matcher matcher = pattern.matcher(raw);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return raw;
	}

	private static List<String> extractEmailList(String raw) {
		if (raw == null || raw.isEmpty()) return Collections.emptyList();
		String[] parts = raw.split(",");
		List<String> emails = new ArrayList<>();
		for (String part : parts) {
			emails.add(extractEmail(part.trim()));
		}
		return emails;
	}

	// verified
	private static void extractBodyAndAttachment(MessagePart part, StringBuilder htmlBuilder, StringBuilder textBuilder, List<GmailAttachmentDto> attachments) {
		if (part == null) return;

		String mimeType = part.getMimeType();
		String filename = part.getFilename();
		MessagePartBody body = part.getBody();

		// Attachment or Inline Image check
		String cid = getContentId(part);
		boolean isInline = cid != null;
		boolean hasBody = body != null && (body.getAttachmentId() != null || body.getData() != null);
		boolean isAttachment = filename != null && !filename.isEmpty() && hasBody;

		if (isAttachment || (isInline && hasBody)) {
			attachments.add(GmailAttachmentDto.builder()
					.attachmentId(body != null ? body.getAttachmentId() : null)
					.filename(filename)
					.mimeType(mimeType)
					.size(body != null && body.getSize() != null ? body.getSize().longValue() : null)
					.contentId(cid)
					.data(body != null ? body.getData() : null)
					.build());
		}

		// Body content
		if (body != null && body.getData() != null) {
			try {
				byte[] decodedBytes = Base64.getUrlDecoder().decode(body.getData());
				String decodedData = new String(decodedBytes, StandardCharsets.UTF_8);
				if ("text/html".equalsIgnoreCase(mimeType)) {
					htmlBuilder.append(decodedData);
				} else if ("text/plain".equalsIgnoreCase(mimeType)) {
					textBuilder.append(decodedData);
				}
			} catch (Exception e) {
				// Log or ignore malformed data
			}
		}

		// Recursive for multipart
		if (part.getParts() != null) {
			for (MessagePart subPart : part.getParts()) {
				extractBodyAndAttachment(subPart, htmlBuilder, textBuilder, attachments);
			}
		}
	}

	// verified
	private static String getContentId(MessagePart part) {
		if (part.getHeaders() == null) return null;
		for (MessagePartHeader header : part.getHeaders()) {
			if ("Content-ID".equalsIgnoreCase(header.getName())) {
				String value = header.getValue();
				if (value != null) {
					// Remove angle brackets if present
					if (value.startsWith("<") && value.endsWith(">")) {
						return value.substring(1, value.length() - 1);
					}
					return value;
				}
			}
		}
		return null;
	}
	// verified
	public static GmailAttachmentDto toGmailAttachmentDto(MessagePartBody body, String filename, String mimeType) {
		if (body == null) {
			return null;
		}

		return GmailAttachmentDto.builder()
				.attachmentId(body.getAttachmentId())
				.filename(filename)
				.mimeType(mimeType)
				.size(body.getSize() != null ? body.getSize().longValue() : null)
				.data(body.getData())
				.build();
	}


    // verified
	public static GmailListHistoryResponseDto toGmailListHistoryResponseDto(ListHistoryResponse response) {
		if (response == null) {
			return null;
		}
		return GmailListHistoryResponseDto.builder()
				.history(response.getHistory() != null ? response.getHistory().stream()
						.map(GmailMapper::toGmailHistoryDto)
						.collect(Collectors.toList()) : null)
				.nextPageToken(response.getNextPageToken())
				.historyId(response.getHistoryId() != null ? response.getHistoryId().longValue() : null)
				.build();
	}

	public static GmailHistoryDto toGmailHistoryDto(History history) {
		if (history == null) {
			return null;
		}
		return GmailHistoryDto.builder()
				.id(history.getId() != null ? history.getId().toString() : null)
				.messages(history.getMessages() != null ? history.getMessages().stream()
						.map(GmailMapper::toGmailMessageDto)
						.collect(Collectors.toList()) : null)
				.messagesAdded(history.getMessagesAdded() != null ? history.getMessagesAdded().stream()
						.map(GmailMapper::toGmailHistoryMessageAddedDto)
						.collect(Collectors.toList()) : null)
				.messagesDeleted(history.getMessagesDeleted() != null ? history.getMessagesDeleted().stream()
						.map(GmailMapper::toGmailHistoryMessageDeletedDto)
						.collect(Collectors.toList()) : null)
				.labelsAdded(history.getLabelsAdded() != null ? history.getLabelsAdded().stream()
						.map(GmailMapper::toGmailHistoryLabelAddedDto)
						.collect(Collectors.toList()) : null)
				.labelsRemoved(history.getLabelsRemoved() != null ? history.getLabelsRemoved().stream()
						.map(GmailMapper::toGmailHistoryLabelRemovedDto)
						.collect(Collectors.toList()) : null)
				.build();
	}

	private static GmailHistoryMessageAddedDto toGmailHistoryMessageAddedDto(HistoryMessageAdded event) {
		if (event == null) {
			return null;
		}
		return GmailHistoryMessageAddedDto.builder()
				.message(toGmailMessageDto(event.getMessage()))
				.build();
	}

	private static GmailHistoryMessageDeletedDto toGmailHistoryMessageDeletedDto(HistoryMessageDeleted event) {
		if (event == null) {
			return null;
		}
		return GmailHistoryMessageDeletedDto.builder()
				.message(toGmailMessageDto(event.getMessage()))
				.build();
	}

	private static GmailHistoryLabelAddedDto toGmailHistoryLabelAddedDto(HistoryLabelAdded event) {
		if (event == null) {
			return null;
		}
		return GmailHistoryLabelAddedDto.builder()
				.message(toGmailMessageDto(event.getMessage()))
				.labelIds(event.getLabelIds())
				.build();
	}

	private static GmailHistoryLabelRemovedDto toGmailHistoryLabelRemovedDto(HistoryLabelRemoved event) {
		if (event == null) {
			return null;
		}
		return GmailHistoryLabelRemovedDto.builder()
				.message(toGmailMessageDto(event.getMessage()))
				.labelIds(event.getLabelIds())
				.build();
	}
}
