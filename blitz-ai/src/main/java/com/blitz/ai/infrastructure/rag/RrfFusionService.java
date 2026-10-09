package com.blitz.ai.infrastructure.rag;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!minimal")
public class RrfFusionService {

    private static final double K = 60.0;

    public List<String> fuse(
            List<RetrievalHit> vectorResults,
            List<RetrievalHit> keywordResults,
            int topK) {

        Map<String, Double> rrfScores = new LinkedHashMap<>();

        for (int i = 0; i < vectorResults.size(); i++) {
            RetrievalHit hit = vectorResults.get(i);
            double rrf = 1.0 / (K + i + 1);
            rrfScores.merge(hit.id(), rrf, Double::sum);
        }

        for (int i = 0; i < keywordResults.size(); i++) {
            RetrievalHit hit = keywordResults.get(i);
            double rrf = 1.0 / (K + i + 1);
            rrfScores.merge(hit.id(), rrf, Double::sum);
        }

        return rrfScores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(topK)
            .map(Map.Entry::getKey)
            .toList();
    }

    public record RetrievalHit(String id, double score, String content) {}
}
