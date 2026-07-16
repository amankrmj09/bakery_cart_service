package com.blubugtech.bakery_cart_service.service.impl;
import com.blubugtech.bakery_cart_service.service.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.blubugtech.bakery_cart_service.gateway.ProductGateway;
import com.blubugtech.bakery_cart_service.dto.cartitem.AddItemRequest;
import com.blubugtech.bakery_cart_service.dto.cartitem.CartItemResponse;
import com.blubugtech.bakery_cart_service.mapper.CartItemMapper;
import com.blubugtech.bakery_cart_service.dto.cartitem.UpdateItemRequest;
import com.blubugtech.common.contract.feign.Product;
import com.blubugtech.common.contract.feign.StockAvailability;
import com.blubugtech.common.contract.feign.ProductValidation;
import com.blubugtech.bakery_cart_service.entity.Cart;
import com.blubugtech.bakery_cart_service.entity.CartItem;
import com.blubugtech.bakery_cart_service.exception.CartServiceException;
import com.blubugtech.bakery_cart_service.repository.CartItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartItemServiceImpl implements CartItemService {

    private static final Logger logger = LoggerFactory.getLogger(CartItemService.class);

    final private CartItemRepository cartItemRepository;

    final private ProductGateway productGateway;
    final private CartItemMapper cartItemMapper;

    final private ObjectMapper objectMapper;

    @Value("${cart.validation.check-stock-on-add:true}")
    private Boolean checkStockOnAdd;

    @Value("${cart.limits.max-quantity-per-item:50}")
    private Integer maxQuantityPerItem;

    public CartItemServiceImpl(CartItemRepository cartItemRepository, ProductGateway productGateway, ObjectMapper objectMapper, CartItemMapper cartItemMapper) {
        this.cartItemRepository = cartItemRepository;
        this.productGateway = productGateway;
        this.objectMapper = objectMapper;
        this.cartItemMapper = cartItemMapper;
    }

    // Add item to cart
    public CartItemResponse addItemToCart(Cart cart, AddItemRequest request) {
        logger.info("Adding item to cart: {} product: {}", cart.getId(), request.getProductId());

        try {
            // Get product information
            Product productInfo = productGateway.getProductById(request.getProductId());
            if (productInfo == null) {
                throw new CartServiceException("Product not found: " + request.getProductId());
            }

            // Validate stock if enabled
            if (checkStockOnAdd) {
                validateStock(request.getProductId(), request.getQuantity());
            }

            // Create cart item
            CartItem cartItem = createCartItemFromProduct(cart, productInfo, request);
            cart.addItem(cartItem);

            CartItem savedItem = cartItemRepository.save(cartItem);
            logger.info("Item added to cart successfully: {}", savedItem.getId());

            return cartItemMapper.toDto(savedItem);

        } catch (Exception e) {
            logger.error("Failed to add item to cart: {}", e.getMessage());
            throw new CartServiceException("Failed to add item to cart: " + e.getMessage());
        }
    }

    // Update cart item
    @CacheEvict(value = "cart-items", key = "#itemId")
    public CartItemResponse updateCartItem(UUID itemId, UpdateItemRequest request) {
        logger.info("Updating cart item: {} quantity: {}", itemId, request.getQuantity());

        try {
            CartItem cartItem = cartItemRepository.findById(itemId)
                    .orElseThrow(() -> new CartServiceException("Cart item not found with ID: " + itemId));

            // Validate new quantity
            if (request.getQuantity() > maxQuantityPerItem) {
                throw new CartServiceException("Maximum quantity per item exceeded: " + maxQuantityPerItem);
            }

            // Validate stock if enabled
            if (checkStockOnAdd) {
                validateStock(cartItem.getProductId(), request.getQuantity());
            }

            cartItem.setQuantity(request.getQuantity());
            if (request.getSpecialInstructions() != null) {
                cartItem.setSpecialInstructions(request.getSpecialInstructions());
            }
            if (request.getMetadata() != null) {
                cartItem.setMetadata(convertMetadataToJson(request.getMetadata()));
            }

            cartItem.calculateTotalPrice();
            CartItem updatedItem = cartItemRepository.save(cartItem);

            // Update cart totals
            Cart cart = cartItem.getCart();
            cart.updateTotals();

            logger.info("Cart item updated successfully: {}", itemId);
            return cartItemMapper.toDto(updatedItem);

        } catch (Exception e) {
            logger.error("Failed to update cart item {}: {}", itemId, e.getMessage());
            throw new CartServiceException("Failed to update cart item: " + e.getMessage());
        }
    }

    // Update item quantity
    public CartItemResponse updateItemQuantity(UUID itemId, Integer newQuantity) {
        UpdateItemRequest request = new UpdateItemRequest(newQuantity);
        return updateCartItem(itemId, request);
    }

    // Remove item from cart
    @CacheEvict(value = "cart-items", key = "#itemId")
    public void removeItemFromCart(UUID itemId) {
        logger.info("Removing item from cart: {}", itemId);

        try {
            CartItem cartItem = cartItemRepository.findById(itemId)
                    .orElseThrow(() -> new CartServiceException("Cart item not found with ID: " + itemId));

            cartItem.remove();
            cartItemRepository.save(cartItem);

            // Update cart totals
            cartItem.getCart().updateTotals();

            logger.info("Item removed from cart successfully: {}", itemId);

        } catch (Exception e) {
            logger.error("Failed to remove item from cart {}: {}", itemId, e.getMessage());
            throw new CartServiceException("Failed to remove item from cart: " + e.getMessage());
        }
    }

    // Save item for later
    @CacheEvict(value = "cart-items", key = "#itemId")
    public CartItemResponse saveItemForLater(UUID itemId) {
        logger.info("Saving item for later: {}", itemId);

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new CartServiceException("Cart item not found with ID: " + itemId));

        cartItem.saveForLater();
        CartItem savedItem = cartItemRepository.save(cartItem);

        // Update cart totals
        cartItem.getCart().updateTotals();

        return cartItemMapper.toDto(savedItem);
    }

    // Move item to cart
    @CacheEvict(value = "cart-items", key = "#itemId")
    public CartItemResponse moveItemToCart(UUID itemId) {
        logger.info("Moving item to cart: {}", itemId);

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new CartServiceException("Cart item not found with ID: " + itemId));

        cartItem.moveToCart();
        CartItem movedItem = cartItemRepository.save(cartItem);

        // Update cart totals
        cartItem.getCart().updateTotals();

        return cartItemMapper.toDto(movedItem);
    }

    // Get cart items
    @Cacheable(value = "cart-items", key = "#cartId")
    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartItems(UUID cartId) {
        logger.debug("Fetching items for cart: {}", cartId);

        return cartItemRepository.findActiveItemsByCartId(cartId).stream()
                .map(cartItemMapper::toDto)
                .collect(Collectors.toList());
    }

    // Get saved items
    @Transactional(readOnly = true)
    public List<CartItemResponse> getSavedItems(UUID cartId) {
        logger.debug("Fetching saved items for cart: {}", cartId);

        return cartItemRepository.findSavedItemsByCartId(cartId).stream()
                .map(cartItemMapper::toDto)
                .collect(Collectors.toList());
    }

    // Get item by ID
    @Transactional(readOnly = true)
    public CartItemResponse getCartItemById(UUID itemId) {
        logger.debug("Fetching cart item by ID: {}", itemId);

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new CartServiceException("Cart item not found with ID: " + itemId));

        return cartItemMapper.toDto(cartItem);
    }

    // Validate cart items
    @Async
    public void validateCartItems(List<CartItem> items) {
        logger.debug("Validating {} cart items", items.size());

        try {
            List<UUID> productIds = items.stream()
                    .map(CartItem::getProductId)
                    .collect(Collectors.toList());

            List<ProductValidation> validations = productGateway.validateProducts(productIds);

            // Update items based on validation results
            for (int i = 0; i < items.size() && i < validations.size(); i++) {
                CartItem item = items.get(i);
                ProductValidation validation = validations.get(i);

                updateItemFromValidation(item, validation);
            }

        } catch (Exception e) {
            logger.warn("Failed to validate cart items: {}", e.getMessage());
        }
    }

    // Private helper methods
    private CartItem createCartItemFromProduct(Cart cart, Product productInfo, AddItemRequest request) {
        String productName = productInfo.getName();
        BigDecimal unitPrice = request.getUnitPriceOverride() != null ?
                request.getUnitPriceOverride() :
                (productInfo.getEffectivePrice() != null ? productInfo.getEffectivePrice() : BigDecimal.ZERO);

        CartItem cartItem = new CartItem(cart, request.getProductId(), productName,
                request.getQuantity(), unitPrice);

        // Set additional product information
        cartItem.setProductSku(productInfo.getSku());
        cartItem.setProductCategory(productInfo.getCategory() != null ? productInfo.getCategory().getName() : null);
        cartItem.setProductDescription(productInfo.getDescription());
        
        String imageUrl = productInfo.getPrimaryImageUrl();
        if (imageUrl == null && productInfo.getMediaUrls() != null && !productInfo.getMediaUrls().isEmpty()) {
            imageUrl = productInfo.getMediaUrls().get(0);
        }
        cartItem.setProductImageUrl(imageUrl);
        
        cartItem.setPreparationTimeMinutes(productInfo.getPreparationTimeMinutes() != null ? productInfo.getPreparationTimeMinutes() : 30);
        cartItem.setSpecialInstructions(request.getSpecialInstructions());
        cartItem.setAddedFrom(request.getAddedFrom());

        if (request.getMetadata() != null) {
            cartItem.setMetadata(convertMetadataToJson(request.getMetadata()));
        }

        return cartItem;
    }

    private void validateStock(UUID productId, Integer quantity) {
        try {
            StockAvailability stockInfo = productGateway.checkStockAvailability(productId, quantity);
            Boolean sufficient = stockInfo.getSufficient();

            if (sufficient == null || !sufficient) {
                throw new CartServiceException("Insufficient stock for product: " + productId);
            }
        } catch (Exception e) {
            logger.warn("Stock validation failed for product {}: {}", productId, e.getMessage());
            // Don't fail the operation, just log the warning
        }
    }

    private void updateItemFromValidation(CartItem item, ProductValidation validation) {
        try {
            // Update availability
            Boolean isAvailable = validation.getAvailable();
            item.setIsAvailable(isAvailable != null ? isAvailable : true);

            // Update stock quantity
            Integer stockQuantity = validation.getStockQuantity();
            item.setStockQuantity(stockQuantity);

            // Update price if changed
            BigDecimal currentPrice = validation.getCurrentPrice();
            if (currentPrice != null && !currentPrice.equals(item.getUnitPrice())) {
                item.setUnitPrice(currentPrice);
                item.checkPriceChange();
            }

            item.updateValidation();
            cartItemRepository.save(item);

        } catch (Exception e) {
            logger.warn("Failed to update item from validation: {}", e.getMessage());
        }
    }



    private String convertMetadataToJson(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            logger.warn("Failed to convert metadata to JSON: {}", e.getMessage());
            return "{}";
        }
    }
}
