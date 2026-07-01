-- ============================================================================
-- EXECUTION ORDER NOTE:
-- V990 runs AFTER V988__svg_placeholders.sql and V989__clean_sku.sql.
-- Lines 18-19 (clearing picsum/placehold images) are redundant when V988 has
-- already run — V988's inline SVG data URIs won't match the '%picsum%' or
-- '%placehold%' LIKE patterns, so those UPDATEs become no-ops.
-- Lines 2-15 (brand normalization: Huawei→华为, Xiaomi→小米, etc.) and
-- line 22 (garbled-brand cleanup) are the primary value of this migration
-- and are independent of the image pipeline.
-- Line 23 sets brand='Blitz' for null/empty brands — this runs BEFORE V994/
-- V995's brand assignments, so V994/V995 will overwrite the Blitz default
-- for products they match.
-- If the database is already seeded, running this migration is safe but its
-- image-clearing statements will be no-ops.
-- ============================================================================
SET NAMES utf8mb4;
UPDATE product SET brand='华为' WHERE brand LIKE '%Huawei%' OR brand LIKE '%huawei%' OR (brand IS NOT NULL AND brand !='' AND brand NOT LIKE '%华为%' AND id IN (SELECT id FROM (SELECT id FROM product WHERE name LIKE '%华为%' OR name LIKE '%Mate%') t));
UPDATE product SET brand='小米' WHERE brand='Xiaomi' OR brand='xiaomi' OR (brand IS NOT NULL AND brand !='' AND name LIKE '%小米%' AND brand NOT LIKE '%小米%');
UPDATE product SET brand='OPPO' WHERE brand='OPPO' OR (name LIKE '%OPPO%' AND brand IS NULL);
UPDATE product SET brand='一加' WHERE brand='OnePlus' OR (name LIKE '%一加%' AND brand IS NULL);
UPDATE product SET brand='戴尔' WHERE brand='Dell' OR (name LIKE '%戴尔%' AND brand IS NULL);
UPDATE product SET brand='海尔' WHERE brand='Haier' OR (name LIKE '%海尔%' AND brand IS NULL);
UPDATE product SET brand='美的' WHERE brand='Midea' OR (name LIKE '%美的%' AND brand IS NULL);
UPDATE product SET brand='格力' WHERE brand='Gree' OR (name LIKE '%格力%' AND brand IS NULL);
UPDATE product SET brand='戴森' WHERE brand='Dyson';
UPDATE product SET brand='小熊' WHERE brand='Bear';
UPDATE product SET brand='IKEA' WHERE brand='IKEA' OR (name LIKE '%宜家%' AND brand IS NULL);
UPDATE product SET brand='MUJI' WHERE brand='MUJI';
UPDATE product SET brand='膳魔师' WHERE brand='Thermos';
UPDATE product SET brand='网易严选' WHERE brand='NetEase' OR brand='NetEase';

-- Fix product images: use colored SVG placeholders
UPDATE product SET main_image='' WHERE main_image IS NULL OR main_image LIKE '%picsum%' OR main_image LIKE '%placehold%';
UPDATE product SET images='[]' WHERE images IS NULL OR images LIKE '%picsum%';

-- Fix any remaining garbled brands
UPDATE product SET brand='' WHERE brand IS NOT NULL AND LENGTH(brand) > 0 AND brand NOT REGEXP '^[a-zA-Z0-9 \\-\\+\\.\\&\\,\\u4e00-\\u9fff]+$';
UPDATE product SET brand='Blitz' WHERE brand IS NULL OR brand='';
