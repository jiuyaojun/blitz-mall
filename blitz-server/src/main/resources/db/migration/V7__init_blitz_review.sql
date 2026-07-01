CREATE TABLE IF NOT EXISTS blitz_review (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id  BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    username    VARCHAR(64) DEFAULT '',
    rating      TINYINT NOT NULL DEFAULT 5,
    content     TEXT,
    images      TEXT COMMENT 'JSON array of image URLs',
    sku_name    VARCHAR(128) DEFAULT '',
    like_count  INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     INT DEFAULT 0,
    INDEX idx_product_id (product_id),
    INDEX idx_user_id (user_id),
    INDEX idx_rating (rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';
