package com.blubugtech.bakery_cart_service.dto.cartitem;

import com.blubugtech.bakery_cart_service.entity.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private UUID id;
    private UUID productId;
    private String productSku;
    private String productName;
    private String productCategory;
    
    @Builder.Default
    private Integer quantity = 0;
    
    @Builder.Default
    private BigDecimal unitPrice = BigDecimal.ZERO;
    
    private BigDecimal totalPrice;
    private BigDecimal originalUnitPrice;
    private String taxClass;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
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

}
