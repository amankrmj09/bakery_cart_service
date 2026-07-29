package com.blubugtech.bakery_cart_service.controller;

import lombok.extern.slf4j.Slf4j;
import com.blubugtech.bakery_cart_service.dto.cart.*;
import com.blubugtech.bakery_cart_service.dto.cartitem.*;
import com.blubugtech.bakery_cart_service.dto.checkout.*;
import com.blubugtech.bakery_cart_service.entity.Cart;
import com.blubugtech.bakery_cart_service.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/carts")
@Tag(name = "Carts", description = "Endpoints for shopping cart management")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    private ObjectMapper objectMapper;

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
            Object result = cartService.getOrCreateCartForUser(userId);
            cartInfo = CartService.convertIfMap(result, objectMapper);
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

        Object result = cartService.getOrCreateCartForUser(userId);
        CartResponse cart = CartService.convertIfMap(result, objectMapper);
        if (cart == null) {
            log.error("Failed to convert cached value to CartResponse for user: {}", userId);
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

    // Add item to cart
    @PostMapping("/{cartId}/items")
    @Operation(summary = "Add an item to a cart")
    public ResponseEntity<CartResponse> addItemToCart(
            @PathVariable UUID cartId,
            @Valid @RequestBody AddItemRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Add item to cart request received: {} product: {}", cartId, request.getProductId());

        // Check cart access
        if (!canAccessCart(cartId, userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponse cart = cartService.addItemToCart(cartId, request);

        log.info("Item added to cart successfully: {}", cartId);
        return ResponseEntity.ok(cart);
    }

    // Add item to 'me' cart
    @PostMapping("/me/items")
    @Operation(summary = "Add an item to 'me' cart")
    public ResponseEntity<CartResponse> addItemToMyCart(
            @Valid @RequestBody AddItemRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Add item to 'me' cart request received: product: {}", request.getProductId());

        CartResponse cartInfo = null;

        if (userId != null) {
            Object result = cartService.getOrCreateCartForUser(userId);
            cartInfo = CartService.convertIfMap(result, objectMapper);
        } else if (sessionId != null) {
            cartInfo = cartService.getOrCreateCartForSession(sessionId);
        } else {
            // Need either userId or sessionId to resolve "me"
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (cartInfo == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        CartResponse updatedCart = cartService.addItemToCart(cartInfo.getId(), request);

        log.info("Item added to 'me' cart successfully: {}", updatedCart.getId());
        return ResponseEntity.ok(updatedCart);
    }

    // Update item in cart
    @PutMapping("/{cartId}/items/{itemId}")
    @Operation(summary = "Update an item in a cart")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable UUID cartId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateItemRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Update cart item request received: {} item: {}", cartId, itemId);

        // Check cart access
        if (!canAccessCart(cartId, userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponse cart = cartService.updateCartItem(cartId, itemId, request);

        log.info("Cart item updated successfully: {}", itemId);
        return ResponseEntity.ok(cart);
    }

    // Remove item from cart
    @DeleteMapping("/{cartId}/items/{itemId}")
    @Operation(summary = "Remove an item from a cart")
    public ResponseEntity<CartResponse> removeItemFromCart(
            @PathVariable UUID cartId,
            @PathVariable UUID itemId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Remove item from cart request received: {} item: {}", cartId, itemId);

        // Check cart access
        if (!canAccessCart(cartId, userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponse cart = cartService.removeItemFromCart(cartId, itemId);

        log.info("Item removed from cart successfully: {}", itemId);
        return ResponseEntity.ok(cart);
    }

    // Clear cart
    @DeleteMapping("/{cartId}/items")
    @Operation(summary = "Clear all items from a cart")
    public ResponseEntity<CartResponse> clearCart(
            @PathVariable UUID cartId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Clear cart request received: {}", cartId);

        // Check cart access
        if (!canAccessCart(cartId, userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponse cart = cartService.clearCart(cartId);

        log.info("Cart cleared successfully: {}", cartId);
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

    // Checkout cart
    @PostMapping("/{cartId}/checkout")
    @Operation(summary = "Checkout a cart")
    public ResponseEntity<CheckoutResponse> checkoutCart(
            @PathVariable UUID cartId,
            @Valid @RequestBody CheckoutRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Checkout cart request received: {}", cartId);

        // Check cart access
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
            Object result = cartService.getOrCreateCartForUser(userId);
            cartInfo = CartService.convertIfMap(result, objectMapper);
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

    // Get carts by status (Admin only)
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get carts by status (Admin only)")
    public ResponseEntity<List<CartResponse>> getCartsByStatus(
            @PathVariable Cart.CartStatus status,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get carts by status request received: {}", status);

        // Only admins can view carts by status
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<CartResponse> carts = cartService.getCartsByStatus(status);

        log.info("Retrieved {} carts with status {}", carts.size(), status);
        return ResponseEntity.ok(carts);
    }

    // Get all carts with pagination (Admin only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all carts (Admin only)")
    public ResponseEntity<Page<CartResponse>> getAllCarts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get all carts request received (page: {}, size: {})", page, size);

        // Only admins can view all carts
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CartResponse> carts = cartService.getAllCarts(pageable);

        log.info("Retrieved {} carts (page {} of {})", carts.getContent().size(),
                page + 1, carts.getTotalPages());
        return ResponseEntity.ok(carts);
    }

    // Get cart statistics (Admin only)
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get cart statistics (Admin only)")
    public ResponseEntity<Map<String, Object>> getCartStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get cart statistics request received");

        // Only admins can view statistics
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Default to last 30 days if no dates provided
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        Map<String, Object> statistics = cartService.getCartStatistics(startDate, endDate);

        log.info("Cart statistics retrieved");
        return ResponseEntity.ok(statistics);
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
            log.warn("Failed to check cart access for cart {}: {}", cartId, e.getMessage());
            return false;
        }
    }
}
