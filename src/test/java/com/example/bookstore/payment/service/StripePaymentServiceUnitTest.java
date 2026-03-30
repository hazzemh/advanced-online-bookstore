package com.example.bookstore.payment.service;

import com.example.bookstore.integration.order.OrderPaymentQuery;
import com.example.bookstore.integration.order.OrderPaymentView;
import com.example.bookstore.payment.entity.Payment;
import com.example.bookstore.payment.entity.PaymentProvider;
import com.example.bookstore.payment.entity.PaymentStatus;
import com.example.bookstore.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StripePaymentServiceUnitTest {

    @Test
    void createPaymentIntent_shouldCreateAndThenReuseExistingPaymentIntent() throws Exception {
        StripeGateway stripeGateway = mock(StripeGateway.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        OrderPaymentQuery orderPaymentQuery = mock(OrderPaymentQuery.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

        StripePaymentService service = new StripePaymentService(
                stripeGateway,
                paymentRepository,
                orderPaymentQuery,
                eventPublisher
        );
        setPrivateField(service, "currency", "usd");

        String userEmail = "pay.user@example.com";

        UUID orderId = UUID.randomUUID();
        BigDecimal subtotal = new BigDecimal("10.00");
        when(orderPaymentQuery.getMyOrderPaymentView(eq(orderId), eq(userEmail)))
                .thenReturn(new OrderPaymentView(orderId, userEmail, "PROCESSING", subtotal));

        when(paymentRepository.findByOrderId(eq(orderId))).thenReturn(Optional.empty());
        when(stripeGateway.createPaymentIntent(anyLong(), anyString(), anyMap()))
                .thenReturn(new StripePaymentIntent("pi_123", "secret_123"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        var first = service.createPaymentIntentForMyOrder(userEmail, orderId);
        assertEquals(orderId, first.orderId());
        assertEquals("pi_123", first.paymentIntentId());
        assertEquals("secret_123", first.clientSecret());

        Payment existing = Payment.builder()
                .orderId(orderId)
                .userEmail(userEmail)
                .provider(PaymentProvider.STRIPE)
                .status(PaymentStatus.CREATED)
                .paymentIntentId("pi_123")
                .amount(subtotal)
                .currency("usd")
                .build();
        when(paymentRepository.findByOrderId(eq(orderId))).thenReturn(Optional.of(existing));
        when(stripeGateway.retrievePaymentIntent(eq("pi_123")))
                .thenReturn(new StripePaymentIntent("pi_123", "secret_123"));

        var second = service.createPaymentIntentForMyOrder(userEmail, orderId);
        assertEquals("pi_123", second.paymentIntentId());
        assertEquals("secret_123", second.clientSecret());

        verify(stripeGateway, times(1)).createPaymentIntent(anyLong(), anyString(), anyMap());
        verify(stripeGateway, times(1)).retrievePaymentIntent(eq("pi_123"));
    }

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}

