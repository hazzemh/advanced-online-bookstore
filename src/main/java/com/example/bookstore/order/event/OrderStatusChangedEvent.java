package com.example.bookstore.order.event;

import com.example.bookstore.order.entity.OrderStatus;

import java.util.UUID;

public record OrderStatusChangedEvent(
        UUID orderId,
        String userEmail,
        OrderStatus oldStatus,
        OrderStatus newStatus
) {
}

