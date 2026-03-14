package com.example.bookstore.cart.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID itemId,
        UUID bookId,
        String title,
        String author,
        String imageUrl,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal
) {
}

