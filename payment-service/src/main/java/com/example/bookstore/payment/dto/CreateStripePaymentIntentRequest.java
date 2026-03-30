package com.example.bookstore.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateStripePaymentIntentRequest(
        @Schema(description = "Order id to pay.", example = "7616a5da-ef05-4117-bf69-7730aff21973")
        @NotNull
        UUID orderId
) {
}

