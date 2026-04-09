package com.vuthevy1209.springmail.service.gmail.feign;

import com.vuthevy1209.springmail.service.gmail.GmailService;
import com.vuthevy1209.springmail.service.gmail.GmailMapper;
import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentBodyDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailListHistoryResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailListThreadsResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailThreadDto;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Primary
public class FeignVersionGmailServiceImpl implements GmailService {

    private final GmailFeignClient gmailFeignClient;
    private final GmailMapper gmailMapper;

    @Override
    public GmailListThreadsResponseDto listThreads(String accessToken, String query, Long maxResults, String pageToken) throws IOException {
        var response = gmailFeignClient.listThreads("Bearer " + accessToken, query, maxResults, pageToken);
        return gmailMapper.toDto(response);
    }

    @Override
    public GmailThreadDto getThread(String accessToken, String threadId, String format, List<String> metadataHeaders) throws IOException {
        var thread = gmailFeignClient.getThread("Bearer " + accessToken, threadId, format, metadataHeaders);
        return gmailMapper.toDto(thread);
    }

    @Override
    public GmailAttachmentBodyDto getAttachment(String accessToken, String messageId, String attachmentId) throws IOException {
        var body = gmailFeignClient.getAttachment("Bearer " + accessToken, messageId, attachmentId);
        return gmailMapper.toDto(body, true);
    }

    @Override
    public GmailListHistoryResponseDto listHistory(String accessToken, String startHistoryId, Long maxResults, String pageToken) throws IOException {
        var response = gmailFeignClient.listHistory("Bearer " + accessToken, startHistoryId, maxResults, pageToken);
        return gmailMapper.toDto(response);
    }
}

