package com.vuthevy1209.springmail.service.auth;

import com.vuthevy1209.springmail.dto.auth.UserResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    UserResponse getCurrentUser();

    void logout(HttpServletRequest request) throws ServletException;
}
