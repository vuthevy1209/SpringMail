package com.vuthevy1209.springmail.controller;

import com.vuthevy1209.springmail.dto.response.ApiResponse;
import com.vuthevy1209.springmail.dto.response.mail.MailThreadResponse;
import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentDto;
import com.vuthevy1209.springmail.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
public class MailController {

	private final MailService mailService;

	@GetMapping("/threads")
	public ApiResponse<List<MailThreadResponse>> getEmails(
			@RequestParam(defaultValue = "inbox") String folder,
			@RequestParam(required = false) String category
	) throws IOException {
		return ApiResponse.<List<MailThreadResponse>>builder()
				.result(mailService.getRecentEmails(folder, category))
				.build();
	}

	@GetMapping("/threads/{id}")
	public ApiResponse<MailThreadResponse> getThread(
			@PathVariable String id
	) throws IOException {
		return ApiResponse.<MailThreadResponse>builder()
				.result(mailService.getThreadDetails(id))
				.build();
	}

	@GetMapping("/attachments")
	public ResponseEntity<byte[]> getAttachment(
			@RequestParam String messageId,
			@RequestParam String attachmentId,
			@RequestParam(required = false) String filename,
			@RequestParam(required = false) String mimeType
	) throws IOException {
		GmailAttachmentDto attachment = mailService.getAttachment(messageId, attachmentId, filename, mimeType);

		byte[] content = Base64.getUrlDecoder().decode(attachment.getData());

		HttpHeaders headers = new HttpHeaders();

		// Thiết lập Content-Type từ mimeType trong DTO
		if (attachment.getMimeType() != null && !attachment.getMimeType().isEmpty()) {
			headers.setContentType(MediaType.parseMediaType(attachment.getMimeType()));
		} else {
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		}

		// Thiết lập Content-Disposition với filename từ DTO
		if (attachment.getFilename() != null && !attachment.getFilename().isEmpty()) {
			ContentDisposition contentDisposition = ContentDisposition.attachment()
					.filename(attachment.getFilename(), StandardCharsets.UTF_8)
					.build();
			headers.setContentDisposition(contentDisposition);
		}

		return new ResponseEntity<>(content, headers, HttpStatus.OK);
	}
}