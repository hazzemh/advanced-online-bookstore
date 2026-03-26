package com.example.bookstore.admin.dto;

import java.time.LocalDate;
import java.util.List;

public record UserActivityResponse(
        LocalDate from,
        LocalDate to,
        TimeBucket bucket,
        List<UserPoint> newUsers,
        List<UserPoint> activeUsers
) {
    public record UserPoint(LocalDate bucketStart, long usersCount) {}
}

