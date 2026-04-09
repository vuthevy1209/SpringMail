package com.vuthevy1209.springmail.service.gmail.dto.profile;

import lombok.Data;

@Data
public class GmailProfileDto {
	private String emailAddress;
	private Long messagesTotal;
	private Long threadsTotal;
	private Long historyId;
}
