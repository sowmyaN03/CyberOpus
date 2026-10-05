package com.cyberopus.repository;

import com.cyberopus.entity.Order;
import com.cyberopus.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserIdOrderByPlacedAtDesc(Long userId, Pageable pageable);
    Optional<Order> findByIdAndUserId(Long id, Long userId);
    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.book WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    List<Order> findByUserIdAndStatusIn(Long userId, List<OrderStatus> statuses);
}
