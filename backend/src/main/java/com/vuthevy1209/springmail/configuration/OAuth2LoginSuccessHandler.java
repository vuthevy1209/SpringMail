package com.vuthevy1209.springmail.configuration;

import com.vuthevy1209.springmail.entity.User;
import com.vuthevy1209.springmail.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    // private final GmailSyncService syncService; // Service bạn sẽ viết để fetch mail

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        System.out.println(oidcUser);

        // Những attribute này đã được CustomOidcUserService lọc và đưa vào
        String googleId = oidcUser.getAttribute("googleId");
        String email = oidcUser.getAttribute("email");
        String name = oidcUser.getAttribute("name");
        String firstName = oidcUser.getAttribute("givenName");
        String avatar = oidcUser.getAttribute("avatar");
        Set<String> scopes = authentication.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("SCOPE_"))
                .map(authority -> authority.replaceFirst("SCOPE_", ""))
                .collect(Collectors.toSet());

        // 1. Upsert User vào MongoDB
        User user = userRepository.findByGoogleId(googleId);
        java.time.Instant now = java.time.Instant.now();

        if (user == null) {
            user = User.builder()
                    .googleId(googleId)
                    .email(email)
                    .fullName(name)
                    .firstName(firstName)
                    .avatar(avatar)
                    .scopes(scopes)
                    .syncStatus("PENDING")
                    .createdAt(now)
                    .build();
            System.out.println("Created new user: " + email);
        } else {
            user.setEmail(email);
            user.setFullName(name);
            user.setFirstName(firstName);
            user.setAvatar(avatar);
            user.setScopes(scopes);
            System.out.println("Updated existing user: " + email);
        }
        user.setUpdatedAt(now);
        userRepository.save(user);

        // 2. Điều hướng về Frontend
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, "http://localhost:5173/inbox");
    }
}
