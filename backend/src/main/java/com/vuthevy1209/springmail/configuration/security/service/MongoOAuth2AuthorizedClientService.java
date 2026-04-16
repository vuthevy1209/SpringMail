package com.vuthevy1209.springmail.configuration.security.service;

import com.vuthevy1209.springmail.entity.PersistentOAuth2AuthorizedClient;
import com.vuthevy1209.springmail.repository.PersistentOAuth2AuthorizedClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MongoOAuth2AuthorizedClientService implements OAuth2AuthorizedClientService {

    private final PersistentOAuth2AuthorizedClientRepository repository;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Override
    @SuppressWarnings("unchecked")
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, String principalName) {
        return (T) repository.findByClientRegistrationIdAndPrincipalName(clientRegistrationId, principalName)
                .map(persistentClient -> {
                    ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(clientRegistrationId);
                    
                    OAuth2AccessToken accessToken = new OAuth2AccessToken(
                            OAuth2AccessToken.TokenType.BEARER,
                            persistentClient.getAccessTokenValue(),
                            persistentClient.getAccessTokenIssuedAt(),
                            persistentClient.getAccessTokenExpiresAt(),
                            persistentClient.getAccessTokenScopes()
                    );
                    
                    OAuth2RefreshToken refreshToken = null;
                    if (persistentClient.getRefreshTokenValue() != null) {
                        refreshToken = new OAuth2RefreshToken(
                                persistentClient.getRefreshTokenValue(),
                                persistentClient.getRefreshTokenIssuedAt()
                        );
                    }
                    
                    return new OAuth2AuthorizedClient(registration, principalName, accessToken, refreshToken);
                })
                .orElse(null);
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
        String clientRegistrationId = authorizedClient.getClientRegistration().getRegistrationId();
        String principalName = principal.getName();
        
        PersistentOAuth2AuthorizedClient persistentClient = repository
                .findByClientRegistrationIdAndPrincipalName(clientRegistrationId, principalName)
                .orElse(PersistentOAuth2AuthorizedClient.builder()
                        .id(clientRegistrationId + ":" + principalName)
                        .clientRegistrationId(clientRegistrationId)
                        .principalName(principalName)
                        .createdAt(Instant.now())
                        .build());

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        persistentClient.setAccessTokenValue(accessToken.getTokenValue());
        persistentClient.setAccessTokenIssuedAt(accessToken.getIssuedAt());
        persistentClient.setAccessTokenExpiresAt(accessToken.getExpiresAt());
        persistentClient.setAccessTokenScopes(accessToken.getScopes());

        OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
        if (refreshToken != null) {
            persistentClient.setRefreshTokenValue(refreshToken.getTokenValue());
            persistentClient.setRefreshTokenIssuedAt(refreshToken.getIssuedAt());
        }

        persistentClient.setUpdatedAt(Instant.now());
        repository.save(persistentClient);
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
        repository.deleteByClientRegistrationIdAndPrincipalName(clientRegistrationId, principalName);
    }
}
