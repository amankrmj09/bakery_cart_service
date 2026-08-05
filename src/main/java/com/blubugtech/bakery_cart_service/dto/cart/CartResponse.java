package com.blubugtech.bakery_cart_service.dto.cart;
import com.blubugtech.bakery_cart_service.dto.cartitem.CartItemResponse;
import com.blubugtech.bakery_cart_service.entity.Cart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private UUID id;
    private UUID userId;
    private String sessionId;
    
    @Builder.Default
    private Cart.CartStatus status = Cart.CartStatus.ACTIVE;
    
    private String customerName;
    private String customerEmail;
    
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;
    
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Builder.Default
    private Integer itemCount = 0;
    
    private Integer totalQuantity;
    private String currencyCode;
    private String discountCode;
    private String specialInstructions;
    private String deliveryType;
    private String deliveryAddress;
    
    @Builder.Default
    private List<CartItemResponse> items = new ArrayList<>();
    
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

}
