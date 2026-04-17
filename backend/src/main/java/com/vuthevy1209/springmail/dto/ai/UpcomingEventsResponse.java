package com.vuthevy1209.springmail.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpcomingEventsResponse {
    private List<EventDto> events;
    private String rawAnalysis;
}
