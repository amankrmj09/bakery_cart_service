# Bakery Cart Service API Reference

This document provides a comprehensive API reference for all REST endpoints exposed by the **Bakery Cart Service** microservice (`com.blubugtech.bakery_cart_service`).

---

## 📋 Summary of Endpoints

| Group | Method | Endpoint | Description | Access |
|-------|--------|----------|-------------|--------|
| **Cart** | `POST` | `/api/carts` | Create a new shopping cart | User / Guest |
| **Cart** | `GET` | `/api/carts/{cartId}` | Get cart by cart ID | User / Guest / Admin |
| **Cart** | `GET` | `/api/carts/me` | Get active cart for current user or session | User / Guest |
| **Cart** | `GET` | `/api/carts/user/{userId}` | Get or create cart for user | User / Admin |
| **Cart** | `GET` | `/api/carts/session/{sessionId}` | Get or create cart for guest session | Guest |
| **Cart** | `PATCH` | `/api/carts/{cartId}` | Update cart details | User / Admin |
| **Cart** | `POST` | `/api/carts/merge` | Merge source cart into target cart | User / Admin |
| **Cart** | `POST` | `/api/carts/{cartId}/save` | Save entire cart for later | User / Admin |
| **Cart** | `GET` | `/api/carts/user/{userId}/all` | Get all carts for user (Paginated) | User / Admin |
| **Cart Items** | `GET` | `/api/cart-items/{itemId}` | Get specific cart item details | User / Admin |
| **Cart Items** | `GET` | `/api/cart-items/cart/{cartId}` | List active items in a cart (Paginated) | User / Admin |
| **Cart Items** | `GET` | `/api/cart-items/cart/{cartId}/saved` | List saved-for-later items in cart (Paginated) | User / Admin |
| **Cart Items** | `POST` | `/api/cart-items/{itemId}/save-for-later` | Save individual item for later | User / Admin |
| **Cart Items** | `POST` | `/api/cart-items/{itemId}/move-to-cart` | Move saved item back to active cart | User / Admin |
| **Cart Items** | `POST` | `/api/carts/{cartId}/items` | Add item to a cart | User / Admin |
| **Cart Items** | `POST` | `/api/carts/me/items` | Add item to current user/session cart | User / Guest |
| **Cart Items** | `PUT` | `/api/carts/{cartId}/items/{itemId}` | Update item quantity or instructions | User / Admin |
| **Cart Items** | `DELETE` | `/api/carts/{cartId}/items/{itemId}` | Remove item from cart | User / Admin |
| **Cart Items** | `DELETE` | `/api/carts/{cartId}/items` | Clear all active items from cart | User / Admin |
| **Checkout** | `POST` | `/api/carts/{cartId}/checkout` | Checkout specified cart | User / Admin |
| **Checkout** | `POST` | `/api/carts/me/checkout` | Checkout current user/session cart | User / Guest |
| **Admin** | `GET` | `/api/carts/status/{status}` | Filter carts by status (Paginated) | Admin Only |
| **Admin** | `GET` | `/api/carts` | Get all system carts (Paginated) | Admin Only |
| **Admin** | `GET` | `/api/carts/statistics` | Get analytics and conversion statistics | Admin Only |
| **Actuator** | `GET` | `/actuator/health` | Health check endpoint | Public |
| **Actuator** | `GET` | `/actuator/info` | Service build & application info | Public |
| **Actuator** | `GET` | `/actuator/prometheus` | Prometheus metrics collection | Public |

---

## 🛒 1. Cart Management Endpoints

### 1.1 Create Cart
Creates a new shopping cart session.

- **Method:** `POST`
- **Path:** `/api/carts`
- **Success:** `201 Created`
- **Request Body ([`CartRequest`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartRequest.java)):**
  ```json
  {
    "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "customerName": "John Doe",
    "deliveryType": "DELIVERY",
    "source": "WEB"
  }
  ```
- **Response Body ([`CartResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartResponse.java)):**
  ```json
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "status": "ACTIVE",
    "itemCount": 0
  }
  ```
- **Error Responses:**
  - `400 Bad Request`: Invalid request data (e.g., failed validation).
    ```json
    { "code": "BAD_REQUEST", "message": "...", "timestamp": "...", "path": "..." }
    ```

---

### 1.2 Get Cart by ID
Retrieves details of a specific cart.

- **Method:** `GET`
- **Path:** `/api/carts/{cartId}`
- **Headers:**
  - `X-User-Id` *(UUID, optional)*
  - `X-User-Role` *(String, optional)*
- **Response:** `200 OK` ([`CartResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartResponse.java)) or `403 Forbidden`

  ```json
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "userId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "sessionId": "sess_123456",
    "status": "ACTIVE",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "subtotal": 25.00,
    "taxAmount": 2.00,
    "discountAmount": 2.50,
    "totalAmount": 24.50,
    "itemCount": 2,
    "totalQuantity": 3,
    "currencyCode": "USD",
    "discountCode": "WELCOME10",
    "specialInstructions": "Fragile items",
    "deliveryType": "DELIVERY",
    "deliveryAddress": "123 Main St",
    "items": [],
    "savedItems": [],
    "isEmpty": false,
    "isExpired": false,
    "isGuest": false,
    "hasStockIssues": false,
    "hasPriceChanges": false,
    "createdAt": "2026-08-05T10:00:00",
    "updatedAt": "2026-08-05T10:15:00",
    "expiresAt": "2026-08-06T10:00:00",
    "lastActivityAt": "2026-08-05T10:15:00",
    "abandonedAt": null,
    "convertedAt": null,
    "convertedOrderId": null,
    "source": "WEB",
    "deviceType": "DESKTOP",
    "metadata": {}
  }
  ```

