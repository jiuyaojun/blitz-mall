package com.blitz.ai.domain.port;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blitz.ai.domain.model.ChatSession;
import java.util.Optional;

public interface ChatSessionRepository {
    Optional<ChatSession> findById(Long id);
    Page<ChatSession> findByUserId(Long userId, Page<ChatSession> page);
    ChatSession save(ChatSession session);
    void deleteById(Long id);
}
