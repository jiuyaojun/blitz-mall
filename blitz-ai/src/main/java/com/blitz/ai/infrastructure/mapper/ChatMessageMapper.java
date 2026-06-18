package com.blitz.ai.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blitz.ai.domain.model.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
