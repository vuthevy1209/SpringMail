package com.vuthevy1209.springmail.service.mail;

import com.vuthevy1209.springmail.dto.ai.AiSummaryResponse;
import com.vuthevy1209.springmail.dto.ai.AiDraftResponse;
import com.vuthevy1209.springmail.dto.ai.MailVectorDto;

public interface MailAiService {
    AiSummaryResponse summarize(String threadId, String emailContent);
    
    AiDraftResponse generateDraft(String threadId, String emailContent, String format);
    
    void saveMailToVectorStore(MailVectorDto mailVectorDto);
    
    void deleteMailFromVectorStore(String mailId);
}
