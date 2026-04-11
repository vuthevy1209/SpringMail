package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.dto.mail.request.AttachmentRequest;
import com.vuthevy1209.springmail.dto.mail.request.MailThreadsRequest;
import com.vuthevy1209.springmail.dto.mail.response.MailThreadResponse;
import com.vuthevy1209.springmail.repository.MailMessageRepository;
import com.vuthevy1209.springmail.repository.MailThreadRepository;
import com.vuthevy1209.springmail.repository.UserRepository;
import com.vuthevy1209.springmail.converters.MailThreadConverter;
import com.vuthevy1209.springmail.entity.MailMessage;
import com.vuthevy1209.springmail.entity.MailThread;
import com.vuthevy1209.springmail.entity.User;
import com.vuthevy1209.springmail.service.gmail.GmailService;
import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentDto;
import com.vuthevy1209.springmail.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

	private final GmailService gmailService;

	private final MailThreadRepository threadRepository;
	private final MailMessageRepository messageRepository;
	private final UserRepository userRepository;
	private final OAuth2AuthorizedClientService authorizedClientService;

	private final MailSyncService mailSyncService;


	private final MailThreadConverter mailThreadConverter;

	@Override
	public Page<MailThreadResponse> getMailThreads(MailThreadsRequest request, int page, int size) throws IOException {
		OAuth2User user = SecurityUtils.getCurrentOAuth2User();
		if (user == null) {
			throw new RuntimeException("Current user not found");
		}

		String userId = user.getAttribute("googleId");
		log.info("Fetching threads for userId: {}, page: {}, size: {}", userId, page, size);
		log.info("request: " + request.getLabelIds());

		Pageable pageable = PageRequest.of(page, size, Sort.by("lastMessageTimestamp").descending());

		Page<MailThread> mailThreadPage;
		if (request.getLabelIds() != null && !request.getLabelIds().isEmpty()) {
			mailThreadPage = threadRepository.findByUserIdAndLabelIdsContainsAll(userId, request.getLabelIds(), pageable);
		} else {
			mailThreadPage = threadRepository.findByUserId(userId, pageable);
		}

		return mailThreadPage.map(mailThreadConverter::toMailThreadResponse);
	}

	@Override
	public MailThreadResponse getThreadDetail(String threadId) throws IOException {
		OAuth2User oauth2User = SecurityUtils.getCurrentOAuth2User();
		if (oauth2User == null) {
			throw new RuntimeException("Current user not found");
		}

		String userId = oauth2User.getAttribute("googleId");

		MailThread mailThread = threadRepository.findByIdAndUserId(threadId, userId)
				.orElseThrow(() -> new RuntimeException("Thread not found or access denied"));

		List<MailMessage> messages = messageRepository.findByThreadIdOrderByInternalDateAsc(threadId);

		return mailThreadConverter.toMailThreadResponse(mailThread, messages);
	}

	@Override
	public ResponseEntity<byte[]> getAttachment(AttachmentRequest request) throws IOException {
		String accessToken = SecurityUtils.getAccessToken("google");
		if (accessToken == null) {
			throw new IOException("Failed to authorize OAuth2 client or get access token");
		}

		GmailAttachmentDto attachment = gmailService.getAttachment(accessToken, request.getMessageId(), request.getAttachmentId(), request.getFilename(), request.getMimeType());

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

	@Override
	public void processNewEmails(String email, String historyId) {
		log.info("Processing new emails for {} triggered by webhook", email);

		User user = userRepository.findByEmail(email).orElse(null);
		if (user == null) {
			log.warn("User {} not found in DB. Cannot process webhook.", email);
			return;
		}

		// OAuth2AuthorizedClientService lưu principalName dưới dạng EMAIL (được setup trong CustomOidcUserService)
		OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("google", user.getEmail());
		if (client == null || client.getAccessToken() == null) {
			log.warn("OAuth2AuthorizedClient not found or missing access token for user {}", email);
			return;
		}

		try {
			String accessToken = client.getAccessToken().getTokenValue();
			mailSyncService.syncMail(user, accessToken);
		} catch (Exception e) {
			log.error("Failed to sync new emails for user {}", email, e);
		}
	}
}

