-- ============================================================================
-- EXECUTION ORDER NOTE:
-- V996 runs AFTER V988__svg_placeholders.sql (which sets inline SVG data URIs),
-- V990__fix_brand_images.sql (which already cleared picsum/placehold images),
-- V994__utf8_fix.sql, and V995__fix_encoding.sql.
-- Since V988 already replaced all picsum/placehold images with inline SVG
-- (data:image/svg+xml), the WHERE clauses here (LIKE '%picsum%' and the
-- broader non-data/non-http/non-path catch-all) will match zero rows in a
-- fully V988-seeded database — this migration is a no-op.
-- Retained as a safety net for databases seeded BEFORE V988 was introduced.
-- ============================================================================
--
-- Remove random landscape picsum images, use empty string for CSS placeholder fallback
UPDATE product SET main_image = '' WHERE main_image LIKE '%picsum%';
UPDATE product SET images = '[]' WHERE images LIKE '%picsum%';

-- Also fix the original seed products that used picsum
UPDATE product SET main_image = '' WHERE main_image IS NOT NULL AND main_image != '' AND main_image NOT LIKE 'data:%' AND main_image NOT LIKE '/%' AND main_image NOT LIKE 'http%://%/%product%';
