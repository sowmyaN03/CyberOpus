package com.cyberopus.service;

import com.cyberopus.dto.request.BuyAgainRequest;
import com.cyberopus.dto.request.CartItemRequest;
import com.cyberopus.dto.response.CartResponse;
import com.cyberopus.entity.Book;
import com.cyberopus.entity.Order;
import com.cyberopus.entity.OrderItem;
import com.cyberopus.exception.BadRequestException;
import com.cyberopus.exception.ResourceNotFoundException;
import com.cyberopus.repository.OrderRepository;
import com.cyberopus.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuyAgainService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final SecurityUtils securityUtils;

    @Transactional
    public CartResponse buyAgain(Long orderId, BuyAgainRequest request) {
        Long userId = securityUtils.getCurrentUserId();

        // findByIdAndUserId already enforces ownership – throws 404 if not found or not owned
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        OrderItem orderItem = order.getItems().stream()
                .filter(i -> i.getId().equals(request.getOrderItemId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found: " + request.getOrderItemId()));

        Book book = orderItem.getBook();

        if (!book.getAvailable() || book.getStockQuantity() == 0) {
            throw new BadRequestException(
                    "'" + book.getTitle() + "' is no longer available for purchase");
        }

        int qty = request.getQuantity() != null ? request.getQuantity() : 1;
        if (qty > book.getStockQuantity()) {
            throw new BadRequestException("Only " + book.getStockQuantity() + " copies available");
        }

        // Use current price – not historical order price
        CartItemRequest cartRequest = new CartItemRequest();
        cartRequest.setBookId(book.getId());
        cartRequest.setQuantity(qty);

        return cartService.addItem(cartRequest);
    }
}
