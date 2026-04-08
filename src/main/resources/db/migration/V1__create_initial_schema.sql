CREATE TABLE users (
    id         BIGSERIAL    PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    phone      VARCHAR(20),
    role       VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE addresses (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    street     VARCHAR(255) NOT NULL,
    city       VARCHAR(100) NOT NULL,
    state      VARCHAR(100) NOT NULL,
    zip_code   VARCHAR(20)  NOT NULL,
    country    VARCHAR(100) NOT NULL,
    is_default BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE categories (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_id   BIGINT       REFERENCES categories(id) ON DELETE SET NULL
);

CREATE TABLE products (
    id          BIGSERIAL      PRIMARY KEY,
    name        VARCHAR(255)   NOT NULL,
    description TEXT,
    price       NUMERIC(10, 2) NOT NULL,
    stock       INTEGER        NOT NULL DEFAULT 0,
    category_id BIGINT         REFERENCES categories(id) ON DELETE SET NULL,
    created_at  TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE product_images (
    id         BIGSERIAL    PRIMARY KEY,
    product_id BIGINT       NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    url        VARCHAR(500) NOT NULL,
    public_id  VARCHAR(255) NOT NULL,
    is_primary BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE orders (
    id                  BIGSERIAL      PRIMARY KEY,
    user_id             BIGINT         NOT NULL REFERENCES users(id),
    status              VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    total               NUMERIC(10, 2) NOT NULL,
    shipping_address_id BIGINT         REFERENCES addresses(id),
    created_at          TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE order_items (
    id                BIGSERIAL      PRIMARY KEY,
    order_id          BIGINT         NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id        BIGINT         NOT NULL REFERENCES products(id),
    quantity          INTEGER        NOT NULL,
    price_at_purchase NUMERIC(10, 2) NOT NULL
);
