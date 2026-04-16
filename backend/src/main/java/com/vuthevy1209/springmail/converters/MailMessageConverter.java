package com.vuthevy1209.springmail.converters;

import com.vuthevy1209.springmail.dto.mail.response.MailMessageResponse;
import com.vuthevy1209.springmail.dto.mail.response.MessageAttachmentResponse;
import com.vuthevy1209.springmail.entity.MailElasticSearch;
import com.vuthevy1209.springmail.entity.MailMessage;
import com.vuthevy1209.springmail.enums.MailLabel;
import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentDto;
import com.vuthevy1209.springmail.service.gmail.dto.message.GmailMessageDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MailMessageConverter {

	private final ModelMapper modelMapper;

	@PostConstruct
	public void init() {
		modelMapper.typeMap(GmailAttachmentDto.class, MessageAttachmentResponse.class)
				.addMapping(GmailAttachmentDto::getAttachmentId, MessageAttachmentResponse::setId);
	}

	public MailMessage toMailMessage(GmailMessageDto dto) {
		return modelMapper.map(dto, MailMessage.class);
	}

	public MailMessageResponse toMailMessageResponse(MailMessage mailMessage) {
		MailMessageResponse messageResponse = modelMapper.map(mailMessage, MailMessageResponse.class);

		boolean isUnread = mailMessage.getLabelIds() != null
				&& mailMessage.getLabelIds().contains(MailLabel.UNREAD.getId());

		messageResponse.setUnread(isUnread);
		return messageResponse;
	}

	public MailMessageResponse toMailMessageResponse(GmailMessageDto gmailMessageDto) {
		MailMessageResponse messageResponse = modelMapper.map(gmailMessageDto, MailMessageResponse.class);

		boolean isUnread = gmailMessageDto.getLabelIds() != null
				&& gmailMessageDto.getLabelIds().contains(MailLabel.UNREAD.getId());

		messageResponse.setUnread(isUnread);
		return messageResponse;
	}

	public List<MailMessageResponse> toMailMessageResponse(List<MailMessage> messages) {
		return messages.stream()
				.map(this::toMailMessageResponse)
				.toList();
	}

	public MailElasticSearch toMailElasticSearch(MailMessage mailMessage) {
		return  MailElasticSearch.builder()
				.id(mailMessage.getId())
				.threadId(mailMessage.getThreadId())
				.subject(mailMessage.getSubject())
				.bodyText(mailMessage.getBodyText())
				.sender(mailMessage.getFromName())
				.senderEmail(mailMessage.getFromEmail())
				.receiver(mailMessage.getToName())
				.receiverEmail(mailMessage.getToEmail())
				.build();
	}


}
