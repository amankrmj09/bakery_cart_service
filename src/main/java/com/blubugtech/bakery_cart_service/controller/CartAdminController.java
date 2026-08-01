package com.blubugtech.bakery_cart_service.controller;

import lombok.extern.slf4j.Slf4j;
import com.blubugtech.bakery_cart_service.dto.cart.*;
import com.blubugtech.bakery_cart_service.entity.Cart;
import com.blubugtech.bakery_cart_service.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/carts")
@Tag(name = "Cart Admin", description = "Endpoints for cart administration")
@RequiredArgsConstructor
@Slf4j
public class CartAdminController {

    private final CartService cartService;

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get carts by status (Admin only)")
    public ResponseEntity<List<CartResponse>> getCartsByStatus(
            @PathVariable Cart.CartStatus status,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get carts by status request received: {}", status);

        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<CartResponse> carts = cartService.getCartsByStatus(status);

        log.info("Retrieved {} carts with status {}", carts.size(), status);
        return ResponseEntity.ok(carts);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all carts (Admin only)")
    public ResponseEntity<Page<CartResponse>> getAllCarts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get all carts request received (page: {}, size: {})", page, size);

        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CartResponse> carts = cartService.getAllCarts(pageable);

        log.info("Retrieved {} carts (page {} of {})", carts.getContent().size(),
                page + 1, carts.getTotalPages());
        return ResponseEntity.ok(carts);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get cart statistics (Admin only)")
    public ResponseEntity<Map<String, Object>> getCartStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        log.info("Get cart statistics request received");

        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        Map<String, Object> statistics = cartService.getCartStatistics(startDate, endDate);

        log.info("Cart statistics retrieved");
        return ResponseEntity.ok(statistics);
    }
}
