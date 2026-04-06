package com.vuthevy1209.springmail.dto.response;

import java.util.List;

public record ThreadResponse(
        String id,
        String subject,
        String snippet,
        String latestDate,
        String latestSenderName,
        boolean unread,
        int messageCount,
        Long internalDate,
        List<EmailResponse> messages
) {}
