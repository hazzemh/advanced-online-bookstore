package com.example.bookstore.notification.email;

import com.example.bookstore.payment.event.PaymentSucceededEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "notifications.email", name = "enabled", havingValue = "true")
public class PaymentEmailNotificationListener {

    private final EmailSender emailSender;

    public PaymentEmailNotificationListener(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Async
    @EventListener
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        if (event == null || event.userEmail() == null || event.userEmail().isBlank()) {
            return;
        }
        String subject = "Payment Received: " + event.orderId();
        String body = """
                We received your payment successfully.

                Order ID: %s
                Amount: %s %s
                PaymentIntent: %s
                """.formatted(event.orderId(), event.amount(), event.currency(), event.paymentIntentId());
        emailSender.send(new EmailMessage(event.userEmail(), subject, body));
    }
}

