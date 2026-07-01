-- Fix: assign products to shops, ensure shops/users exist
-- Create extra shop for seed products if not exists
INSERT IGNORE INTO shop (id, user_id, name, description, logo, status) VALUES
(1, (SELECT id FROM user WHERE username='shop1'), '测试店铺', '精选好物，品质保障', '', 1),
(2, (SELECT id FROM user WHERE username='merchant'), 'Blitz优选旗舰店', '官方授权，正品保证，售后无忧', '', 1);

-- Update existing users if needed (ensure MERCHANT role)
UPDATE user SET role_id = 3 WHERE username IN ('shop1', 'merchant') AND role_id != 3;

-- Assign products to shops: shop 1 gets products 1-15, shop 2 gets 16-30, rest stay NULL (platform)
UPDATE product SET shop_id = NULL WHERE shop_id IS NULL OR shop_id = 0;
UPDATE product SET shop_id = 1 WHERE id BETWEEN 1 AND 15 AND shop_id IS NULL;
UPDATE product SET shop_id = 2 WHERE id BETWEEN 16 AND 30 AND shop_id IS NULL;

-- Fix: make sure reviews reference correct user IDs
-- User 'test' should be user_id for most reviews, 'shop1' for some
UPDATE blitz_review SET user_id = (SELECT id FROM user WHERE username='test' LIMIT 1) WHERE user_id NOT IN (SELECT id FROM user);
