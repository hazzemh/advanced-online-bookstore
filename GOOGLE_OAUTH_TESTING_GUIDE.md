# Google OAuth2 Login (Sign In With Google) Testing Guide

This project supports "Sign in with Google" using Spring Security OAuth2 Login, and then issues the same JWT used by `/api/auth/login`.

## 1) Configure Credentials (Do Not Commit)

Put credentials in your **repo-root** `application.yml` (this file is git-ignored in this repo), or set them as environment variables.

Example (recommended: env vars):

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`

If you prefer `application.yml`, add:

```yml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid, profile, email
```

## 2) Google Console Redirect URI

Make sure your OAuth client has this Authorized Redirect URI:

- `http://localhost:8081/login/oauth2/code/google`

## 3) Start the App

Run the backend on port `8081`.

## 4) Trigger Login In the Browser

Open:

- `http://localhost:8081/oauth2/authorization/google`

After Google login, the backend responds with JSON:

```json
{"token":"<JWT>"}
```

## 5) Use the JWT for Protected APIs

Copy the token and call any protected endpoint with:

- Header: `Authorization: Bearer <JWT>`

Example endpoints that require auth depend on your security config; any endpoint not in the `permitAll()` list requires a JWT.

## Notes / Customization

- If this is used by a frontend SPA, you may prefer redirecting to your frontend URL instead of returning JSON. That behavior is controlled by `OAuth2LoginSuccessHandler`.
- Google users are auto-provisioned in the `users` table (role `USER`) on first successful login.

