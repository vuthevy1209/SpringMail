package com.vuthevy1209.springmail.converters;

import com.vuthevy1209.springmail.dto.mail.response.MailThreadResponse;
import com.vuthevy1209.springmail.entity.MailMessage;
import com.vuthevy1209.springmail.entity.MailThread;
import com.vuthevy1209.springmail.enums.MailLabel;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailThreadDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MailThreadConverter {

	private final ModelMapper modelMapper;
	private final MailMessageConverter mailMessageConverter;

	public MailThread toMailThread(GmailThreadDto dto) {
		return modelMapper.map(dto, MailThread.class);
	}

	public MailThreadResponse toMailThreadResponse(MailThread mailThread) {
		return toMailThreadResponse(mailThread, null);
	}

	public MailThreadResponse toMailThreadResponse(MailThread mailThread, List<MailMessage> messages) {
		MailThreadResponse response = modelMapper.map(mailThread, MailThreadResponse.class);

		boolean isUnread = mailThread.getLabelIds() != null
				&& mailThread.getLabelIds().contains(MailLabel.UNREAD.getId());

		response.setUnread(isUnread);
		response.setInternalDate(mailThread.getLastMessageTimestamp());

		if (messages != null && !messages.isEmpty()) {
			response.setMessages(mailMessageConverter.toMailMessageResponse(messages));
			if (!isUnread) {
				response.getMessages().forEach(m -> {
					if (m.getLabelIds() != null) {
						m.getLabelIds().remove(MailLabel.UNREAD.getId());
					}
				});
			} else {
				response.getMessages().forEach(m -> {
					if (m.getLabelIds() != null && !m.getLabelIds().contains(MailLabel.UNREAD.getId())) {
						m.getLabelIds().add(MailLabel.UNREAD.getId());
					}
				});
			}
		}

		return response;
	}
}
