package com.example.bookstore.review.event;

import java.util.UUID;

public record ReviewCreatedEvent(
        UUID reviewId,
        UUID bookId,
        UUID userId,
        int rating
) {
}

