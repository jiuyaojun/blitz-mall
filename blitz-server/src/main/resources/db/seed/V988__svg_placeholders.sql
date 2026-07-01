SET NAMES utf8mb4;
-- Generate SVG placeholders by category color + brand initial
-- Each category gets a distinct gradient background + product initial letter

UPDATE product SET main_image = CONCAT(
  'data:image/svg+xml,',
  '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="400">',
  '<rect width="400" height="400" fill="',
  CASE
    WHEN category_id IN (1,4) THEN '%23e3f2fd'    -- 手机数码: light blue
    WHEN category_id = 2 THEN '%23fce4ec'          -- 电脑办公: light pink
    WHEN category_id = 3 THEN '%23e8f5e9'          -- 家用电器: light green
    WHEN category_id = 8 THEN '%23fff3e0'          -- 服饰鞋包: light orange
    WHEN category_id = 9 THEN '%23f3e5f5'          -- 家居日用: light purple
    WHEN category_id = 16 THEN '%23e8eaf6'         -- 食品生鲜: indigo
    WHEN category_id = 20 THEN '%23fce4ec'         -- 美妆个护: pink
    WHEN category_id = 24 THEN '%23e0f2f1'         -- 运动户外: teal
    WHEN category_id = 28 THEN '%23eceff1'         -- 汽车用品: blue grey
    ELSE '%23f5f5f5'
  END, '"/>',
  '<text x="200" y="180" text-anchor="middle" font-family="sans-serif" font-size="80" font-weight="bold" fill="',
  CASE
    WHEN category_id IN (1,4) THEN '%231976d2'
    WHEN category_id = 2 THEN '%23c62828'
    WHEN category_id = 3 THEN '%232e7d32'
    WHEN category_id = 8 THEN '%23e65100'
    WHEN category_id = 9 THEN '%237b1fa2'
    WHEN category_id = 16 THEN '%232832c6'
    WHEN category_id = 20 THEN '%23ad1457'
    WHEN category_id = 24 THEN '%2300685c'
    WHEN category_id = 28 THEN '%2337464f'
    ELSE '%23666'
  END, '">',
  UPPER(LEFT(COALESCE(NULLIF(brand,''), name), 1)),
  '</text>',
  '<text x="200" y="240" text-anchor="middle" font-family="sans-serif" font-size="22" fill="',
  CASE
    WHEN category_id IN (1,4) THEN '%231976d2'
    WHEN category_id = 2 THEN '%23c62828'
    WHEN category_id = 3 THEN '%232e7d32'
    WHEN category_id = 8 THEN '%23e65100'
    WHEN category_id = 9 THEN '%237b1fa2'
    WHEN category_id = 16 THEN '%232832c6'
    WHEN category_id = 20 THEN '%23ad1457'
    WHEN category_id = 24 THEN '%2300685c'
    WHEN category_id = 28 THEN '%2337464f'
    ELSE '%23666'
  END, '" opacity="0.6">',
  COALESCE(NULLIF(brand,''), name),
  '</text>',
  '</svg>'
) WHERE main_image IS NULL OR main_image = '' OR main_image LIKE '%picsum%' OR main_image LIKE '%placehold%';
