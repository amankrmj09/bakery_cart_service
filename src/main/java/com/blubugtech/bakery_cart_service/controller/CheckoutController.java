package com.blubugtech.bakery_cart_service.controller;

import lombok.extern.slf4j.Slf4j;
import com.blubugtech.bakery_cart_service.dto.cart.*;
import com.blubugtech.bakery_cart_service.dto.checkout.*;
import com.blubugtech.bakery_cart_service.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.UUID;

@RestController
@RequestMapping("/api/carts")
@Tag(name = "Checkout", description = "Endpoints for cart checkout")
@RequiredArgsConstructor
@Slf4j
public class CheckoutController {

    private final CartService cartService;

    // Checkout cart
    @PostMapping("/{cartId}/checkout")
    @Operation(summary = "Checkout a cart")
    public ResponseEntity<CheckoutResponse> checkoutCart(
            @PathVariable UUID cartId,
            @Valid @RequestBody CheckoutRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Checkout cart request received: {}", cartId);

        if (!canAccessCart(cartId, userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CheckoutResponse result = cartService.checkoutCart(cartId, request);

        log.info("Cart checked out successfully: {}", cartId);
        return ResponseEntity.ok(result);
    }

    // Checkout 'me' cart
    @PostMapping("/me/checkout")
    @Operation(summary = "Checkout 'me' cart")
    public ResponseEntity<CheckoutResponse> checkoutMyCart(
            @Valid @RequestBody CheckoutRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Checkout 'me' cart request received");

        CartResponse cartInfo = null;
        if (userId != null) {
            cartInfo = cartService.getOrCreateCartForUser(userId);
        } else if (sessionId != null) {
            cartInfo = cartService.getOrCreateCartForSession(sessionId);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (cartInfo == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        CheckoutResponse result = cartService.checkoutCart(cartInfo.getId(), request);

        log.info("Cart checked out successfully: {}", cartInfo.getId());
        return ResponseEntity.ok(result);
    }

    private boolean canAccessCart(UUID cartId, UUID userId, String userRole) {
        if ("ADMIN".equals(userRole)) {
            return true;
        }
        try {
            CartResponse cart = cartService.getCartById(cartId);
            return cart.getUserId() == null || (userId != null && userId.equals(cart.getUserId()));
        } catch (Exception e) {
            log.error("Failed to check cart access for cart {}", cartId, e);
            return false;
        }
    }
}
