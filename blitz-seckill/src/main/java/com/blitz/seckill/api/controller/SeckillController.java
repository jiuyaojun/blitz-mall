package com.blitz.seckill.api.controller;

import com.blitz.common.result.Result;
import com.blitz.seckill.api.dto.SeckillExecuteRequest;
import com.blitz.seckill.api.dto.SeckillPathVO;
import com.blitz.seckill.application.service.SeckillApplicationService;
import com.blitz.seckill.application.service.SeckillCacheService;
import com.blitz.security.annotation.RequireRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/seckill")
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("!minimal")
public class SeckillController {

    private final SeckillApplicationService seckillApplicationService;
    private final SeckillCacheService seckillCacheService;

    /** 获取秒杀动态路径（HMAC签名） */
    @GetMapping("/path")
    public Result<SeckillPathVO> getPath(@RequestParam Long seckillProductId) {
        Long userId = getCurrentUserId();
        String path = seckillApplicationService.getSeckillPath(seckillProductId, userId);
        return Result.success(new SeckillPathVO(path, 60));
    }

    /** 执行秒杀 */
    @PostMapping("/execute")
    public Result<Map<String, String>> execute(@Valid @RequestBody SeckillExecuteRequest req) {
        Long userId = getCurrentUserId();
        Map<String, String> result = seckillApplicationService.executeSeckill(
            req.seckillProductId(), userId, req.path());
        return Result.success(result);
    }

    /** 查询秒杀结果（轮询降级——当 WebSocket 不可用时） */
    @GetMapping("/result")
    public Result<String> result(@RequestParam Long seckillProductId) {
        Long userId = getCurrentUserId();
        return Result.success(seckillApplicationService.queryResult(seckillProductId, userId));
    }

    /** 管理员：预热活动库存到 Redis */
    @PostMapping("/admin/warmup/{activityId}")
    @RequireRole("ADMIN")
    public Result<Void> warmup(@PathVariable Long activityId) {
        seckillCacheService.warmUpActivity(activityId);
        return Result.success();
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ((Number) principal).longValue();
    }
}
