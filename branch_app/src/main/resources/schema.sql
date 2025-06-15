-- ========================
-- CUSTOMERS TABLE
-- ========================
CREATE TABLE IF NOT EXISTS customers
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    Customer_name   VARCHAR(255) UNIQUE,
    Customer_number VARCHAR(20) UNIQUE
);

-- ========================
-- DRINKS TABLE
-- ========================
CREATE TABLE IF NOT EXISTS drinks
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    drink_name     VARCHAR(255) UNIQUE,
    drink_quantity INT,
    drink_price    DOUBLE
);

-- ========================
-- ORDERS TABLE
-- ========================
CREATE TABLE IF NOT EXISTS orders
(
    order_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(255) UNIQUE,
    branch       VARCHAR(50), -- From Branch enum
    customer_id  BIGINT,
    order_status VARCHAR(50), -- From OrderStatus enum
    order_date   TIMESTAMP,
    total_amount DOUBLE,
    CONSTRAINT fk_customer_id
        FOREIGN KEY (customer_id)
            REFERENCES customers (id)
            ON DELETE SET NULL
);

-- ========================
-- ORDER_ITEMS TABLE
-- ========================
CREATE TABLE IF NOT EXISTS order_items
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id    BIGINT,
    drink_id    BIGINT,
    quantity    INT,
    unit_price  DOUBLE,
    total_price DOUBLE,
    CONSTRAINT fk_order_id
        FOREIGN KEY (order_id)
            REFERENCES orders (order_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_drink_id
        FOREIGN KEY (drink_id)
            REFERENCES drinks (id)
            ON DELETE CASCADE
);

-- ========================
-- PAYMENTS TABLE
-- ========================
CREATE TABLE IF NOT EXISTS payments
(
    payment_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id        BIGINT,
    customer_number VARCHAR(20), -- number used for the payment
    payment_method  VARCHAR(50), -- e.g., M-PESA, CARD
    payment_status  VARCHAR(50), -- e.g., SUCCESS, FAILED
    transaction_id  VARCHAR(100) UNIQUE,
    payment_time    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_order_id
        FOREIGN KEY (order_id)
            REFERENCES orders (order_id)
            ON DELETE CASCADE
);
