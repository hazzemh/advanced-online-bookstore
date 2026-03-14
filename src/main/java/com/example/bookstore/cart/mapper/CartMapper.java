package com.example.bookstore.cart.mapper;

import com.example.bookstore.cart.dto.CartItemResponse;
import com.example.bookstore.cart.dto.CartResponse;
import com.example.bookstore.cart.entity.Cart;
import com.example.bookstore.cart.entity.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Component
public class CartMapper {

    public CartResponse toResponse(Cart cart) {
        if (cart == null) {
            return empty(null);
        }

        List<CartItemResponse> items = cart.getItems().stream()
                .sorted(Comparator.comparing(CartItem::getCreatedAt))
                .map(this::toItemResponse)
                .toList();

        int totalQuantity = items.stream()
                .map(CartItemResponse::quantity)
                .filter(q -> q != null)
                .mapToInt(Integer::intValue)
                .sum();

        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::lineTotal)
                .filter(t -> t != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), items, totalQuantity, subtotal);
    }

    public CartResponse empty(UUID cartId) {
        return new CartResponse(cartId, Collections.emptyList(), 0, BigDecimal.ZERO);
    }

    private CartItemResponse toItemResponse(CartItem item) {
        BigDecimal lineTotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponse(
                item.getId(),
                item.getBook().getId(),
                item.getBook().getTitle(),
                item.getBook().getAuthor(),
                item.getBook().getImageUrl(),
                item.getUnitPrice(),
                item.getQuantity(),
                lineTotal
        );
    }
}

