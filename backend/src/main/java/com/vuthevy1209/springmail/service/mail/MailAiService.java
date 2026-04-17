package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.dto.AiSummaryResponse;

public interface MailAiService {
    AiSummaryResponse summarize(String threadId, String emailContent);
}