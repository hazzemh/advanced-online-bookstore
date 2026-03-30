package com.example.bookstore.notification.email;

import com.example.bookstore.review.event.ReviewCreatedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "notifications.email", name = "enabled", havingValue = "true")
public class ReviewEmailNotificationListener {

    private final EmailSender emailSender;

    public ReviewEmailNotificationListener(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Async
    @EventListener
    public void onReviewCreated(ReviewCreatedEvent event) {
        if (event == null || event.userEmail() == null || event.userEmail().isBlank()) {
            return;
        }
        String subject = "Thanks for your review";
        String body = """
                Thanks for leaving a review.

                Review ID: %s
                Book ID: %s
                Rating: %s/5
                """.formatted(event.reviewId(), event.bookId(), event.rating());
        emailSender.send(new EmailMessage(event.userEmail(), subject, body));
    }
}

