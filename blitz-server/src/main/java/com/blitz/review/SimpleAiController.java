package com.blitz.review;

import com.blitz.common.result.Result;
import com.blitz.security.annotation.RequireRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@Profile("minimal")
public class SimpleAiController {

    @Value("${OPENAI_API_KEY}")
    private String apiKey;

    @Value("${OPENAI_BASE_URL:https://open.bigmodel.cn/api/paas/v4}")
    private String baseUrl;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RequireRole("USER")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody Map<String, Object> body) {
        getCurrentUserId(); // Require auth
        String message = (String) body.getOrDefault("message", "");
        SseEmitter emitter = new SseEmitter(300_000L);

        new Thread(() -> {
            try {
                String sysPrompt = "你是Blitz智能商城的AI客服助手。请用中文简洁、专业地回答用户问题。" +
                    "你可以帮助用户了解：如何下单、支付方式、退换货政策、物流查询、秒杀活动参与方式等。" +
                    "回答请控制在200字以内，使用友好亲切的语气。";

                List<Map<String, String>> messages = new ArrayList<>();
                messages.add(Map.of("role", "system", "content", sysPrompt));
                messages.add(Map.of("role", "user", "content", message));

                Map<String, Object> reqMap = Map.of(
                    "model", "glm-4-flash",
                    "messages", messages,
                    "temperature", 0.6,
                    "stream", true
                );
                String reqBody = objectMapper.writeValueAsString(reqMap);

                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(reqBody))
                    .build();

                HttpResponse<java.io.InputStream> resp = httpClient.send(req,
                    HttpResponse.BodyHandlers.ofInputStream());

                if (resp.statusCode() != 200) {
                    String errBody = new String(resp.body().readAllBytes());
                    if (errBody.length() > 200) errBody = errBody.substring(0, 200);
                    emitter.send(SseEmitter.event().name("message").data("AI服务异常: " + errBody));
                    emitter.complete();
                    return;
                }

                // Use BufferedReader for correct UTF-8 handling
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(resp.body(), java.nio.charset.StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || !line.startsWith("data: ")) continue;
                    String data = line.substring(6);
                    if (data.equals("[DONE]")) continue;
                    try {
                        Map<String, Object> parsed = new com.fasterxml.jackson.databind.ObjectMapper().readValue(data, Map.class);
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) parsed.get("choices");
                        if (choices != null && !choices.isEmpty()) {
                            Map<String, Object> delta = (Map<String, Object>) choices.get(0).get("delta");
                            if (delta != null) {
                                String content = (String) delta.get("content");
                                if (content != null && !content.isEmpty()) {
                                    emitter.send(SseEmitter.event().name("message").data(content));
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Skip unparseable SSE lines (common with streaming APIs)
                        log.debug("SSE parse skipped: {}", e.getMessage());
                    }
                }
                reader.close();
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("message").data("抱歉，" + e.getMessage()));
                } catch (IOException ex) {
                    log.warn("Failed to send AI error to SSE client", ex);
                }
                emitter.complete();
            }
        }).start();

        return emitter;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SimpleAiController.class);

    @RequireRole("USER")
    @PostMapping("/session")
    public Map<String, Long> createSession(@RequestParam(defaultValue = "新对话") String title) {
        return Map.of("sessionId", 1L);
    }

    @RequireRole("USER")
    @GetMapping("/session/{id}/messages")
    public List<Map<String, String>> getMessages(@PathVariable Long id) {
        return List.of(Map.of("role", "assistant", "content", "你好！有什么可以帮你的？"));
    }

    private Long getCurrentUserId() {
        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return p instanceof Number ? ((Number) p).longValue() : Long.valueOf(p.toString());
    }
}
