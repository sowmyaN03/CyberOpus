package com.cyberopus.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items",
        indexes = {
                @Index(name = "idx_order_items_order", columnList = "order_id"),
                @Index(name = "idx_order_items_book", columnList = "book_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    /** Immutable snapshot of title at time of purchase */
    @Column(name = "book_title_snapshot", nullable = false, length = 500)
    private String bookTitleSnapshot;

    /** Immutable snapshot of author at time of purchase */
    @Column(name = "book_author_snapshot", nullable = false, length = 255)
    private String bookAuthorSnapshot;

    /** Immutable snapshot of price at time of purchase */
    @Column(name = "price_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceSnapshot;

    @Column(nullable = false)
    private Integer quantity;
}
