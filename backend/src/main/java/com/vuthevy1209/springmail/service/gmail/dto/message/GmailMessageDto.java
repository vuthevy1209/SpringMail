package com.vuthevy1209.springmail.service.gmail.dto.message;

import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GmailMessageDto {
	private String id;
	private String threadId;
	private String messageIdHeader; // RFC 2822 Message-ID
	private String snippet;
	private Long internalDate;
	private Long historyId;
	private List<String> labelIds;

	private String fromEmail;
	private String fromName;
	private String toEmail;
	private String toName;
	private List<String> cc;
	private List<String> bcc;
	private String subject;
	private String dateString;

	private String bodyHtml;
	private String bodyText;
	private Long sizeEstimate;
	private String raw;
	private List<GmailAttachmentDto> attachments;
}
