package com.vuthevy1209.springmail.controller;

import com.vuthevy1209.springmail.dto.ai.AiDraftRequest;
import com.vuthevy1209.springmail.dto.ai.AiDraftResponse;
import com.vuthevy1209.springmail.dto.ai.AiSummaryRequest;
import com.vuthevy1209.springmail.dto.ai.AiSummaryResponse;
import com.vuthevy1209.springmail.service.mail.MailAiService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final MailAiService mailAiService;

    @PostMapping("/summarize")
    public ResponseEntity<AiSummaryResponse> summarizeEmail(@RequestBody AiSummaryRequest request) {
        AiSummaryResponse response = mailAiService.summarize(request.getThreadId(), request.getContent());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/draft")
    public ResponseEntity<AiDraftResponse> generateDraft(@RequestBody AiDraftRequest request) {
        AiDraftResponse response = mailAiService.generateDraft(request.getThreadId(), request.getContent(), request.getFormat());
        return ResponseEntity.ok(response);
    }
}
