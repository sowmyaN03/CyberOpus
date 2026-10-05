package com.cyberopus.service;

import com.cyberopus.entity.RewardPoints;
import com.cyberopus.entity.User;
import com.cyberopus.exception.InsufficientRewardPointsException;
import com.cyberopus.repository.RewardPointTransactionRepository;
import com.cyberopus.repository.RewardPointsRepository;
import com.cyberopus.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @Mock RewardPointsRepository rewardPointsRepository;
    @Mock RewardPointTransactionRepository transactionRepository;
    @Mock SecurityUtils securityUtils;

    @InjectMocks RewardService rewardService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rewardService, "earnRate", 10);
        ReflectionTestUtils.setField(rewardService, "redeemRate", 100);
        ReflectionTestUtils.setField(rewardService, "maxRedeemPct", 0.20);
    }

    @Test
    void validateAndComputeDiscount_ValidRedemption_ReturnsDiscount() {
        User user = User.builder().id(1L).build();
        RewardPoints rp = RewardPoints.builder().user(user).balance(500).build();
        when(rewardPointsRepository.findByUserId(1L)).thenReturn(Optional.of(rp));

        // 500 points = $5.00, subtotal $50 → 20% cap = $10 → $5 is within cap
        BigDecimal discount = rewardService.validateAndComputeDiscount(1L, 500, new BigDecimal("50.00"));

        assertThat(discount).isEqualByComparingTo("5.00");
    }

    @Test
    void validateAndComputeDiscount_ExceedsBalance_ThrowsException() {
        User user = User.builder().id(1L).build();
        RewardPoints rp = RewardPoints.builder().user(user).balance(100).build();
        when(rewardPointsRepository.findByUserId(1L)).thenReturn(Optional.of(rp));

        assertThatThrownBy(() ->
                rewardService.validateAndComputeDiscount(1L, 200, new BigDecimal("50.00")))
                .isInstanceOf(InsufficientRewardPointsException.class);
    }

    @Test
    void validateAndComputeDiscount_ExceedsCap_ThrowsException() {
        User user = User.builder().id(1L).build();
        // 2000 points = $20 but 20% of $50 = $10 cap → rejected
        RewardPoints rp = RewardPoints.builder().user(user).balance(2000).build();
        when(rewardPointsRepository.findByUserId(1L)).thenReturn(Optional.of(rp));

        assertThatThrownBy(() ->
                rewardService.validateAndComputeDiscount(1L, 2000, new BigDecimal("50.00")))
                .isInstanceOf(InsufficientRewardPointsException.class);
    }

    @Test
    void validateAndComputeDiscount_ZeroPoints_ReturnsZero() {
        BigDecimal result = rewardService.validateAndComputeDiscount(1L, 0, new BigDecimal("50.00"));
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getMyRewards_ReturnsBalanceAndTransactions() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        User user = User.builder().id(1L).build();
        RewardPoints rp = RewardPoints.builder().user(user).balance(500).build();
        when(rewardPointsRepository.findByUserId(1L)).thenReturn(Optional.of(rp));
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(new ArrayList<>());

        var result = rewardService.getMyRewards();

        assertThat(result.getBalance()).isEqualTo(500);
        assertThat(result.getTransactions()).isEmpty();
    }
}
