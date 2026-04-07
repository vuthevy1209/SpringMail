package com.vuthevy1209.springmail.security;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOidcUserService extends OidcUserService {

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Load the full user profile from the provider (Google)
        OidcUser oidcUser = super.loadUser(userRequest);
        Map<String, Object> attributes = oidcUser.getAttributes();

        // Filter and rename attributes as requested
        Map<String, Object> filteredAttributes = new HashMap<>();
        filteredAttributes.put("google-id", attributes.get("sub"));
        filteredAttributes.put("given_name", attributes.get("given_name"));
        filteredAttributes.put("email", attributes.get("email"));
        filteredAttributes.put("avatar", attributes.get("picture"));

        // Return a custom OidcUser implementation with filtered attributes
        return new FilteredOidcUser(oidcUser, filteredAttributes);
    }

    /**
     * Named static class for better serialization support in Redis.
     */
    public static class FilteredOidcUser extends DefaultOidcUser {
        private final Map<String, Object> filteredAttributes;

        public FilteredOidcUser(OidcUser originalUser, Map<String, Object> filteredAttributes) {
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
