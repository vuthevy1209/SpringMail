package com.vuthevy1209.springmail.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDraftRequest {
    private String threadId;
    private String content;
    private String format;
}
