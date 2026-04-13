package com.vuthevy1209.springmail.service.auth;

import java.util.Map;

import com.vuthevy1209.springmail.enums.SyncStatus;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import com.vuthevy1209.springmail.utils.SecurityUtils;
import com.vuthevy1209.springmail.dto.auth.UserResponse;

import jakarta.servlet.ServletException;
import com.vuthevy1209.springmail.repository.UserRepository;
import com.vuthevy1209.springmail.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Override
    public UserResponse getCurrentUser() {
        OAuth2User user = SecurityUtils.getCurrentOAuth2User();
        if (user == null) {
            return null;
        }

        Map<String, Object> attributes = user.getAttributes();
        String email = (String) attributes.get("email");

        User dbUser = userRepository.findByEmail(email).orElse(null);
        SyncStatus syncStatus = dbUser != null ? dbUser.getSyncStatus() : null;
        Integer initialSyncProgress = (dbUser != null && dbUser.getInitialSyncProgress() != null) ? dbUser.getInitialSyncProgress() : 0;

        return UserResponse.builder()
                .googleId((String) attributes.get("googleId"))
                .givenName((String) attributes.get("givenName"))
                .email(email)
                .avatar((String) attributes.get("avatar"))
                .syncStatus(syncStatus)
                .initialSyncProgress(initialSyncProgress)
                .build();
    }

    @Override
    public void logout(HttpServletRequest request) throws ServletException {
        try {
            request.logout();
            if (request.getSession(false) != null) {
                request.getSession(false).invalidate();
            }
            SecurityUtils.clearContext();
        } catch (ServletException e) {
            log.error("Error during logout", e);
            throw e;
        }
    }
}
