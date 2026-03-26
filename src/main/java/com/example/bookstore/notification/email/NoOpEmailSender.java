package com.example.bookstore.notification.email;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "notifications.email", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpEmailSender implements EmailSender {
    @Override
    public void send(EmailMessage message) {
        // Intentionally no-op. Enable via notifications.email.enabled=true and configure spring.mail.*.
    }
}

