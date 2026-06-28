package com.shah_s.bakery_cart_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Setter
@Table(name = "carts", indexes = {
    @Index(name = "idx_cart_user", columnList = "user_id"),
    @Index(name = "idx_cart_session", columnList = "session_id"),
    @Index(name = "idx_cart_status", columnList = "status"),
    @Index(name = "idx_cart_updated", columnList = "updated_at"),
    @Index(name = "idx_cart_expires", columnList = "expires_at"),
    @Index(name = "idx_cart_user_status", columnList = "user_id, status")
})
public class Cart {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId; // NULL for guest carts

    @Column(name = "session_id", length = 255)
    @Size(max = 255, message = "Session ID must not exceed 255 characters")
    private String sessionId; // For guest cart identification

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CartStatus status = CartStatus.ACTIVE;

    @Column(name = "customer_name", length = 100)
    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    private String customerName;

    @Column(name = "customer_email", length = 255)
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String customerEmail;

    @Column(name = "subtotal", precision = 12, scale = 2, nullable = false)
    @DecimalMin(value = "0.00", message = "Subtotal cannot be negative")
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Tax amount cannot be negative")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Discount amount cannot be negative")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    @DecimalMin(value = "0.00", message = "Total amount cannot be negative")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "item_count", nullable = false)
    @Min(value = 0, message = "Item count cannot be negative")
    private Integer itemCount = 0;

    @Column(name = "total_quantity", nullable = false)
    @Min(value = 0, message = "Total quantity cannot be negative")
    private Integer totalQuantity = 0;

    @Column(name = "currency_code", length = 3)
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    private String currencyCode = "USD";

    @Column(name = "discount_code", length = 50)
    @Size(max = 50, message = "Discount code must not exceed 50 characters")
    private String discountCode;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    @Column(name = "delivery_type", length = 20)
    @Size(max = 20, message = "Delivery type must not exceed 20 characters")
    private String deliveryType; // PICKUP, DELIVERY

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "abandoned_at")
    private LocalDateTime abandonedAt;

    @Column(name = "converted_at")
    private LocalDateTime convertedAt;

    @Column(name = "converted_order_id")
    private UUID convertedOrderId;

    // Relationships
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("addedAt ASC")
    @BatchSize(size = 50)
    private List<CartItem> items = new ArrayList<>();

    // Metadata for additional information
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data

    @Column(name = "source", length = 50)
    @Size(max = 50, message = "Source must not exceed 50 characters")
    private String source; // WEB, MOBILE, API

    @Column(name = "device_type", length = 20)
    @Size(max = 20, message = "Device type must not exceed 20 characters")
    private String deviceType; // DESKTOP, MOBILE, TABLET

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    // Constructors
    public Cart() {
        this.lastActivityAt = LocalDateTime.now();
    }

    public Cart(UUID userId, String sessionId) {
        this();
        this.userId = userId;
        this.sessionId = sessionId;
        setExpirationTime();
    }

    public Cart(String sessionId) {
        this(null, sessionId);
    }


    public void setUserId(UUID userId) {
        this.userId = userId;
        setExpirationTime(); // Update expiration when user is set
    }

    // Business Logic Methods
    public void addItem(CartItem item) {
        item.setCart(this);
        items.add(item);
        updateTotals();
        updateActivity();
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        updateTotals();
        updateActivity();
    }

    public void clearItems() {
        items.clear();
        updateTotals();
        updateActivity();
    }

    public void updateTotals() {
        this.subtotal = items.stream()
                .filter(item -> item.getStatus() == CartItem.CartItemStatus.ACTIVE)
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.itemCount = (int) items.stream()
                .filter(item -> item.getStatus() == CartItem.CartItemStatus.ACTIVE)
                .count();

        this.totalQuantity = items.stream()
                .filter(item -> item.getStatus() == CartItem.CartItemStatus.ACTIVE)
                .mapToInt(CartItem::getQuantity)
                .sum();

        // Calculate tax (8% default)
        this.taxAmount = subtotal.multiply(new BigDecimal("0.08"));

        // Calculate total (subtotal + tax - discount)
        this.totalAmount = subtotal.add(taxAmount).subtract(discountAmount);

        // Ensure total is not negative
        if (this.totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            this.totalAmount = BigDecimal.ZERO;
        }
    }

    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();

        // Extend expiration time on activity
        setExpirationTime();
    }

    public void setExpirationTime() {
        LocalDateTime now = LocalDateTime.now();
        if (userId != null) {
            // User carts expire after 30 days
            this.expiresAt = now.plusDays(30);
        } else {
            // Guest carts expire after 24 hours
            this.expiresAt = now.plusHours(24);
        }
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isEmpty() {
        return items.isEmpty() || items.stream()
                .noneMatch(item -> item.getStatus() == CartItem.CartItemStatus.ACTIVE);
    }

    public boolean isGuest() {
        return userId == null;
    }

    public boolean hasUser() {
        return userId != null;
    }

    public void markAsAbandoned() {
        this.status = CartStatus.ABANDONED;
        this.abandonedAt = LocalDateTime.now();
    }

    public void markAsConverted(UUID orderId) {
        this.status = CartStatus.CONVERTED;
        this.convertedAt = LocalDateTime.now();
        this.convertedOrderId = orderId;
    }

    public void markAsSaved() {
        this.status = CartStatus.SAVED;
        updateActivity();
    }

    public void reactivate() {
        this.status = CartStatus.ACTIVE;
        updateActivity();
    }

    public CartItem findItemByProductId(UUID productId) {
        return items.stream()
                .filter(item -> item.getProductId().equals(productId) &&
                               item.getStatus() == CartItem.CartItemStatus.ACTIVE)
                .findFirst()
                .orElse(null);
    }

    public boolean hasItem(UUID productId) {
        return findItemByProductId(productId) != null;
    }

    public int getActiveItemsCount() {
        return (int) items.stream()
                .filter(item -> item.getStatus() == CartItem.CartItemStatus.ACTIVE)
                .count();
    }

    public List<CartItem> getActiveItems() {
        return items.stream()
                .filter(item -> item.getStatus() == CartItem.CartItemStatus.ACTIVE)
                .toList();
    }

    // Enum for cart status
    public enum CartStatus {
        ACTIVE,     // Cart is being actively used
        SAVED,      // Cart is saved for later
        ABANDONED,  // Cart was abandoned (no activity for extended period)
        CONVERTED,  // Cart was converted to an order
        EXPIRED     // Cart has expired and should be cleaned up
    }

    @Override
    public String toString() {
        return String.format("Cart{id=%s, userId=%s, status=%s, itemCount=%d, totalAmount=%s}",
                           id, userId, status, itemCount, totalAmount);
    }
}
