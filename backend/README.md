# CyberOpus Backend

**IBM AI Specialist Cloud FullStack Capstone Project**

CyberOpus is a fully-featured e-commerce e-bookstore backend built with Spring Boot, providing a complete REST API for browsing, purchasing, and managing books.

---

## Architecture

```
HTTP Client
    │
    ▼
Controller Layer  (REST endpoints, @Valid, ResponseEntity)
    │
    ▼
Service Layer     (Business logic, @Transactional)
    │
    ▼
Repository Layer  (Spring Data JPA)
    │
    ▼
PostgreSQL Database
```

---

## Technology Stack

| Component | Version |
|---|---|
| Java | 21 (compiled with JDK 26) |
| Spring Boot | 3.4.5 |
| Maven | 3.9.x |
| Spring Data JPA | Included in Boot |
| PostgreSQL | 14+ |
| Hibernate | Included in Boot |
| Flyway | Included in Boot |
| Spring Security | Included in Boot |
| JWT (JJWT) | 0.12.6 |
| Lombok | Latest via Boot BOM |
| SpringDoc OpenAPI | 2.8.6 |
| JUnit 5 + Mockito | Included in Boot Test |

> **Version Note:** The instructor specification requested Java 25 / Spring Boot 4.1.0. Neither exists as a released version. This implementation uses the latest available stable equivalents: Java 21 LTS + Spring Boot 3.4.5.

---

## Prerequisites

- **Java JDK 21+** (JDK 26 also works)
- **Maven 3.9+**
- **PostgreSQL 14+**

---

## PostgreSQL Setup

```sql
-- Connect to PostgreSQL as superuser
CREATE USER cyberopus_user WITH PASSWORD 'your_secure_password';
CREATE DATABASE cyberopus OWNER cyberopus_user;
GRANT ALL PRIVILEGES ON DATABASE cyberopus TO cyberopus_user;
```

---

## Environment Variables

Copy `.env.example` to `.env` and fill in your values:

```bash
cp .env.example .env
```

| Variable | Description | Required |
|---|---|---|
| `DB_URL` | JDBC URL e.g. `jdbc:postgresql://localhost:5432/cyberopus` | Yes |
| `DB_USERNAME` | Database username | Yes |
| `DB_PASSWORD` | Database password | Yes |
| `JWT_SECRET` | HS256 signing secret (min 32 chars) | Yes |
| `JWT_EXPIRATION_MS` | Token TTL in milliseconds (default 86400000 = 24h) | No |
| `SERVER_PORT` | HTTP port (default 8080) | No |

> **Security:** Never commit `.env` to version control. The application will fail to start if `DB_PASSWORD` or `JWT_SECRET` are not set.

Set environment variables before running:

```powershell
# Windows PowerShell
$env:DB_URL="jdbc:postgresql://localhost:5432/cyberopus"
$env:DB_USERNAME="cyberopus_user"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="your-secret-key-at-least-32-chars-long-here"
```

```bash
# Linux / macOS
export DB_URL=jdbc:postgresql://localhost:5432/cyberopus
export DB_USERNAME=cyberopus_user
export DB_PASSWORD=your_password
export JWT_SECRET=your-secret-key-at-least-32-chars-long-here
```

---

## How to Run

```bash
# From the /backend directory
mvn spring-boot:run
```

The application starts at `http://localhost:8080`.

Flyway automatically creates all tables and loads seed/demo data on first startup.

---

## How to Test

```bash
# Unit tests only (16 tests, no DB required)
mvn test

# All tests: 47 total (16 unit + 31 integration, H2 in-memory)
mvn verify
```

Integration tests use H2 in-memory database — no PostgreSQL required for testing.

---

## Swagger UI

Once running, open:

```
http://localhost:8080/swagger-ui.html
```

API docs JSON:

```
http://localhost:8080/v3/api-docs
```

Click **Authorize** → enter `Bearer <your_jwt_token>` to test protected endpoints.

---

## API Endpoint List

### Authentication
| Method | URL | Auth | Description |
|---|---|---|---|
| POST | /api/auth/register | Public | Register new customer |
| POST | /api/auth/login | Public | Login, receive JWT |
| GET | /api/auth/me | Required | Current user profile |

### Books
| Method | URL | Auth | Description |
|---|---|---|---|
| GET | /api/books | Public | List all books (paginated) |
| GET | /api/books/{id} | Public | Book detail + delivery date |
| GET | /api/books/search | Public | Search + filter + sort |
| GET | /api/books/{id}/related | Public | Related books (same category) |
| POST | /api/books | ADMIN | Create book |
| PUT | /api/books/{id} | ADMIN | Update book |
| DELETE | /api/books/{id} | ADMIN | Delete book |

### Categories
| Method | URL | Auth | Description |
|---|---|---|---|
| GET | /api/categories | Public | List all categories |
| GET | /api/categories/{id} | Public | Category detail |
| GET | /api/categories/{id}/books | Public | Books by category |
| POST | /api/categories | ADMIN | Create category |
| PUT | /api/categories/{id} | ADMIN | Update category |
| DELETE | /api/categories/{id} | ADMIN | Delete category |

