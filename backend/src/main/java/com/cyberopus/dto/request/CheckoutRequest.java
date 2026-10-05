package com.cyberopus.dto.request;

import com.cyberopus.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CheckoutRequest {

    @NotNull(message = "Address ID is required")
    private Long addressId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Pattern(regexp = "\\*{4}\\d{4}", message = "Masked card number must be in format ****1234")
    private String maskedCardNumber;

    @Size(max = 200)
    private String cardHolderName;

    @PositiveOrZero(message = "Reward points to redeem must be zero or positive")
    private Integer rewardPointsToRedeem = 0;
}