---

### 1.3 Get Current ('Me') Cart
Retrieves or auto-creates the active cart associated with the `X-User-Id` or `X-Session-Id` header.

- **Method:** `GET`
- **Path:** `/api/carts/me`
- **Headers:**
  - `X-User-Id` *(UUID, optional)*
  - `X-Session-Id` *(String, optional)*
- **Response:** `200 OK` ([`CartResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartResponse.java)) or `400 Bad Request`

  ```json
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "userId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "sessionId": "sess_123456",
    "status": "ACTIVE",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "subtotal": 25.00,
    "taxAmount": 2.00,
    "discountAmount": 2.50,
    "totalAmount": 24.50,
    "itemCount": 2,
    "totalQuantity": 3,
    "currencyCode": "USD",
    "discountCode": "WELCOME10",
    "specialInstructions": "Fragile items",
    "deliveryType": "DELIVERY",
    "deliveryAddress": "123 Main St",
    "items": [],
    "savedItems": [],
    "isEmpty": false,
    "isExpired": false,
    "isGuest": false,
    "hasStockIssues": false,
    "hasPriceChanges": false,
    "createdAt": "2026-08-05T10:00:00",
    "updatedAt": "2026-08-05T10:15:00",
    "expiresAt": "2026-08-06T10:00:00",
    "lastActivityAt": "2026-08-05T10:15:00",
    "abandonedAt": null,
    "convertedAt": null,
    "convertedOrderId": null,
    "source": "WEB",
    "deviceType": "DESKTOP",
    "metadata": {}
  }
  ```

---

### 1.4 Get or Create Cart for User
Retrieves or initializes an active cart for a given user UUID.

- **Method:** `GET`
- **Path:** `/api/carts/user/{userId}`
- **Headers:**
  - `X-User-Id` *(UUID, optional)*
  - `X-User-Role` *(String, optional)*
- **Response:** `200 OK` ([`CartResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartResponse.java))

  ```json
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "userId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "sessionId": "sess_123456",
    "status": "ACTIVE",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "subtotal": 25.00,
    "taxAmount": 2.00,
    "discountAmount": 2.50,
    "totalAmount": 24.50,
    "itemCount": 2,
    "totalQuantity": 3,
    "currencyCode": "USD",
    "discountCode": "WELCOME10",
    "specialInstructions": "Fragile items",
    "deliveryType": "DELIVERY",
    "deliveryAddress": "123 Main St",
    "items": [],
    "savedItems": [],
    "isEmpty": false,
    "isExpired": false,
    "isGuest": false,
    "hasStockIssues": false,
    "hasPriceChanges": false,
    "createdAt": "2026-08-05T10:00:00",
    "updatedAt": "2026-08-05T10:15:00",
    "expiresAt": "2026-08-06T10:00:00",
    "lastActivityAt": "2026-08-05T10:15:00",
    "abandonedAt": null,
    "convertedAt": null,
    "convertedOrderId": null,
    "source": "WEB",
    "deviceType": "DESKTOP",
    "metadata": {}
  }
  ```

---

### 1.5 Get or Create Cart for Session
Retrieves or initializes an active cart for a guest session ID.

- **Method:** `GET`
- **Path:** `/api/carts/session/{sessionId}`
- **Headers:**
  - `X-Session-Id` *(String, optional)*
- **Response:** `200 OK` ([`CartResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartResponse.java))

  ```json
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "userId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "sessionId": "sess_123456",
    "status": "ACTIVE",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "subtotal": 25.00,
    "taxAmount": 2.00,
    "discountAmount": 2.50,
    "totalAmount": 24.50,
    "itemCount": 2,
    "totalQuantity": 3,
    "currencyCode": "USD",
    "discountCode": "WELCOME10",
    "specialInstructions": "Fragile items",
    "deliveryType": "DELIVERY",
    "deliveryAddress": "123 Main St",
    "items": [],
    "savedItems": [],
    "isEmpty": false,
    "isExpired": false,
    "isGuest": false,
    "hasStockIssues": false,
    "hasPriceChanges": false,
    "createdAt": "2026-08-05T10:00:00",
    "updatedAt": "2026-08-05T10:15:00",
    "expiresAt": "2026-08-06T10:00:00",
    "lastActivityAt": "2026-08-05T10:15:00",
    "abandonedAt": null,
    "convertedAt": null,
    "convertedOrderId": null,
    "source": "WEB",
    "deviceType": "DESKTOP",
    "metadata": {}
  }
  ```

---

### 1.6 Update Cart Details
Updates top-level cart attributes such as customer details, delivery options, or promo code.

- **Method:** `PATCH`
- **Path:** `/api/carts/{cartId}`
- **Request Body ([`CartUpdateRequest`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartUpdateRequest.java)):**
  ```json
  {
    "customerName": "Jane Doe",
    "customerEmail": "jane.doe@example.com",
    "discountCode": "SUMMER50",
    "specialInstructions": "Ring the bell",
    "deliveryType": "PICKUP",
    "deliveryAddress": "Store Location #1",
    "metadata": {}
  }
  ```
- **Response:** `200 OK` ([`CartResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartResponse.java))

  ```json
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "userId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "sessionId": "sess_123456",
    "status": "ACTIVE",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "subtotal": 25.00,
    "taxAmount": 2.00,
    "discountAmount": 2.50,
    "totalAmount": 24.50,
    "itemCount": 2,
    "totalQuantity": 3,
    "currencyCode": "USD",
    "discountCode": "WELCOME10",
    "specialInstructions": "Fragile items",
    "deliveryType": "DELIVERY",
    "deliveryAddress": "123 Main St",
    "items": [],
    "savedItems": [],
    "isEmpty": false,
    "isExpired": false,
    "isGuest": false,
    "hasStockIssues": false,
    "hasPriceChanges": false,
    "createdAt": "2026-08-05T10:00:00",
    "updatedAt": "2026-08-05T10:15:00",
    "expiresAt": "2026-08-06T10:00:00",
    "lastActivityAt": "2026-08-05T10:15:00",
    "abandonedAt": null,
    "convertedAt": null,
    "convertedOrderId": null,
    "source": "WEB",
    "deviceType": "DESKTOP",
    "metadata": {}
  }
  ```

