package com.vuthevy1209.springmail.dto.response.mail;

public record MailAttachmentResponse(
	String id,
	String filename,
	String mimeType,
	Long size,
	String contentId,
	String data
) {}
