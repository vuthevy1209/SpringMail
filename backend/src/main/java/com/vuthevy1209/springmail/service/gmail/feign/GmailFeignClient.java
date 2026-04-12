package com.vuthevy1209.springmail.service.gmail.feign;

import com.google.api.services.gmail.model.ListHistoryResponse;
import com.google.api.services.gmail.model.ListThreadsResponse;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.Profile;
import com.google.api.services.gmail.model.Thread;
import java.util.List;

import com.vuthevy1209.springmail.service.gmail.dto.thread.ModifyThreadRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "gmailFeignClient", url = "https://gmail.googleapis.com", configuration = GmailFeignConfiguration.class)
public interface GmailFeignClient {
 
    @GetMapping("/gmail/v1/users/me/profile")
    Profile getProfile(@RequestHeader("Authorization") String authHeader);

    @GetMapping("/gmail/v1/users/me/threads")
    ListThreadsResponse listThreads(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("q") String query,
            @RequestParam("maxResults") Long maxResults,
            @RequestParam(value = "pageToken", required = false) String pageToken
    );

    @GetMapping(value = "/gmail/v1/users/me/threads/{id}", produces = "application/json")
    Thread getThread(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") String threadId,
            @RequestParam("format") String format,
            @RequestParam(value = "metadataHeaders", required = false) List<String> metadataHeaders
    );

    @GetMapping("/gmail/v1/users/me/messages/{messageId}/attachments/{id}")
    MessagePartBody getAttachment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("messageId") String messageId,
            @PathVariable("id") String attachmentId
    );

    @GetMapping("/gmail/v1/users/me/history")
    ListHistoryResponse listHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("startHistoryId") String startHistoryId,
            @RequestParam(value = "maxResults", required = false) Long maxResults,
            @RequestParam(value = "pageToken", required = false) String pageToken
    );

    @PostMapping(value = "/gmail/v1/users/me/threads/{id}/modify", produces = "application/json", consumes = "application/json")
    Thread modifyThread(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") String threadId,
            @RequestBody ModifyThreadRequestDto request
    );

    @PostMapping(value = "/gmail/v1/users/me/threads/{id}/trash", produces = "application/json")
    Thread trashThread(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") String threadId
    );
}

