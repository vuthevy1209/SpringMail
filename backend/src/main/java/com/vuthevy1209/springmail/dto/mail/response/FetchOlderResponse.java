package com.vuthevy1209.springmail.dto.mail.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchOlderResponse {
    private String nextPageToken;
    private int fetchedCount;
}
