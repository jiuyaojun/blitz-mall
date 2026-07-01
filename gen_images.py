#!/usr/bin/env python3
"""
Generate professional product placeholder images.
Each product gets a unique, consistent image based on category color + product name.
Uses placehold.co which renders clean cards with text.
"""

import urllib.parse
import json

# Category color mapping
CAT_COLORS = {
    '手机数码': ('4A90D9', 'white'),   # Blue
    '电脑办公': ('2C3E50', 'white'),   # Dark blue
    '平板电脑': ('3498DB', 'white'),   # Light blue
    '智能穿戴': ('8E44AD', 'white'),   # Purple
    '家电': ('E67E22', 'white'),       # Orange
    '家居生活': ('16A085', 'white'),   # Teal
    '服饰鞋包': ('E91E63', 'white'),   # Pink
    '运动户外': ('4CAF50', 'white'),   # Green
    '食品生鲜': ('FF9800', 'white'),   # Orange
    '图书': ('795548', 'white'),       # Brown
    '母婴': ('FF5722', 'white'),       # Deep orange
    '美妆': ('F44336', 'white'),       # Red
}

# SQL to update images - use a reliable placeholder service
# Format: https://placehold.co/400x400/COLOR/white?text=NAME&font=raleway

# Build SQL updates for all products based on category
# Since we can't easily cross-reference MySQL here,
# just generate a generic approach: picum with product-specific seed

sql = """-- Update all product images to use consistent, category-colored placeholders
-- Each product gets a unique image based on its ID

UPDATE product SET main_image = CONCAT('https://placehold.co/400x400/4A90D9/white?text=', REPLACE(name, ' ', '+'), '&font=raleway') WHERE category_id IN (1, 4, 5) AND (main_image LIKE '%picsum%' OR main_image IS NULL OR main_image = '');

UPDATE product SET main_image = CONCAT('https://placehold.co/400x400/2C3E50/white?text=', REPLACE(name, ' ', '+'), '&font=raleway') WHERE category_id IN (2, 6) AND (main_image LIKE '%picsum%' OR main_image IS NULL OR main_image = '');

UPDATE product SET main_image = CONCAT('https://placehold.co/400x400/16A085/white?text=', REPLACE(name, ' ', '+'), '&font=raleway') WHERE category_id IN (9, 14) AND (main_image LIKE '%picsum%' OR main_image IS NULL OR main_image = '');

UPDATE product SET main_image = CONCAT('https://placehold.co/400x400/E91E63/white?text=', REPLACE(name, ' ', '+'), '&font=raleway') WHERE category_id IN (8) AND (main_image LIKE '%picsum%' OR main_image IS NULL OR main_image = '');

UPDATE product SET main_image = CONCAT('https://placehold.co/400x400/4CAF50/white?text=', REPLACE(name, ' ', '+'), '&font=raleway') WHERE category_id IN (10) AND (main_image LIKE '%picsum%' OR main_image IS NULL OR main_image = '');

UPDATE product SET main_image = CONCAT('https://placehold.co/400x400/FF9800/white?text=', REPLACE(name, ' ', '+'), '&font=raleway') WHERE category_id IN (11) AND (main_image LIKE '%picsum%' OR main_image IS NULL OR main_image = '');
"""

print(sql)
print("\n---\nRun this SQL against the blitz database:")
print("docker exec -i blitz-mysql mysql -uroot -proot blitz < /tmp/update_images.sql")
