package com.cyberopus.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 500)
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 255)
    private String author;

    @Size(max = 5000)
    private String description;

    @Size(max = 20)
    private String isbn;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(message = "Stock quantity must be zero or positive")
    private Integer stockQuantity;

    @Size(max = 1000)
    private String imageUrl;

    @NotNull(message = "Category ID is required")
    @Positive
    private Long categoryId;

    @NotNull(message = "Brand ID is required")
    @Positive
    private Long brandId;

    private Boolean available = true;
}
