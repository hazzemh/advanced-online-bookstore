package com.example.bookstore.cart.controller;

import com.example.bookstore.cart.dto.AddToCartRequest;
import com.example.bookstore.cart.dto.CartResponse;
import com.example.bookstore.cart.dto.UpdateCartItemQuantityRequest;
import com.example.bookstore.cart.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getMyCart(Authentication authentication) {
        CartResponse response = cartService.getMyCart(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @RequestBody AddToCartRequest request,
            Authentication authentication
    ) {
        CartResponse response = cartService.addToCart(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            @PathVariable UUID itemId,
            @RequestBody UpdateCartItemQuantityRequest request,
            Authentication authentication
    ) {
        CartResponse response = cartService.updateItemQuantity(authentication.getName(), itemId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeCartItem(
            @PathVariable UUID itemId,
            Authentication authentication
    ) {
        CartResponse response = cartService.removeItem(authentication.getName(), itemId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        cartService.clearCart(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}

