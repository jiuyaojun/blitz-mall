CREATE TABLE IF NOT EXISTS `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号(雪花ID)',
    user_id BIGINT NOT NULL,
    status INT DEFAULT 0 COMMENT '0-待支付 1-已支付 2-已发货 3-已收货 4-已取消 5-已退款',
    total_amount DECIMAL(10,2) NOT NULL,
    freight_amount DECIMAL(10,2) DEFAULT 0,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    payment_method INT DEFAULT NULL COMMENT '1-支付宝 2-微信',
    payment_time DATETIME DEFAULT NULL,
    delivery_time DATETIME DEFAULT NULL,
    receive_time DATETIME DEFAULT NULL,
    cancel_time DATETIME DEFAULT NULL,
    receiver_name VARCHAR(32) DEFAULT NULL,
    receiver_phone VARCHAR(16) DEFAULT NULL,
    receiver_address VARCHAR(255) DEFAULT NULL,
    note VARCHAR(512) DEFAULT NULL,
    version INT DEFAULT 0 COMMENT '乐观锁',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status),
    INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    sku_name VARCHAR(255) NOT NULL,
    sku_spec VARCHAR(512) DEFAULT NULL,
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    event_type VARCHAR(32) NOT NULL COMMENT 'ORDER_CREATED,ORDER_PAID,ORDER_SHIPPED,ORDER_CANCELLED',
    event_data JSON DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_event_type (event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_operate_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    operate_type VARCHAR(32) NOT NULL,
    operator VARCHAR(64) DEFAULT NULL,
    note VARCHAR(512) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
