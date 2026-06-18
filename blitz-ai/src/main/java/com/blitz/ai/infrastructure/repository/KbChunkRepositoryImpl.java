package com.blitz.ai.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blitz.ai.domain.model.KbChunk;
import com.blitz.ai.domain.port.KbChunkRepository;
import com.blitz.ai.infrastructure.mapper.KbChunkMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class KbChunkRepositoryImpl implements KbChunkRepository {

    private final KbChunkMapper mapper;

    public KbChunkRepositoryImpl(KbChunkMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<KbChunk> findByDocumentId(Long documentId) {
        return mapper.selectList(new LambdaQueryWrapper<KbChunk>()
                .eq(KbChunk::getDocumentId, documentId));
    }

    @Override
    public void saveBatch(List<KbChunk> chunks) {
        for (KbChunk c : chunks) {
            if (c.getId() == null) {
                mapper.insert(c);
            } else {
                mapper.updateById(c);
            }
        }
    }

    @Override
    public void deleteByDocumentId(Long documentId) {
        mapper.delete(new LambdaQueryWrapper<KbChunk>()
                .eq(KbChunk::getDocumentId, documentId));
    }
}
