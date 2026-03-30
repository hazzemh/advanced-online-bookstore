package com.example.bookstore.order.listener;

import com.example.bookstore.order.entity.OrderStatus;
import com.example.bookstore.order.service.OrderService;
import com.example.bookstore.payment.event.PaymentFailedEvent;
import com.example.bookstore.payment.event.PaymentSucceededEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventOrderListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventOrderListener.class);

    private final OrderService orderService;

    public PaymentEventOrderListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @Async
    @EventListener
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        if (event == null || event.orderId() == null) {
            return;
        }
        try {
            orderService.updateOrderStatus(event.orderId(), OrderStatus.PAID);
        } catch (RuntimeException ex) {
            log.warn("Failed to mark order as PAID after payment succeeded orderId={} paymentIntentId={}",
                    event.orderId(), event.paymentIntentId(), ex);
        }
    }

    @Async
    @EventListener
    public void onPaymentFailed(PaymentFailedEvent event) {
        if (event == null || event.orderId() == null) {
            return;
        }
        try {
            orderService.updateOrderStatus(event.orderId(), OrderStatus.CANCELED);
        } catch (RuntimeException ex) {
            log.warn("Failed to cancel order after payment failed orderId={} paymentIntentId={}",
                    event.orderId(), event.paymentIntentId(), ex);
        }
    }
}

