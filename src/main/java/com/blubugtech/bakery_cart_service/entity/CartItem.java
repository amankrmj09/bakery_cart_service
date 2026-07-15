package com.blubugtech.bakery_cart_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "cart_items", indexes = {
    @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
    @Index(name = "idx_cart_item_product", columnList = "product_id"),
    @Index(name = "idx_cart_item_status", columnList = "status"),
    @Index(name = "idx_cart_item_added", columnList = "added_at"),
    @Index(name = "idx_cart_product_status", columnList = "cart_id, product_id, status")
})
public class CartItem {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @NotNull(message = "Cart is required")
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    @NotNull(message = "Product ID is required")
    private UUID productId;

    @Column(name = "product_sku", length = 100)
    @Size(max = 100, message = "Product SKU must not exceed 100 characters")
    private String productSku;

    @Column(name = "product_name", length = 255, nullable = false)
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String productName;

    @Column(name = "product_category", length = 100)
    @Size(max = 100, message = "Product category must not exceed 100 characters")
    private String productCategory;

    @Column(nullable = false)
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 50, message = "Quantity cannot exceed 50")
    private Integer quantity;

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.00", message = "Unit price cannot be negative")
    private BigDecimal unitPrice;

    @Column(name = "total_price", precision = 12, scale = 2, nullable = false)
    @DecimalMin(value = "0.00", message = "Total price cannot be negative")
    private BigDecimal totalPrice;

    @Column(name = "original_unit_price", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Original unit price cannot be negative")
    private BigDecimal originalUnitPrice; // For price comparison

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CartItemStatus status = CartItemStatus.ACTIVE;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;

    @Column(name = "product_image_url", length = 500)
    @Size(max = 500, message = "Product image URL must not exceed 500 characters")
    private String productImageUrl;

    @Column(name = "preparation_time_minutes")
    @Min(value = 0, message = "Preparation time cannot be negative")
    private Integer preparationTimeMinutes;

    @Column(name = "currency_code", length = 3)
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    private String currencyCode = "USD";

    // Product availability info
    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(name = "stock_quantity")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Column(name = "availability_message", length = 255)
    @Size(max = 255, message = "Availability message must not exceed 255 characters")
    private String availabilityMessage;

    // Price change tracking
    @Column(name = "price_changed")
    private Boolean priceChanged = false;

    @Column(name = "price_change_amount", precision = 10, scale = 2)
    private BigDecimal priceChangeAmount = BigDecimal.ZERO;

    // Timestamps
    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_validated_at")
    private LocalDateTime lastValidatedAt;

    @Column(name = "saved_for_later_at")
    private LocalDateTime savedForLaterAt;

    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    // Metadata for additional information
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data

    @Column(name = "added_from", length = 50)
    @Size(max = 50, message = "Added from must not exceed 50 characters")
    private String addedFrom; // PRODUCT_PAGE, CATEGORY_PAGE, SEARCH, RECOMMENDATION

    // Constructors
    public CartItem() {}

    public CartItem(Cart cart, UUID productId, String productName, Integer quantity, BigDecimal unitPrice) {
        this.cart = cart;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.originalUnitPrice = unitPrice;
        this.currencyCode = cart.getCurrencyCode();
        calculateTotalPrice();
    }


    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateTotalPrice();
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotalPrice();
        checkPriceChange();
    }

    // Business Logic Methods
    public void calculateTotalPrice() {
        if (quantity != null && unitPrice != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        } else {
            this.totalPrice = BigDecimal.ZERO;
        }
    }

    public void checkPriceChange() {
        if (originalUnitPrice != null && unitPrice != null) {
            this.priceChangeAmount = unitPrice.subtract(originalUnitPrice);
            this.priceChanged = priceChangeAmount.compareTo(BigDecimal.ZERO) != 0;
        }
    }

    public void increaseQuantity(int amount) {
        this.quantity += amount;
        calculateTotalPrice();
    }

    public void decreaseQuantity(int amount) {
        this.quantity = Math.max(1, this.quantity - amount);
        calculateTotalPrice();
    }

    public void saveForLater() {
        this.status = CartItemStatus.SAVED_FOR_LATER;
        this.savedForLaterAt = LocalDateTime.now();
    }

    public void moveToCart() {
        this.status = CartItemStatus.ACTIVE;
        this.savedForLaterAt = null;
    }

    public void remove() {
        this.status = CartItemStatus.REMOVED;
        this.removedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == CartItemStatus.ACTIVE;
    }

    public boolean isSavedForLater() {
        return status == CartItemStatus.SAVED_FOR_LATER;
    }

    public boolean isRemoved() {
        return status == CartItemStatus.REMOVED;
    }

    public boolean hasStockIssue() {
        return !isAvailable || (stockQuantity != null && stockQuantity < quantity);
    }

    public boolean hasPriceChanged() {
        return priceChanged != null && priceChanged;
    }

    public int getTotalPreparationTime() {
        return preparationTimeMinutes != null ? preparationTimeMinutes * quantity : 0;
    }

    public void updateValidation() {
        this.lastValidatedAt = LocalDateTime.now();
    }

    // Enum for cart item status
    public enum CartItemStatus {
        ACTIVE,           // Item is active in cart
        SAVED_FOR_LATER,  // Item is saved for later
        REMOVED           // Item was removed from cart
    }

    @Override
    public String toString() {
        return String.format("CartItem{id=%s, productId=%s, productName='%s', quantity=%d, unitPrice=%s, status=%s}",
                           id, productId, productName, quantity, unitPrice, status);
    }
}
