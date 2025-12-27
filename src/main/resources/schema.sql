DROP TABLE IF EXISTS order_lines CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP SEQUENCE IF EXISTS order_id_seq CASCADE;

CREATE SEQUENCE order_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE orders (
                        id BIGINT PRIMARY KEY DEFAULT nextval('order_id_seq'),
                        order_number VARCHAR(255) NOT NULL
);

CREATE TABLE order_lines (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             item_name VARCHAR(255) NOT NULL,
                             quantity INTEGER NOT NULL CHECK (quantity > 0),
                             price INTEGER NOT NULL CHECK (price > 0),
                             FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);