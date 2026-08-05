package com.blubugtech.bakery_cart_service.client.product;

import lombok.extern.slf4j.Slf4j;
import org.blubakery.common.feign.contract.feign.CouponValidationResponse;
import org.blubakery.common.feign.contract.feign.Product;
import org.blubakery.common.feign.contract.feign.ProductValidation;
import org.blubakery.common.feign.contract.feign.StockAvailability;
import org.blubakery.common.messaging.stock.StockOperationRequestPayload;
import org.blubakery.common.messaging.stock.StockOperationResponsePayload;
import org.blubakery.common.feign.exception.common.FeignClientException;
import org.blubakery.common.core.exception.common.ServiceUnavailableException;
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
                throw new ServiceUnavailableException("Product Service is currently unavailable. Please try again later.", cause);
            }

            @Override
            public List<Product> getProductsByIds(List<UUID> productIds) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                log.error("Fallback triggered for getProductsByIds: {}", productIds, cause);
                throw new ServiceUnavailableException("Product Service is currently unavailable. Please try again later.", cause);
            }

            @Override
            public StockAvailability checkStockAvailability(UUID productId, Integer quantity) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                log.error("Fallback triggered for checkStockAvailability: {} for qty {}", productId, quantity, cause);
                throw new ServiceUnavailableException("Product Service is currently unavailable. Please try again later.", cause);
            }

            @Override
            public StockOperationResponsePayload reserveStock(UUID productId, StockOperationRequestPayload request) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                log.error("Fallback triggered for reserveStock: {}", productId, cause);
                throw new ServiceUnavailableException("Product Service is currently unavailable. Please try again later.", cause);
            }

            @Override
            public StockOperationResponsePayload releaseStock(UUID productId, StockOperationRequestPayload request) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                log.error("Fallback triggered for releaseStock: {}", productId, cause);
                throw new ServiceUnavailableException("Product Service is currently unavailable. Please try again later.", cause);
            }

            @Override
            public List<ProductValidation> validateProducts(List<UUID> productIds) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                log.error("Fallback triggered for validateProducts: {}", productIds, cause);
                throw new ServiceUnavailableException("Product Service is currently unavailable. Please try again later.", cause);
            }

            @Override
            public CouponValidationResponse validateCoupon(String code, Double cartTotal) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                log.error("Fallback triggered for validateCoupon: {}", code, cause);
                throw new ServiceUnavailableException("Product Service is currently unavailable. Please try again later.", cause);
            }
        };
    }
}
