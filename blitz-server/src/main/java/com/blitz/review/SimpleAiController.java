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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SimpleAiController.class);

    @Value("${OPENAI_API_KEY:sk-demo}")
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
                // Fallback mode: when no real API key configured, use canned responses
                if ("sk-demo".equals(apiKey)) {
                    String fallback = getFallbackResponse(message);
                    // Simulate streaming by sending char-by-char with small delay
                    for (int i = 0; i < fallback.length(); i++) {
                        emitter.send(SseEmitter.event().name("message")
                            .data(String.valueOf(fallback.charAt(i))));
                        Thread.sleep(30);
                    }
                    emitter.complete();
                    return;
                }

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
                        Map<String, Object> parsed = objectMapper.readValue(data, Map.class);
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

    /** Fallback canned responses when no real API key is configured. */
    private String getFallbackResponse(String question) {
        String q = question != null ? question.toLowerCase() : "";
        if (q.contains("退") || q.contains("换") || q.contains("货")) {
            return "Blitz商城支持7天无理由退换货。在订单详情页点击「申请退款」即可发起，商家审核通过后款项将在3-5个工作日原路退回。";
        }
        if (q.contains("支付") || q.contains("付钱") || q.contains("付款")) {
            return "Blitz商城支持模拟支付（演示环境）。在支付页面选择支付方式后点击确认即可完成支付，系统会自动更新订单状态。";
        }
        if (q.contains("物流") || q.contains("快递") || q.contains("发货")) {
            return "商家发货后您可以在订单详情页查看物流信息。通常下单后24小时内发货，快递配送时效为1-3天。";
        }
        if (q.contains("秒杀") || q.contains("抢购") || q.contains("限时")) {
            return "秒杀活动有固定的时间窗口。活动开始前链接隐藏，开始后从秒杀页面获取动态链接参与。库存有限，先到先得哦！";
        }
        if (q.contains("优惠") || q.contains("券") || q.contains("折扣")) {
            return "您可以在优惠券页面查看平台发放的优惠券，点击「领取」后即可使用。下单时在结算页面选择可用优惠券即可享受折扣。";
        }
        if (q.contains("材质") || q.contains("面料") || q.contains("尺码") || q.contains("规格")
            || q.contains("颜色") || q.contains("大小")) {
            return "商品的材质、尺码、颜色等规格信息可以在商品详情页查看。点击不同SKU规格即可看到对应的价格和库存信息。";
        }
        if (q.contains("你好") || q.contains("在吗") || q.contains("您好") || q.contains("hi")
            || q.contains("hello") || q.contains("嗨")) {
            return "您好！我是Blitz智能商城的AI客服助手。有什么可以帮助您的吗？您可以咨询下单流程、支付方式、退换货政策、秒杀活动等问题。";
        }
        return "感谢您的咨询！Blitz商城支持完整的购物流程：浏览商品→加入购物车→下单支付→物流配送→确认收货。如需帮助，请具体说明您遇到的问题。";
    }

    private Long getCurrentUserId() {
        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return p instanceof Number ? ((Number) p).longValue() : Long.valueOf(p.toString());
    }
}
