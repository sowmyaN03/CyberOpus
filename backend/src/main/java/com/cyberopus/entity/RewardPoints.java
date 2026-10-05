package com.cyberopus.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "reward_points")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RewardPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Integer balance = 0;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    public void addPoints(int points) {
        this.balance += points;
        this.updatedAt = Instant.now();
    }

    public void deductPoints(int points) {
        if (points > this.balance) {
            throw new IllegalStateException("Insufficient reward points");
        }
        this.balance -= points;
        this.updatedAt = Instant.now();
    }
}
