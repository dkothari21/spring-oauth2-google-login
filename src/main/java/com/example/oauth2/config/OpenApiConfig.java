package com.example.oauth2.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Google OAuth2 API")
                        .version("1.0")
                        .description("API secured with Google OAuth2 authentication.\n\n" +
                                "**How to test the protected endpoints:**\n\n" +
                                "1. Open a new tab and visit: http://localhost:8080/oauth2/authorization/google\n" +
                                "2. Complete the Google login flow\n" +
                                "3. Return to this Swagger UI tab\n" +
                                "4. Your session is now authenticated - try the API endpoints below!\n\n" +
                                "The API uses session-based authentication after OAuth2 login."));
    }
}
