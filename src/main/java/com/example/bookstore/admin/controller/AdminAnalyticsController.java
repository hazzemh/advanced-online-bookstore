package com.example.bookstore.admin.controller;

import com.example.bookstore.admin.dto.*;
import com.example.bookstore.admin.service.AdminAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {

    private final AdminAnalyticsService analyticsService;

    public AdminAnalyticsController(AdminAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryResponse> summary(
            @RequestParam(defaultValue = "30") int days
    ) {
        return ResponseEntity.ok(analyticsService.getSummary(days));
    }

    @GetMapping("/sales/trends")
    public ResponseEntity<SalesTrendResponse> salesTrends(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "DAY") TimeBucket bucket
    ) {
        return ResponseEntity.ok(analyticsService.getSalesTrends(from, to, bucket));
    }

    @GetMapping("/top-books")
    public ResponseEntity<List<TopBookStat>> topBooks(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "REVENUE") TopMetric metric
    ) {
        return ResponseEntity.ok(analyticsService.getTopBooks(days, limit, metric));
    }

    @GetMapping("/top-genres")
    public ResponseEntity<List<TopGenreStat>> topGenres(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "REVENUE") TopMetric metric
    ) {
        return ResponseEntity.ok(analyticsService.getTopGenres(days, limit, metric));
    }

    @GetMapping("/users/activity")
    public ResponseEntity<UserActivityResponse> userActivity(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "DAY") TimeBucket bucket
    ) {
        return ResponseEntity.ok(analyticsService.getUserActivity(from, to, bucket));
    }
}

