package com.vuthevy1209.springmail.service.gmail.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GmailMessagePartDto {
	private String partId;
	private String mimeType;
	private String filename;
	private List<GmailHeaderDto> headers;
	private GmailMessageBodyDto body;
	private List<GmailMessagePartDto> parts;
}
