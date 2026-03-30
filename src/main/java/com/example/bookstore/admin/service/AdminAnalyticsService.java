package com.example.bookstore.admin.service;

import com.example.bookstore.admin.dto.*;
import com.example.bookstore.cart.service.CartAnalyticsService;
import com.example.bookstore.order.entity.OrderStatus;
import com.example.bookstore.order.service.OrderAnalyticsService;
import com.example.bookstore.review.service.ReviewAnalyticsService;
import com.example.bookstore.user.service.UserAnalyticsService;
import com.example.bookstore.wishlist.service.WishlistAnalyticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AdminAnalyticsService {

    private static final EnumSet<OrderStatus> REVENUE_STATUSES =
            EnumSet.of(OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private final OrderAnalyticsService orderAnalyticsService;
    private final UserAnalyticsService userAnalyticsService;
    private final ReviewAnalyticsService reviewAnalyticsService;
    private final WishlistAnalyticsService wishlistAnalyticsService;
    private final CartAnalyticsService cartAnalyticsService;

    public AdminAnalyticsService(
            OrderAnalyticsService orderAnalyticsService,
            UserAnalyticsService userAnalyticsService,
            ReviewAnalyticsService reviewAnalyticsService,
            WishlistAnalyticsService wishlistAnalyticsService,
            CartAnalyticsService cartAnalyticsService
    ) {
        this.orderAnalyticsService = orderAnalyticsService;
        this.userAnalyticsService = userAnalyticsService;
        this.reviewAnalyticsService = reviewAnalyticsService;
        this.wishlistAnalyticsService = wishlistAnalyticsService;
        this.cartAnalyticsService = cartAnalyticsService;
    }

    public AnalyticsSummaryResponse getSummary(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("days must be >= 1");
        }

        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(days - 1L);
        LocalDate toDate = today;

        LocalDateTimeRange range = toLocalDateTimeRange(fromDate, toDate);

        long totalUsers = userAnalyticsService.countAllUsers();
        long newUsers = userAnalyticsService.countCreatedBetween(range.from, range.to);

        long totalOrders = orderAnalyticsService.countAllOrders();
        long paidOrders = orderAnalyticsService.countByStatusIn(REVENUE_STATUSES);

        BigDecimal revenue = orderAnalyticsService.sumSubtotalByStatusIn(REVENUE_STATUSES);
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }

        BigDecimal avgOrderValue = paidOrders == 0
                ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(paidOrders), 2, RoundingMode.HALF_UP);

        long activeUsers = countActiveUsers(range);

        return new AnalyticsSummaryResponse(
                totalUsers,
                newUsers,
                activeUsers,
                totalOrders,
                paidOrders,
                revenue,
                avgOrderValue
        );
    }

    public SalesTrendResponse getSalesTrends(LocalDate from, LocalDate to, TimeBucket bucket) {
        DateRange r = normalizeRange(from, to);
        TimeBucket b = bucket == null ? TimeBucket.DAY : bucket;

        LocalDateTimeRange range = toLocalDateTimeRange(r.from, r.to);
        List<Object[]> rows = orderAnalyticsService.findCreatedAtAndSubtotalBetweenAndStatusIn(range.from, range.to, REVENUE_STATUSES);

        Map<LocalDate, MutableSales> byBucket = new HashMap<>();
        for (Object[] row : rows) {
            LocalDateTime createdAt = (LocalDateTime) row[0];
            BigDecimal subtotal = (BigDecimal) row[1];

            LocalDate key = bucketStart(createdAt.toLocalDate(), b);
            MutableSales agg = byBucket.computeIfAbsent(key, __ -> new MutableSales());
            agg.orders++;
            agg.revenue = agg.revenue.add(subtotal == null ? BigDecimal.ZERO : subtotal);
        }

        List<SalesPoint> points = materializeSalesPoints(r.from, r.to, b, byBucket);
        return new SalesTrendResponse(r.from, r.to, b, points);
    }

    public List<TopBookStat> getTopBooks(int days, int limit, TopMetric metric) {
        if (days <= 0) {
            throw new IllegalArgumentException("days must be >= 1");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be >= 1");
        }
        TopMetric m = metric == null ? TopMetric.REVENUE : metric;

        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(days - 1L);

        LocalDateTimeRange range = toLocalDateTimeRange(fromDate, today);

        List<Object[]> rows = orderAnalyticsService.sumByBookBetweenAndOrderStatusIn(range.from, range.to, REVENUE_STATUSES);

        List<TopBookStat> stats = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            stats.add(new TopBookStat(
                    (UUID) row[0],
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    ((Number) row[4]).longValue(),
                    (BigDecimal) row[5]
            ));
        }

        stats.sort((a, b) -> {
            if (m == TopMetric.QUANTITY) {
                return Long.compare(b.quantitySold(), a.quantitySold());
            }
            // REVENUE
            return b.revenue().compareTo(a.revenue());
        });

        return stats.stream().limit(limit).toList();
    }

    public List<TopGenreStat> getTopGenres(int days, int limit, TopMetric metric) {
        if (days <= 0) {
            throw new IllegalArgumentException("days must be >= 1");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be >= 1");
        }
        TopMetric m = metric == null ? TopMetric.REVENUE : metric;

        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(days - 1L);

        LocalDateTimeRange range = toLocalDateTimeRange(fromDate, today);
        List<Object[]> rows = orderAnalyticsService.sumByGenreBetweenAndOrderStatusIn(range.from, range.to, REVENUE_STATUSES);

        List<TopGenreStat> stats = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            stats.add(new TopGenreStat(
                    (String) row[0],
                    ((Number) row[1]).longValue(),
                    (BigDecimal) row[2]
            ));
        }

        stats.sort((a, b) -> {
            if (m == TopMetric.QUANTITY) {
                return Long.compare(b.quantitySold(), a.quantitySold());
            }
            return b.revenue().compareTo(a.revenue());
        });

        return stats.stream().limit(limit).toList();
    }

    public UserActivityResponse getUserActivity(LocalDate from, LocalDate to, TimeBucket bucket) {
        DateRange r = normalizeRange(from, to);
        TimeBucket b = bucket == null ? TimeBucket.DAY : bucket;

        LocalDateTimeRange range = toLocalDateTimeRange(r.from, r.to);

        List<LocalDateTime> userCreatedAts = userAnalyticsService.findCreatedAtBetween(range.from, range.to);
        Map<LocalDate, Long> newUsers = bucketCounts(userCreatedAts, b);

        // "Active" user = did any action (order/review/wishlist/cart update) in the range.
        Map<LocalDate, Set<UUID>> activeByBucket = new HashMap<>();
        addActiveUsers(activeByBucket, orderAnalyticsService.findUserIdsAndCreatedAtBetween(range.from, range.to), b);
        addActiveUsers(activeByBucket, reviewAnalyticsService.findUserIdsAndCreatedAtBetween(range.from, range.to), b);
        addActiveUsers(activeByBucket, wishlistAnalyticsService.findUserIdsAndAddedAtBetween(range.from, range.to), b);
        addActiveUsers(activeByBucket, cartAnalyticsService.findUserIdsAndUpdatedAtBetween(range.from, range.to), b);

        Map<LocalDate, Long> activeCounts = new HashMap<>();
        for (Map.Entry<LocalDate, Set<UUID>> e : activeByBucket.entrySet()) {
            activeCounts.put(e.getKey(), (long) e.getValue().size());
        }

        List<UserActivityResponse.UserPoint> newPoints = materializeUserPoints(r.from, r.to, b, newUsers);
        List<UserActivityResponse.UserPoint> activePoints = materializeUserPoints(r.from, r.to, b, activeCounts);

        return new UserActivityResponse(r.from, r.to, b, newPoints, activePoints);
    }

    private long countActiveUsers(LocalDateTimeRange range) {
        Set<UUID> active = new HashSet<>();
        active.addAll(orderAnalyticsService.findDistinctUserIdsBetween(range.from, range.to));
        active.addAll(reviewAnalyticsService.findDistinctUserIdsBetween(range.from, range.to));
        active.addAll(wishlistAnalyticsService.findDistinctUserIdsBetween(range.from, range.to));
        active.addAll(cartAnalyticsService.findDistinctUserIdsBetween(range.from, range.to));
        return active.size();
    }

    private void addActiveUsers(Map<LocalDate, Set<UUID>> activeByBucket, List<Object[]> rows, TimeBucket bucket) {
        for (Object[] row : rows) {
            UUID userId = (UUID) row[0];
            LocalDateTime ts = (LocalDateTime) row[1];
            LocalDate key = bucketStart(ts.toLocalDate(), bucket);
            activeByBucket.computeIfAbsent(key, __ -> new HashSet<>()).add(userId);
        }
    }

    private Map<LocalDate, Long> bucketCounts(List<LocalDateTime> timestamps, TimeBucket bucket) {
        Map<LocalDate, Long> out = new HashMap<>();
        for (LocalDateTime ts : timestamps) {
            LocalDate key = bucketStart(ts.toLocalDate(), bucket);
            out.merge(key, 1L, Long::sum);
        }
        return out;
    }

    private List<SalesPoint> materializeSalesPoints(LocalDate from, LocalDate to, TimeBucket bucket, Map<LocalDate, MutableSales> data) {
        List<SalesPoint> points = new ArrayList<>();
        LocalDate cursor = bucketStart(from, bucket);
        LocalDate end = bucketStart(to, bucket);
        while (!cursor.isAfter(end)) {
            MutableSales s = data.get(cursor);
            points.add(new SalesPoint(cursor, s == null ? 0 : s.orders, s == null ? BigDecimal.ZERO : s.revenue));
            cursor = nextBucket(cursor, bucket);
        }
        return points;
    }

    private List<UserActivityResponse.UserPoint> materializeUserPoints(LocalDate from, LocalDate to, TimeBucket bucket, Map<LocalDate, Long> data) {
        List<UserActivityResponse.UserPoint> points = new ArrayList<>();
        LocalDate cursor = bucketStart(from, bucket);
        LocalDate end = bucketStart(to, bucket);
        while (!cursor.isAfter(end)) {
            points.add(new UserActivityResponse.UserPoint(cursor, data.getOrDefault(cursor, 0L)));
            cursor = nextBucket(cursor, bucket);
        }
        return points;
    }

    private LocalDate bucketStart(LocalDate d, TimeBucket bucket) {
        return switch (bucket) {
            case DAY -> d;
            case WEEK -> {
                DayOfWeek first = WeekFields.ISO.getFirstDayOfWeek();
                yield d.with(TemporalAdjusters.previousOrSame(first));
            }
            case MONTH -> d.withDayOfMonth(1);
        };
    }

    private LocalDate nextBucket(LocalDate current, TimeBucket bucket) {
        return switch (bucket) {
            case DAY -> current.plusDays(1);
            case WEEK -> current.plusWeeks(1);
            case MONTH -> current.plusMonths(1).withDayOfMonth(1);
        };
    }

    private DateRange normalizeRange(LocalDate from, LocalDate to) {
        LocalDate f = from;
        LocalDate t = to;
        if (f == null && t == null) {
            t = LocalDate.now();
            f = t.minusDays(29);
        } else if (f == null) {
            f = t.minusDays(29);
        } else if (t == null) {
            t = f.plusDays(29);
        }
        if (t.isBefore(f)) {
            LocalDate tmp = f;
            f = t;
            t = tmp;
        }
        return new DateRange(f, t);
    }

    private LocalDateTimeRange toLocalDateTimeRange(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime endExclusive = to.plusDays(1).atStartOfDay();
        return new LocalDateTimeRange(start, endExclusive);
    }

    private record DateRange(LocalDate from, LocalDate to) {}

    private record LocalDateTimeRange(LocalDateTime from, LocalDateTime to) {}

    private static class MutableSales {
        long orders = 0;
        BigDecimal revenue = BigDecimal.ZERO;
    }
}
