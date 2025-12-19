# 🧪 Testing Guide

A simple guide to test your Google OAuth2 Spring Boot application.

## 🎯 Quick Start

### Prerequisites
- Application is running on `http://localhost:8080`
- You have set `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` environment variables
- Your Google Console has the correct redirect URI configured

## 📋 Step-by-Step Testing

### Step 1: Start the Application

```bash
# Option A: Using the helper script
./run.sh

# Option B: Using Gradle directly
GOOGLE_CLIENT_ID="your-id" GOOGLE_CLIENT_SECRET="your-secret" ./gradlew bootRun
```

Wait for the message: `Started GoogleOAuth2Application`

### Step 2: Authenticate with Google

**Open this URL in your browser:**
```
http://localhost:8080/oauth2/authorization/google
```

**What happens:**
1. You'll be redirected to Google's login page
2. Choose your Google account
3. Grant permissions (openid, profile, email)
4. You'll be redirected back to Swagger UI
5. **You're now authenticated!** ✅

### Step 3: Test the API with Swagger

**Open Swagger UI:**
```
http://localhost:8080/swagger-ui/index.html
```

**Test the `/api/hello` endpoint:**
1. Expand **GET /api/hello**
2. Click **"Try it out"**
3. Click **"Execute"**

**Expected Response (200 OK):**
```json
{
  "message": "Hello, Your Name!",
  "email": "your-email@gmail.com",
  "name": "Your Name",
  "picture": "https://lh3.googleusercontent.com/...",
  "authenticated": true
}
```

### Step 4: Test the `/api/user` endpoint

1. Expand **GET /api/user**
2. Click **"Try it out"**
3. Click **"Execute"**

**Expected Response (200 OK):**
```json
{
  "attributes": {
    "sub": "1234567890",
    "name": "Your Name",
    "given_name": "Your",
    "family_name": "Name",
    "picture": "https://...",
    "email": "your-email@gmail.com",
    "email_verified": true,
    "locale": "en"
  },
  "authorities": [...]
}
```

## 🔄 How Authentication Works

```
┌─────────────────────────────────────────────────────────┐
│  1. Visit /oauth2/authorization/google                 │
│     → Initiates OAuth2 flow                             │
└────────────────────┬────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────┐
│  2. Redirected to Google                                │
│     → Log in with your Google account                   │
└────────────────────┬────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────┐
│  3. Google redirects back with authorization code       │
│     → Spring Security exchanges code for tokens         │
└────────────────────┬────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────┐
│  4. Spring fetches your user info from Google           │
│     → Creates an authenticated session                  │
└────────────────────┬────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────┐
│  5. Session cookie set in browser (JSESSIONID)          │
│     → All API calls now include this cookie             │
└────────────────────┬────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────┐
│  6. Protected /api/** endpoints now work! ✅            │
│     → Swagger automatically includes session cookie     │
└─────────────────────────────────────────────────────────┘
```

## 🔍 Testing Without Swagger

You can also test with `curl`:

### 1. Get the session cookie by logging in

Open your browser and visit:
```
http://localhost:8080/oauth2/authorization/google
```

Complete the Google login flow.

### 2. Get the session cookie from browser

In Chrome/Firefox:
1. Open Developer Tools (F12)
2. Go to Application/Storage → Cookies
3. Find `JSESSIONID` for `localhost:8080`
4. Copy the value

### 3. Use curl with the session cookie

```bash
curl -X GET "http://localhost:8080/api/hello" \
  -H "Cookie: JSESSIONID=your-session-id-here"
```

## ❌ Testing Unauthenticated Access

Try accessing a protected endpoint without logging in:

```bash
curl -X GET "http://localhost:8080/api/hello"
```

**Expected Response (401 Unauthorized):**
```json
{
  "timestamp": "2025-12-19T...",
  "status": 401,
  "error": "Unauthorized",
  "path": "/api/hello"
}
```

This confirms that the endpoints are properly protected!

## 🔧 Troubleshooting