---

### 1.7 Merge Carts
Merges items from a source cart into a target cart (e.g. merging guest cart into user cart upon login).

- **Method:** `POST`
- **Path:** `/api/carts/merge`
- **Request Body ([`MergeCartsRequest`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/MergeCartsRequest.java)):**
  ```json
  {
    "sourceCartId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "targetCartId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "deleteSourceCart": true,
    "handleDuplicates": true
  }
  ```
- **Response:** `200 OK` ([`CartResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartResponse.java))

  ```json
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "userId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "sessionId": "sess_123456",
    "status": "ACTIVE",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "subtotal": 25.00,
    "taxAmount": 2.00,
    "discountAmount": 2.50,
    "totalAmount": 24.50,
    "itemCount": 2,
    "totalQuantity": 3,
    "currencyCode": "USD",
    "discountCode": "WELCOME10",
    "specialInstructions": "Fragile items",
    "deliveryType": "DELIVERY",
    "deliveryAddress": "123 Main St",
    "items": [],
    "savedItems": [],
    "isEmpty": false,
    "isExpired": false,
    "isGuest": false,
    "hasStockIssues": false,
    "hasPriceChanges": false,
    "createdAt": "2026-08-05T10:00:00",
    "updatedAt": "2026-08-05T10:15:00",
    "expiresAt": "2026-08-06T10:00:00",
    "lastActivityAt": "2026-08-05T10:15:00",
    "abandonedAt": null,
    "convertedAt": null,
    "convertedOrderId": null,
    "source": "WEB",
    "deviceType": "DESKTOP",
    "metadata": {}
  }
  ```

---

### 1.8 Save Cart for Later
Saves all active items in the specified cart for later purchase.

- **Method:** `POST`
- **Path:** `/api/carts/{cartId}/save`
- **Response:** `200 OK` ([`CartResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartResponse.java))

  ```json
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "userId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "sessionId": "sess_123456",
    "status": "ACTIVE",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "subtotal": 25.00,
    "taxAmount": 2.00,
    "discountAmount": 2.50,
    "totalAmount": 24.50,
    "itemCount": 2,
    "totalQuantity": 3,
    "currencyCode": "USD",
    "discountCode": "WELCOME10",
    "specialInstructions": "Fragile items",
    "deliveryType": "DELIVERY",
    "deliveryAddress": "123 Main St",
    "items": [],
    "savedItems": [],
    "isEmpty": false,
    "isExpired": false,
    "isGuest": false,
    "hasStockIssues": false,
    "hasPriceChanges": false,
    "createdAt": "2026-08-05T10:00:00",
    "updatedAt": "2026-08-05T10:15:00",
    "expiresAt": "2026-08-06T10:00:00",
    "lastActivityAt": "2026-08-05T10:15:00",
    "abandonedAt": null,
    "convertedAt": null,
    "convertedOrderId": null,
    "source": "WEB",
    "deviceType": "DESKTOP",
    "metadata": {}
  }
  ```

---

### 1.9 Get User's Cart History
Retrieves all historical and active carts associated with a user.

- **Method:** `GET`
- **Path:** `/api/carts/user/{userId}/all`
- **Query Parameters:**
  - `page` *(int, default: 0)*
  - `size` *(int, default: 20)*
  - `sortBy` *(String, default: "createdAt")*
  - `sortDir` *(String, default: "DESC")*
