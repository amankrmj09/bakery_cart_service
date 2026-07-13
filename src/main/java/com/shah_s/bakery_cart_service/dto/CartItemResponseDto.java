package com.shah_s.bakery_cart_service.dto;

import com.shah_s.bakery_cart_service.entity.CartItem;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Setter
@Getter
public class CartItemResponseDto {

    // Getters and Setters
    private UUID id;
    private UUID productId;
    private String productSku;
    private String productName;
    private String productCategory;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private BigDecimal originalUnitPrice;
    private CartItem.CartItemStatus status;
    private String specialInstructions;
    private String productDescription;
    private String productImageUrl;
    private Integer preparationTimeMinutes;
    private String currencyCode;
    private Boolean isAvailable;
    private Integer stockQuantity;
    private String availabilityMessage;
    private Boolean priceChanged;
    private BigDecimal priceChangeAmount;
    private Boolean hasStockIssue;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastValidatedAt;
    private LocalDateTime savedForLaterAt;
    private LocalDateTime removedAt;
    private String addedFrom;
    private Map<String, Object> metadata;

    // Constructors
    public CartItemResponseDto() {}

    // Static factory method
    public static CartItemResponseDto from(CartItem item) {
        CartItemResponseDto response = new CartItemResponseDto();
        response.id = item.getId();
        response.productId = item.getProductId();
        response.productSku = item.getProductSku();
        response.productName = item.getProductName();
        response.productCategory = item.getProductCategory();
        response.quantity = item.getQuantity();
        response.unitPrice = item.getUnitPrice();
        response.totalPrice = item.getTotalPrice();
        response.originalUnitPrice = item.getOriginalUnitPrice();
        response.status = item.getStatus();
        response.specialInstructions = item.getSpecialInstructions();
        response.productDescription = item.getProductDescription();
        response.productImageUrl = item.getProductImageUrl();
        response.preparationTimeMinutes = item.getPreparationTimeMinutes();
        response.currencyCode = item.getCurrencyCode();
        response.isAvailable = item.getIsAvailable();
        response.stockQuantity = item.getStockQuantity();
        response.availabilityMessage = item.getAvailabilityMessage();
        response.priceChanged = item.getPriceChanged();
        response.priceChangeAmount = item.getPriceChangeAmount();
        response.hasStockIssue = item.hasStockIssue();
        response.addedAt = item.getAddedAt();
        response.updatedAt = item.getUpdatedAt();
        response.lastValidatedAt = item.getLastValidatedAt();
        response.savedForLaterAt = item.getSavedForLaterAt();
        response.removedAt = item.getRemovedAt();
        response.addedFrom = item.getAddedFrom();

        // Parse metadata JSON if exists
        if (item.getMetadata() != null) {
            try {
                response.metadata = Map.of("raw", item.getMetadata());
            } catch (Exception e) {
                response.metadata = Map.of("error", "Failed to parse metadata");
            }
        }

        return response;
    }

}
