package com.blitz.seckill.application.service;

import com.blitz.common.exception.BusinessException;
import com.blitz.common.exception.ErrorCode;
import com.blitz.seckill.domain.model.SeckillActivity;
import com.blitz.seckill.domain.model.SeckillProduct;
import com.blitz.seckill.domain.port.SeckillActivityRepository;
import com.blitz.seckill.domain.port.SeckillProductRepository;
import com.blitz.seckill.infrastructure.kafka.SeckillOrderMessage;
import com.blitz.seckill.infrastructure.kafka.SeckillOrderProducer;
import com.blitz.seckill.infrastructure.redis.SeckillStockRedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeckillApplicationService — core seckill orchestration")
class SeckillApplicationServiceTest {

    @Mock
    private SeckillActivityRepository activityRepository;

    @Mock
    private SeckillProductRepository seckillProductRepository;

    @Mock
    private SeckillStockRedisService stockRedisService;

    @Mock
    private SeckillPathService seckillPathService;

    @Mock
    private SeckillOrderProducer orderProducer;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RRateLimiter rRateLimiter;

    @InjectMocks
    private SeckillApplicationService service;

    private static final Long SP_ID = 1L;
    private static final Long USER_ID = 100L;
    private static final Long ACTIVITY_ID = 10L;
    private static final String TEST_PATH = "valid-sig.9999999999";

    private SeckillProduct mockProduct;
    private SeckillActivity mockActivity;

    @BeforeEach
    void setUp() {
        mockProduct = new SeckillProduct();
        mockProduct.setId(SP_ID);
        mockProduct.setActivityId(ACTIVITY_ID);
        mockProduct.setSeckillStock(100);

        mockActivity = new SeckillActivity();
        mockActivity.setId(ACTIVITY_ID);
        mockActivity.setStatus(1); // in progress
        mockActivity.setStartTime(LocalDateTime.now().minusHours(1));
        mockActivity.setEndTime(LocalDateTime.now().plusHours(1));

        // Common mocking for rate limiter
        lenient().when(redissonClient.getRateLimiter(anyString())).thenReturn(rRateLimiter);
        lenient().when(rRateLimiter.trySetRate(
            any(RateType.class), anyLong(), anyLong(), any(RateIntervalUnit.class)))
            .thenReturn(true);
    }

    @Nested
    @DisplayName("getSeckillPath()")
    class GetSeckillPath {

        @Test
        @DisplayName("returns path when activity is in progress")
        void returnsPathWhenActivityInProgress() {
            when(seckillProductRepository.findById(SP_ID))
                .thenReturn(Optional.of(mockProduct));
            when(activityRepository.findById(ACTIVITY_ID))
                .thenReturn(Optional.of(mockActivity));
            when(seckillPathService.generatePath(SP_ID, USER_ID))
                .thenReturn(TEST_PATH);

            String result = service.getSeckillPath(SP_ID, USER_ID);

            assertEquals(TEST_PATH, result);
            verify(seckillPathService).generatePath(SP_ID, USER_ID);
        }

        @Test
        @DisplayName("throws SECKILL_OVER when activity is not in progress")
        void throwsWhenActivityNotInProgress() {
            mockActivity.setStatus(0); // pending, not in progress

            when(seckillProductRepository.findById(SP_ID))
                .thenReturn(Optional.of(mockProduct));
            when(activityRepository.findById(ACTIVITY_ID))
                .thenReturn(Optional.of(mockActivity));

            BusinessException ex = assertThrows(BusinessException.class,
                () -> service.getSeckillPath(SP_ID, USER_ID));
            assertEquals(ErrorCode.SECKILL_OVER.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("throws NOT_FOUND when product does not exist")
        void throwsWhenProductNotFound() {
            when(seckillProductRepository.findById(SP_ID))
                .thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                () -> service.getSeckillPath(SP_ID, USER_ID));
            assertEquals(ErrorCode.NOT_FOUND.getCode(), ex.getCode());
        }
    }

    @Nested
    @DisplayName("executeSeckill()")
    class ExecuteSeckill {

        private void setupSuccessfulMocks() {
            // Path valid
            when(seckillPathService.validatePath(TEST_PATH, SP_ID, USER_ID))
                .thenReturn(SP_ID);
            // Rate limiter passes
            when(rRateLimiter.tryAcquire()).thenReturn(true);
            // Not sold out
            when(stockRedisService.isOver(SP_ID)).thenReturn(false);
            // Lua deduct succeeds (returns remaining stock)
            when(stockRedisService.executeDeduct(SP_ID, USER_ID)).thenReturn(5L);
        }

        @Test
        @DisplayName("success: path valid, rate limiter passes, Lua returns 5, Kafka sent")
        void successFullPath() {
            setupSuccessfulMocks();
            doNothing().when(orderProducer).send(any(SeckillOrderMessage.class));

            Map<String, String> result = service.executeSeckill(SP_ID, USER_ID, TEST_PATH);

            assertNotNull(result);
            assertEquals("QUEUING", result.get("status"));
            assertNotNull(result.get("orderNo"));
            assertFalse(result.get("orderNo").isEmpty());

            verify(orderProducer).send(argThat(msg ->
                msg.userId().equals(USER_ID)
                    && msg.seckillProductId().equals(SP_ID)
                    && msg.orderNo().equals(result.get("orderNo"))
            ));
        }

