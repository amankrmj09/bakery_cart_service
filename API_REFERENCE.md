# Bakery Cart Service API Reference

This document provides a comprehensive reference of all endpoints exposed by the Bakery Cart Service.

---

## Cart Controller
**Base Path:** `/api/carts`

### 1. Create a new cart
- **Method:** `POST`
- **Path:** `/api/carts`
- **Type of API:** `User`
- **Request Body:**
  ```json
  {
    "userId": "UUID",
    "sessionId": "String",
    "customerName": "String",
    "customerEmail": "String",
    "currencyCode": "String (default USD)",
    "discountCode": "String",
    "specialInstructions": "String",
    "deliveryType": "String",
    "deliveryAddress": "String",
    "source": "String",
    "deviceType": "String",
    "userAgent": "String",
    "metadata": {}
  }
  ```
- **Response Body:** `200 OK`
  `CartResponseDto`

### 2. Get cart by ID
- **Method:** `GET`
- **Path:** `/api/carts/{cartId}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `CartResponseDto`

### 3. Get 'me' cart
- **Method:** `GET`
- **Path:** `/api/carts/me`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `CartResponseDto`

### 4. Get or create cart for user
- **Method:** `GET`
- **Path:** `/api/carts/user/{userId}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `CartResponseDto`

### 5. Get or create cart for session
- **Method:** `GET`
- **Path:** `/api/carts/session/{sessionId}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `CartResponseDto`

### 6. Add item to cart
- **Method:** `POST`
- **Path:** `/api/carts/{cartId}/items`
- **Type of API:** `User`
- **Request Body:**
  ```json
  {
    "productId": "UUID (required)",
    "quantity": "Integer (required, min 1, max 50)",
    "unitPriceOverride": "BigDecimal",
    "specialInstructions": "String",
    "addedFrom": "String",
    "metadata": {}
  }
  ```
- **Response Body:** `200 OK`
  `CartResponseDto`

### 7. Add item to 'me' cart
- **Method:** `POST`
- **Path:** `/api/carts/me/items`
- **Type of API:** `User`
- **Request Body:**
  ```json
  {
    "productId": "UUID (required)",
    "quantity": "Integer (required, min 1, max 50)",
    "unitPriceOverride": "BigDecimal",
    "specialInstructions": "String",
    "addedFrom": "String",
    "metadata": {}
  }
  ```
- **Response Body:** `200 OK`
  `CartResponseDto`

### 8. Update item in cart
- **Method:** `PUT`
- **Path:** `/api/carts/{cartId}/items/{itemId}`
- **Type of API:** `User`
- **Request Body:**
  ```json
  {
    "quantity": "Integer (required, min 1, max 50)",
    "specialInstructions": "String",
    "metadata": {}
  }
  ```
- **Response Body:** `200 OK`
  `CartResponseDto`

### 9. Remove item from cart
- **Method:** `DELETE`
- **Path:** `/api/carts/{cartId}/items/{itemId}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `CartResponseDto`

### 10. Clear cart
- **Method:** `DELETE`
- **Path:** `/api/carts/{cartId}/items`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `CartResponseDto`

### 11. Update cart details
- **Method:** `PATCH`
- **Path:** `/api/carts/{cartId}`
- **Type of API:** `User`
- **Request Body:**
  ```json
  {
    "customerName": "String",
    "customerEmail": "String",
    "discountCode": "String",
    "specialInstructions": "String",
    "deliveryType": "String",
    "deliveryAddress": "String",
    "metadata": {}
  }
  ```
- **Response Body:** `200 OK`
  `CartResponseDto`

### 12. Merge two carts
- **Method:** `POST`
- **Path:** `/api/carts/merge`
- **Type of API:** `User`
- **Request Body:**
  ```json
  {
    "sourceCartId": "UUID (required)",
    "targetCartId": "UUID (required)",
    "deleteSourceCart": "Boolean (default true)",
    "handleDuplicates": "Boolean (default true)"
  }
  ```
- **Response Body:** `200 OK`
  `CartResponseDto`

### 13. Save cart for later
- **Method:** `POST`
- **Path:** `/api/carts/{cartId}/save`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `CartResponseDto`

### 14. Checkout cart
- **Method:** `POST`
- **Path:** `/api/carts/{cartId}/checkout`
- **Type of API:** `User`
- **Request Body:**
  ```json
  {
    "customerName": "String (required)",
    "customerEmail": "String (required)",
    "customerPhone": "String",
    "deliveryType": "String (required)",
    "deliveryAddress": "String",
    "deliveryDate": "LocalDateTime",
    "specialInstructions": "String",
    "discountCode": "String",
    "paymentMethod": "String (required)",
    "cardLastFour": "String",
    "cardBrand": "String",
    "cardType": "String",
    "digitalWalletProvider": "String",
    "bankName": "String",
    "paymentNotes": "String",
    "metadata": {}
  }
  ```
- **Response Body:** `200 OK`
  ```json
  {
    "orderId": "UUID",
    "status": "String",
    "message": "String"
  }
  ```

### 15. Checkout 'me' cart
- **Method:** `POST`
- **Path:** `/api/carts/me/checkout`
- **Type of API:** `User`
- **Request Body:**
  *(Same as Checkout cart)*
