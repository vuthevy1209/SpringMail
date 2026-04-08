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

        return authorizedClientManager.authorize(authorizeRequest);
    }

    public static String getAccessToken(String registrationId) {
        OAuth2AuthorizedClient client = getAuthorizedClient(registrationId);
        if (client == null) {
            return null;
        }
        return client.getAccessToken().getTokenValue();
    }

    public static OAuth2User getCurrentOAuth2User() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User user) {
            return user;
        }
        return null;
    }

    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }
}
