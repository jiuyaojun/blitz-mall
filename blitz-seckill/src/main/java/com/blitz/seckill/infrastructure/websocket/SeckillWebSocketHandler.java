package com.blitz.seckill.infrastructure.websocket;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SeckillWebSocketHandler extends TextWebSocketHandler {

    /** userId → WebSocketSession */
    private final ConcurrentHashMap<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserId(session);
        if (userId != null) {
            userSessions.put(userId, session);
            log.info("WebSocket connected: userId={}, sessionId={}", userId, session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserId(session);
        if (userId != null) {
            userSessions.remove(userId);
            log.info("WebSocket disconnected: userId={}", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 心跳处理
        if ("ping".equals(message.getPayload())) {
            try {
                session.sendMessage(new TextMessage("pong"));
            } catch (Exception ignored) {}
        }
    }

    /** 推送秒杀结果给指定用户 */
    public void pushSeckillResult(Long userId, Map<String, Object> result) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String json = JSONUtil.toJsonStr(result);
                session.sendMessage(new TextMessage(json));
                log.info("Seckill result pushed: userId={}, result={}", userId, json);
            } catch (Exception e) {
                log.error("Failed to push seckill result: userId={}", userId, e);
            }
        } else {
            log.warn("User not connected: userId={}", userId);
        }
    }

    /** 从 session attributes 获取 userId */
    private Long getUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        return userId != null ? ((Number) userId).longValue() : null;
    }
}
