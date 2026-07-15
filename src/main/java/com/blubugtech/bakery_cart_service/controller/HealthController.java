package com.blubugtech.bakery_cart_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")

public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Main service health check
    @GetMapping("/health")
    public ResponseEntity<com.blubugtech.common.dto.HealthResponseDto> health() {
        com.blubugtech.common.dto.HealthResponseDto response = new com.blubugtech.common.dto.HealthResponseDto("UP", "bakery-cart-service");
        Map<String, Object> details = new HashMap<>();
        details.put("version", "1.0.0");

        // Check database connectivity
        try (Connection connection = dataSource.getConnection()) {
            details.put("database", "UP");
            details.put("databaseUrl", connection.getMetaData().getURL());
        } catch (Exception e) {
            details.put("database", "DOWN");
            details.put("databaseError", e.getMessage());
        }

        // Check Redis connectivity
        try {
            redisTemplate.opsForValue().set("health-check", "OK");
            String redisResponse = (String) redisTemplate.opsForValue().get("health-check");
            details.put("redis", "OK".equals(redisResponse) ? "UP" : "DOWN");
        } catch (Exception e) {
            details.put("redis", "DOWN");
            details.put("redisError", e.getMessage());
        }
        
        response.setDetails(details);
        return ResponseEntity.ok(response);
    }

    // Service info
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("serviceName", "Bakery Cart Service");
        response.put("description", "Shopping cart management and session handling service");
        response.put("version", "1.0.0");
        response.put("features", Map.of(
            "carts", "User and guest cart management",
            "persistence", "Redis caching with PostgreSQL persistence",
            "validation", "Real-time stock and price validation",
            "checkout", "Seamless order creation integration",
            "analytics", "Cart abandonment and conversion tracking"
        ));
        response.put("endpoints", Map.of(
            "carts", "/api/carts",
            "items", "/api/cart-items"
        ));

        return ResponseEntity.ok(response);
    }

    // Service metrics
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        Map<String, Object> response = new HashMap<>();
        response.put("uptime", getUptime());
        response.put("timestamp", LocalDateTime.now().toString());

        // Memory info
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("maxMemory", runtime.maxMemory() / 1024 / 1024 + " MB");
        memory.put("totalMemory", runtime.totalMemory() / 1024 / 1024 + " MB");
        memory.put("freeMemory", runtime.freeMemory() / 1024 / 1024 + " MB");
        memory.put("usedMemory", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + " MB");
        response.put("memory", memory);

        // Cache info
        try {
            Map<String, Object> cache = new HashMap<>();
            cache.put("redisConnections", "active");
            cache.put("cacheHitRate", "N/A"); // Would need cache statistics
            response.put("cache", cache);
        } catch (Exception e) {
            response.put("cache", Map.of("status", "ERROR", "error", e.getMessage()));
        }

        return ResponseEntity.ok(response);
    }

    private String getUptime() {
        long uptime = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        return String.format("%d days, %d hours, %d minutes, %d seconds",
                days, hours % 24, minutes % 60, seconds % 60);
    }
}
