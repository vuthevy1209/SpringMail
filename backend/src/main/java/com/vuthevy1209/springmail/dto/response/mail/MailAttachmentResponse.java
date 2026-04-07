package com.vuthevy1209.springmail.dto.response.mail;

public record MailAttachmentResponse(
        String id,
        String filename,
        String mimeType,
        String contentId
) {}
