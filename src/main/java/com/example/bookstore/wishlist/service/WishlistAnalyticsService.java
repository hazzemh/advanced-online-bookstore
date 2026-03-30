package com.example.bookstore.wishlist.service;

import com.example.bookstore.wishlist.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WishlistAnalyticsService {

    private final WishlistRepository wishlistRepository;

    public WishlistAnalyticsService(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    public List<UUID> findDistinctUserIdsBetween(LocalDateTime from, LocalDateTime to) {
        return wishlistRepository.findDistinctUserIdsBetween(from, to);
    }

    public List<Object[]> findUserIdsAndAddedAtBetween(LocalDateTime from, LocalDateTime to) {
        return wishlistRepository.findUserIdsAndAddedAtBetween(from, to);
    }
}

