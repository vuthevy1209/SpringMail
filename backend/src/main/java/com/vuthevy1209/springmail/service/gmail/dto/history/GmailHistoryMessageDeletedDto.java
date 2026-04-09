package com.vuthevy1209.springmail.service.gmail.dto.history;

import com.vuthevy1209.springmail.service.gmail.dto.message.GmailMessageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GmailHistoryMessageDeletedDto {
	private GmailMessageDto message;
}
