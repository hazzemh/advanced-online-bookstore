package com.example.bookstore.review.service;

import com.example.bookstore.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReviewSignalService {

    private final ReviewRepository reviewRepository;

    public ReviewSignalService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public List<UUID> findReviewedBookIdsByUserId(UUID userId) {
        return reviewRepository.findReviewedBookIdsByUserId(userId);
    }

    public List<UUID> findBookIdsByUserIdAndMinRating(UUID userId, int minRating) {
        return reviewRepository.findBookIdsByUserIdAndMinRating(userId, minRating);
    }

    public List<UUID> findBookIdsByUserIdAndMaxRating(UUID userId, int maxRating) {
        return reviewRepository.findBookIdsByUserIdAndMaxRating(userId, maxRating);
    }

    public List<Object[]> findAllUserBookIdsWithMinRating(int minRating) {
        return reviewRepository.findAllUserBookIdsWithMinRating(minRating);
    }
}

