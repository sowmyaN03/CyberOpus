package com.cyberopus.service;

import com.cyberopus.dto.response.OrderResponse;
import com.cyberopus.dto.response.OrderItemResponse;
import com.cyberopus.entity.Order;
import com.cyberopus.enums.OrderStatus;
import com.cyberopus.enums.PaymentStatus;
import com.cyberopus.exception.ForbiddenException;
import com.cyberopus.exception.OrderCancellationException;
import com.cyberopus.exception.ResourceNotFoundException;
import com.cyberopus.repository.BookRepository;
import com.cyberopus.repository.OrderRepository;
import com.cyberopus.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Set<OrderStatus> CANCELLABLE_STATUSES = Set.of(OrderStatus.PENDING, OrderStatus.CONFIRMED);
    private static final long CANCELLATION_WINDOW_HOURS = 48;

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final RewardService rewardService;
    private final PaymentService paymentService;
    private final SecurityUtils securityUtils;

    public Page<OrderResponse> getMyOrders(Pageable pageable) {
        Long userId = securityUtils.getCurrentUserId();
        return orderRepository.findByUserIdOrderByPlacedAtDesc(userId, pageable)
                .map(o -> toOrderResponse(o));
    }

    public OrderResponse getOrderById(Long id) {
        Long userId = securityUtils.getCurrentUserId();
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        if (!order.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not own this order");
        }
        return toOrderResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Long userId = securityUtils.getCurrentUserId();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        // Ownership check
        if (!order.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not own this order");
        }

        // Status check
        if (!CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new OrderCancellationException(
                    "Order cannot be cancelled – current status: " + order.getStatus());
        }

        // 48-hour window check (server-side, not trusted from client)
        long hoursElapsed = Duration.between(order.getPlacedAt(), Instant.now()).toHours();
        if (hoursElapsed >= CANCELLATION_WINDOW_HOURS) {
            throw new OrderCancellationException(
                    "Cancellation window has expired. Orders can only be cancelled within 48 hours of placement.");
        }

        // Restore stock
        for (var item : order.getItems()) {
            var book = item.getBook();
            book.setStockQuantity(book.getStockQuantity() + item.getQuantity());
            bookRepository.save(book);
        }

        // Refund payment
        paymentService.refundPayment(order.getId());

        // Refund reward points
        rewardService.refundPoints(order.getUser(), order);

        // Update order status
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        return toOrderResponse(order);
    }

    public OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(oi -> OrderItemResponse.builder()
                        .id(oi.getId())
                        .bookId(oi.getBook().getId())
                        .bookTitleSnapshot(oi.getBookTitleSnapshot())
                        .bookAuthorSnapshot(oi.getBookAuthorSnapshot())
                        .priceSnapshot(oi.getPriceSnapshot())
                        .quantity(oi.getQuantity())
                        .lineTotal(oi.getPriceSnapshot().multiply(BigDecimal.valueOf(oi.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .rewardDiscount(order.getRewardDiscount())
                .deliveryCharge(order.getDeliveryCharge())
                .totalAmount(order.getTotalAmount())
                .addressSnapshot(order.getAddressSnapshot())
                .placedAt(order.getPlacedAt())
                .items(items)
                .payment(paymentService.toResponse(order.getPayment()))
                .build();
    }
}
