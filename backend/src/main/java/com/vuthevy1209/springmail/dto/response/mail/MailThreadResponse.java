package com.vuthevy1209.springmail.dto.response.mail;

import java.util.List;

public record MailThreadResponse(
        String id,
        String subject,
        String snippet,
        String latestDate,
        String latestSenderName,
        boolean unread,
        int messageCount,
        Long internalDate,
        List<MailResponse> messages
) {}
