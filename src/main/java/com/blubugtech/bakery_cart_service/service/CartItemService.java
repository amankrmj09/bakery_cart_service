package com.blubugtech.bakery_cart_service.service;

import com.blubugtech.bakery_cart_service.dto.cartitem.*;
import com.blubugtech.bakery_cart_service.entity.Cart;
import com.blubugtech.bakery_cart_service.entity.CartItem;
import java.util.List;
import java.util.UUID;

public interface CartItemService {
    CartItemResponse addItemToCart(Cart cart, AddItemRequest request);
    CartItemResponse updateCartItem(UUID itemId, UpdateItemRequest request);
    CartItemResponse updateItemQuantity(UUID itemId, Integer newQuantity);
    void removeItemFromCart(UUID itemId);
    CartItemResponse saveItemForLater(UUID itemId);
    CartItemResponse moveItemToCart(UUID itemId);
    org.springframework.data.web.PagedModel<CartItemResponse> getCartItems(UUID cartId, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.web.PagedModel<CartItemResponse> getSavedItems(UUID cartId, org.springframework.data.domain.Pageable pageable);
    CartItemResponse getCartItemById(UUID itemId);
    void validateCartItems(List<CartItem> items);
}
