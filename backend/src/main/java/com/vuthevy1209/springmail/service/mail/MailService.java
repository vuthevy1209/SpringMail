package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.dto.mail.request.AttachmentRequest;
import com.vuthevy1209.springmail.dto.mail.request.MailThreadsRequest;
import com.vuthevy1209.springmail.dto.mail.response.MailThreadResponse;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import org.springframework.data.domain.Page;

public interface MailService {

	Page<MailThreadResponse> getMailThreads(MailThreadsRequest request, int page, int size) throws IOException;

	MailThreadResponse getThreadDetail(String threadId) throws IOException;

	ResponseEntity<byte[]> getAttachment(AttachmentRequest request) throws IOException;

	void processNewEmails(String email, String historyId);
}