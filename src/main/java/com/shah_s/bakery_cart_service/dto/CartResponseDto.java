package com.shah_s.bakery_cart_service.dto;

import com.shah_s.bakery_cart_service.entity.Cart;
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
public class CartResponseDto {

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
    private List<CartItemResponseDto> items;
    private List<CartItemResponseDto> savedItems; // Items saved for later
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
    public CartResponseDto() {}

    // Static factory method
    public static CartResponseDto from(Cart cart) {
        CartResponseDto response = new CartResponseDto();
        response.id = cart.getId();
        response.userId = cart.getUserId();
        response.sessionId = cart.getSessionId();
        response.status = cart.getStatus();
        response.customerName = cart.getCustomerName();
        response.customerEmail = cart.getCustomerEmail();
        response.subtotal = cart.getSubtotal();
        response.taxAmount = cart.getTaxAmount();
        response.discountAmount = cart.getDiscountAmount();
        response.totalAmount = cart.getTotalAmount();
        response.itemCount = cart.getItemCount();
        response.totalQuantity = cart.getTotalQuantity();
        response.currencyCode = cart.getCurrencyCode();
        response.discountCode = cart.getDiscountCode();
        response.specialInstructions = cart.getSpecialInstructions();
        response.deliveryType = cart.getDeliveryType();
        response.deliveryAddress = cart.getDeliveryAddress();

        // Convert items
        response.items = cart.getActiveItems().stream()
                .map(CartItemResponseDto::from)
                .collect(Collectors.toList());

        response.savedItems = cart.getItems().stream()
                .filter(item -> item.getStatus() == com.shah_s.bakery_cart_service.entity.CartItem.CartItemStatus.SAVED_FOR_LATER)
                .map(CartItemResponseDto::from)
                .collect(Collectors.toList());

        // Calculate derived fields
        response.isEmpty = cart.isEmpty();
        response.isExpired = cart.isExpired();
        response.isGuest = cart.isGuest();
        response.hasStockIssues = cart.getActiveItems().stream().anyMatch(item -> item.hasStockIssue());
        response.hasPriceChanges = cart.getActiveItems().stream().anyMatch(item -> item.hasPriceChanged());

        // Timestamps
        response.createdAt = cart.getCreatedAt();
        response.updatedAt = cart.getUpdatedAt();
        response.expiresAt = cart.getExpiresAt();
        response.lastActivityAt = cart.getLastActivityAt();
        response.abandonedAt = cart.getAbandonedAt();
        response.convertedAt = cart.getConvertedAt();
        response.convertedOrderId = cart.getConvertedOrderId();

        // Additional info
        response.source = cart.getSource();
        response.deviceType = cart.getDeviceType();

        // Parse metadata JSON if exists
        if (cart.getMetadata() != null) {
            try {
                response.metadata = Map.of("raw", cart.getMetadata());
            } catch (Exception e) {
                response.metadata = Map.of("error", "Failed to parse metadata");
            }
        }

        return response;
    }

}
