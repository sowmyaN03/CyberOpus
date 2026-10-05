package com.cyberopus.dto.response;

import com.cyberopus.enums.RewardTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RewardTransactionResponse {
    private Long id;
    private RewardTransactionType type;
    private Integer points;
    private String description;
    private Long orderId;
    private Instant createdAt;
}
