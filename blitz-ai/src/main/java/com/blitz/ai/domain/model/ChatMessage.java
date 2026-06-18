package com.blitz.ai.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private String role;          // USER / AI
    private String content;       // TEXT
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public static final String ROLE_USER = "USER";
    public static final String ROLE_AI = "AI";

    public static ChatMessage of(Long sessionId, String role, String content) {
        ChatMessage m = new ChatMessage();
        m.sessionId = sessionId;
        m.role = role;
        m.content = content;
        return m;
    }
}
