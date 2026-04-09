package com.vuthevy1209.springmail.service.gmail.dto.attachment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GmailAttachmentDto {
	private String attachmentId;
	private String filename;
	private String mimeType;
	private Long size;
	private String contentId;
	private String data; // Base64 encoded
}
