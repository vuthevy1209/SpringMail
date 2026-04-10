package com.vuthevy1209.springmail.configuration;

import com.vuthevy1209.springmail.enums.SyncStatus;
import com.vuthevy1209.springmail.converters.UserConverter;
import com.vuthevy1209.springmail.entity.User;
import com.vuthevy1209.springmail.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.time.Instant;

import com.vuthevy1209.springmail.service.mail.MailSyncService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final MailSyncService mailSyncService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    private final UserConverter userConverter;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String googleId = oidcUser.getAttribute("googleId");
        String email = oidcUser.getAttribute("email");

        // 1. Upsert User vào MongoDB
        User user = userRepository.findByGoogleId(googleId);
        Instant now = Instant.now();

        if (user == null) {
            user = userConverter.toEntity(oidcUser, authentication.getAuthorities());
            log.info("Created new user: {}", email);
        } else {
            userConverter.updateEntity(user, oidcUser, authentication.getAuthorities());
            log.info("Updated existing user: {}", email);
        }
        user.setUpdatedAt(now);
        User savedUser = userRepository.save(user);

		// 2. Trigger background mail sync
		OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
		String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();
		var authorizedClient = authorizedClientService.loadAuthorizedClient(clientRegistrationId, oauthToken.getName());
		if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
			String accessTokenValue = authorizedClient.getAccessToken().getTokenValue();
			CompletableFuture.runAsync(() -> {
				try {
					mailSyncService.syncMail(savedUser, accessTokenValue);
				} catch (IOException e) {
					System.err.println("Background sync failed for user " + email + ": " + e.getMessage());
				}
			});
		}

        // 3. Điều hướng về Frontend
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, "http://localhost:5173/inbox");
    }
}
