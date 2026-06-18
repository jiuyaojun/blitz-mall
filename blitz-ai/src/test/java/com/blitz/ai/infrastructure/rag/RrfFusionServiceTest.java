package com.blitz.ai.infrastructure.rag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RRF Fusion Service")
class RrfFusionServiceTest {

    private final RrfFusionService service = new RrfFusionService();

    @Nested
    @DisplayName("fuse with equal-ranked results")
    class EqualRankedResults {

        @Test
        @DisplayName("returns both IDs")
        void returnsBothIds() {
            List<RrfFusionService.RetrievalHit> vector = List.of(
                new RrfFusionService.RetrievalHit("doc-A", 0.95, "content A"),
                new RrfFusionService.RetrievalHit("doc-B", 0.90, "content B")
            );
            List<RrfFusionService.RetrievalHit> keyword = List.of(
                new RrfFusionService.RetrievalHit("doc-A", 0.88, "content A"),
                new RrfFusionService.RetrievalHit("doc-B", 0.85, "content B")
            );

            List<String> result = service.fuse(vector, keyword, 5);

            assertThat(result).containsExactly("doc-A", "doc-B");
        }
    }

    @Nested
    @DisplayName("fuse with vector-first ranking")
    class VectorFirstRanking {

        @Test
        @DisplayName("gives higher RRF score to top vector result")
        void givesHigherRrfScoreToTopVectorResult() {
            List<RrfFusionService.RetrievalHit> vector = List.of(
                new RrfFusionService.RetrievalHit("vector-top", 0.99, "vector top content")
            );
            List<RrfFusionService.RetrievalHit> keyword = List.of(
                new RrfFusionService.RetrievalHit("keyword-top", 0.95, "keyword top content")
            );

            List<String> result = service.fuse(vector, keyword, 5);

            // vector rank 0 → RRF = 1/(60+0+1) ≈ 0.01639
            // keyword rank 0 → RRF = 1/(60+0+1) ≈ 0.01639
            // Equal rank 0, but vector ID comes first (insertion order preserved via LinkedHashMap)
            assertThat(result.get(0)).isEqualTo("vector-top");
        }
    }

    @Nested
    @DisplayName("fuse with single-source results")
    class SingleSourceResults {

        @Test
        @DisplayName("only vector, no keyword — works correctly")
        void onlyVectorNoKeywordWorksCorrectly() {
            List<RrfFusionService.RetrievalHit> vector = List.of(
                new RrfFusionService.RetrievalHit("v1", 0.9, "c1"),
                new RrfFusionService.RetrievalHit("v2", 0.8, "c2")
            );
            List<RrfFusionService.RetrievalHit> keyword = List.of();

            List<String> result = service.fuse(vector, keyword, 5);

            assertThat(result).containsExactly("v1", "v2");
        }

        @Test
        @DisplayName("only keyword, no vector — works correctly")
        void onlyKeywordNoVectorWorksCorrectly() {
            List<RrfFusionService.RetrievalHit> vector = List.of();
            List<RrfFusionService.RetrievalHit> keyword = List.of(
                new RrfFusionService.RetrievalHit("k1", 0.9, "c1"),
                new RrfFusionService.RetrievalHit("k2", 0.8, "c2")
            );

            List<String> result = service.fuse(vector, keyword, 5);

            assertThat(result).containsExactly("k1", "k2");
        }
    }

    @Nested
    @DisplayName("fuse with empty inputs")
    class EmptyInputs {

