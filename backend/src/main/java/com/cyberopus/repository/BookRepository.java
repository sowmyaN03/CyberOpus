package com.cyberopus.repository;

import com.cyberopus.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    Page<Book> findByAvailableTrue(Pageable pageable);

    List<Book> findByCategoryIdAndAvailableTrueAndIdNot(Long categoryId, Long excludeId, Pageable pageable);

    Page<Book> findByCategoryIdAndAvailableTrue(Long categoryId, Pageable pageable);

    Page<Book> findByBrandIdAndAvailableTrue(Long brandId, Pageable pageable);

    @Query("""
            SELECT b FROM Book b
            WHERE b.available = true
            AND (:q IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%'))
                             OR LOWER(b.author) LIKE LOWER(CONCAT('%', :q, '%'))
                             OR LOWER(b.description) LIKE LOWER(CONCAT('%', :q, '%')))
            AND (:categoryId IS NULL OR b.category.id = :categoryId)
            AND (:brandId IS NULL OR b.brand.id = :brandId)
            AND (:minPrice IS NULL OR b.price >= :minPrice)
            AND (:maxPrice IS NULL OR b.price <= :maxPrice)
            """)
    Page<Book> search(@Param("q") String q,
                      @Param("categoryId") Long categoryId,
                      @Param("brandId") Long brandId,
                      @Param("minPrice") BigDecimal minPrice,
                      @Param("maxPrice") BigDecimal maxPrice,
                      Pageable pageable);

    /** Best-seller fallback: books ordered by total units sold */
    @Query("""
            SELECT b FROM Book b
            WHERE b.available = true AND b.stockQuantity > 0
            AND b.id NOT IN (
                SELECT DISTINCT oi.book.id FROM OrderItem oi WHERE oi.order.user.id = :userId
            )
            ORDER BY (
                SELECT COALESCE(SUM(oi2.quantity), 0) FROM OrderItem oi2 WHERE oi2.book.id = b.id
            ) DESC
            """)
    List<Book> findBestsellersExcludingPurchased(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT b FROM Book b
            WHERE b.available = true AND b.stockQuantity > 0
            AND b.id NOT IN :purchasedIds
            ORDER BY (
                SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.book.id = b.id
            ) DESC
            """)
    List<Book> findCandidatesForRecommendation(@Param("purchasedIds") List<Long> purchasedIds, Pageable pageable);
}
