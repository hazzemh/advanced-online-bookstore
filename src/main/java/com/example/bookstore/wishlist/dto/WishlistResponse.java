package com.example.bookstore.wishlist.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WishlistResponse(
        UUID wishlistItemId,
        UUID bookId,
        String title,
        String author,
        BigDecimal price,
        String imageUrl,
        Double averageRating,
        Integer totalReviews,
        LocalDateTime addedAt
) {
}
