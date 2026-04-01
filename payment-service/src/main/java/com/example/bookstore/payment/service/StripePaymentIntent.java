package com.example.bookstore.payment.service;

public record StripePaymentIntent(
        String id,
        String clientSecret
) {
}

