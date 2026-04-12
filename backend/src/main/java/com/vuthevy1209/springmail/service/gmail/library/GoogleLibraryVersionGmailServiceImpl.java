package com.vuthevy1209.springmail.service.gmail.library;

import com.google.api.client.http.InputStreamContent;
import java.io.ByteArrayInputStream;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.model.Thread;
import com.vuthevy1209.springmail.configuration.GmailServiceFactory;
import com.vuthevy1209.springmail.service.gmail.GmailService;
import com.vuthevy1209.springmail.service.gmail.GmailMapper;
import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentDto;
import com.vuthevy1209.springmail.service.gmail.dto.profile.GmailProfileDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailListHistoryResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailListThreadsResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailThreadDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.ModifyThreadRequestDto;
import com.vuthevy1209.springmail.dto.mail.request.SendMailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.web.multipart.MultipartFile;
import jakarta.mail.Message;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.IOException;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;

@Service
@Primary
@RequiredArgsConstructor
public class GoogleLibraryVersionGmailServiceImpl implements GmailService {

	private final GmailServiceFactory gmailServiceFactory;
 
	@Override
	public GmailProfileDto getProfile(String accessToken) throws IOException {
		Gmail service = gmailServiceFactory.build(accessToken);
		Profile profile = service.users().getProfile("me").execute();
		return GmailMapper.toGmailProfileDto(profile);
	}

	@Override
	public GmailListThreadsResponseDto listThreads(String accessToken, String query, Long maxResults, String pageToken) throws IOException {
		Gmail service = gmailServiceFactory.build(accessToken);
		ListThreadsResponse response = service.users().threads().list("me")
				.setQ(query)
				.setMaxResults(maxResults)
				.setPageToken(pageToken)
				.execute();
		return GmailMapper.toGmailListThreadsResponseDto(response);
	}

	@Override
	public GmailThreadDto getThread(String accessToken, String threadId, String format, List<String> metadataHeaders) throws IOException {
		Gmail service = gmailServiceFactory.build(accessToken);
		Thread response = service.users().threads().get("me", threadId)
				.setFormat(format)
				.setMetadataHeaders(metadataHeaders)
				.execute();
		return GmailMapper.toGmailThreadDto(response);
	}

	@Override
	public GmailAttachmentDto getAttachment(String accessToken, String messageId, String attachmentId, String filename, String mimeType) throws IOException {
		Gmail service = gmailServiceFactory.build(accessToken);
		MessagePartBody response = service.users().messages().attachments()
				.get("me", messageId, attachmentId)
				.execute();
		return GmailMapper.toGmailAttachmentDto(response, filename, mimeType);
	}

	@Override
	public GmailListHistoryResponseDto listHistory(String accessToken, String startHistoryId, Long maxResults, String pageToken) throws IOException {
		Gmail service = gmailServiceFactory.build(accessToken);
		ListHistoryResponse response = service.users().history().list("me")
				.setStartHistoryId(new BigInteger(startHistoryId))
				.setMaxResults(maxResults)
				.setPageToken(pageToken)
				.execute();
		return GmailMapper.toGmailListHistoryResponseDto(response);
	}

	@Override
	public List<GmailThreadDto> getThreadsBatch(String accessToken, List<String> threadIds) throws IOException {
		if (threadIds == null || threadIds.isEmpty()) {
			return Collections.emptyList();
		}

		Gmail service = gmailServiceFactory.build(accessToken);
		BatchRequest batch = service.batch();
		List<Thread> threads = Collections.synchronizedList(new ArrayList<>());

		JsonBatchCallback<Thread> callback = new JsonBatchCallback<>() {
			@Override
			public void onSuccess(Thread thread, HttpHeaders responseHeaders) {
				threads.add(thread);
			}

			@Override
			public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
				System.err.println("Error fetching thread in batch: " + e.getMessage());
			}
		};

		for (String threadId : threadIds) {
			service.users().threads().get("me", threadId)
					.setFormat("full")
					.queue(batch, callback);
		}

		batch.execute();

		return threads.stream()
				.map(GmailMapper::toGmailThreadDto)
				.toList();
	}

	@Override
	public GmailThreadDto modifyThread(String accessToken, String threadId, ModifyThreadRequestDto request) throws IOException {
		Gmail service = gmailServiceFactory.build(accessToken);

		ModifyThreadRequest modifyRequest = new ModifyThreadRequest()
			.setAddLabelIds(request.getAddLabelIds())
			.setRemoveLabelIds(request.getRemoveLabelIds());

		Thread thread = service.users().threads().modify("me", threadId, modifyRequest).execute();

		return GmailMapper.toGmailThreadDto(thread);
	}

	@Override
	public void sendMail(String accessToken, SendMailRequest request) throws IOException {
		Gmail service = gmailServiceFactory.build(accessToken);
		try {
			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props, null);
			MimeMessage email = new MimeMessage(session);

			email.setFrom(new InternetAddress("me"));
			email.addRecipient(Message.RecipientType.TO, new InternetAddress(request.getTo()));
			if (request.getCc() != null && !request.getCc().trim().isEmpty()) {
				email.addRecipients(Message.RecipientType.CC, InternetAddress.parse(request.getCc()));
			}
			if (request.getBcc() != null && !request.getBcc().trim().isEmpty()) {
				email.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(request.getBcc()));
			}

			email.setSubject(request.getSubject() != null ? request.getSubject() : "");

			MimeMultipart multipart = new MimeMultipart();

			MimeBodyPart textPart = new MimeBodyPart();
			String content = request.getBody() != null ? request.getBody() : "";
			
			if (Boolean.TRUE.equals(request.getIsHtml())) {
				textPart.setContent(content, "text/html; charset=utf-8");
			} else {
				textPart.setText(content, "utf-8");
			}
			
			multipart.addBodyPart(textPart);

			if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
				for (MultipartFile file : request.getAttachments()) {
					if (file.isEmpty()) continue;
					MimeBodyPart attachmentPart = new MimeBodyPart();
					ByteArrayDataSource dataSource = new ByteArrayDataSource(file.getInputStream(), file.getContentType());
					attachmentPart.setDataHandler(new DataHandler(dataSource));
					attachmentPart.setFileName(file.getOriginalFilename());
					multipart.addBodyPart(attachmentPart);
				}
			}

			email.setContent(multipart);

			// For reply thread handling
			com.google.api.services.gmail.model.Message message = new com.google.api.services.gmail.model.Message();
			if (request.getThreadId() != null && !request.getThreadId().trim().isEmpty()) {
				message.setThreadId(request.getThreadId());
				if (request.getInReplyTo() != null && !request.getInReplyTo().isEmpty()) {
					email.setHeader("In-Reply-To", request.getInReplyTo());
					email.setHeader("References", request.getInReplyTo());
				}
			}

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			email.writeTo(buffer);
			byte[] rawMessageBytes = buffer.toByteArray();
			
			InputStreamContent mediaContent = new InputStreamContent("message/rfc822", new ByteArrayInputStream(rawMessageBytes));

			service.users().messages().send("me", message, mediaContent).execute();
		} catch (MessagingException e) {
			throw new IOException("Error creating email message", e);
		}
	}

	@Override
	public GmailThreadDto trashThread(String accessToken, String threadId) throws IOException {
		Gmail service = gmailServiceFactory.build(accessToken);

		Thread thread = service.users().threads().trash("me", threadId).execute();

		return GmailMapper.toGmailThreadDto(thread);
	}
}
