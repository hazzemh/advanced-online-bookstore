package com.example.bookstore.cart.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID cartId,
        List<CartItemResponse> items,
        Integer totalQuantity,
        BigDecimal subtotal
) {
}

