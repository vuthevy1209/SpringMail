package com.vuthevy1209.springmail.service.gmail;

import com.google.api.services.gmail.model.ListThreadsResponse;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.Thread;

import java.util.List;
import java.io.IOException;

public interface GmailClient {
    ListThreadsResponse listThreads(String accessToken, String query, Long maxResults) throws IOException;
    Thread getThread(String accessToken, String threadId, String format, List<String> metadataHeaders) throws IOException;
    MessagePartBody getAttachment(String accessToken, String messageId, String attachmentId) throws IOException;
}
