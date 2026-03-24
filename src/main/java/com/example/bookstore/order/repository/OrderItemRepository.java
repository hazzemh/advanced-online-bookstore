package com.example.bookstore.order.repository;

import com.example.bookstore.order.entity.OrderItem;
import com.example.bookstore.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    @Query("""
            select distinct oi.book.id
            from OrderItem oi
            where oi.order.user.id = :userId
              and oi.order.status in :statuses
            """)
    List<UUID> findPurchasedBookIdsByUserIdAndStatuses(
            @Param("userId") UUID userId,
            @Param("statuses") Collection<OrderStatus> statuses
    );

    @Query("""
            select oi.order.user.id, oi.book.id
            from OrderItem oi
            where oi.order.status in :statuses
            """)
    List<Object[]> findAllUserBookIdsByStatuses(@Param("statuses") Collection<OrderStatus> statuses);
}

