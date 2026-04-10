package com.vuthevy1209.springmail.entity;

import com.vuthevy1209.springmail.enums.SyncStatus;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Document(collection = "users")
public class User {
    @Id
    private String id; // googleId

    @Indexed(unique = true)
    private String email;
    private String fullName;
    private String firstName;
    private String avatar;
    private Set<String> scopes;
    private Long lastHistoryId; // Mốc để 'Sync bù' khi server sập
    private SyncStatus syncStatus; 
    private Instant lastSyncAt; 
    private String nextPageToken; // Lưu lại để nếu đang Full Sync mà sập thì biết đường chạy tiếp
    private Instant createdAt;
    private Instant updatedAt;
    private Instant watchExpiration;
}