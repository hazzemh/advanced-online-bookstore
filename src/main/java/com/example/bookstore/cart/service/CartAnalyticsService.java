package com.example.bookstore.cart.service;

import com.example.bookstore.cart.repository.CartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CartAnalyticsService {

    private final CartItemRepository cartItemRepository;

    public CartAnalyticsService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    public List<UUID> findDistinctUserIdsBetween(LocalDateTime from, LocalDateTime to) {
        return cartItemRepository.findDistinctUserIdsBetween(from, to);
    }

    public List<Object[]> findUserIdsAndUpdatedAtBetween(LocalDateTime from, LocalDateTime to) {
        return cartItemRepository.findUserIdsAndUpdatedAtBetween(from, to);
    }
}

