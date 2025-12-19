package com.example.oauth2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Allow public access to Swagger UI and API docs
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/",
                                "/index.html")
                        .permitAll()
                        // Allow public access to logout endpoint
                        .requestMatchers("/api/logout").permitAll()
                        // Protect all /api/** endpoints - require authentication
                        .requestMatchers("/api/**").authenticated()
                        // Allow all other requests
                        .anyRequest().permitAll())
                .csrf(csrf -> csrf
                        // Disable CSRF for logout endpoint to allow easy testing
                        .ignoringRequestMatchers("/api/logout"))
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/swagger-ui/index.html", true))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"));

        return http.build();
    }
}
