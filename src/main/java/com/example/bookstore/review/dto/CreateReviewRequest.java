package com.example.bookstore.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateReviewRequest(
    @Schema(description = "Book id to review.")
    @NotNull
    UUID bookId,

    @Schema(description = "Rating 1..5.", example = "5")
    @NotNull
    @Min(1)
    @Max(5)
    Integer rating,

    @Schema(description = "Optional review comment.", example = "Great book.")
    @Size(max = 2000)
    String comment
) {}

