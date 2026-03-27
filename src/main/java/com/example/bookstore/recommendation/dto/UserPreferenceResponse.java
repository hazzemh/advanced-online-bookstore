package com.example.bookstore.recommendation.dto;

import java.io.Serializable;
import java.util.List;

public record UserPreferenceResponse(
        List<PreferenceStat> topGenres,
        List<PreferenceStat> topAuthors,
        int positiveSignals
) implements Serializable {}

