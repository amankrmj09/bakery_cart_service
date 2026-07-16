package com.blubugtech.bakery_cart_service.client.product;

import com.blubugtech.common.contract.feign.Product;
import com.blubugtech.common.contract.feign.ProductValidation;
import com.blubugtech.common.contract.feign.StockAvailability;
import com.blubugtech.common.contract.messaging.StockOperationRequestPayload;
import com.blubugtech.common.contract.messaging.StockOperationResponsePayload;
import com.blubugtech.common.exception.common.FeignClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class ProductServiceClientFallbackFactory implements FallbackFactory<ProductServiceClient> {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceClientFallbackFactory.class);

    @Override
    public ProductServiceClient create(Throwable cause) {
        return new ProductServiceClient() {
            @Override
            public Product getProductById(UUID productId) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                logger.error("Fallback triggered for getProductById: {}", productId, cause);
                return null;
            }

            @Override
            public List<Product> getProductsByIds(List<UUID> productIds) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                logger.error("Fallback triggered for getProductsByIds: {}", productIds, cause);
                return Collections.emptyList();
            }

            @Override
            public StockAvailability checkStockAvailability(UUID productId, Integer quantity) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                logger.error("Fallback triggered for checkStockAvailability: {} for qty {}", productId, quantity, cause);
                StockAvailability dto = new StockAvailability();
                dto.setSufficient(false);
                dto.setAvailableQuantity(0);
                return dto;
            }

            @Override
            public StockOperationResponsePayload reserveStock(UUID productId, StockOperationRequestPayload request) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                logger.error("Fallback triggered for reserveStock: {}", productId, cause);
                return createErrorResponse(productId);
            }

            @Override
            public StockOperationResponsePayload releaseStock(UUID productId, StockOperationRequestPayload request) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                logger.error("Fallback triggered for releaseStock: {}", productId, cause);
                return createErrorResponse(productId);
            }

            @Override
            public List<ProductValidation> validateProducts(List<UUID> productIds) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                logger.error("Fallback triggered for validateProducts: {}", productIds, cause);
                return Collections.emptyList();
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
