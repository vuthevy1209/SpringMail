package com.vuthevy1209.springmail.dto.response;

import java.util.List;

public record EmailResponse(
        String id,
        String from,
        String senderName,
        String subject,
        String date,
        String snippet,
        String content,
        boolean unread,
        List<String> attachments
) {}
