# Architecture Overview

This document explains how OAuth2, OIDC, JWT, Spring Security, and Swagger work together in this application.

## Table of Contents

1. [High-Level Architecture](#high-level-architecture)
2. [OAuth2 & OIDC Flow](#oauth2--oidc-flow)
3. [Component Breakdown](#component-breakdown)
4. [Security Layer](#security-layer)
5. [Data Flow](#data-flow)
6. [Session Management](#session-management)

## High-Level Architecture

```mermaid
graph TB
    subgraph "Client (Browser)"
        A[User/Browser]
        B[Login Page]
        C[Swagger UI]
    end
    
    subgraph "Spring Boot Application"
        D[SecurityConfig]
        E[OAuth2 Client]
        F[Controllers]
        G[OpenAPI Config]
        H[Logout Endpoint]
    end
    
    subgraph "Google Services"
        I[Google OAuth2]
        J[UserInfo Endpoint]
    end
    
    A -->|1. Visit root| B
    B -->|2. Click Sign in with Google| E
    E -->|3. Redirect to Google with prompt| I
    I -->|4. User Selects Account and Logs In| A
    I -->|5. Authorization Code| E
    E -->|6. Exchange for Tokens| I
    E -->|7. Fetch User Info| J
    J -->|8. User Attributes| E
    E -->|9. Create Session| D
    D -->|10. Redirect to Swagger| C
    C -->|11. Test APIs| F
    F -->|12. Return User Data| C
    A -->|13. Visit logout URL| H
    H -->|14. Clear Session| D
    D -->|15. Redirect to Login| B
```

## OAuth2 & OIDC Flow

### The Complete Authentication Journey

```mermaid
sequenceDiagram
    participant U as User (Browser)
    participant LP as Login Page
    participant S as Spring Boot App
    participant G as Google OAuth2
    participant UI as Google UserInfo API
    
    Note over U,UI: Step 1: User Visits Login Page
    U->>LP: Visit http://localhost:8080/
    LP->>U: Show login page with Google button
    
    Note over U,UI: Step 2: User Initiates Login
    U->>LP: Click "Sign in with Google"
    LP->>S: Navigate to /oauth2/authorization/google
    S->>S: Generate OAuth2 request
    S->>U: Redirect to Google with prompt=select_account
    
    Note over U,UI: Step 3: Google Authentication
    U->>G: GET /o/oauth2/v2/auth<br/>?client_id=...&redirect_uri=...&scope=openid+profile+email<br/>&prompt=select_account
    G->>U: Show Account Selection Page
    U->>G: Select account (user@example.com)
    G->>U: Show Google Login Page (if needed)
    U->>G: Enter credentials & grant permissions
    
    Note over U,UI: Step 4: Authorization Code Exchange
    G->>U: Redirect with authorization code
    U->>S: GET /login/oauth2/code/google?code=ABC123...
    S->>G: POST /token<br/>code=ABC123&client_id=...&client_secret=...
    G->>S: Return tokens<br/>{access_token, id_token (JWT), refresh_token}
    
    Note over U,UI: Step 5: Fetch User Information
    S->>UI: GET /oauth2/v3/userinfo<br/>Authorization: Bearer {access_token}
    UI->>S: Return user data<br/>{sub, name, email, picture}
    
    Note over U,UI: Step 6: Create Session
    S->>S: Validate JWT (ID Token)
    S->>S: Create OAuth2User object
    S->>S: Store in session
    S->>U: Set session cookie (JSESSIONID)
    S->>U: Redirect to Swagger UI
    
    Note over U,UI: Step 7: Access Protected APIs
    U->>S: GET /api/hello<br/>Cookie: JSESSIONID=...
    S->>S: Validate session
    S->>S: Extract OAuth2User from session
    S->>S: Inject into @AuthenticationPrincipal
    S->>U: Return personalized response
    
    Note over U,UI: Step 8: Logout
    U->>S: Visit http://localhost:8080/api/logout
    S->>S: Invalidate session
    S->>U: Redirect to login page
    U->>LP: Back to login page
```

### What is OIDC?

**OpenID Connect (OIDC)** is an identity layer on top of OAuth2. It adds:

- **ID Token**: A JWT containing user identity information
- **UserInfo Endpoint**: Standardized endpoint for user profile data
- **Standard Claims**: Predefined fields like `name`, `email`, `picture`

In this app, when you request the `openid` scope, you're using OIDC!

### What is JWT?

**JSON Web Token (JWT)** is a compact, URL-safe token format. The `id_token` from Google is a JWT.

```mermaid
graph LR
    A[JWT Structure] --> B[Header]
    A --> C[Payload]
    A --> D[Signature]
    
    B --> B1[Algorithm: RS256]
    B --> B2[Type: JWT]
    
    C --> C1[sub: User ID]
    C --> C2[name: Full Name]
    C --> C3[email: Email Address]
    C --> C4[exp: Expiration]
    
    D --> D1[Cryptographic Signature]
    D --> D2[Verifies Authenticity]
```

Spring Security automatically:
- Validates the JWT signature
- Extracts user information
- Creates an `OAuth2User` object

## Component Breakdown

### Architecture Components

```mermaid
graph TD
    subgraph "Configuration Layer"
        A[SecurityConfig.java]
        B[OpenApiConfig.java]
        C[application.yml]
    end
    
    subgraph "Controller Layer"
        D[HelloController.java]
    end
    
    subgraph "Spring Security"
        E[OAuth2LoginAuthenticationFilter]
        F[OAuth2AuthorizationRequestRedirectFilter]
        G[SecurityFilterChain]
    end
    
    subgraph "OAuth2 Client"
        H[ClientRegistration]
        I[OAuth2AuthorizedClient]
        J[OAuth2UserService]
    end
    
    C -->|Configures| H
    A -->|Defines| G
    G -->|Contains| E
    G -->|Contains| F
    E -->|Uses| J
    J -->|Creates| I
    D -->|Receives| I
    B -->|Documents| D
```

### 1. SecurityConfig.java

**Purpose**: Defines security rules for the application.

```java
.requestMatchers("/api/**").authenticated()
```
- All `/api/**` endpoints require authentication
- Swagger UI is public (so you can access it before logging in)
- OAuth2 login is enabled

**Key Concept**: This is the "bouncer" at the door. It checks if you're logged in before letting you access protected endpoints.

### 2. OpenApiConfig.java

**Purpose**: Configures Swagger UI with clear authentication instructions.

Provides users with step-by-step instructions on how to authenticate using Spring Security's OAuth2 login flow.

### 3. HelloController.java

**Purpose**: Example API endpoints demonstrating authentication.

```java
public Map<String, Object> hello(@AuthenticationPrincipal OAuth2User principal)
```

**The Magic**: `@AuthenticationPrincipal` automatically injects the authenticated user!

Spring Security:
1. Checks the session cookie
2. Finds the authenticated user
3. Injects it as the `principal` parameter

No manual token parsing needed!

### 4. application.yml

**Purpose**: OAuth2 client configuration.

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: [openid, profile, email]
```

This tells Spring:
- "I want to use Google as an OAuth2 provider"
- "Here are my credentials"
- "Request these scopes"
- "Use this redirect URI"

## Security Layer

### How Spring Security Protects Your API

```mermaid
flowchart TD
    A[Incoming Request] --> B{Is URL /api/**?}
    B -->|No| C[Allow Access]
    B -->|Yes| D{Is User Authenticated?}
    D -->|No| E[Return 401 Unauthorized<br/>or Redirect to Login]
    D -->|Yes| F{Extract Session}
    F --> G[Find OAuth2User in Session]
    G --> H[Inject into @AuthenticationPrincipal]
    H --> I[Execute Controller Method]
    I --> J[Return Response]
```

### Session Management

```mermaid
graph LR
    subgraph "After OAuth2 Login"
        A[Spring Security] -->|Creates| B[Session]
        B -->|Stores| C[OAuth2User Object]
        C -->|Contains| D[User Attributes]
    end
    
    subgraph "Subsequent Requests"
        E[Browser] -->|Sends| F[Session Cookie]
        F -->|JSESSIONID| G[Spring Security]
        G -->|Looks Up| B
        B -->|Returns| C
    end
```

After successful OAuth2 login:
1. Spring creates a session
2. Session ID stored in cookie: `JSESSIONID`
3. Session contains `OAuth2User` object
4. Subsequent requests include the cookie
5. Spring looks up the session and finds the user

**Important**: The JWT is validated once during login. After that, the session cookie is used.

## Data Flow

### Complete Request Flow for GET /api/hello

```mermaid
sequenceDiagram
    participant B as Browser (Swagger UI)
    participant SF as Spring Security Filter
    participant S as Session Store
    participant C as HelloController
    
    Note over B,C: User is already authenticated
    B->>SF: GET /api/hello<br/>Cookie: JSESSIONID=ABC123
    SF->>SF: Check if /api/** requires auth ✓
    SF->>S: Lookup session ABC123
    S->>SF: Return OAuth2User object
    SF->>SF: User is authenticated ✓
    SF->>C: Call hello(OAuth2User principal)
    Note over C: principal.getAttribute("name")<br/>principal.getAttribute("email")
    C->>C: Build response with user data
    C->>SF: Return response object
    SF->>B: 200 OK<br/>{message, email, name, picture}
```

### Authentication State Diagram

```mermaid
stateDiagram-v2
    [*] --> Anonymous: User visits app
    Anonymous --> LoginRedirect: Access /api/** endpoint
    LoginRedirect --> GoogleAccountSelection: Redirect to Google
    GoogleAccountSelection --> GoogleLogin: Select account (prompt=select_account)
    GoogleLogin --> CodeExchange: User logs in
    CodeExchange --> TokenValidation: Receive auth code
    TokenValidation --> UserInfoFetch: Exchange for tokens
    UserInfoFetch --> SessionCreated: Fetch user details
    SessionCreated --> Authenticated: Store in session
    Authenticated --> Authenticated: Subsequent API calls
    Authenticated --> LoggedOut: POST /api/logout
    LoggedOut --> [*]: Session invalidated
    Authenticated --> [*]: Session expires
```

## Key Concepts Explained

### OAuth2 vs OIDC vs JWT

| Concept | What It Is | Role in This App |
|---------|-----------|------------------|
| **OAuth2** | Authorization framework | Allows app to access Google on your behalf |
| **OIDC** | Identity layer on OAuth2 | Provides user identity information |
| **JWT** | Token format | Google's ID token is a JWT |

### Scopes Explained

```mermaid
graph TD
    A[OAuth2 Scopes] --> B[openid]
    A --> C[profile]
    A --> D[email]
    
    B --> B1[Enables OIDC]
    B --> B2[Provides user ID 'sub']
    
    C --> C1[Name]
    C --> C2[Picture]
    C --> C3[Locale]
    
    D --> D1[Email address]
    D --> D2[Email verified status]
```

### Spring Security Auto-Magic

Spring Security automatically handles:
- ✅ Redirect to Google
- ✅ Authorization code exchange
- ✅ Token validation
- ✅ User info fetching
- ✅ Session management
- ✅ CSRF protection
- ✅ Security headers

You just configure it and use `@AuthenticationPrincipal`!

## Security Considerations

### What's Protected

```mermaid
graph LR
    subgraph "Server-Side (Secure)"
        A[Client Secret]
        B[Access Tokens]
        C[ID Tokens JWT]
        D[Session Data]
    end
    
    subgraph "Client-Side (Browser)"
        E[Session Cookie Only]
    end
    
    A -.->|Never sent| E
    B -.->|Never sent| E
    C -.->|Never sent| E
    D -.->|Never sent| E
    E -->|JSESSIONID| D
```

1. **Client Secret**: Never exposed to browser
2. **Tokens**: Stored server-side in session
3. **API Endpoints**: Protected by authentication
4. **CSRF**: Enabled by default in Spring Security

### What's Public

1. **Swagger UI**: Must be public to show documentation
2. **OAuth2 endpoints**: Spring's built-in login/callback URLs
3. **Static resources**: If you add any

### Production Checklist

- [ ] Use HTTPS (not HTTP)
- [ ] Set secure session cookies
- [ ] Use environment variables for secrets
- [ ] Configure CORS properly
- [ ] Add rate limiting
- [ ] Enable security headers
- [ ] Use production OAuth2 credentials
- [ ] Remove debug logging
- [ ] Set session timeout appropriately
- [ ] Implement logout functionality

## Testing the Application

### Step-by-Step Testing Flow

```mermaid
flowchart TD
    A[Start Application] --> B[Open Swagger UI<br/>http://localhost:8080/swagger-ui/index.html]
    B --> C[Read authentication instructions]
    C --> D[Open new tab:<br/>http://localhost:8080/oauth2/authorization/google]
    D --> E[Redirected to Google Login]
    E --> F[Enter Google credentials]
    F --> G[Grant permissions]
    G --> H[Redirected back to Swagger UI]
    H --> I[Session authenticated!]
    I --> J[Test GET /api/hello]
    J --> K{Response 200 OK?}
    K -->|Yes| L[Success! ✅<br/>See your profile data]
    K -->|No| M[Check session cookie<br/>Try logging in again]
```

## Debugging Tips

### Enable Debug Logging

Already enabled in `application.yml`:
```yaml
logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.security.oauth2: DEBUG
```

### Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| 401 Unauthorized | Not authenticated | Visit /oauth2/authorization/google to log in |
| redirect_uri_mismatch | Wrong redirect URI | Check Google Console config |
| Invalid client | Wrong credentials | Verify CLIENT_ID and SECRET |
| Session expired | Session timeout | Re-authenticate |

## Further Reading

- [Spring Security OAuth2 Docs](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [OAuth2 RFC 6749](https://tools.ietf.org/html/rfc6749)
- [OpenID Connect Spec](https://openid.net/connect/)
- [JWT Introduction](https://jwt.io/introduction)
- [SpringDoc OpenAPI](https://springdoc.org/)

---

**Questions?** Review the code in `src/main/java/com/example/oauth2/` to see these concepts in action!
