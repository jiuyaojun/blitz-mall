package com.blitz.ai.domain.port;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blitz.ai.domain.model.KbDocument;
import java.util.List;
import java.util.Optional;

public interface KbDocumentRepository {
    Optional<KbDocument> findById(Long id);
    Page<KbDocument> findAllPage(Page<KbDocument> page);
    List<KbDocument> findByStatus(String status);
    KbDocument save(KbDocument doc);
    void deleteById(Long id);
}
