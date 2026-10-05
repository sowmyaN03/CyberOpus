package com.cyberopus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientRewardPointsException extends RuntimeException {
    public InsufficientRewardPointsException(int requested, int available) {
        super(String.format("Insufficient reward points: requested %d, available %d", requested, available));
    }
}
