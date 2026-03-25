package com.example.bookstore.payment.dto;

import java.util.UUID;

public record CreateStripePaymentIntentRequest(
        UUID orderId
) {
}

