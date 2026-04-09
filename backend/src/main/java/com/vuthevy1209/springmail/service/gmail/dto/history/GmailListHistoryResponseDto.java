package com.vuthevy1209.springmail.service.gmail.dto.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GmailListHistoryResponseDto {
	private List<GmailHistoryDto> history;
	private String nextPageToken;
	private Long historyId;
}
