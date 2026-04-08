package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.dto.response.mail.MailThreadResponse;

import java.io.IOException;
import java.util.List;

public interface MailService {
    List<MailThreadResponse> getRecentEmails(String folder, String category) throws IOException;

    MailThreadResponse getThreadDetails(String threadId) throws IOException;

    byte[] getAttachment(String messageId, String attachmentId) throws IOException;
}