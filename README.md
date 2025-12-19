# Google OAuth2 Spring Boot Application

A Spring Boot application demonstrating Google OAuth2 authentication with Swagger/OpenAPI integration for easy API testing.

## 🎯 Features

- ✅ Google OAuth2 authentication (OIDC)
- ✅ Protected API endpoints (`/api/**`)
- ✅ Swagger UI for interactive API testing
- ✅ JWT token handling (automatic)
- ✅ User information extraction from Google

## 📋 Prerequisites

- Java 17 or higher
- Gradle 7.x or higher
- A Google Cloud Console account

## 🔧 Setup Instructions

### 1. Create Google OAuth2 Credentials

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project (or select an existing one)
3. Navigate to **APIs & Services** → **Credentials**
4. Click **Create Credentials** → **OAuth Client ID**
5. Select **Web Application**
6. Add the following to **Authorized Redirect URIs**:
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
7. Click **Create** and save your:
   - **Client ID**
   - **Client Secret**

### 2. Configure the Application

You have two options to provide your Google credentials:

#### Option A: Use .env File (Easiest for Local Development)
```bash
# Create .env file from example
cp .env.example .env

# Edit .env and add your credentials
# GOOGLE_CLIENT_ID=your-client-id-here
# GOOGLE_CLIENT_SECRET=your-client-secret-here
```

Then just run:
```bash
./run.sh
```

The script will automatically load your credentials! 🎉

#### Option B: Environment Variables
```bash
export GOOGLE_CLIENT_ID="your-client-id-here"
export GOOGLE_CLIENT_SECRET="your-client-secret-here"
```

⚠️ **Important**: Never commit your `.env` file or actual credentials to version control!

**See [docs/LOCAL_SETUP.md](docs/LOCAL_SETUP.md) for detailed local development setup.**

### 3. Run the Application

**Option A: Using the helper script (loads .env automatically)**
```bash
./run.sh
```

**Option B: Using Gradle directly**
```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

## 🧪 Testing the Application

### Quick Start

1. **Start the application:**
   ```bash
   ./run.sh
   ```

2. **Login:**
   - Visit: `http://localhost:8080/`
   - Click "Sign in with Google"
   - Select your Google account
   - Grant permissions

3. **Test APIs in Swagger:**
   - After login, you'll be redirected to Swagger UI
   - Try `GET /api/hello` - Returns personalized greeting
   - Try `GET /api/user` - Returns your user profile

4. **Check Cookies** (DevTools → Application → Cookies):
   - `auth_token` - JWT token (URL-encoded JSON)
   - `session_id` - Random session ID

5. **Logout:**
   - Visit: `http://localhost:8080/api/logout`
   - Cookies cleared, redirected to login

## 📚 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/hello` | Personalized greeting |
| GET | `/api/user` | User profile from Google |
| GET | `/api/logout` | Logout and clear cookies |

## 🔐 How It Works

1. **OAuth2 Login** - Authenticate with Google
2. **JWT Creation** - Create JWT from user data
3. **Cookie Storage** - Store JWT in `auth_token` cookie
4. **Stateless Auth** - Validate JWT on each request
5. **Logout** - Clear cookies

**See detailed diagrams:** [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

### Key Components

- **`SecurityConfig.java`**: Defines which endpoints require authentication
- **`OpenApiConfig.java`**: Provides authentication instructions in Swagger
- **`HelloController.java`**: Demonstrates `@AuthenticationPrincipal` usage
- **`application.yml`**: Contains OAuth2 client configuration

### The Magic of @AuthenticationPrincipal

```java
public Map<String, Object> hello(@AuthenticationPrincipal OAuth2User principal) {
    // Spring Boot automatically injects the authenticated user here!
    String name = principal.getAttribute("name");
    String email = principal.getAttribute("email");
    // ...
}
```

## 🎓 What You're Learning

✅ **OAuth2 & OIDC**: Industry-standard authentication protocols  
✅ **JWT Tokens**: Automatic token handling by Spring Security  
✅ **Spring Security**: Protecting endpoints and managing authentication  
✅ **Swagger/OpenAPI**: Interactive API documentation and testing  
✅ **Dependency Injection**: How Spring Boot wires everything together  

## 🐛 Troubleshooting

### "401 Unauthorized" Error
- Make sure you clicked "Authorize" in Swagger UI
- Verify you completed the Google login flow
- Check that your session hasn't expired

### "redirect_uri_mismatch" Error
- Verify the redirect URI in Google Console exactly matches:
  ```
  http://localhost:8080/login/oauth2/code/google
  ```
- No trailing slashes or extra characters

### Application Won't Start
- Verify your `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` are set
- Check that port 8080 is not already in use
- Review the console logs for specific error messages

## 📖 Documentation

- **[docs/GOOGLE_SETUP.md](docs/GOOGLE_SETUP.md)** - Google Console setup guide
- **[docs/TESTING_GUIDE.md](docs/TESTING_GUIDE.md)** - Testing & troubleshooting
- **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** - Technical deep dive with Mermaid diagrams

### External Resources

- [Spring Security OAuth2 Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [Google OAuth2 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [SpringDoc OpenAPI Documentation](https://springdoc.org/)

## 📝 License

This project is open source and available under the MIT License.
