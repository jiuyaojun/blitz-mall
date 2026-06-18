package com.blitz.ai.infrastructure.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Streaming chat service backed by Spring AI ChatClient.
 * Supports RAG (Retrieval-Augmented Generation) with prompt template
 * and simple direct chat, both returning Flux<String> for SSE streaming.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingChatService {

    private final ChatClient chatClient;

    private String ragPromptTemplate;

    /** Load RAG prompt template from classpath (cached). */
    public String loadRagTemplate() throws IOException {
        if (ragPromptTemplate == null) {
            ClassPathResource resource = new ClassPathResource("prompts/rag-system.st");
            ragPromptTemplate = resource.getContentAsString(StandardCharsets.UTF_8);
        }
        return ragPromptTemplate;
    }

    /**
     * RAG streaming chat — LLM generation with knowledge-base context.
     *
     * @param question user question
     * @param context  retrieved knowledge-base content (joined by separator)
     * @param history  formatted conversation history string
     * @return Flux<String> streaming output chunks
     */
    public Flux<String> streamRagChat(String question, String context, String history) {
        try {
            String template = loadRagTemplate();
            PromptTemplate promptTemplate = new PromptTemplate(template);
            var prompt = promptTemplate.create(Map.of(
                "context", context != null && !context.isEmpty() ? context : "暂无相关知识库内容",
                "history", history != null && !history.isEmpty() ? history : "无历史对话",
                "question", question
            ));

            return chatClient.prompt(prompt).stream().content();
        } catch (IOException e) {
            log.error("Failed to load RAG prompt template", e);
            return Flux.just("抱歉，系统暂不可用，请稍后再试。");
        }
    }

    /**
     * Simple direct chat without knowledge-base retrieval.
     *
     * @param message user message
     * @return Flux<String> streaming output chunks
     */
    public Flux<String> streamSimpleChat(String message) {
        return chatClient.prompt().user(message).stream().content();
    }
}
