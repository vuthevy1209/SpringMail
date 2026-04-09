package com.vuthevy1209.springmail.controller;

import com.vuthevy1209.springmail.dto.response.ApiResponse;
import com.vuthevy1209.springmail.service.mail.MailSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
public class SyncMailController {

	private final MailSyncService mailSyncService;

	@PostMapping
	public ApiResponse<String> sync() throws IOException {
		mailSyncService.syncForUser();
		return ApiResponse.<String>builder().
				result("Sync successful")
				.build();
	}
}
