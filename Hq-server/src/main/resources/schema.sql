-- ========================
-- DRINKS TABLE (MODIFIED)
-- No longer contains quantity. It's now a catalog of drink types.
-- ========================
CREATE TABLE IF NOT EXISTS drinks
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    drink_name     VARCHAR(255) UNIQUE NOT NULL,
    drink_price    DOUBLE NOT NULL
);

-- ========================
-- BRANCH_STOCK TABLE (NEW)
-- This table will manage the inventory for each drink at each branch.
-- ========================
CREATE TABLE IF NOT EXISTS branch_stock (
                                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                            branch VARCHAR(50) NOT NULL,
                                            drink_id BIGINT NOT NULL,
                                            quantity INT NOT NULL,
                                            low_stock_threshold INT NOT NULL DEFAULT 10, -- Default low stock threshold
                                            CONSTRAINT fk_stock_drink_id FOREIGN KEY (drink_id) REFERENCES drinks(id) ON DELETE CASCADE,
                                            UNIQUE (branch, drink_id) -- Ensures one entry per drink per branch
);

-- ========================
-- RESTOCK_REQUESTS TABLE (NEW)
-- This table will store requests for restocking from branches.
-- ========================
CREATE TABLE IF NOT EXISTS restock_requests (
                                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                branch VARCHAR(50) NOT NULL,
                                                drink_id BIGINT NOT NULL,
                                                requested_quantity INT NOT NULL,
                                                request_date TIMESTAMP NOT NULL,
                                                fulfilled BOOLEAN NOT NULL DEFAULT FALSE,
                                                CONSTRAINT fk_restock_drink_id FOREIGN KEY (drink_id) REFERENCES drinks(id) ON DELETE CASCADE
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