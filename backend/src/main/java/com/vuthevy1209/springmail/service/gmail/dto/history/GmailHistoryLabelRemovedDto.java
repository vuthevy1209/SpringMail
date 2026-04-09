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
public class GmailHistoryLabelRemovedDto {
	private GmailMessageDto message;
	private List<String> labelIds;
}
