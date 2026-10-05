package com.cyberopus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CartItemResponse {
    private Long itemId;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String imageUrl;
    private BigDecimal priceAtAdd;
    private BigDecimal currentPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
}
