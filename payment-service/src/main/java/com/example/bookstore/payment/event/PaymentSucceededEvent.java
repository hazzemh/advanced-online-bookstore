package com.example.bookstore.payment.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentSucceededEvent(
        UUID orderId,
        String userEmail,
        String paymentIntentId,
        BigDecimal amount,
        String currency
) {
}

