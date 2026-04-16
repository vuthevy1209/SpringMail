package com.vuthevy1209.springmail.configuration.security.handler;

import com.vuthevy1209.springmail.converters.UserConverter;
import com.vuthevy1209.springmail.entity.User;
import com.vuthevy1209.springmail.repository.UserRepository;
import com.vuthevy1209.springmail.service.gmail.GmailWatchService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import com.vuthevy1209.springmail.service.mail.MailSyncService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final MailSyncService mailSyncService;
    private final GmailWatchService gmailWatchService;
    private final OAuth2AuthorizedClientService authorizedClientService;

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

		// 2. Trigger background mail sync và Cài đặt Gmail Watch (để nhận Webhook)
		OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
		String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();
		var authorizedClient = authorizedClientService.loadAuthorizedClient(clientRegistrationId, oauthToken.getName());
		if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
			String accessTokenValue = authorizedClient.getAccessToken().getTokenValue();

            // Cập nhật trạng thái PENDING/INITIAL_SYNC_IN_PROGRESS trước khi sync
            if (savedUser.getSyncStatus() == null || savedUser.getSyncStatus() == com.vuthevy1209.springmail.enums.SyncStatus.PENDING) {
                savedUser.setSyncStatus(com.vuthevy1209.springmail.enums.SyncStatus.INITIAL_SYNC_IN_PROGRESS);
                userRepository.save(savedUser);
            }

            // Chạy bất đồng bộ
            CompletableFuture.runAsync(() -> {
                try {
                    // Đăng ký nhận thông báo thay đổi mail cho user này qua Pub/Sub
                    gmailWatchService.setupWatch(savedUser, accessTokenValue);

                    // Kéo dữ liệu lần đầu hoặc Incremental
                    mailSyncService.syncMail(savedUser, accessTokenValue);
                } catch (IOException e) {
                    log.error("Background sync / watch failed for user {}: {}", email, e.getMessage());
                    savedUser.setSyncStatus(com.vuthevy1209.springmail.enums.SyncStatus.FAILED);
                    userRepository.save(savedUser);
                }
            });
		}

        // 3. Điều hướng về Frontend
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, "http://localhost:5173/inbox");
    }
}
