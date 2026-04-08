package com.vuthevy1209.springmail.service.gmail.library;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListThreadsResponse;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.Thread;
import com.vuthevy1209.springmail.configuration.GmailServiceFactory;
import com.vuthevy1209.springmail.service.gmail.GmailClient;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class GoogleLibraryGmailClient implements GmailClient {

    private final GmailServiceFactory gmailServiceFactory;

    @Override
    public ListThreadsResponse listThreads(String accessToken, String query, Long maxResults) throws IOException {
        Gmail service = gmailServiceFactory.build(accessToken);
        return service.users().threads().list("me")
                .setQ(query)
                .setMaxResults(maxResults)
                .execute();
    }

    @Override
    public Thread getThread(String accessToken, String threadId, String format, List<String> metadataHeaders) throws IOException {
        Gmail service = gmailServiceFactory.build(accessToken);
        return service.users().threads().get("me", threadId)
                .setFormat(format)
                .setMetadataHeaders(metadataHeaders)
                .execute();
    }

    @Override
    public MessagePartBody getAttachment(String accessToken, String messageId, String attachmentId) throws IOException {
        Gmail service = gmailServiceFactory.build(accessToken);
        return service.users().messages().attachments()
                .get("me", messageId, attachmentId)
                .execute();
    }
}
