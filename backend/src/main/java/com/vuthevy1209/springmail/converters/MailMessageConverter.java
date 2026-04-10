package com.vuthevy1209.springmail.converters;

import com.vuthevy1209.springmail.dto.response.mail.MailAttachmentResponse;
import com.vuthevy1209.springmail.dto.response.mail.MailResponse;
import com.vuthevy1209.springmail.entity.MailMessage;
import com.vuthevy1209.springmail.service.gmail.dto.message.GmailMessageDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MailMessageConverter {

	private final ModelMapper modelMapper;

	public MailMessage toMailMessage(GmailMessageDto dto) {
		return modelMapper.map(dto, MailMessage.class);
	}

	public MailResponse toMailResponse(GmailMessageDto message) {
		if (message == null) return null;

		List<MailAttachmentResponse> attachments = message.getAttachments() == null ? new ArrayList<>() :
				message.getAttachments().stream()
						.map(a -> new MailAttachmentResponse(
							a.getAttachmentId(),
							a.getFilename(),
							a.getMimeType(),
							a.getSize(),
							a.getContentId(),
							a.getData()
						))
						.collect(Collectors.toList());

		boolean unread = message.getLabelIds() != null && message.getLabelIds().contains("UNREAD");
		String from = message.getFromName() != null ? message.getFromName() + " <" + message.getFromEmail() + ">" : message.getFromEmail();
		String to = message.getToName() != null ? message.getToName() + " <" + message.getToEmail() + ">" : message.getToEmail();
		String content = message.getBodyHtml() != null && !message.getBodyHtml().isEmpty() ? message.getBodyHtml() : message.getBodyText();

		return new MailResponse(
				message.getId(),
				message.getThreadId(),
				from,
				to,
				message.getFromName(),
				message.getFromEmail(),
				message.getToName(),
				message.getToEmail(),
				message.getCc(),
				message.getBcc(),
				message.getSubject(),
				message.getDateString(),
				message.getSnippet(),
				content,
				message.getBodyHtml(),
				message.getBodyText(),
				unread,
				message.getInternalDate(),
				message.getSizeEstimate(),
				message.getHistoryId(),
				message.getLabelIds() == null ? Set.of() : Set.copyOf(message.getLabelIds()),
				attachments
		);
	}

	public MailResponse toMailResponse(MailMessage message) {
		if (message == null) return null;

		List<MailAttachmentResponse> attachments = message.getAttachments() == null ? new ArrayList<>() :
				message.getAttachments().stream()
						.map(a -> new MailAttachmentResponse(
							a.getAttachmentId(),
							a.getFilename(),
							a.getMimeType(),
							a.getSize(),
							a.getContentId(),
							a.getData()
						))
						.collect(Collectors.toList());

		boolean unread = message.getLabelIds() != null && message.getLabelIds().contains("UNREAD");
		String from = message.getFromName() != null ? message.getFromName() + " <" + message.getFromEmail() + ">" : message.getFromEmail();
		String to = message.getToName() != null ? message.getToName() + " <" + message.getToEmail() + ">" : message.getToEmail();
		String content = message.getBodyHtml() != null && !message.getBodyHtml().isEmpty() ? message.getBodyHtml() : message.getBodyText();

		return new MailResponse(
				message.getId(),
				message.getThreadId(),
				from,
				to,
				message.getFromName(),
				message.getFromEmail(),
				message.getToName(),
				message.getToEmail(),
				message.getCc(),
				message.getBcc(),
				message.getSubject(),
				formatTimestamp(message.getInternalDate()),
				message.getSnippet(),
				content,
				message.getBodyHtml(),
				message.getBodyText(),
				unread,
				message.getInternalDate(),
				message.getSizeEstimate(),
				null, // HistoryId not present in entity (or I missed it)
				message.getLabelIds() == null ? Set.of() : Set.copyOf(message.getLabelIds()),
				attachments
		);
	}

	public String formatTimestamp(long timestampMs) {
		Instant instant = Instant.ofEpochMilli(timestampMs);
		LocalDate today = LocalDate.now();
		LocalDate msgDate = instant
				.atZone(ZoneId.systemDefault())
				.toLocalDate();

		if (msgDate.equals(today)) {
			return DateTimeFormatter
					.ofPattern("HH:mm")
					.withZone(ZoneId.systemDefault())
					.format(instant);
		} else if (msgDate.getYear() == today.getYear()) {
			return DateTimeFormatter
					.ofPattern("d/M")
					.withZone(ZoneId.systemDefault())
					.format(instant);
		} else {
			return DateTimeFormatter
					.ofPattern("d/M/yyyy")
					.withZone(ZoneId.systemDefault())
					.format(instant);
		}
	}

	private String extractName(String raw) {
		if (raw == null) return null;
		int bracketIndex = raw.indexOf('<');
		if (bracketIndex != -1) {
			return raw.substring(0, bracketIndex).trim().replace("\"", "");
		}
		return raw;
	}

	private String extractEmail(String raw) {
		if (raw == null) return null;
		Pattern pattern = Pattern.compile("<(.*?)>");
		Matcher matcher = pattern.matcher(raw);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return raw;
	}
}
