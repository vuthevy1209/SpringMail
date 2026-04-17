package com.vuthevy1209.springmail.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiSummaryRequest {
    private String threadId;
    private String content;
}