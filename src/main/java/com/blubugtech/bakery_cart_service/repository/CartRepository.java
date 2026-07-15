package com.blubugtech.bakery_cart_service.repository;

import com.blubugtech.bakery_cart_service.entity.Cart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    @EntityGraph(attributePaths = {"items"})
    List<Cart> findAll();

    @EntityGraph(attributePaths = {"items"})
    Page<Cart> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"items"})
    Optional<Cart> findById(UUID id);

    // Find cart by user ID
    @EntityGraph(attributePaths = {"items"})
    Optional<Cart> findByUserIdAndStatus(UUID userId, Cart.CartStatus status);

    // Find active cart for user
    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT c FROM Cart c WHERE c.userId = :userId AND c.status = 'ACTIVE' ORDER BY c.lastActivityAt DESC")
    Optional<Cart> findActiveCartByUserId(@Param("userId") UUID userId);

    // Find cart by session ID
    @EntityGraph(attributePaths = {"items"})
    Optional<Cart> findBySessionIdAndStatus(String sessionId, Cart.CartStatus status);

    // Find active cart for guest session
    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT c FROM Cart c WHERE c.sessionId = :sessionId AND c.status = 'ACTIVE' ORDER BY c.lastActivityAt DESC")
    Optional<Cart> findActiveCartBySessionId(@Param("sessionId") String sessionId);

    // Find cart by user or session
    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT c FROM Cart c WHERE " +
           "(:userId IS NOT NULL AND c.userId = :userId) OR " +
           "(:sessionId IS NOT NULL AND c.sessionId = :sessionId) " +
           "AND c.status = 'ACTIVE' ORDER BY c.lastActivityAt DESC")
    Optional<Cart> findActiveCartByUserOrSession(@Param("userId") UUID userId,
                                                @Param("sessionId") String sessionId);

    // Find carts by status
    @EntityGraph(attributePaths = {"items"})
    List<Cart> findByStatusOrderByUpdatedAtDesc(Cart.CartStatus status);

    // Find carts by status with pagination
    @EntityGraph(attributePaths = {"items"})
    Page<Cart> findByStatus(Cart.CartStatus status, Pageable pageable);

    // Find carts by user
    @EntityGraph(attributePaths = {"items"})
    List<Cart> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // Find carts by user with pagination
    @EntityGraph(attributePaths = {"items"})
    Page<Cart> findByUserId(UUID userId, Pageable pageable);

    // Find guest carts by session
    @EntityGraph(attributePaths = {"items"})
    List<Cart> findBySessionIdOrderByCreatedAtDesc(String sessionId);

    // Find expired carts
    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT c FROM Cart c WHERE c.expiresAt < :currentTime AND c.status IN ('ACTIVE', 'SAVED') ORDER BY c.expiresAt ASC")
    List<Cart> findExpiredCarts(@Param("currentTime") LocalDateTime currentTime);

    // Find abandoned carts (no activity for specified duration)
    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT c FROM Cart c WHERE c.status = 'ACTIVE' AND c.lastActivityAt < :cutoffTime ORDER BY c.lastActivityAt ASC")
    List<Cart> findAbandonedCarts(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Find carts ready for cleanup
    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT c FROM Cart c WHERE " +
           "(c.status = 'EXPIRED' AND c.updatedAt < :cutoffTime) OR " +
           "(c.status = 'CONVERTED' AND c.convertedAt < :cutoffTime) OR " +
           "(c.status = 'ABANDONED' AND c.abandonedAt < :cutoffTime)")
    List<Cart> findCartsReadyForCleanup(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Find empty carts
    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT c FROM Cart c WHERE c.itemCount = 0 AND c.updatedAt < :cutoffTime")
    List<Cart> findEmptyCartsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Find carts by date range
    @EntityGraph(attributePaths = {"items"})
    List<Cart> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    // Find carts by date range with pagination
    @EntityGraph(attributePaths = {"items"})
    Page<Cart> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find carts with specific total amount range
    @EntityGraph(attributePaths = {"items"})
    List<Cart> findByTotalAmountBetweenOrderByUpdatedAtDesc(BigDecimal minAmount, BigDecimal maxAmount);

    // Find carts by item count range
    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT c FROM Cart c WHERE c.itemCount BETWEEN :minItems AND :maxItems ORDER BY c.updatedAt DESC")
    List<Cart> findByItemCountRange(@Param("minItems") Integer minItems, @Param("maxItems") Integer maxItems);

    // Find carts with discount code
    @EntityGraph(attributePaths = {"items"})
    List<Cart> findByDiscountCodeOrderByUpdatedAtDesc(String discountCode);

    // Find carts by delivery type
    @EntityGraph(attributePaths = {"items"})
    List<Cart> findByDeliveryTypeOrderByUpdatedAtDesc(String deliveryType);

    // Find carts by source
    @EntityGraph(attributePaths = {"items"})
    List<Cart> findBySourceOrderByCreatedAtDesc(String source);

    // Count carts by status
    long countByStatus(Cart.CartStatus status);

    // Count carts by user
    long countByUserId(UUID userId);

    // Count guest carts
    long countByUserIdIsNull();

    // Count carts by date range
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Count carts by status and date range
    long countByStatusAndCreatedAtBetween(Cart.CartStatus status, LocalDateTime startDate, LocalDateTime endDate);

    // Get cart statistics
    @Query("SELECT " +
           "COUNT(c) as totalCarts, " +
           "COUNT(CASE WHEN c.status = 'ACTIVE' THEN 1 END) as activeCarts, " +
           "COUNT(CASE WHEN c.status = 'ABANDONED' THEN 1 END) as abandonedCarts, " +
           "COUNT(CASE WHEN c.status = 'CONVERTED' THEN 1 END) as convertedCarts, " +
           "AVG(c.totalAmount) as averageCartValue, " +
           "AVG(c.itemCount) as averageItemCount " +
           "FROM Cart c " +
           "WHERE c.createdAt BETWEEN :startDate AND :endDate")
    Object[] getCartStatistics(@Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate);

    // Get daily cart statistics
    @Query(value = "SELECT DATE(c.created_at) as cart_date, " +
                   "COUNT(c) as cart_count, " +
                   "COUNT(CASE WHEN c.status = 'CONVERTED' THEN 1 END) as converted_count, " +
                   "COUNT(CASE WHEN c.status = 'ABANDONED' THEN 1 END) as abandoned_count, " +
                   "AVG(c.total_amount) as average_value, " +
                   "SUM(c.total_amount) as total_value " +
                   "FROM carts c " +
                   "WHERE c.created_at BETWEEN :startDate AND :endDate " +
                   "GROUP BY DATE(c.created_at) " +
                   "ORDER BY DATE(c.created_at) DESC", nativeQuery = true)
    List<Object[]> getDailyCartStatistics(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // Get cart conversion rate
    @Query("SELECT " +
           "COUNT(c) as totalCarts, " +
           "COUNT(CASE WHEN c.status = 'CONVERTED' THEN 1 END) as convertedCarts, " +
           "COUNT(CASE WHEN c.status = 'ABANDONED' THEN 1 END) as abandonedCarts " +
           "FROM Cart c " +
           "WHERE c.createdAt BETWEEN :startDate AND :endDate")
    Object[] getCartConversionRate(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    // Get cart statistics by source
    @Query("SELECT c.source as source, " +
           "COUNT(c) as cartCount, " +
           "AVG(c.totalAmount) as averageValue, " +
           "COUNT(CASE WHEN c.status = 'CONVERTED' THEN 1 END) as convertedCount " +
           "FROM Cart c " +
           "WHERE c.createdAt BETWEEN :startDate AND :endDate " +
           "AND c.source IS NOT NULL " +
           "GROUP BY c.source " +
           "ORDER BY COUNT(c) DESC")
    List<Object[]> getCartStatisticsBySource(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    // Get average cart session duration
    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (c.updated_at - c.created_at))/60) " +
                   "FROM carts c " +
                   "WHERE c.status = 'CONVERTED' " +
                   "AND c.created_at BETWEEN :startDate AND :endDate", nativeQuery = true)
    Double getAverageSessionDurationInMinutes(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    // Get top users by cart activity
    @Query("SELECT c.userId as userId, " +
           "COUNT(c) as cartCount, " +
           "AVG(c.totalAmount) as averageValue, " +
           "SUM(c.totalAmount) as totalValue " +
           "FROM Cart c " +
           "WHERE c.userId IS NOT NULL " +
           "AND c.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY c.userId " +
           "ORDER BY COUNT(c) DESC")
    List<Object[]> getTopUsersByCartActivity(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            Pageable pageable);

    // Advanced search with multiple filters
    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT c FROM Cart c " +
           "WHERE (:userId IS NULL OR c.userId = :userId) " +
           "AND (:sessionId IS NULL OR c.sessionId = :sessionId) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:deliveryType IS NULL OR c.deliveryType = :deliveryType) " +
           "AND (:minAmount IS NULL OR c.totalAmount >= :minAmount) " +
           "AND (:maxAmount IS NULL OR c.totalAmount <= :maxAmount) " +
           "AND (:minItems IS NULL OR c.itemCount >= :minItems) " +
           "AND (:maxItems IS NULL OR c.itemCount <= :maxItems) " +
           "AND (:startDate IS NULL OR c.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR c.createdAt <= :endDate) " +
           "AND (:source IS NULL OR c.source = :source) " +
           "ORDER BY c.updatedAt DESC")
    List<Cart> findCartsWithFilters(@Param("userId") UUID userId,
                                   @Param("sessionId") String sessionId,
                                   @Param("status") Cart.CartStatus status,
                                   @Param("deliveryType") String deliveryType,
                                   @Param("minAmount") BigDecimal minAmount,
                                   @Param("maxAmount") BigDecimal maxAmount,
                                   @Param("minItems") Integer minItems,
                                   @Param("maxItems") Integer maxItems,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   @Param("source") String source);

    // Search carts by customer information
    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT c FROM Cart c " +
           "WHERE LOWER(c.customerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(c.customerEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY c.updatedAt DESC")
    List<Cart> searchCartsByCustomerInfo(@Param("searchTerm") String searchTerm);

    // Bulk operations
    @Modifying
    @Query("UPDATE Cart c SET c.status = 'EXPIRED' WHERE c.expiresAt < :currentTime AND c.status IN ('ACTIVE', 'SAVED')")
    int markExpiredCarts(@Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query("UPDATE Cart c SET c.status = 'ABANDONED', c.abandonedAt = :currentTime WHERE c.status = 'ACTIVE' AND c.lastActivityAt < :cutoffTime")
    int markAbandonedCarts(@Param("currentTime") LocalDateTime currentTime, @Param("cutoffTime") LocalDateTime cutoffTime);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.status IN ('EXPIRED', 'ABANDONED') AND c.updatedAt < :cutoffTime")
    int cleanupOldCarts(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.itemCount = 0 AND c.updatedAt < :cutoffTime")
    int cleanupEmptyCarts(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Check if user has active cart
    boolean existsByUserIdAndStatus(UUID userId, Cart.CartStatus status);

    // Check if session has active cart
    boolean existsBySessionIdAndStatus(String sessionId, Cart.CartStatus status);
}
