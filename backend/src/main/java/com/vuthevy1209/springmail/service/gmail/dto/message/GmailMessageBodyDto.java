package com.vuthevy1209.springmail.service.gmail.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GmailMessageBodyDto {
	private String data;
	private String attachmentId;
	private Integer size;
}
