package com.vuthevy1209.springmail.dto.mail.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageAttachmentResponse {
	private String id;
	private String filename;
	private String mimeType;
	private Long size;
	private String contentId;
	private String data;
}
