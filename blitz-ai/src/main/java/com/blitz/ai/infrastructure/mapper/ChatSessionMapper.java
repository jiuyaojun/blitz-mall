package com.blitz.ai.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blitz.ai.domain.model.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
