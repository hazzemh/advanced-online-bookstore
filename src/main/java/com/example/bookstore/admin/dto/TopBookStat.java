package com.example.bookstore.admin.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TopBookStat(
        UUID bookId,
        String title,
        String author,
        String genre,
        long quantitySold,
        BigDecimal revenue
) {
}

