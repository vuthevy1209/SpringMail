package com.vuthevy1209.springmail.service.gmail.dto.history;

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
public class GmailHistoryDto {
	private String id;
	private List<GmailMessageDto> messages;
	private List<GmailHistoryMessageAddedDto> messagesAdded;
	private List<GmailHistoryMessageDeletedDto> messagesDeleted;
	private List<GmailHistoryLabelAddedDto> labelsAdded;
	private List<GmailHistoryLabelRemovedDto> labelsRemoved;
}
