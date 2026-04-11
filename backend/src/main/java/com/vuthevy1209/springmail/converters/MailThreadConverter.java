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
		}

		return response;
	}

	public MailThreadResponse toMailThreadResponse(GmailThreadDto gmailThreadDto) {
		MailThreadResponse response = modelMapper.map(gmailThreadDto, MailThreadResponse.class);
		
		boolean isUnread = gmailThreadDto.getMessages() != null && gmailThreadDto.getMessages().stream()
				.anyMatch(m -> m.getLabelIds() != null && m.getLabelIds().contains(MailLabel.UNREAD.getId()));
		
		response.setUnread(isUnread);
		
		if (gmailThreadDto.getMessages() != null) {
			response.setMessages(gmailThreadDto.getMessages().stream()
					.map(mailMessageConverter::toMailMessageResponse)
					.collect(Collectors.toList()));
					
			if (!gmailThreadDto.getMessages().isEmpty()) {
				response.setInternalDate(gmailThreadDto.getMessages().get(gmailThreadDto.getMessages().size() - 1).getInternalDate());
			}
		}
		
		return response;
	}
}
