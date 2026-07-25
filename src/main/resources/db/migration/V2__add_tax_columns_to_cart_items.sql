ALTER TABLE cart_items
    ADD COLUMN tax_class VARCHAR(50),
    ADD COLUMN tax_rate DECIMAL(10, 4),
    ADD COLUMN tax_amount DECIMAL(10, 2);
