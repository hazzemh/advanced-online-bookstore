package com.example.bookstore.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Schema(description = "User email address.", example = "user@example.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "User password (min 8 characters).", example = "P@ssw0rd123")
        @NotBlank
        @Size(min = 8, max = 72)
        String password,

        @Schema(description = "First name.", example = "Hazzem")
        @NotBlank
        @Size(max = 50)
        String firstName,

        @Schema(description = "Last name.", example = "Ali")
        @NotBlank
        @Size(max = 50)
        String lastName
) {
}
