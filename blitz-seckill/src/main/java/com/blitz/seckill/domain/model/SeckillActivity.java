package com.blitz.seckill.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("seckill_activity")
public class SeckillActivity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status; // 0-未开始 1-进行中 2-已结束
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public boolean isInProgress() {
        LocalDateTime now = LocalDateTime.now();
        return status != null && status == 1
            && startTime != null && now.isAfter(startTime)
            && endTime != null && now.isBefore(endTime);
    }

    public void start() { this.status = 1; }
    public void end() { this.status = 2; }
    public boolean isPending() { return status != null && status == 0; }
    public boolean isEnded() { return status != null && status == 2; }
}
