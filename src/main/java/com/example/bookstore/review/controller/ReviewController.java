package com.example.bookstore.review.controller;

import com.example.bookstore.review.dto.CreateReviewRequest;
import com.example.bookstore.review.dto.UpdateReviewRequest;
import com.example.bookstore.review.dto.ReviewResponse;
import com.example.bookstore.review.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Create a new review for a book
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponse> createReview(
            @RequestBody CreateReviewRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        ReviewResponse response = reviewService.createReview(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all reviews for a book
     */
    @GetMapping("/book/{bookId}")
    public ResponseEntity<Page<ReviewResponse>> getBookReviews(
            @PathVariable UUID bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse> response = reviewService.getBookReviews(bookId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get reviews for a book with minimum rating filter
     */
    @GetMapping("/book/{bookId}/min-rating")
    public ResponseEntity<Page<ReviewResponse>> getBookReviewsByMinRating(
            @PathVariable UUID bookId,
            @RequestParam Integer minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse> response = reviewService.getBookReviewsByMinRating(bookId, minRating, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all reviews by a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ReviewResponse>> getUserReviews(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse> response = reviewService.getUserReviews(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific review
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable UUID reviewId) {
        ReviewResponse response = reviewService.getReview(reviewId);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if user has reviewed a book
     */
    @GetMapping("/book/{bookId}/user/{userId}/has-review")
    public ResponseEntity<Boolean> hasUserReviewedBook(
            @PathVariable UUID bookId,
            @PathVariable UUID userId) {
        boolean hasReviewed = reviewService.hasUserReviewedBook(bookId, userId);
        return ResponseEntity.ok(hasReviewed);
    }

    /**
     * Get user's review for a book
     */
    @GetMapping("/book/{bookId}/user/{userId}")
    public ResponseEntity<ReviewResponse> getUserReviewForBook(
            @PathVariable UUID bookId,
            @PathVariable UUID userId) {
        ReviewResponse response = reviewService.getUserReviewForBook(bookId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a review
     */
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable UUID reviewId,
            @RequestBody UpdateReviewRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        ReviewResponse response = reviewService.updateReview(reviewId, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a review
     */
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID reviewId,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }
}

