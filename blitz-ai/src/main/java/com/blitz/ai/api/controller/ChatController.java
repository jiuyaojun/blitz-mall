package com.blitz.ai.api.controller;

import com.blitz.ai.api.dto.ChatRequest;
import com.blitz.ai.application.service.ChatApplicationService;
import com.blitz.ai.domain.model.ChatSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("!minimal")
public class ChatController {

    private final ChatApplicationService chatApplicationService;

    /** RAG 流式对话（SSE）——⑨ SSE 输出 */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@Valid @RequestBody ChatRequest req) {
        Long userId = getCurrentUserId();
        SseEmitter emitter = new SseEmitter(300_000L); // 5 min timeout

        // 获取或创建会话
        Long sessionId = req.sessionId();
        if (sessionId == null) {
            ChatSession session = chatApplicationService.createSession(userId, null);
            sessionId = session.getId();
        }

        final Long finalSessionId = sessionId;
        Flux<String> stream = chatApplicationService.chat(finalSessionId, userId, req.message());

        stream.subscribe(
            chunk -> {
                try {
                    emitter.send(SseEmitter.event()
                        .name("message")
                        .data(chunk));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            },
            error -> emitter.completeWithError(error),
            () -> {
                try {
                    emitter.send(SseEmitter.event()
                        .name("done")
                        .data(Map.of("sessionId", finalSessionId)));
                    emitter.complete();
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }
        );

        return emitter;
    }

    /** 创建新会话 */
    @PostMapping("/session")
    public Map<String, Long> createSession(@RequestParam(defaultValue = "新对话") String title) {
        Long userId = getCurrentUserId();
        ChatSession session = chatApplicationService.createSession(userId, title);
        return Map.of("sessionId", session.getId());
    }

    /** 获取会话消息 */
    @GetMapping("/session/{sessionId}/messages")
    public Object getMessages(@PathVariable Long sessionId) {
        return chatApplicationService.getMessages(sessionId);
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ((Number) principal).longValue();
    }
}
