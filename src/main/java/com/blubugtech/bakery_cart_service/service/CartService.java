package com.blubugtech.bakery_cart_service.service;

import com.blubugtech.bakery_cart_service.dto.cart.*;
import com.blubugtech.bakery_cart_service.dto.checkout.*;
import com.blubugtech.bakery_cart_service.dto.cartitem.*;
import com.blubugtech.bakery_cart_service.entity.Cart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CartService {
    CartResponse createCart(CartRequest request);
    CartResponse getCartById(UUID cartId);
    CartResponse getOrCreateCartForUser(UUID userId);
    CartResponse getOrCreateCartForSession(String sessionId);
    CartResponse addItemToCart(UUID cartId, AddItemRequest request);
    CartResponse updateCartItem(UUID cartId, UUID itemId, UpdateItemRequest request);
    CartResponse removeItemFromCart(UUID cartId, UUID itemId);
    CartResponse clearCart(UUID cartId);
    CartResponse updateCart(UUID cartId, CartUpdateRequest request);
    CartResponse mergeCarts(MergeCartsRequest request);
    CartResponse saveCartForLater(UUID cartId);
    CheckoutResponse checkoutCart(UUID cartId, CheckoutRequest request);
    List<CartResponse> getUserCarts(UUID userId);
    List<CartResponse> getCartsByStatus(Cart.CartStatus status);
    Page<CartResponse> getAllCarts(Pageable pageable);
    Map<String, Object> getCartStatistics(LocalDateTime startDate, LocalDateTime endDate);
    
    static CartResponse convertIfMap(Object obj, ObjectMapper objectMapper) {
        if (obj instanceof CartResponse) {
            return (CartResponse) obj;
        }
        if (obj instanceof Map) {
            return objectMapper.convertValue(obj, CartResponse.class);
        }
        throw new IllegalArgumentException("Unknown type for cart: " + obj.getClass());
    }
}
