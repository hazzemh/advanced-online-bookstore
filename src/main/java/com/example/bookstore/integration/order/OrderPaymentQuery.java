package com.example.bookstore.integration.order;

import java.util.UUID;

/**
 * Read-only contract that the payment module uses to fetch the minimum order info required to start a payment.
 *
 * In the monolith this is implemented in the order module. After extraction, payment can switch to an HTTP client
 * implementation without changing StripePaymentService.
 */
public interface OrderPaymentQuery {
    OrderPaymentView getMyOrderPaymentView(UUID orderId, String userEmail);
}

