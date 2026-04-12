package com.vuthevy1209.springmail.dto.mail.request;

import lombok.Data;
import java.util.List;

@Data
public class ThreadActionRequest {
    private List<String> addLabelIds;
    private List<String> removeLabelIds;
}
