package com.blitz.seckill.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seckill_product")
public class SeckillProduct {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long productId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer seckillStock;    // 秒杀总库存
    private Integer limitPerUser;    // 每人限购数量（默认1）
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public boolean hasStock() {
        return seckillStock != null && seckillStock > 0;
    }

    /** 扣减秒杀库存（MySQL 乐观锁兜底） */
    public void deductStock(int qty) {
        if (this.seckillStock == null || this.seckillStock < qty) {
            throw new IllegalStateException("Insufficient seckill stock");
        }
        this.seckillStock -= qty;
    }

    /** 恢复秒杀库存（补偿操作） */
    public void restoreStock(int qty) {
        if (this.seckillStock == null) this.seckillStock = 0;
        this.seckillStock += qty;
    }
}
