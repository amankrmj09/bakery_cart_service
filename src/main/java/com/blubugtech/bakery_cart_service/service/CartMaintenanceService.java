package com.blubugtech.bakery_cart_service.service;

public interface CartMaintenanceService {
    void cleanupExpiredCarts();
    void sendAbandonmentNotifications();
}
