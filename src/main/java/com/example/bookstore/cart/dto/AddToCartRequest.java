package com.example.bookstore.cart.dto;

import java.util.UUID;

public record AddToCartRequest(
        UUID bookId,
        Integer quantity
) {
}

