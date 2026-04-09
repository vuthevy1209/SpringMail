package com.vuthevy1209.springmail.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Document(collection = "mail_messages")
public class MailMessage {

    @Id
    private String id; // Gmail Message ID

    @Indexed
    private String threadId; // Gmail Thread ID

    @Indexed
    private String userId;

    private Set<String> labelIds;
    
    private String snippet;
    
    private String subject;
    
    private String from;
    
    private String to;
    
    private Long internalDate; // Use this for sorting within threads
    
    private String bodyHtml;
    
    private String historyId;
}
