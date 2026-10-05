-- CyberOpus – V1 Schema Migration
-- Creates all tables for the CyberOpus e-bookstore backend

CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    first_name    VARCHAR(100)  NOT NULL,
    last_name     VARCHAR(100)  NOT NULL,
    email         VARCHAR(255)  NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    phone         VARCHAR(20),
    role          VARCHAR(20)   NOT NULL DEFAULT 'CUSTOMER',
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS brands (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS books (
    id             BIGSERIAL PRIMARY KEY,
    title          VARCHAR(500)   NOT NULL,
    author         VARCHAR(255)   NOT NULL,
    description    TEXT,
    isbn           VARCHAR(20)    UNIQUE,
    price          NUMERIC(10,2)  NOT NULL CHECK (price > 0),
    stock_quantity INTEGER        NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    image_url      VARCHAR(1000),
    category_id    BIGINT         NOT NULL REFERENCES categories(id),
    brand_id       BIGINT         NOT NULL REFERENCES brands(id),
    available      BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_books_category  ON books(category_id);
CREATE INDEX IF NOT EXISTS idx_books_brand     ON books(brand_id);
CREATE INDEX IF NOT EXISTS idx_books_available ON books(available);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS carts (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL UNIQUE REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS cart_items (
    id           BIGSERIAL PRIMARY KEY,
    cart_id      BIGINT         NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    book_id      BIGINT         NOT NULL REFERENCES books(id),
    quantity     INTEGER        NOT NULL CHECK (quantity >= 1),
    price_at_add NUMERIC(10,2)  NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    UNIQUE (cart_id, book_id)
);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS addresses (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id),
    full_name   VARCHAR(200) NOT NULL,
    line1       VARCHAR(300) NOT NULL,
    line2       VARCHAR(300),
    city        VARCHAR(100) NOT NULL,
    state       VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20)  NOT NULL,
    country     VARCHAR(100) NOT NULL DEFAULT 'US',
    phone       VARCHAR(20),
    is_default  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_addresses_user ON addresses(user_id);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS orders (
    id               BIGSERIAL PRIMARY KEY,
    order_number     VARCHAR(50)    NOT NULL UNIQUE,
    user_id          BIGINT         NOT NULL REFERENCES users(id),
    address_snapshot TEXT           NOT NULL,
    subtotal         NUMERIC(10,2)  NOT NULL,
    reward_discount  NUMERIC(10,2)  NOT NULL DEFAULT 0,
    delivery_charge  NUMERIC(10,2)  NOT NULL DEFAULT 0,
    total_amount     NUMERIC(10,2)  NOT NULL,
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    placed_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_orders_user   ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_placed ON orders(placed_at);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS order_items (
    id                    BIGSERIAL PRIMARY KEY,
    order_id              BIGINT         NOT NULL REFERENCES orders(id),
    book_id               BIGINT         NOT NULL REFERENCES books(id),
    book_title_snapshot   VARCHAR(500)   NOT NULL,
    book_author_snapshot  VARCHAR(255)   NOT NULL,
    price_snapshot        NUMERIC(10,2)  NOT NULL,
    quantity              INTEGER        NOT NULL CHECK (quantity >= 1)
);

CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_book  ON order_items(book_id);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS payments (
    id                 BIGSERIAL PRIMARY KEY,
    order_id           BIGINT         NOT NULL UNIQUE REFERENCES orders(id),
    payment_method     VARCHAR(20)    NOT NULL,
    masked_card_number VARCHAR(20),
    card_holder_name   VARCHAR(200),
    transaction_ref    VARCHAR(100)   UNIQUE,
    amount             NUMERIC(10,2)  NOT NULL,
    status             VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    initiated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    completed_at       TIMESTAMPTZ
);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS reward_points (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL UNIQUE REFERENCES users(id),
    balance    INTEGER     NOT NULL DEFAULT 0 CHECK (balance >= 0),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------

CREATE TABLE IF NOT EXISTS reward_point_transactions (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users(id),
    order_id    BIGINT      REFERENCES orders(id),
    type        VARCHAR(20) NOT NULL,
    points      INTEGER     NOT NULL,
    description VARCHAR(300),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rpt_user ON reward_point_transactions(user_id);
