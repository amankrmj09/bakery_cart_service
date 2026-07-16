package com.blubugtech.bakery_cart_service.service.impl;
import com.blubugtech.bakery_cart_service.service.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.blubugtech.bakery_cart_service.gateway.OrderGateway;
import com.blubugtech.bakery_cart_service.gateway.ProductGateway;
import com.blubugtech.bakery_cart_service.dto.cart.*;
import com.blubugtech.bakery_cart_service.dto.cartitem.*;
import com.blubugtech.bakery_cart_service.dto.checkout.*;
import com.blubugtech.bakery_cart_service.dto.checkout.CheckoutResponse;
import com.blubugtech.bakery_cart_service.dto.order.CreateOrderRequest;
import com.blubugtech.bakery_cart_service.dto.order.OrderResponse;
import com.blubugtech.common.contract.feign.ProductValidation;
import com.blubugtech.bakery_cart_service.entity.Cart;
import com.blubugtech.bakery_cart_service.mapper.CartMapper;
import com.blubugtech.bakery_cart_service.entity.CartItem;
import com.blubugtech.bakery_cart_service.exception.CartServiceException;
import com.blubugtech.bakery_cart_service.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private ProductGateway productGateway;

    @Autowired
    private OrderGateway orderGateway;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CartMapper cartMapper;

    @Value("${cart.limits.max-items-per-cart:100}")
    private Integer maxItemsPerCart;

    @Value("${cart.limits.max-quantity-per-item:50}")
    private Integer maxQuantityPerItem;

    @Value("${cart.limits.max-cart-value:2000.00}")
    private BigDecimal maxCartValue;

    @Value("${cart.validation.check-stock-on-add:true}")
    private Boolean checkStockOnAdd;

    @Value("${cart.validation.check-price-on-view:true}")
    private Boolean checkPriceOnView;

    // Create or get cart
    public CartResponse createCart(CartRequest request) {
        logger.info("Creating cart for user: {} session: {}", request.getUserId(), request.getSessionId());

        try {
            // Check if cart already exists
            Optional<Cart> existingCart = findExistingCart(request.getUserId(), request.getSessionId());
            if (existingCart.isPresent()) {
                Cart cart = existingCart.get();
                cart.updateActivity();
                return cartMapper.toDto(cartRepository.save(cart));
            }

            // Create new cart
            Cart cart = new Cart(request.getUserId(), request.getSessionId());
            cart.setCustomerName(request.getCustomerName());
            cart.setCustomerEmail(request.getCustomerEmail());
            cart.setCurrencyCode(request.getCurrencyCode());
            cart.setDiscountCode(request.getDiscountCode());
            cart.setSpecialInstructions(request.getSpecialInstructions());
            cart.setDeliveryType(request.getDeliveryType());
            cart.setDeliveryAddress(request.getDeliveryAddress());
            cart.setSource(request.getSource());
            cart.setDeviceType(request.getDeviceType());
            cart.setUserAgent(request.getUserAgent());

            if (request.getMetadata() != null) {
                cart.setMetadata(convertMetadataToJson(request.getMetadata()));
            }

            Cart savedCart = cartRepository.save(cart);
            logger.info("Cart created successfully: {}", savedCart.getId());

            return cartMapper.toDto(savedCart);

        } catch (Exception e) {
            logger.error("Failed to create cart: {}", e.getMessage());
            throw new CartServiceException("Failed to create cart: " + e.getMessage());
        }
    }

    // Get cart by ID
    @Cacheable(value = "carts", key = "#cartId")
    @Transactional(readOnly = true)
    public CartResponse getCartById(UUID cartId) {
        logger.debug("Fetching cart by ID: {}", cartId);
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartServiceException("Cart not found with ID: " + cartId));
        if (checkPriceOnView) {
            validateCartItems(cart);
        }
        CartResponse response = cartMapper.toDto(cart);
        return convertIfMap(response, objectMapper);
    }

    // Utility to convert LinkedHashMap to CartResponse
    public static CartResponse convertIfMap(Object obj, ObjectMapper objectMapper) {
        if (obj instanceof java.util.LinkedHashMap) {
            try {
                // Remove '@class' field if present
                Map<?, ?> map = (Map<?, ?>) obj;
                if (map.containsKey("@class")) {
                    Map<Object, Object> cleaned = new java.util.LinkedHashMap<>(map);
                    cleaned.remove("@class");
                    return objectMapper.convertValue(cleaned, CartResponse.class);
                }
                return objectMapper.convertValue(obj, CartResponse.class);
            } catch (Exception e) {
                LoggerFactory.getLogger(CartService.class).error("Failed to convert cached map to CartResponse", e);
                return null;
            }
        }
        return (CartResponse) obj;
    }

    // Get or create cart for user
    @Cacheable(value = "carts", key = "'user-' + #userId")
    public CartResponse getOrCreateCartForUser(UUID userId) {
        logger.debug("Getting or creating cart for user: {}", userId);
        Optional<Cart> existingCart = cartRepository.findActiveCartByUserId(userId);
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            if (checkPriceOnView) {
                validateCartItems(cart);
            }
            CartResponse response = cartMapper.toDto(cart);
            return convertIfMap(response, objectMapper);
        }
        CartRequest request = new CartRequest(userId, null);
        CartResponse response = createCart(request);
        return convertIfMap(response, objectMapper);
    }

    // Get or create cart for session
    @Cacheable(value = "carts", key = "'session-' + #sessionId")
    public CartResponse getOrCreateCartForSession(String sessionId) {
        logger.debug("Getting or creating cart for session: {}", sessionId);
        Optional<Cart> existingCart = cartRepository.findActiveCartBySessionId(sessionId);
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            if (checkPriceOnView) {
                validateCartItems(cart);
            }
            CartResponse response = cartMapper.toDto(cart);
            return convertIfMap(response, objectMapper);
        }
        CartRequest request = new CartRequest(sessionId);
        CartResponse response = createCart(request);
        return convertIfMap(response, objectMapper);
    }

    // Add item to cart
    @CacheEvict(value = "carts", allEntries = true)
    public CartResponse addItemToCart(UUID cartId, AddItemRequest request) {
        logger.info("Adding item to cart: {} product: {} quantity: {}",
                cartId, request.getProductId(), request.getQuantity());

        try {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new CartServiceException("Cart not found with ID: " + cartId));

            // Validate cart limits
            validateCartLimits(cart, request.getQuantity());

            // Check if item already exists in cart
            CartItem existingItem = cart.findItemByProductId(request.getProductId());
            if (existingItem != null) {
                // Update existing item quantity
                int newQuantity = existingItem.getQuantity() + request.getQuantity();
                if (newQuantity > maxQuantityPerItem) {
                    throw new CartServiceException("Maximum quantity per item exceeded: " + maxQuantityPerItem);
                }

                cartItemService.updateItemQuantity(existingItem.getId(), newQuantity);
            } else {
                // Add new item
                cartItemService.addItemToCart(cart, request);
            }

            // Refresh cart
            Cart updatedCart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new CartServiceException("Cart not found after update"));

            logger.info("Item added to cart successfully: {}", cartId);
            return cartMapper.toDto(updatedCart);

        } catch (Exception e) {
            logger.error("Failed to add item to cart {}: {}", cartId, e.getMessage());
            throw new CartServiceException("Failed to add item to cart: " + e.getMessage());
        }
    }

    // Update item in cart
    @CacheEvict(value = "carts", allEntries = true)
    public CartResponse updateCartItem(UUID cartId, UUID itemId, UpdateItemRequest request) {
        logger.info("Updating cart item: {} in cart: {}", itemId, cartId);

        try {
            cartItemService.updateCartItem(itemId, request);

            Cart updatedCart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new CartServiceException("Cart not found after update"));

            logger.info("Cart item updated successfully: {}", itemId);
            return cartMapper.toDto(updatedCart);

        } catch (Exception e) {
            logger.error("Failed to update cart item {}: {}", itemId, e.getMessage());
            throw new CartServiceException("Failed to update cart item: " + e.getMessage());
        }
    }

    // Remove item from cart
    @CacheEvict(value = "carts", allEntries = true)
    public CartResponse removeItemFromCart(UUID cartId, UUID itemId) {
        logger.info("Removing item from cart: {} item: {}", cartId, itemId);

        try {
            cartItemService.removeItemFromCart(itemId);

            Cart updatedCart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new CartServiceException("Cart not found after update"));

            logger.info("Item removed from cart successfully: {}", itemId);
            return cartMapper.toDto(updatedCart);

        } catch (Exception e) {
            logger.error("Failed to remove item from cart {}: {}", cartId, e.getMessage());
            throw new CartServiceException("Failed to remove item from cart: " + e.getMessage());
        }
    }

    // Clear cart
    @CacheEvict(value = "carts", allEntries = true)
    public CartResponse clearCart(UUID cartId) {
        logger.info("Clearing cart: {}", cartId);

        try {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new CartServiceException("Cart not found with ID: " + cartId));

            cart.clearItems();
            Cart clearedCart = cartRepository.save(cart);

            logger.info("Cart cleared successfully: {}", cartId);
            return cartMapper.toDto(clearedCart);

        } catch (Exception e) {
            logger.error("Failed to clear cart {}: {}", cartId, e.getMessage());
            throw new CartServiceException("Failed to clear cart: " + e.getMessage());
        }
    }

    // Update cart details
    @CacheEvict(value = "carts", allEntries = true)
    public CartResponse updateCart(UUID cartId, CartUpdateRequest request) {
        logger.info("Updating cart: {}", cartId);

        try {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new CartServiceException("Cart not found with ID: " + cartId));

            // Update cart fields
            if (request.getCustomerName() != null) {
                cart.setCustomerName(request.getCustomerName());
            }
            if (request.getCustomerEmail() != null) {
                cart.setCustomerEmail(request.getCustomerEmail());
            }
            if (request.getDiscountCode() != null) {
                cart.setDiscountCode(request.getDiscountCode());
                // TODO: Apply discount logic
            }
            if (request.getSpecialInstructions() != null) {
                cart.setSpecialInstructions(request.getSpecialInstructions());
            }
            if (request.getDeliveryType() != null) {
                cart.setDeliveryType(request.getDeliveryType());
            }
            if (request.getDeliveryAddress() != null) {
                cart.setDeliveryAddress(request.getDeliveryAddress());
            }
            if (request.getMetadata() != null) {
                cart.setMetadata(convertMetadataToJson(request.getMetadata()));
            }

            cart.updateActivity();
            Cart updatedCart = cartRepository.save(cart);

            logger.info("Cart updated successfully: {}", cartId);
            return cartMapper.toDto(updatedCart);

        } catch (Exception e) {
            logger.error("Failed to update cart {}: {}", cartId, e.getMessage());
            throw new CartServiceException("Failed to update cart: " + e.getMessage());
        }
    }

    // Merge carts (for user login)
    @CacheEvict(value = "carts", allEntries = true)
    public CartResponse mergeCarts(MergeCartsRequest request) {
        logger.info("Merging carts: {} -> {}", request.getSourceCartId(), request.getTargetCartId());

        try {
            Cart sourceCart = cartRepository.findById(request.getSourceCartId())
                    .orElseThrow(() -> new CartServiceException("Source cart not found"));

            Cart targetCart = cartRepository.findById(request.getTargetCartId())
                    .orElseThrow(() -> new CartServiceException("Target cart not found"));

            // Merge items
            for (CartItem sourceItem : sourceCart.getActiveItems()) {
                CartItem targetItem = targetCart.findItemByProductId(sourceItem.getProductId());

                if (targetItem != null && request.getHandleDuplicates()) {
                    // Merge quantities
                    int newQuantity = Math.min(targetItem.getQuantity() + sourceItem.getQuantity(), maxQuantityPerItem);
                    targetItem.setQuantity(newQuantity);
                } else if (targetItem == null) {
                    // Add new item
                    CartItem newItem = new CartItem(targetCart, sourceItem.getProductId(),
                            sourceItem.getProductName(), sourceItem.getQuantity(),
                            sourceItem.getUnitPrice());
                    newItem.setProductSku(sourceItem.getProductSku());
                    newItem.setProductCategory(sourceItem.getProductCategory());
                    newItem.setProductDescription(sourceItem.getProductDescription());
                    newItem.setProductImageUrl(sourceItem.getProductImageUrl());
                    newItem.setSpecialInstructions(sourceItem.getSpecialInstructions());
                    targetCart.addItem(newItem);
                }
            }

            // Update target cart information from source if target is empty
            if (targetCart.getCustomerName() == null && sourceCart.getCustomerName() != null) {
                targetCart.setCustomerName(sourceCart.getCustomerName());
            }
            if (targetCart.getCustomerEmail() == null && sourceCart.getCustomerEmail() != null) {
                targetCart.setCustomerEmail(sourceCart.getCustomerEmail());
            }

            targetCart.updateActivity();
            Cart mergedCart = cartRepository.save(targetCart);

            // Delete source cart if requested
            if (request.getDeleteSourceCart()) {
                cartRepository.delete(sourceCart);
            }

            logger.info("Carts merged successfully: {}", request.getTargetCartId());
            return cartMapper.toDto(mergedCart);

        } catch (Exception e) {
            logger.error("Failed to merge carts: {}", e.getMessage());
            throw new CartServiceException("Failed to merge carts: " + e.getMessage());
        }
    }

    // Save cart for later
    @CacheEvict(value = "carts", key = "#cartId")
    public CartResponse saveCartForLater(UUID cartId) {
        logger.info("Saving cart for later: {}", cartId);

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartServiceException("Cart not found with ID: " + cartId));

        cart.markAsSaved();
        Cart savedCart = cartRepository.save(cart);

        return cartMapper.toDto(savedCart);
    }

    // Checkout cart
    @CacheEvict(value = "carts", allEntries = true)
    public CheckoutResponse checkoutCart(UUID cartId, CheckoutRequest request) {
        logger.info("Checking out cart: {}", cartId);

        try {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new CartServiceException("Cart not found with ID: " + cartId));

            if (cart.isEmpty()) {
                throw new CartServiceException("Cannot checkout empty cart");
            }

            // Validate all items before checkout
            validateCartItems(cart);

            // Create order request
            CreateOrderRequest orderRequest = createOrderRequest(cart, request);

            // Call Order Service
            OrderResponse orderResponse = orderGateway.createOrder(orderRequest,
                    cart.getUserId() != null ? cart.getUserId().toString() : null, "USER");

            // Mark cart as converted
            cart.markAsConverted(orderResponse.getId());
            cartRepository.save(cart);

            logger.info("Cart checked out successfully: {} -> Order: {}", cartId, orderResponse.getId());

            return CheckoutResponse.builder()
                    .cart(cartMapper.toDto(cart))
                    .order(orderResponse)
                    .build();

        } catch (Exception e) {
            logger.error("Failed to checkout cart {}: {}", cartId, e.getMessage());
            throw new CartServiceException("Failed to checkout cart: " + e.getMessage());
        }
    }

    // Get user carts
    @Transactional(readOnly = true)
    public List<CartResponse> getUserCarts(UUID userId) {
        logger.debug("Fetching carts for user: {}", userId);

        return cartRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(cartMapper::toDto)
                .collect(Collectors.toList());
    }

    // Get carts by status
    @Transactional(readOnly = true)
    public List<CartResponse> getCartsByStatus(Cart.CartStatus status) {
        logger.debug("Fetching carts by status: {}", status);

        return cartRepository.findByStatusOrderByUpdatedAtDesc(status).stream()
                .map(cartMapper::toDto)
                .collect(Collectors.toList());
    }

    // Get all carts with pagination
    @Transactional(readOnly = true)
    public Page<CartResponse> getAllCarts(Pageable pageable) {
        logger.debug("Fetching all carts with pagination");

        return cartRepository.findAll(pageable)
                .map(cartMapper::toDto);
    }

    // Get cart statistics
    @Cacheable(value = "cart-stats", key = "#startDate + '-' + #endDate")
    @Transactional(readOnly = true)
    public Map<String, Object> getCartStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Fetching cart statistics");

        try {
            Object[] stats = cartRepository.getCartStatistics(startDate, endDate);
            Object[] conversionRate = cartRepository.getCartConversionRate(startDate, endDate);
            List<Object[]> dailyStats = cartRepository.getDailyCartStatistics(startDate, endDate);
            List<Object[]> sourceStats = cartRepository.getCartStatisticsBySource(startDate, endDate);

            return Map.of(
                    "totalCarts", stats[0],
                    "activeCarts", stats[1],
                    "abandonedCarts", stats[2],
                    "convertedCarts", stats[3],
                    "averageCartValue", stats[4],
                    "averageItemCount", stats[5],
                    "conversionRate", calculateConversionRate(conversionRate),
                    "dailyStats", dailyStats,
                    "sourceStats", sourceStats,
                    "dateRange", Map.of(
                            "startDate", startDate.toString(),
                            "endDate", endDate.toString()
                    )
            );
        } catch (Exception e) {
            logger.error("Error fetching cart statistics: {}", e.getMessage());
            return Map.of("error", "Statistics temporarily unavailable");
        }
    }

    // Private helper methods
    private Optional<Cart> findExistingCart(UUID userId, String sessionId) {
        if (userId != null) {
            return cartRepository.findActiveCartByUserId(userId);
        } else if (sessionId != null) {
            return cartRepository.findActiveCartBySessionId(sessionId);
        }
        return Optional.empty();
    }

    private void validateCartLimits(Cart cart, int additionalQuantity) {
        if (cart.getItemCount() >= maxItemsPerCart) {
            throw new CartServiceException("Maximum items per cart exceeded: " + maxItemsPerCart);
        }

        BigDecimal projectedTotal = cart.getTotalAmount().add(BigDecimal.valueOf(additionalQuantity * 10)); // Rough estimate
        if (projectedTotal.compareTo(maxCartValue) > 0) {
            throw new CartServiceException("Maximum cart value exceeded: " + maxCartValue);
        }
    }

    @Async
    protected void validateCartItems(Cart cart) {
        try {
            List<UUID> productIds = cart.getActiveItems().stream()
                    .map(CartItem::getProductId)
                    .collect(Collectors.toList());

            if (productIds.isEmpty()) return;

            List<ProductValidation> productValidations = productGateway.validateProducts(productIds);
            // TODO: Update cart items based on validation results

        } catch (Exception e) {
            logger.warn("Failed to validate cart items for cart {}: {}", cart.getId(), e.getMessage());
        }
    }

    private CreateOrderRequest createOrderRequest(Cart cart, CheckoutRequest request) {
        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setUserId(cart.getUserId());
        orderRequest.setCustomerName(request.getCustomerName());
        orderRequest.setCustomerEmail(request.getCustomerEmail());
        orderRequest.setCustomerPhone(request.getCustomerPhone());
        orderRequest.setDeliveryType(request.getDeliveryType());
        orderRequest.setDeliveryAddress(request.getDeliveryAddress());
        orderRequest.setDeliveryDate(request.getDeliveryDate());
        orderRequest.setSpecialInstructions(request.getSpecialInstructions());
        orderRequest.setDiscountCode(request.getDiscountCode());

        // Payment information
        orderRequest.setPaymentMethod(request.getPaymentMethod());
        
        BigDecimal paymentAmount = cart.getTotalAmount();
        if ("DELIVERY".equals(request.getDeliveryType())) {
            paymentAmount = paymentAmount.add(new BigDecimal("5.00"));
        }
        orderRequest.setPaymentAmount(paymentAmount);
        
        orderRequest.setCurrencyCode(cart.getCurrencyCode());
        orderRequest.setCardLastFour(request.getCardLastFour());
        orderRequest.setCardBrand(request.getCardBrand());
        orderRequest.setCardType(request.getCardType());
        orderRequest.setDigitalWalletProvider(request.getDigitalWalletProvider());
        orderRequest.setBankName(request.getBankName());
        orderRequest.setPaymentNotes(request.getPaymentNotes());

        // Order items
        List<CreateOrderRequest.OrderItemDto> items = cart.getActiveItems().stream()
                .map(this::convertCartItemToOrderItem)
                .collect(Collectors.toList());
        orderRequest.setItems(items);

        return orderRequest;
    }

    private CreateOrderRequest.OrderItemDto convertCartItemToOrderItem(CartItem cartItem) {
        CreateOrderRequest.OrderItemDto orderItem = new CreateOrderRequest.OrderItemDto();
        orderItem.setProductId(cartItem.getProductId());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setUnitPriceOverride(cartItem.getUnitPrice());
        orderItem.setSpecialInstructions(cartItem.getSpecialInstructions());
        return orderItem;
    }

    private String convertMetadataToJson(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            logger.warn("Failed to convert metadata to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    private double calculateConversionRate(Object[] conversionRate) {
        if (conversionRate == null || conversionRate.length < 2) return 0.0;

        Long total = (Long) conversionRate[0];
        Long converted = (Long) conversionRate[1];

        return total > 0 ? (converted.doubleValue() / total.doubleValue()) * 100 : 0.0;
    }
}
