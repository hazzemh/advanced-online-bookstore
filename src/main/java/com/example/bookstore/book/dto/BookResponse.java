package com.example.bookstore.book.dto;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.UUID;

public record BookResponse(
    UUID id,
    String title,
    String author,
    String description,
    BigDecimal price,
    Integer stockQuantity,
    String isbn,
    String genre,
    Integer publicationYear,
    Integer pages,
    String publisher,
    String imageUrl,
    Double averageRating,
    Integer totalReviews,
    Boolean isActive
) implements Serializable {}

