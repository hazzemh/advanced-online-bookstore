package com.example.bookstore.internal;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class InternalApiTokenValidator {

    @Value("${internal.api.token:}")
    private String expectedToken;

    public void requireValid(String providedToken) {
        if (expectedToken == null || expectedToken.isBlank()) {
            throw new IllegalStateException("internal.api.token is not configured");
        }
        if (providedToken == null || providedToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Missing internal token");
        }
        if (!expectedToken.equals(providedToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal token");
        }
    }
}
