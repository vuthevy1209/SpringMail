package com.vuthevy1209.springmail.service.gmail;

import com.vuthevy1209.springmail.dto.response.mail.MailThreadResponse;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

import java.io.IOException;
import java.util.List;

public interface MailService {
    List<MailThreadResponse> getRecentEmails(OAuth2AuthorizedClient client, String folder, String category) throws IOException;
    byte[] getAttachment(OAuth2AuthorizedClient client, String messageId, String attachmentId) throws IOException;
}