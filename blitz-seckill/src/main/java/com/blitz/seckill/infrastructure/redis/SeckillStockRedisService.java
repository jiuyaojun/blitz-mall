package com.blitz.seckill.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis Lua 原子操作服务——封装秒杀库存的预扣、回滚、缓存管理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillStockRedisService {

    private final RedissonClient redissonClient;

    private String luaSha;
    private final ConcurrentHashMap<String, String> shaCache = new ConcurrentHashMap<>();

    private static final String STOCK_KEY = "seckill:stock:%d";
    private static final String OVER_KEY = "seckill:over:%d";
    private static final String USER_KEY = "seckill:user:%d:%d";

    /** 加载 Lua 脚本到 Redis（返回 SHA） */
    public String loadScript() throws IOException {
        ClassPathResource resource = new ClassPathResource("lua/seckill_deduct.lua");
        String script = resource.getContentAsString(StandardCharsets.UTF_8);
        this.luaSha = redissonClient.getScript().scriptLoad(script);
        log.info("Seckill Lua script loaded: SHA={}", luaSha);
        return luaSha;
    }

    /** 预热库存到 Redis */
    public void initStock(Long seckillProductId, int stock) {
        String stockKey = String.format(STOCK_KEY, seckillProductId);
        redissonClient.getBucket(stockKey, StringCodec.INSTANCE).set(String.valueOf(stock));
        // 清除旧的售罄标记
        String overKey = String.format(OVER_KEY, seckillProductId);
        redissonClient.getBucket(overKey, StringCodec.INSTANCE).delete();
        log.info("Seckill stock initialized: seckillProductId={}, stock={}", seckillProductId, stock);
    }

    /** 查询剩余库存 */
    public int getRemainingStock(Long seckillProductId) {
        String stockKey = String.format(STOCK_KEY, seckillProductId);
        String val = (String) redissonClient.getBucket(stockKey, StringCodec.INSTANCE).get();
        return val == null ? 0 : Integer.parseInt(val);
    }

    /** 是否已售罄 */
    public boolean isOver(Long seckillProductId) {
        String overKey = String.format(OVER_KEY, seckillProductId);
        String val = (String) redissonClient.getBucket(overKey, StringCodec.INSTANCE).get();
        return "1".equals(val);
    }

    /**
     * 执行 Lua 原子扣减
     * @return >=0 剩余库存, -1 售罄, -2 重复购买, -3 库存不足
     */
    public long executeDeduct(Long seckillProductId, Long userId) {
        if (luaSha == null) {
            throw new IllegalStateException("Lua script not loaded — call loadScript() first");
        }

        List<Object> keys = Arrays.asList(
            String.format(STOCK_KEY, seckillProductId),
            String.format(OVER_KEY, seckillProductId),
            String.format(USER_KEY, seckillProductId, userId)
        );

        RScript script = redissonClient.getScript(StringCodec.INSTANCE);
        return script.evalSha(
            RScript.Mode.READ_WRITE,
            luaSha,
            RScript.ReturnType.INTEGER,
            keys
        );
    }

    /** 回滚库存（Kafka 消费失败时调用） */
    public void rollbackStock(Long seckillProductId, Long userId) {
        String stockKey = String.format(STOCK_KEY, seckillProductId);
        redissonClient.getAtomicLong(stockKey).incrementAndGet();
        String userKey = String.format(USER_KEY, seckillProductId, userId);
        redissonClient.getBucket(userKey, StringCodec.INSTANCE).delete();
        log.info("Stock rolled back: seckillProductId={}, userId={}", seckillProductId, userId);
    }
}
