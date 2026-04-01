package com.example.bookstore.payment.event;

import java.util.UUID;

public record PaymentFailedEvent(
        UUID orderId,
        String userEmail,
        String paymentIntentId
) {
}

