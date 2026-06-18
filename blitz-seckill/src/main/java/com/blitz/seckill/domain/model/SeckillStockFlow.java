package com.blitz.seckill.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("seckill_stock_flow")
public class SeckillStockFlow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long seckillProductId;
    private Integer delta;       // 变化量(-1扣减, +1恢复)
    private String source;       // REDIS_PRE_DECR / MYSQL_CONFIRM / COMPENSATE_ROLLBACK
    private String orderNo;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public static final String REDIS_PRE_DECR = "REDIS_PRE_DECR";
    public static final String MYSQL_CONFIRM = "MYSQL_CONFIRM";
    public static final String COMPENSATE_ROLLBACK = "COMPENSATE_ROLLBACK";

    public static SeckillStockFlow of(Long spId, int delta, String source, String orderNo) {
        SeckillStockFlow f = new SeckillStockFlow();
        f.seckillProductId = spId;
        f.delta = delta;
        f.source = source;
        f.orderNo = orderNo;
        return f;
    }
}
