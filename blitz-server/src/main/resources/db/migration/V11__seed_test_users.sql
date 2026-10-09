-- V11: Seed test user accounts for demo & defense
-- BCrypt 10 rounds, passwords:
--   testuser/123456  testmerchant/123456  admin/admin123
--   test/User123456  demo/Demo123456  shop1/Shop123456
INSERT INTO user (username, password, phone, email, role_id, status) VALUES
('testuser', '$2b$10$v0fdxSLgATnhTomBgVcaue8D0P0YNbBLK0KfwGy5oVyFghjtMwpva', '13800000001', 'testuser@blitz.local', 2, 1),
('testmerchant', '$2b$10$v0fdxSLgATnhTomBgVcaue8D0P0YNbBLK0KfwGy5oVyFghjtMwpva', '13800000002', 'testmerchant@blitz.local', 3, 1),
('admin', '$2b$10$6l/bmA6r4os9Twk497KU/Ofi.IQQEJ4mIm80uLkTpW4kaqPjwpee.', '13800000000', 'admin@blitz.local', 1, 1)
ON DUPLICATE KEY UPDATE password=VALUES(password), role_id=VALUES(role_id), status=VALUES(status);

-- Also ensure test/demo/shop1 accounts exist
INSERT INTO user (username, password, phone, role_id, status) VALUES
('test', '$2b$10$eFUzsQWzmDGzTjV.yltcW.r9EUl7Y.LyDQwiAVGDqqZc3s6Xp6DqG', '13800000003', 2, 1),
('demo', '$2b$10$5J22nxR4Wqu6sAwLkbPmn.999djQ51gs3uJY/WpiRyL3DArU7GMX2', '13800000004', 1, 1)
ON DUPLICATE KEY UPDATE password=VALUES(password), role_id=VALUES(role_id), status=VALUES(status);

-- Seed shop for testmerchant (user id will be the auto-generated one from testmerchant insert)
INSERT INTO shop (user_id, name, description, logo, status)
SELECT u.id, 'Blitz 演示旗舰店', 'Blitz智能商城官方演示店铺，展示商家管理功能', NULL, 1
FROM user u WHERE u.username = 'testmerchant'
ON DUPLICATE KEY UPDATE name=VALUES(name);
