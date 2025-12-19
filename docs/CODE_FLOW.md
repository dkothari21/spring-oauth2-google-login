# code Flow Code

Simple guide to how the application handles login and requests.

## 1. Login Flow (Getting In)

1.  **Start**: User clicks login. `SecurityConfig.java` sends them to Google.
2.  **Google Callback**: Google sends the user back to us.
3.  **Success Handler Runs**: Spring calls `OAuth2SuccessHandler.java` automatically.
    *   **Get User**: We get the user's email/name from Google (Line 28).
    *   **Make Token**: We create a signed JWT using `JwtUtil.java` (Line 31).
    *   **Set Cookie**: We put that JWT into an `auth_token` HTTP-only cookie (Line 42).
    *   **Redirect**: Note this line determines where they go next—currently sending them to Swagger UI (Line 58).

## 2. Request Flow (Doing Things)

Every time the user tries to load a page or call an API (like `/api/hello`), this happens:

1.  **Filter Checks Request**: `JwtAuthFilter.java` runs before anything else.
2.  **Find Cookie**: It looks for the `auth_token` cookie (Line 66).
3.  **Validate**: It checks if the "signature" on the token is valid (Line 39).
4.  **Log Them In**: If valid, it tells Spring Security "This user is logged in as [Email]" (Line 56).
5.  **Controller Runs**: Finally, `HelloController.java` gets the request.
    *   The `@AuthenticationPrincipal` annotation (Line 25) lets us easily grab the user data we just set in the filter.

## Summary

| Component | Responsibility |
| :--- | :--- |
| **SecurityConfig** | Traffic cop. Decides who needs to login vs. what is public. |
| **OAuth2SuccessHandler** | The "Door Greeter". Gives the user their ID badge (Cookie) after Google checks their ID. |
| **JwtAuthFilter** | The "Security Guard". Checks every person's ID badge (Cookie) on every single request. |
| **HelloController** | The destination. The code that actually does the work (e.g., saying "Hello"). |
