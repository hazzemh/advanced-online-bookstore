package com.example.bookstore.order.service;

import com.example.bookstore.order.entity.OrderStatus;
import com.example.bookstore.order.repository.OrderItemRepository;
import com.example.bookstore.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderAnalyticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderAnalyticsService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public long countAllOrders() {
        return orderRepository.count();
    }

    public long countByStatusIn(Collection<OrderStatus> statuses) {
        return orderRepository.countByStatusIn(statuses);
    }

    public BigDecimal sumSubtotalByStatusIn(Collection<OrderStatus> statuses) {
        return orderRepository.sumSubtotalByStatusIn(statuses);
    }

    public List<Object[]> findCreatedAtAndSubtotalBetweenAndStatusIn(LocalDateTime from, LocalDateTime to, Collection<OrderStatus> statuses) {
        return orderRepository.findCreatedAtAndSubtotalBetweenAndStatusIn(from, to, statuses);
    }

    public List<UUID> findDistinctUserIdsBetween(LocalDateTime from, LocalDateTime to) {
        return orderRepository.findDistinctUserIdsBetween(from, to);
    }

    public List<Object[]> findUserIdsAndCreatedAtBetween(LocalDateTime from, LocalDateTime to) {
        return orderRepository.findUserIdsAndCreatedAtBetween(from, to);
    }

    public List<Object[]> sumByBookBetweenAndOrderStatusIn(LocalDateTime from, LocalDateTime to, Collection<OrderStatus> statuses) {
        return orderItemRepository.sumByBookBetweenAndOrderStatusIn(from, to, statuses);
    }

    public List<Object[]> sumByGenreBetweenAndOrderStatusIn(LocalDateTime from, LocalDateTime to, Collection<OrderStatus> statuses) {
        return orderItemRepository.sumByGenreBetweenAndOrderStatusIn(from, to, statuses);
    }
}

