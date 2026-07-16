package com.blubugtech.bakery_cart_service.dto.checkout;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class CheckoutRequest {

    // Getters and Setters
    @NotBlank(message = "Customer name is required")
    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String customerEmail;

    @Size(max = 20, message = "Customer phone must not exceed 20 characters")
    private String customerPhone;

    @NotBlank(message = "Delivery type is required")
    @Size(max = 20, message = "Delivery type must not exceed 20 characters")
    private String deliveryType; // PICKUP, DELIVERY

    private String deliveryAddress;

    private LocalDateTime deliveryDate;

    private String specialInstructions;

    @Size(max = 50, message = "Discount code must not exceed 50 characters")
    private String discountCode;

    @NotBlank(message = "Payment method is required")
    @Size(max = 20, message = "Payment method must not exceed 20 characters")
    private String paymentMethod; // CASH, CARD, DIGITAL_WALLET

    // Card payment details (if applicable)
    @Size(max = 4, message = "Card last four must be 4 digits")
    private String cardLastFour;

    @Size(max = 20, message = "Card brand must not exceed 20 characters")
    private String cardBrand;

    @Size(max = 20, message = "Card type must not exceed 20 characters")
    private String cardType;

    // Digital wallet details (if applicable)
    @Size(max = 50, message = "Wallet provider must not exceed 50 characters")
    private String digitalWalletProvider;

    // Bank details (if applicable)
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    private String paymentNotes;

    private Map<String, Object> metadata;

    // Constructors
    public CheckoutRequest() {}

}