- **Response:** `200 OK` ([`PagedModel`](https://spring.io/projects/spring-data) [`CartResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartResponse.java))

  ```json
  {
    "content": [
      {
        "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "userId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
        "sessionId": "sess_123456",
        "status": "ACTIVE",
        "customerName": "John Doe",
        "customerEmail": "john@example.com",
        "subtotal": 25.00,
        "taxAmount": 2.00,
        "discountAmount": 2.50,
        "totalAmount": 24.50,
        "itemCount": 2,
        "totalQuantity": 3,
        "currencyCode": "USD",
        "discountCode": "WELCOME10",
        "specialInstructions": "Fragile items",
        "deliveryType": "DELIVERY",
        "deliveryAddress": "123 Main St",
        "items": [],
        "savedItems": [],
        "isEmpty": false,
        "isExpired": false,
        "isGuest": false,
        "hasStockIssues": false,
        "hasPriceChanges": false,
        "createdAt": "2026-08-05T10:00:00",
        "updatedAt": "2026-08-05T10:15:00",
        "expiresAt": "2026-08-06T10:00:00",
        "lastActivityAt": "2026-08-05T10:15:00",
        "abandonedAt": null,
        "convertedAt": null,
        "convertedOrderId": null,
        "source": "WEB",
        "deviceType": "DESKTOP",
        "metadata": {}
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
  ```

---

## 🛑 Common Error Responses

The Bakery Cart Service uses a standardized error payload for all exceptions handled by `GlobalExceptionHandler`.

| Status Code | Error Description |
|---|---|
| `400 Bad Request` | Validation failure, business logic error, empty cart on checkout. |
| `404 Not Found` | Cart or Item not found. |
| `503 Service Unavailable` | Downstream service (e.g., Order/Product Service) is unavailable. |

**Standard Error Payload:**
```json
{
  "code": "BAD_REQUEST",
  "message": "Error details here",
  "timestamp": "2026-08-05T20:15:00",
  "path": "/api/..."
}
```

---

## 📦 2. Cart Item Endpoints

### 2.1 Get Cart Item by ID
- **Method:** `GET`
- **Path:** `/api/cart-items/{itemId}`
- **Response:** `200 OK` ([`CartItemResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cartitem/CartItemResponse.java))

  ```json
  {
    "id": "4a71bc18-294b-4b13-a442-520e11894d31",
    "productId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
    "productSku": "BAK-CK-001",
    "productName": "Chocolate Mousse Cake",
    "productCategory": "Cakes",
    "quantity": 2,
    "unitPrice": 12.50,
    "totalPrice": 25.00,
    "originalUnitPrice": 12.50,
    "taxClass": "STANDARD",
    "taxRate": 0.08,
    "taxAmount": 2.00,
    "status": "ACTIVE",
    "specialInstructions": "Extra candles",
    "productDescription": "Rich chocolate mousse layer cake",
    "productImageUrl": "https://cdn.example.com/cakes/choc-mousse.jpg",
    "preparationTimeMinutes": 30,
    "currencyCode": "USD",
    "isAvailable": true,
    "stockQuantity": 15,
    "availabilityMessage": "In Stock",
    "priceChanged": false,
    "priceChangeAmount": 0.00,
    "hasStockIssue": false,
    "addedAt": "2026-08-05T10:00:00",
    "updatedAt": "2026-08-05T10:00:00",
    "lastValidatedAt": "2026-08-05T10:00:00",
    "savedForLaterAt": null,
    "removedAt": null,
    "addedFrom": "PRODUCT_PAGE",
    "metadata": {}
  }
  ```

---

### 2.2 List Cart Items (Paginated)
- **Method:** `GET`
- **Path:** `/api/cart-items/cart/{cartId}`
- **Query Parameters:** `page`, `size`, `sortBy`, `sortDir`
- **Response:** `200 OK` ([`PagedModel`](https://spring.io/projects/spring-data) [`CartItemResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cartitem/CartItemResponse.java))

  ```json
  {
    "content": [
      {
        "id": "4a71bc18-294b-4b13-a442-520e11894d31",
        "productId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
        "productSku": "BAK-CK-001",
        "productName": "Chocolate Mousse Cake",
        "productCategory": "Cakes",
        "quantity": 2,
        "unitPrice": 12.50,
        "totalPrice": 25.00,
        "originalUnitPrice": 12.50,
        "taxClass": "STANDARD",
        "taxRate": 0.08,
        "taxAmount": 2.00,
        "status": "ACTIVE",
        "specialInstructions": "Extra candles",
        "productDescription": "Rich chocolate mousse layer cake",
        "productImageUrl": "https://cdn.example.com/cakes/choc-mousse.jpg",
        "preparationTimeMinutes": 30,
        "currencyCode": "USD",
        "isAvailable": true,
        "stockQuantity": 15,
        "availabilityMessage": "In Stock",
        "priceChanged": false,
        "priceChangeAmount": 0.00,
        "hasStockIssue": false,
        "addedAt": "2026-08-05T10:00:00",
        "updatedAt": "2026-08-05T10:00:00",
        "lastValidatedAt": "2026-08-05T10:00:00",
        "savedForLaterAt": null,
        "removedAt": null,
        "addedFrom": "PRODUCT_PAGE",
        "metadata": {}
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
  ```

---

### 2.3 List Saved-For-Later Items (Paginated)
- **Method:** `GET`
- **Path:** `/api/cart-items/cart/{cartId}/saved`
- **Query Parameters:** `page`, `size`, `sortBy`, `sortDir`
- **Response:** `200 OK` ([`PagedModel`](https://spring.io/projects/spring-data) [`CartItemResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cartitem/CartItemResponse.java))

  ```json
  {
    "content": [
      {
        "id": "4a71bc18-294b-4b13-a442-520e11894d31",
        "productId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
        "productSku": "BAK-CK-001",
        "productName": "Chocolate Mousse Cake",
        "productCategory": "Cakes",
        "quantity": 2,
        "unitPrice": 12.50,
        "totalPrice": 25.00,
        "originalUnitPrice": 12.50,
        "taxClass": "STANDARD",
        "taxRate": 0.08,
        "taxAmount": 2.00,
        "status": "ACTIVE",
        "specialInstructions": "Extra candles",
        "productDescription": "Rich chocolate mousse layer cake",
        "productImageUrl": "https://cdn.example.com/cakes/choc-mousse.jpg",
        "preparationTimeMinutes": 30,
        "currencyCode": "USD",
        "isAvailable": true,
        "stockQuantity": 15,
        "availabilityMessage": "In Stock",
        "priceChanged": false,
        "priceChangeAmount": 0.00,
        "hasStockIssue": false,
        "addedAt": "2026-08-05T10:00:00",
        "updatedAt": "2026-08-05T10:00:00",
        "lastValidatedAt": "2026-08-05T10:00:00",
        "savedForLaterAt": null,
        "removedAt": null,
        "addedFrom": "PRODUCT_PAGE",
        "metadata": {}
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
  ```

---

### 2.4 Save Item for Later
- **Method:** `POST`
- **Path:** `/api/cart-items/{itemId}/save-for-later`
- **Response:** `200 OK` ([`CartItemResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cartitem/CartItemResponse.java))

  ```json
  {
    "id": "4a71bc18-294b-4b13-a442-520e11894d31",
    "productId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
    "productSku": "BAK-CK-001",
    "productName": "Chocolate Mousse Cake",
    "productCategory": "Cakes",
    "quantity": 2,
    "unitPrice": 12.50,
    "totalPrice": 25.00,
    "originalUnitPrice": 12.50,
    "taxClass": "STANDARD",
    "taxRate": 0.08,
    "taxAmount": 2.00,
    "status": "ACTIVE",
    "specialInstructions": "Extra candles",
    "productDescription": "Rich chocolate mousse layer cake",
    "productImageUrl": "https://cdn.example.com/cakes/choc-mousse.jpg",
    "preparationTimeMinutes": 30,
    "currencyCode": "USD",
    "isAvailable": true,
    "stockQuantity": 15,
    "availabilityMessage": "In Stock",
    "priceChanged": false,
    "priceChangeAmount": 0.00,
    "hasStockIssue": false,
    "addedAt": "2026-08-05T10:00:00",
    "updatedAt": "2026-08-05T10:00:00",
    "lastValidatedAt": "2026-08-05T10:00:00",
    "savedForLaterAt": null,
    "removedAt": null,
    "addedFrom": "PRODUCT_PAGE",
    "metadata": {}
  }
  ```

---

### 2.5 Move Item Back to Active Cart
- **Method:** `POST`
- **Path:** `/api/cart-items/{itemId}/move-to-cart`
- **Response:** `200 OK` ([`CartItemResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cartitem/CartItemResponse.java))

  ```json
  {
    "id": "4a71bc18-294b-4b13-a442-520e11894d31",
    "productId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
    "productSku": "BAK-CK-001",
    "productName": "Chocolate Mousse Cake",
    "productCategory": "Cakes",
    "quantity": 2,
    "unitPrice": 12.50,
    "totalPrice": 25.00,
    "originalUnitPrice": 12.50,
    "taxClass": "STANDARD",
    "taxRate": 0.08,
    "taxAmount": 2.00,
    "status": "ACTIVE",
    "specialInstructions": "Extra candles",
    "productDescription": "Rich chocolate mousse layer cake",
    "productImageUrl": "https://cdn.example.com/cakes/choc-mousse.jpg",
    "preparationTimeMinutes": 30,
    "currencyCode": "USD",
    "isAvailable": true,
    "stockQuantity": 15,
    "availabilityMessage": "In Stock",
    "priceChanged": false,
    "priceChangeAmount": 0.00,
    "hasStockIssue": false,
    "addedAt": "2026-08-05T10:00:00",
    "updatedAt": "2026-08-05T10:00:00",
    "lastValidatedAt": "2026-08-05T10:00:00",
    "savedForLaterAt": null,
    "removedAt": null,
    "addedFrom": "PRODUCT_PAGE",
    "metadata": {}
  }
  ```

---

### 2.6 Add Item to Cart
- **Method:** `POST`
- **Path:** `/api/carts/{cartId}/items`
- **Request Body ([`AddItemRequest`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cartitem/AddItemRequest.java)):**
  ```json
  {
    "productId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
    "quantity": 2,
    "unitPriceOverride": 12.50,
    "specialInstructions": "Extra chocolate chip sprinkles",
    "addedFrom": "PRODUCT_PAGE",
    "metadata": {}
  }
  ```
- **Response:** `200 OK` ([`CartResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartResponse.java))

  ```json
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "userId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "sessionId": "sess_123456",
    "status": "ACTIVE",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "subtotal": 25.00,
    "taxAmount": 2.00,
    "discountAmount": 2.50,
    "totalAmount": 24.50,
    "itemCount": 2,
    "totalQuantity": 3,
    "currencyCode": "USD",
    "discountCode": "WELCOME10",
    "specialInstructions": "Fragile items",
    "deliveryType": "DELIVERY",
    "deliveryAddress": "123 Main St",
    "items": [],
    "savedItems": [],
    "isEmpty": false,
    "isExpired": false,
    "isGuest": false,
    "hasStockIssues": false,
    "hasPriceChanges": false,
    "createdAt": "2026-08-05T10:00:00",
    "updatedAt": "2026-08-05T10:15:00",
    "expiresAt": "2026-08-06T10:00:00",
    "lastActivityAt": "2026-08-05T10:15:00",
    "abandonedAt": null,
    "convertedAt": null,
    "convertedOrderId": null,
    "source": "WEB",
    "deviceType": "DESKTOP",
    "metadata": {}
  }
  ```

---

### 2.7 Add Item to Current ('Me') Cart
- **Method:** `POST`
- **Path:** `/api/carts/me/items`
- **Headers:** `X-User-Id` or `X-Session-Id`
- **Request Body ([`AddItemRequest`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cartitem/AddItemRequest.java)):**

  ```json
  {
    "productId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
    "quantity": 2,
    "unitPriceOverride": 12.50,
    "specialInstructions": "Extra chocolate chip sprinkles",
    "addedFrom": "PRODUCT_PAGE",
    "metadata": {}
  }
  ```
- **Response:** `200 OK` ([`CartResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartResponse.java))

  ```json
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "userId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "sessionId": "sess_123456",
    "status": "ACTIVE",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "subtotal": 25.00,
    "taxAmount": 2.00,
    "discountAmount": 2.50,
    "totalAmount": 24.50,
    "itemCount": 2,
    "totalQuantity": 3,
    "currencyCode": "USD",
    "discountCode": "WELCOME10",
    "specialInstructions": "Fragile items",
    "deliveryType": "DELIVERY",
    "deliveryAddress": "123 Main St",
    "items": [],
    "savedItems": [],
    "isEmpty": false,
    "isExpired": false,
    "isGuest": false,
    "hasStockIssues": false,
    "hasPriceChanges": false,
    "createdAt": "2026-08-05T10:00:00",
    "updatedAt": "2026-08-05T10:15:00",
    "expiresAt": "2026-08-06T10:00:00",
    "lastActivityAt": "2026-08-05T10:15:00",
    "abandonedAt": null,
    "convertedAt": null,
    "convertedOrderId": null,
    "source": "WEB",
    "deviceType": "DESKTOP",
    "metadata": {}
  }
  ```

---

### 2.8 Update Cart Item
- **Method:** `PUT`
- **Path:** `/api/carts/{cartId}/items/{itemId}`
- **Request Body ([`UpdateItemRequest`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cartitem/UpdateItemRequest.java)):**
  ```json
  {
    "quantity": 5,
    "specialInstructions": "Updated notes",
    "metadata": {}
  }
  ```
- **Response:** `200 OK` ([`CartResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartResponse.java))

  ```json
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "userId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "sessionId": "sess_123456",
    "status": "ACTIVE",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "subtotal": 25.00,
    "taxAmount": 2.00,
    "discountAmount": 2.50,
    "totalAmount": 24.50,
    "itemCount": 2,
    "totalQuantity": 3,
    "currencyCode": "USD",
    "discountCode": "WELCOME10",
    "specialInstructions": "Fragile items",
    "deliveryType": "DELIVERY",
    "deliveryAddress": "123 Main St",
    "items": [],
    "savedItems": [],
    "isEmpty": false,
    "isExpired": false,
    "isGuest": false,
    "hasStockIssues": false,
    "hasPriceChanges": false,
    "createdAt": "2026-08-05T10:00:00",
    "updatedAt": "2026-08-05T10:15:00",
    "expiresAt": "2026-08-06T10:00:00",
    "lastActivityAt": "2026-08-05T10:15:00",
    "abandonedAt": null,
    "convertedAt": null,
    "convertedOrderId": null,
    "source": "WEB",
    "deviceType": "DESKTOP",
    "metadata": {}
  }
  ```

---

### 2.9 Remove Item from Cart
- **Method:** `DELETE`
- **Path:** `/api/carts/{cartId}/items/{itemId}`
- **Response:** `200 OK` ([`CartResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartResponse.java))

  ```json
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "userId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "sessionId": "sess_123456",
    "status": "ACTIVE",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "subtotal": 25.00,
    "taxAmount": 2.00,
    "discountAmount": 2.50,
    "totalAmount": 24.50,
    "itemCount": 2,
    "totalQuantity": 3,
    "currencyCode": "USD",
    "discountCode": "WELCOME10",
    "specialInstructions": "Fragile items",
    "deliveryType": "DELIVERY",
    "deliveryAddress": "123 Main St",
    "items": [],
    "savedItems": [],
    "isEmpty": false,
    "isExpired": false,
    "isGuest": false,
    "hasStockIssues": false,
    "hasPriceChanges": false,
    "createdAt": "2026-08-05T10:00:00",
    "updatedAt": "2026-08-05T10:15:00",
    "expiresAt": "2026-08-06T10:00:00",
    "lastActivityAt": "2026-08-05T10:15:00",
    "abandonedAt": null,
    "convertedAt": null,
    "convertedOrderId": null,
    "source": "WEB",
    "deviceType": "DESKTOP",
    "metadata": {}
  }
  ```

---

### 2.10 Clear Cart
- **Method:** `DELETE`
- **Path:** `/api/carts/{cartId}/items`
- **Response:** `200 OK` ([`CartResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartResponse.java))

  ```json
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "userId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "sessionId": "sess_123456",
    "status": "ACTIVE",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "subtotal": 25.00,
    "taxAmount": 2.00,
    "discountAmount": 2.50,
    "totalAmount": 24.50,
    "itemCount": 2,
    "totalQuantity": 3,
    "currencyCode": "USD",
    "discountCode": "WELCOME10",
    "specialInstructions": "Fragile items",
    "deliveryType": "DELIVERY",
    "deliveryAddress": "123 Main St",
    "items": [],
    "savedItems": [],
    "isEmpty": false,
    "isExpired": false,
    "isGuest": false,
    "hasStockIssues": false,
    "hasPriceChanges": false,
    "createdAt": "2026-08-05T10:00:00",
    "updatedAt": "2026-08-05T10:15:00",
    "expiresAt": "2026-08-06T10:00:00",
    "lastActivityAt": "2026-08-05T10:15:00",
    "abandonedAt": null,
    "convertedAt": null,
    "convertedOrderId": null,
    "source": "WEB",
    "deviceType": "DESKTOP",
    "metadata": {}
  }
  ```

---

## 💳 3. Checkout Endpoints

### 3.1 Checkout Cart by ID
Validates cart items against Product inventory, locks prices, and creates an order via Order Service Feign client.

- **Method:** `POST`
- **Path:** `/api/carts/{cartId}/checkout`
- **Request Body ([`CheckoutRequest`](./src/main/java/com/blubugtech/bakery_cart_service/dto/checkout/CheckoutRequest.java)):**
  ```json
  {
    "customerName": "Alice Smith",
    "customerEmail": "alice.smith@example.com",
    "customerPhone": "+15551234567",
    "deliveryType": "DELIVERY",
    "deliveryAddress": "456 Elm St, Suite 2B",
    "deliveryDate": "2026-08-06T14:00:00",
    "specialInstructions": "Leave at front door",
    "discountCode": "SUMMER10",
    "paymentMethod": "CARD",
    "cardLastFour": "4242",
    "cardBrand": "VISA",
    "cardType": "CREDIT",
    "digitalWalletProvider": null,
    "bankName": null,
    "paymentNotes": "Paid via Stripe online",
    "metadata": {}
  }
  ```
- **Response Body ([`CheckoutResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/checkout/CheckoutResponse.java)):** `200 OK`
  ```json
  {
    "cart": { /* [`CartResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartResponse.java) */ },
    "order": {
      "id": "e4a7a810-7212-421f-829b-02b489bc1012",
      "orderNumber": "ORD-2026-0089",
      "status": "CREATED",
      "totalAmount": 25.00
    }
  }
  ```

---

### 3.2 Checkout 'Me' Cart
- **Method:** `POST`
- **Path:** `/api/carts/me/checkout`
- **Headers:** `X-User-Id` or `X-Session-Id`
- **Request Body:** [`CheckoutRequest`](./src/main/java/com/blubugtech/bakery_cart_service/dto/checkout/CheckoutRequest.java)

  ```json
  {
    "customerName": "Alice Smith",
    "customerEmail": "alice.smith@example.com",
    "customerPhone": "+15551234567",
    "deliveryType": "DELIVERY",
    "deliveryAddress": "456 Elm St, Suite 2B",
    "deliveryDate": "2026-08-06T14:00:00",
    "specialInstructions": "Leave at front door",
    "discountCode": "SUMMER10",
    "paymentMethod": "CARD",
    "cardLastFour": "4242",
    "cardBrand": "VISA",
    "cardType": "CREDIT",
    "digitalWalletProvider": null,
    "bankName": null,
    "paymentNotes": "Paid via Stripe online",
    "metadata": {}
  }
  ```
- **Response:** `200 OK` ([`CheckoutResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/checkout/CheckoutResponse.java))

  ```json
  {
    "cart": {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "status": "CHECKED_OUT"
    },
    "order": {
      "id": "e4a7a810-7212-421f-829b-02b489bc1012",
      "orderNumber": "ORD-2026-0089",
      "status": "CREATED",
      "totalAmount": 25.00
    }
  }
  ```

