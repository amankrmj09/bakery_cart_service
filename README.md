# 🧁 Cart Service

![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)

Welcome to the **Cart Service**, a core component of the Shah's Bakery Microservice Platform.

## 📑 Table of Contents
- [Features](#-features)
- [Folder Structure](#-folder-structure)
- [Dependencies](#-dependencies)
- [Endpoints](#-endpoints)
- [How to Run](#-how-to-run)
- [Related Links](#-related-links)

## ✨ Features
- Session-based user shopping carts.
- Add, update, or remove items from the cart.
- Automatic price and total calculations.
- Seamless conversion from cart to order.
- Automated Cart Maintenance & stale session clearing.

## 📁 Folder Structure
The main `src/main/java` directory is organized as follows:
```text
src/
└── main/
    └── java/.../bakery_cart_service/
        ├── client/     # Feign clients communicating with Order and Product services.
        ├── config/     # Configurations for Redis caching and Repositories.
        ├── controller/ # REST endpoints for managing shopping carts and items.
        ├── dto/        # Data Transfer Objects for cart operations.
        ├── entity/     # Redis entities representing Cart and CartItem sessions.
        ├── exception/  # Custom exceptions and error handlers.
        ├── repository/ # Spring Data Redis interfaces.
        └── service/    # Business logic including automated Cart Maintenance for stale sessions.
```

## 🛠️ Dependencies
- **Framework:** Spring Boot
- **Database:** Redis (for high-speed session storage)
- **Key Modules:** Eureka Client, Spring Data Redis, Spring Web

## 🌐 Endpoints
> [!NOTE]
> For complete and detailed API definitions, please refer to the OpenAPI Reference available via the API Gateway's Swagger UI.

- `POST /api/carts` - Creates a new shopping cart.
- `GET /api/carts/{cartId}` - Retrieves the current state of a cart.
- `POST /api/carts/{cartId}/items` - Adds a new item to the cart.
- `DELETE /api/carts/{cartId}/items/{itemId}` - Removes a specific item from the cart.

## 🚀 How to Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/amankrmj01/bakery_cart_service.git
   cd bakery_cart_service
   ```

2. **Configure Environment:**
   Ensure your Redis instance is running and configured in your properties.

3. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```

## 🔗 Related Links
- [Main Platform README](../README.md)
