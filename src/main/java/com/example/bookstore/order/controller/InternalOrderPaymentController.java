package com.example.bookstore.order.controller;

import com.example.bookstore.integration.order.OrderPaymentQuery;
import com.example.bookstore.integration.order.OrderPaymentView;
import com.example.bookstore.internal.InternalApiTokenValidator;
import com.example.bookstore.order.entity.OrderStatus;
import com.example.bookstore.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/internal/orders")
public class InternalOrderPaymentController {

    private final InternalApiTokenValidator tokenValidator;
    private final OrderPaymentQuery orderPaymentQuery;
    private final OrderService orderService;

    public InternalOrderPaymentController(
            InternalApiTokenValidator tokenValidator,
            OrderPaymentQuery orderPaymentQuery,
            OrderService orderService
    ) {
        this.tokenValidator = tokenValidator;
        this.orderPaymentQuery = orderPaymentQuery;
        this.orderService = orderService;
    }

    @GetMapping("/{orderId}/payment-view")
    public ResponseEntity<OrderPaymentView> paymentView(
            @RequestHeader(name = "X-Internal-Token") String internalToken,
            @PathVariable UUID orderId,
            @RequestParam @NotBlank String userEmail
    ) {
        tokenValidator.requireValid(internalToken);
        return ResponseEntity.ok(orderPaymentQuery.getMyOrderPaymentView(orderId, userEmail));
    }

    @PostMapping("/{orderId}/status")
    public ResponseEntity<Void> updateStatus(
            @RequestHeader(name = "X-Internal-Token") String internalToken,
            @PathVariable UUID orderId,
            @RequestBody @Valid UpdateInternalOrderStatusRequest request
    ) {
        tokenValidator.requireValid(internalToken);
        OrderStatus status = OrderStatus.valueOf(request.status().trim().toUpperCase(Locale.ROOT));
        orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.noContent().build();
    }

    public record UpdateInternalOrderStatusRequest(@NotBlank String status) {
    }
}

