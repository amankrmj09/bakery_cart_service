package com.blubugtech.bakery_cart_service.service.impl;

import lombok.extern.slf4j.Slf4j;import com.blubugtech.bakery_cart_service.service.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.blubakery.common.core.dto.RestPageResponse;
import com.blubugtech.bakery_cart_service.gateway.OrderGateway;
import com.blubugtech.bakery_cart_service.gateway.ProductGateway;
import com.blubugtech.bakery_cart_service.dto.cart.*;
import com.blubugtech.bakery_cart_service.dto.cartitem.*;
import com.blubugtech.bakery_cart_service.dto.checkout.*;
import com.blubugtech.bakery_cart_service.dto.checkout.CheckoutResponse;
import com.blubugtech.bakery_cart_service.dto.order.CreateOrderRequest;
import com.blubugtech.bakery_cart_service.dto.order.OrderResponse;
import org.blubakery.common.feign.contract.feign.ProductValidation;
import com.blubugtech.bakery_cart_service.strategy.DiscountCalculationStrategy;
import com.blubugtech.bakery_cart_service.strategy.OrderRequestMappingStrategy;
import com.blubugtech.bakery_cart_service.mapper.CartMapper;
import com.blubugtech.bakery_cart_service.entity.Cart;
import com.blubugtech.bakery_cart_service.entity.CartItem;
import com.blubugtech.bakery_cart_service.exception.CartServiceException;
import com.blubugtech.bakery_cart_service.repository.CartRepository;
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
@Slf4j
public class CartServiceImpl implements CartService {

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

    @Autowired
    private DiscountCalculationStrategy discountCalculationStrategy;

    @Autowired
    private OrderRequestMappingStrategy orderRequestMappingStrategy;

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
        log.info("Creating cart for user: {} session: {}", request.getUserId(), request.getSessionId());

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
            log.info("Cart created successfully: {}", savedCart.getId());

