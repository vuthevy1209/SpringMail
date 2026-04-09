package com.vuthevy1209.springmail.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Document(collection = "mail_threads")
public class MailThread {

    @Id
    private String id; // Gmail Thread ID

    @Indexed
    private String userId; // Link to User

    private String historyId;
    
    private String snippet;

    private Long lastMessageTimestamp; // For sorting

    private Instant createdAt;
    private Instant updatedAt;
}
