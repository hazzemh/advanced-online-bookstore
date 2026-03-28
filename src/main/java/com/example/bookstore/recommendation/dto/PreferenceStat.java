package com.example.bookstore.recommendation.dto;

import java.io.Serializable;

public record PreferenceStat(
        String key,
        int weight
) implements Serializable {}

