package com.example.bookstore.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemQuantityRequest(
        @Schema(description = "New quantity for the cart item.", example = "2")
        @NotNull
        @Min(1)
        Integer quantity
) {
}

