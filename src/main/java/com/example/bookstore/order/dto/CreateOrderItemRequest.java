package com.example.bookstore.order.dto;

import java.util.UUID;

public record CreateOrderItemRequest(
        UUID bookId,
        Integer quantity
) {
}

