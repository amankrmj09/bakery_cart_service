package com.blubugtech.bakery_cart_service.service.impl;

import lombok.extern.slf4j.Slf4j;import com.blubugtech.bakery_cart_service.service.*;

import com.blubugtech.bakery_cart_service.entity.Cart;
import com.blubugtech.bakery_cart_service.repository.CartItemRepository;
import com.blubugtech.bakery_cart_service.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CartMaintenanceServiceImpl implements CartMaintenanceService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Value("${cart.expiration.cleanup-interval-hours:6}")
    private Integer cleanupIntervalHours;

    // Clean up expired carts every 6 hours
    @Scheduled(fixedRateString = "${cart.expiration.cleanup-interval-hours:6}000000") // Convert hours to milliseconds
    @Transactional
    @CacheEvict(value = {"carts", "cart-items"}, allEntries = true)
    public void cleanupExpiredCarts() {
        log.info("Starting expired cart cleanup");

        try {
            LocalDateTime now = LocalDateTime.now();

            // Mark expired carts
            int expiredCount = cartRepository.markExpiredCarts(now);
            log.info("Marked {} carts as expired", expiredCount);

            // Mark abandoned carts (no activity for 24 hours)
            LocalDateTime abandonedCutoff = now.minusHours(24);
            int abandonedCount = cartRepository.markAbandonedCarts(now, abandonedCutoff);
            log.info("Marked {} carts as abandoned", abandonedCount);

            // Clean up old expired/abandoned carts (older than 7 days)
            LocalDateTime cleanupCutoff = now.minusDays(7);
            int cleanedUp = cartRepository.cleanupOldCarts(cleanupCutoff);
            log.info("Cleaned up {} old carts", cleanedUp);

            // Clean up empty carts (older than 1 hour)
            LocalDateTime emptyCutoff = now.minusHours(1);
            int emptyCleanedUp = cartRepository.cleanupEmptyCarts(emptyCutoff);
            log.info("Cleaned up {} empty carts", emptyCleanedUp);

            // Clean up removed cart items (older than 30 days)
            LocalDateTime itemCleanupCutoff = now.minusDays(30);
            int itemsCleanedUp = cartItemRepository.cleanupRemovedItems(itemCleanupCutoff);
            log.info("Cleaned up {} removed cart items", itemsCleanedUp);

        } catch (Exception e) {
            log.error("Error during cart cleanup: {}", e.getMessage(), e);
        }
    }

    // Send abandonment notifications (placeholder for future implementation)
    @Scheduled(cron = "0 0 12 * * ?") // Daily at noon
    public void sendAbandonmentNotifications() {
        log.debug("Checking for carts needing abandonment notifications");

        try {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(2);
            List<Cart> abandonedCarts = cartRepository.findAbandonedCarts(cutoff);

            // TODO: Send notifications via email/SMS service
            log.info("Found {} abandoned carts for notification", abandonedCarts.size());

        } catch (Exception e) {
            log.error("Error sending abandonment notifications: {}", e.getMessage());
        }
    }
}
