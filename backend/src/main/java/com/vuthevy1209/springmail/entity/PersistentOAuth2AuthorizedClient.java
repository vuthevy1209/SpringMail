package com.vuthevy1209.springmail.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "authorized_clients")
public class PersistentOAuth2AuthorizedClient {

    @Id
    private String id; // clientRegistrationId + ":" + principalName

    private String clientRegistrationId;
    private String principalName;

    private String accessTokenValue;
    private Instant accessTokenIssuedAt;
    private Instant accessTokenExpiresAt;
    private Set<String> accessTokenScopes;

    private String refreshTokenValue;
    private Instant refreshTokenIssuedAt;

    private Instant createdAt;
    private Instant updatedAt;
}
