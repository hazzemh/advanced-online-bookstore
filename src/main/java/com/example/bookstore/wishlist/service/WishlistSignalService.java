package com.example.bookstore.wishlist.service;

import com.example.bookstore.wishlist.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WishlistSignalService {

    private final WishlistRepository wishlistRepository;

    public WishlistSignalService(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    public List<UUID> findBookIdsByUserId(UUID userId) {
        return wishlistRepository.findBookIdsByUserId(userId);
    }

    public List<Object[]> findAllUserBookIds() {
        return wishlistRepository.findAllUserBookIds();
    }
}

