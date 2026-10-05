package com.cyberopus.repository;

import com.cyberopus.entity.RewardPointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RewardPointTransactionRepository extends JpaRepository<RewardPointTransaction, Long> {
    List<RewardPointTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<RewardPointTransaction> findByOrderIdAndType(Long orderId, com.cyberopus.enums.RewardTransactionType type);
    List<RewardPointTransaction> findByOrderId(Long orderId);
}
