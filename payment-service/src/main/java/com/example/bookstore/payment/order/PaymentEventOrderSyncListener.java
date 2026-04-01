package com.example.bookstore.payment.order;

import com.example.bookstore.payment.event.PaymentFailedEvent;
import com.example.bookstore.payment.event.PaymentSucceededEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventOrderSyncListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventOrderSyncListener.class);

    private final OrderServiceClient orderServiceClient;

    public PaymentEventOrderSyncListener(OrderServiceClient orderServiceClient) {
        this.orderServiceClient = orderServiceClient;
    }

    @Async
    @EventListener
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        if (event == null || event.orderId() == null) {
            return;
        }
        try {
            orderServiceClient.updateOrderStatus(event.orderId(), "PAID");
        } catch (RuntimeException ex) {
            log.warn("Failed to sync order status to PAID orderId={} paymentIntentId={}",
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
            orderServiceClient.updateOrderStatus(event.orderId(), "CANCELED");
        } catch (RuntimeException ex) {
            log.warn("Failed to sync order status to CANCELED orderId={} paymentIntentId={}",
                    event.orderId(), event.paymentIntentId(), ex);
        }
    }
}

