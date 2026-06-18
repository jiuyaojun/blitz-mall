package com.blitz.product.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sku_stock")
public class SkuStock {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long skuId;
    private Integer stock;      // available stock
    private Integer lockStock;  // locked stock (seckill pre-deduction)
    @Version
    private Integer version;    // optimistic lock
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // Behavior methods
    public void deduct(int qty) {
        if (this.stock == null || this.stock < qty) {
            throw new IllegalStateException("Insufficient stock: have " + stock + ", need " + qty);
        }
        this.stock -= qty;
    }

    public void lockStock(int qty) {
        if (this.stock == null || this.stock < qty) {
            throw new IllegalStateException("Insufficient stock to lock: have " + stock + ", need " + qty);
        }
        this.stock -= qty;
        if (this.lockStock == null) this.lockStock = 0;
        this.lockStock += qty;
    }

    public void unlockStock(int qty) {
        if (this.lockStock == null || this.lockStock < qty) {
            throw new IllegalStateException("Locked stock insufficient to unlock: locked " + lockStock + ", need " + qty);
        }
        this.lockStock -= qty;
        this.stock += qty;
    }
}
