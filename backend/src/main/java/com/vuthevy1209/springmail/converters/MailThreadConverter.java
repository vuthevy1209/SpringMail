package com.vuthevy1209.springmail.converters;

import com.vuthevy1209.springmail.dto.response.mail.MailResponse;
import com.vuthevy1209.springmail.dto.response.mail.MailThreadResponse;
import com.vuthevy1209.springmail.entity.MailMessage;
import com.vuthevy1209.springmail.entity.MailThread;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailThreadDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MailThreadConverter {

	private final ModelMapper modelMapper;
	private final MailMessageConverter mailMessageConverter;

	public MailThread toMailThread(GmailThreadDto dto) {
		return modelMapper.map(dto, MailThread.class);
	}

	public MailThreadResponse toMailThreadResponse(GmailThreadDto thread) {
		if (thread == null) return null;

		List<MailResponse> messages = thread.getMessages() == null ? new ArrayList<>() :
				thread.getMessages().stream()
						.map(mailMessageConverter::toMailResponse)
						.collect(Collectors.toList());

		String latestSubject = messages.isEmpty() ? "" : messages.get(messages.size() - 1).subject();
		String latestSenderName = messages.isEmpty() ? "" : messages.get(messages.size() - 1).fromName();
		String latestDate = messages.isEmpty() ? "" : messages.get(messages.size() - 1).date();
		List<String> senderNames = messages.stream()
				.map(MailResponse::fromName)
				.distinct()
				.collect(Collectors.toList());
		boolean unread = thread.getMessages() != null && thread.getMessages().stream()
				.anyMatch(m -> m.getLabelIds() != null && m.getLabelIds().contains("UNREAD"));

		Long latestInternalDate = messages.isEmpty() ? null : messages.get(messages.size() - 1).internalDate();

		return new MailThreadResponse(
				thread.getId(),
				latestSubject,
				thread.getSnippet(),
				latestDate,
				latestSenderName,
				senderNames,
				unread,
				thread.getMessages() == null ? 0 : thread.getMessages().size(),
				latestInternalDate,
				latestInternalDate,
				thread.getHistoryId(),
				thread.getMessages() == null ? Set.of() : thread.getMessages().stream()
						.flatMap(m -> m.getLabelIds() == null ? java.util.stream.Stream.empty() : m.getLabelIds().stream())
						.collect(Collectors.toSet()),
				messages
		);
	}

	public MailThreadResponse toMailThreadResponse(MailThread thread, List<MailMessage> messages) {
		if (thread == null) return null;

		List<MailResponse> messageResponses = messages == null ? new ArrayList<>() :
				messages.stream()
						.map(mailMessageConverter::toMailResponse)
						.collect(Collectors.toList());

		String latestSenderName = (thread.getSenderNames() != null && !thread.getSenderNames().isEmpty()) ? thread.getSenderNames().get(0) : "";
		String latestDate = thread.getLastMessageTimestamp() != null ? mailMessageConverter.formatTimestamp(thread.getLastMessageTimestamp()) : "";
		boolean unread = thread.getLabelIds() != null && thread.getLabelIds().contains("UNREAD");

		return new MailThreadResponse(
				thread.getId(),
				thread.getSubject(),
				thread.getSnippet(),
				latestDate,
				latestSenderName,
				thread.getSenderNames(),
				unread,
				thread.getMessageCount(),
				thread.getLastMessageTimestamp(),
				thread.getLastMessageTimestamp(),
				thread.getHistoryId(),
				thread.getLabelIds() == null ? Set.of() : Set.copyOf(thread.getLabelIds()),
				messageResponses
		);
	}

	public MailThreadResponse toShallowResponse(MailThreadResponse source) {
		if (source == null) return null;
		return new MailThreadResponse(
				source.id(),
				source.subject(),
				source.snippet(),
				source.latestDate(),
				source.latestSenderName(),
				source.senderNames(),
				source.unread(),
				source.messageCount(),
				source.internalDate(),
				source.lastMessageTimestamp(),
				source.historyId(),
				source.labelIds(),
				new ArrayList<>()
		);
	}
}
