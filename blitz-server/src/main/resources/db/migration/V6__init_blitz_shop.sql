-- Shop table and product.shop_id column
CREATE TABLE IF NOT EXISTS shop (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    name        VARCHAR(128) NOT NULL,
    description TEXT,
    logo        VARCHAR(512) DEFAULT NULL,
    status      INT DEFAULT 1 COMMENT '1-营业 0-休息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     INT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺表';

-- Flyway ensures these run only once per environment
ALTER TABLE product ADD COLUMN shop_id BIGINT DEFAULT NULL AFTER category_id;
ALTER TABLE product ADD INDEX idx_shop_id (shop_id);
