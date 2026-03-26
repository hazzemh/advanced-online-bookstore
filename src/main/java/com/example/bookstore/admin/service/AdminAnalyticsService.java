package com.example.bookstore.admin.service;

import com.example.bookstore.admin.dto.*;
import com.example.bookstore.cart.repository.CartItemRepository;
import com.example.bookstore.order.entity.OrderStatus;
import com.example.bookstore.order.repository.OrderItemRepository;
import com.example.bookstore.order.repository.OrderRepository;
import com.example.bookstore.review.repository.ReviewRepository;
import com.example.bookstore.user.repository.UserRepository;
import com.example.bookstore.wishlist.repository.WishlistRepository;
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

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final WishlistRepository wishlistRepository;
    private final CartItemRepository cartItemRepository;

    public AdminAnalyticsService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository,
            ReviewRepository reviewRepository,
            WishlistRepository wishlistRepository,
            CartItemRepository cartItemRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.wishlistRepository = wishlistRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public AnalyticsSummaryResponse getSummary(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("days must be >= 1");
        }

        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(days - 1L);
        LocalDate toDate = today;

        LocalDateTimeRange range = toLocalDateTimeRange(fromDate, toDate);

        long totalUsers = userRepository.count();
        long newUsers = userRepository.countCreatedBetween(range.from, range.to);

        long totalOrders = orderRepository.count();
        long paidOrders = orderRepository.countByStatusIn(REVENUE_STATUSES);

        BigDecimal revenue = orderRepository.sumSubtotalByStatusIn(REVENUE_STATUSES);
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
        List<Object[]> rows = orderRepository.findCreatedAtAndSubtotalBetweenAndStatusIn(range.from, range.to, REVENUE_STATUSES);

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

        List<Object[]> rows = orderItemRepository.sumByBookBetweenAndOrderStatusIn(range.from, range.to, REVENUE_STATUSES);

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
        List<Object[]> rows = orderItemRepository.sumByGenreBetweenAndOrderStatusIn(range.from, range.to, REVENUE_STATUSES);

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

        List<LocalDateTime> userCreatedAts = userRepository.findCreatedAtBetween(range.from, range.to);
        Map<LocalDate, Long> newUsers = bucketCounts(userCreatedAts, b);

        // "Active" user = did any action (order/review/wishlist/cart update) in the range.
        Map<LocalDate, Set<UUID>> activeByBucket = new HashMap<>();
        addActiveUsers(activeByBucket, orderRepository.findUserIdsAndCreatedAtBetween(range.from, range.to), b);
        addActiveUsers(activeByBucket, reviewRepository.findUserIdsAndCreatedAtBetween(range.from, range.to), b);
        addActiveUsers(activeByBucket, wishlistRepository.findUserIdsAndAddedAtBetween(range.from, range.to), b);
        addActiveUsers(activeByBucket, cartItemRepository.findUserIdsAndUpdatedAtBetween(range.from, range.to), b);

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
        active.addAll(orderRepository.findDistinctUserIdsBetween(range.from, range.to));
        active.addAll(reviewRepository.findDistinctUserIdsBetween(range.from, range.to));
        active.addAll(wishlistRepository.findDistinctUserIdsBetween(range.from, range.to));
        active.addAll(cartItemRepository.findDistinctUserIdsBetween(range.from, range.to));
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
