package com.example.bookstore.order.service;

import com.example.bookstore.order.entity.OrderStatus;
import com.example.bookstore.order.repository.OrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderSignalService {

    private final OrderItemRepository orderItemRepository;

    public OrderSignalService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    public List<UUID> findPurchasedBookIdsByUserIdAndStatuses(UUID userId, Collection<OrderStatus> statuses) {
        return orderItemRepository.findPurchasedBookIdsByUserIdAndStatuses(userId, statuses);
    }

    public List<Object[]> findAllUserBookIdsByStatuses(Collection<OrderStatus> statuses) {
        return orderItemRepository.findAllUserBookIdsByStatuses(statuses);
    }
}

