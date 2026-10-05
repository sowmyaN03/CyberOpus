package com.cyberopus.service;

import com.cyberopus.dto.response.RewardResponse;
import com.cyberopus.dto.response.RewardTransactionResponse;
import com.cyberopus.entity.Order;
import com.cyberopus.entity.RewardPointTransaction;
import com.cyberopus.entity.RewardPoints;
import com.cyberopus.entity.User;
import com.cyberopus.enums.RewardTransactionType;
import com.cyberopus.exception.InsufficientRewardPointsException;
import com.cyberopus.exception.ResourceNotFoundException;
import com.cyberopus.repository.RewardPointTransactionRepository;
import com.cyberopus.repository.RewardPointsRepository;
import com.cyberopus.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardPointsRepository rewardPointsRepository;
    private final RewardPointTransactionRepository transactionRepository;
    private final SecurityUtils securityUtils;

    @Value("${app.rewards.earn-rate:10}")
    private int earnRate;

    @Value("${app.rewards.redeem-rate:100}")
    private int redeemRate;

    @Value("${app.rewards.max-redeem-pct:0.20}")
    private double maxRedeemPct;

    public RewardResponse getMyRewards() {
        Long userId = securityUtils.getCurrentUserId();
        RewardPoints rp = getOrCreate(userId);
        List<RewardTransactionResponse> transactions = transactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
        return RewardResponse.builder()
                .balance(rp.getBalance())
                .transactions(transactions)
                .build();
    }

    /** Validate redemption request. Returns BigDecimal discount amount. */
    public BigDecimal validateAndComputeDiscount(Long userId, int pointsToRedeem, BigDecimal subtotal) {
        if (pointsToRedeem <= 0) return BigDecimal.ZERO;

        RewardPoints rp = getOrCreate(userId);
        if (pointsToRedeem > rp.getBalance()) {
            throw new InsufficientRewardPointsException(pointsToRedeem, rp.getBalance());
        }

        // Cap at maxRedeemPct of subtotal
        BigDecimal maxDiscount = subtotal.multiply(BigDecimal.valueOf(maxRedeemPct))
                .setScale(2, RoundingMode.DOWN);
        BigDecimal requestedDiscount = BigDecimal.valueOf((double) pointsToRedeem / redeemRate)
                .setScale(2, RoundingMode.DOWN);

        if (requestedDiscount.compareTo(maxDiscount) > 0) {
            throw new InsufficientRewardPointsException(pointsToRedeem,
                    maxDiscount.multiply(BigDecimal.valueOf(redeemRate)).intValue());
        }
        return requestedDiscount;
    }

    @Transactional
    public void redeemPoints(User user, Order order, int pointsToRedeem) {
        if (pointsToRedeem <= 0) return;
        RewardPoints rp = getOrCreate(user.getId());
        rp.deductPoints(pointsToRedeem);
        rewardPointsRepository.save(rp);

        RewardPointTransaction tx = RewardPointTransaction.builder()
                .user(user)
                .order(order)
                .type(RewardTransactionType.REDEEM)
                .points(-pointsToRedeem)
                .description("Redeemed at checkout for order " + order.getOrderNumber())
                .build();
        transactionRepository.save(tx);
    }

    @Transactional
    public void earnPoints(User user, Order order) {
        int points = (int) (order.getTotalAmount().doubleValue() * earnRate);
        if (points <= 0) return;

        RewardPoints rp = getOrCreate(user.getId());
        rp.addPoints(points);
        rewardPointsRepository.save(rp);

        RewardPointTransaction tx = RewardPointTransaction.builder()
                .user(user)
                .order(order)
                .type(RewardTransactionType.EARN)
                .points(points)
                .description("Earned for order " + order.getOrderNumber())
                .build();
        transactionRepository.save(tx);
    }

    @Transactional
    public void refundPoints(User user, Order order) {
        // Refund any redeemed points
        transactionRepository.findByOrderId(order.getId()).forEach(tx -> {
            if (tx.getType() == RewardTransactionType.REDEEM) {
                int refundAmt = Math.abs(tx.getPoints());
                RewardPoints rp = getOrCreate(user.getId());
                rp.addPoints(refundAmt);
                rewardPointsRepository.save(rp);
                transactionRepository.save(RewardPointTransaction.builder()
                        .user(user)
                        .order(order)
                        .type(RewardTransactionType.REFUND)
                        .points(refundAmt)
                        .description("Refund of redeemed points for cancelled order " + order.getOrderNumber())
                        .build());
            } else if (tx.getType() == RewardTransactionType.EARN) {
                int reversal = tx.getPoints();
                RewardPoints rp = getOrCreate(user.getId());
                if (rp.getBalance() >= reversal) {
                    rp.deductPoints(reversal);
                    rewardPointsRepository.save(rp);
                    transactionRepository.save(RewardPointTransaction.builder()
                            .user(user)
                            .order(order)
                            .type(RewardTransactionType.REFUND)
                            .points(-reversal)
                            .description("Reversal of earned points for cancelled order " + order.getOrderNumber())
                            .build());
                }
            }
        });
    }

    private RewardPoints getOrCreate(Long userId) {
        return rewardPointsRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Reward points record not found for user " + userId));
    }

    private RewardTransactionResponse toTransactionResponse(RewardPointTransaction tx) {
        return RewardTransactionResponse.builder()
                .id(tx.getId())
                .type(tx.getType())
                .points(tx.getPoints())
                .description(tx.getDescription())
                .orderId(tx.getOrder() != null ? tx.getOrder().getId() : null)
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