        @Test
        @DisplayName("throws FORBIDDEN when path is invalid")
        void throwsWhenPathInvalid() {
            when(seckillPathService.validatePath(TEST_PATH, SP_ID, USER_ID))
                .thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                () -> service.executeSeckill(SP_ID, USER_ID, TEST_PATH));
            assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("throws 429 when rate limited")
        void throwsWhenRateLimited() {
            when(seckillPathService.validatePath(TEST_PATH, SP_ID, USER_ID))
                .thenReturn(SP_ID);
            when(rRateLimiter.tryAcquire()).thenReturn(false);

            BusinessException ex = assertThrows(BusinessException.class,
                () -> service.executeSeckill(SP_ID, USER_ID, TEST_PATH));
            assertEquals(429, ex.getCode());
        }

        @Test
        @DisplayName("throws SECKILL_OVER when isOver returns true")
        void throwsWhenOverFlagSet() {
            when(seckillPathService.validatePath(TEST_PATH, SP_ID, USER_ID))
                .thenReturn(SP_ID);
            when(rRateLimiter.tryAcquire()).thenReturn(true);
            when(stockRedisService.isOver(SP_ID)).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class,
                () -> service.executeSeckill(SP_ID, USER_ID, TEST_PATH));
            assertEquals(ErrorCode.SECKILL_OVER.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("throws SECKILL_OVER when Lua returns -1 (sold out)")
        void throwsWhenLuaReturnsSoldOut() {
            when(seckillPathService.validatePath(TEST_PATH, SP_ID, USER_ID))
                .thenReturn(SP_ID);
            when(rRateLimiter.tryAcquire()).thenReturn(true);
            when(stockRedisService.isOver(SP_ID)).thenReturn(false);
            when(stockRedisService.executeDeduct(SP_ID, USER_ID)).thenReturn(-1L);

            BusinessException ex = assertThrows(BusinessException.class,
                () -> service.executeSeckill(SP_ID, USER_ID, TEST_PATH));
            assertEquals(ErrorCode.SECKILL_OVER.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("throws SECKILL_REPEAT when Lua returns -2 (repeat purchase)")
        void throwsWhenLuaReturnsRepeat() {
            when(seckillPathService.validatePath(TEST_PATH, SP_ID, USER_ID))
                .thenReturn(SP_ID);
            when(rRateLimiter.tryAcquire()).thenReturn(true);
            when(stockRedisService.isOver(SP_ID)).thenReturn(false);
            when(stockRedisService.executeDeduct(SP_ID, USER_ID)).thenReturn(-2L);

            BusinessException ex = assertThrows(BusinessException.class,
                () -> service.executeSeckill(SP_ID, USER_ID, TEST_PATH));
            assertEquals(ErrorCode.SECKILL_REPEAT.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("throws STOCK_INSUFFICIENT when Lua returns -3 (insufficient stock)")
        void throwsWhenLuaReturnsInsufficient() {
            when(seckillPathService.validatePath(TEST_PATH, SP_ID, USER_ID))
                .thenReturn(SP_ID);
            when(rRateLimiter.tryAcquire()).thenReturn(true);
            when(stockRedisService.isOver(SP_ID)).thenReturn(false);
            when(stockRedisService.executeDeduct(SP_ID, USER_ID)).thenReturn(-3L);

            BusinessException ex = assertThrows(BusinessException.class,
                () -> service.executeSeckill(SP_ID, USER_ID, TEST_PATH));
            assertEquals(ErrorCode.STOCK_INSUFFICIENT.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("rolls back Redis when Kafka send fails")
        void rollsBackWhenKafkaFails() {
            setupSuccessfulMocks();
            doThrow(new RuntimeException("Kafka connection refused"))
                .when(orderProducer).send(any(SeckillOrderMessage.class));

            BusinessException ex = assertThrows(BusinessException.class,
                () -> service.executeSeckill(SP_ID, USER_ID, TEST_PATH));
            assertEquals(500, ex.getCode());

            // Verify rollback called
            verify(stockRedisService).rollbackStock(SP_ID, USER_ID);
        }

        @Test
        @DisplayName("does not send Kafka when Lua deduct fails")
        void doesNotSendKafkaWhenLuaFails() {
            when(seckillPathService.validatePath(TEST_PATH, SP_ID, USER_ID))
                .thenReturn(SP_ID);
            when(rRateLimiter.tryAcquire()).thenReturn(true);
            when(stockRedisService.isOver(SP_ID)).thenReturn(false);
            when(stockRedisService.executeDeduct(SP_ID, USER_ID)).thenReturn(-1L);

            assertThrows(BusinessException.class,
                () -> service.executeSeckill(SP_ID, USER_ID, TEST_PATH));

            // Kafka producer should never be called if Lua fails
            verify(orderProducer, never()).send(any());
        }
    }

    @Nested
    @DisplayName("queryResult()")
    class QueryResult {

        @Test
        @DisplayName("returns SOLD_OUT when over flag is true")
        void returnsSoldOutWhenOver() {
            when(stockRedisService.isOver(SP_ID)).thenReturn(true);

            String result = service.queryResult(SP_ID, USER_ID);

            assertEquals("SOLD_OUT", result);
        }

        @Test
        @DisplayName("returns UNKNOWN when over flag is false")
        void returnsUnknownWhenNotOver() {
            when(stockRedisService.isOver(SP_ID)).thenReturn(false);

            String result = service.queryResult(SP_ID, USER_ID);

            assertEquals("UNKNOWN", result);
        }
    }
}
