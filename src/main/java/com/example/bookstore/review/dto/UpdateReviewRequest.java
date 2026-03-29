package com.example.bookstore.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateReviewRequest(
    @Schema(description = "Rating 1..5.", example = "4")
    @NotNull
    @Min(1)
    @Max(5)
    Integer rating,

    @Schema(description = "Optional review comment.", example = "Still good on re-read.")
    @Size(max = 2000)
    String comment
) {}

