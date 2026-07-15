package com.blubugtech.bakery_cart_service.dto.order;

import java.util.UUID;

public class OrderResponseDto {
    private UUID id;
    private String orderNumber;
    private String status;

    public OrderResponseDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
