package com.vuthevy1209.springmail.converters;

import com.vuthevy1209.springmail.dto.mail.response.MailMessageResponse;
import com.vuthevy1209.springmail.dto.mail.response.MessageAttachmentResponse;
import com.vuthevy1209.springmail.entity.MailChunkElasticSearch;
import com.vuthevy1209.springmail.entity.MailElasticSearch;
import com.vuthevy1209.springmail.entity.MailMessage;
import com.vuthevy1209.springmail.enums.MailLabel;
import com.vuthevy1209.springmail.service.embedding.EmbeddingService;
import com.vuthevy1209.springmail.service.gmail.dto.attachment.GmailAttachmentDto;
import com.vuthevy1209.springmail.service.gmail.dto.message.GmailMessageDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MailMessageConverter {

	private final ModelMapper modelMapper;

	private final EmbeddingService embeddingService;

	@PostConstruct
	public void init() {
		modelMapper.typeMap(GmailAttachmentDto.class, MessageAttachmentResponse.class)
				.addMapping(GmailAttachmentDto::getAttachmentId, MessageAttachmentResponse::setId);
	}

	public MailMessage toMailMessage(GmailMessageDto dto) {
		return modelMapper.map(dto, MailMessage.class);
	}

	public MailMessageResponse toMailMessageResponse(MailMessage mailMessage) {
		MailMessageResponse messageResponse = modelMapper.map(mailMessage, MailMessageResponse.class);

		boolean isUnread = mailMessage.getLabelIds() != null
				&& mailMessage.getLabelIds().contains(MailLabel.UNREAD.getId());

		messageResponse.setUnread(isUnread);
		return messageResponse;
	}

	public MailMessageResponse toMailMessageResponse(GmailMessageDto gmailMessageDto) {
		MailMessageResponse messageResponse = modelMapper.map(gmailMessageDto, MailMessageResponse.class);

		boolean isUnread = gmailMessageDto.getLabelIds() != null
				&& gmailMessageDto.getLabelIds().contains(MailLabel.UNREAD.getId());

		messageResponse.setUnread(isUnread);
		return messageResponse;
	}

	public List<MailMessageResponse> toMailMessageResponse(List<MailMessage> messages) {
		return messages.stream()
				.map(this::toMailMessageResponse)
				.toList();
	}

	public MailElasticSearch toMailElasticSearch(MailMessage mailMessage) {
		return  MailElasticSearch.builder()
				.id(mailMessage.getId())
				.userId(mailMessage.getUserId())
				.threadId(mailMessage.getThreadId())
				.subject(mailMessage.getSubject())
				.snippet(mailMessage.getSnippet())
				.sender(mailMessage.getFromName())
				.senderEmail(mailMessage.getFromEmail())
				.receiver(mailMessage.getToName())
				.receiverEmail(mailMessage.getToEmail())
				.labelIds(mailMessage.getLabelIds())
				.timestamp(mailMessage.getInternalDate())
				.build();
	}

	public List<MailChunkElasticSearch> toMailChunksElasticSearch(MailMessage mailMessage) {
		// 1) Normalize input text
		String bodyText = (mailMessage.getBodyText() != null && !mailMessage.getBodyText().isBlank())
				? mailMessage.getBodyText()
				: (mailMessage.getBodyHtml() != null ? mailMessage.getBodyHtml() : "");

		String subject = mailMessage.getSubject() != null ? mailMessage.getSubject() : "";

		// 2) Build source document
		Document source = new Document(
				bodyText,
				Map.of(
						"subject", subject,
						"mailId", mailMessage.getId(),
						"threadId", mailMessage.getThreadId(),
						"userId", mailMessage.getUserId()
				)
		);

		// 3) Split into chunks
		TokenTextSplitter splitter = TokenTextSplitter.builder()
				.withChunkSize(300)
				.withMinChunkSizeChars(100)
				.withMinChunkLengthToEmbed(20)
				.withMaxNumChunks(1000)
				.withKeepSeparator(true)
				.build();

		List<Document> chunkDocs = splitter.apply(List.of(source));

		// 4) Prepare chunk texts for embedding
		List<String> chunkTexts = chunkDocs.stream()
				.map(Document::getText)
				.filter(text -> text != null && !text.isBlank())
				.toList();

		if (chunkTexts.isEmpty()) {
			return List.of();
		}

		// 5) Embed per chunk \(batch\)
		List<List<Double>> vectors = embeddingService.embedBatch(chunkTexts);

		// 6) Map each chunk to Elasticsearch entity
		List<MailChunkElasticSearch> results = new ArrayList<>(chunkTexts.size());
		for (int i = 0; i < chunkTexts.size(); i++) {
			results.add(MailChunkElasticSearch.builder()
					.id(mailMessage.getId() + "_" + i)
					.chunkIndex(i)
					.mailId(mailMessage.getId())
					.userId(mailMessage.getUserId())
					.threadId(mailMessage.getThreadId())
					.labelIds(mailMessage.getLabelIds())
					.chunkText(chunkTexts.get(i))
					.contentVector(vectors.get(i))
					.timestamp(mailMessage.getInternalDate())
					.build());
		}

		return results;
	}

}