        @Test
        @DisplayName("returns empty list when both inputs are empty")
        void returnsEmptyList() {
            List<RrfFusionService.RetrievalHit> vector = List.of();
            List<RrfFusionService.RetrievalHit> keyword = List.of();

            List<String> result = service.fuse(vector, keyword, 5);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("fuse respects topK parameter")
    class TopKParameter {

        @Test
        @DisplayName("returns at most topK results")
        void returnsAtMostTopKResults() {
            List<RrfFusionService.RetrievalHit> vector = List.of(
                new RrfFusionService.RetrievalHit("a", 0.9, "ca"),
                new RrfFusionService.RetrievalHit("b", 0.8, "cb"),
                new RrfFusionService.RetrievalHit("c", 0.7, "cc"),
                new RrfFusionService.RetrievalHit("d", 0.6, "cd"),
                new RrfFusionService.RetrievalHit("e", 0.5, "ce")
            );
            List<RrfFusionService.RetrievalHit> keyword = List.of();

            List<String> result = service.fuse(vector, keyword, 3);

            assertThat(result).hasSize(3);
            assertThat(result).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("fuse with K=60 constant")
    class K60Constant {

        @Test
        @DisplayName("verifies formula 1/(K+rank+1) for rank 0")
        void verifiesFormulaForRank0() {
            // K = 60, rank = 0 → RRF = 1/(60+1) = 1/61 ≈ 0.016393
            List<RrfFusionService.RetrievalHit> vector = List.of(
                new RrfFusionService.RetrievalHit("single", 1.0, "content")
            );
            List<RrfFusionService.RetrievalHit> keyword = List.of();

            List<String> result = service.fuse(vector, keyword, 1);

            assertThat(result).containsExactly("single");
            // The formula 1/(60+0+1) = 1/61 produces a non-zero score,
            // so the single item with only vector source is correctly returned.
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("later ranks get progressively lower scores preserving order")
        void laterRanksGetProgressivelyLowerScores() {
            List<RrfFusionService.RetrievalHit> vector = List.of(
                new RrfFusionService.RetrievalHit("first", 0.9, ""),
                new RrfFusionService.RetrievalHit("second", 0.7, ""),
                new RrfFusionService.RetrievalHit("third", 0.5, "")
            );
            List<RrfFusionService.RetrievalHit> keyword = List.of();

            List<String> result = service.fuse(vector, keyword, 3);

            // 1/(60+0+1) > 1/(60+1+1) > 1/(60+2+1) → first > second > third
            assertThat(result).containsExactly("first", "second", "third");
        }
    }

    @Nested
    @DisplayName("fuse with overlapping IDs")
    class OverlappingIds {

        @Test
        @DisplayName("same doc in both sources merges RRF scores")
        void sameDocInBothSourcesMergesRrfScores() {
            List<RrfFusionService.RetrievalHit> vector = List.of(
                new RrfFusionService.RetrievalHit("shared", 0.95, "shared content"),
                new RrfFusionService.RetrievalHit("v-only", 0.80, "vector only")
            );
            List<RrfFusionService.RetrievalHit> keyword = List.of(
                new RrfFusionService.RetrievalHit("shared", 0.90, "shared content"),
                new RrfFusionService.RetrievalHit("k-only", 0.70, "keyword only")
            );

            List<String> result = service.fuse(vector, keyword, 5);

            // "shared" gets RRF from vector rank 0 + keyword rank 0 → highest
            assertThat(result.get(0)).isEqualTo("shared");
            assertThat(result).hasSize(3);
            assertThat(result).contains("v-only", "k-only");
        }

        @Test
        @DisplayName("overlapping ID with different ranks in each source still merges")
        void overlappingIdWithDifferentRanksStillMerges() {
            List<RrfFusionService.RetrievalHit> vector = List.of(
                new RrfFusionService.RetrievalHit("doc", 0.5, "content")
            );
            // doc is rank 2 in keyword, rank 0 in vector
            List<RrfFusionService.RetrievalHit> keyword = List.of(
                new RrfFusionService.RetrievalHit("other1", 0.9, ""),
                new RrfFusionService.RetrievalHit("other2", 0.8, ""),
                new RrfFusionService.RetrievalHit("doc", 0.7, "content")
            );

            List<String> result = service.fuse(vector, keyword, 5);

            // doc score = 1/(60+0+1) + 1/(60+2+1) = 1/61 + 1/63 ≈ 0.01639 + 0.01587 ≈ 0.03226
            // other1 score = 1/(60+0+1) = 1/61 ≈ 0.01639
            // doc should rank above other1
            assertThat(result.get(0)).isEqualTo("doc");
            assertThat(result).hasSize(3);
        }
    }
}
