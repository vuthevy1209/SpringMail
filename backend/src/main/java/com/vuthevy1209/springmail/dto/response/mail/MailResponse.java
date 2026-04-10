package com.vuthevy1209.springmail.dto.response.mail;

import java.util.List;
import java.util.Set;

public record MailResponse(
	String id,
	String threadId,
	String from,
	String to,
	String fromName,
	String fromEmail,
	String toName,
	String toEmail,
	List<String> cc,
	List<String> bcc,
	String subject,
	String date,
	String snippet,
	String content,
	String bodyHtml,
	String bodyText,
	boolean unread,
	Long internalDate,
	Long sizeEstimate,
	Long historyId,
	Set<String> labelIds,
	List<MailAttachmentResponse> attachments
) {}
