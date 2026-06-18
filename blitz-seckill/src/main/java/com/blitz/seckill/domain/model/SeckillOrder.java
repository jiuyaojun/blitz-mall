package com.blitz.seckill.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("seckill_order")
public class SeckillOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long seckillProductId;
    private Integer status; // 1-已创建 2-已取消
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public void cancel() { this.status = 2; }
    public boolean isCreated() { return status != null && status == 1; }
}
