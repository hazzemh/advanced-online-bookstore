package com.example.bookstore.review.service;

import com.example.bookstore.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReviewAnalyticsService {

    private final ReviewRepository reviewRepository;

    public ReviewAnalyticsService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public List<UUID> findDistinctUserIdsBetween(LocalDateTime from, LocalDateTime to) {
        return reviewRepository.findDistinctUserIdsBetween(from, to);
    }

    public List<Object[]> findUserIdsAndCreatedAtBetween(LocalDateTime from, LocalDateTime to) {
        return reviewRepository.findUserIdsAndCreatedAtBetween(from, to);
    }
}

