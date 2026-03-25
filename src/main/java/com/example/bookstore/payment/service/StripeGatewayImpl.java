package com.example.bookstore.payment.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StripeGatewayImpl implements StripeGateway {

    @Value("${stripe.secret-key:}")
    private String secretKey;

    @Override
    public StripePaymentIntent createPaymentIntent(long amountMinor, String currency, Map<String, String> metadata) {
        requireSecretKey();
        try {
            Stripe.apiKey = secretKey;
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountMinor)
                    .setCurrency(currency)
                    .putAllMetadata(metadata)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();
            PaymentIntent pi = PaymentIntent.create(params);
            return new StripePaymentIntent(pi.getId(), pi.getClientSecret());
        } catch (StripeException e) {
            throw new RuntimeException("Stripe create PaymentIntent failed: " + e.getMessage(), e);
        }
    }

    @Override
    public StripePaymentIntent retrievePaymentIntent(String paymentIntentId) {
        requireSecretKey();
        try {
            Stripe.apiKey = secretKey;
            PaymentIntent pi = PaymentIntent.retrieve(paymentIntentId);
            return new StripePaymentIntent(pi.getId(), pi.getClientSecret());
        } catch (StripeException e) {
            throw new RuntimeException("Stripe retrieve PaymentIntent failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Event constructEvent(String payload, String signatureHeader, String webhookSecret) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalStateException("stripe.webhook-secret is required to validate Stripe webhooks");
        }
        try {
            return Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        } catch (Exception e) {
            throw new RuntimeException("Stripe webhook signature verification failed", e);
        }
    }

    private void requireSecretKey() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("stripe.secret-key is not configured");
        }
    }
}

