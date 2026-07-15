package com.blubugtech.bakery_cart_service.repository;

import com.blubugtech.bakery_cart_service.entity.CartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    // Find items by cart ID
    List<CartItem> findByCartIdOrderByAddedAtAsc(UUID cartId);

    // Find active items by cart ID
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.status = 'ACTIVE' ORDER BY ci.addedAt ASC")
    List<CartItem> findActiveItemsByCartId(@Param("cartId") UUID cartId);

    // Find saved items by cart ID
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.status = 'SAVED_FOR_LATER' ORDER BY ci.savedForLaterAt DESC")
    List<CartItem> findSavedItemsByCartId(@Param("cartId") UUID cartId);

    // Find item by cart and product
    Optional<CartItem> findByCartIdAndProductIdAndStatus(UUID cartId, UUID productId, CartItem.CartItemStatus status);

    // Find active item by cart and product
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.productId = :productId AND ci.status = 'ACTIVE'")
    Optional<CartItem> findActiveItemByCartAndProduct(@Param("cartId") UUID cartId, @Param("productId") UUID productId);

    // Find items by product ID
    List<CartItem> findByProductIdOrderByAddedAtDesc(UUID productId);

    // Find items by product ID and status
    List<CartItem> findByProductIdAndStatusOrderByAddedAtDesc(UUID productId, CartItem.CartItemStatus status);

    // Find items by status
    List<CartItem> findByStatusOrderByUpdatedAtDesc(CartItem.CartItemStatus status);

    // Find items by status with pagination
    Page<CartItem> findByStatus(CartItem.CartItemStatus status, Pageable pageable);

    // Find items with stock issues
    @Query("SELECT ci FROM CartItem ci WHERE ci.isAvailable = false OR ci.stockQuantity < ci.quantity ORDER BY ci.updatedAt DESC")
    List<CartItem> findItemsWithStockIssues();

    // Find items with price changes
    @Query("SELECT ci FROM CartItem ci WHERE ci.priceChanged = true ORDER BY ci.updatedAt DESC")
    List<CartItem> findItemsWithPriceChanges();

    // Find items needing validation
    @Query("SELECT ci FROM CartItem ci WHERE ci.lastValidatedAt IS NULL OR ci.lastValidatedAt < :cutoffTime ORDER BY ci.lastValidatedAt ASC")
    List<CartItem> findItemsNeedingValidation(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Find items by date range
    List<CartItem> findByAddedAtBetweenOrderByAddedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    // Find items by price range
    List<CartItem> findByUnitPriceBetweenOrderByAddedAtDesc(BigDecimal minPrice, BigDecimal maxPrice);

    // Find items by quantity range
    @Query("SELECT ci FROM CartItem ci WHERE ci.quantity BETWEEN :minQty AND :maxQty ORDER BY ci.addedAt DESC")
    List<CartItem> findByQuantityRange(@Param("minQty") Integer minQuantity, @Param("maxQty") Integer maxQuantity);

    // Find items by category
    List<CartItem> findByProductCategoryOrderByAddedAtDesc(String productCategory);

    // Find items added from specific source
    List<CartItem> findByAddedFromOrderByAddedAtDesc(String addedFrom);

    // Count items by cart
    long countByCartId(UUID cartId);

    // Count active items by cart
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.status = 'ACTIVE'")
    long countActiveItemsByCartId(@Param("cartId") UUID cartId);

    // Count items by product
    long countByProductId(UUID productId);

    // Count items by status
    long countByStatus(CartItem.CartItemStatus status);

    // Count items by date range
    long countByAddedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Get most popular products
    @Query("SELECT ci.productId as productId, " +
           "ci.productName as productName, " +
           "COUNT(ci) as itemCount, " +
           "SUM(ci.quantity) as totalQuantity, " +
           "AVG(ci.unitPrice) as averagePrice " +
           "FROM CartItem ci " +
           "WHERE ci.addedAt BETWEEN :startDate AND :endDate " +
           "AND ci.status = 'ACTIVE' " +
           "GROUP BY ci.productId, ci.productName " +
           "ORDER BY COUNT(ci) DESC")
    List<Object[]> getMostPopularProducts(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         Pageable pageable);

    // Get cart item statistics
    @Query("SELECT " +
           "COUNT(ci) as totalItems, " +
           "COUNT(CASE WHEN ci.status = 'ACTIVE' THEN 1 END) as activeItems, " +
           "COUNT(CASE WHEN ci.status = 'SAVED_FOR_LATER' THEN 1 END) as savedItems, " +
           "COUNT(CASE WHEN ci.status = 'REMOVED' THEN 1 END) as removedItems, " +
           "AVG(ci.unitPrice) as averagePrice, " +
           "AVG(ci.quantity) as averageQuantity " +
           "FROM CartItem ci " +
           "WHERE ci.addedAt BETWEEN :startDate AND :endDate")
    Object[] getCartItemStatistics(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    // Get daily item statistics
    @Query(value = "SELECT DATE(ci.added_at) as item_date, " +
                   "COUNT(ci) as item_count, " +
                   "SUM(ci.quantity) as total_quantity, " +
                   "AVG(ci.unit_price) as average_price " +
                   "FROM cart_items ci " +
                   "WHERE ci.added_at BETWEEN :startDate AND :endDate " +
                   "GROUP BY DATE(ci.added_at) " +
                   "ORDER BY DATE(ci.added_at) DESC", nativeQuery = true)
    List<Object[]> getDailyItemStatistics(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // Get item statistics by category
    @Query("SELECT ci.productCategory as category, " +
           "COUNT(ci) as itemCount, " +
           "SUM(ci.quantity) as totalQuantity, " +
           "AVG(ci.unitPrice) as averagePrice " +
           "FROM CartItem ci " +
           "WHERE ci.addedAt BETWEEN :startDate AND :endDate " +
           "AND ci.productCategory IS NOT NULL " +
           "GROUP BY ci.productCategory " +
           "ORDER BY COUNT(ci) DESC")
    List<Object[]> getItemStatisticsByCategory(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    // Get total cart value by cart
    @Query("SELECT SUM(ci.totalPrice) FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.status = 'ACTIVE'")
    BigDecimal getTotalCartValue(@Param("cartId") UUID cartId);

    // Get total quantity by cart
    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.status = 'ACTIVE'")
    Integer getTotalCartQuantity(@Param("cartId") UUID cartId);

    // Advanced search with multiple filters
    @Query("SELECT ci FROM CartItem ci " +
           "WHERE (:cartId IS NULL OR ci.cart.id = :cartId) " +
           "AND (:productId IS NULL OR ci.productId = :productId) " +
           "AND (:status IS NULL OR ci.status = :status) " +
           "AND (:category IS NULL OR ci.productCategory = :category) " +
           "AND (:minPrice IS NULL OR ci.unitPrice >= :minPrice) " +
           "AND (:maxPrice IS NULL OR ci.unitPrice <= :maxPrice) " +
           "AND (:minQty IS NULL OR ci.quantity >= :minQty) " +
           "AND (:maxQty IS NULL OR ci.quantity <= :maxQty) " +
           "AND (:startDate IS NULL OR ci.addedAt >= :startDate) " +
           "AND (:endDate IS NULL OR ci.addedAt <= :endDate) " +
           "AND (:addedFrom IS NULL OR ci.addedFrom = :addedFrom) " +
           "ORDER BY ci.addedAt DESC")
    List<CartItem> findItemsWithFilters(@Param("cartId") UUID cartId,
                                       @Param("productId") UUID productId,
                                       @Param("status") CartItem.CartItemStatus status,
                                       @Param("category") String category,
                                       @Param("minPrice") BigDecimal minPrice,
                                       @Param("maxPrice") BigDecimal maxPrice,
                                       @Param("minQty") Integer minQuantity,
                                       @Param("maxQty") Integer maxQuantity,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       @Param("addedFrom") String addedFrom);

    // Search items by product name
    @Query("SELECT ci FROM CartItem ci " +
           "WHERE LOWER(ci.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY ci.addedAt DESC")
    List<CartItem> searchItemsByProductName(@Param("searchTerm") String searchTerm);

    // Bulk operations
    @Modifying
    @Query("UPDATE CartItem ci SET ci.status = 'REMOVED', ci.removedAt = :currentTime WHERE ci.cart.id = :cartId")
    int removeAllItemsFromCart(@Param("cartId") UUID cartId, @Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query("UPDATE CartItem ci SET ci.status = 'SAVED_FOR_LATER', ci.savedForLaterAt = :currentTime WHERE ci.cart.id = :cartId AND ci.status = 'ACTIVE'")
    int saveAllItemsForLater(@Param("cartId") UUID cartId, @Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query("UPDATE CartItem ci SET ci.status = 'ACTIVE', ci.savedForLaterAt = NULL WHERE ci.cart.id = :cartId AND ci.status = 'SAVED_FOR_LATER'")
    int moveAllItemsToCart(@Param("cartId") UUID cartId);

    @Modifying
    @Query("UPDATE CartItem ci SET ci.lastValidatedAt = :currentTime WHERE ci.cart.id = :cartId")
    int updateValidationTimestamp(@Param("cartId") UUID cartId, @Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.status = 'REMOVED' AND ci.removedAt < :cutoffTime")
    int cleanupRemovedItems(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Check if item exists in cart
    boolean existsByCartIdAndProductIdAndStatus(UUID cartId, UUID productId, CartItem.CartItemStatus status);
}
