package com.vuthevy1209.springmail.controller;

import com.vuthevy1209.springmail.dto.response.mail.MailThreadResponse;
import com.vuthevy1209.springmail.service.gmail.MailService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.vuthevy1209.springmail.dto.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    @GetMapping("/get-emails")
    public ApiResponse<List<MailThreadResponse>> getEmails(
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
            @RequestParam(defaultValue = "inbox") String folder,
            @RequestParam(required = false) String category
    ) throws IOException {
        return ApiResponse.<List<MailThreadResponse>>builder()
                .result(mailService.getRecentEmails(authorizedClient, folder, category))
                .build();
    }

    @GetMapping("/get-thread/{id}")
    public ApiResponse<MailThreadResponse> getThread(
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
            @org.springframework.web.bind.annotation.PathVariable String id
    ) throws IOException {
        return ApiResponse.<MailThreadResponse>builder()
                .result(mailService.getThreadDetails(authorizedClient, id))
                .build();
    }

    @GetMapping("/get-attachment")
    public ResponseEntity<byte[]> getAttachment(
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
            @RequestParam String messageId,
            @RequestParam String attachmentId,
            @RequestParam(required = false) String filename,
            @RequestParam(required = false) String contentType
    ) throws IOException {
        byte[] content = mailService.getAttachment(authorizedClient, messageId, attachmentId);
        
        HttpHeaders headers = new HttpHeaders();
        
        // Thiết lập Content-Type
        if (contentType != null && !contentType.isEmpty()) {
            headers.setContentType(MediaType.parseMediaType(contentType));
        } else {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }
        
        // Thiết lập Content-Disposition để trình duyệt hiểu là file tải về
        if (filename != null && !filename.isEmpty()) {
            ContentDisposition contentDisposition = ContentDisposition.attachment()
                    .filename(filename, StandardCharsets.UTF_8)
                    .build();
            headers.setContentDisposition(contentDisposition);
        }
        
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}