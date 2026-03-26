package com.example.bookstore.order.repository;

import com.example.bookstore.order.entity.Order;
import com.example.bookstore.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Optional<Order> findByIdAndUserId(UUID orderId, UUID userId);

    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    long countByStatusIn(Collection<OrderStatus> statuses);

    @Query("select coalesce(sum(o.subtotal), 0) from Order o where o.status in :statuses")
    BigDecimal sumSubtotalByStatusIn(@Param("statuses") Collection<OrderStatus> statuses);

    @Query("""
            select o.createdAt, o.subtotal
            from Order o
            where o.createdAt >= :from and o.createdAt < :to
              and o.status in :statuses
            """)
    List<Object[]> findCreatedAtAndSubtotalBetweenAndStatusIn(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("statuses") Collection<OrderStatus> statuses
    );

    @Query("select distinct o.user.id from Order o where o.createdAt >= :from and o.createdAt < :to")
    List<UUID> findDistinctUserIdsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            select o.user.id, o.createdAt
            from Order o
            where o.createdAt >= :from and o.createdAt < :to
            """)
    List<Object[]> findUserIdsAndCreatedAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}

