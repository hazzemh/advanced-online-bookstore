package com.example.bookstore.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Standard error response payload.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    @Schema(example = "Validation failed")
    private String message;

    @Schema(example = "400")
    private int status;

    @Schema(example = "1711690000000")
    private long timestamp;
}

