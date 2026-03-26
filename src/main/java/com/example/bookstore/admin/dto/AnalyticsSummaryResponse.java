package com.example.bookstore.admin.dto;

import java.math.BigDecimal;

public record AnalyticsSummaryResponse(
        long totalUsers,
        long newUsers,
        long activeUsers,
        long totalOrders,
        long paidOrders,
        BigDecimal revenue,
        BigDecimal avgOrderValue
) {
}

