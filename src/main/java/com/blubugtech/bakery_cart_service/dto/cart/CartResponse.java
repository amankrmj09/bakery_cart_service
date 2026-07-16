package com.blubugtech.bakery_cart_service.dto.cart;
import com.blubugtech.bakery_cart_service.dto.cartitem.CartItemResponse;

import com.blubugtech.bakery_cart_service.entity.Cart;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class CartResponse {

    // Getters and Setters
    private UUID id;
    private UUID userId;
    private String sessionId;
    private Cart.CartStatus status;
    private String customerName;
    private String customerEmail;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private Integer itemCount;
    private Integer totalQuantity;
    private String currencyCode;
    private String discountCode;
    private String specialInstructions;
    private String deliveryType;
    private String deliveryAddress;
    private List<CartItemResponse> items;
    private List<CartItemResponse> savedItems; // Items saved for later
    private Boolean isEmpty;
    private Boolean isExpired;
    private Boolean isGuest;
    private Boolean hasStockIssues;
    private Boolean hasPriceChanges;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime lastActivityAt;
    private LocalDateTime abandonedAt;
    private LocalDateTime convertedAt;
    private UUID convertedOrderId;
    private String source;
    private String deviceType;
    private Map<String, Object> metadata;

    // Constructors
    public CartResponse() {}

    }
