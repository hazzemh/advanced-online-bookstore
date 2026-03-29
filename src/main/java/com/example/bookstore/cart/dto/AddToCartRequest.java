package com.example.bookstore.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddToCartRequest(
        @Schema(description = "Book id to add to cart.")
        @NotNull
        UUID bookId,

        @Schema(description = "Quantity to add.", example = "1")
        @NotNull
        @Min(1)
        Integer quantity
) {
}

