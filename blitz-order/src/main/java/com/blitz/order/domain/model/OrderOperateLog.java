package com.blitz.order.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_operate_log")
public class OrderOperateLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String operateType;
    private String operator;
    private String note;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public static OrderOperateLog of(Long orderId, String operateType, String operator, String note) {
        OrderOperateLog log = new OrderOperateLog();
        log.orderId = orderId;
        log.operateType = operateType;
        log.operator = operator;
        log.note = note;
        return log;
    }
}
