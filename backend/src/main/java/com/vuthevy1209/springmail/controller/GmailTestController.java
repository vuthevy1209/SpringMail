package com.vuthevy1209.springmail.controller;

import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentBodyDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailListThreadsResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailThreadDto;
import com.vuthevy1209.springmail.dto.response.ApiResponse;
import com.vuthevy1209.springmail.service.gmail.GmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/test/gmail")
@RequiredArgsConstructor
public class GmailTestController {

    private final GmailService gmailClient;

    @GetMapping("/threads")
    public ApiResponse<GmailListThreadsResponseDto> listThreads(
        @RequestHeader("Authorization") String authHeader,
        @RequestParam(value = "q", required = false) String query,
        @RequestParam(value = "maxResults", required = false) Long maxResults
    ) throws IOException {
        String accessToken = extractToken(authHeader);
        return ApiResponse.<GmailListThreadsResponseDto>builder()
            .result(gmailClient.listThreads(accessToken, query, maxResults, null))
            .build();
    }

    @GetMapping("/threads/{id}")
    public ApiResponse<GmailThreadDto> getThread(
        @RequestHeader("Authorization") String authHeader,
        @PathVariable("id") String threadId,
        @RequestParam(value = "format", defaultValue = "full") String format,
        @RequestParam(value = "metadataHeaders", required = false) List<String> metadataHeaders
    ) throws IOException {
        String accessToken = extractToken(authHeader);
        return ApiResponse.<GmailThreadDto>builder()
            .result(gmailClient.getThread(accessToken, threadId, format, metadataHeaders))
            .build();
    }

    @GetMapping("/messages/{messageId}/attachments/{id}")
    public ApiResponse<GmailAttachmentBodyDto> getAttachment(
        @RequestHeader("Authorization") String authHeader,
        @PathVariable("messageId") String messageId,
        @PathVariable("id") String attachmentId
    ) throws IOException {
        String accessToken = extractToken(authHeader);
        return ApiResponse.<GmailAttachmentBodyDto>builder()
            .result(gmailClient.getAttachment(accessToken, messageId, attachmentId))
            .build();
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return authHeader != null ? authHeader.trim() : null;
    }
}
