package com.cyberopus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private Long bookId;
    private String bookTitleSnapshot;
    private String bookAuthorSnapshot;
    private BigDecimal priceSnapshot;
    private Integer quantity;
    private BigDecimal lineTotal;
}
