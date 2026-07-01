CREATE TABLE IF NOT EXISTS coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(16) DEFAULT 'OFFICIAL' COMMENT 'OFFICIAL/SHOP',
    shop_id BIGINT DEFAULT NULL,
    discount_type VARCHAR(16) DEFAULT 'FULL_REDUCTION' COMMENT 'FULL_REDUCTION/PERCENTAGE',
    discount_value DECIMAL(10,2) NOT NULL,
    min_spend DECIMAL(10,2) DEFAULT 0,
    total_count INT DEFAULT 100,
    claimed_count INT DEFAULT 0,
    start_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    end_time DATETIME,
    status INT DEFAULT 1 COMMENT '1-有效 0-失效',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_type (type),
    INDEX idx_shop_id (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    status VARCHAR(16) DEFAULT 'UNUSED' COMMENT 'UNUSED/USED/EXPIRED',
    claim_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    use_time DATETIME DEFAULT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_coupon_id (coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO coupon (name, type, discount_type, discount_value, min_spend, total_count, end_time) VALUES
('新人专享券', 'OFFICIAL', 'FULL_REDUCTION', 20.00, 99.00, 1000, '2026-12-31 23:59:59'),
('满200减30', 'OFFICIAL', 'FULL_REDUCTION', 30.00, 200.00, 500, '2026-12-31 23:59:59'),
('满500减80', 'OFFICIAL', 'FULL_REDUCTION', 80.00, 500.00, 200, '2026-12-31 23:59:59'),
('618狂欢券', 'OFFICIAL', 'FULL_REDUCTION', 50.00, 300.00, 300, '2026-07-31 23:59:59'),
('全场9折券', 'OFFICIAL', 'PERCENTAGE', 10.00, 100.00, 200, '2026-12-31 23:59:59');

INSERT INTO coupon (name, type, shop_id, discount_type, discount_value, min_spend, total_count, end_time) VALUES
('店铺满100减15', 'SHOP', 1, 'FULL_REDUCTION', 15.00, 100.00, 100, '2026-12-31 23:59:59'),
('店铺满300减50', 'SHOP', 1, 'FULL_REDUCTION', 50.00, 300.00, 50, '2026-12-31 23:59:59'),
('旗舰店95折', 'SHOP', 2, 'PERCENTAGE', 5.00, 200.00, 80, '2026-12-31 23:59:59'),
('旗舰店满500减100', 'SHOP', 2, 'FULL_REDUCTION', 100.00, 500.00, 30, '2026-12-31 23:59:59');
