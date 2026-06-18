package com.blitz.product.domain.model;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Data
@TableName("sku")
public class Sku {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private String name;
    private String spec;  // JSON: {"颜色":"黑色","尺寸":"42"}
    private BigDecimal price;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    // Behavior methods
    @SuppressWarnings("unchecked")
    public Map<String, String> getSpecMap() {
        if (spec == null || spec.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return JSONUtil.toBean(spec, Map.class);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
