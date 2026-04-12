package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.dto.mail.request.AttachmentRequest;
import com.vuthevy1209.springmail.dto.mail.request.MailThreadsRequest;
import com.vuthevy1209.springmail.dto.mail.request.SendMailRequest;
import com.vuthevy1209.springmail.dto.mail.request.ThreadActionRequest;
import com.vuthevy1209.springmail.dto.mail.response.MailThreadResponse;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import org.springframework.data.domain.Page;

public interface MailService {

	Page<MailThreadResponse> getMailThreads(MailThreadsRequest request, int page, int size) throws IOException;

	MailThreadResponse getThreadDetail(String threadId) throws IOException;

	MailThreadResponse modifyThread(String threadId, ThreadActionRequest request) throws IOException;

	void sendMail(SendMailRequest request) throws IOException;

	void trashThread(String threadId) throws IOException;

	ResponseEntity<byte[]> getAttachment(AttachmentRequest request) throws IOException;

	void processNewEmails(String email, String historyId);
}