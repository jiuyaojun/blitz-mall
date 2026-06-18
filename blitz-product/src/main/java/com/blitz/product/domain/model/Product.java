package com.blitz.product.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long categoryId;
    private String brand;
    private String description;
    private String mainImage;
    private String images;  // JSON array
    private Integer status; // 1-on sale 0-off shelf
    private Integer saleCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    // Behavior methods
    public void publish() { this.status = 1; }
    public void withdraw() { this.status = 0; }
    public boolean isPublished() { return status != null && status == 1; }
    public void incrementSaleCount(int delta) {
        if (this.saleCount == null) this.saleCount = 0;
        this.saleCount += delta;
    }
}
