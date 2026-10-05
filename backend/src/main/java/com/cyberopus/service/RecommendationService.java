package com.cyberopus.service;

import com.cyberopus.dto.response.BookResponse;
import com.cyberopus.entity.Book;
import com.cyberopus.entity.OrderItem;
import com.cyberopus.repository.BookRepository;
import com.cyberopus.repository.OrderItemRepository;
import com.cyberopus.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final OrderItemRepository orderItemRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final SecurityUtils securityUtils;

    /**
     * Deterministic recommendation algorithm based on order history.
     *
     * Scoring:
     *   - +2 per item for matching category
     *   - +1 per item for matching brand/publisher
     *
     * If no order history exists, falls back to bestsellers.
     */
    public List<BookResponse> getRecommendations(int limit) {
        Long userId = securityUtils.getCurrentUserId();

        List<OrderItem> history = orderItemRepository.findAllByUserId(userId);

        if (history.isEmpty()) {
            // Fallback: bestsellers not yet purchased by anyone
            return bookRepository.findBestsellersExcludingPurchased(userId, PageRequest.of(0, limit))
                    .stream().map(bookService::toResponse).collect(Collectors.toList());
        }

        // Build preference profile
        Set<Long> purchasedIds = new HashSet<>();
        Map<Long, Integer> categoryScores = new HashMap<>();
        Map<Long, Integer> brandScores = new HashMap<>();

        for (OrderItem item : history) {
            purchasedIds.add(item.getBook().getId());
            Long catId = item.getBook().getCategory().getId();
            Long brandId = item.getBook().getBrand().getId();
            categoryScores.merge(catId, item.getQuantity(), Integer::sum);
            brandScores.merge(brandId, item.getQuantity(), Integer::sum);
        }

        // Candidate pool: available, in stock, not previously purchased
        List<Book> candidates = bookRepository.findCandidatesForRecommendation(
                new ArrayList<>(purchasedIds), PageRequest.of(0, limit * 5));

        // Score each candidate
        List<Map.Entry<Book, Integer>> scored = candidates.stream()
                .map(book -> {
                    int score = categoryScores.getOrDefault(book.getCategory().getId(), 0) * 2
                            + brandScores.getOrDefault(book.getBrand().getId(), 0);
                    return Map.entry(book, score);
                })
                .sorted(Map.Entry.<Book, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());

        return scored.stream()
                .map(e -> bookService.toResponse(e.getKey()))
                .collect(Collectors.toList());
    }
}
