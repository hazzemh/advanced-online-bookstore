package com.example.bookstore.payment.service;

import com.example.bookstore.order.entity.Order;
import com.example.bookstore.order.entity.OrderStatus;
import com.example.bookstore.order.repository.OrderRepository;
import com.example.bookstore.order.service.OrderService;
import com.example.bookstore.payment.entity.Payment;
import com.example.bookstore.payment.entity.PaymentProvider;
import com.example.bookstore.payment.entity.PaymentStatus;
import com.example.bookstore.payment.repository.PaymentRepository;
import com.example.bookstore.user.entity.User;
import com.example.bookstore.user.service.UserService;
import org.junit.jupiter.api.Test;

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
        OrderRepository orderRepository = mock(OrderRepository.class);
        UserService userService = mock(UserService.class);
        OrderService orderService = mock(OrderService.class);

        StripePaymentService service = new StripePaymentService(
                stripeGateway,
                paymentRepository,
                orderRepository,
                userService,
                orderService
        );
        setPrivateField(service, "currency", "usd");

        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("pay.user@example.com").build();

        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .user(user)
                .status(OrderStatus.PROCESSING)
                .subtotal(new BigDecimal("10.00"))
                .build();

        when(userService.requireUserByEmail(eq(user.getEmail()))).thenReturn(user);
        when(orderRepository.findByIdAndUserId(eq(orderId), eq(userId))).thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(eq(orderId))).thenReturn(Optional.empty());
        when(stripeGateway.createPaymentIntent(anyLong(), anyString(), anyMap()))
                .thenReturn(new StripePaymentIntent("pi_123", "secret_123"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        var first = service.createPaymentIntentForMyOrder(user.getEmail(), orderId);
        assertEquals(orderId, first.orderId());
        assertEquals("pi_123", first.paymentIntentId());
        assertEquals("secret_123", first.clientSecret());

        Payment existing = Payment.builder()
                .order(order)
                .provider(PaymentProvider.STRIPE)
                .status(PaymentStatus.CREATED)
                .paymentIntentId("pi_123")
                .amount(order.getSubtotal())
                .currency("usd")
                .build();
        when(paymentRepository.findByOrderId(eq(orderId))).thenReturn(Optional.of(existing));
        when(stripeGateway.retrievePaymentIntent(eq("pi_123")))
                .thenReturn(new StripePaymentIntent("pi_123", "secret_123"));

        var second = service.createPaymentIntentForMyOrder(user.getEmail(), orderId);
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

