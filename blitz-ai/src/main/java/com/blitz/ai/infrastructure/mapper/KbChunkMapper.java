package com.blitz.ai.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blitz.ai.domain.model.KbChunk;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KbChunkMapper extends BaseMapper<KbChunk> {
}
