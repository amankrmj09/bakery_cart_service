package com.blubugtech.bakery_cart_service.client;

import com.blubugtech.common.dto.ProductDto;
import com.blubugtech.common.dto.ProductValidationDto;
import com.blubugtech.common.dto.StockAvailabilityDto;
import com.blubugtech.common.dto.StockOperationRequestDto;
import com.blubugtech.common.dto.StockOperationResponseDto;
import com.blubugtech.common.exception.FeignClientException;
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
            public ProductDto getProductById(UUID productId) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                logger.error("Fallback triggered for getProductById: {}", productId, cause);
                return null;
            }

            @Override
            public List<ProductDto> getProductsByIds(List<UUID> productIds) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                logger.error("Fallback triggered for getProductsByIds: {}", productIds, cause);
                return Collections.emptyList();
            }

            @Override
            public StockAvailabilityDto checkStockAvailability(UUID productId, Integer quantity) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                logger.error("Fallback triggered for checkStockAvailability: {} for qty {}", productId, quantity, cause);
                StockAvailabilityDto dto = new StockAvailabilityDto();
                dto.setSufficient(false);
                dto.setAvailableQuantity(0);
                return dto;
            }

            @Override
            public StockOperationResponseDto reserveStock(UUID productId, StockOperationRequestDto request) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                logger.error("Fallback triggered for reserveStock: {}", productId, cause);
                return createErrorResponse(productId);
            }

            @Override
            public StockOperationResponseDto releaseStock(UUID productId, StockOperationRequestDto request) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                logger.error("Fallback triggered for releaseStock: {}", productId, cause);
                return createErrorResponse(productId);
            }

            @Override
            public List<ProductValidationDto> validateProducts(List<UUID> productIds) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                logger.error("Fallback triggered for validateProducts: {}", productIds, cause);
                return Collections.emptyList();
            }

            private StockOperationResponseDto createErrorResponse(UUID productId) {
                StockOperationResponseDto dto = new StockOperationResponseDto();
                dto.setProductId(productId);
                dto.setSuccess(false);
                dto.setMessage("Service unavailable");
                return dto;
            }
        };
    }
}
