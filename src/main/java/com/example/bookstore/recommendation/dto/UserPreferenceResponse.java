package com.example.bookstore.recommendation.dto;

import java.util.List;

public record UserPreferenceResponse(
        List<PreferenceStat> topGenres,
        List<PreferenceStat> topAuthors,
        int positiveSignals
) {
}

