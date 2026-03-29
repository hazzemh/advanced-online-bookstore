package com.example.bookstore.recommendation.controller;

import com.example.bookstore.book.dto.BookResponse;
import com.example.bookstore.recommendation.dto.UserPreferenceResponse;
import com.example.bookstore.recommendation.service.RecommendationService;
import com.example.bookstore.recommendation.service.RecommendationStrategy;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Recommendations", description = "Personalized recommendations and preference profile endpoints.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> getRecommendations(
            Authentication authentication,
            @RequestParam(defaultValue = "HYBRID") RecommendationStrategy strategy,
            @RequestParam(defaultValue = "10") int limit
    ) {
        String userEmail = authentication.getName();
        List<BookResponse> recs = recommendationService.recommend(userEmail, strategy, limit);
        return ResponseEntity.ok(recs);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserPreferenceResponse> getPreferenceProfile(
            Authentication authentication,
            @RequestParam(defaultValue = "5") int maxGenres,
            @RequestParam(defaultValue = "5") int maxAuthors
    ) {
        String userEmail = authentication.getName();
        UserPreferenceResponse profile = recommendationService.getUserPreferences(userEmail, maxGenres, maxAuthors);
        return ResponseEntity.ok(profile);
    }
}

