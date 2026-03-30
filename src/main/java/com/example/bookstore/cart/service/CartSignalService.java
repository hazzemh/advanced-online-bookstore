package com.example.bookstore.cart.service;

import com.example.bookstore.cart.repository.CartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CartSignalService {

    private final CartItemRepository cartItemRepository;

    public CartSignalService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    public List<UUID> findBookIdsInUserCart(UUID userId) {
        return cartItemRepository.findBookIdsInUserCart(userId);
    }
}

