package com.example.bookstore.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesPoint(
        LocalDate bucketStart,
        long ordersCount,
        BigDecimal revenue
) {
}

