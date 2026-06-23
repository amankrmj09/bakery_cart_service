# bakery_cart_service API Report

## CartController

### `POST` `/api/carts`
- **API Name:** createCart
- **Type:** REST / Synchronous
- **Request Headers:** 
  - `X-User-Id` (UUID, optional)
  - `X-Session-Id` (String, optional)
  - `X-User-Role` (String, optional)

**Request:**
```json
{
  "userId": "UUID - NULL for guest carts",
  "sessionId": "String - Max 255 chars",
  "customerName": "String - Max 100 chars",
  "customerEmail": "String - Valid email format",
  "currencyCode": "String - 3 chars (e.g., 'USD')",
  "discountCode": "String - Max 50 chars",
  "specialInstructions": "String",
  "deliveryType": "String - PICKUP or DELIVERY",
  "deliveryAddress": "String",
  "source": "String - WEB, MOBILE, API",
  "deviceType": "String - DESKTOP, MOBILE, TABLET",
  "userAgent": "String",
  "metadata": {
    "key": "value"
  }
}
```

**Response:**
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
      "addedAt": "DateTime",
      "updatedAt": "DateTime",
      "lastValidatedAt": "DateTime",
      "savedForLaterAt": "DateTime",
      "removedAt": "DateTime",
      "addedFrom": "String",
      "metadata": {}
    }
  ],
  "savedItems": [
    {
      "id": "UUID",
      "productId": "UUID",
      "..." : "..."
    }
  ],
  "isEmpty": "Boolean",
  "isExpired": "Boolean",
  "isGuest": "Boolean",
  "hasStockIssues": "Boolean",
  "hasPriceChanges": "Boolean",
  "createdAt": "DateTime",
  "updatedAt": "DateTime",
  "expiresAt": "DateTime",
  "lastActivityAt": "DateTime",
  "abandonedAt": "DateTime",
  "convertedAt": "DateTime",
  "convertedOrderId": "UUID",
  "source": "String",
  "deviceType": "String",
  "metadata": {}
}
```

---

### `GET` `/api/carts/{cartId}`
- **API Name:** getCartById
- **Type:** REST / Synchronous
- **Path Variable:** `cartId` (UUID)
- **Request Headers:** `X-User-Id`, `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Same as `createCart` CartResponse)*

---

### `GET` `/api/carts/user/{userId}`
- **API Name:** getOrCreateCartForUser
- **Type:** REST / Synchronous
- **Path Variable:** `userId` (UUID)
- **Request Headers:** `X-User-Id`, `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Same as `createCart` CartResponse)*

---

### `GET` `/api/carts/session/{sessionId}`
- **API Name:** getOrCreateCartForSession
- **Type:** REST / Synchronous
- **Path Variable:** `sessionId` (String)
- **Request Headers:** `X-Session-Id` (optional)

**Request:**
None

**Response:**
*(Same as `createCart` CartResponse)*

---

### `POST` `/api/carts/{cartId}/items`
- **API Name:** addItemToCart
- **Type:** REST / Synchronous
- **Path Variable:** `cartId` (UUID)
- **Request Headers:** `X-User-Id`, `X-User-Role` (optional)

**Request:**
```json
{
  "productId": "UUID - Required",
  "quantity": "Integer - Min 1, Max 50",
  "unitPriceOverride": "BigDecimal - Optional",
  "specialInstructions": "String - Optional",
  "addedFrom": "String - PRODUCT_PAGE, CATEGORY_PAGE, etc.",
  "metadata": {}
}
```

**Response:**
*(Same as `createCart` CartResponse)*

---

### `PUT` `/api/carts/{cartId}/items/{itemId}`
- **API Name:** updateCartItem
- **Type:** REST / Synchronous
- **Path Variables:** `cartId` (UUID), `itemId` (UUID)
- **Request Headers:** `X-User-Id`, `X-User-Role` (optional)

**Request:**
```json
{
  "quantity": "Integer - Min 1, Max 50",
  "specialInstructions": "String - Optional",
  "metadata": {}
}
```

**Response:**
*(Same as `createCart` CartResponse)*

---

### `DELETE` `/api/carts/{cartId}/items/{itemId}`
- **API Name:** removeItemFromCart
- **Type:** REST / Synchronous
- **Path Variables:** `cartId` (UUID), `itemId` (UUID)
- **Request Headers:** `X-User-Id`, `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Same as `createCart` CartResponse)*

---

