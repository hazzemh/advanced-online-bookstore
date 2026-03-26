package com.example.bookstore.notification.email;

public record EmailMessage(
        String to,
        String subject,
        String body
) {
}

