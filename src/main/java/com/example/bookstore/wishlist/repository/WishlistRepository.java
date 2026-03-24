package com.example.bookstore.wishlist.repository;

import com.example.bookstore.wishlist.entity.WishlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WishlistRepository extends JpaRepository<WishlistItem, UUID> {

    Optional<WishlistItem> findByUserIdAndBookId(UUID userId, UUID bookId);

    boolean existsByUserIdAndBookId(UUID userId, UUID bookId);

    Page<WishlistItem> findByUserIdOrderByAddedAtDesc(UUID userId, Pageable pageable);

    @Query("select w.book.id from WishlistItem w where w.user.id = :userId")
    List<UUID> findBookIdsByUserId(@Param("userId") UUID userId);

    @Query("select w.user.id, w.book.id from WishlistItem w")
    List<Object[]> findAllUserBookIds();
}
