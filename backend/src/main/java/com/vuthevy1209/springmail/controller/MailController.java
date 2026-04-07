package com.vuthevy1209.springmail.controller;

import com.vuthevy1209.springmail.dto.response.mail.MailThreadResponse;
import com.vuthevy1209.springmail.service.gmail.MailService;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
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
}