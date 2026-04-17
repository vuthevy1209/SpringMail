package com.vuthevy1209.springmail.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailVectorDto {
    private String mailId;
    private String threadId;
    private String userId;
    private String subject;
    private String sender;
    private String content; // bodyText
    private String dateStr;
}
