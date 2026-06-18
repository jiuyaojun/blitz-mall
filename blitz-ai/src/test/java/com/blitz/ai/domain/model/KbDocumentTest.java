package com.blitz.ai.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("KbDocument entity")
class KbDocumentTest {

    @Nested
    @DisplayName("status constants and transitions")
    class StatusTransitions {

        @Test
        @DisplayName("PENDING constant value is correct")
        void pendingConstantValueIsCorrect() {
            assertThat(KbDocument.PENDING).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("VECTORIZING constant value is correct")
        void vectorizingConstantValueIsCorrect() {
            assertThat(KbDocument.VECTORIZING).isEqualTo("VECTORIZING");
        }

        @Test
        @DisplayName("VECTORIZED constant value is correct")
        void vectorizedConstantValueIsCorrect() {
            assertThat(KbDocument.VECTORIZED).isEqualTo("VECTORIZED");
        }

        @Test
        @DisplayName("FAILED constant value is correct")
        void failedConstantValueIsCorrect() {
            assertThat(KbDocument.FAILED).isEqualTo("FAILED");
        }

        @Test
        @DisplayName("markVectorizing changes status to VECTORIZING")
        void markVectorizingChangesStatus() {
            KbDocument doc = new KbDocument();
            doc.markVectorizing();

            assertThat(doc.getStatus()).isEqualTo(KbDocument.VECTORIZING);
        }

        @Test
        @DisplayName("markVectorized changes status + sets chunkCount")
        void markVectorizedChangesStatusAndSetsChunkCount() {
            KbDocument doc = new KbDocument();
            doc.markVectorized(42);

            assertThat(doc.getStatus()).isEqualTo(KbDocument.VECTORIZED);
            assertThat(doc.getChunkCount()).isEqualTo(42);
        }

        @Test
        @DisplayName("markFailed changes status to FAILED")
        void markFailedChangesStatus() {
            KbDocument doc = new KbDocument();
            doc.markFailed();

            assertThat(doc.getStatus()).isEqualTo(KbDocument.FAILED);
        }
    }

    @Nested
    @DisplayName("isVectorized checks")
    class IsVectorizedChecks {

        @Test
        @DisplayName("returns true only for VECTORIZED status")
        void returnsTrueOnlyForVectorizedStatus() {
            KbDocument doc = new KbDocument();

            doc.setStatus(KbDocument.VECTORIZED);
            assertThat(doc.isVectorized()).isTrue();
        }

        @Test
        @DisplayName("returns false for PENDING status")
        void returnsFalseForPendingStatus() {
            KbDocument doc = new KbDocument();
            doc.setStatus(KbDocument.PENDING);

            assertThat(doc.isVectorized()).isFalse();
        }

        @Test
        @DisplayName("returns false for VECTORIZING status")
        void returnsFalseForVectorizingStatus() {
            KbDocument doc = new KbDocument();
            doc.setStatus(KbDocument.VECTORIZING);

            assertThat(doc.isVectorized()).isFalse();
        }

        @Test
        @DisplayName("returns false for FAILED status")
        void returnsFalseForFailedStatus() {
            KbDocument doc = new KbDocument();
            doc.setStatus(KbDocument.FAILED);

            assertThat(doc.isVectorized()).isFalse();
        }

        @Test
        @DisplayName("returns false for null status")
        void returnsFalseForNullStatus() {
            KbDocument doc = new KbDocument();

            // default status is null — no @Builder.Default or field initializer
            assertThat(doc.getStatus()).isNull();
            assertThat(doc.isVectorized()).isFalse();
        }

        @Test
        @DisplayName("after full lifecycle: markVectorizing → markVectorized, isVectorized returns true")
        void fullLifecycleMarkVectorizedReturnsTrue() {
            KbDocument doc = new KbDocument();
            doc.markVectorizing();
            assertThat(doc.isVectorized()).isFalse();

            doc.markVectorized(10);
            assertThat(doc.isVectorized()).isTrue();
        }

        @Test
        @DisplayName("after markFailed, isVectorized returns false")
        void afterMarkFailedReturnsFalse() {
            KbDocument doc = new KbDocument();
            doc.markVectorizing();
            doc.markVectorized(5);
            doc.markFailed();

            assertThat(doc.isVectorized()).isFalse();
        }
    }

    @Nested
    @DisplayName("field access")
    class FieldAccess {

        @Test
        @DisplayName("can set and get id")
        void canSetAndGetId() {
            KbDocument doc = new KbDocument();
            doc.setId(100L);

            assertThat(doc.getId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("can set and get title")
        void canSetAndGetTitle() {
            KbDocument doc = new KbDocument();
            doc.setTitle("测试文档");

            assertThat(doc.getTitle()).isEqualTo("测试文档");
        }

        @Test
        @DisplayName("can set and get content")
        void canSetAndGetContent() {
            KbDocument doc = new KbDocument();
            doc.setContent("Hello World");

            assertThat(doc.getContent()).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("can set and get fileType")
        void canSetAndGetFileType() {
            KbDocument doc = new KbDocument();
            doc.setFileType("PDF");

            assertThat(doc.getFileType()).isEqualTo("PDF");
        }
    }
}
