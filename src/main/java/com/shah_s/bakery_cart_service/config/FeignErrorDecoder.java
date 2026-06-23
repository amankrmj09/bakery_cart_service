package com.shah_s.bakery_cart_service.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import com.shah_s.bakery_cart_service.exception.FeignClientException;

import java.io.InputStream;

public class FeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        HttpStatus status = HttpStatus.valueOf(response.status());
        try (InputStream is = response.body().asInputStream()) {
            JsonNode errorResponse = objectMapper.readTree(is);
            String message = errorResponse.has("message") ? errorResponse.get("message").asText() : "Unknown external error";
            return new FeignClientException(message, status);
        } catch (Exception e) {
            return new FeignClientException("Unknown error occurred during external call", status);
        }
    }
}
