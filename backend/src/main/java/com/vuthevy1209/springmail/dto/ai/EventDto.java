package com.vuthevy1209.springmail.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private String title;
    private String date; // Event date/time
    private String location;
    private String description;
    private String threadId;
}