            return cartMapper.toDto(savedCart);

        } catch (Exception e) {
            log.error("Failed to create cart", e);
            throw new CartServiceException("Failed to create cart: " + e.getMessage());
        }
    }

    // Get cart by ID
    @Cacheable(value = "carts", key = "#cartId")
    @Transactional(readOnly = true)
    public CartResponse getCartById(UUID cartId) {
        log.debug("Fetching cart by ID: {}", cartId);
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartServiceException("Cart not found with ID: " + cartId));
        if (checkPriceOnView) {
            validateCartItems(cart);
        }
        return cartMapper.toDto(cart);
    }

    // Get or create cart for user
    @Cacheable(value = "carts", key = "'user-' + #userId")
    public CartResponse getOrCreateCartForUser(UUID userId) {
        log.debug("Getting or creating cart for user: {}", userId);
        Optional<Cart> existingCart = cartRepository.findActiveCartByUserId(userId);
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            if (checkPriceOnView) {
                validateCartItems(cart);
            }
            return cartMapper.toDto(cart);
        }
        CartRequest request = new CartRequest();
        request.setUserId(userId);
        return createCart(request);
    }

    // Get or create cart for session
    @Cacheable(value = "carts", key = "'session-' + #sessionId")
    public CartResponse getOrCreateCartForSession(String sessionId) {
        log.debug("Getting or creating cart for session: {}", sessionId);
        Optional<Cart> existingCart = cartRepository.findActiveCartBySessionId(sessionId);
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            if (checkPriceOnView) {
                validateCartItems(cart);
            }
            return cartMapper.toDto(cart);
        }
        CartRequest request = new CartRequest();
        request.setSessionId(sessionId);
        return createCart(request);
    }

    // Add item to cart
    @CacheEvict(value = "carts", allEntries = true)
    public CartResponse addItemToCart(UUID cartId, AddItemRequest request) {
        log.info("Adding item to cart: {} product: {} quantity: {}",
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

            log.info("Item added to cart successfully: {}", cartId);
            return cartMapper.toDto(updatedCart);

        } catch (Exception e) {
            log.error("Failed to add item to cart {}", cartId, e);
            throw new CartServiceException("Failed to add item to cart: " + e.getMessage());
        }
    }

    // Update item in cart
    @CacheEvict(value = "carts", allEntries = true)
    public CartResponse updateCartItem(UUID cartId, UUID itemId, UpdateItemRequest request) {
        log.info("Updating cart item: {} in cart: {}", itemId, cartId);

        try {
            cartItemService.updateCartItem(itemId, request);

            Cart updatedCart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new CartServiceException("Cart not found after update"));

            log.info("Cart item updated successfully: {}", itemId);
            return cartMapper.toDto(updatedCart);

        } catch (Exception e) {
            log.error("Failed to update cart item {}", itemId, e);
            throw new CartServiceException("Failed to update cart item: " + e.getMessage());
        }
    }

    // Remove item from cart
    @CacheEvict(value = "carts", allEntries = true)
    public CartResponse removeItemFromCart(UUID cartId, UUID itemId) {
        log.info("Removing item from cart: {} item: {}", cartId, itemId);

        try {
            cartItemService.removeItemFromCart(itemId);

            Cart updatedCart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new CartServiceException("Cart not found after update"));

            log.info("Item removed from cart successfully: {}", itemId);
            return cartMapper.toDto(updatedCart);

        } catch (Exception e) {
            log.error("Failed to remove item from cart {}", cartId, e);
            throw new CartServiceException("Failed to remove item from cart: " + e.getMessage());
        }
    }

    // Clear cart
    @CacheEvict(value = "carts", allEntries = true)
    public CartResponse clearCart(UUID cartId) {
        log.info("Clearing cart: {}", cartId);

        try {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new CartServiceException("Cart not found with ID: " + cartId));

            cart.clearItems();
            Cart clearedCart = cartRepository.save(cart);

            log.info("Cart cleared successfully: {}", cartId);
            return cartMapper.toDto(clearedCart);

        } catch (Exception e) {
            log.error("Failed to clear cart {}", cartId, e);
            throw new CartServiceException("Failed to clear cart: " + e.getMessage());
        }
    }

    // Update cart details
    @CacheEvict(value = "carts", allEntries = true)
    public CartResponse updateCart(UUID cartId, CartUpdateRequest request) {
        log.info("Updating cart: {}", cartId);

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
                if (request.getDiscountCode().trim().isEmpty()) {
                    cart.setDiscountCode(null);
                    cart.setDiscountAmount(BigDecimal.ZERO);
                } else {
                    try {
                        org.blubakery.common.feign.contract.feign.CouponValidationResponse couponDetails = productGateway.validateCoupon(request.getDiscountCode(), cart.getSubtotal().doubleValue());
                        cart.setDiscountCode(couponDetails.getCouponCode());
                        
                        String discountType = couponDetails.getDiscountType();
                        Double discountValue = couponDetails.getDiscountValue();
                        
                        BigDecimal discountAmt = discountCalculationStrategy.calculateDiscount(discountType, discountValue, cart.getSubtotal());
                        cart.setDiscountAmount(discountAmt);
                    } catch (Exception e) {
                        log.error("Failed to validate discount code {}", request.getDiscountCode(), e);
                        if (e.getMessage() != null && e.getMessage().contains("expired")) {
                            throw new CartServiceException("Coupon code expired and not valid");
                        } else if (e.getMessage() != null && e.getMessage().contains("doesn't apply")) {
                            throw new CartServiceException("Doesn't apply on this cart (minimum value not met)");
                        }
                        throw new CartServiceException("Invalid coupon code");
                    }
                }
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

            cart.updateTotals();
            cart.updateActivity();
            Cart updatedCart = cartRepository.save(cart);

            log.info("Cart updated successfully: {}", cartId);
            return cartMapper.toDto(updatedCart);

        } catch (Exception e) {
            log.error("Failed to update cart {}", cartId, e);
            throw new CartServiceException("Failed to update cart: " + e.getMessage());
        }
    }

    // Merge carts (for user login)
    @CacheEvict(value = "carts", allEntries = true)
    public CartResponse mergeCarts(MergeCartsRequest request) {
        log.info("Merging carts: {} -> {}", request.getSourceCartId(), request.getTargetCartId());

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

            log.info("Carts merged successfully: {}", request.getTargetCartId());
            return cartMapper.toDto(mergedCart);

        } catch (Exception e) {
            log.error("Failed to merge carts", e);
            throw new CartServiceException("Failed to merge carts: " + e.getMessage());
        }
    }

    // Save cart for later
    @CacheEvict(value = "carts", key = "#cartId")
    public CartResponse saveCartForLater(UUID cartId) {
        log.info("Saving cart for later: {}", cartId);

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartServiceException("Cart not found with ID: " + cartId));

        cart.markAsSaved();
        Cart savedCart = cartRepository.save(cart);

        return cartMapper.toDto(savedCart);
    }

    // Checkout cart
    @CacheEvict(value = "carts", allEntries = true)
    public CheckoutResponse checkoutCart(UUID cartId, CheckoutRequest request) {
        log.info("Checking out cart: {}", cartId);

        try {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new CartServiceException("Cart not found with ID: " + cartId));

            if (cart.isEmpty()) {
                throw new CartServiceException("Cannot checkout empty cart");
            }

            // Validate stock before checkout
            validateStockForCheckout(cart);

            // Create order request
            CreateOrderRequest orderRequest = orderRequestMappingStrategy.createOrderRequest(cart, request);

            // Call Order Service
            OrderResponse orderResponse = orderGateway.createOrder(orderRequest,
                    cart.getUserId() != null ? cart.getUserId().toString() : null, "USER");

            // Mark cart as converted
            cart.markAsConverted(orderResponse.getId());
            cartRepository.save(cart);

            log.info("Cart checked out successfully: {} -> Order: {}", cartId, orderResponse.getId());

            return CheckoutResponse.builder()
                    .cart(cartMapper.toDto(cart))
                    .order(orderResponse)
                    .build();

        } catch (Exception e) {
            log.error("Failed to checkout cart {}", cartId, e);
            throw new CartServiceException("Failed to checkout cart: " + e.getMessage());
        }
    }

    // Get user carts
    @Transactional(readOnly = true)
    public RestPageResponse<CartResponse> getUserCarts(UUID userId, Pageable pageable) {
        log.debug("Fetching carts for user: {}", userId);

        Page<CartResponse> page = cartRepository.findByUserId(userId, pageable)
                .map(cartMapper::toDto);
        return new RestPageResponse<>(page.getContent(), page.getPageable(), page.getTotalElements());
    }

    // Get carts by status
    @Transactional(readOnly = true)
    public RestPageResponse<CartResponse> getCartsByStatus(Cart.CartStatus status, Pageable pageable) {
        log.debug("Fetching carts by status: {}", status);

        Page<CartResponse> page = cartRepository.findByStatus(status, pageable)
                .map(cartMapper::toDto);
        return new RestPageResponse<>(page.getContent(), page.getPageable(), page.getTotalElements());
    }

    // Get all carts with pagination
    @Transactional(readOnly = true)
    public RestPageResponse<CartResponse> getAllCarts(Pageable pageable) {
        log.debug("Fetching all carts with pagination");

        Page<CartResponse> page = cartRepository.findAll(pageable)
                .map(cartMapper::toDto);
        return new RestPageResponse<>(page.getContent(), page.getPageable(), page.getTotalElements());
    }

    // Get cart statistics
    @Cacheable(value = "cart-stats", key = "#startDate + '-' + #endDate")
    @Transactional(readOnly = true)
    public com.blubugtech.bakery_cart_service.dto.CartStatisticsResponse getCartStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching cart statistics");

        try {
            Object[] stats = cartRepository.getCartStatistics(startDate, endDate);
            Object[] conversionRate = cartRepository.getCartConversionRate(startDate, endDate);
            List<Object[]> dailyStatsObj = cartRepository.getDailyCartStatistics(startDate, endDate);
            List<Object[]> sourceStatsObj = cartRepository.getCartStatisticsBySource(startDate, endDate);
            
            List<com.blubugtech.bakery_cart_service.dto.DailyCartStatResponse> dailyStats = dailyStatsObj.stream().map(obj -> 
                com.blubugtech.bakery_cart_service.dto.DailyCartStatResponse.builder()
                    .date(String.valueOf(obj[0]))
                    .convertedCount(((Number) obj[1]).longValue())
                    .abandonedCount(((Number) obj[2]).longValue())
                    .averageValue(obj[3] != null ? ((Number) obj[3]).doubleValue() : 0.0)
                    .totalValue(obj[4] != null ? ((Number) obj[4]).doubleValue() : 0.0)
                    .build()
            ).toList();

            List<com.blubugtech.bakery_cart_service.dto.CartSourceStatResponse> sourceStats = sourceStatsObj.stream().map(obj -> 
                com.blubugtech.bakery_cart_service.dto.CartSourceStatResponse.builder()
                    .source(String.valueOf(obj[0]))
                    .cartCount(((Number) obj[1]).longValue())
                    .averageValue(obj[2] != null ? ((Number) obj[2]).doubleValue() : 0.0)
                    .convertedCount(((Number) obj[3]).longValue())
                    .build()
            ).toList();

            return com.blubugtech.bakery_cart_service.dto.CartStatisticsResponse.builder()
                    .totalCarts(((Number) stats[0]).longValue())
                    .activeCarts(((Number) stats[1]).longValue())
                    .abandonedCarts(((Number) stats[2]).longValue())
                    .convertedCarts(((Number) stats[3]).longValue())
                    .averageCartValue((BigDecimal) stats[4])
                    .averageItemCount(stats[5] != null ? ((Number) stats[5]).doubleValue() : 0.0)
                    .conversionRate(calculateConversionRate(conversionRate))
                    .dailyStats(dailyStats)
                    .sourceStats(sourceStats)
                    .dateRange(com.blubugtech.bakery_cart_service.dto.DateRangeResponse.builder()
                            .startDate(startDate.toString())
                            .endDate(endDate.toString())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Error fetching cart statistics", e);
            throw new RuntimeException("Statistics temporarily unavailable", e);
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
            if (cart.getActiveItems().isEmpty()) return;
            cartItemService.validateCartItems(cart.getActiveItems());
        } catch (Exception e) {
            log.error("Failed to validate cart items for cart {}", cart.getId(), e);
        }
    }

    private void validateStockForCheckout(Cart cart) {
        List<UUID> productIds = cart.getActiveItems().stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toList());

        if (productIds.isEmpty()) return;

        try {
            List<ProductValidation> validations = productGateway.validateProducts(productIds);
            
            Map<UUID, ProductValidation> validationMap = validations.stream()
                    .collect(Collectors.toMap(ProductValidation::getProductId, v -> v, (v1, v2) -> v1));
            
            for (CartItem item : cart.getActiveItems()) {
                ProductValidation validation = validationMap.get(item.getProductId());
                
                if (validation == null) {
                    throw new CartServiceException("Product validation failed for item: " + item.getProductName());
                }
                
                Boolean isAvailable = validation.getAvailable();
                Integer stockQuantity = validation.getStockQuantity();
                
                if (isAvailable == null || !isAvailable || stockQuantity == null || stockQuantity < item.getQuantity()) {
                    throw new CartServiceException("Currently this item run out of stock please remove this item and proceed: " + item.getProductName());
                }
            }
        } catch (CartServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to validate checkout stock", e);
            throw new CartServiceException("Failed to verify stock for checkout");
        }
    }

    private String convertMetadataToJson(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            log.error("Failed to convert metadata to JSON", e);
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
