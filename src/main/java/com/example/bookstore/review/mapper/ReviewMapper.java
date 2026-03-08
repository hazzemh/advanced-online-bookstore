package com.example.bookstore.review.mapper;

import com.example.bookstore.review.entity.Review;
import com.example.bookstore.review.dto.ReviewResponse;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse mapToResponse(Review review) {
        String userName = review.getUser().getFirstName() != null && review.getUser().getLastName() != null
                ? review.getUser().getFirstName() + " " + review.getUser().getLastName()
                : review.getUser().getEmail();

        return new ReviewResponse(
                review.getId(),
                review.getBook().getId(),
                review.getBook().getTitle(),
                review.getUser().getId(),
                userName,
                review.getRating(),
                review.getComment(),
                review.getIsVerifiedPurchase(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}

