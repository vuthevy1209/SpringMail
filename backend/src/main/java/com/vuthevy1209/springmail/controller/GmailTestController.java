package com.vuthevy1209.springmail.controller;

import com.google.api.services.gmail.model.ListThreadsResponse;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.Thread;
import com.vuthevy1209.springmail.dto.response.ApiResponse;
import com.vuthevy1209.springmail.service.gmail.GmailClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/test/gmail")
@RequiredArgsConstructor
public class GmailTestController {

    private final GmailClient gmailClient;

    @GetMapping("/threads")
    public ApiResponse<ListThreadsResponse> listThreads(
        @RequestHeader("Authorization") String authHeader,
        @RequestParam(value = "q", required = false) String query,
        @RequestParam(value = "maxResults", required = false) Long maxResults
    ) throws IOException {
        String accessToken = extractToken(authHeader);
        return ApiResponse.<ListThreadsResponse>builder()
            .result(gmailClient.listThreads(accessToken, query, maxResults))
            .build();
    }

    @GetMapping("/threads/{id}")
    public ApiResponse<Thread> getThread(
        @RequestHeader("Authorization") String authHeader,
        @PathVariable("id") String threadId,
        @RequestParam(value = "format", defaultValue = "full") String format,
        @RequestParam(value = "metadataHeaders", required = false) List<String> metadataHeaders
    ) throws IOException {
        String accessToken = extractToken(authHeader);
        return ApiResponse.<Thread>builder()
            .result(gmailClient.getThread(accessToken, threadId, format, metadataHeaders))
            .build();
    }

    @GetMapping("/messages/{messageId}/attachments/{id}")
    public ApiResponse<MessagePartBody> getAttachment(
        @RequestHeader("Authorization") String authHeader,
        @PathVariable("messageId") String messageId,
        @PathVariable("id") String attachmentId
    ) throws IOException {
        String accessToken = extractToken(authHeader);
        return ApiResponse.<MessagePartBody>builder()
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
