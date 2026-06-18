package com.blitz.seckill.infrastructure.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.*;
import org.redisson.client.codec.StringCodec;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeckillStockRedisService — Redis Lua stock operations (mocked Redisson)")
class SeckillStockRedisServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RScript rScript;

    @Mock
    private RBucket<Object> rBucket;

    @Mock
    private RAtomicLong rAtomicLong;

    @InjectMocks
    private SeckillStockRedisService service;

    private static final Long SP_ID = 1L;
    private static final Long USER_ID = 100L;
    private static final int STOCK = 100;

    @BeforeEach
    void setUp() throws Exception {
        // Inject luaSha via reflection (normally set by loadScript())
        Field luaShaField = SeckillStockRedisService.class.getDeclaredField("luaSha");
        luaShaField.setAccessible(true);
        luaShaField.set(service, "mock-sha-abcdef");

        // Common mocking: getBucket returns the same RBucket mock
        lenient().when(redissonClient.getBucket(anyString(), eq(StringCodec.INSTANCE)))
            .thenReturn(rBucket);
        lenient().when(redissonClient.getBucket(anyString()))
            .thenReturn(rBucket);
        lenient().when(redissonClient.getScript(StringCodec.INSTANCE)).thenReturn(rScript);
        lenient().when(redissonClient.getScript()).thenReturn(rScript);
    }

    @Nested
    @DisplayName("initStock()")
    class InitStock {

        @Test
        @DisplayName("sets stock bucket and deletes over flag")
        void setsStockAndDeletesOverFlag() {
            service.initStock(SP_ID, STOCK);

            // Verify stock bucket set with string value
            verify(redissonClient).getBucket(
                eq(String.format("seckill:stock:%d", SP_ID)), eq(StringCodec.INSTANCE));
            verify(rBucket).set(eq(String.valueOf(STOCK)));

            // Verify over flag bucket deleted
            verify(redissonClient).getBucket(
                eq(String.format("seckill:over:%d", SP_ID)), eq(StringCodec.INSTANCE));
            verify(rBucket).delete();
        }
    }

    @Nested
    @DisplayName("executeDeduct()")
    class ExecuteDeduct {

        @Test
        @DisplayName("delegates to RScript.evalSha with correct keys")
        void delegatesToEvalShaWithCorrectKeys() {
            when(rScript.evalSha(any(RScript.Mode.class), anyString(),
                any(RScript.ReturnType.class), anyList()))
                .thenReturn(5L);

            long result = service.executeDeduct(SP_ID, USER_ID);

            assertEquals(5L, result);

            // Capture the keys argument
            ArgumentCaptor<List<Object>> keysCaptor = ArgumentCaptor.forClass(List.class);
            verify(rScript).evalSha(
                eq(RScript.Mode.READ_WRITE),
                eq("mock-sha-abcdef"),
                eq(RScript.ReturnType.INTEGER),
                keysCaptor.capture()
            );

            List<Object> keys = keysCaptor.getValue();
            assertEquals(3, keys.size());
            assertEquals(String.format("seckill:stock:%d", SP_ID), keys.get(0));
            assertEquals(String.format("seckill:over:%d", SP_ID), keys.get(1));
            assertEquals(String.format("seckill:user:%d:%d", SP_ID, USER_ID), keys.get(2));
        }

        @Test
        @DisplayName("throws when luaSha is null")
        void throwsWhenLuaShaNull() throws Exception {
            // Create a fresh service without luaSha set
            SeckillStockRedisService freshService = new SeckillStockRedisService(redissonClient);

            assertThrows(IllegalStateException.class,
                () -> freshService.executeDeduct(SP_ID, USER_ID));
        }
    }

    @Nested
    @DisplayName("rollbackStock()")
    class RollbackStock {

        @Test
        @DisplayName("increments stock and deletes user key")
        void incrementsStockAndDeletesUserKey() {
            when(redissonClient.getAtomicLong(
                eq(String.format("seckill:stock:%d", SP_ID))))
                .thenReturn(rAtomicLong);
            when(rAtomicLong.incrementAndGet()).thenReturn(101L);

            service.rollbackStock(SP_ID, USER_ID);

            // Verify stock incremented
            verify(rAtomicLong).incrementAndGet();

            // Verify user key deleted
            verify(redissonClient).getBucket(
                eq(String.format("seckill:user:%d:%d", SP_ID, USER_ID)),
                eq(StringCodec.INSTANCE));
            verify(rBucket).delete();
        }
    }

    @Nested
    @DisplayName("isOver()")
    class IsOver {

        @Test
        @DisplayName("returns true when over flag is '1'")
        void returnsTrueWhenOverFlagIs1() {
            when(rBucket.get()).thenReturn("1");

            boolean result = service.isOver(SP_ID);

            assertTrue(result);
            verify(redissonClient).getBucket(
                eq(String.format("seckill:over:%d", SP_ID)), eq(StringCodec.INSTANCE));
        }

        @Test
        @DisplayName("returns false when over flag is null")
        void returnsFalseWhenOverFlagIsNull() {
            when(rBucket.get()).thenReturn(null);

            boolean result = service.isOver(SP_ID);

            assertFalse(result);
        }

        @Test
        @DisplayName("returns false when over flag is not '1'")
        void returnsFalseWhenOverFlagNot1() {
            when(rBucket.get()).thenReturn("0");

            boolean result = service.isOver(SP_ID);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("getRemainingStock()")
    class GetRemainingStock {

        @Test
        @DisplayName("returns parsed integer when stock exists")
        void returnsStockValue() {
            when(rBucket.get()).thenReturn("42");

            int result = service.getRemainingStock(SP_ID);

            assertEquals(42, result);
        }

        @Test
        @DisplayName("returns 0 when stock is null")
        void returnsZeroWhenNull() {
            when(rBucket.get()).thenReturn(null);

            int result = service.getRemainingStock(SP_ID);

            assertEquals(0, result);
        }
    }
}
