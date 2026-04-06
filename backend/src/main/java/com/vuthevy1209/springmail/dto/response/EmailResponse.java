package com.vuthevy1209.springmail.dto.response;

public record EmailResponse(
        String from,
        String subject,
        String snippet
) {}
