package com.cyberopus.service;

import com.cyberopus.dto.request.CartItemRequest;
import com.cyberopus.dto.request.UpdateCartItemRequest;
import com.cyberopus.dto.response.CartItemResponse;
import com.cyberopus.dto.response.CartResponse;
import com.cyberopus.entity.Book;
import com.cyberopus.entity.Cart;
import com.cyberopus.entity.CartItem;
import com.cyberopus.exception.BadRequestException;
import com.cyberopus.exception.ForbiddenException;
import com.cyberopus.exception.InsufficientStockException;
import com.cyberopus.exception.ResourceNotFoundException;
import com.cyberopus.repository.CartItemRepository;
import com.cyberopus.repository.CartRepository;
import com.cyberopus.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookService bookService;
    private final SecurityUtils securityUtils;

    public CartResponse getCart() {
        Cart cart = getOrCreateCart();
        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse addItem(CartItemRequest request) {
        Cart cart = getOrCreateCart();
        Book book = bookService.findEntityById(request.getBookId());

        if (!book.getAvailable() || book.getStockQuantity() == 0) {
            throw new BadRequestException("Book is not available: " + book.getTitle());
        }

        // Check if item already in cart
        var existingOpt = cartItemRepository.findByCartIdAndBookId(cart.getId(), book.getId());
        if (existingOpt.isPresent()) {
            CartItem existing = existingOpt.get();
            int newQty = existing.getQuantity() + request.getQuantity();
            if (newQty > book.getStockQuantity()) {
                throw new InsufficientStockException(book.getTitle(), newQty, book.getStockQuantity());
            }
            existing.setQuantity(newQty);
            cartItemRepository.save(existing);
        } else {
            if (request.getQuantity() > book.getStockQuantity()) {
                throw new InsufficientStockException(book.getTitle(), request.getQuantity(), book.getStockQuantity());
            }
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .book(book)
                    .quantity(request.getQuantity())
                    .priceAtAdd(book.getPrice())
                    .build();
            cart.getItems().add(cartItemRepository.save(item));
        }

        return toCartResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Transactional
    public CartResponse updateItem(Long itemId, UpdateCartItemRequest request) {
        CartItem item = findItemWithOwnerCheck(itemId);
        Book book = item.getBook();

        if (request.getQuantity() > book.getStockQuantity()) {
            throw new InsufficientStockException(book.getTitle(), request.getQuantity(), book.getStockQuantity());
        }
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return toCartResponse(getOrCreateCart());
    }

    @Transactional
    public CartResponse removeItem(Long itemId) {
        CartItem item = findItemWithOwnerCheck(itemId);
        Cart cart = item.getCart();
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return toCartResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Transactional
    public void clearCart() {
        Cart cart = getOrCreateCart();
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    /** Used by CheckoutService to clear cart after order is placed */
    @Transactional
    public void clearCartForUser(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    private CartItem findItemWithOwnerCheck(Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));
        Long currentUserId = securityUtils.getCurrentUserId();
        if (!item.getCart().getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("You do not own this cart item");
        }
        return item;
    }

    private Cart getOrCreateCart() {
        Long userId = securityUtils.getCurrentUserId();
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart c = Cart.builder()
                            .user(securityUtils.getCurrentUser())
                            .build();
                    return cartRepository.save(c);
                });
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .subtotal(subtotal)
                .itemCount(items.size())
                .build();
    }

    private CartItemResponse toItemResponse(CartItem item) {
        BigDecimal lineTotal = item.getPriceAtAdd()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        return CartItemResponse.builder()
                .itemId(item.getId())
                .bookId(item.getBook().getId())
                .bookTitle(item.getBook().getTitle())
                .bookAuthor(item.getBook().getAuthor())
                .imageUrl(item.getBook().getImageUrl())
                .priceAtAdd(item.getPriceAtAdd())
                .currentPrice(item.getBook().getPrice())
                .quantity(item.getQuantity())
                .lineTotal(lineTotal)
                .build();
    }
}
