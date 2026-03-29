package com.example.bookstore.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "User email address.", example = "user@example.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "User password.", example = "P@ssw0rd123")
        @NotBlank
        String password
) {
}
