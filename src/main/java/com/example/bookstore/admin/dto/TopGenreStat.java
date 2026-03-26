package com.example.bookstore.admin.dto;

import java.math.BigDecimal;

public record TopGenreStat(
        String genre,
        long quantitySold,
        BigDecimal revenue
) {
}

