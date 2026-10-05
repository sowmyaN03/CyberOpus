package com.cyberopus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String bookTitle, int requested, int available) {
        super(String.format("Insufficient stock for '%s': requested %d, available %d",
                bookTitle, requested, available));
    }
}