---

## 📊 4. Admin Operations Endpoints

> [!IMPORTANT]
> All Admin endpoints require administrative credentials (`X-User-Role: ADMIN` or Spring Security `@PreAuthorize("hasRole('ADMIN')")`).

### 4.1 Filter Carts by Status
- **Method:** `GET`
- **Path:** `/api/carts/status/{status}`
- **Path Parameters:**
  - `status` *(CartStatus enum: `ACTIVE`, `SAVED_FOR_LATER`, `MERGED`, `CHECKED_OUT`, `EXPIRED`, `ABANDONED`)*
- **Query Parameters:** `page`, `size`, `sortBy`, `sortDir`
- **Response:** `200 OK` (`PagedModel<[`CartResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartResponse.java)>`)

  ```json
  {
    "content": [
      {
        "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "userId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
        "sessionId": "sess_123456",
        "status": "ACTIVE",
        "customerName": "John Doe",
        "customerEmail": "john@example.com",
        "subtotal": 25.00,
        "taxAmount": 2.00,
        "discountAmount": 2.50,
        "totalAmount": 24.50,
        "itemCount": 2,
        "totalQuantity": 3,
        "currencyCode": "USD",
        "discountCode": "WELCOME10",
        "specialInstructions": "Fragile items",
        "deliveryType": "DELIVERY",
        "deliveryAddress": "123 Main St",
        "items": [],
        "savedItems": [],
        "isEmpty": false,
        "isExpired": false,
        "isGuest": false,
        "hasStockIssues": false,
        "hasPriceChanges": false,
        "createdAt": "2026-08-05T10:00:00",
        "updatedAt": "2026-08-05T10:15:00",
        "expiresAt": "2026-08-06T10:00:00",
        "lastActivityAt": "2026-08-05T10:15:00",
        "abandonedAt": null,
        "convertedAt": null,
        "convertedOrderId": null,
        "source": "WEB",
        "deviceType": "DESKTOP",
        "metadata": {}
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
  ```

---

### 4.2 Get All System Carts
- **Method:** `GET`
- **Path:** `/api/carts`
- **Query Parameters:** `page`, `size`, `sortBy`, `sortDir`
- **Response:** `200 OK` (`PagedModel<[`CartResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartResponse.java)>`)

  ```json
  {
    "content": [
      {
        "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "userId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
        "sessionId": "sess_123456",
        "status": "ACTIVE",
        "customerName": "John Doe",
        "customerEmail": "john@example.com",
        "subtotal": 25.00,
        "taxAmount": 2.00,
        "discountAmount": 2.50,
        "totalAmount": 24.50,
        "itemCount": 2,
        "totalQuantity": 3,
        "currencyCode": "USD",
        "discountCode": "WELCOME10",
        "specialInstructions": "Fragile items",
        "deliveryType": "DELIVERY",
        "deliveryAddress": "123 Main St",
        "items": [],
        "savedItems": [],
        "isEmpty": false,
        "isExpired": false,
        "isGuest": false,
        "hasStockIssues": false,
        "hasPriceChanges": false,
        "createdAt": "2026-08-05T10:00:00",
        "updatedAt": "2026-08-05T10:15:00",
        "expiresAt": "2026-08-06T10:00:00",
        "lastActivityAt": "2026-08-05T10:15:00",
        "abandonedAt": null,
        "convertedAt": null,
        "convertedOrderId": null,
        "source": "WEB",
        "deviceType": "DESKTOP",
        "metadata": {}
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
  ```

---

### 4.3 Get Cart Statistics & Analytics
- **Method:** `GET`
- **Path:** `/api/carts/statistics`
- **Query Parameters:**
  - `startDate` *(ISO LocalDateTime, optional)*
  - `endDate` *(ISO LocalDateTime, optional)*
- **Response Body ([`CartStatisticsResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/CartStatisticsResponse.java)):** `200 OK`
  ```json
  {
    "totalCarts": 1500,
    "activeCarts": 320,
    "abandonedCarts": 180,
    "convertedCarts": 1000,
    "averageCartValue": 45.50,
    "averageItemCount": 3.4,
    "conversionRate": 66.67,
    "dailyStats": [],
    "sourceStats": [],
    "dateRange": {
      "startDate": "2026-07-06T00:00:00",
      "endDate": "2026-08-05T20:00:00"
    }
  }
  ```

---

## 📈 5. Monitoring & Actuator Endpoints

- **Health Check:** `GET /actuator/health` -> Returns `{"status": "UP"}`
- **App Info:** `GET /actuator/info` -> Returns build metadata
- **Metrics:** `GET /actuator/prometheus` -> Prometheus formatted metrics

---

## 🧱 Key Response Models (DTO Schemas)

### [`CartResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cart/CartResponse.java)
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "userId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "sessionId": "sess_123456",
  "status": "ACTIVE",
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "subtotal": 25.00,
  "taxAmount": 2.00,
  "discountAmount": 2.50,
  "totalAmount": 24.50,
  "itemCount": 2,
  "totalQuantity": 3,
  "currencyCode": "USD",
  "discountCode": "WELCOME10",
  "specialInstructions": "Fragile items",
  "deliveryType": "DELIVERY",
  "deliveryAddress": "123 Main St",
  "items": [ /* [`CartItemResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cartitem/CartItemResponse.java) */ ],
  "savedItems": [ /* [`CartItemResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cartitem/CartItemResponse.java) */ ],
  "isEmpty": false,
  "isExpired": false,
  "isGuest": false,
  "hasStockIssues": false,
  "hasPriceChanges": false,
  "createdAt": "2026-08-05T10:00:00",
  "updatedAt": "2026-08-05T10:15:00",
  "expiresAt": "2026-08-06T10:00:00",
  "lastActivityAt": "2026-08-05T10:15:00",
  "abandonedAt": null,
  "convertedAt": null,
  "convertedOrderId": null,
  "source": "WEB",
  "deviceType": "DESKTOP",
  "metadata": {}
}
```

### [`CartItemResponse`](./src/main/java/com/blubugtech/bakery_cart_service/dto/cartitem/CartItemResponse.java)
```json
{
  "id": "4a71bc18-294b-4b13-a442-520e11894d31",
  "productId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
  "productSku": "BAK-CK-001",
  "productName": "Chocolate Mousse Cake",
  "productCategory": "Cakes",
  "quantity": 2,
  "unitPrice": 12.50,
  "totalPrice": 25.00,
  "originalUnitPrice": 12.50,
  "taxClass": "STANDARD",
  "taxRate": 0.08,
  "taxAmount": 2.00,
  "status": "ACTIVE",
  "specialInstructions": "Extra candles",
  "productDescription": "Rich chocolate mousse layer cake",
  "productImageUrl": "https://cdn.example.com/cakes/choc-mousse.jpg",
  "preparationTimeMinutes": 30,
  "currencyCode": "USD",
  "isAvailable": true,
  "stockQuantity": 15,
  "availabilityMessage": "In Stock",
  "priceChanged": false,
  "priceChangeAmount": 0.00,
  "hasStockIssue": false,
  "addedAt": "2026-08-05T10:00:00",
  "updatedAt": "2026-08-05T10:00:00",
  "lastValidatedAt": "2026-08-05T10:00:00",
  "savedForLaterAt": null,
  "removedAt": null,
  "addedFrom": "PRODUCT_PAGE",
  "metadata": {}
}
```
