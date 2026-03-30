package com.example.bookstore.cart.service;

import com.example.bookstore.book.entity.Book;
import com.example.bookstore.book.service.BookService;
import com.example.bookstore.cart.dto.AddToCartRequest;
import com.example.bookstore.cart.dto.CartResponse;
import com.example.bookstore.cart.dto.UpdateCartItemQuantityRequest;
import com.example.bookstore.cart.entity.Cart;
import com.example.bookstore.cart.entity.CartItem;
import com.example.bookstore.cart.mapper.CartMapper;
import com.example.bookstore.cart.repository.CartItemRepository;
import com.example.bookstore.cart.repository.CartRepository;
import com.example.bookstore.user.entity.User;
import com.example.bookstore.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final BookService bookService;
    private final CartMapper cartMapper;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            UserService userService,
            BookService bookService,
            CartMapper cartMapper
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userService = userService;
        this.bookService = bookService;
        this.cartMapper = cartMapper;
    }

    public CartResponse getMyCart(String userEmail) {
        User user = userService.requireUserByEmail(userEmail);
        return cartRepository.findByUserId(user.getId())
                .map(cartMapper::toResponse)
                .orElseGet(() -> cartMapper.empty(null));
    }

    public CartResponse addToCart(String userEmail, AddToCartRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.bookId() == null) {
            throw new IllegalArgumentException("bookId is required");
        }
        int quantity = request.quantity() == null ? 0 : request.quantity();
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        User user = userService.requireUserByEmail(userEmail);
        Book book = bookService.requireActiveBookEntity(request.bookId());

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));

        CartItem item = cartItemRepository.findByCartIdAndBookId(cart.getId(), book.getId())
                .orElse(null);

        if (item == null) {
            validateStock(book, quantity);
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .book(book)
                    .quantity(quantity)
                    .unitPrice(book.getPrice())
                    .build();
            cart.getItems().add(newItem);
            cartRepository.save(cart);
            return cartMapper.toResponse(cart);
        }

        int newQuantity = item.getQuantity() + quantity;
        validateStock(book, newQuantity);
        item.setQuantity(newQuantity);
        item.setUnitPrice(book.getPrice());
        cartItemRepository.save(item);

        Cart refreshed = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        return cartMapper.toResponse(refreshed);
    }

    public CartResponse updateItemQuantity(String userEmail, UUID itemId, UpdateCartItemQuantityRequest request) {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId is required");
        }
        if (request == null || request.quantity() == null) {
            throw new IllegalArgumentException("Quantity is required");
        }

        User user = userService.requireUserByEmail(userEmail);
        CartItem item = cartItemRepository.findByIdAndCartUserId(itemId, user.getId())
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        int quantity = request.quantity();
        if (quantity <= 0) {
            Cart cart = item.getCart();
            // Keep the in-memory collection consistent with the DB to avoid returning stale items.
            cart.getItems().removeIf(ci -> ci.getId().equals(item.getId()));
            cartRepository.save(cart);
            return cartMapper.toResponse(cart);
        }

        Book book = bookService.requireActiveBookEntity(item.getBook().getId());
        validateStock(book, quantity);
        item.setQuantity(quantity);
        item.setUnitPrice(book.getPrice());
        cartItemRepository.save(item);

        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        return cartMapper.toResponse(cart);
    }

    public CartResponse removeItem(String userEmail, UUID itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId is required");
        }
        User user = userService.requireUserByEmail(userEmail);
        CartItem item = cartItemRepository.findByIdAndCartUserId(itemId, user.getId())
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        Cart cart = item.getCart();
        cart.getItems().removeIf(ci -> ci.getId().equals(item.getId()));
        cartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    public void clearCart(String userEmail) {
        User user = userService.requireUserByEmail(userEmail);
        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart == null) {
            return;
        }
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private void validateStock(Book book, int quantity) {
        if (book.getStockQuantity() == null || book.getStockQuantity() <= 0) {
            throw new IllegalArgumentException("Book is out of stock");
        }
        if (quantity > book.getStockQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock");
        }
    }
}

