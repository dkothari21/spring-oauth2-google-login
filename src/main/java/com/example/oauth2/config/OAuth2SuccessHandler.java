package com.example.oauth2.config;

import com.example.oauth2.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    public OAuth2SuccessHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        // Generate JWT from OAuth2 user
        String jwt = jwtUtil.generateToken(oauth2User);

        // Create JSON object with tokens (like the website you showed)
        String tokensJson = String.format(
                "{\"access_token\":\"%s\",\"token_type\":\"Bearer\",\"expires_in\":604800}",
                jwt);

        // URL encode the JSON
        String encodedTokens = java.net.URLEncoder.encode(tokensJson, "UTF-8");

        // Store JWT in cookie
        Cookie tokensCookie = new Cookie("auth_token", encodedTokens);
        tokensCookie.setHttpOnly(true);
        tokensCookie.setPath("/");
        tokensCookie.setMaxAge(7 * 24 * 60 * 60); // 604800 seconds = 7 days
        tokensCookie.setSecure(false); // Set to true in production with HTTPS
        response.addCookie(tokensCookie);

        // Create session ID cookie
        String sessionId = java.util.UUID.randomUUID().toString().replace("-", "");
        Cookie sessionCookie = new Cookie("session_id", sessionId);
        sessionCookie.setHttpOnly(true);
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(sessionCookie);

        // Redirect to Swagger
        getRedirectStrategy().sendRedirect(request, response, "/swagger-ui/index.html");
    }
}
