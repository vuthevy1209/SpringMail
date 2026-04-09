package com.vuthevy1209.springmail.service.gmail;

import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentBodyDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailListHistoryResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailListThreadsResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailThreadDto;

import java.util.List;
import java.io.IOException;

public interface GmailService {
    GmailListThreadsResponseDto listThreads(String accessToken, String query, Long maxResults, String pageToken) throws IOException;
    GmailThreadDto getThread(String accessToken, String threadId, String format, List<String> metadataHeaders) throws IOException;
    GmailAttachmentBodyDto getAttachment(String accessToken, String messageId, String attachmentId) throws IOException;
    GmailListHistoryResponseDto listHistory(String accessToken, String startHistoryId, Long maxResults, String pageToken) throws IOException;
}
