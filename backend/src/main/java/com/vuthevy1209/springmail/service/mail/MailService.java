package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.dto.response.mail.MailThreadResponse;
import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentDto;

import java.io.IOException;
import java.util.List;

public interface MailService {
	List<MailThreadResponse> getMailThreads(String folder, String category) throws IOException;

	MailThreadResponse getThreadDetails(String threadId) throws IOException;

	GmailAttachmentDto getAttachment(String messageId, String attachmentId, String filename, String mimeType) throws IOException;

	void syncMail() throws IOException;
}