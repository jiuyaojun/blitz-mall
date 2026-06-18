package com.blitz.ai.application.service;

import com.blitz.ai.domain.model.ChatMessage;
import com.blitz.ai.domain.model.ChatSession;
import com.blitz.ai.domain.port.ChatMessageRepository;
import com.blitz.ai.domain.port.ChatSessionRepository;
import com.blitz.ai.infrastructure.llm.StreamingChatService;
import com.blitz.ai.infrastructure.rag.HybridRetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatApplicationService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final HybridRetrievalService retrievalService;
    private final StreamingChatService streamingChatService;

    private static final int HISTORY_LIMIT = 10;

    /** 创建新会话 */
    @Transactional
    public ChatSession createSession(Long userId, String title) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(title != null ? title : "新对话");
        sessionRepository.save(session);
        return session;
    }

    /** RAG 流式对话（完整 10 步链路） */
    @Transactional
    public Flux<String> chat(Long sessionId, Long userId, String question) {
        // ① 保存用户消息
        ChatMessage userMsg = ChatMessage.of(sessionId, ChatMessage.ROLE_USER, question);
        messageRepository.save(userMsg);

        // ②-⑥ 混合检索（向量 + 关键词 → RRF 融合）
        List<String> retrieved = retrievalService.retrieve(question);
        String context = String.join("\n\n---\n\n", retrieved);

        // ③ 获取对话历史
        List<ChatMessage> history = messageRepository.findRecentBySessionId(sessionId, HISTORY_LIMIT);
        String historyStr = history.stream()
            .map(m -> m.getRole() + ": " + m.getContent())
            .collect(Collectors.joining("\n"));

        // ④-⑨ RAG 流式生成（LLM streaming → SSE）
        StringBuilder fullResponse = new StringBuilder();
        return streamingChatService.streamRagChat(question, context, historyStr)
            .doOnNext(fullResponse::append)
            .doOnComplete(() -> {
                // ⑩ 保存 AI 回复
                ChatMessage aiMsg = ChatMessage.of(sessionId, ChatMessage.ROLE_AI, fullResponse.toString());
                messageRepository.save(aiMsg);

                // 首次对话时更新标题
                ChatSession session = sessionRepository.findById(sessionId).orElse(null);
                if (session != null && "新对话".equals(session.getTitle())) {
                    String title = question.length() > 30 ? question.substring(0, 30) + "..." : question;
                    session.updateTitle(title);
                    sessionRepository.save(session);
                }
            });
    }

    /** 获取会话消息列表 */
    public List<ChatMessage> getMessages(Long sessionId) {
        return messageRepository.findBySessionId(sessionId);
    }
}