### Issue: "401 Unauthorized" in Swagger

**Cause**: You're not authenticated

**Solution**:
1. Open a new tab
2. Visit: `http://localhost:8080/oauth2/authorization/google`
3. Complete the Google login
4. Return to Swagger UI and try again

### Issue: "redirect_uri_mismatch"

**Cause**: Redirect URI in Google Console doesn't match

**Solution**:
1. Go to [Google Cloud Console - Credentials](https://console.cloud.google.com/apis/credentials)
2. Click your OAuth 2.0 Client ID
3. Add this exact URI to "Authorized redirect URIs":
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
4. Also add (for Swagger):
   ```
   http://localhost:8080/swagger-ui/oauth2-redirect.html
   ```
5. Click **Save**

### Issue: "Invalid client"

**Cause**: Wrong `GOOGLE_CLIENT_ID` or `GOOGLE_CLIENT_SECRET`

**Solution**:
1. Verify environment variables are set:
   ```bash
   echo $GOOGLE_CLIENT_ID
   echo $GOOGLE_CLIENT_SECRET
   ```
2. Check they match your Google Console credentials
3. Restart the application

### Issue: Session expired

**Cause**: Session timeout (default: 30 minutes)

**Solution**:
1. Log in again by visiting: `http://localhost:8080/oauth2/authorization/google`
2. Your session will be refreshed

### Issue: Can't see user data in response

**Cause**: Not logged in or session expired

**Solution**:
1. Check if you have a `JSESSIONID` cookie in your browser
2. If not, log in again
3. Make sure you're using the same browser for login and testing

## 📊 Verifying the Setup

### Check 1: Application Started
```bash
curl http://localhost:8080/swagger-ui/index.html
```
Should return HTML (status 200)

### Check 2: OAuth2 Endpoint Available
```bash
curl -I http://localhost:8080/oauth2/authorization/google
```
Should return status 302 (redirect to Google)

### Check 3: Protected Endpoint Blocked
```bash
curl -I http://localhost:8080/api/hello
```
Should return status 401 (unauthorized)

### Check 4: Swagger UI Accessible
Open in browser: `http://localhost:8080/swagger-ui/index.html`
Should show the API documentation

## 🎓 Understanding the Difference

### ❌ Old Approach (Didn't Work)
- Swagger's "Authorize" button tried to handle OAuth2
- Client secret needed to be sent from browser (security risk!)
- Got "invalid_client" error

### ✅ New Approach (Works!)
- Spring Security handles OAuth2 on the server
- Client secret stays on the server (secure!)
- Browser only gets a session cookie
- Swagger uses the session cookie automatically

## 🚀 Next Steps

Once you've verified everything works:

1. **Explore the code**: Check out `src/main/java/com/example/oauth2/`
2. **Read the architecture**: See `ARCHITECTURE.md` for detailed diagrams
3. **Add more endpoints**: Create new controllers with `@AuthenticationPrincipal`
4. **Customize**: Modify the response format, add more user attributes

## 📝 Quick Reference

| URL | Purpose |
|-----|---------|
| `http://localhost:8080/oauth2/authorization/google` | Login with Google |
| `http://localhost:8080/swagger-ui/index.html` | Swagger UI (API testing) |
| `http://localhost:8080/api/hello` | Protected endpoint (greeting) |
| `http://localhost:8080/api/user` | Protected endpoint (full user data) |
| `http://localhost:8080/v3/api-docs` | OpenAPI specification (JSON) |

## 🔐 Security Notes

- ✅ Client secret is never exposed to the browser
- ✅ Tokens are stored server-side in the session
- ✅ Only a session cookie is sent to the browser
- ✅ CSRF protection is enabled by default
- ✅ All `/api/**` endpoints require authentication

---

**Happy Testing! 🎉**

For more details, see:
- `README.md` - Complete project documentation
- `ARCHITECTURE.md` - Technical deep dive with Mermaid diagrams
- `GOOGLE_SETUP.md` - Google Console setup guide
