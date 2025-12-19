package com.example.oauth2.config;

import com.example.oauth2.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String jwt = getJwtFromCookies(request);

        if (jwt != null && jwtUtil.validateToken(jwt)) {
            Claims claims = jwtUtil.getClaims(jwt);

            // Create OAuth2User from JWT
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("email", claims.getSubject());
            attributes.put("name", claims.get("name"));
            attributes.put("picture", claims.get("picture"));

            OAuth2User oauth2User = new DefaultOAuth2User(
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                    attributes,
                    "email");

            // Set authentication
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(oauth2User, null,
                    oauth2User.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("auth_token".equals(cookie.getName())) {
                    try {
                        // Decode URL-encoded JSON
                        String decoded = java.net.URLDecoder.decode(cookie.getValue(), "UTF-8");
                        // Extract access_token from JSON
                        // Simple parsing:
                        // {"access_token":"TOKEN","token_type":"Bearer","expires_in":604800}
                        int start = decoded.indexOf("\"access_token\":\"") + 16;
                        int end = decoded.indexOf("\"", start);
                        return decoded.substring(start, end);
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }
}
