package com.blitz.ai.infrastructure.rag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HybridRetrievalService {

    private final PgVectorStore pgVectorStore;
    private final EmbeddingService embeddingService;
    private final KeywordSearchService keywordSearchService;
    private final RrfFusionService rrfFusionService;

    private static final int VECTOR_TOP_K = 10;
    private static final int KEYWORD_TOP_K = 10;
    private static final int FINAL_TOP_K = 5;

    public List<String> retrieve(String query) {
        List<Document> vectorDocs = pgVectorStore.similaritySearch(
            SearchRequest.builder().query(query).topK(VECTOR_TOP_K).build());

        List<RrfFusionService.RetrievalHit> vectorHits = vectorDocs.stream()
            .map(d -> new RrfFusionService.RetrievalHit(
                d.getId(), d.getScore() != null ? d.getScore() : 1.0, d.getText()))
            .toList();

        List<Map<String, Object>> keywordRows = keywordSearchService.search(query, KEYWORD_TOP_K);

        List<RrfFusionService.RetrievalHit> keywordHits = keywordRows.stream()
            .map(row -> new RrfFusionService.RetrievalHit(
                (String) row.get("id"),
                ((Number) row.getOrDefault("score", 0.0)).doubleValue(),
                (String) row.getOrDefault("content", "")))
            .toList();

        List<String> fusedIds = rrfFusionService.fuse(vectorHits, keywordHits, FINAL_TOP_K);

        List<String> results = new ArrayList<>();
        for (String id : fusedIds) {
            vectorDocs.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .ifPresent(d -> results.add(d.getText()));
            keywordHits.stream()
                .filter(h -> h.id().equals(id))
                .findFirst()
                .ifPresent(h -> {
                    if (!results.contains(h.content())) results.add(h.content());
                });
        }

        log.info("Hybrid retrieval: query='{}', vectorResults={}, keywordResults={}, fusedResults={}",
            query, vectorDocs.size(), keywordRows.size(), results.size());
        return results;
    }
}
