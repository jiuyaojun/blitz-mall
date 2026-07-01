package com.blitz.review;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.blitz.data.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("blitz_review")
public class Review extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long userId;
    private String username;
    private Integer rating;      // 1-5 stars
    private String content;      // review text
    private String reply;        // merchant reply
    private String images;       // JSON array of image URLs/base64
    private String skuName;      // purchased SKU name
    private Integer likeCount;   // helpful count
}
