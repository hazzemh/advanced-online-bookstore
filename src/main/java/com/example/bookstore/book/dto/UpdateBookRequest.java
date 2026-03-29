package com.example.bookstore.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateBookRequest(
    @Schema(example = "Clean Code (2nd Edition)")
    @NotBlank
    @Size(max = 200)
    String title,

    @Schema(example = "Robert C. Martin")
    @NotBlank
    @Size(max = 120)
    String author,

    @Schema(example = "Updated edition with additional content.")
    @Size(max = 5000)
    String description,

    @Schema(example = "69.99")
    @NotNull
    @Positive
    BigDecimal price,

    @Schema(example = "10")
    @NotNull
    @Min(0)
    Integer stockQuantity,

    @Schema(example = "9780132350884")
    @NotBlank
    @Size(max = 32)
    String isbn,

    @Schema(example = "Software Engineering")
    @Size(max = 80)
    String genre,

    @Schema(example = "2026")
    @Min(0)
    Integer publicationYear,

    @Schema(example = "500")
    @Min(0)
    Integer pages,

    @Schema(example = "Prentice Hall")
    @Size(max = 120)
    String publisher
) {}

