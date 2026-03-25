package com.example.bookstore.payment.controller;

import com.example.bookstore.payment.dto.CreateStripePaymentIntentRequest;
import com.example.bookstore.payment.dto.CreateStripePaymentIntentResponse;
import com.example.bookstore.payment.service.StripePaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/stripe")
public class StripePaymentController {

    private final StripePaymentService stripePaymentService;

    public StripePaymentController(StripePaymentService stripePaymentService) {
        this.stripePaymentService = stripePaymentService;
    }

    @PostMapping("/payment-intents")
    public ResponseEntity<CreateStripePaymentIntentResponse> createPaymentIntent(
            Authentication authentication,
            @RequestBody CreateStripePaymentIntentRequest request
    ) {
        if (request == null || request.orderId() == null) {
            throw new IllegalArgumentException("orderId is required");
        }
        String userEmail = authentication.getName();
        CreateStripePaymentIntentResponse response =
                stripePaymentService.createPaymentIntentForMyOrder(userEmail, request.orderId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

