package com.cyberopus.dto.response;

import com.cyberopus.enums.PaymentMethod;
import com.cyberopus.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private PaymentMethod paymentMethod;
    private String maskedCardNumber;
    private String cardHolderName;
    private String transactionRef;
    private BigDecimal amount;
    private PaymentStatus status;
    private Instant initiatedAt;
    private Instant completedAt;
}
