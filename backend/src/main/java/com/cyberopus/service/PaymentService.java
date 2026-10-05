package com.cyberopus.service;

import com.cyberopus.dto.response.PaymentResponse;
import com.cyberopus.entity.Order;
import com.cyberopus.entity.Payment;
import com.cyberopus.enums.PaymentMethod;
import com.cyberopus.enums.PaymentStatus;
import com.cyberopus.exception.ResourceNotFoundException;
import com.cyberopus.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * Simulates a payment for the given order.
     *
     * Test payment rules:
     *   - Card holder name "FAIL_TEST" → always returns FAILED (for test purposes)
     *   - All other valid inputs → returns SUCCESS
     *
     * NEVER stores full card number or CVV.
     */
    @Transactional
    public Payment processPayment(Order order, PaymentMethod method,
                                   String maskedCardNumber, String cardHolderName) {
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(method)
                .maskedCardNumber(maskedCardNumber)
                .cardHolderName(cardHolderName)
                .transactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase())
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .initiatedAt(Instant.now())
                .build();

        // Deterministic simulation: "FAIL_TEST" card holder always fails
        boolean success = !"FAIL_TEST".equalsIgnoreCase(cardHolderName);
        payment.setStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        payment.setCompletedAt(Instant.now());

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment refundPayment(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setCompletedAt(Instant.now());
        return paymentRepository.save(payment);
    }

    public PaymentResponse getByOrderId(Long orderId, Long currentUserId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));
        if (!payment.getOrder().getUser().getId().equals(currentUserId)) {
            throw new com.cyberopus.exception.ForbiddenException("You do not own this payment");
        }
        return toResponse(payment);
    }

    public PaymentResponse toResponse(Payment p) {
        if (p == null) return null;
        return PaymentResponse.builder()
                .id(p.getId())
                .paymentMethod(p.getPaymentMethod())
                .maskedCardNumber(p.getMaskedCardNumber())
                .cardHolderName(p.getCardHolderName())
                .transactionRef(p.getTransactionRef())
                .amount(p.getAmount())
                .status(p.getStatus())
                .initiatedAt(p.getInitiatedAt())
                .completedAt(p.getCompletedAt())
                .build();
    }
}
