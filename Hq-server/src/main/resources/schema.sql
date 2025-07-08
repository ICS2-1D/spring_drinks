-- ========================
-- DRINKS TABLE
-- ========================
CREATE TABLE IF NOT EXISTS drinks
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    drink_name     VARCHAR(255) UNIQUE NOT NULL, -- Added NOT NULL
    drink_quantity INT NOT NULL,                 -- Added NOT NULL
    drink_price    DOUBLE NOT NULL               -- Added NOT NULL
);

-- ========================
-- CUSTOMERS TABLE
-- ========================
CREATE TABLE IF NOT EXISTS customers (
                                         customer_id         BIGINT PRIMARY KEY AUTO_INCREMENT, -- Changed from 'id' to 'customer_id' for clarity and better foreign key naming
                                         customer_name       VARCHAR(255) NOT NULL,
                                         customer_phone_number VARCHAR(20) UNIQUE NOT NULL -- Added UNIQUE and NOT NULL
);


-- ========================
-- ORDERS TABLE
-- ========================
CREATE TABLE IF NOT EXISTS orders
(
    order_id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number          VARCHAR(255) UNIQUE NOT NULL, -- Added NOT NULL
    branch                VARCHAR(50) NOT NULL,         -- Added NOT NULL
    customer_id           BIGINT NOT NULL,              -- ADDED: Foreign key to customers table
    order_status          VARCHAR(50) NOT NULL,         -- Added NOT NULL
    order_date            TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- Added NOT NULL
    total_amount          DOUBLE NOT NULL,              -- Added NOT NULL

    CONSTRAINT fk_customer_id
        FOREIGN KEY (customer_id)
            REFERENCES customers (customer_id) -- REFERENCES the new customer_id in customers table
            ON DELETE RESTRICT -- Changed to RESTRICT to prevent deleting customers with active orders
);


-- ========================
-- ORDER_ITEMS TABLE
-- ========================
CREATE TABLE IF NOT EXISTS order_items
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id    BIGINT NOT NULL,     -- Added NOT NULL
    drink_id    BIGINT NOT NULL,     -- Added NOT NULL
    quantity    INT NOT NULL,        -- Added NOT NULL
    unit_price  DOUBLE NOT NULL,     -- Added NOT NULL
    total_price DOUBLE NOT NULL,     -- Added NOT NULL

    CONSTRAINT fk_order_item_order_id
        FOREIGN KEY (order_id)
            REFERENCES orders (order_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_order_item_drink_id
        FOREIGN KEY (drink_id)
            REFERENCES drinks (id)
            ON DELETE RESTRICT -- Changed to RESTRICT to prevent deleting drinks with active order items
);

-- ========================
-- PAYMENTS TABLE
-- ========================
CREATE TABLE IF NOT EXISTS payments
(
    payment_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id        BIGINT NOT NULL,                   -- Added NOT NULL
    customer_number VARCHAR(20) NOT NULL,              -- Added NOT NULL
    payment_method  VARCHAR(50) NOT NULL,              -- Added NOT NULL
    payment_status  VARCHAR(50) NOT NULL,              -- Added NOT NULL
    transaction_id  VARCHAR(100) UNIQUE NOT NULL,      -- Added NOT NULL
    payment_time    TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- Added NOT NULL

    CONSTRAINT fk_payment_order_id
        FOREIGN KEY (order_id)
            REFERENCES orders (order_id)
            ON DELETE CASCADE
);

-- ========================
-- ADMINS TABLE
-- ========================
CREATE TABLE IF NOT EXISTS admins
(
    id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(50) DEFAULT 'ADMIN' NOT NULL, -- Added NOT NULL
    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL -- Added NOT NULL
);

-- ========================
-- BRANCH_INVENTORY TABLE
-- ========================
CREATE TABLE IF NOT EXISTS branch_inventory
(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    branch              VARCHAR(50) NOT NULL,
    drink_id            BIGINT NOT NULL,
    quantity            INT NOT NULL DEFAULT 0,
    low_stock_threshold INT NOT NULL DEFAULT 10,
    last_updated        TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_branch_inventory_drink_id
        FOREIGN KEY (drink_id)
            REFERENCES drinks (id)
            ON DELETE CASCADE,
    CONSTRAINT uq_branch_drink UNIQUE (branch, drink_id)
);
