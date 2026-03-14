package com.example.bookstore.order.dto;

import com.example.bookstore.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        OrderStatus status,
        List<OrderItemResponse> items,
        Integer totalQuantity,
        BigDecimal subtotal,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime statusUpdatedAt,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        LocalDateTime canceledAt
) {
}

