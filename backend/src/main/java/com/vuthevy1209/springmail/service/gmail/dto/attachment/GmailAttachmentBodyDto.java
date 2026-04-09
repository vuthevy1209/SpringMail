package com.vuthevy1209.springmail.service.gmail.dto.attachment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GmailAttachmentBodyDto {
	private String data; // Base64 encoded
	private Long size;
	private String attachmentId;
}
