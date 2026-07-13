package com.shah_s.bakery_cart_service.controller;

import com.shah_s.bakery_cart_service.dto.*;
import com.shah_s.bakery_cart_service.entity.Cart;
import com.shah_s.bakery_cart_service.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/carts")

public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    // Create cart
    @PostMapping
    public ResponseEntity<CartResponseDto> createCart(
            @Valid @RequestBody CartRequestDto request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Create cart request received for user: {} session: {}", userId, sessionId);

        // Use header values if available
        if (userId != null) {
            request.setUserId(userId);
        }
        if (sessionId != null) {
            request.setSessionId(sessionId);
        }

        CartResponseDto cart = cartService.createCart(request);

        logger.info("Cart created successfully: {}", cart.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }

    // Get cart by ID
    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponseDto> getCartById(
            @PathVariable UUID cartId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get cart by ID request received: {}", cartId);

        CartResponseDto cart = cartService.getCartById(cartId);

        // Check if user can access this cart (unless admin)
        if (userId != null && !"ADMIN".equals(userRole) && cart.getUserId() != null
                && !cart.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        logger.info("Cart retrieved: {}", cart.getId());
        return ResponseEntity.ok(cart);
    }

    // Get 'me' cart
    @GetMapping("/me")
    public ResponseEntity<CartResponseDto> getMyCart(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        logger.info("Get 'me' cart request received");

        CartResponseDto cartInfo = null;
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
    public ResponseEntity<CartResponseDto> getOrCreateCartForUser(
            @PathVariable UUID userId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requestUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get or create cart for user request received: {}", userId);

        // Check if user can access this cart (unless admin)
        if (requestUserId != null && !"ADMIN".equals(userRole) && !userId.equals(requestUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Object result = cartService.getOrCreateCartForUser(userId);
        CartResponseDto cart = CartService.convertIfMap(result, objectMapper);
        if (cart == null) {
            logger.error("Failed to convert cached value to CartResponseDto for user: {}", userId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
        logger.info("Cart retrieved/created for user: {}", userId);
        return ResponseEntity.ok(cart);
    }

    // Get or create cart for session
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<CartResponseDto> getOrCreateCartForSession(
            @PathVariable String sessionId,
            @RequestHeader(value = "X-Session-Id", required = false) String requestSessionId) {

        logger.info("Get or create cart for session request received: {}", sessionId);

        // Basic session validation (could be enhanced with proper session management)
        if (requestSessionId != null && !sessionId.equals(requestSessionId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponseDto cart = cartService.getOrCreateCartForSession(sessionId);

        logger.info("Cart retrieved/created for session: {}", sessionId);
        return ResponseEntity.ok(cart);
    }

    // Add item to cart
    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartResponseDto> addItemToCart(
            @PathVariable UUID cartId,
            @Valid @RequestBody AddItemRequestDto request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Add item to cart request received: {} product: {}", cartId, request.getProductId());

        // Check cart access
        if (!canAccessCart(cartId, userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponseDto cart = cartService.addItemToCart(cartId, request);

        logger.info("Item added to cart successfully: {}", cartId);
        return ResponseEntity.ok(cart);
    }

    // Add item to 'me' cart
    @PostMapping("/me/items")
    public ResponseEntity<CartResponseDto> addItemToMyCart(
            @Valid @RequestBody AddItemRequestDto request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Add item to 'me' cart request received: product: {}", request.getProductId());

        CartResponseDto cartInfo = null;
        
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

        CartResponseDto updatedCart = cartService.addItemToCart(cartInfo.getId(), request);

        logger.info("Item added to 'me' cart successfully: {}", updatedCart.getId());
        return ResponseEntity.ok(updatedCart);
    }

    // Update item in cart
    @PutMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<CartResponseDto> updateCartItem(
            @PathVariable UUID cartId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateItemRequestDto request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Update cart item request received: {} item: {}", cartId, itemId);

        // Check cart access
        if (!canAccessCart(cartId, userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponseDto cart = cartService.updateCartItem(cartId, itemId, request);

        logger.info("Cart item updated successfully: {}", itemId);
        return ResponseEntity.ok(cart);
    }

    // Remove item from cart
    @DeleteMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<CartResponseDto> removeItemFromCart(
            @PathVariable UUID cartId,
            @PathVariable UUID itemId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Remove item from cart request received: {} item: {}", cartId, itemId);

        // Check cart access
        if (!canAccessCart(cartId, userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponseDto cart = cartService.removeItemFromCart(cartId, itemId);

        logger.info("Item removed from cart successfully: {}", itemId);
        return ResponseEntity.ok(cart);
    }

    // Clear cart
    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<CartResponseDto> clearCart(
            @PathVariable UUID cartId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Clear cart request received: {}", cartId);

        // Check cart access
        if (!canAccessCart(cartId, userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponseDto cart = cartService.clearCart(cartId);

        logger.info("Cart cleared successfully: {}", cartId);
        return ResponseEntity.ok(cart);
    }

    // Update cart details
    @PatchMapping("/{cartId}")
    public ResponseEntity<CartResponseDto> updateCart(
            @PathVariable UUID cartId,
            @Valid @RequestBody CartUpdateRequestDto request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Update cart request received: {}", cartId);

        // Check cart access
        if (!canAccessCart(cartId, userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponseDto cart = cartService.updateCart(cartId, request);

        logger.info("Cart updated successfully: {}", cartId);
        return ResponseEntity.ok(cart);
    }

    // Merge carts
    @PostMapping("/merge")
    public ResponseEntity<CartResponseDto> mergeCarts(
            @Valid @RequestBody MergeCartsRequestDto request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Merge carts request received: {} -> {}",
                request.getSourceCartId(), request.getTargetCartId());

        // Check access to both carts
        if (!canAccessCart(request.getSourceCartId(), userId, userRole) ||
                !canAccessCart(request.getTargetCartId(), userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponseDto cart = cartService.mergeCarts(request);

        logger.info("Carts merged successfully: {}", request.getTargetCartId());
        return ResponseEntity.ok(cart);
    }

    // Save cart for later
    @PostMapping("/{cartId}/save")
    public ResponseEntity<CartResponseDto> saveCartForLater(
            @PathVariable UUID cartId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Save cart for later request received: {}", cartId);

        // Check cart access
        if (!canAccessCart(cartId, userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponseDto cart = cartService.saveCartForLater(cartId);

        logger.info("Cart saved for later: {}", cartId);
        return ResponseEntity.ok(cart);
    }

    // Checkout cart
    @PostMapping("/{cartId}/checkout")
    public ResponseEntity<Map<String, Object>> checkoutCart(
            @PathVariable UUID cartId,
            @Valid @RequestBody CheckoutRequestDto request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Checkout cart request received: {}", cartId);

        // Check cart access
        if (!canAccessCart(cartId, userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Map<String, Object> result = cartService.checkoutCart(cartId, request);

        logger.info("Cart checked out successfully: {}", cartId);
        return ResponseEntity.ok(result);
    }

    // Checkout 'me' cart
    @PostMapping("/me/checkout")
    public ResponseEntity<Map<String, Object>> checkoutMyCart(
            @Valid @RequestBody CheckoutRequestDto request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Checkout 'me' cart request received");

        CartResponseDto cartInfo = null;
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

        Map<String, Object> result = cartService.checkoutCart(cartInfo.getId(), request);

        logger.info("Cart checked out successfully: {}", cartInfo.getId());
        return ResponseEntity.ok(result);
    }

    // Get user's carts
    @GetMapping("/user/{userId}/all")
    public ResponseEntity<List<CartResponseDto>> getUserCarts(
            @PathVariable UUID userId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requestUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get user carts request received: {}", userId);

        // Check if user can access these carts (unless admin)
        if (requestUserId != null && !"ADMIN".equals(userRole) && !userId.equals(requestUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<CartResponseDto> carts = cartService.getUserCarts(userId);

        logger.info("Retrieved {} carts for user", carts.size());
        return ResponseEntity.ok(carts);
    }

    // Get carts by status (Admin only)
    @GetMapping("/status/{status}")
    public ResponseEntity<List<CartResponseDto>> getCartsByStatus(
            @PathVariable Cart.CartStatus status,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get carts by status request received: {}", status);

        // Only admins can view carts by status
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<CartResponseDto> carts = cartService.getCartsByStatus(status);

        logger.info("Retrieved {} carts with status {}", carts.size(), status);
        return ResponseEntity.ok(carts);
    }

    // Get all carts with pagination (Admin only)
    @GetMapping
    public ResponseEntity<Page<CartResponseDto>> getAllCarts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get all carts request received (page: {}, size: {})", page, size);

        // Only admins can view all carts
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CartResponseDto> carts = cartService.getAllCarts(pageable);

        logger.info("Retrieved {} carts (page {} of {})", carts.getContent().size(),
                page + 1, carts.getTotalPages());
        return ResponseEntity.ok(carts);
    }

    // Get cart statistics (Admin only)
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCartStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get cart statistics request received");

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

        logger.info("Cart statistics retrieved");
        return ResponseEntity.ok(statistics);
    }

    // Health check
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "cart-service-carts");
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    // Private helper method for access control
    private boolean canAccessCart(UUID cartId, UUID userId, String userRole) {
        if ("ADMIN".equals(userRole)) {
            return true; // Admins can access any cart
        }

        try {
            CartResponseDto cart = cartService.getCartById(cartId);

            // Check if user owns the cart or it's a guest cart
            return cart.getUserId() == null ||
                    (userId != null && userId.equals(cart.getUserId()));
        } catch (Exception e) {
            logger.warn("Failed to check cart access for cart {}: {}", cartId, e.getMessage());
            return false;
        }
    }
}
