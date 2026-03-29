package com.example.bookstore.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @Schema(description = "Order items (at least 1).")
        @NotEmpty
        @Valid
        List<CreateOrderItemRequest> items
) {
}

