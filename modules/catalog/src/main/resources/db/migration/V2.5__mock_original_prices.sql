-- MOCK ORIGINAL_PRICE FOR A PORTION OF PRODUCT VARIANTS TO SHOW DISCOUNTS
UPDATE product_variants
SET original_price = ROUND(price * 1.25, 0)
WHERE length(sku_id) % 2 = 0;
