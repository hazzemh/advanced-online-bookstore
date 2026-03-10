package com.example.bookstore.wishlist.controller;

import com.example.bookstore.wishlist.dto.WishlistResponse;
import com.example.bookstore.wishlist.service.WishlistService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping("/books/{bookId}")
    public ResponseEntity<WishlistResponse> addBookToWishlist(
            @PathVariable UUID bookId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        WishlistResponse response = wishlistService.addBookToWishlist(bookId, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<WishlistResponse>> getUserWishlist(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String userEmail = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<WishlistResponse> response = wishlistService.getUserWishlist(userEmail, pageable);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/books/{bookId}")
    public ResponseEntity<Void> removeBookFromWishlist(
            @PathVariable UUID bookId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        wishlistService.removeBookFromWishlist(bookId, userEmail);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/books/{bookId}/exists")
    public ResponseEntity<Boolean> isBookInWishlist(
            @PathVariable UUID bookId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        boolean exists = wishlistService.isBookInWishlist(bookId, userEmail);
        return ResponseEntity.ok(exists);
    }
}
