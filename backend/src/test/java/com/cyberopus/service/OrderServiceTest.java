package com.cyberopus.service;

import com.cyberopus.entity.Order;
import com.cyberopus.entity.User;
import com.cyberopus.enums.OrderStatus;
import com.cyberopus.exception.ForbiddenException;
import com.cyberopus.exception.OrderCancellationException;
import com.cyberopus.exception.ResourceNotFoundException;
import com.cyberopus.repository.BookRepository;
import com.cyberopus.repository.OrderRepository;
import com.cyberopus.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock BookRepository bookRepository;
    @Mock RewardService rewardService;
    @Mock PaymentService paymentService;
    @Mock SecurityUtils securityUtils;

    @InjectMocks OrderService orderService;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = User.builder().id(1L).email("alice@demo.com").build();
        bob   = User.builder().id(2L).email("bob@demo.com").build();
    }

    private Order buildOrder(OrderStatus status, Instant placedAt, User user) {
        return Order.builder()
                .id(10L)
                .orderNumber("ORD-TEST01")
                .user(user)
                .status(status)
                .subtotal(BigDecimal.TEN)
                .rewardDiscount(BigDecimal.ZERO)
                .deliveryCharge(new BigDecimal("4.99"))
                .totalAmount(new BigDecimal("14.99"))
                .placedAt(placedAt)
                .addressSnapshot("{}")
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void cancelOrder_WithinWindow_Succeeds() {
        Order order = buildOrder(OrderStatus.CONFIRMED, Instant.now().minusSeconds(3600), alice);
        when(securityUtils.getCurrentUserId()).thenReturn(alice.getId());
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        doNothing().when(rewardService).refundPoints(any(), any());
        when(paymentService.refundPayment(any())).thenReturn(null);

        orderService.cancelOrder(10L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_AfterWindow_ThrowsOrderCancellationException() {
        Instant tooLate = Instant.now().minusSeconds(49 * 3600L); // 49 hours ago
        Order order = buildOrder(OrderStatus.CONFIRMED, tooLate, alice);
        when(securityUtils.getCurrentUserId()).thenReturn(alice.getId());
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(10L))
                .isInstanceOf(OrderCancellationException.class)
                .hasMessageContaining("48 hours");
    }

    @Test
    void cancelOrder_WrongStatus_ThrowsOrderCancellationException() {
        Order order = buildOrder(OrderStatus.DELIVERED, Instant.now().minusSeconds(3600), alice);
        when(securityUtils.getCurrentUserId()).thenReturn(alice.getId());
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(10L))
                .isInstanceOf(OrderCancellationException.class);
    }

    @Test
    void cancelOrder_OtherUserOrder_ThrowsForbiddenException() {
        Order order = buildOrder(OrderStatus.CONFIRMED, Instant.now().minusSeconds(3600), alice);
        when(securityUtils.getCurrentUserId()).thenReturn(bob.getId()); // bob tries to cancel alice's order
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(10L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getOrderById_OtherUser_ThrowsForbiddenException() {
        Order order = buildOrder(OrderStatus.DELIVERED, Instant.now().minusSeconds(3600), alice);
        when(securityUtils.getCurrentUserId()).thenReturn(bob.getId());
        when(orderRepository.findByIdWithItems(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderById(10L))
                .isInstanceOf(ForbiddenException.class);
    }
}
