package com.example.bookstore.admin.dto;

import java.time.LocalDate;
import java.util.List;

public record SalesTrendResponse(
        LocalDate from,
        LocalDate to,
        TimeBucket bucket,
        List<SalesPoint> points
) {
}

