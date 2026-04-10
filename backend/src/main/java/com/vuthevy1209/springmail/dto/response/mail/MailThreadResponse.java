package com.vuthevy1209.springmail.dto.response.mail;

import java.util.List;
import java.util.Set;

public record MailThreadResponse(
	String id,
	String subject,
	String snippet,
	String latestDate,
	String latestSenderName,
	List<String> senderNames,
	boolean unread,
	int messageCount,
	Long internalDate,
	Long lastMessageTimestamp,
	Long historyId,
	Set<String> labelIds,
	List<MailResponse> messages
) {}
