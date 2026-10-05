package com.cyberopus.controller;

import com.cyberopus.dto.request.BuyAgainRequest;
import com.cyberopus.dto.response.CartResponse;
import com.cyberopus.dto.response.OrderResponse;
import com.cyberopus.service.BuyAgainService;
import com.cyberopus.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order history and management")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final BuyAgainService buyAgainService;

    @GetMapping
    @Operation(summary = "Get order history (paginated)")
    public ResponseEntity<Page<OrderResponse>> getAll(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(orderService.getMyOrders(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order detail")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order (only within 48 hours of placement)")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @PostMapping("/{orderId}/buy-again")
    @Operation(summary = "Add a previously purchased book back to the cart at current price")
    public ResponseEntity<CartResponse> buyAgain(@PathVariable Long orderId,
                                                  @Valid @RequestBody BuyAgainRequest request) {
        return ResponseEntity.ok(buyAgainService.buyAgain(orderId, request));
    }
}
