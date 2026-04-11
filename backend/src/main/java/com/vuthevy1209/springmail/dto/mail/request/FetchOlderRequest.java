package com.vuthevy1209.springmail.dto.mail.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchOlderRequest {
    private List<String> labelIds;
    private String pageToken;
    private int maxResults;
    private Long beforeTimestamp;
}
