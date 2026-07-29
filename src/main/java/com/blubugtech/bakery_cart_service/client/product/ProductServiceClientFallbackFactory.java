package com.blubugtech.bakery_cart_service.client.product;

import lombok.extern.slf4j.Slf4j;
import org.blubakery.bakery_common_libs.contract.feign.CouponValidationResponse;
import org.blubakery.bakery_common_libs.contract.feign.Product;
import org.blubakery.bakery_common_libs.contract.feign.ProductValidation;
import org.blubakery.bakery_common_libs.contract.feign.StockAvailability;
import org.blubakery.bakery_common_libs.contract.messaging.StockOperationRequestPayload;
import org.blubakery.bakery_common_libs.contract.messaging.StockOperationResponsePayload;
import org.blubakery.bakery_common_libs.exception.common.FeignClientException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class ProductServiceClientFallbackFactory implements FallbackFactory<ProductServiceClient> {

    @Override
    public ProductServiceClient create(Throwable cause) {
        return new ProductServiceClient() {
            @Override
            public Product getProductById(UUID productId) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                log.error("Fallback triggered for getProductById: {}", productId, cause);
                return null;
            }

            @Override
            public List<Product> getProductsByIds(List<UUID> productIds) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                log.error("Fallback triggered for getProductsByIds: {}", productIds, cause);
                return Collections.emptyList();
            }

            @Override
            public StockAvailability checkStockAvailability(UUID productId, Integer quantity) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                log.error("Fallback triggered for checkStockAvailability: {} for qty {}", productId, quantity, cause);
                StockAvailability dto = new StockAvailability();
                dto.setSufficient(false);
                dto.setAvailableQuantity(0);
                return dto;
            }

            @Override
            public StockOperationResponsePayload reserveStock(UUID productId, StockOperationRequestPayload request) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                log.error("Fallback triggered for reserveStock: {}", productId, cause);
                return createErrorResponse(productId);
            }

            @Override
            public StockOperationResponsePayload releaseStock(UUID productId, StockOperationRequestPayload request) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                log.error("Fallback triggered for releaseStock: {}", productId, cause);
                return createErrorResponse(productId);
            }

            @Override
            public List<ProductValidation> validateProducts(List<UUID> productIds) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                log.error("Fallback triggered for validateProducts: {}", productIds, cause);
                return Collections.emptyList();
            }

            @Override
            public CouponValidationResponse validateCoupon(String code, Double cartTotal) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                log.error("Fallback triggered for validateCoupon: {}", code, cause);
                throw new RuntimeException("Service unavailable");
            }

            private StockOperationResponsePayload createErrorResponse(UUID productId) {
                StockOperationResponsePayload dto = new StockOperationResponsePayload();
                dto.setProductId(productId);
                dto.setSuccess(false);
                dto.setMessage("Service unavailable");
                return dto;
            }
        };
    }
}
