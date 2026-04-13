package com.vuthevy1209.springmail.dto.auth;

import com.vuthevy1209.springmail.enums.SyncStatus;
import lombok.Builder;

@Builder
public record UserResponse(
    String googleId,
    String givenName,
    String email,
    String avatar,
    SyncStatus syncStatus,
    Integer initialSyncProgress
) {
}
