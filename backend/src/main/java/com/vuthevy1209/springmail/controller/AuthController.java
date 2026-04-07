package com.vuthevy1209.springmail.controller;

import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.ServletException;
import com.vuthevy1209.springmail.dto.response.ApiResponse;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2User user = (OAuth2User) authentication.getPrincipal();

//        System.out.println("User attributes: " + user.getAttributes());
//        System.out.println("User authorities: " + user.getAuthorities());
//        System.out.println("User name: " + user.getName());
        
        return ApiResponse.<Map<String, Object>>builder()
                .result(user.getAttributes())
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request) throws ServletException {
        request.logout();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        return ApiResponse.<String>builder()
                .result("Logged out successfully")
                .build();
    }
}
