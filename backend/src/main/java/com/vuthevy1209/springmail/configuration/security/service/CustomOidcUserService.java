package com.vuthevy1209.springmail.configuration.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Load the full user profile from Google
        OidcUser oidcUser = super.loadUser(userRequest);
        Map<String, Object> attributes = oidcUser.getAttributes();

        // Chuẩn bị filtered attributes để lưu vào Redis Session (giảm dung lượng)
        Map<String, Object> filteredAttributes = new HashMap<>();
        filteredAttributes.put("googleId", attributes.get("sub"));
        filteredAttributes.put("email", attributes.get("email"));
        filteredAttributes.put("name", attributes.get("name"));
        filteredAttributes.put("givenName", attributes.get("given_name"));
        filteredAttributes.put("avatar", attributes.get("picture"));

        return new FilteredOidcUser(oidcUser, filteredAttributes);
    }

    /**
     * Named static class for better serialization support in Redis.
     * Only stores essential profile info.
     */
    public static class FilteredOidcUser extends DefaultOidcUser {
        private final Map<String, Object> filteredAttributes;

        public FilteredOidcUser(OidcUser originalUser, Map<String, Object> filteredAttributes) {
            // "email" is designated as the 'name' attribute for SecurityContext
            super(originalUser.getAuthorities(), originalUser.getIdToken(), originalUser.getUserInfo(), "email");
            this.filteredAttributes = filteredAttributes;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return filteredAttributes;
        }

        @Override
        public String getName() {
            return (String) filteredAttributes.get("email");
        }
    }
}
