package com.vuthevy1209.springmail.controller;

import com.vuthevy1209.springmail.dto.ApiResponse;
import com.vuthevy1209.springmail.dto.mail.request.AttachmentRequest;
import com.vuthevy1209.springmail.dto.mail.request.FetchOlderRequest;
import com.vuthevy1209.springmail.dto.mail.request.MailThreadsRequest;
import com.vuthevy1209.springmail.dto.mail.request.SendMailRequest;
import com.vuthevy1209.springmail.dto.mail.request.ThreadActionRequest;
import com.vuthevy1209.springmail.dto.mail.response.FetchOlderResponse;
import com.vuthevy1209.springmail.dto.mail.response.MailThreadResponse;
import com.vuthevy1209.springmail.service.mail.MailService;
import com.vuthevy1209.springmail.service.mail.MailSyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
public class MailController {

	private final MailService mailService;
	private final MailSyncService mailSyncService;


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

	@PostMapping("/sync/fetch-older")
	public ApiResponse<FetchOlderResponse> fetchOlderThreads(@RequestBody FetchOlderRequest request) throws IOException {
		return ApiResponse.<FetchOlderResponse>builder()
				.result(mailSyncService.fetchOlderThreads(request))
				.build();
	}

	@PostMapping("/threads/{id}/modify")
	public ApiResponse<MailThreadResponse> modifyThread(@PathVariable String id, @RequestBody ThreadActionRequest request) throws IOException {
		return ApiResponse.<MailThreadResponse>builder()
				.result(mailService.modifyThread(id, request))
				.build();
	}

	@PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<Void> sendMail(@ModelAttribute SendMailRequest request) throws IOException {
		mailService.sendMail(request);
		return ApiResponse.<Void>builder().build();
	}

	@DeleteMapping("/threads/{id}/trash")
	public ApiResponse<Void> trashThread(@PathVariable String id) throws IOException {
		mailService.trashThread(id);
		return ApiResponse.<Void>builder().build();
	}

}
