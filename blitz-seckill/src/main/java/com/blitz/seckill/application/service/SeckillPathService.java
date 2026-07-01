package com.blitz.seckill.application.service;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 秒杀动态路径——防刷机制
 * 服务端生成 HMAC-SHA256 签名路径，客户端秒杀前先获取动态 path
 */
@Slf4j
@Service
@org.springframework.context.annotation.Profile("!minimal")
public class SeckillPathService {

    private final HMac hMac;

    /** 路径有效期 60 秒 */
    private static final long PATH_EXPIRE_SECONDS = 60;

    public SeckillPathService(@Value("${blitz.seckill.path-secret}") String secret) {
        this.hMac = new HMac(HmacAlgorithm.HmacSHA256, secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成秒杀动态路径
     * @return 签名字符串（URL-safe Base64）
     */
    public String generatePath(Long seckillProductId, Long userId) {
        long expireTime = System.currentTimeMillis() / 1000 + PATH_EXPIRE_SECONDS;
        String payload = seckillProductId + ":" + userId + ":" + expireTime;
        String signature = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(hMac.digest(payload));
        return signature + "." + expireTime;
    }

    /**
     * 校验秒杀路径是否合法
     * @param path 客户端发来的路径（signature.expireTime）
     * @return null if invalid, or the seckillProductId if valid
     */
    public Long validatePath(String path, Long seckillProductId, Long userId) {
        try {
            String[] parts = path.split("\\.");
            if (parts.length != 2) return null;

            String signature = parts[0];
            long expireTime = Long.parseLong(parts[1]);

            // 检查过期
            if (System.currentTimeMillis() / 1000 > expireTime) {
                log.warn("Seckill path expired: spId={}, userId={}", seckillProductId, userId);
                return null;
            }

            // 验证签名
            String expectedPayload = seckillProductId + ":" + userId + ":" + expireTime;
            String expectedSig = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(hMac.digest(expectedPayload));

            if (!signature.equals(expectedSig)) {
                log.warn("Seckill path invalid: spId={}, userId={}", seckillProductId, userId);
                return null;
            }

            return seckillProductId;
        } catch (Exception e) {
            log.error("Path validation error", e);
            return null;
        }
    }
}
