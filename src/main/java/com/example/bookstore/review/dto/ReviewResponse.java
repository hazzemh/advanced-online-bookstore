package com.example.bookstore.review.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
    UUID id,
    UUID bookId,
    String bookTitle,
    UUID userId,
    String userName,
    Integer rating,
    String comment,
    Boolean isVerifiedPurchase,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

