package com.cyberopus.controller;

import com.cyberopus.dto.response.RewardResponse;
import com.cyberopus.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
@Tag(name = "Rewards", description = "Reward/gift points balance and history")
@SecurityRequirement(name = "bearerAuth")
public class RewardController {

    private final RewardService rewardService;

    @GetMapping
    @Operation(summary = "Get reward points balance and transaction history")
    public ResponseEntity<RewardResponse> getMyRewards() {
        return ResponseEntity.ok(rewardService.getMyRewards());
    }
}
