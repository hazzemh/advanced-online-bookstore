package com.example.bookstore.payment.controller;

import com.example.bookstore.payment.service.StripePaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/stripe")
public class StripeWebhookController {

    private final StripePaymentService stripePaymentService;

    public StripeWebhookController(StripePaymentService stripePaymentService) {
        this.stripePaymentService = stripePaymentService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader(name = "Stripe-Signature") String stripeSignature
    ) {
        stripePaymentService.handleWebhook(payload, stripeSignature);
        return ResponseEntity.ok().build();
    }
}
