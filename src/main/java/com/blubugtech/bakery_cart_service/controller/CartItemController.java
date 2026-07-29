package com.blubugtech.bakery_cart_service.controller;

import lombok.extern.slf4j.Slf4j;
import com.blubugtech.bakery_cart_service.dto.cartitem.CartItemResponse;
import com.blubugtech.bakery_cart_service.entity.CartItem;
import com.blubugtech.bakery_cart_service.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart-items")
@Tag(name = "Cart Items", description = "Endpoints for managing items within a cart")
@RequiredArgsConstructor
@Slf4j
public class CartItemController {

    private final CartItemService cartItemService;

    // Get cart item by ID
    @GetMapping("/{itemId}")
    @Operation(summary = "Get cart item by ID")
    public ResponseEntity<CartItemResponse> getCartItemById(
            @PathVariable UUID itemId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get cart item by ID request received: {}", itemId);

        CartItemResponse item = cartItemService.getCartItemById(itemId);

        // Basic access control - could be enhanced with cart ownership check
        log.info("Cart item retrieved: {}", itemId);
        return ResponseEntity.ok(item);
    }

    // Get items for cart
    @GetMapping("/cart/{cartId}")
    @Operation(summary = "Get all items in a cart")
    public ResponseEntity<List<CartItemResponse>> getCartItems(
            @PathVariable UUID cartId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get cart items request received for cart: {}", cartId);

        List<CartItemResponse> items = cartItemService.getCartItems(cartId);

        log.info("Retrieved {} items for cart", items.size());
        return ResponseEntity.ok(items);
    }

    // Get saved items for cart
    @GetMapping("/cart/{cartId}/saved")
    @Operation(summary = "Get saved items for a cart")
    public ResponseEntity<List<CartItemResponse>> getSavedItems(
            @PathVariable UUID cartId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get saved items request received for cart: {}", cartId);

        List<CartItemResponse> items = cartItemService.getSavedItems(cartId);

        log.info("Retrieved {} saved items for cart", items.size());
        return ResponseEntity.ok(items);
    }

    // Save item for later
    @PostMapping("/{itemId}/save-for-later")
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
    @PostMapping("/{itemId}/move-to-cart")
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


}
