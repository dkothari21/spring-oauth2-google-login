package com.example.oauth2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Hello API", description = "Protected API endpoints requiring Google OAuth2 authentication")
public class HelloController {

    @GetMapping("/hello")
    @Operation(summary = "Get personalized greeting", description = "Returns a greeting with the authenticated user's information from Google")
    public Map<String, Object> hello(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> response = new HashMap<>();

        if (principal != null) {
            response.put("message", "Hello, " + principal.getAttribute("name") + "!");
            response.put("email", principal.getAttribute("email"));
            response.put("name", principal.getAttribute("name"));
            response.put("picture", principal.getAttribute("picture"));
            response.put("authenticated", true);
        } else {
            response.put("message", "Hello, Guest!");
            response.put("authenticated", false);
        }

        return response;
    }

    @GetMapping("/user")
    @Operation(summary = "Get current user details", description = "Returns all available information about the authenticated user from Google")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> response = new HashMap<>();

        if (principal != null) {
            response.put("attributes", principal.getAttributes());
            response.put("authorities", principal.getAuthorities());
        }

        return response;
    }

    @GetMapping("/logout")
    @Operation(summary = "Logout", description = "Clears session and redirects to login page. NOTE: Visit this URL directly in browser: http://localhost:8080/api/logout (won't work from Swagger)")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect("/");
    }
}
