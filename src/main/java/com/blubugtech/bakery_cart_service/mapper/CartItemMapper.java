package com.blubugtech.bakery_cart_service.mapper;

import com.blubugtech.bakery_cart_service.dto.cartitem.CartItemResponse;
import com.blubugtech.bakery_cart_service.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {JsonMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartItemMapper {
    @Mapping(target = "metadata", source = "metadata", qualifiedByName = "stringToMap")
    CartItemResponse toDto(CartItem cartItem);
}
