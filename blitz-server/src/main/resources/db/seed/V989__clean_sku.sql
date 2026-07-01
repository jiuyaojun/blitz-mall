SET NAMES utf8mb4;

-- Clean garbled SKU names caused by latin1-encoded Chinese text stored as utf8mb4.
-- Scans ALL sku rows for mojibake patterns (characters like Ã, â, é, ç, etc.)
-- and updates garbled names to a clean default based on the associated product name.
--
-- Detection criteria:
--   1) sku.name contains known latin1 mojibake characters (Ã¢éç‰ˆ® etc.)
--   2) sku.name has mixed byte/char lengths indicating non-ASCII corruption
--
-- When a name is detected as garbled, it is replaced with CONCAT(product.name, ' 默认规格')
-- as a safe fallback. Targeted per-product fixes in later migrations (e.g. V993)
-- will further refine these to the correct SKU variant names.

UPDATE sku s
JOIN product p ON s.product_id = p.id
SET s.name = CONCAT(p.name, ' 默认规格')
WHERE s.name REGEXP '[Ã¢éç‰ˆ®]'
   OR LENGTH(s.name) != CHAR_LENGTH(s.name);
