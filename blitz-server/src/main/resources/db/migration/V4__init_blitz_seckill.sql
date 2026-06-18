CREATE TABLE IF NOT EXISTS seckill_activity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status INT DEFAULT 0 COMMENT '0-未开始 1-进行中 2-已结束',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_start_end (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS seckill_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    activity_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    seckill_price DECIMAL(10,2) NOT NULL,
    seckill_stock INT NOT NULL COMMENT '秒杀总库存',
    limit_per_user INT DEFAULT 1 COMMENT '每人限购',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_activity_id (activity_id),
    INDEX idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS seckill_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    seckill_product_id BIGINT NOT NULL,
    status INT DEFAULT 1 COMMENT '1-已创建 2-已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_seckill (user_id, seckill_product_id),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS seckill_stock_flow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seckill_product_id BIGINT NOT NULL,
    delta INT NOT NULL COMMENT '变化量(-1表示扣减)',
    source VARCHAR(32) NOT NULL COMMENT 'REDIS_PRE_DECR, MYSQL_CONFIRM, COMPENSATE_ROLLBACK',
    order_no VARCHAR(32) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_seckill_product (seckill_product_id),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
