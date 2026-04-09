package com.vuthevy1209.springmail.service.gmail.library;

import com.google.api.services.gmail.Gmail;
import com.vuthevy1209.springmail.configuration.GmailServiceFactory;
import com.vuthevy1209.springmail.service.gmail.GmailService;
import com.vuthevy1209.springmail.service.gmail.GmailMapper;
import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentBodyDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailListHistoryResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailListThreadsResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailThreadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class GoogleLibraryVersionGmailServiceImpl implements GmailService {

	private final GmailServiceFactory gmailServiceFactory;
	private final GmailMapper gmailMapper;

	@Override
	public GmailListThreadsResponseDto listThreads(String accessToken, String query, Long maxResults, String pageToken) throws IOException {
		Gmail service = gmailServiceFactory.build(accessToken);
		var response = service.users().threads().list("me")
				.setQ(query)
				.setMaxResults(maxResults)
				.setPageToken(pageToken)
				.execute();
		return gmailMapper.toDto(response);
	}

	@Override
	public GmailThreadDto getThread(String accessToken, String threadId, String format, List<String> metadataHeaders) throws IOException {
		Gmail service = gmailServiceFactory.build(accessToken);
		var response = service.users().threads().get("me", threadId)
				.setFormat(format)
				.setMetadataHeaders(metadataHeaders)
				.execute();
		return gmailMapper.toDto(response);
	}

	@Override
	public GmailAttachmentBodyDto getAttachment(String accessToken, String messageId, String attachmentId) throws IOException {
		Gmail service = gmailServiceFactory.build(accessToken);
		var response = service.users().messages().attachments()
				.get("me", messageId, attachmentId)
				.execute();
		return gmailMapper.toDto(response, true);
	}

	@Override
	public GmailListHistoryResponseDto listHistory(String accessToken, String startHistoryId, Long maxResults, String pageToken) throws IOException {
		Gmail service = gmailServiceFactory.build(accessToken);
		var response = service.users().history().list("me")
				.setStartHistoryId(new BigInteger(startHistoryId))
				.setMaxResults(maxResults)
				.setPageToken(pageToken)
				.execute();
		return gmailMapper.toDto(response);
	}
}