- **Response Body:** `200 OK`
  *(Same as Checkout cart Response)*

### 16. Get all carts for a user
- **Method:** `GET`
- **Path:** `/api/carts/user/{userId}/all`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  ```json
  [
    { /* CartResponseDto */ }
  ]
  ```

### 17. Get carts by status
- **Method:** `GET`
- **Path:** `/api/carts/status/{status}`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  `List<CartResponseDto>`

### 18. Get all carts (Paginated)
- **Method:** `GET`
- **Path:** `/api/carts`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  ```json
  {
    "content": [
      { /* CartResponseDto */ }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": 0,
    "totalPages": 0,
    "last": true
  }
  ```

### 19. Get cart statistics
- **Method:** `GET`
- **Path:** `/api/carts/statistics`
- **Type of API:** `Admin`
- **Request Body:** None
- **Response Body:** `200 OK`
  ```json
  {
    "totalCarts": "Integer",
    "activeCarts": "Integer",
    "abandonedCarts": "Integer",
    "conversionRate": "Double"
  }
  ```


---

## CartItem Controller
**Base Path:** `/api/cart-items`

### 1. Get cart item by ID
- **Method:** `GET`
- **Path:** `/api/cart-items/{itemId}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `CartItemResponseDto`

### 2. Get all items for a cart
- **Method:** `GET`
- **Path:** `/api/cart-items/cart/{cartId}`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `List<CartItemResponseDto>`

### 3. Get saved items for a cart
- **Method:** `GET`
- **Path:** `/api/cart-items/cart/{cartId}/saved`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `List<CartItemResponseDto>`

### 4. Save item for later
- **Method:** `POST`
- **Path:** `/api/cart-items/{itemId}/save-for-later`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `CartItemResponseDto`

### 5. Move saved item back to cart
- **Method:** `POST`
- **Path:** `/api/cart-items/{itemId}/move-to-cart`
- **Type of API:** `User`
- **Request Body:** None
- **Response Body:** `200 OK`
  `CartItemResponseDto`


---

## System & Monitoring (Actuator)
**Base Path:** `/actuator`

Standard Spring Boot Actuator endpoints are used for monitoring and metrics.

### 1. Health Check
- **Method:** `GET`
- **Path:** `/actuator/health`
- **Type of API:** `Public`
- **Response Body:** `200 OK` (Standard Actuator Health JSON)

### 2. Service Info
- **Method:** `GET`
- **Path:** `/actuator/info`
- **Type of API:** `Public`
- **Response Body:** `200 OK` (Standard Actuator Info JSON)

### 3. Prometheus Metrics
- **Method:** `GET`
- **Path:** `/actuator/prometheus`
- **Type of API:** `Public`
- **Response Body:** `200 OK` (Prometheus Text Format)

---

## Common DTOs

### CartResponseDto
```json
{
  "id": "UUID",
  "userId": "UUID",
  "sessionId": "String",
  "status": "String (e.g., ACTIVE, ABANDONED, CHECKED_OUT)",
  "customerName": "String",
  "customerEmail": "String",
  "subtotal": "BigDecimal",
  "taxAmount": "BigDecimal",
  "discountAmount": "BigDecimal",
  "totalAmount": "BigDecimal",
  "itemCount": "Integer",
  "totalQuantity": "Integer",
  "currencyCode": "String",
  "discountCode": "String",
  "specialInstructions": "String",
  "deliveryType": "String",
  "deliveryAddress": "String",
  "items": [
    { /* CartItemResponseDto */ }
  ],
  "savedItems": [
    { /* CartItemResponseDto */ }
  ],
  "isEmpty": "Boolean",
  "isExpired": "Boolean",
  "isGuest": "Boolean",
  "hasStockIssues": "Boolean",
  "hasPriceChanges": "Boolean",
  "createdAt": "LocalDateTime",
  "updatedAt": "LocalDateTime",
  "expiresAt": "LocalDateTime",
  "lastActivityAt": "LocalDateTime",
  "abandonedAt": "LocalDateTime",
  "convertedAt": "LocalDateTime",
  "convertedOrderId": "UUID",
  "source": "String",
  "deviceType": "String",
  "metadata": {
    "raw": {}
  }
}
```

### CartItemResponseDto
```json
{
  "id": "UUID",
  "productId": "UUID",
  "productSku": "String",
  "productName": "String",
  "productCategory": "String",
  "quantity": "Integer",
  "unitPrice": "BigDecimal",
  "totalPrice": "BigDecimal",
  "originalUnitPrice": "BigDecimal",
  "status": "String",
  "specialInstructions": "String",
  "productDescription": "String",
  "productImageUrl": "String",
  "preparationTimeMinutes": "Integer",
  "currencyCode": "String",
  "isAvailable": "Boolean",
  "stockQuantity": "Integer",
  "availabilityMessage": "String",
  "priceChanged": "Boolean",
  "priceChangeAmount": "BigDecimal",
  "hasStockIssue": "Boolean",
  "addedAt": "LocalDateTime",
  "updatedAt": "LocalDateTime",
  "lastValidatedAt": "LocalDateTime",
  "savedForLaterAt": "LocalDateTime",
  "removedAt": "LocalDateTime",
  "addedFrom": "String",
  "metadata": {
    "raw": {}
  }
}
```
