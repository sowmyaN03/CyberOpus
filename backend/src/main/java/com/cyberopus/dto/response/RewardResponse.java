package com.cyberopus.dto.response;

import com.cyberopus.enums.RewardTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RewardResponse {
    private Integer balance;
    private List<RewardTransactionResponse> transactions;
}
