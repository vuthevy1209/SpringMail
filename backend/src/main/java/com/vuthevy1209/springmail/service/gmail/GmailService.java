package com.vuthevy1209.springmail.service.gmail;

import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentDto;
import com.vuthevy1209.springmail.service.gmail.dto.profile.GmailProfileDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailListHistoryResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailListThreadsResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailThreadDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.ModifyThreadRequestDto;

import java.util.List;
import java.io.IOException;

public interface GmailService {
    GmailProfileDto getProfile(String accessToken) throws IOException;
    GmailListThreadsResponseDto listThreads(String accessToken, String query, Long maxResults, String pageToken) throws IOException;
    GmailThreadDto getThread(String accessToken, String threadId, String format, List<String> metadataHeaders) throws IOException;
    GmailAttachmentDto getAttachment(String accessToken, String messageId, String attachmentId, String filename, String mimeType) throws IOException;
    GmailListHistoryResponseDto listHistory(String accessToken, String startHistoryId, Long maxResults, String pageToken) throws IOException;
    List<GmailThreadDto> getThreadsBatch(String accessToken, List<String> threadIds) throws IOException;
	GmailThreadDto modifyThread(String accessToken, String threadId, ModifyThreadRequestDto request) throws IOException;
	GmailThreadDto trashThread(String accessToken, String threadId) throws IOException;
}
