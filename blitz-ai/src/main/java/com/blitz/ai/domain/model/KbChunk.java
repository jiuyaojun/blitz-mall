package com.blitz.ai.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("kb_chunk")
public class KbChunk {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long documentId;
    private String chunkText;     // TEXT
    private Integer chunkIndex;
    private String vectorId;      // pgvector 关联ID (UUID)
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public static KbChunk of(Long documentId, String text, int index, String vectorId) {
        KbChunk c = new KbChunk();
        c.documentId = documentId;
        c.chunkText = text;
        c.chunkIndex = index;
        c.vectorId = vectorId;
        return c;
    }
}
