package com.blitz.ai.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blitz.ai.domain.model.KbDocument;
import com.blitz.ai.domain.port.KbDocumentRepository;
import com.blitz.ai.infrastructure.mapper.KbDocumentMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class KbDocumentRepositoryImpl implements KbDocumentRepository {

    private final KbDocumentMapper mapper;

    public KbDocumentRepositoryImpl(KbDocumentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<KbDocument> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    @Override
    public Page<KbDocument> findAllPage(Page<KbDocument> page) {
        return mapper.selectPage(page, new LambdaQueryWrapper<KbDocument>()
                .orderByDesc(KbDocument::getCreateTime));
    }

    @Override
    public List<KbDocument> findByStatus(String status) {
        return mapper.selectList(new LambdaQueryWrapper<KbDocument>()
                .eq(KbDocument::getStatus, status));
    }

    @Override
    public KbDocument save(KbDocument doc) {
        if (doc.getId() == null) {
            mapper.insert(doc);
        } else {
            mapper.updateById(doc);
        }
        return doc;
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }
}
