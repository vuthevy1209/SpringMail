package com.vuthevy1209.springmail.dto.response.mail;

import java.util.List;

public record MailResponse(
        String id,
        String from,
        String to,
        String senderName,
        String senderEmail,
        String subject,
        String date,
        String snippet,
        String content,
        boolean unread,
        Long internalDate,
        List<MailAttachmentResponse> attachments
) {}
