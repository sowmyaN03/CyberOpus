package com.cyberopus.dto.response;

import com.cyberopus.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal rewardDiscount;
    private BigDecimal deliveryCharge;
    private BigDecimal totalAmount;
    private String addressSnapshot;
    private Instant placedAt;
    private List<OrderItemResponse> items;
    private PaymentResponse payment;
}
