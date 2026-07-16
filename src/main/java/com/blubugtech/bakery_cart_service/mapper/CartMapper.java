package com.blubugtech.bakery_cart_service.mapper;

import com.blubugtech.bakery_cart_service.dto.cart.CartResponse;
import com.blubugtech.bakery_cart_service.entity.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class, JsonMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {
    @Mapping(target = "metadata", source = "metadata", qualifiedByName = "stringToMap")
    CartResponse toDto(Cart cart);
}
