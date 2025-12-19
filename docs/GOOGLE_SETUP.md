# Google Cloud Console Setup Guide

This guide walks you through setting up OAuth2 credentials in Google Cloud Console.

## Step 1: Create a New Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Click the project dropdown at the top
3. Click **New Project**
4. Enter a project name (e.g., "OAuth2 Demo")
5. Click **Create**

## Step 2: Enable Required APIs

1. In the left sidebar, go to **APIs & Services** → **Library**
2. Search for "Google+ API" or "People API"
3. Click **Enable** (this allows your app to access user profile information)

## Step 3: Configure OAuth Consent Screen

1. Go to **APIs & Services** → **OAuth consent screen**
2. Select **External** (unless you have a Google Workspace account)
3. Click **Create**
4. Fill in the required fields:
   - **App name**: Your application name (e.g., "My OAuth2 App")
   - **User support email**: Your email address
   - **Developer contact information**: Your email address
5. Click **Save and Continue**
6. On the **Scopes** page, click **Save and Continue** (we'll use default scopes)
7. On the **Test users** page:
   - Click **Add Users**
   - Add your Gmail address (you'll use this to test)
   - Click **Save and Continue**
8. Click **Back to Dashboard**

## Step 4: Create OAuth2 Credentials

1. Go to **APIs & Services** → **Credentials**
2. Click **Create Credentials** → **OAuth client ID**
3. If prompted, configure the consent screen (follow Step 3 above)
4. Select **Application type**: **Web application**
5. Enter a name (e.g., "OAuth2 Web Client")
6. Under **Authorized redirect URIs**, click **Add URI**
7. Enter exactly:
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
8. Click **Create**

## Step 5: Save Your Credentials

A dialog will appear with your credentials:

- **Client ID**: Something like `123456789-abcdefg.apps.googleusercontent.com`
- **Client Secret**: A random string

**Important**: 
- Copy both values immediately
- Store them securely (password manager recommended)
- Never commit them to version control

## Step 6: Set Environment Variables

### macOS/Linux:
```bash
export GOOGLE_CLIENT_ID="your-client-id-here"
export GOOGLE_CLIENT_SECRET="your-client-secret-here"
```

To make these permanent, add them to your `~/.zshrc` or `~/.bash_profile`:
```bash
echo 'export GOOGLE_CLIENT_ID="your-client-id-here"' >> ~/.zshrc
echo 'export GOOGLE_CLIENT_SECRET="your-client-secret-here"' >> ~/.zshrc
source ~/.zshrc
```

### Windows (PowerShell):
```powershell
$env:GOOGLE_CLIENT_ID="your-client-id-here"
$env:GOOGLE_CLIENT_SECRET="your-client-secret-here"
```

## Verification Checklist

Before running the application, verify:

- ✅ OAuth consent screen is configured
- ✅ Your Gmail is added as a test user
- ✅ OAuth Client ID is created
- ✅ Redirect URI is exactly: `http://localhost:8080/login/oauth2/code/google`
- ✅ Environment variables are set
- ✅ You can access both Client ID and Client Secret

## Common Issues

### "redirect_uri_mismatch" Error
**Problem**: The redirect URI doesn't match what's configured in Google Console.

**Solution**: 
1. Go to Google Console → Credentials
2. Click your OAuth Client ID
3. Verify the redirect URI is exactly: `http://localhost:8080/login/oauth2/code/google`
4. No trailing slash, no extra characters

### "Access blocked: This app's request is invalid"
**Problem**: OAuth consent screen is not properly configured.

**Solution**:
1. Complete the OAuth consent screen configuration (Step 3)
2. Add your Gmail as a test user
3. Make sure the app is in "Testing" mode

### "Error 400: invalid_client"
**Problem**: Client ID or Client Secret is incorrect.

**Solution**:
1. Verify your environment variables are set correctly
2. Check for extra spaces or quotes in the values
3. Regenerate the client secret if needed

## Security Best Practices

1. **Never commit credentials**: Always use environment variables
2. **Use different credentials for production**: Create separate OAuth clients for dev/prod
3. **Regularly rotate secrets**: Update your client secret periodically
4. **Limit scopes**: Only request the permissions you actually need
5. **Monitor usage**: Check Google Console for unusual activity

## Next Steps

Once you have your credentials:

1. Set the environment variables
2. Run the application: `./gradlew bootRun`
3. Open Swagger UI: `http://localhost:8080/swagger-ui/index.html`
4. Click "Authorize" and test the authentication flow

## Additional Resources

- [Google OAuth2 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [OAuth2 Scopes Reference](https://developers.google.com/identity/protocols/oauth2/scopes)
- [Google Cloud Console](https://console.cloud.google.com/)
