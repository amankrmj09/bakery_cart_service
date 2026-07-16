package com.blubugtech.bakery_cart_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import java.util.Map;

@Mapper(componentModel = "spring")
public class JsonMapper {
    @Named("stringToMap")
    public Map<String, Object> stringToMap(String metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        return Map.of("raw", metadata);
    }
}
