package com.blitz.ai.application.service;

import com.blitz.ai.domain.model.KbChunk;
import com.blitz.ai.domain.model.KbDocument;
import com.blitz.ai.domain.port.KbChunkRepository;
import com.blitz.ai.domain.port.KbDocumentRepository;
import com.blitz.ai.infrastructure.rag.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Profile("!minimal")
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final KbDocumentRepository documentRepository;
    private final KbChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;
    private final PgVectorStore pgVectorStore;

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 50;

    /** 创建文档并启动向量化管道 */
    @Transactional
    public KbDocument ingestDocument(String title, String content, String fileType) {
        // 1. 保存文档
        KbDocument doc = new KbDocument();
        doc.setTitle(title);
        doc.setContent(content);
        doc.setFileType(fileType != null ? fileType : "TXT");
        doc.setStatus(KbDocument.PENDING);
        documentRepository.save(doc);

        // 2. 标记为向量化中
        doc.markVectorizing();
        documentRepository.save(doc);

        try {
            // 3. 文本切片
            List<String> chunks = splitText(content, CHUNK_SIZE, CHUNK_OVERLAP);

            // 4. Embedding + 存储到 pgvector
            List<KbChunk> kbChunks = new ArrayList<>();
            Map<String, Object> metadata = Map.of("document_id", doc.getId().toString(), "title", title);

            List<Document> embeddedDocs = embeddingService.embed(chunks, metadata);
            pgVectorStore.add(embeddedDocs);

            // 5. 保存 chunk 记录到 MySQL（vectorId 从 pgvector 返回的 Document.id 获取）
            for (int i = 0; i < embeddedDocs.size(); i++) {
                Document embeddedDoc = embeddedDocs.get(i);
                KbChunk chunk = KbChunk.of(doc.getId(), chunks.get(i), i, embeddedDoc.getId());
                kbChunks.add(chunk);
            }
            chunkRepository.saveBatch(kbChunks);

            // 6. 标记完成
            doc.markVectorized(chunks.size());
            documentRepository.save(doc);

            log.info("Document ingested: id={}, chunks={}", doc.getId(), chunks.size());
        } catch (Exception e) {
            log.error("Document vectorization failed: id={}", doc.getId(), e);
            doc.markFailed();
            documentRepository.save(doc);
        }

        return doc;
    }

    /** 删除文档及其向量 */
    @Transactional
    public void deleteDocument(Long docId) {
        List<KbChunk> chunks = chunkRepository.findByDocumentId(docId);
        List<String> vectorIds = chunks.stream()
            .map(KbChunk::getVectorId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        // 删除 pgvector 中的向量
        if (!vectorIds.isEmpty()) {
            pgVectorStore.delete(vectorIds);
        }

        // 删除 MySQL 记录
        chunkRepository.deleteByDocumentId(docId);
        documentRepository.deleteById(docId);
        log.info("Document deleted: id={}, vectorsDeleted={}", docId, vectorIds.size());
    }

    /** 简单文本切片（按字符数 + 重叠，尽量在句子边界断开） */
    public List<String> splitText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) return chunks;

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            // Try to break at a sentence boundary
            if (end < text.length()) {
                int lastPeriod = text.lastIndexOf('。', end);
                int lastNewline = text.lastIndexOf('\n', end);
                int breakPoint = Math.max(lastPeriod, lastNewline);
                if (breakPoint > start + chunkSize / 2) {
                    end = breakPoint + 1;
                }
            }
            chunks.add(text.substring(start, end).trim());
            start = end - overlap;
            if (start < 0) start = 0;
        }
        return chunks;
    }
}
