# 🚀 Cart Service

![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)
![Database](https://img.shields.io/badge/Database-Redis%2BPostgreSQL-blue.svg)

Welcome to the **Cart Service**, a core component of the Shah's Bakery Microservice Platform. It manages session-based user shopping carts, calculates totals, and prepares carts for order conversion.

## 📑 Table of Contents
- [Architecture & Design](#-architecture--design)
- [Features](#-features)
- [Folder Structure](#-folder-structure)
- [API Reference](#-api-reference)
- [Configuration](#-configuration)
- [How to Run Locally](#-how-to-run-locally)
- [Testing](#-testing)
- [Dependencies](#-dependencies)
- [Related Links](#-related-links)

## 🏗️ Architecture & Design
Provide a brief overview of the architecture of this service.
- **Data Storage**: PostgreSQL for relational data and Redis for high-speed session storage.
- **Communication**: REST API for client communication, Eureka Client for discovery, and Feign clients for inter-service communication (with Order and Product services). RabbitMQ (Messaging) for async events.
- **Key Design Patterns**: MVC, Repository Pattern, DTO pattern with MapStruct.

## ✨ Features
List the core capabilities and features of this service.
- Session-based user shopping carts with Redis.
- Add, update, or remove items from the cart.
- Automatic price and total calculations.
- Seamless conversion from cart to order.
- Automated Cart Maintenance & stale session clearing.
- Configurable limits for cart items and values.

## 📁 Folder Structure
The project structure for `bakery_cart_service` is organized as follows:
```text
bakery_cart_service/
├── .env
├── .env.example
├── .gitattributes
├── .gitignore
├── Dockerfile
├── README.md
├── API_REFERENCE.md
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── src/
    ├── main/
    │   ├── java/com/blubugtech/bakery_cart_service/
    │   │   ├── BakeryCartServiceApplication.java
    │   │   ├── client/                  # Feign Clients & Fallbacks (Order, Product)
    │   │   │   ├── order/               # Order Service Feign Client
    │   │   │   └── product/             # Product Service Feign Client
    │   │   ├── config/                  # App configurations (Cache, OpenAPI, Repositories)
    │   │   ├── constants/               # System & Cart application constants
    │   │   ├── controller/              # REST Endpoints (Cart, Admin, CartItem, Checkout)
    │   │   │   ├── CartAdminController.java
    │   │   │   ├── CartController.java
    │   │   │   ├── CartItemController.java
    │   │   │   └── CheckoutController.java
    │   │   ├── dto/                     # Data Transfer Objects
    │   │   │   ├── cart/                # Cart DTOs (CartRequest, CartResponse, etc.)
    │   │   │   ├── cartitem/            # Cart item DTOs (AddItemRequest, etc.)
    │   │   │   ├── checkout/            # Checkout DTOs (CheckoutRequest, CheckoutResponse)
    │   │   │   └── order/               # Order bridge DTOs
    │   │   ├── entity/                  # JPA entities (Cart, CartItem)
    │   │   ├── enums/                   # Enums (CartStatus, CartType)
    │   │   ├── exception/               # Custom domain exceptions & GlobalExceptionHandler
    │   │   ├── gateway/                 # External service gateways (Order, Product)
    │   │   ├── mapper/                  # MapStruct & JSON mappers
    │   │   ├── repository/              # Spring Data JPA Repositories
    │   │   ├── service/                 # Business logic & background maintenance services
    │   │   └── strategy/                # Strategy pattern implementations (Discount, Shipping, etc.)
    │   └── resources/
    │       ├── application.yml          # Core YAML configuration
    │       ├── application-dev.yml      # Development profile configuration
    │       ├── application-docker.yml   # Docker deployment profile
    │       ├── application-prod.yml     # Production profile configuration
    │       ├── logback-spring.xml       # Spring logging configuration
    │       └── db/migration/            # Flyway SQL migrations (V1, V2, V3)
    └── test/
        ├── java/com/blubugtech/bakery_cart_service/
        └── resources/
            └── application-test.yml
```

## 🌐 API Reference
For complete endpoint specifications and request/response payload schemas, refer to [`API_REFERENCE.md`](./API_REFERENCE.md).

**Key Controller Groups:**
- **Cart Management (`/api/carts`)**: Create, retrieve, update, save for later, and merge shopping carts.
- **Cart Item Management (`/api/cart-items`, `/api/carts/{cartId}/items`)**: Add, update, remove items, move to/from save-for-later.
- **Checkout (`/api/carts/{cartId}/checkout`, `/api/carts/me/checkout`)**: Validate cart and create orders.
- **Admin Operations (`/api/carts/status/{status}`, `/api/carts/statistics`)**: Filter carts by status, view paginated carts, and generate analytics reports.

## ⚙️ Configuration
List required environment variables and configurations.
You can copy `.env.example` to `.env` and fill in the values.

| Variable | Description | Default / Example |
|----------|-------------|-------------------|
| `ACTIVE_PROFILE` | Spring active profile | `dev` |
| `SERVER_PORT` | Port for the service | `8080` |
| `CART_DB_URL` | PostgreSQL Database connection URL | `jdbc:postgresql://localhost:5432/cart_db` |
| `CART_DB_USER` | Database user | `postgres` |
| `CART_DB_PASSWORD` | Database password | `password` |
| `CONFIG_SERVER_URL` | Spring Cloud Config Server URL | `http://localhost:8888` |
| `EUREKA_URL`| Eureka server URL | `http://localhost:8761/eureka/` |
| `REDIS_HOST` | Redis Server Host | `localhost` |
| `REDIS_PORT_CART` | Redis Port | `6379` |
| `CART_CHECK_STOCK` | Enable stock checking on adding to cart | `true` |
| `CART_CHECK_PRICE` | Enable price checking | `true` |
| `CART_MAX_QTY` | Max quantity for a single item | `10` |
| `CART_MAX_ITEMS` | Max items in a cart | `20` |
| `CART_MAX_VALUE` | Max total value of a cart | `1000.00` |
| `CART_CLEANUP_HOURS` | Time after which idle carts are cleared | `24` |

## 🚀 How to Run Locally

### Prerequisites
- JDK 21+
- Gradle
- PostgreSQL
- Redis

### Steps
1. **Clone the repository:**
   ```bash
   git clone https://github.com/amankrmj01/bakery_cart_service.git
   cd bakery_cart_service
   ```

2. **Configure Environment:**
   Set up your `.env` file based on `.env.example`. Make sure backing services (like PostgreSQL and Redis) are running.
   You can use the provided Docker Compose file or run them locally:
   ```bash
   docker-compose up -d
   ```

3. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```

## 🧪 Testing
To run the test suite:
```bash
./gradlew test
```

## 🛠️ Dependencies
- **Framework:** Spring Boot 3.5.15
- **Database:** PostgreSQL (Relational Data), Redis (Session Storage)
- **Key Modules:** Spring Web, Spring Data JPA, Spring Data Redis, Eureka Client, OpenFeign, Spring Security, Spring Cloud Config, Flyway, MapStruct, Lombok

## 🔗 Related Links
- [Main Platform README](../README.md)
- [Parent Repository](https://github.com/amankrmj09/Blu_s_Bakery)
- [API Reference](./API_REFERENCE.md)
