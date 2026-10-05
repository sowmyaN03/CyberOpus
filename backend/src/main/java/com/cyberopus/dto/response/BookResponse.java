package com.cyberopus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String description;
    private String isbn;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private CategoryResponse category;
    private BrandResponse brand;
    private Boolean available;
    private Instant createdAt;
    private Instant updatedAt;
    /** Tentative delivery date computed by the service */
    private LocalDate tentativeDeliveryDate;
}
