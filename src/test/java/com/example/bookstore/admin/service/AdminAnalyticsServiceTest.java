package com.example.bookstore.admin.service;

import com.example.bookstore.admin.dto.TopMetric;
import com.example.bookstore.book.entity.Book;
import com.example.bookstore.book.repository.BookRepository;
import com.example.bookstore.cart.repository.CartItemRepository;
import com.example.bookstore.cart.repository.CartRepository;
import com.example.bookstore.order.entity.Order;
import com.example.bookstore.order.entity.OrderItem;
import com.example.bookstore.order.entity.OrderStatus;
import com.example.bookstore.order.repository.OrderItemRepository;
import com.example.bookstore.order.repository.OrderRepository;
import com.example.bookstore.review.repository.ReviewRepository;
import com.example.bookstore.user.entity.Role;
import com.example.bookstore.user.entity.User;
import com.example.bookstore.user.repository.UserRepository;
import com.example.bookstore.wishlist.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AdminAnalyticsServiceTest {

    @Autowired
    private AdminAnalyticsService analyticsService;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        wishlistRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void topGenres_shouldRankByRevenue() {
        User u1 = userRepository.save(User.builder()
                .email("u1@example.com")
                .password("x")
                .role(Role.USER)
                .enabled(true)
                .build());

        Book fiction = bookRepository.save(Book.builder()
                .title("Fiction Book")
                .author("A")
                .description("d")
                .price(new BigDecimal("20.00"))
                .stockQuantity(10)
                .pages(100)
                .publisher("P")
                .genre("Fiction")
                .build());

        Book science = bookRepository.save(Book.builder()
                .title("Science Book")
                .author("B")
                .description("d")
                .price(new BigDecimal("10.00"))
                .stockQuantity(10)
                .pages(100)
                .publisher("P")
                .genre("Science")
                .build());

        Order order = Order.builder()
                .user(u1)
                .status(OrderStatus.PAID)
                .subtotal(new BigDecimal("30.00"))
                .build();
        order.getItems().add(OrderItem.builder()
                .order(order)
                .book(fiction)
                .quantity(1)
                .unitPrice(fiction.getPrice())
                .lineTotal(fiction.getPrice())
                .build());
        order.getItems().add(OrderItem.builder()
                .order(order)
                .book(science)
                .quantity(1)
                .unitPrice(science.getPrice())
                .lineTotal(science.getPrice())
                .build());
        orderRepository.save(order);

        var top = analyticsService.getTopGenres(30, 10, TopMetric.REVENUE);
        assertFalse(top.isEmpty());
        assertEquals("Fiction", top.get(0).genre());
        assertTrue(top.get(0).revenue().compareTo(new BigDecimal("20.00")) >= 0);
    }
}

