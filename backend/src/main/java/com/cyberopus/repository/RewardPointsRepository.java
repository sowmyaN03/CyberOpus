package com.cyberopus.repository;

import com.cyberopus.entity.RewardPoints;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RewardPointsRepository extends JpaRepository<RewardPoints, Long> {
    Optional<RewardPoints> findByUserId(Long userId);
}
