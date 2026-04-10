package com.vuthevy1209.springmail.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Document(collection = "mail_threads")
public class MailThread {
	private String userId;

	@Id
	private String id; // Gmail Thread ID
	private String snippet;
	private Long historyId;
	private Set<String> labelIds;

	// This info is extracted from the message List in thread
	private String subject;
	private List<String> senderNames;
	private int messageCount;
	private Long lastMessageTimestamp;
	private Instant createdAt;
	private Instant updatedAt;
}

