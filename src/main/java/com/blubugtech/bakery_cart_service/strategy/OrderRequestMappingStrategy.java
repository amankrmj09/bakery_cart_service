package com.blubugtech.bakery_cart_service.strategy;

import com.blubugtech.bakery_cart_service.dto.checkout.CheckoutRequest;
import com.blubugtech.bakery_cart_service.dto.order.CreateOrderRequest;
import com.blubugtech.bakery_cart_service.entity.Cart;
import com.blubugtech.bakery_cart_service.entity.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderRequestMappingStrategy {

    private final ShippingFeeCalculationStrategy shippingFeeCalculationStrategy;

    public OrderRequestMappingStrategy(ShippingFeeCalculationStrategy shippingFeeCalculationStrategy) {
        this.shippingFeeCalculationStrategy = shippingFeeCalculationStrategy;
    }

    public CreateOrderRequest createOrderRequest(Cart cart, CheckoutRequest request) {
        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setUserId(cart.getUserId());
        orderRequest.setCustomerName(request.getCustomerName());
        orderRequest.setCustomerEmail(request.getCustomerEmail());
        orderRequest.setCustomerPhone(request.getCustomerPhone());
        orderRequest.setDeliveryType(request.getDeliveryType());
        orderRequest.setDeliveryAddress(request.getDeliveryAddress());
        orderRequest.setDeliveryDate(request.getDeliveryDate());
        orderRequest.setSpecialInstructions(request.getSpecialInstructions());
        orderRequest.setDiscountCode(request.getDiscountCode());
        orderRequest.setDiscountAmount(cart.getDiscountAmount());
        orderRequest.setTaxAmount(cart.getTaxAmount());

        // Payment information
        orderRequest.setPaymentMethod(request.getPaymentMethod());
        
        BigDecimal paymentAmount = cart.getTotalAmount();
        BigDecimal shippingFee = shippingFeeCalculationStrategy.calculateShippingFee(request.getDeliveryType());
        paymentAmount = paymentAmount.add(shippingFee);
        
        orderRequest.setPaymentAmount(paymentAmount);
        
        orderRequest.setCurrencyCode(cart.getCurrencyCode());
        orderRequest.setCardLastFour(request.getCardLastFour());
        orderRequest.setCardBrand(request.getCardBrand());
        orderRequest.setCardType(request.getCardType());
        orderRequest.setDigitalWalletProvider(request.getDigitalWalletProvider());
        orderRequest.setBankName(request.getBankName());
        orderRequest.setPaymentNotes(request.getPaymentNotes());

        // Order items
        List<CreateOrderRequest.OrderItemDto> items = cart.getActiveItems().stream()
                .map(this::convertCartItemToOrderItem)
                .collect(Collectors.toList());
        orderRequest.setItems(items);

        return orderRequest;
    }

    private CreateOrderRequest.OrderItemDto convertCartItemToOrderItem(CartItem cartItem) {
        CreateOrderRequest.OrderItemDto orderItem = new CreateOrderRequest.OrderItemDto();
        orderItem.setProductId(cartItem.getProductId());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setUnitPriceOverride(cartItem.getUnitPrice());
        orderItem.setSpecialInstructions(cartItem.getSpecialInstructions());
        return orderItem;
    }
}
