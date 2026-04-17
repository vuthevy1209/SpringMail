package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.dto.mail.request.AttachmentRequest;
import com.vuthevy1209.springmail.dto.mail.request.MailThreadsRequest;
import com.vuthevy1209.springmail.dto.mail.request.SendMailRequest;
import com.vuthevy1209.springmail.dto.mail.request.ThreadActionRequest;
import com.vuthevy1209.springmail.dto.mail.response.MailThreadResponse;
import com.vuthevy1209.springmail.repository.MailMessageRepository;
import com.vuthevy1209.springmail.repository.MailThreadRepository;
import com.vuthevy1209.springmail.converters.MailThreadConverter;
import com.vuthevy1209.springmail.entity.MailMessage;
import com.vuthevy1209.springmail.entity.MailThread;
import com.vuthevy1209.springmail.service.gmail.GmailService;
import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.ModifyThreadRequestDto;
import com.vuthevy1209.springmail.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	@Transactional
	public MailThreadResponse modifyThread(String threadId, ThreadActionRequest request) throws IOException {
			String accessToken = SecurityUtils.getAccessToken("google");
			if (accessToken == null) {
					throw new IOException("Failed to authorize OAuth2 client or get access token");
			}

			ModifyThreadRequestDto gmailRequest = ModifyThreadRequestDto.builder()
					.addLabelIds(request.getAddLabelIds())
					.removeLabelIds(request.getRemoveLabelIds())
					.build();

			gmailService.modifyThread(accessToken, threadId, gmailRequest);

			MailThread thread = threadRepository.findById(threadId).orElse(null);
			if (thread != null) {
					if (request.getAddLabelIds() != null) {
							for(String label : request.getAddLabelIds()) {
									if(!thread.getLabelIds().contains(label)) {
											thread.getLabelIds().add(label);
									}
							}
					}
					if (request.getRemoveLabelIds() != null) {
							thread.getLabelIds().removeAll(request.getRemoveLabelIds());
					}
					threadRepository.save(thread);
			}

			return getThreadDetail(threadId);
	}

	@Override
	public void sendMail(SendMailRequest request) throws IOException {
		String accessToken = SecurityUtils.getAccessToken("google");
		if (accessToken == null) {
			throw new IOException("Failed to authorize OAuth2 client or get access token");
		}
		gmailService.sendMail(accessToken, request);
	}

	@Override
	@Transactional
	public void trashThread(String threadId) throws IOException {
			String accessToken = SecurityUtils.getAccessToken("google");
			if (accessToken == null) {
					throw new IOException("Failed to authorize OAuth2 client or get access token");
			}
			gmailService.trashThread(accessToken, threadId);

			MailThread thread = threadRepository.findById(threadId).orElse(null);
			if (thread != null) {
					if(!thread.getLabelIds().contains("TRASH")) {
							thread.getLabelIds().add("TRASH");
					}
					thread.getLabelIds().remove("INBOX");
					threadRepository.save(thread);
			}
	}
}
