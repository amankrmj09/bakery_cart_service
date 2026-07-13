package com.shah_s.bakery_cart_service.controller;

import com.shah_s.bakery_cart_service.dto.CartItemResponseDto;
import com.shah_s.bakery_cart_service.entity.CartItem;
import com.shah_s.bakery_cart_service.service.CartItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart-items")

public class CartItemController {

    private static final Logger logger = LoggerFactory.getLogger(CartItemController.class);

    @Autowired
    private CartItemService cartItemService;

    // Get cart item by ID
    @GetMapping("/{itemId}")
    public ResponseEntity<CartItemResponseDto> getCartItemById(
            @PathVariable UUID itemId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get cart item by ID request received: {}", itemId);

        CartItemResponseDto item = cartItemService.getCartItemById(itemId);

        // Basic access control - could be enhanced with cart ownership check
        logger.info("Cart item retrieved: {}", itemId);
        return ResponseEntity.ok(item);
    }

    // Get items for cart
    @GetMapping("/cart/{cartId}")
    public ResponseEntity<List<CartItemResponseDto>> getCartItems(
            @PathVariable UUID cartId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get cart items request received for cart: {}", cartId);

        List<CartItemResponseDto> items = cartItemService.getCartItems(cartId);

        logger.info("Retrieved {} items for cart", items.size());
        return ResponseEntity.ok(items);
    }

    // Get saved items for cart
    @GetMapping("/cart/{cartId}/saved")
    public ResponseEntity<List<CartItemResponseDto>> getSavedItems(
            @PathVariable UUID cartId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get saved items request received for cart: {}", cartId);

        List<CartItemResponseDto> items = cartItemService.getSavedItems(cartId);

        logger.info("Retrieved {} saved items for cart", items.size());
        return ResponseEntity.ok(items);
    }

    // Save item for later
    @PostMapping("/{itemId}/save-for-later")
    public ResponseEntity<CartItemResponseDto> saveItemForLater(
            @PathVariable UUID itemId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Save item for later request received: {}", itemId);

        CartItemResponseDto item = cartItemService.saveItemForLater(itemId);

        logger.info("Item saved for later: {}", itemId);
        return ResponseEntity.ok(item);
    }

    // Move item to cart
    @PostMapping("/{itemId}/move-to-cart")
    public ResponseEntity<CartItemResponseDto> moveItemToCart(
            @PathVariable UUID itemId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Move item to cart request received: {}", itemId);

        CartItemResponseDto item = cartItemService.moveItemToCart(itemId);

        logger.info("Item moved to cart: {}", itemId);
        return ResponseEntity.ok(item);
    }

    // Health check
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "cart-service-items");
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }
}
