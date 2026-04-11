package com.vuthevy1209.springmail.controller;

import com.vuthevy1209.springmail.dto.ApiResponse;
import com.vuthevy1209.springmail.dto.mail.request.AttachmentRequest;
import com.vuthevy1209.springmail.dto.mail.request.MailThreadsRequest;
import com.vuthevy1209.springmail.dto.mail.response.MailThreadResponse;
import com.vuthevy1209.springmail.service.mail.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
public class MailController {

	private final MailService mailService;


	@PostMapping("/threads")
	public ApiResponse<Page<MailThreadResponse>> getMailThreads(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestBody MailThreadsRequest request) throws IOException {
		return ApiResponse.<Page<MailThreadResponse>>builder()
				.result(mailService.getMailThreads(request, page, size))
				.build();
	}

	@GetMapping("/threads/{id}")
	public ApiResponse<MailThreadResponse> getThreadDetail(@PathVariable String id) throws IOException {
		return ApiResponse.<MailThreadResponse>builder()
				.result(mailService.getThreadDetail(id))
				.build();
	}

    @PostMapping("/attachments")
    public ResponseEntity<byte[]> getAttachmentPost(@Valid @RequestBody AttachmentRequest request) throws IOException {
        return mailService.getAttachment(request);
    }

    @GetMapping("/attachments")
    public ResponseEntity<byte[]> getAttachmentGet(
            @RequestParam String messageId,
            @RequestParam String attachmentId,
            @RequestParam(required = false) String filename,
            @RequestParam(required = false) String mimeType) throws IOException {
        AttachmentRequest request = AttachmentRequest.builder()
                .messageId(messageId)
                .attachmentId(attachmentId)
                .filename(filename)
                .mimeType(mimeType)
                .build();
        return mailService.getAttachment(request);
    }
}