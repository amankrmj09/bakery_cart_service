package com.blubugtech.bakery_cart_service.controller;

import lombok.extern.slf4j.Slf4j;
import com.blubugtech.bakery_cart_service.dto.cart.*;
import com.blubugtech.bakery_cart_service.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/carts")
@Tag(name = "Carts", description = "Endpoints for shopping cart management")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    // Create cart
    @PostMapping
    @Operation(summary = "Create a new cart")
    public ResponseEntity<CartResponse> createCart(
            @Valid @RequestBody CartRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Create cart request received for user: {} session: {}", userId, sessionId);

        // Use header values if available
        if (userId != null) {
            request.setUserId(userId);
        }
        if (sessionId != null) {
            request.setSessionId(sessionId);
        }

        CartResponse cart = cartService.createCart(request);

        log.info("Cart created successfully: {}", cart.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }

    // Get cart by ID
    @GetMapping("/{cartId}")
    @Operation(summary = "Get cart by ID")
    public ResponseEntity<CartResponse> getCartById(
            @PathVariable UUID cartId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get cart by ID request received: {}", cartId);

        CartResponse cart = cartService.getCartById(cartId);

        // Check if user can access this cart (unless admin)
        if (userId != null && !"ADMIN".equals(userRole) && cart.getUserId() != null
                && !cart.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info("Cart retrieved: {}", cart.getId());
        return ResponseEntity.ok(cart);
    }

    // Get 'me' cart
    @GetMapping("/me")
    @Operation(summary = "Get 'me' cart")
    public ResponseEntity<CartResponse> getMyCart(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        log.info("Get 'me' cart request received");

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

        return ResponseEntity.ok(cartInfo);
    }

    // Get or create cart for user
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get or create cart for user")
    public ResponseEntity<CartResponse> getOrCreateCartForUser(
            @PathVariable UUID userId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requestUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get or create cart for user request received: {}", userId);

        // Check if user can access this cart (unless admin)
        if (requestUserId != null && !"ADMIN".equals(userRole) && !userId.equals(requestUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponse cart = cartService.getOrCreateCartForUser(userId);
        if (cart == null) {
            log.error("Failed to fetch CartResponse for user: {}", userId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
        log.info("Cart retrieved/created for user: {}", userId);
        return ResponseEntity.ok(cart);
    }

    // Get or create cart for session
    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get or create cart for session")
    public ResponseEntity<CartResponse> getOrCreateCartForSession(
            @PathVariable String sessionId,
            @RequestHeader(value = "X-Session-Id", required = false) String requestSessionId) {

        log.info("Get or create cart for session request received: {}", sessionId);

        // Basic session validation (could be enhanced with proper session management)
        if (requestSessionId != null && !sessionId.equals(requestSessionId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponse cart = cartService.getOrCreateCartForSession(sessionId);

        log.info("Cart retrieved/created for session: {}", sessionId);
        return ResponseEntity.ok(cart);
    }

    // Update cart details
    @PatchMapping("/{cartId}")
    @Operation(summary = "Update cart details")
    public ResponseEntity<CartResponse> updateCart(
            @PathVariable UUID cartId,
            @Valid @RequestBody CartUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Update cart request received: {}", cartId);

        // Check cart access
        if (!canAccessCart(cartId, userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponse cart = cartService.updateCart(cartId, request);

        log.info("Cart updated successfully: {}", cartId);
        return ResponseEntity.ok(cart);
    }

    // Merge carts
    @PostMapping("/merge")
    @Operation(summary = "Merge two carts")
    public ResponseEntity<CartResponse> mergeCarts(
            @Valid @RequestBody MergeCartsRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Merge carts request received: {} -> {}",
                request.getSourceCartId(), request.getTargetCartId());

        // Check access to both carts
        if (!canAccessCart(request.getSourceCartId(), userId, userRole) ||
                !canAccessCart(request.getTargetCartId(), userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponse cart = cartService.mergeCarts(request);

        log.info("Carts merged successfully: {}", request.getTargetCartId());
        return ResponseEntity.ok(cart);
    }

    // Save cart for later
    @PostMapping("/{cartId}/save")
    @Operation(summary = "Save an entire cart for later")
    public ResponseEntity<CartResponse> saveCartForLater(
            @PathVariable UUID cartId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Save cart for later request received: {}", cartId);

        // Check cart access
        if (!canAccessCart(cartId, userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponse cart = cartService.saveCartForLater(cartId);

        log.info("Cart saved for later: {}", cartId);
        return ResponseEntity.ok(cart);
    }

    // Get user's carts
    @GetMapping("/user/{userId}/all")
    @Operation(summary = "Get all carts for a user")
    public ResponseEntity<List<CartResponse>> getUserCarts(
            @PathVariable UUID userId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requestUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get user carts request received: {}", userId);

        // Check if user can access these carts (unless admin)
        if (requestUserId != null && !"ADMIN".equals(userRole) && !userId.equals(requestUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<CartResponse> carts = cartService.getUserCarts(userId);

        log.info("Retrieved {} carts for user", carts.size());
        return ResponseEntity.ok(carts);
    }

    // Private helper method for access control
    private boolean canAccessCart(UUID cartId, UUID userId, String userRole) {
        if ("ADMIN".equals(userRole)) {
            return true; // Admins can access any cart
        }

        try {
            CartResponse cart = cartService.getCartById(cartId);

            // Check if user owns the cart or it's a guest cart
            return cart.getUserId() == null ||
                    (userId != null && userId.equals(cart.getUserId()));
        } catch (Exception e) {
            log.error("Failed to check cart access for cart {}", cartId, e);
            return false;
        }
    }
}
