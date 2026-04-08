package com.vuthevy1209.springmail.entity;

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
    private String id;

    @Indexed(unique = true)
    private String googleId;

    @Indexed(unique = true)
    private String email;

    private String fullName;
    private String firstName;
    private String avatar;
    private Set<String> scopes;

    private String lastHistoryId; // Mốc để 'Sync bù' khi server sập

    private String syncStatus; // IN_PROGRESS, COMPLETED, FAILED

    private Instant lastSyncAt; // Lần cuối cùng gọi API sync thành công

    private String nextPageToken; // Lưu lại để nếu đang Full Sync mà sập thì biết đường chạy tiếp

    private Instant createdAt;
    private Instant updatedAt;

    // Trường này dùng để kiểm tra xem Watch (Push Notification) còn hạn không
    private Instant watchExpiration;
}