package com.example.bookstore.order.service;

import com.example.bookstore.book.entity.Book;
import com.example.bookstore.book.repository.BookRepository;
import com.example.bookstore.order.dto.CreateOrderItemRequest;
import com.example.bookstore.order.dto.CreateOrderRequest;
import com.example.bookstore.order.entity.OrderStatus;
import com.example.bookstore.order.repository.OrderRepository;
import com.example.bookstore.user.entity.Role;
import com.example.bookstore.user.entity.User;
import com.example.bookstore.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User user;
    private Book bookA;
    private Book bookB;
    private String suffix;

    @BeforeEach
    void setUp() {
        suffix = java.util.UUID.randomUUID().toString().substring(0, 8);

        user = userRepository.save(User.builder()
                .email("order.user+" + suffix + "@test.com")
                .password("password")
                .firstName("Order")
                .lastName("User")
                .role(Role.USER)
                .enabled(true)
                .build());

        bookA = bookRepository.save(Book.builder()
                .title("Book A")
                .author("Author A")
                .description("Desc A")
                .price(new BigDecimal("15.00"))
                .stockQuantity(10)
                .isbn("ISBN-A-" + suffix)
                .genre("Fiction")
                .publicationYear(2022)
                .pages(100)
                .publisher("Pub A")
                .isActive(true)
                .build());

        bookB = bookRepository.save(Book.builder()
                .title("Book B")
                .author("Author B")
                .description("Desc B")
                .price(new BigDecimal("20.00"))
                .stockQuantity(3)
                .isbn("ISBN-B-" + suffix)
                .genre("Sci-Fi")
                .publicationYear(2021)
                .pages(200)
                .publisher("Pub B")
                .isActive(true)
                .build());
    }

    @Test
    void testCreateOrder_DecrementsStockAndReturnsResponse() {
        var request = new CreateOrderRequest(List.of(
                new CreateOrderItemRequest(bookA.getId(), 2),
                new CreateOrderItemRequest(bookB.getId(), 1)
        ));

        var response = orderService.createOrder(user.getEmail(), request);

        assertNotNull(response.orderId());
        assertEquals(OrderStatus.PROCESSING, response.status());
        assertEquals(3, response.totalQuantity());
        assertEquals(0, response.subtotal().compareTo(new BigDecimal("50.00")));

        Book updatedA = bookRepository.findById(bookA.getId()).orElseThrow();
        Book updatedB = bookRepository.findById(bookB.getId()).orElseThrow();
        assertEquals(8, updatedA.getStockQuantity());
        assertEquals(2, updatedB.getStockQuantity());
    }

    @Test
    void testCreateOrder_MergesDuplicateItems() {
        var request = new CreateOrderRequest(List.of(
                new CreateOrderItemRequest(bookA.getId(), 1),
                new CreateOrderItemRequest(bookA.getId(), 2)
        ));

        var response = orderService.createOrder(user.getEmail(), request);

        assertEquals(1, response.items().size());
        assertEquals(3, response.totalQuantity());
        assertEquals(0, response.subtotal().compareTo(new BigDecimal("45.00")));

        Book updatedA = bookRepository.findById(bookA.getId()).orElseThrow();
        assertEquals(7, updatedA.getStockQuantity());
    }

    @Test
    void testCancelOrder_RestoresStock() {
        var request = new CreateOrderRequest(List.of(
                new CreateOrderItemRequest(bookB.getId(), 2)
        ));
        var created = orderService.createOrder(user.getEmail(), request);

        var canceled = orderService.cancelMyOrder(user.getEmail(), created.orderId());

        assertEquals(OrderStatus.CANCELED, canceled.status());

        Book updatedB = bookRepository.findById(bookB.getId()).orElseThrow();
        assertEquals(3, updatedB.getStockQuantity());
    }

    @Test
    void testGetMyOrders_ReturnsPagedHistory() {
        orderService.createOrder(user.getEmail(), new CreateOrderRequest(List.of(
                new CreateOrderItemRequest(bookA.getId(), 1)
        )));
        orderService.createOrder(user.getEmail(), new CreateOrderRequest(List.of(
                new CreateOrderItemRequest(bookA.getId(), 1)
        )));

        var page = orderService.getMyOrders(user.getEmail(), PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
    }

    @Test
    void testCreateOrder_ExceedsStockThrows() {
        var request = new CreateOrderRequest(List.of(
                new CreateOrderItemRequest(bookB.getId(), 4)
        ));

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(user.getEmail(), request));
    }
}
