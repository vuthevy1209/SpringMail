package com.vuthevy1209.springmail.converters;

import com.vuthevy1209.springmail.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserConverter {

	public User toEntity(OidcUser oidcUser, Collection<? extends GrantedAuthority> authorities) {
		Set<String> scopes = extractScopes(authorities);

		return User.builder()
				.id(oidcUser.getAttribute("googleId"))
				.email(oidcUser.getAttribute("email"))
				.fullName(oidcUser.getAttribute("name"))
				.firstName(oidcUser.getAttribute("givenName"))
				.avatar(oidcUser.getAttribute("avatar"))
				.scopes(scopes)
				.build();
	}

	public void updateEntity(User user, OidcUser oidcUser, Collection<? extends GrantedAuthority> authorities) {
		user.setEmail(oidcUser.getAttribute("email"));
		user.setFullName(oidcUser.getAttribute("name"));
		user.setFirstName(oidcUser.getAttribute("givenName"));
		user.setAvatar(oidcUser.getAttribute("avatar"));
		user.setScopes(extractScopes(authorities));
	}

	private Set<String> extractScopes(Collection<? extends GrantedAuthority> authorities) {
		return authorities.stream()
				.map(GrantedAuthority::getAuthority)
				.filter(authority -> authority.startsWith("SCOPE_"))
				.map(authority -> authority.replaceFirst("SCOPE_", ""))
				.collect(Collectors.toSet());
	}
}
