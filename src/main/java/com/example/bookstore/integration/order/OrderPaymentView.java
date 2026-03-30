package com.example.bookstore.integration.order;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderPaymentView(
        UUID orderId,
        String userEmail,
        String status,
        BigDecimal subtotal
) {
}

