package com.example.bookstore.order.event;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        String userEmail,
        BigDecimal subtotal
) {
}

