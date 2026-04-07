package com.vuthevy1209.springmail.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.ServletException;
import com.vuthevy1209.springmail.dto.response.ApiResponse;
import com.vuthevy1209.springmail.dto.response.auth.UserResponse;
import com.vuthevy1209.springmail.service.auth.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser() {
        return ApiResponse.<UserResponse>builder()
                .result(authService.getCurrentUser())
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request) throws ServletException {
        authService.logout(request);
        return ApiResponse.<String>builder()
                .result("Logged out successfully")
                .build();
    }
}
