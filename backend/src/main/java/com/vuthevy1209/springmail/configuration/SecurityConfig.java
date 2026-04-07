package com.vuthevy1209.springmail.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;

import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import com.vuthevy1209.springmail.security.CustomOidcUserService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableRedisHttpSession(redisNamespace = "springmail:session", maxInactiveIntervalInSeconds = 3600) // 1 ngày
public class SecurityConfig {

    private final CustomOidcUserService customOidcUserService;
    private final OAuth2AuthorizationRequestResolver authorizationRequestResolver;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                // Tắt CSRF vì frontend và backend nằm trên 2 domain khác nhau
                .csrf(AbstractHttpConfigurer::disable)
                // Sử dụng HttpSessionSecurityContextRepository để lưu security context vào session
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/oauth2/**", "/auth/logout").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        // Đảm bảo authorized client được lưu vào session
                        .authorizedClientRepository(authorizedClientRepository())
                        // Cấu hình custom AuthorizationRequestResolver để thêm tham số access_type=offline, prompt=consent;
                        // Mục đích chính là để đảm bảo refresh token được cấp phát nếu access token hết hạn
                        .authorizationEndpoint(auth -> auth
                                .authorizationRequestResolver(authorizationRequestResolver))
                        // Cấu hình custom OidcUserService để map thông tin user từ Google lưu vào session
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService))
                        // Sau khi đăng nhập thành công, redirect trình duyệt về frontend với cookie
                        .defaultSuccessUrl("http://localhost:5173/inbox", true))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint));

        return http.build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new HttpSessionOAuth2AuthorizedClientRepository();
    }
}
