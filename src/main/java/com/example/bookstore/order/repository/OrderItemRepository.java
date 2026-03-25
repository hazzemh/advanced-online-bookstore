package com.example.bookstore.order.repository;

import com.example.bookstore.order.entity.OrderItem;
import com.example.bookstore.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.time.LocalDateTime;
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

    @Query("""
            select oi.book.id,
                   oi.book.title,
                   oi.book.author,
                   oi.book.genre,
                   coalesce(sum(oi.quantity), 0),
                   coalesce(sum(oi.lineTotal), 0)
            from OrderItem oi
            where oi.order.createdAt >= :from and oi.order.createdAt < :to
              and oi.order.status in :statuses
            group by oi.book.id, oi.book.title, oi.book.author, oi.book.genre
            """)
    List<Object[]> sumByBookBetweenAndOrderStatusIn(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("statuses") Collection<OrderStatus> statuses
    );

    @Query("""
            select oi.book.genre,
                   coalesce(sum(oi.quantity), 0),
                   coalesce(sum(oi.lineTotal), 0)
            from OrderItem oi
            where oi.order.createdAt >= :from and oi.order.createdAt < :to
              and oi.order.status in :statuses
            group by oi.book.genre
            """)
    List<Object[]> sumByGenreBetweenAndOrderStatusIn(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("statuses") Collection<OrderStatus> statuses
    );
}

