package com.example.bookstore.book.dto;

import java.math.BigDecimal;

public record CreateBookRequest(
    String title,
    String author,
    String description,
    BigDecimal price,
    Integer stockQuantity,
    String isbn,
    String genre,
    Integer publicationYear,
    Integer pages,
    String publisher
) {}

