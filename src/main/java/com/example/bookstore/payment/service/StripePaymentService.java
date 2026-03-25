package com.example.bookstore.payment.service;

import com.example.bookstore.order.entity.Order;
import com.example.bookstore.order.entity.OrderStatus;
import com.example.bookstore.order.repository.OrderRepository;
import com.example.bookstore.order.service.OrderService;
import com.example.bookstore.payment.dto.CreateStripePaymentIntentResponse;
import com.example.bookstore.payment.entity.Payment;
import com.example.bookstore.payment.entity.PaymentProvider;
import com.example.bookstore.payment.entity.PaymentStatus;
import com.example.bookstore.payment.repository.PaymentRepository;
import com.example.bookstore.user.entity.User;
import com.example.bookstore.user.repository.UserRepository;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class StripePaymentService {

    private final StripeGateway stripeGateway;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;

    @Value("${stripe.currency:aed}")
    private String currency;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    public StripePaymentService(
            StripeGateway stripeGateway,
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            OrderService orderService
    ) {
        this.stripeGateway = stripeGateway;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderService = orderService;
    }

    public CreateStripePaymentIntentResponse createPaymentIntentForMyOrder(String userEmail, UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId is required");
        }

        User user = getUserByEmail(userEmail);
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalArgumentException("Cannot pay for a canceled order");
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot pay for a delivered order");
        }

        BigDecimal amount = order.getSubtotal() == null ? BigDecimal.ZERO : order.getSubtotal();
        long amountMinor = toMinorUnits(amount);
        if (amountMinor <= 0) {
            throw new IllegalArgumentException("Order subtotal must be > 0 to create a payment");
        }

        Payment existing = paymentRepository.findByOrderId(orderId).orElse(null);
        if (existing != null) {
            if (existing.getStatus() == PaymentStatus.SUCCEEDED) {
                throw new IllegalArgumentException("Order is already paid");
            }
            StripePaymentIntent pi = stripeGateway.retrievePaymentIntent(existing.getPaymentIntentId());
            return new CreateStripePaymentIntentResponse(orderId, pi.id(), pi.clientSecret(), amount, currency);
        }

        Map<String, String> metadata = new HashMap<>();
        metadata.put("orderId", orderId.toString());
        metadata.put("userId", user.getId().toString());

        StripePaymentIntent pi = stripeGateway.createPaymentIntent(amountMinor, currency, metadata);

        Payment payment = Payment.builder()
                .order(order)
                .provider(PaymentProvider.STRIPE)
                .status(PaymentStatus.CREATED)
                .paymentIntentId(pi.id())
                .amount(amount)
                .currency(currency)
                .build();
        paymentRepository.save(payment);

        return new CreateStripePaymentIntentResponse(orderId, pi.id(), pi.clientSecret(), amount, currency);
    }

    public void handleWebhook(String payload, String stripeSignatureHeader) {
        Event event = stripeGateway.constructEvent(payload, stripeSignatureHeader, webhookSecret);

        // We only care about PaymentIntent outcomes for now.
        if (event == null || event.getType() == null) {
            return;
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> onPaymentIntentSucceeded(event);
            case "payment_intent.payment_failed" -> onPaymentIntentFailed(event);
            default -> {
                // ignore
            }
        }
    }

    void onPaymentIntentSucceeded(Event event) {
        PaymentIntent pi = deserializePaymentIntent(event);
        if (pi == null) {
            return;
        }

        Payment payment = paymentRepository.findByPaymentIntentId(pi.getId()).orElse(null);
        if (payment == null) {
            return;
        }

        if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            paymentRepository.save(payment);
        }

        UUID orderId = payment.getOrder().getId();
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return;
        }
        if (order.getStatus() == OrderStatus.CANCELED || order.getStatus() == OrderStatus.DELIVERED) {
            return;
        }
        if (order.getStatus() != OrderStatus.PAID) {
            orderService.updateOrderStatus(orderId, OrderStatus.PAID);
        }
    }

    void onPaymentIntentFailed(Event event) {
        PaymentIntent pi = deserializePaymentIntent(event);
        if (pi == null) {
            return;
        }

        Payment payment = paymentRepository.findByPaymentIntentId(pi.getId()).orElse(null);
        if (payment == null) {
            return;
        }
        if (payment.getStatus() != PaymentStatus.FAILED) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }

        // Best-effort: cancel order (OrderService's cancel also restores stock, but we don't depend on it here).
        UUID orderId = payment.getOrder().getId();
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return;
        }
        if (order.getStatus() == OrderStatus.CANCELED || order.getStatus() == OrderStatus.DELIVERED) {
            return;
        }
        orderService.updateOrderStatus(orderId, OrderStatus.CANCELED);
    }

    private PaymentIntent deserializePaymentIntent(Event event) {
        return event.getDataObjectDeserializer()
                .getObject()
                .filter(PaymentIntent.class::isInstance)
                .map(PaymentIntent.class::cast)
                .orElse(null);
    }

    private long toMinorUnits(BigDecimal amount) {
        // Stripe requires integer minor units (e.g., cents). Assumes 2-decimal currencies.
        return amount
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact();
    }

    private User getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Unauthenticated");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
