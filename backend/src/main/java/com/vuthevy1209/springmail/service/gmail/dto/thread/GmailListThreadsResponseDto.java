package com.vuthevy1209.springmail.service.gmail.dto.thread;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GmailListThreadsResponseDto {
	private List<GmailThreadDto> threads;
	private String nextPageToken;
	private Long resultSizeEstimate;
}
