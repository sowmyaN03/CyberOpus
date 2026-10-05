package com.cyberopus.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BuyAgainRequest {

    @NotNull(message = "Order item ID is required")
    private Long orderItemId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;
}
