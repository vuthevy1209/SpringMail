package com.vuthevy1209.springmail.controller;

import com.vuthevy1209.springmail.dto.response.ThreadResponse;
import com.vuthevy1209.springmail.service.GmailService;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.List;

@RestController
public class MailController {

    private final GmailService gmailService;

    public MailController(GmailService gmailService) {
        this.gmailService = gmailService;
    }

    @GetMapping("/get-emails")
    public List<ThreadResponse> getEmails(
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
            @RequestParam(defaultValue = "inbox") String folder,
            @RequestParam(required = false) String category
    ) throws IOException {
        return gmailService.getRecentEmails(authorizedClient, folder, category);
    }
}