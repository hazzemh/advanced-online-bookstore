package com.example.bookstore.order.service;

import com.example.bookstore.book.entity.Book;
import com.example.bookstore.book.repository.BookRepository;
import com.example.bookstore.order.dto.CreateOrderItemRequest;
import com.example.bookstore.order.dto.CreateOrderRequest;
import com.example.bookstore.order.dto.OrderResponse;
import com.example.bookstore.order.entity.Order;
import com.example.bookstore.order.entity.OrderItem;
import com.example.bookstore.order.entity.OrderStatus;
import com.example.bookstore.order.event.OrderCreatedEvent;
import com.example.bookstore.order.event.OrderStatusChangedEvent;
import com.example.bookstore.order.mapper.OrderMapper;
import com.example.bookstore.order.repository.OrderRepository;
import com.example.bookstore.user.entity.User;
import com.example.bookstore.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            BookRepository bookRepository,
            OrderMapper orderMapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.orderMapper = orderMapper;
        this.eventPublisher = eventPublisher;
    }

    public OrderResponse createOrder(String userEmail, CreateOrderRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Order items are required");
        }

        User user = getUserByEmail(userEmail);

        List<CreateOrderItemRequest> items = mergeDuplicateItems(request.items());
        Map<UUID, Book> bookMap = loadBooks(items);
        validateStock(items, bookMap);

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PROCESSING)
                .subtotal(BigDecimal.ZERO)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CreateOrderItemRequest itemRequest : items) {
            Book book = bookMap.get(itemRequest.bookId());
            BigDecimal unitPrice = book.getPrice();
            int quantity = itemRequest.quantity();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .book(book)
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .lineTotal(lineTotal)
                    .build();
            order.getItems().add(orderItem);

            subtotal = subtotal.add(lineTotal);

            book.setStockQuantity(book.getStockQuantity() - quantity);
            bookRepository.save(book);
        }
        order.setSubtotal(subtotal);

        Order saved = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderCreatedEvent(saved.getId(), user.getEmail(), saved.getSubtotal()));
        return orderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(orderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(String userEmail, UUID orderId) {
        User user = getUserByEmail(userEmail);
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return orderMapper.toResponse(order);
    }

    public OrderResponse cancelMyOrder(String userEmail, UUID orderId) {
        User user = getUserByEmail(userEmail);
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELED) {
            return orderMapper.toResponse(order);
        }
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot cancel an order that is already shipped or delivered");
        }

        restoreStock(order);
        order.setStatus(OrderStatus.CANCELED);
        order.setCanceledAt(LocalDateTime.now());
        order.setStatusUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable, OrderStatus status) {
        if (status == null) {
            return orderRepository.findAll(pageable).map(orderMapper::toResponse);
        }
        return orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(orderMapper::toResponse);
    }

    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status is required");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus current = order.getStatus();
        if (current == newStatus) {
            return orderMapper.toResponse(order);
        }
        if (current == OrderStatus.CANCELED) {
            throw new IllegalArgumentException("Cannot change status of a canceled order");
        }
        if (current == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot change status of a delivered order");
        }

        if (newStatus == OrderStatus.CANCELED) {
            restoreStock(order);
            order.setCanceledAt(LocalDateTime.now());
        }
        if (newStatus == OrderStatus.SHIPPED) {
            order.setShippedAt(LocalDateTime.now());
        }
        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        order.setStatus(newStatus);
        order.setStatusUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        String userEmail = saved.getUser() == null ? null : saved.getUser().getEmail();
        eventPublisher.publishEvent(new OrderStatusChangedEvent(saved.getId(), userEmail, current, newStatus));
        return orderMapper.toResponse(saved);
    }

    private User getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Unauthenticated");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private List<CreateOrderItemRequest> mergeDuplicateItems(List<CreateOrderItemRequest> items) {
        Map<UUID, Integer> quantities = new LinkedHashMap<>();
        for (CreateOrderItemRequest item : items) {
            if (item == null || item.bookId() == null) {
                throw new IllegalArgumentException("bookId is required");
            }
            int qty = item.quantity() == null ? 0 : item.quantity();
            if (qty <= 0) {
                throw new IllegalArgumentException("Quantity must be at least 1");
            }
            quantities.merge(item.bookId(), qty, Integer::sum);
        }
        return quantities.entrySet().stream()
                .map(e -> new CreateOrderItemRequest(e.getKey(), e.getValue()))
                .toList();
    }

    private Map<UUID, Book> loadBooks(List<CreateOrderItemRequest> items) {
        Map<UUID, Book> bookMap = new HashMap<>();
        for (CreateOrderItemRequest item : items) {
            Book book = bookRepository.findById(item.bookId())
                    .orElseThrow(() -> new RuntimeException("Book not found"));
            if (!Boolean.TRUE.equals(book.getIsActive())) {
                throw new RuntimeException("Book is not available");
            }
            bookMap.put(book.getId(), book);
        }
        return bookMap;
    }

    private void validateStock(List<CreateOrderItemRequest> items, Map<UUID, Book> bookMap) {
        for (CreateOrderItemRequest item : items) {
            Book book = bookMap.get(item.bookId());
            int requested = item.quantity();
            if (book.getStockQuantity() == null || book.getStockQuantity() <= 0) {
                throw new IllegalArgumentException("Book is out of stock");
            }
            if (requested > book.getStockQuantity()) {
                throw new IllegalArgumentException("Requested quantity exceeds available stock");
            }
        }
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Book book = bookRepository.findById(item.getBook().getId())
                    .orElse(null);
            if (book == null) {
                continue;
            }
            Integer current = book.getStockQuantity() == null ? 0 : book.getStockQuantity();
            book.setStockQuantity(current + item.getQuantity());
            bookRepository.save(book);
        }
    }
}

