package com.example.bookstore.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateOrderItemRequest(
        @Schema(description = "Book id.", example = "9d25c9d5-2d1e-4d7f-a6d6-3edcdb2f0c10")
        @NotNull
        UUID bookId,

        @Schema(description = "Quantity to purchase.", example = "1")
        @NotNull
        @Min(1)
        Integer quantity
) {
}

