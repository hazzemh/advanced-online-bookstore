package com.example.bookstore.cart.repository;

import com.example.bookstore.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCartIdAndBookId(UUID cartId, UUID bookId);

    Optional<CartItem> findByIdAndCartUserId(UUID itemId, UUID userId);
}

