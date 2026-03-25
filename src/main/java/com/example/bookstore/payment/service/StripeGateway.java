package com.example.bookstore.payment.service;

import com.stripe.model.Event;

import java.util.Map;

public interface StripeGateway {

    StripePaymentIntent createPaymentIntent(long amountMinor, String currency, Map<String, String> metadata);

    StripePaymentIntent retrievePaymentIntent(String paymentIntentId);

    Event constructEvent(String payload, String signatureHeader, String webhookSecret);
}
