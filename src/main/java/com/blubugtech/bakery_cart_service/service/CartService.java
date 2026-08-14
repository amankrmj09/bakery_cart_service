package com.blubugtech.bakery_cart_service.service;

import com.blubugtech.bakery_cart_service.dto.cart.*;
import com.blubugtech.bakery_cart_service.dto.checkout.*;
import com.blubugtech.bakery_cart_service.dto.cartitem.*;
import com.blubugtech.bakery_cart_service.entity.Cart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.blubakery.common.core.dto.RestPageResponse;
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
    RestPageResponse<CartResponse> getUserCarts(UUID userId, Pageable pageable);
    RestPageResponse<CartResponse> getCartsByStatus(Cart.CartStatus status, Pageable pageable);
    RestPageResponse<CartResponse> getAllCarts(Pageable pageable);
    com.blubugtech.bakery_cart_service.dto.CartStatisticsResponse getCartStatistics(LocalDateTime startDate, LocalDateTime endDate);
    
}
