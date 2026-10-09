package com.blitz.ai.infrastructure.rag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("!minimal")
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public List<Document> embed(List<String> texts, Map<String, Object> metadata) {
        List<float[]> embeddings = embeddingModel.embed(texts);
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            Document doc = new Document(texts.get(i), new HashMap<>(metadata));
            doc.getMetadata().put("chunk_index", i);
            documents.add(doc);
        }
        return documents;
    }

    public float[] embedQuery(String query) {
        return embeddingModel.embed(query);
    }

    public int dimension() {
        return embeddingModel.dimensions();
    }
}
