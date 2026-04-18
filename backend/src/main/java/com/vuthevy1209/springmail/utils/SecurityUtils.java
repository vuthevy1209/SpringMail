package com.vuthevy1209.springmail.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private static OAuth2AuthorizedClientManager authorizedClientManager;

    public SecurityUtils(OAuth2AuthorizedClientManager authorizedClientManager) {
        SecurityUtils.authorizedClientManager = authorizedClientManager;
    }

    public static OAuth2AuthorizedClient getAuthorizedClient(String registrationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(registrationId)
                .principal(authentication)
                .build();

        // Manager sẽ tự động thực hiện refresh token nếu cần thiết
        return authorizedClientManager.authorize(authorizeRequest);
    }

    public static String getAccessToken(String registrationId) {
        OAuth2AuthorizedClient client = getAuthorizedClient(registrationId);
        if (client == null) {
            return null;
        }
        return client.getAccessToken().getTokenValue();
    }

    public static String getAccessTokenForUser(String registrationId, String email) {
        if (email == null) return null;

        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(registrationId)
                .principal(email)
                .build();

        OAuth2AuthorizedClient client = authorizedClientManager.authorize(authorizeRequest);
        return (client != null) ? client.getAccessToken().getTokenValue() : null;
    }

    public static OAuth2User getCurrentOAuth2User() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User user) {
            return user;
        }
        return null;
    }

    public static String getAuthenticatedUserId() {
        OAuth2User user = getCurrentOAuth2User();
        if (user != null) {
            Object googleId = user.getAttribute("googleId");
            return googleId != null ? googleId.toString() : null;
        }
        return null;
    }

    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }
}
