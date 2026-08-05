package com.blubugtech.bakery_cart_service.controller;

import lombok.extern.slf4j.Slf4j;
import com.blubugtech.bakery_cart_service.dto.cart.*;
import com.blubugtech.bakery_cart_service.dto.cartitem.*;
import com.blubugtech.bakery_cart_service.service.CartService;
import com.blubugtech.bakery_cart_service.service.CartItemService;
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
@RequestMapping("/api")
@Tag(name = "Cart Items", description = "Endpoints for managing items within a cart")
@RequiredArgsConstructor
@Slf4j
public class CartItemController {

    private final CartItemService cartItemService;
    private final CartService cartService;

    // --- Endpoints originally in CartItemController (/api/cart-items) ---

    // Get cart item by ID
    @GetMapping("/cart-items/{itemId}")
    @Operation(summary = "Get cart item by ID")
    public ResponseEntity<CartItemResponse> getCartItemById(
            @PathVariable UUID itemId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get cart item by ID request received: {}", itemId);

        CartItemResponse item = cartItemService.getCartItemById(itemId);

        log.info("Cart item retrieved: {}", itemId);
        return ResponseEntity.ok(item);
    }

    // Get items for cart
    @GetMapping("/cart-items/cart/{cartId}")
    @Operation(summary = "Get all items in a cart")
    public ResponseEntity<org.springframework.data.web.PagedModel<CartItemResponse>> getCartItems(
            @PathVariable UUID cartId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get cart items request received for cart: {}", cartId);

        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.fromString(sortDir), sortBy);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);

        org.springframework.data.web.PagedModel<CartItemResponse> items = cartItemService.getCartItems(cartId, pageable);

        log.info("Retrieved {} items for cart", items.getContent().size());
        return ResponseEntity.ok(items);
    }

    // Get saved items for cart
    @GetMapping("/cart-items/cart/{cartId}/saved")
    @Operation(summary = "Get saved items for a cart")
    public ResponseEntity<org.springframework.data.web.PagedModel<CartItemResponse>> getSavedItems(
            @PathVariable UUID cartId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get saved items request received for cart: {}", cartId);

        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.fromString(sortDir), sortBy);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);

        org.springframework.data.web.PagedModel<CartItemResponse> items = cartItemService.getSavedItems(cartId, pageable);

        log.info("Retrieved {} saved items for cart", items.getContent().size());
        return ResponseEntity.ok(items);
    }

    // Save item for later
    @PostMapping("/cart-items/{itemId}/save-for-later")
    @Operation(summary = "Save an item for later")
    public ResponseEntity<CartItemResponse> saveItemForLater(
            @PathVariable UUID itemId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Save item for later request received: {}", itemId);

        CartItemResponse item = cartItemService.saveItemForLater(itemId);

        log.info("Item saved for later: {}", itemId);
        return ResponseEntity.ok(item);
    }

    // Move item to cart
    @PostMapping("/cart-items/{itemId}/move-to-cart")
    @Operation(summary = "Move a saved item back to the cart")
    public ResponseEntity<CartItemResponse> moveItemToCart(
            @PathVariable UUID itemId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Move item to cart request received: {}", itemId);

        CartItemResponse item = cartItemService.moveItemToCart(itemId);

        log.info("Item moved to cart: {}", itemId);
        return ResponseEntity.ok(item);
    }

    // --- Endpoints migrated from CartController (/api/carts) ---

    // Add item to cart
    @PostMapping("/carts/{cartId}/items")
    @Operation(summary = "Add an item to a cart")
    public ResponseEntity<CartResponse> addItemToCart(
            @PathVariable UUID cartId,
            @Valid @RequestBody AddItemRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Add item to cart request received: {} product: {}", cartId, request.getProductId());

        if (!canAccessCart(cartId, userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponse cart = cartService.addItemToCart(cartId, request);

        log.info("Item added to cart successfully: {}", cartId);
        return ResponseEntity.ok(cart);
    }

    // Add item to 'me' cart
    @PostMapping("/carts/me/items")
    @Operation(summary = "Add an item to 'me' cart")
    public ResponseEntity<CartResponse> addItemToMyCart(
            @Valid @RequestBody AddItemRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Add item to 'me' cart request received: product: {}", request.getProductId());

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

        CartResponse updatedCart = cartService.addItemToCart(cartInfo.getId(), request);

        log.info("Item added to 'me' cart successfully: {}", updatedCart.getId());
        return ResponseEntity.ok(updatedCart);
    }

    // Update item in cart
    @PutMapping("/carts/{cartId}/items/{itemId}")
    @Operation(summary = "Update an item in a cart")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable UUID cartId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateItemRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Update cart item request received: {} item: {}", cartId, itemId);

        if (!canAccessCart(cartId, userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponse cart = cartService.updateCartItem(cartId, itemId, request);

        log.info("Cart item updated successfully: {}", itemId);
        return ResponseEntity.ok(cart);
    }

    // Remove item from cart
    @DeleteMapping("/carts/{cartId}/items/{itemId}")
    @Operation(summary = "Remove an item from a cart")
    public ResponseEntity<CartResponse> removeItemFromCart(
            @PathVariable UUID cartId,
            @PathVariable UUID itemId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Remove item from cart request received: {} item: {}", cartId, itemId);

        if (!canAccessCart(cartId, userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponse cart = cartService.removeItemFromCart(cartId, itemId);

        log.info("Item removed from cart successfully: {}", itemId);
        return ResponseEntity.ok(cart);
    }

    // Clear cart
    @DeleteMapping("/carts/{cartId}/items")
    @Operation(summary = "Clear all items from a cart")
    public ResponseEntity<CartResponse> clearCart(
            @PathVariable UUID cartId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Clear cart request received: {}", cartId);

        if (!canAccessCart(cartId, userId, userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartResponse cart = cartService.clearCart(cartId);

        log.info("Cart cleared successfully: {}", cartId);
        return ResponseEntity.ok(cart);
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
