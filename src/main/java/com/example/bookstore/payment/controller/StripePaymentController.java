package com.example.bookstore.payment.controller;

import com.example.bookstore.payment.dto.CreateStripePaymentIntentRequest;
import com.example.bookstore.payment.dto.CreateStripePaymentIntentResponse;
import com.example.bookstore.payment.service.StripePaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag(name = "Payments", description = "Payment initiation endpoints (Stripe).")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/payments/stripe")
public class StripePaymentController {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentController.class);

    private final StripePaymentService stripePaymentService;

    public StripePaymentController(StripePaymentService stripePaymentService) {
        this.stripePaymentService = stripePaymentService;
    }

    @PostMapping("/payment-intents")
    public ResponseEntity<CreateStripePaymentIntentResponse> createPaymentIntent(
            Authentication authentication,
            @RequestBody @Valid CreateStripePaymentIntentRequest request
    ) {
        if (request == null || request.orderId() == null) {
            throw new IllegalArgumentException("orderId is required");
        }
        String userEmail = authentication.getName();
        log.info("Create Stripe PaymentIntent requested orderId={} userEmail={}", request.orderId(), userEmail);
        CreateStripePaymentIntentResponse response =
                stripePaymentService.createPaymentIntentForMyOrder(userEmail, request.orderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

