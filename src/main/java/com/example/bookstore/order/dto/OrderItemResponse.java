package com.example.bookstore.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
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

