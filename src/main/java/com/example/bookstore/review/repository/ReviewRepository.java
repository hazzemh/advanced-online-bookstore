package com.example.bookstore.review.repository;

import com.example.bookstore.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    /**
     * Find all reviews for a specific book
     */
    Page<Review> findByBookId(UUID bookId, Pageable pageable);

    /**
     * Find all reviews by a specific user
     */
    Page<Review> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find a specific review by book and user
     */
    Optional<Review> findByBookIdAndUserId(UUID bookId, UUID userId);

    /**
     * Find reviews with minimum rating for a book
     */
    @Query("SELECT r FROM Review r WHERE r.book.id = :bookId AND r.rating >= :minRating")
    Page<Review> findByBookIdAndRatingGreaterThan(@Param("bookId") UUID bookId, @Param("minRating") Integer minRating, Pageable pageable);

    /**
     * Count reviews for a book
     */
    Long countByBookId(UUID bookId);

    /**
     * Count verified purchase reviews for a book
     */
    Long countByBookIdAndIsVerifiedPurchaseTrue(UUID bookId);

    /**
     * Get average rating for a book
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Double getAverageRatingByBookId(@Param("bookId") UUID bookId);
}

