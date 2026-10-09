-- ============================================================
-- V10: Schema cleanup — remove orphaned tables, standardize types
-- ============================================================

-- 1. Drop unused product_attribute table (no Java entity, no seed data)
DROP TABLE IF EXISTS product_attribute;

-- 2. Standardize blitz_review.deleted type: TINYINT → INT
--    Matches all other tables (product, sku, user, address, order, kb_document)
ALTER TABLE blitz_review MODIFY COLUMN deleted INT DEFAULT 0;

-- 3. Add order.address_id for traceability (which saved address was used)
--    Companion to the denormalized receiver_* snapshot fields
--    Note: Flyway ensures this runs only once per environment
ALTER TABLE `order` ADD COLUMN address_id BIGINT DEFAULT NULL AFTER user_id;
