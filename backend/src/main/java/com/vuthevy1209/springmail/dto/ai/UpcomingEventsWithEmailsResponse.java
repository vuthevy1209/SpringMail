package com.vuthevy1209.springmail.dto.ai;

import com.vuthevy1209.springmail.dto.mail.response.MailThreadResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpcomingEventsWithEmailsResponse {
    private UpcomingEventsResponse aiResult;
    private List<MailThreadResponse> relatedEmails;
}
