package com.example.bookstore.order.service;

import com.example.bookstore.integration.order.OrderPaymentQuery;
import com.example.bookstore.integration.order.OrderPaymentView;
import com.example.bookstore.order.entity.Order;
import com.example.bookstore.order.repository.OrderRepository;
import com.example.bookstore.user.entity.User;
import com.example.bookstore.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderPaymentQueryService implements OrderPaymentQuery {

    private final OrderRepository orderRepository;
    private final UserService userService;

    public OrderPaymentQueryService(OrderRepository orderRepository, UserService userService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
    }

    @Override
    public OrderPaymentView getMyOrderPaymentView(UUID orderId, String userEmail) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId is required");
        }
        if (userEmail == null || userEmail.isBlank()) {
            throw new IllegalArgumentException("userEmail is required");
        }

        User user = userService.requireUserByEmail(userEmail);
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return new OrderPaymentView(
                order.getId(),
                user.getEmail(),
                order.getStatus() == null ? null : order.getStatus().name(),
                order.getSubtotal()
        );
    }
}

