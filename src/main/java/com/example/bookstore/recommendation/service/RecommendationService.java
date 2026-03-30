package com.example.bookstore.recommendation.service;

import com.example.bookstore.book.dto.BookResponse;
import com.example.bookstore.book.entity.Book;
import com.example.bookstore.book.service.BookService;
import com.example.bookstore.cart.repository.CartItemRepository;
import com.example.bookstore.order.entity.OrderStatus;
import com.example.bookstore.order.repository.OrderItemRepository;
import com.example.bookstore.recommendation.dto.PreferenceStat;
import com.example.bookstore.recommendation.dto.UserPreferenceResponse;
import com.example.bookstore.review.repository.ReviewRepository;
import com.example.bookstore.user.entity.User;
import com.example.bookstore.user.service.UserService;
import com.example.bookstore.wishlist.repository.WishlistRepository;
import com.example.bookstore.config.CacheConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private static final int POSITIVE_REVIEW_RATING = 4;
    private static final int NEGATIVE_REVIEW_RATING = 2;

    private static final EnumSet<OrderStatus> POSITIVE_ORDER_STATUSES =
            EnumSet.of(OrderStatus.PAID, OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private final UserService userService;
    private final BookService bookService;
    private final ReviewRepository reviewRepository;
    private final WishlistRepository wishlistRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;

    public RecommendationService(
            UserService userService,
            BookService bookService,
            ReviewRepository reviewRepository,
            WishlistRepository wishlistRepository,
            CartItemRepository cartItemRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.userService = userService;
        this.bookService = bookService;
        this.reviewRepository = reviewRepository;
        this.wishlistRepository = wishlistRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Cacheable(
            value = CacheConfig.RECOMMENDATIONS_CACHE,
            key = "#userEmail + ':' + (#strategy == null ? 'HYBRID' : #strategy.name()) + ':' + #limit"
    )
    public List<BookResponse> recommend(String userEmail, RecommendationStrategy strategy, int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be >= 1");
        }

        User user = getUserByEmail(userEmail);

        UUID userId = user.getId();

        Set<UUID> purchasedBookIds = new HashSet<>(
                orderItemRepository.findPurchasedBookIdsByUserIdAndStatuses(userId, POSITIVE_ORDER_STATUSES)
        );
        Set<UUID> wishlistBookIds = new HashSet<>(wishlistRepository.findBookIdsByUserId(userId));
        Set<UUID> cartBookIds = new HashSet<>(cartItemRepository.findBookIdsInUserCart(userId));
        Set<UUID> reviewedBookIds = new HashSet<>(reviewRepository.findReviewedBookIdsByUserId(userId));

        Set<UUID> positiveReviewBookIds = new HashSet<>(reviewRepository.findBookIdsByUserIdAndMinRating(userId, POSITIVE_REVIEW_RATING));
        Set<UUID> negativeReviewBookIds = new HashSet<>(reviewRepository.findBookIdsByUserIdAndMaxRating(userId, NEGATIVE_REVIEW_RATING));

        Set<UUID> positiveSignalBookIds = new HashSet<>();
        positiveSignalBookIds.addAll(purchasedBookIds);
        positiveSignalBookIds.addAll(wishlistBookIds);
        positiveSignalBookIds.addAll(cartBookIds);
        positiveSignalBookIds.addAll(positiveReviewBookIds);

        Set<UUID> excludedBookIds = new HashSet<>();
        excludedBookIds.addAll(purchasedBookIds);
        excludedBookIds.addAll(wishlistBookIds);
        excludedBookIds.addAll(cartBookIds);
        excludedBookIds.addAll(reviewedBookIds);
        excludedBookIds.addAll(negativeReviewBookIds);

        Map<String, Integer> genrePrefs = new HashMap<>();
        Map<String, Integer> authorPrefs = new HashMap<>();
        // Purchases are the strongest signal.
        buildPreferencesFromBooks(purchasedBookIds, 3, genrePrefs, authorPrefs);
        buildPreferencesFromBooks(wishlistBookIds, 2, genrePrefs, authorPrefs);
        buildPreferencesFromBooks(cartBookIds, 1, genrePrefs, authorPrefs);
        buildPreferencesFromBooks(positiveReviewBookIds, 1, genrePrefs, authorPrefs);

        Map<UUID, Double> scored;
        if (strategy == null) {
            strategy = RecommendationStrategy.HYBRID;
        }
        switch (strategy) {
            case CONTENT -> scored = contentBasedScores(genrePrefs, authorPrefs, excludedBookIds);
            case COLLABORATIVE -> scored = collaborativeScores(userId, excludedBookIds);
            case HYBRID -> {
                Map<UUID, Double> content = contentBasedScores(genrePrefs, authorPrefs, excludedBookIds);
                Map<UUID, Double> collab = collaborativeScores(userId, excludedBookIds);
                scored = mergeScores(content, collab, 0.6, 0.8);
            }
            default -> throw new IllegalArgumentException("Unknown strategy: " + strategy);
        }

        // If we have no signals yet, fall back to top-rated active books.
        if (scored.isEmpty()) {
            return fallbackTopRated(excludedBookIds, limit);
        }

        List<UUID> topIds = scored.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();

        Map<UUID, Book> byId = bookService.loadBooksById(topIds);

        // Preserve score ordering.
        List<BookResponse> result = new ArrayList<>(topIds.size());
        for (UUID id : topIds) {
            Book b = byId.get(id);
            if (b == null) {
                continue;
            }
            if (!Boolean.TRUE.equals(b.getIsActive())) {
                continue;
            }
            result.add(mapToResponse(b));
        }
        return result;
    }

    @Cacheable(
            value = CacheConfig.RECOMMENDATION_PROFILE_CACHE,
            key = "#userEmail + ':' + #maxGenres + ':' + #maxAuthors"
    )
    public UserPreferenceResponse getUserPreferences(String userEmail, int maxGenres, int maxAuthors) {
        if (maxGenres <= 0 || maxAuthors <= 0) {
            throw new IllegalArgumentException("maxGenres/maxAuthors must be >= 1");
        }

        User user = getUserByEmail(userEmail);
        UUID userId = user.getId();

        Set<UUID> positiveSignalBookIds = new HashSet<>();
        positiveSignalBookIds.addAll(orderItemRepository.findPurchasedBookIdsByUserIdAndStatuses(userId, POSITIVE_ORDER_STATUSES));
        positiveSignalBookIds.addAll(wishlistRepository.findBookIdsByUserId(userId));
        positiveSignalBookIds.addAll(cartItemRepository.findBookIdsInUserCart(userId));
        positiveSignalBookIds.addAll(reviewRepository.findBookIdsByUserIdAndMinRating(userId, POSITIVE_REVIEW_RATING));

        Map<String, Integer> genrePrefs = new HashMap<>();
        Map<String, Integer> authorPrefs = new HashMap<>();
        // Keep it simple: aggregate all positive signals for the profile.
        buildPreferencesFromBooks(positiveSignalBookIds, 1, genrePrefs, authorPrefs);

        List<PreferenceStat> topGenres = topStats(genrePrefs, maxGenres);
        List<PreferenceStat> topAuthors = topStats(authorPrefs, maxAuthors);

        return new UserPreferenceResponse(topGenres, topAuthors, positiveSignalBookIds.size());
    }

    private void buildPreferencesFromBooks(Set<UUID> bookIds, int weight, Map<String, Integer> genrePrefs, Map<String, Integer> authorPrefs) {
        if (bookIds == null || bookIds.isEmpty()) {
            return;
        }
        int w = Math.max(1, weight);
        for (Book b : bookService.loadBooksById(bookIds).values()) {
            if (!Boolean.TRUE.equals(b.getIsActive())) {
                continue;
            }
            String genre = normalize(b.getGenre());
            if (genre != null) {
                genrePrefs.merge(genre, w, Integer::sum);
            }
            String author = normalize(b.getAuthor());
            if (author != null) {
                authorPrefs.merge(author, w, Integer::sum);
            }
        }
    }

    private Map<UUID, Double> contentBasedScores(
            Map<String, Integer> genrePrefs,
            Map<String, Integer> authorPrefs,
            Set<UUID> excludedBookIds
    ) {
        if ((genrePrefs == null || genrePrefs.isEmpty()) && (authorPrefs == null || authorPrefs.isEmpty())) {
            return Collections.emptyMap();
        }

        Map<UUID, Double> scores = new HashMap<>();
        for (Book b : bookService.getAllBookEntities()) {
            if (!Boolean.TRUE.equals(b.getIsActive())) {
                continue;
            }
            if (b.getStockQuantity() != null && b.getStockQuantity() <= 0) {
                continue;
            }
            UUID id = b.getId();
            if (id == null || (excludedBookIds != null && excludedBookIds.contains(id))) {
                continue;
            }

            double score = 0.0;

            String genre = normalize(b.getGenre());
            if (genre != null) {
                score += 3.0 * genrePrefs.getOrDefault(genre, 0);
            }
            String author = normalize(b.getAuthor());
            if (author != null) {
                score += 2.0 * authorPrefs.getOrDefault(author, 0);
            }

            double ratingBoost = b.getAverageRating() == null ? 0.0 : (b.getAverageRating() / 5.0);
            score += 0.5 * ratingBoost;

            if (score > 0.0) {
                scores.put(id, score);
            }
        }
        return scores;
    }

    private Map<UUID, Double> collaborativeScores(UUID userId, Set<UUID> excludedBookIds) {
        Set<UUID> myLikes = new HashSet<>();
        myLikes.addAll(orderItemRepository.findPurchasedBookIdsByUserIdAndStatuses(userId, POSITIVE_ORDER_STATUSES));
        myLikes.addAll(wishlistRepository.findBookIdsByUserId(userId));
        myLikes.addAll(reviewRepository.findBookIdsByUserIdAndMinRating(userId, POSITIVE_REVIEW_RATING));
        myLikes.addAll(cartItemRepository.findBookIdsInUserCart(userId));

        if (myLikes.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<UUID, Set<UUID>> likesByUser = new HashMap<>();

        for (Object[] row : orderItemRepository.findAllUserBookIdsByStatuses(POSITIVE_ORDER_STATUSES)) {
            UUID u = (UUID) row[0];
            UUID b = (UUID) row[1];
            likesByUser.computeIfAbsent(u, __ -> new HashSet<>()).add(b);
        }

        for (Object[] row : wishlistRepository.findAllUserBookIds()) {
            UUID u = (UUID) row[0];
            UUID b = (UUID) row[1];
            likesByUser.computeIfAbsent(u, __ -> new HashSet<>()).add(b);
        }

        for (Object[] row : reviewRepository.findAllUserBookIdsWithMinRating(POSITIVE_REVIEW_RATING)) {
            UUID u = (UUID) row[0];
            UUID b = (UUID) row[1];
            likesByUser.computeIfAbsent(u, __ -> new HashSet<>()).add(b);
        }

        Map<UUID, Double> candidateScores = new HashMap<>();

        int mySize = myLikes.size();
        for (Map.Entry<UUID, Set<UUID>> entry : likesByUser.entrySet()) {
            UUID otherUser = entry.getKey();
            if (userId.equals(otherUser)) {
                continue;
            }

            Set<UUID> otherLikes = entry.getValue();
            if (otherLikes == null || otherLikes.isEmpty()) {
                continue;
            }

            int overlap = intersectionSize(myLikes, otherLikes);
            if (overlap == 0) {
                continue;
            }

            // Cosine similarity on binary likes.
            double sim = overlap / Math.sqrt((double) mySize * (double) otherLikes.size());

            for (UUID bookId : otherLikes) {
                if (bookId == null) {
                    continue;
                }
                if (myLikes.contains(bookId)) {
                    continue;
                }
                if (excludedBookIds != null && excludedBookIds.contains(bookId)) {
                    continue;
                }
                candidateScores.merge(bookId, sim, Double::sum);
            }
        }

        // Add a tiny global-quality boost to help ordering ties.
        if (!candidateScores.isEmpty()) {
            Map<UUID, Book> byId = bookService.loadBooksById(candidateScores.keySet());
            for (Map.Entry<UUID, Double> e : new ArrayList<>(candidateScores.entrySet())) {
                Book b = byId.get(e.getKey());
                if (b == null || !Boolean.TRUE.equals(b.getIsActive())) {
                    candidateScores.remove(e.getKey());
                    continue;
                }
                double ratingBoost = b.getAverageRating() == null ? 0.0 : (b.getAverageRating() / 5.0);
                candidateScores.put(e.getKey(), e.getValue() + 0.1 * ratingBoost);
            }
        }

        return candidateScores;
    }

    private Map<UUID, Double> mergeScores(Map<UUID, Double> a, Map<UUID, Double> b, double aWeight, double bWeight) {
        Map<UUID, Double> merged = new HashMap<>();
        if (a != null) {
            for (Map.Entry<UUID, Double> e : a.entrySet()) {
                merged.put(e.getKey(), e.getValue() * aWeight);
            }
        }
        if (b != null) {
            for (Map.Entry<UUID, Double> e : b.entrySet()) {
                merged.merge(e.getKey(), e.getValue() * bWeight, Double::sum);
            }
        }
        return merged;
    }

    private List<BookResponse> fallbackTopRated(Set<UUID> excludedBookIds, int limit) {
        List<Book> all = bookService.getAllBookEntities();
        return all.stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsActive()))
                .filter(b -> b.getStockQuantity() == null || b.getStockQuantity() > 0)
                .filter(b -> excludedBookIds == null || !excludedBookIds.contains(b.getId()))
                .sorted(Comparator.comparing(Book::getAverageRating, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .map(this::mapToResponse)
                .toList();
    }

    private int intersectionSize(Set<UUID> a, Set<UUID> b) {
        if (a.size() > b.size()) {
            Set<UUID> tmp = a;
            a = b;
            b = tmp;
        }
        int count = 0;
        for (UUID id : a) {
            if (b.contains(id)) {
                count++;
            }
        }
        return count;
    }

    private String normalize(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim().toLowerCase(Locale.ROOT);
        return t.isBlank() ? null : t;
    }

    private List<PreferenceStat> topStats(Map<String, Integer> counts, int limit) {
        return counts.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(e -> new PreferenceStat(e.getKey(), e.getValue()))
                .toList();
    }

    private User getUserByEmail(String email) {
        return userService.requireUserByEmail(email);
    }

    private BookResponse mapToResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getDescription(),
                book.getPrice(),
                book.getStockQuantity(),
                book.getIsbn(),
                book.getGenre(),
                book.getPublicationYear(),
                book.getPages(),
                book.getPublisher(),
                book.getImageUrl(),
                book.getAverageRating(),
                book.getTotalReviews(),
                book.getIsActive()
        );
    }
}

