# Bakery Cart Service API Reference

This document provides a comprehensive reference of all endpoints exposed by the Bakery Cart Service.

## Common DTOs

### CartResponseDto (Response)
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
  ],
  "savedItems": [
    /* Same structure as items above */
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

### CartItemResponseDto (Response)
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

---

## CartController (`/api/carts`)

### `POST /api/carts`
**Description:** Create a new cart.  
**Request Headers:** `X-User-Id`, `X-Session-Id`, `X-User-Role`

**Request Body (CartRequestDto):**
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

**Response:** `CartResponseDto`

---

### `GET /api/carts/{cartId}`
**Description:** Get cart by ID.  
**Request Headers:** `X-User-Id`, `X-User-Role`

**Response:** `CartResponseDto`

---

### `GET /api/carts/me`
**Description:** Get 'me' cart based on user ID or session ID from headers.  
**Request Headers:** `X-User-Id`, `X-Session-Id`

**Response:** `CartResponseDto`

---

### `GET /api/carts/user/{userId}`
**Description:** Get or create cart for user.  
**Request Headers:** `X-User-Id`, `X-User-Role`

**Response:** `CartResponseDto`

---

### `GET /api/carts/session/{sessionId}`
**Description:** Get or create cart for session.  
**Request Headers:** `X-Session-Id`

**Response:** `CartResponseDto`

---

### `POST /api/carts/{cartId}/items`
**Description:** Add item to cart.  
**Request Headers:** `X-User-Id`, `X-User-Role`

**Request Body (AddItemRequestDto):**
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

**Response:** `CartResponseDto`

---

### `POST /api/carts/me/items`
**Description:** Add item to 'me' cart.  
**Request Headers:** `X-User-Id`, `X-Session-Id`, `X-User-Role`

**Request Body (AddItemRequestDto):**
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

**Response:** `CartResponseDto`

---

### `PUT /api/carts/{cartId}/items/{itemId}`
**Description:** Update item in cart.  
**Request Headers:** `X-User-Id`, `X-User-Role`

**Request Body (UpdateItemRequestDto):**
```json
{
  "quantity": "Integer (required, min 1, max 50)",
  "specialInstructions": "String",
  "metadata": {}
}
```

**Response:** `CartResponseDto`

---

### `DELETE /api/carts/{cartId}/items/{itemId}`
**Description:** Remove item from cart.  
**Request Headers:** `X-User-Id`, `X-User-Role`

**Response:** `CartResponseDto`

---

### `DELETE /api/carts/{cartId}/items`
**Description:** Clear cart.  
**Request Headers:** `X-User-Id`, `X-User-Role`

**Response:** `CartResponseDto`

---

### `PATCH /api/carts/{cartId}`
**Description:** Update cart details.  
**Request Headers:** `X-User-Id`, `X-User-Role`

**Request Body (CartUpdateRequestDto):**
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

**Response:** `CartResponseDto`

---

### `POST /api/carts/merge`
**Description:** Merge two carts.  
**Request Headers:** `X-User-Id`, `X-User-Role`

**Request Body (MergeCartsRequestDto):**
```json
{
  "sourceCartId": "UUID (required)",
  "targetCartId": "UUID (required)",
  "deleteSourceCart": "Boolean (default true)",
  "handleDuplicates": "Boolean (default true)"
}
```

**Response:** `CartResponseDto`

---

### `POST /api/carts/{cartId}/save`
**Description:** Save cart for later.  
**Request Headers:** `X-User-Id`, `X-User-Role`

**Response:** `CartResponseDto`

---

### `POST /api/carts/{cartId}/checkout`
**Description:** Checkout cart.  
**Request Headers:** `X-User-Id`, `X-User-Role`

**Request Body (CheckoutRequestDto):**
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

**Response:**
```json
{
  "orderId": "UUID",
  "status": "String",
  "message": "String"
}
```

---

### `POST /api/carts/me/checkout`
**Description:** Checkout 'me' cart.  
**Request Headers:** `X-User-Id`, `X-Session-Id`, `X-User-Role`

**Request Body (CheckoutRequestDto):** *(Same as above)*

**Response:** `Map<String, Object>`

---

### `GET /api/carts/user/{userId}/all`
**Description:** Get all carts for a user.  
**Request Headers:** `X-User-Id`, `X-User-Role`

**Response:** List of `CartResponseDto`
```json
[
  { /* CartResponseDto */ }
]
```

---

### `GET /api/carts/status/{status}`
**Description:** Get carts by status (Admin only).  
**Request Headers:** `X-User-Role` (must be ADMIN)

**Response:** List of `CartResponseDto`

---

### `GET /api/carts`
**Description:** Get all carts with pagination (Admin only).  
**Request Headers:** `X-User-Role` (must be ADMIN)  
**Query Parameters:** 
- `page` (int, default 0)
- `size` (int, default 20)
- `sortBy` (String, default "updatedAt")
- `sortDir` (String, default "DESC")

**Response:** Page of `CartResponseDto`
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

---

### `GET /api/carts/statistics`
**Description:** Get cart statistics (Admin only).  
**Request Headers:** `X-User-Role` (must be ADMIN)  
**Query Parameters:**
- `startDate` (DateTime, optional)
- `endDate` (DateTime, optional)

**Response:**
```json
{
  "totalCarts": "Integer",
  "activeCarts": "Integer",
  "abandonedCarts": "Integer",
  "conversionRate": "Double"
}
```

---

### `GET /api/carts/health`
**Description:** Health check for Cart Controller.

**Response:**
```json
{
  "status": "UP",
  "service": "cart-service-carts",
  "timestamp": "LocalDateTime"
}
```

---

## CartItemController (`/api/cart-items`)

### `GET /api/cart-items/{itemId}`
**Description:** Get cart item by ID.  
**Request Headers:** `X-User-Id`, `X-User-Role`

**Response:** `CartItemResponseDto`

---

### `GET /api/cart-items/cart/{cartId}`
**Description:** Get all items for a cart.  
**Request Headers:** `X-User-Id`, `X-User-Role`

**Response:** List of `CartItemResponseDto`
```json
[
  { /* CartItemResponseDto */ }
]
```

---

### `GET /api/cart-items/cart/{cartId}/saved`
**Description:** Get saved items for a cart.  
**Request Headers:** `X-User-Id`, `X-User-Role`

**Response:** List of `CartItemResponseDto`

---

### `POST /api/cart-items/{itemId}/save-for-later`
**Description:** Save item for later.  
**Request Headers:** `X-User-Id`, `X-User-Role`

**Response:** `CartItemResponseDto`

---

### `POST /api/cart-items/{itemId}/move-to-cart`
**Description:** Move saved item back to cart.  
**Request Headers:** `X-User-Id`, `X-User-Role`

**Response:** `CartItemResponseDto`

---

### `GET /api/cart-items/health`
**Description:** Health check for Cart Item Controller.

**Response:**
```json
{
  "status": "UP",
  "service": "cart-service-items",
  "timestamp": "LocalDateTime"
}
```

---

## HealthController (`/api`)

### `GET /api/health`
**Description:** Main service health check, including Database and Redis connectivity.

**Response:**
```json
{
  "status": "UP",
  "service": "bakery-cart-service",
  "timestamp": "LocalDateTime",
  "version": "1.0.0",
  "database": "UP",
  "databaseUrl": "String",
  "redis": "UP"
}
```

---

### `GET /api/info`
**Description:** Service info details.

**Response:**
```json
{
  "serviceName": "Bakery Cart Service",
  "description": "Shopping cart management and session handling service",
  "version": "1.0.0",
  "features": {
    "carts": "User and guest cart management",
    "persistence": "Redis caching with PostgreSQL persistence",
    "validation": "Real-time stock and price validation",
    "checkout": "Seamless order creation integration",
    "analytics": "Cart abandonment and conversion tracking"
  },
  "endpoints": {
    "carts": "/api/carts",
    "items": "/api/cart-items"
  }
}
```

---

### `GET /api/metrics`
**Description:** Service metrics, including uptime and memory usage.

**Response:**
```json
{
  "uptime": "String (e.g., '0 days, 2 hours, 15 minutes, 30 seconds')",
  "timestamp": "LocalDateTime",
  "memory": {
    "maxMemory": "String (MB)",
    "totalMemory": "String (MB)",
    "freeMemory": "String (MB)",
    "usedMemory": "String (MB)"
  },
  "cache": {
    "redisConnections": "active",
    "cacheHitRate": "N/A"
  }
}
```
