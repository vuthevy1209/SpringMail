package com.vuthevy1209.springmail.service.gmail.feign;

import com.google.api.services.gmail.model.ListHistoryResponse;
import com.google.api.services.gmail.model.ListThreadsResponse;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.Profile;
import com.google.api.services.gmail.model.Thread;
import com.vuthevy1209.springmail.service.gmail.GmailService;
import com.vuthevy1209.springmail.service.gmail.GmailMapper;
import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentDto;
import com.vuthevy1209.springmail.service.gmail.dto.profile.GmailProfileDto;
import com.vuthevy1209.springmail.service.gmail.dto.history.GmailListHistoryResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailListThreadsResponseDto;
import com.vuthevy1209.springmail.service.gmail.dto.thread.GmailThreadDto;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Primary
public class FeignVersionGmailServiceImpl implements GmailService {

	private final GmailFeignClient gmailFeignClient;
 
	@Override
	public GmailProfileDto getProfile(String accessToken) throws IOException {
		Profile profile = gmailFeignClient.getProfile("Bearer " + accessToken);
		return GmailMapper.toGmailProfileDto(profile);
	}

	@Override
	public GmailListThreadsResponseDto listThreads(String accessToken, String query, Long maxResults, String pageToken) throws IOException {
		ListThreadsResponse response = gmailFeignClient.listThreads("Bearer " + accessToken, query, maxResults, pageToken);
		return GmailMapper.toGmailListThreadsResponseDto(response);
	}

	@Override
	public GmailThreadDto getThread(String accessToken, String threadId, String format, List<String> metadataHeaders) throws IOException {
		Thread thread = gmailFeignClient.getThread("Bearer " + accessToken, threadId, format, metadataHeaders);
		return GmailMapper.toGmailThreadDto(thread);
	}

	@Override
	public GmailAttachmentDto getAttachment(String accessToken, String messageId, String attachmentId, String filename, String mimeType) throws IOException {
		MessagePartBody body = gmailFeignClient.getAttachment("Bearer " + accessToken, messageId, attachmentId);
		return GmailMapper.toGmailAttachmentDto(body, filename, mimeType);
	}

	@Override
	public GmailListHistoryResponseDto listHistory(String accessToken, String startHistoryId, Long maxResults, String pageToken) throws IOException {
		ListHistoryResponse response = gmailFeignClient.listHistory("Bearer " + accessToken, startHistoryId, maxResults, pageToken);
		return GmailMapper.toGmailListHistoryResponseDto(response);
	}
}
