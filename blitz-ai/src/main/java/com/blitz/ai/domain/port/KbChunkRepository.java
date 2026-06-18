package com.blitz.ai.domain.port;

import com.blitz.ai.domain.model.KbChunk;
import java.util.List;

public interface KbChunkRepository {
    List<KbChunk> findByDocumentId(Long documentId);
    void saveBatch(List<KbChunk> chunks);
    void deleteByDocumentId(Long documentId);
}
