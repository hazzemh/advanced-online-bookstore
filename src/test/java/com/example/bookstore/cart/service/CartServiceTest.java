package com.example.bookstore.cart.service;

import com.example.bookstore.book.entity.Book;
import com.example.bookstore.book.repository.BookRepository;
import com.example.bookstore.cart.dto.AddToCartRequest;
import com.example.bookstore.cart.dto.CartResponse;
import com.example.bookstore.cart.dto.UpdateCartItemQuantityRequest;
import com.example.bookstore.cart.repository.CartItemRepository;
import com.example.bookstore.cart.repository.CartRepository;
import com.example.bookstore.user.entity.Role;
import com.example.bookstore.user.entity.User;
import com.example.bookstore.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("cart.user@example.com")
                .password("password")
                .firstName("Cart")
                .lastName("User")
                .role(Role.USER)
                .enabled(true)
                .build());

        book = bookRepository.save(Book.builder()
                .title("Test Book")
                .author("Test Author")
                .description("Test Description")
                .price(new BigDecimal("10.00"))
                .stockQuantity(5)
                .isbn("TEST-ISBN-123")
                .genre("Fiction")
                .publicationYear(2020)
                .pages(200)
                .publisher("Test Publisher")
                .isActive(true)
                .build());
    }

    @Test
    void testAddToCart_CreatesCartAndItem() {
        CartResponse response = cartService.addToCart(user.getEmail(), new AddToCartRequest(book.getId(), 2));

        assertNotNull(response.cartId());
        assertEquals(1, response.items().size());
        assertEquals(2, response.totalQuantity());
        assertEquals(0, response.subtotal().compareTo(new BigDecimal("20.00")));
    }

    @Test
    void testAddToCart_SameBookIncrementsQuantity() {
        cartService.addToCart(user.getEmail(), new AddToCartRequest(book.getId(), 2));
        CartResponse response = cartService.addToCart(user.getEmail(), new AddToCartRequest(book.getId(), 1));

        assertEquals(1, response.items().size());
        assertEquals(3, response.totalQuantity());
        assertEquals(0, response.subtotal().compareTo(new BigDecimal("30.00")));
    }

    @Test
    void testUpdateItemQuantity_ZeroRemovesItem() {
        CartResponse added = cartService.addToCart(user.getEmail(), new AddToCartRequest(book.getId(), 2));
        var itemId = added.items().get(0).itemId();

        CartResponse response = cartService.updateItemQuantity(
                user.getEmail(),
                itemId,
                new UpdateCartItemQuantityRequest(0)
        );

        assertEquals(0, response.items().size());
        assertEquals(0, response.totalQuantity());
        assertEquals(0, response.subtotal().compareTo(BigDecimal.ZERO));
    }

    @Test
    void testAddToCart_ExceedsStockThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            cartService.addToCart(user.getEmail(), new AddToCartRequest(book.getId(), 6));
        });
    }
}

