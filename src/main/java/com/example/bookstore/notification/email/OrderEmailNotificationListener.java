package com.example.bookstore.notification.email;

import com.example.bookstore.order.entity.OrderStatus;
import com.example.bookstore.order.event.OrderCreatedEvent;
import com.example.bookstore.order.event.OrderStatusChangedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "notifications.email", name = "enabled", havingValue = "true")
public class OrderEmailNotificationListener {

    private final EmailSender emailSender;

    public OrderEmailNotificationListener(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Async
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        if (event == null || event.userEmail() == null || event.userEmail().isBlank()) {
            return;
        }
        String subject = "Order Confirmation: " + event.orderId();
        String body = """
                Your order has been created successfully.

                Order ID: %s
                Subtotal: %s

                Thanks for shopping with us.
                """.formatted(event.orderId(), event.subtotal());
        emailSender.send(new EmailMessage(event.userEmail(), subject, body));
    }

    @Async
    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        if (event == null || event.userEmail() == null || event.userEmail().isBlank()) {
            return;
        }

        // Only notify on meaningful status updates to avoid noisy emails.
        if (!shouldNotify(event.newStatus())) {
            return;
        }

        String subject = "Order Update: " + event.orderId() + " is now " + event.newStatus();
        String body = """
                Your order status has been updated.

                Order ID: %s
                Previous status: %s
                New status: %s
                """.formatted(event.orderId(), event.oldStatus(), event.newStatus());
        emailSender.send(new EmailMessage(event.userEmail(), subject, body));
    }

    private boolean shouldNotify(OrderStatus status) {
        return status == OrderStatus.PAID
                || status == OrderStatus.SHIPPED
                || status == OrderStatus.DELIVERED
                || status == OrderStatus.CANCELED;
    }
}

