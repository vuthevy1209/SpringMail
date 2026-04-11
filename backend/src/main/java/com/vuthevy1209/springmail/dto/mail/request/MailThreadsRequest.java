package com.vuthevy1209.springmail.dto.mail.request;

import com.vuthevy1209.springmail.dto.mail.validation.ValidMailLabel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailThreadsRequest {
    private List<@ValidMailLabel String> labelIds;
}
