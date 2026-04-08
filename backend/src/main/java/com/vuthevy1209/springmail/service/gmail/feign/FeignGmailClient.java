package com.vuthevy1209.springmail.service.gmail.feign;

import com.google.api.services.gmail.model.ListThreadsResponse;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.Thread;
import com.vuthevy1209.springmail.service.gmail.GmailClient;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Primary
public class FeignGmailClient implements GmailClient {

    private final GmailFeignClient gmailFeignClient;

    @Override
    public ListThreadsResponse listThreads(String accessToken, String query, Long maxResults) throws IOException {
        return gmailFeignClient.listThreads("Bearer " + accessToken, query, maxResults);
    }

    @Override
    public Thread getThread(String accessToken, String threadId, String format, List<String> metadataHeaders) throws IOException {
        return gmailFeignClient.getThread("Bearer " + accessToken, threadId, format, metadataHeaders);
    }

    @Override
    public MessagePartBody getAttachment(String accessToken, String messageId, String attachmentId) throws IOException {
        return gmailFeignClient.getAttachment("Bearer " + accessToken, messageId, attachmentId);
    }
}
