package com.example.bookstore.review.service;

import com.example.bookstore.review.entity.Review;
import com.example.bookstore.review.dto.CreateReviewRequest;
import com.example.bookstore.review.dto.UpdateReviewRequest;
import com.example.bookstore.review.dto.ReviewResponse;
import com.example.bookstore.review.repository.ReviewRepository;
import com.example.bookstore.review.mapper.ReviewMapper;
import com.example.bookstore.book.entity.Book;
import com.example.bookstore.book.service.BookService;
import com.example.bookstore.user.entity.User;
import com.example.bookstore.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final BookService bookService;
    private final UserService userService;

    public ReviewService(ReviewRepository reviewRepository,
                        ReviewMapper reviewMapper,
                        BookService bookService,
                        UserService userService) {
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
        this.bookService = bookService;
        this.userService = userService;
    }

    /**
     * Create a new review for a book
     */
    public ReviewResponse createReview(CreateReviewRequest request, UUID userId) {
        // Validate rating is between 1-5
        if (request.rating() < 1 || request.rating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Check if user already reviewed this book
        if (reviewRepository.findByBookIdAndUserId(request.bookId(), userId).isPresent()) {
            throw new RuntimeException("User has already reviewed this book");
        }

        Book book = bookService.requireActiveBookEntity(request.bookId());
        User user = userService.requireUserById(userId);

        Review review = Review.builder()
                .book(book)
                .user(user)
                .rating(request.rating())
                .comment(request.comment())
                .isVerifiedPurchase(false)
                .build();

        Review savedReview = reviewRepository.save(review);

        // Update book's average rating
        updateBookAverageRating(book.getId());

        return reviewMapper.mapToResponse(savedReview);
    }

    /**
     * Update a review
     */
    public ReviewResponse updateReview(UUID reviewId, UpdateReviewRequest request, UUID userId) {
        if (request.rating() < 1 || request.rating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // Only the review author can update it
        if (!review.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only update your own reviews");
        }

        review.setRating(request.rating());
        review.setComment(request.comment());

        Review updatedReview = reviewRepository.save(review);

        // Update book's average rating
        updateBookAverageRating(review.getBook().getId());

        return reviewMapper.mapToResponse(updatedReview);
    }

    /**
     * Delete a review
     */
    public void deleteReview(UUID reviewId, UUID userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // Only the review author or admin can delete it
        if (!review.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own reviews");
        }

        UUID bookId = review.getBook().getId();
        reviewRepository.deleteById(reviewId);

        // Update book's average rating
        updateBookAverageRating(bookId);
    }

    /**
     * Get all reviews for a book
     */
    public Page<ReviewResponse> getBookReviews(UUID bookId, Pageable pageable) {
        // Verify book exists
        bookService.getBookEntityById(bookId);

        return reviewRepository.findByBookId(bookId, pageable)
                .map(reviewMapper::mapToResponse);
    }

    /**
     * Get all reviews by a user
     */
    public Page<ReviewResponse> getUserReviews(UUID userId, Pageable pageable) {
        // Verify user exists
        userService.requireUserById(userId);

        return reviewRepository.findByUserId(userId, pageable)
                .map(reviewMapper::mapToResponse);
    }

    /**
     * Get a specific review
     */
    public ReviewResponse getReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        return reviewMapper.mapToResponse(review);
    }

    /**
     * Get reviews for a book with minimum rating
     */
    public Page<ReviewResponse> getBookReviewsByMinRating(UUID bookId, Integer minRating, Pageable pageable) {
        if (minRating < 1 || minRating > 5) {
            throw new IllegalArgumentException("Min rating must be between 1 and 5");
        }

        bookService.getBookEntityById(bookId);

        return reviewRepository.findByBookIdAndRatingGreaterThan(bookId, minRating, pageable)
                .map(reviewMapper::mapToResponse);
    }

    /**
     * Update book's average rating and review count
     */
    @Transactional
    public void updateBookAverageRating(UUID bookId) {
        Double averageRating = reviewRepository.getAverageRatingByBookId(bookId);
        Long reviewCount = reviewRepository.countByBookId(bookId);
        bookService.updateRatingStats(
                bookId,
                averageRating != null ? averageRating : 0.0,
                reviewCount == null ? 0 : reviewCount.intValue()
        );
    }

    /**
     * Check if user has reviewed a book
     */
    public boolean hasUserReviewedBook(UUID bookId, UUID userId) {
        return reviewRepository.findByBookIdAndUserId(bookId, userId).isPresent();
    }

    /**
     * Get user's review for a book
     */
    public ReviewResponse getUserReviewForBook(UUID bookId, UUID userId) {
        Review review = reviewRepository.findByBookIdAndUserId(bookId, userId)
                .orElseThrow(() -> new RuntimeException("No review found for this user and book"));
        return reviewMapper.mapToResponse(review);
    }
}

