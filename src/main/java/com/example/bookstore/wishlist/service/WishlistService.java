package com.example.bookstore.wishlist.service;

import com.example.bookstore.book.entity.Book;
import com.example.bookstore.book.service.BookService;
import com.example.bookstore.user.entity.User;
import com.example.bookstore.user.service.UserService;
import com.example.bookstore.wishlist.dto.WishlistResponse;
import com.example.bookstore.wishlist.entity.WishlistItem;
import com.example.bookstore.wishlist.mapper.WishlistMapper;
import com.example.bookstore.wishlist.repository.WishlistRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistMapper wishlistMapper;
    private final UserService userService;
    private final BookService bookService;

    public WishlistService(WishlistRepository wishlistRepository,
                           WishlistMapper wishlistMapper,
                           UserService userService,
                           BookService bookService) {
        this.wishlistRepository = wishlistRepository;
        this.wishlistMapper = wishlistMapper;
        this.userService = userService;
        this.bookService = bookService;
    }

    public WishlistResponse addBookToWishlist(UUID bookId, String userEmail) {
        User user = userService.requireUserByEmail(userEmail);

        if (wishlistRepository.existsByUserIdAndBookId(user.getId(), bookId)) {
            throw new RuntimeException("Book is already in wishlist");
        }

        Book book = bookService.requireActiveBookEntity(bookId);

        WishlistItem wishlistItem = WishlistItem.builder()
                .user(user)
                .book(book)
                .build();

        WishlistItem savedWishlistItem = wishlistRepository.save(wishlistItem);
        return wishlistMapper.mapToResponse(savedWishlistItem);
    }

    public Page<WishlistResponse> getUserWishlist(String userEmail, Pageable pageable) {
        User user = userService.requireUserByEmail(userEmail);
        return wishlistRepository.findByUserIdOrderByAddedAtDesc(user.getId(), pageable)
                .map(wishlistMapper::mapToResponse);
    }

    public void removeBookFromWishlist(UUID bookId, String userEmail) {
        User user = userService.requireUserByEmail(userEmail);

        WishlistItem wishlistItem = wishlistRepository.findByUserIdAndBookId(user.getId(), bookId)
                .orElseThrow(() -> new RuntimeException("Book is not in wishlist"));

        wishlistRepository.delete(wishlistItem);
    }

    public boolean isBookInWishlist(UUID bookId, String userEmail) {
        User user = userService.requireUserByEmail(userEmail);
        return wishlistRepository.existsByUserIdAndBookId(user.getId(), bookId);
    }
}
