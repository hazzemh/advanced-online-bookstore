package com.example.bookstore.security.oauth2;

import com.example.bookstore.security.jwt.JwtService;
import com.example.bookstore.user.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;

    public OAuth2LoginSuccessHandler(
            UserService userService,
            JwtService jwtService
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Auth)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unsupported authentication type");
            return;
        }

        OAuth2User oauth2User = oauth2Auth.getPrincipal();
        Map<String, Object> attributes = oauth2User.getAttributes();

        // For Google (OpenID Connect), email is typically present when requesting the "email" scope.
        String email = asString(attributes.get("email"));
        if (email == null || email.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Google account did not provide an email");
            return;
        }

        // Provision user if first time logging in with Google.
        String givenName = asString(attributes.get("given_name"));
        String familyName = asString(attributes.get("family_name"));
        userService.findOrCreateOauthUser(email, givenName, familyName);

        String token = jwtService.generateToken(email);

        // Return JSON so you can capture the token even without a frontend SPA redirect.
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.getWriter().write("{\"token\":\"" + token + "\"}");
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
