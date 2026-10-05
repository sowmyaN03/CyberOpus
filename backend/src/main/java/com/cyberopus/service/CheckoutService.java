package com.cyberopus.service;

import com.cyberopus.dto.request.CheckoutRequest;
import com.cyberopus.dto.response.OrderItemResponse;
import com.cyberopus.dto.response.OrderResponse;
import com.cyberopus.entity.*;
import com.cyberopus.enums.OrderStatus;
import com.cyberopus.enums.PaymentStatus;
import com.cyberopus.exception.BadRequestException;
import com.cyberopus.exception.InsufficientStockException;
import com.cyberopus.repository.BookRepository;
import com.cyberopus.repository.CartRepository;
import com.cyberopus.repository.OrderRepository;
import com.cyberopus.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final SecurityUtils securityUtils;
    private final CartRepository cartRepository;
    private final AddressService addressService;
    private final RewardService rewardService;
    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.delivery.charge:4.99}")
    private String deliveryChargeStr;

    private BigDecimal getDeliveryCharge() {
        return new BigDecimal(deliveryChargeStr);
    }

    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        User user = securityUtils.getCurrentUser();

        // 1. Get cart entity and validate non-empty
        Cart cartEntity = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Cart not found"));
        if (cartEntity.getItems().isEmpty()) {
            throw new BadRequestException("Cannot checkout with an empty cart");
        }

        // 2. Validate address belongs to user
        Address address = addressService.findOwnedByIdForUser(request.getAddressId(), user.getId());

        // 3. Validate stock and compute subtotal at current live prices
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cartEntity.getItems()) {
            Book book = item.getBook();
            if (!book.getAvailable() || book.getStockQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(book.getTitle(), item.getQuantity(), book.getStockQuantity());
            }
            subtotal = subtotal.add(book.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // 4. Validate and compute reward discount
        int pointsToRedeem = request.getRewardPointsToRedeem() != null ? request.getRewardPointsToRedeem() : 0;
        BigDecimal rewardDiscount = rewardService.validateAndComputeDiscount(user.getId(), pointsToRedeem, subtotal);

        // 5. Calculate total
        BigDecimal deliveryCharge = getDeliveryCharge();
        BigDecimal total = subtotal.subtract(rewardDiscount).add(deliveryCharge);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        // 6. Build address snapshot (JSON)
        String addressSnapshot = buildAddressSnapshot(address);

        // 7. Create Order
        Order order = Order.builder()
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .user(user)
                .addressSnapshot(addressSnapshot)
                .subtotal(subtotal)
                .rewardDiscount(rewardDiscount)
                .deliveryCharge(deliveryCharge)
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .placedAt(Instant.now())
                .items(new ArrayList<>())
                .build();

        // 8. Add order items with price + title + author snapshots
        for (CartItem item : cartEntity.getItems()) {
            Book book = item.getBook();
            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .book(book)
                    .bookTitleSnapshot(book.getTitle())
                    .bookAuthorSnapshot(book.getAuthor())
                    .priceSnapshot(book.getPrice())
                    .quantity(item.getQuantity())
                    .build();
            order.getItems().add(oi);
        }
        orderRepository.save(order);

        // 9. Process simulated payment
        Payment payment = paymentService.processPayment(order, request.getPaymentMethod(),
                request.getMaskedCardNumber(), request.getCardHolderName());

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            // 10. Deduct stock
            for (CartItem item : cartEntity.getItems()) {
                Book book = item.getBook();
                book.setStockQuantity(book.getStockQuantity() - item.getQuantity());
                bookRepository.save(book);
            }
            // 11. Confirm order
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);

            // 12. Deduct redeemed points
            if (pointsToRedeem > 0) {
                rewardService.redeemPoints(user, order, pointsToRedeem);
            }
            // 13. Award earned points
            rewardService.earnPoints(user, order);

            // 14. Clear cart
            cartEntity.getItems().clear();
            cartRepository.save(cartEntity);
        }

        return toOrderResponse(order, payment);
    }

    private String buildAddressSnapshot(Address address) {
        try {
            Map<String, Object> snap = new LinkedHashMap<>();
            snap.put("fullName", address.getFullName());
            snap.put("line1", address.getLine1());
            snap.put("line2", address.getLine2());
            snap.put("city", address.getCity());
            snap.put("state", address.getState());
            snap.put("postalCode", address.getPostalCode());
            snap.put("country", address.getCountry());
            snap.put("phone", address.getPhone());
            return objectMapper.writeValueAsString(snap);
        } catch (Exception e) {
            return address.getFullName() + ", " + address.getLine1() + ", " + address.getCity();
        }
    }

    public OrderResponse toOrderResponse(Order order, Payment payment) {
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
                .payment(paymentService.toResponse(payment))
                .build();
    }
}