### Brands / Publishers
| Method | URL | Auth | Description |
|---|---|---|---|
| GET | /api/brands | Public | List all brands |
| GET | /api/brands/{id} | Public | Brand detail |
| GET | /api/brands/{id}/books | Public | Books by brand |
| POST | /api/brands | ADMIN | Create brand |
| PUT | /api/brands/{id} | ADMIN | Update brand |
| DELETE | /api/brands/{id} | ADMIN | Delete brand |

### Cart
| Method | URL | Auth | Description |
|---|---|---|---|
| GET | /api/cart | Required | View cart with totals |
| POST | /api/cart/items | Required | Add book to cart |
| PUT | /api/cart/items/{itemId} | Required | Update quantity |
| DELETE | /api/cart/items/{itemId} | Required | Remove item |
| DELETE | /api/cart | Required | Clear entire cart |

### Addresses
| Method | URL | Auth | Description |
|---|---|---|---|
| GET | /api/addresses | Required | List my addresses |
| POST | /api/addresses | Required | Add address |
| PUT | /api/addresses/{id} | Required | Update address |
| DELETE | /api/addresses/{id} | Required | Delete address |
| PATCH | /api/addresses/{id}/default | Required | Set as default |

### Checkout
| Method | URL | Auth | Description |
|---|---|---|---|
| POST | /api/checkout | Required | Place order (full flow) |

### Orders
| Method | URL | Auth | Description |
|---|---|---|---|
| GET | /api/orders | Required | Order history |
| GET | /api/orders/{id} | Required | Order detail |
| POST | /api/orders/{id}/cancel | Required | Cancel order (48h window) |
| POST | /api/orders/{orderId}/buy-again | Required | Re-add item to cart |

### Payments
| Method | URL | Auth | Description |
|---|---|---|---|
| GET | /api/payments/order/{orderId} | Required | Payment detail for order |

### Rewards
| Method | URL | Auth | Description |
|---|---|---|---|
| GET | /api/rewards | Required | Balance + transaction history |

### Recommendations
| Method | URL | Auth | Description |
|---|---|---|---|
| GET | /api/recommendations | Required | Personalised book recommendations |

---

## Demo Credentials

| Role | Email | Password |
|---|---|---|
| Customer | alice@demo.com | Password123! |
| Customer | bob@demo.com | Password123! |
| Admin | admin@cyberopus.com | Admin123! |

Alice has:
- 2 pre-seeded delivery addresses
- Order history (DELIVERED, CONFIRMED-cancellable, CANCELLED)
- Reward points balance for redemption demo

---

## Test Payment Behavior

Payments are fully simulated. **No real payment gateway is called.**

| Scenario | How to trigger | Result |
|---|---|---|
| Successful payment | Any `cardHolderName` other than `FAIL_TEST` | Payment → SUCCESS, Order → CONFIRMED |
| Failed payment | Set `cardHolderName` to `FAIL_TEST` | Payment → FAILED, Order stays PENDING, cart preserved |

**Never store:** full card numbers, CVV, expiry. Only `maskedCardNumber` (e.g., `****1234`) and `cardHolderName` are stored.

---

## Reward Points Rules

| Rule | Value |
|---|---|
| Points earned per $1 spent | 10 points |
| Points required to get $1 off | 100 points |
| Maximum redemption per order | 20% of subtotal |
| Redemption cap example | On a $50 order, max $10 discount = max 1000 points |

Points are awarded after a **successful payment** only. Points are refunded on order cancellation.

---

## Recommendation Logic

1. **With order history:** Analyses previously purchased categories (weight ×2) and brands (weight ×1). Scores all available, in-stock, not-yet-purchased books and returns the top N ranked.
2. **Without order history:** Returns bestsellers (most total units sold across all orders), excluding books already purchased.

No machine learning. Entirely deterministic.

---

## 48-Hour Cancellation Rule

- Cancellation is allowed only within **48 hours** of `placed_at` (server-side timestamp).
- Cancellable order statuses: `PENDING`, `CONFIRMED`.
- Non-cancellable statuses: `SHIPPED`, `DELIVERED`, `CANCELLED`.
- On cancellation:
  - Payment status → `REFUNDED`
  - Book stock restored
  - Reward points earned → reversed
  - Reward points redeemed → refunded
  - All operations in a single `@Transactional` method

---

## Database Schema

12 tables: `users`, `categories`, `brands`, `books`, `carts`, `cart_items`, `addresses`, `orders`, `order_items`, `payments`, `reward_points`, `reward_point_transactions`

Schema is managed by **Flyway**:
- `V1__create_schema.sql` — all table definitions
- `V2__seed_data.sql` — demo data

Historical order prices never change: `order_items` stores `price_snapshot`, `book_title_snapshot`, and `book_author_snapshot` at the time of purchase.

---

## Troubleshooting

| Problem | Solution |
|---|---|
| `DB_PASSWORD` not set | Set the environment variable before running |
| `JWT_SECRET` too short | Must be at least 32 characters |
| Flyway baseline error | Set `spring.flyway.baseline-on-migrate=true` (already set) |
| Port already in use | Change `SERVER_PORT` environment variable |
| Tests fail on JDK 26 | Already handled via `--add-opens` in pom.xml and mock-maker-subclass |
| Swagger 401 | Click Authorize and enter `Bearer <token>` |
| CORS errors from frontend | Set `CORS_ORIGINS` to your frontend URL |
