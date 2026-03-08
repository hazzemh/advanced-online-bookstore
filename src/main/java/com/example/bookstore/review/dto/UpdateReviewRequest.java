package com.example.bookstore.review.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateReviewRequest(
    Integer rating,
    String comment
) {}

