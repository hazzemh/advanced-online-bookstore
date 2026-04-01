package com.example.bookstore.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateStripePaymentIntentResponse(
        UUID orderId,
        String paymentIntentId,
        String clientSecret,
        BigDecimal amount,
        String currency
) {
}

