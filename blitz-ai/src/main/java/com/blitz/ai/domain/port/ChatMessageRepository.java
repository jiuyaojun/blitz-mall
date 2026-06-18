package com.blitz.ai.domain.port;

import com.blitz.ai.domain.model.ChatMessage;
import java.util.List;

public interface ChatMessageRepository {
    List<ChatMessage> findBySessionId(Long sessionId);
    ChatMessage save(ChatMessage message);
    List<ChatMessage> findRecentBySessionId(Long sessionId, int limit);
}
