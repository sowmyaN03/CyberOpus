package com.cyberopus.repository;

import com.cyberopus.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @Query("""
            SELECT oi FROM OrderItem oi
            JOIN oi.order o
            WHERE o.user.id = :userId
            """)
    List<OrderItem> findAllByUserId(@Param("userId") Long userId);
}
