package com.example.bookstore.payment.controller;

import com.example.bookstore.payment.service.StripePaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/payments/stripe")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    private final StripePaymentService stripePaymentService;

    public StripeWebhookController(StripePaymentService stripePaymentService) {
        this.stripePaymentService = stripePaymentService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader(name = "Stripe-Signature") String stripeSignature
    ) {
        log.debug("Stripe webhook endpoint hit payloadBytes={}", payload == null ? 0 : payload.length());
        stripePaymentService.handleWebhook(payload, stripeSignature);
        return ResponseEntity.ok().build();
    }
}