### `DELETE` `/api/carts/{cartId}/items`
- **API Name:** clearCart
- **Type:** REST / Synchronous
- **Path Variable:** `cartId` (UUID)
- **Request Headers:** `X-User-Id`, `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Same as `createCart` CartResponse)*

---

### `PATCH` `/api/carts/{cartId}`
- **API Name:** updateCart
- **Type:** REST / Synchronous
- **Path Variable:** `cartId` (UUID)
- **Request Headers:** `X-User-Id`, `X-User-Role` (optional)

**Request:**
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

**Response:**
*(Same as `createCart` CartResponse)*

---

### `POST` `/api/carts/merge`
- **API Name:** mergeCarts
- **Type:** REST / Synchronous
- **Request Headers:** `X-User-Id`, `X-User-Role` (optional)

**Request:**
```json
{
  "sourceCartId": "UUID - Required",
  "targetCartId": "UUID - Required",
  "deleteSourceCart": "Boolean - Default true",
  "handleDuplicates": "Boolean - Default true"
}
```

**Response:**
*(Same as `createCart` CartResponse)*

---

### `POST` `/api/carts/{cartId}/save`
- **API Name:** saveCartForLater
- **Type:** REST / Synchronous
- **Path Variable:** `cartId` (UUID)
- **Request Headers:** `X-User-Id`, `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Same as `createCart` CartResponse)*

---

### `POST` `/api/carts/{cartId}/checkout`
- **API Name:** checkoutCart
- **Type:** REST / Synchronous
- **Path Variable:** `cartId` (UUID)
- **Request Headers:** `X-User-Id`, `X-User-Role` (optional)

**Request:**
```json
{
  "customerName": "String",
  "customerEmail": "String",
  "customerPhone": "String",
  "deliveryType": "String - PICKUP or DELIVERY",
  "deliveryAddress": "String",
  "deliveryDate": "DateTime",
  "specialInstructions": "String",
  "discountCode": "String",
  "paymentMethod": "String - CASH, CARD, DIGITAL_WALLET",
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
*(Exact map structure not defined in DTO, inferred from common checkout)*

---

### `GET` `/api/carts/user/{userId}/all`
- **API Name:** getUserCarts
- **Type:** REST / Synchronous
- **Path Variable:** `userId` (UUID)
- **Request Headers:** `X-User-Id`, `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of `createCart` CartResponse)*

---

### `GET` `/api/carts/status/{status}`
- **API Name:** getCartsByStatus
- **Type:** REST / Synchronous
- **Requires Role:** ADMIN
- **Path Variable:** `status` (String - CartStatus)
- **Request Headers:** `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of `createCart` CartResponse)*

---

### `GET` `/api/carts/statistics`
- **API Name:** getCartStatistics
- **Type:** REST / Synchronous
- **Requires Role:** ADMIN
- **Query Parameters:** `startDate` (DateTime), `endDate` (DateTime)

**Request:**
None

**Response:**
```json
{
  "totalCarts": "Integer",
  "activeCarts": "Integer",
  "abandonedCarts": "Integer",
  "conversionRate": "Double"
}
```
*(Inferred from Map<String, Object>)*

---

## CartItemController

### `GET` `/api/cart-items/{itemId}`
- **API Name:** getCartItemById
- **Type:** REST / Synchronous
- **Path Variable:** `itemId` (UUID)
- **Request Headers:** `X-User-Id`, `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Returns single CartItemResponse, structure documented in CartResponse `items` list)*

---

### `GET` `/api/cart-items/cart/{cartId}`
- **API Name:** getCartItems
- **Type:** REST / Synchronous
- **Path Variable:** `cartId` (UUID)
- **Request Headers:** `X-User-Id`, `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of CartItemResponse)*

---

### `GET` `/api/cart-items/cart/{cartId}/saved`
- **API Name:** getSavedItems
- **Type:** REST / Synchronous
- **Path Variable:** `cartId` (UUID)
- **Request Headers:** `X-User-Id`, `X-User-Role` (optional)

**Request:**
None

**Response:**
*(List of CartItemResponse)*

---

### `POST` `/api/cart-items/{itemId}/save-for-later`
- **API Name:** saveItemForLater
- **Type:** REST / Synchronous
- **Path Variable:** `itemId` (UUID)
- **Request Headers:** `X-User-Id`, `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Returns CartItemResponse)*

---

### `POST` `/api/cart-items/{itemId}/move-to-cart`
- **API Name:** moveItemToCart
- **Type:** REST / Synchronous
- **Path Variable:** `itemId` (UUID)
- **Request Headers:** `X-User-Id`, `X-User-Role` (optional)

**Request:**
None

**Response:**
*(Returns CartItemResponse)*
