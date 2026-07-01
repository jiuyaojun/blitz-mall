-- ============================================================================
-- SUPERSEDED by V988__svg_placeholders.sql (Flyway migration in db/seed/).
-- This ad-hoc script uses placehold.co URLs and is NOT part of the Flyway
-- migration chain. If V988 has already been applied (setting inline SVG
-- data:image/svg+xml URIs), DO NOT run this script — it would overwrite
-- V988's SVG placeholders with placehold.co URLs.
-- Use only when you need a quick non-Flyway image fix and V988 has NOT run.
-- ============================================================================
--
-- Generate unique, consistent product images
-- Category colors via placehold.co with product name overlay
-- This produces clean, professional placeholder cards

-- Phone/tablet products (category 1,4,5) - Blue theme
UPDATE product SET main_image = CONCAT(
  'https://placehold.co/400x400/2563eb/ffffff?text=',
  REPLACE(SUBSTRING(name, 1, 20), ' ', '+'),
  '&font=raleway'
) WHERE category_id IN (1, 4, 5);

-- Computer products (category 2, 6) - Dark theme
UPDATE product SET main_image = CONCAT(
  'https://placehold.co/400x400/1e293b/ffffff?text=',
  REPLACE(SUBSTRING(name, 1, 20), ' ', '+'),
  '&font=raleway'
) WHERE category_id IN (2, 6);

-- Smart wearables (category 3, 7) - Purple theme
UPDATE product SET main_image = CONCAT(
  'https://placehold.co/400x400/7c3aed/ffffff?text=',
  REPLACE(SUBSTRING(name, 1, 20), ' ', '+'),
  '&font=raleway'
) WHERE category_id IN (3, 7);

-- Home/lifestyle (category 9, 14) - Teal theme
UPDATE product SET main_image = CONCAT(
  'https://placehold.co/400x400/0d9488/ffffff?text=',
  REPLACE(SUBSTRING(name, 1, 20), ' ', '+'),
  '&font=raleway'
) WHERE category_id IN (9, 14);

-- Fashion (category 8) - Rose theme
UPDATE product SET main_image = CONCAT(
  'https://placehold.co/400x400/e11d48/ffffff?text=',
  REPLACE(SUBSTRING(name, 1, 20), ' ', '+'),
  '&font=raleway'
) WHERE category_id = 8;

-- Sports (category 10) - Green theme
UPDATE product SET main_image = CONCAT(
  'https://placehold.co/400x400/16a34a/ffffff?text=',
  REPLACE(SUBSTRING(name, 1, 20), ' ', '+'),
  '&font=raleway'
) WHERE category_id = 10;

-- Food (category 11) - Amber theme
UPDATE product SET main_image = CONCAT(
  'https://placehold.co/400x400/d97706/ffffff?text=',
  REPLACE(SUBSTRING(name, 1, 20), ' ', '+'),
  '&font=raleway'
) WHERE category_id = 11;

-- Any remaining products with picsum or null images - use product-specific seed
UPDATE product SET main_image = CONCAT(
  'https://placehold.co/400x400/6366f1/ffffff?text=',
  REPLACE(SUBSTRING(name, 1, 20), ' ', '+'),
  '&font=raleway'
) WHERE main_image LIKE '%picsum%' OR main_image IS NULL OR main_image = '';
