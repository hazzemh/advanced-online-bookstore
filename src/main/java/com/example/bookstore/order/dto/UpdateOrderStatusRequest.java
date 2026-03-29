package com.example.bookstore.order.dto;

import com.example.bookstore.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @Schema(description = "New order status.", example = "SHIPPED")
        @NotNull
        OrderStatus status
) {
}

