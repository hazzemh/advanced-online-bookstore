package com.example.bookstore.cart.repository;

import com.example.bookstore.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCartIdAndBookId(UUID cartId, UUID bookId);

    Optional<CartItem> findByIdAndCartUserId(UUID itemId, UUID userId);

    @Query("select ci.book.id from CartItem ci where ci.cart.user.id = :userId")
    List<UUID> findBookIdsInUserCart(@Param("userId") UUID userId);
}

