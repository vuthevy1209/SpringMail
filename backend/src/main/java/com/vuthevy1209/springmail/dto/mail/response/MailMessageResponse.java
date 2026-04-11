package com.vuthevy1209.springmail.dto.mail.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailMessageResponse {
	private String id;
	private String threadId;
	private String snippet;
	private Long internalDate;
	private Long historyId;
	private Set<String> labelIds;

	private String fromName;
	private String fromEmail;
	private String toName;
	private String toEmail;
	private List<String> cc;
	private List<String> bcc;
	private String subject;
	private String dateString;

	private String bodyHtml;
	private String bodyText;
	private Long sizeEstimate;
	private List<MessageAttachmentResponse> attachments;

	private boolean unread; // Inferred attribute based on label
}
