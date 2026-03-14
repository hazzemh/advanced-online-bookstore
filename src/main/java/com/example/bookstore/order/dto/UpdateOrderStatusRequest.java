package com.example.bookstore.order.dto;

import com.example.bookstore.order.entity.OrderStatus;

public record UpdateOrderStatusRequest(
        OrderStatus status
) {
}

