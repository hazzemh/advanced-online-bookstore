package com.example.bookstore.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(
        @Schema(description = "JWT access token.", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzExNjkwMDAwLCJleHAiOjE3MTE3NzY0MDB9.signature")
        String token
) {
}
