package com.vuthevy1209.springmail.dto.mail.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailThreadResponse {
	private String id;
	private String snippet;
	private Long historyId;
	private Set<String> labelIds;
	private String subject;
	private List<String> senderNames;
	private int messageCount;
	private Long internalDate;
	private Long lastMessageTimestamp;
	private boolean unread;
	private List<MailMessageResponse> messages;
}
