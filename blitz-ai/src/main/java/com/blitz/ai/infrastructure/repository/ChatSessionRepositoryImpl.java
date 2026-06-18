package com.blitz.ai.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blitz.ai.domain.model.ChatSession;
import com.blitz.ai.domain.port.ChatSessionRepository;
import com.blitz.ai.infrastructure.mapper.ChatSessionMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ChatSessionRepositoryImpl implements ChatSessionRepository {

    private final ChatSessionMapper mapper;

    public ChatSessionRepositoryImpl(ChatSessionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<ChatSession> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    @Override
    public Page<ChatSession> findByUserId(Long userId, Page<ChatSession> page) {
        return mapper.selectPage(page, new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .orderByDesc(ChatSession::getUpdateTime));
    }

    @Override
    public ChatSession save(ChatSession session) {
        if (session.getId() == null) {
            mapper.insert(session);
        } else {
            mapper.updateById(session);
        }
        return session;
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }
}
