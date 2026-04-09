package com.vuthevy1209.springmail.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Document(collection = "mail_threads")
public class MailThread {

	@Id
	private String id; // Gmail Thread ID

	@Indexed
	private String userId; // Link to User

	private String historyId;

	private String snippet;

	// -------------------------------------------------------------------------
	// Denormalized fields for fast list rendering (no extra DB query needed)
	// -------------------------------------------------------------------------

	/** Subject lấy từ tin nhắn đầu tiên của thread */
	private String subject;

	/** Tên hiển thị của sender trong tin nhắn mới nhất */
	private String latestSenderName;

	/** Email của sender trong tin nhắn mới nhất */
	private String latestSenderEmail;

	/** Số lượng tin nhắn trong thread */
	private int messageCount;

	/** Union label IDs từ tất cả messages (INBOX, UNREAD, SENT, CATEGORY_*, ...) */
	private Set<String> labelIds;

	private Long lastMessageTimestamp; // For sorting

	private Instant createdAt;
	private Instant updatedAt;
}

