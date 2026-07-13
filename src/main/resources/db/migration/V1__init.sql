CREATE TABLE cart_items
(
    id                       UUID           NOT NULL,
    cart_id                  UUID           NOT NULL,
    product_id               UUID           NOT NULL,
    product_sku              VARCHAR(100),
    product_name             VARCHAR(255)   NOT NULL,
    product_category         VARCHAR(100),
    quantity                 INTEGER        NOT NULL,
    unit_price               DECIMAL(10, 2) NOT NULL,
    total_price              DECIMAL(12, 2) NOT NULL,
    original_unit_price      DECIMAL(10, 2),
    status                   VARCHAR(255)   NOT NULL,
    special_instructions     TEXT,
    product_description      TEXT,
    product_image_url        VARCHAR(500),
    preparation_time_minutes INTEGER,
    currency_code            VARCHAR(3),
    is_available             BOOLEAN,
    stock_quantity           INTEGER,
    availability_message     VARCHAR(255),
    price_changed            BOOLEAN,
    price_change_amount      DECIMAL(10, 2),
    added_at                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at               TIMESTAMP WITHOUT TIME ZONE,
    last_validated_at        TIMESTAMP WITHOUT TIME ZONE,
    saved_for_later_at       TIMESTAMP WITHOUT TIME ZONE,
    removed_at               TIMESTAMP WITHOUT TIME ZONE,
    metadata                 TEXT,
    added_from               VARCHAR(50),
    CONSTRAINT pk_cart_items PRIMARY KEY (id)
);

CREATE TABLE carts
(
    id                   UUID           NOT NULL,
    user_id              UUID,
    session_id           VARCHAR(255),
    status               VARCHAR(255)   NOT NULL,
    customer_name        VARCHAR(100),
    customer_email       VARCHAR(255),
    subtotal             DECIMAL(12, 2) NOT NULL,
    tax_amount           DECIMAL(10, 2),
    discount_amount      DECIMAL(10, 2),
    total_amount         DECIMAL(12, 2) NOT NULL,
    item_count           INTEGER        NOT NULL,
    total_quantity       INTEGER        NOT NULL,
    currency_code        VARCHAR(3),
    discount_code        VARCHAR(50),
    special_instructions TEXT,
    delivery_type        VARCHAR(20),
    delivery_address     TEXT,
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at           TIMESTAMP WITHOUT TIME ZONE,
    expires_at           TIMESTAMP WITHOUT TIME ZONE,
    last_activity_at     TIMESTAMP WITHOUT TIME ZONE,
    abandoned_at         TIMESTAMP WITHOUT TIME ZONE,
    converted_at         TIMESTAMP WITHOUT TIME ZONE,
    converted_order_id   UUID,
    metadata             TEXT,
    source               VARCHAR(50),
    device_type          VARCHAR(20),
    user_agent           TEXT,
    CONSTRAINT pk_carts PRIMARY KEY (id)
);

CREATE INDEX idx_cart_expires ON carts (expires_at);

CREATE INDEX idx_cart_item_added ON cart_items (added_at);

CREATE INDEX idx_cart_item_product ON cart_items (product_id);

CREATE INDEX idx_cart_item_status ON cart_items (status);

CREATE INDEX idx_cart_product_status ON cart_items (cart_id, product_id, status);

CREATE INDEX idx_cart_session ON carts (session_id);

CREATE INDEX idx_cart_status ON carts (status);

CREATE INDEX idx_cart_updated ON carts (updated_at);

CREATE INDEX idx_cart_user ON carts (user_id);

CREATE INDEX idx_cart_user_status ON carts (user_id, status);

ALTER TABLE cart_items
    ADD CONSTRAINT FK_CART_ITEMS_ON_CART FOREIGN KEY (cart_id) REFERENCES carts (id);

CREATE INDEX idx_cart_item_cart ON cart_items (cart_id);
