# CyberOpus – Capstone Implementation Traceability

This document maps every IBM AI Specialist Cloud FullStack Capstone requirement to the corresponding CyberOpus implementation.

---

## Requirement Traceability Matrix

| Capstone Requirement | CyberOpus Implementation | API / Component | Status |
|---|---|---|---|
| User registration | `AuthService.register()` creates user, cart, and reward points record | `POST /api/auth/register` | ✅ Implemented |
| User login / authentication | `AuthService.login()` validates credentials and returns signed JWT | `POST /api/auth/login` | ✅ Implemented |
| JWT-based session management | `JwtTokenProvider` + `JwtAuthenticationFilter` on every request | `SecurityConfig`, `JwtAuthenticationFilter` | ✅ Implemented |
| BCrypt password hashing | `BCryptPasswordEncoder` (strength 12) — plaintext never stored | `SecurityConfig.passwordEncoder()` | ✅ Implemented |
| Browse all books | Paginated book listing with sort | `GET /api/books` | ✅ Implemented |
| View book detail | Full book detail with category, brand, tentative delivery date | `GET /api/books/{id}` | ✅ Implemented |
| Tentative delivery date | Computed as today + N business days (configurable, default 5) | `BookService.computeDeliveryDate()` | ✅ Implemented |
| Search books by keyword | JPQL search across title, author, description | `GET /api/books/search?q=` | ✅ Implemented |
| Filter by category | Category filter in search | `GET /api/books/search?categoryId=` | ✅ Implemented |
| Filter by brand/publisher | Brand filter in search | `GET /api/books/search?brandId=` | ✅ Implemented |
| Filter by price range | Min/max price filter in search | `GET /api/books/search?minPrice=&maxPrice=` | ✅ Implemented |
| Sort results | Sort by price, title (via Pageable sort param) | `GET /api/books?sort=price,asc` | ✅ Implemented |
| Browse books by category | Dedicated category-books endpoint | `GET /api/categories/{id}/books` | ✅ Implemented |
| Browse brands/publishers | List all brands; list books by brand | `GET /api/brands`, `GET /api/brands/{id}/books` | ✅ Implemented |
| Related books | Same-category books, excluding current book, limited to 8 | `GET /api/books/{id}/related` | ✅ Implemented |
| Add book to cart | Validates availability, stock, increments if already in cart | `POST /api/cart/items` | ✅ Implemented |
| Update cart quantity | Validates stock before update | `PUT /api/cart/items/{itemId}` | ✅ Implemented |
| Remove cart item | Removes line from cart | `DELETE /api/cart/items/{itemId}` | ✅ Implemented |
| View cart with totals | Returns items, subtotal, item count | `GET /api/cart` | ✅ Implemented |
| Clear cart | Removes all items | `DELETE /api/cart` | ✅ Implemented |
| Manage delivery addresses | Full CRUD + default flag management | `GET/POST/PUT/DELETE /api/addresses` | ✅ Implemented |
| Set default address | Clear previous default, set new | `PATCH /api/addresses/{id}/default` | ✅ Implemented |
| Checkout: address selection | Address ID validated to belong to user | `POST /api/checkout` (addressId field) | ✅ Implemented |
| Checkout: price server-side | Subtotal computed from live `books.price`, not client-supplied | `CheckoutService` | ✅ Implemented |
| Checkout: stock validation | Each cart item checked against `books.stock_quantity` | `CheckoutService` | ✅ Implemented |
| Historical price preservation | `order_items.price_snapshot` captured at checkout | `OrderItem.priceSnapshot` | ✅ Implemented |
| Historical title/author preservation | `book_title_snapshot`, `book_author_snapshot` captured | `OrderItem.bookTitleSnapshot/bookAuthorSnapshot` | ✅ Implemented |
| Place an order | Full order creation flow | `POST /api/checkout` | ✅ Implemented |
| Purchase confirmation | Order response with status, items, payment | Checkout response body | ✅ Implemented |
| Simulated payment (Credit Card) | CREDIT_CARD payment method | `POST /api/checkout` (paymentMethod: CREDIT_CARD) | ✅ Implemented |
| Simulated payment (Debit Card) | DEBIT_CARD payment method | `POST /api/checkout` (paymentMethod: DEBIT_CARD) | ✅ Implemented |
| Payment PENDING/SUCCESS/FAILED/REFUNDED | Full status lifecycle | `PaymentStatus` enum, `PaymentService` | ✅ Implemented |
| Deterministic payment simulation | `cardHolderName=FAIL_TEST` → FAILED; else SUCCESS | `PaymentService.processPayment()` | ✅ Implemented |
| No full card number stored | Only `masked_card_number` (e.g. ****1234) stored | `Payment` entity | ✅ Implemented |
| Cart cleared after successful order | `cartEntity.getItems().clear()` in `CheckoutService` | `CheckoutService` | ✅ Implemented |
| Inventory decremented on order | `book.stock_quantity -= item.quantity` | `CheckoutService` | ✅ Implemented |
| View order history | Paginated, newest first | `GET /api/orders` | ✅ Implemented |
| View order detail | Full detail with items and payment | `GET /api/orders/{id}` | ✅ Implemented |
| Redeem reward/gift points | Validated redemption applied as discount | `POST /api/checkout` (rewardPointsToRedeem) | ✅ Implemented |
| Reward points balance | Current balance and transaction history | `GET /api/rewards` | ✅ Implemented |
| Earn reward points | Awarded after successful payment | `RewardService.earnPoints()` | ✅ Implemented |
| Redemption cap | Max 20% of order subtotal (configurable) | `RewardService.validateAndComputeDiscount()` | ✅ Implemented |
| Prevent over-redemption | Throws `InsufficientRewardPointsException` | `RewardService` | ✅ Implemented |
| Reward transaction history | EARN, REDEEM, REFUND transaction records | `RewardPointTransaction` entity | ✅ Implemented |
| Personalised recommendations | Deterministic algorithm: category ×2 + brand ×1 scores | `GET /api/recommendations` | ✅ Implemented |
| Recommendation fallback | Bestsellers when no order history | `BookRepository.findBestsellersExcludingPurchased()` | ✅ Implemented |
| Buy Again | Re-adds previously purchased book to cart at **current** price | `POST /api/orders/{orderId}/buy-again` | ✅ Implemented |
| Buy Again uses current availability | Validates `available=true` and `stockQuantity>0` | `BuyAgainService` | ✅ Implemented |
| 48-hour cancellation window | Server-side check: `NOW() - placed_at < 48h` | `OrderService.cancelOrder()` | ✅ Implemented |
| Cancellation: status check | Only PENDING/CONFIRMED are cancellable | `OrderService.CANCELLABLE_STATUSES` | ✅ Implemented |
| Cancellation: stock restored | `book.stockQuantity += item.quantity` | `OrderService.cancelOrder()` | ✅ Implemented |
| Cancellation: payment refunded | Payment status → REFUNDED | `PaymentService.refundPayment()` | ✅ Implemented |
| Cancellation: points refunded | Earn reversed + redeem refunded | `RewardService.refundPoints()` | ✅ Implemented |
| Cancellation: ownership check | JWT user must own the order | `OrderService.cancelOrder()` | ✅ Implemented |
| User ownership enforcement | All user-scoped resources verify `userId == currentUser.id` | All services | ✅ Implemented |
| Protected endpoints | JWT required for cart, addresses, orders, rewards, recommendations | `SecurityConfig` | ✅ Implemented |
| Role-based access control | ADMIN role required for book/category/brand write operations | `@PreAuthorize("hasRole('ADMIN')")` | ✅ Implemented |
| Input validation | All request DTOs validated with Jakarta Bean Validation | `@Valid` + `@NotBlank`, `@Positive`, etc. | ✅ Implemented |
| Centralized error handling | `@RestControllerAdvice` returns consistent JSON | `GlobalExceptionHandler` | ✅ Implemented |
| PostgreSQL database | Production database via Flyway-managed schema | `application.properties`, Flyway | ✅ Implemented |
| Externalized configuration | All secrets via environment variables | `application.properties` + `.env.example` | ✅ Implemented |
| Database migrations | Flyway V1 schema + V2 seed data | `db/migration/*.sql` | ✅ Implemented |
| OpenAPI / Swagger | SpringDoc OpenAPI 3 with Bearer JWT auth | `http://localhost:8080/swagger-ui.html` | ✅ Implemented |
| Unit tests (JUnit 5 + Mockito) | AuthService, BookService, OrderService, RewardService | `src/test/java/...service/` | ✅ Implemented |
| Integration tests (Spring Boot Test) | AuthController, BookController, CartController, AddressController | `src/test/java/...controller/` | ✅ Implemented |
| Demo seed data | 3 users, 20 books, 6 categories, 5 brands, addresses, orders, points | `V2__seed_data.sql` | ✅ Implemented |

