package com.vuthevy1209.springmail.controller;

import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

//    @GetMapping("/me")
//    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OAuth2User user) {
//        if (user == null) {
//            return ResponseEntity.status(401).body("Unauthenticated");
//        }
//
//        System.out.println("User attributes: " + user.getAttributes());
//        return ResponseEntity.ok(user.getAttributes());
//    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        OAuth2User user = null;
        if (authentication != null) {
            user = (OAuth2User) authentication.getPrincipal();

            System.out.println("User attributes: " + user.getAttributes());
            System.out.println("User authorities: " + user.getAuthorities());
            System.out.println("User name: " + user.getName());
            return ResponseEntity.ok(user.getAttributes());
        }

        return ResponseEntity.status(401).body("Unauthenticated");
    }
}
