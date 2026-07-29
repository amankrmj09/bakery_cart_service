-- Drop the existing foreign key constraint
ALTER TABLE cart_items DROP CONSTRAINT fk_cart_items_on_cart;

-- Recreate it with ON DELETE CASCADE
ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_on_cart
    FOREIGN KEY (cart_id) REFERENCES carts (id)
    ON DELETE CASCADE;