---

## Business Rules Enforced

| Rule | Location |
|---|---|
| Price must be > 0 | `@DecimalMin("0.01")` on BookRequest + DB CHECK constraint |
| Stock must be ≥ 0 | `@PositiveOrZero` on BookRequest + DB CHECK constraint |
| Title must not be blank | `@NotBlank` on BookRequest |
| Author must not be blank | `@NotBlank` on BookRequest |
| Cart quantity ≥ 1 | `@Min(1)` on CartItemRequest |
| Cart quantity ≤ stock | `InsufficientStockException` in CartService |
| Checkout: live price used | Subtotal re-computed from DB in CheckoutService (not client value) |
| Reward redemption ≤ balance | `InsufficientRewardPointsException` in RewardService |
| Reward redemption ≤ 20% subtotal | Cap check in RewardService |
| Payment: no raw card data | Only maskedCardNumber and cardHolderName accepted |
| Cancellation: 48h window server-side | Duration check in OrderService — no client bypass possible |
| Order ownership | ForbiddenException if `order.userId != currentUserId` |

---

## Security Controls

| Control | Implementation |
|---|---|
| Password hashing | BCryptPasswordEncoder (strength 12) |
| JWT authentication | JJWT 0.12.6, HS256 signing |
| Stateless sessions | `SessionCreationPolicy.STATELESS` |
| CORS restriction | Configurable via `CORS_ORIGINS` environment variable |
| CSRF disabled | Stateless JWT API — CSRF not applicable |
| 401 for unauthenticated | Custom `AuthenticationEntryPoint` |
| 403 for wrong owner | `ForbiddenException` in all service ownership checks |
| Secrets in environment | Never hardcoded in source code |
| No stack traces to client | `GlobalExceptionHandler` returns generic messages |
| No PII in logs | Logging does not include passwords, tokens, or card data |

---

## Assumptions Made

| # | Assumption | Rationale |
|---|---|---|
| 1 | Java 21 + Spring Boot 3.4.5 used instead of Java 25 / Spring Boot 4.1.0 | Neither version exists as a released product |
| 2 | Delivery charge is a flat $4.99 (configurable) | No specification provided; simple and configurable |
| 3 | Tentative delivery = 5 business days (configurable) | Realistic default; configurable via `app.delivery.business-days` |
| 4 | 1 point = $0.01 (100 points = $1 discount) | Simple round-number conversion |
| 5 | 10 points earned per $1 spent | Standard loyalty rate |
| 6 | Max 20% of order subtotal redeemable as points | Prevents full order discount; configurable |
| 7 | `cardHolderName=FAIL_TEST` triggers simulated payment failure | Deterministic demo control |
| 8 | Admin user created via seed data only (no admin registration endpoint) | Reduces attack surface |
| 9 | No email notifications | Out of scope; would require SMTP/SES integration |
| 10 | Recommendation returns up to 10 books (configurable via `?limit=N`) | Reasonable default |
