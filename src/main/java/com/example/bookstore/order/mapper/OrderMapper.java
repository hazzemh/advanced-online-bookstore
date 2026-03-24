package com.example.bookstore.order.mapper;

import com.example.bookstore.order.dto.OrderItemResponse;
import com.example.bookstore.order.dto.OrderResponse;
import com.example.bookstore.order.entity.Order;
import com.example.bookstore.order.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .sorted(Comparator.comparing(OrderItem::getCreatedAt))
                .map(this::toItemResponse)
                .toList();

        int totalQuantity = items.stream()
                .map(OrderItemResponse::quantity)
                .filter(q -> q != null)
                .mapToInt(Integer::intValue)
                .sum();

        BigDecimal subtotal = order.getSubtotal() == null ? BigDecimal.ZERO : order.getSubtotal();

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                items,
                totalQuantity,
                subtotal,
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getStatusUpdatedAt(),
                order.getShippedAt(),
                order.getDeliveredAt(),
                order.getCanceledAt()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getBook().getId(),
                item.getBook().getTitle(),
                item.getBook().getAuthor(),
                item.getBook().getImageUrl(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getLineTotal()
        );
    }
}

