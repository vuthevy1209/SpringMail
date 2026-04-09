package com.vuthevy1209.springmail.service.gmail.dto.thread;

import com.vuthevy1209.springmail.service.gmail.dto.message.GmailMessageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GmailThreadDto {
	private String id;
	private String snippet;
	private Long historyId;
	private List<GmailMessageDto> messages;
}
