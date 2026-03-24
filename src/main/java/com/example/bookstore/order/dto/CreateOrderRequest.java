package com.example.bookstore.order.dto;

import java.util.List;

public record CreateOrderRequest(
        List<CreateOrderItemRequest> items
) {
}

