package com.vuthevy1209.springmail.configuration.security.handler;

import com.vuthevy1209.springmail.converters.UserConverter;
import com.vuthevy1209.springmail.entity.User;
import com.vuthevy1209.springmail.enums.SyncStatus;
import com.vuthevy1209.springmail.event.UserLoggedInEvent;
import com.vuthevy1209.springmail.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final ApplicationEventPublisher eventPublisher;

    private final UserConverter userConverter;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                        @NonNull Authentication authentication) throws IOException {

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        
        if (oidcUser == null) {
            log.error("Authentication failed: OidcUser is null");
            getRedirectStrategy().sendRedirect(request, response, "http://localhost:5173/login?error=missing_user");
            return;
        }

        String email = oidcUser.getAttribute("email");
        
        if (email == null) {
            log.error("Authentication failed: email attribute is missing from OidcUser");
            getRedirectStrategy().sendRedirect(request, response, "http://localhost:5173/login?error=missing_email");
            return;
        }

        // 1. Upsert User vào MongoDB
        User user = userRepository.findByEmail(email)
                .map(existingUser -> {
                    userConverter.updateEntity(existingUser, oidcUser, authentication.getAuthorities());
                    log.info("Updated existing user: {}", email);
                    return existingUser;
                })
                .orElseGet(() -> {
                    User newUser = userConverter.toEntity(oidcUser, authentication.getAuthorities());
                    log.info("Created new user: {}", email);
                    return newUser;
                });

        user.setUpdatedAt(Instant.now());
        User savedUser = userRepository.save(user);

        // 2. Publish login event to trigger background sync and watch setup
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();
        var authorizedClient = authorizedClientService.loadAuthorizedClient(clientRegistrationId, oauthToken.getName());
        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            String accessTokenValue = authorizedClient.getAccessToken().getTokenValue();

            // update status PENDING/INITIAL_SYNC_IN_PROGRESS before sync
            if (savedUser.getSyncStatus() == null || savedUser.getSyncStatus() == SyncStatus.PENDING) {
                savedUser.setSyncStatus(SyncStatus.INITIAL_SYNC_IN_PROGRESS);
                userRepository.save(savedUser);
            }

            log.info("Publishing UserLoggedInEvent for user: {}", email);
            eventPublisher.publishEvent(new UserLoggedInEvent(this, savedUser, accessTokenValue));
        }

        // 3. redirect to Frontend
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, "http://localhost:5173/inbox");
    }
}
