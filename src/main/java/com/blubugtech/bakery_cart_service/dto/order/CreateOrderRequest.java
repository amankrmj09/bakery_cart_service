package com.blubugtech.bakery_cart_service.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class CreateOrderRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Customer name is required")
    @Size(max = 200)
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email
    @Size(max = 255)
    private String customerEmail;

    private String customerPhone;

    @NotNull(message = "Delivery type is required")
    private String deliveryType;
    private String deliveryAddress;
    private LocalDateTime deliveryDate;
    private String specialInstructions;
    private String discountCode;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal paymentAmount;
    private String currencyCode;
    private String cardLastFour;
    private String cardBrand;
    private String cardType;
    private String digitalWalletProvider;
    private String bankName;
    private String paymentNotes;
    
    @Valid
    @NotEmpty(message="At least one item is required")
    private List<OrderItemDto> items;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class OrderItemDto {
        private UUID productId;
        private Integer quantity;
        private BigDecimal unitPriceOverride;
        private String specialInstructions;
    }
}
