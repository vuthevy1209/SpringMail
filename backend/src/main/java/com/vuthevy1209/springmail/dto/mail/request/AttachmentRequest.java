package com.vuthevy1209.springmail.dto.mail.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentRequest {

    @NotBlank(message = "messageId is required")
    private String messageId;

    @NotBlank(message = "attachmentId is required")
    private String attachmentId;

    private String filename;
    private String mimeType;
}