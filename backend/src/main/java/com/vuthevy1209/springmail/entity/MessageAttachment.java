package com.vuthevy1209.springmail.entity;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MessageAttachment {
    private String id; // attachmentId from Gmail API
    private String messageId;
    private String filename;
    private String mimeType;
    private Long size;
    private String contentId;
}
