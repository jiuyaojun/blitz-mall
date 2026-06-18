package com.blitz.ai.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blitz.ai.domain.model.ChatMessage;
import com.blitz.ai.domain.port.ChatMessageRepository;
import com.blitz.ai.infrastructure.mapper.ChatMessageMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChatMessageRepositoryImpl implements ChatMessageRepository {

    private final ChatMessageMapper mapper;

    public ChatMessageRepositoryImpl(ChatMessageMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ChatMessage> findBySessionId(Long sessionId) {
        return mapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId));
    }

    @Override
    public ChatMessage save(ChatMessage message) {
        if (message.getId() == null) {
            mapper.insert(message);
        } else {
            mapper.updateById(message);
        }
        return message;
    }

    @Override
    public List<ChatMessage> findRecentBySessionId(Long sessionId, int limit) {
        return mapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByDesc(ChatMessage::getCreatedAt)
                .last("LIMIT " + limit));
    }
}
