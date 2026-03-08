package com.example.bookstore.review.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateReviewRequest(
    UUID bookId,
    Integer rating,
    String comment
) {}

