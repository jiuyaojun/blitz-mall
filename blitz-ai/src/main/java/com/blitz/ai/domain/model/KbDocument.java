package com.blitz.ai.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("kb_document")
public class KbDocument {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;       // LONGTEXT
    private String fileType;      // TXT, PDF, MD
    private String status;        // PENDING → VECTORIZING → VECTORIZED / FAILED
    private Integer chunkCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    // Status constants
    public static final String PENDING = "PENDING";
    public static final String VECTORIZING = "VECTORIZING";
    public static final String VECTORIZED = "VECTORIZED";
    public static final String FAILED = "FAILED";

    public void markVectorizing() { this.status = VECTORIZING; }
    public void markVectorized(int chunkCount) { this.status = VECTORIZED; this.chunkCount = chunkCount; }
    public void markFailed() { this.status = FAILED; }
    public boolean isVectorized() { return VECTORIZED.equals(this.status); }
}
