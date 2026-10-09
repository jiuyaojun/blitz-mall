package com.blitz.order.application.service;

import com.blitz.common.exception.BusinessException;
import com.blitz.common.exception.ErrorCode;
import com.blitz.order.domain.model.Order;
import com.blitz.order.domain.model.OrderOperateLog;
import com.blitz.order.domain.model.OrderStatus;
import com.blitz.order.domain.port.OrderEventRepository;
import com.blitz.order.domain.port.OrderItemRepository;
import com.blitz.order.domain.port.OrderRepository;
import com.blitz.order.infrastructure.mapper.OrderOperateLogMapper;
import com.blitz.order.infrastructure.messaging.OrderEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OrderApplicationService} — mock all dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderApplicationService unit tests")
class OrderApplicationServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private OrderEventRepository orderEventRepository;
    @Mock private OrderEventPublisher orderEventPublisher;
    @Mock private OrderOperateLogMapper orderOperateLogMapper;
    @Mock private StockDeductionService stockDeductionService;

    @InjectMocks
    private OrderApplicationService service;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        // orderEventPublisher 在 OrderApplicationService 里是 @Autowired(required = false)
        // 的非 final 字段（minimal 兼容补丁把它从 final 改掉了），因此不在 Lombok
        // @RequiredArgsConstructor 生成的构造器参数里。
        // Mockito 的 @InjectMocks 只采用一种注入策略：存在可满足的构造器时只用构造器，
        // 不会再做字段注入 —— 于是该字段恒为 null，
        // 服务里 `if (orderEventPublisher != null)` 短路，publishXxx 永远不会被调用。
        // 生产环境由 Spring 正常注入，这里显式补上。
        org.springframework.test.util.ReflectionTestUtils.setField(
            service, "orderEventPublisher", orderEventPublisher);

        sampleOrder = new Order();
        sampleOrder.setId(1L);
        sampleOrder.setOrderNo("SN2024061800001");
        sampleOrder.setUserId(100L);
        sampleOrder.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        sampleOrder.setTotalAmount(new BigDecimal("199.00"));
        sampleOrder.setFreightAmount(BigDecimal.ZERO);
        sampleOrder.setDiscountAmount(BigDecimal.ZERO);

        // Default mocks (lenient because not all tests use every stub)
        lenient().doNothing().when(stockDeductionService).deductWithRetry(anyList(), anyInt());
        lenient().doNothing().when(orderEventPublisher).publishOrderCreated(any(), any());
        lenient().doNothing().when(orderEventPublisher).publishOrderPaid(any());
        lenient().doNothing().when(orderEventPublisher).publishOrderCancelled(any(), anyString());
        lenient().when(orderOperateLogMapper.insert(any(OrderOperateLog.class))).thenReturn(1);
    }

    // ---- createOrder ----

    @Test
    @DisplayName("createOrder: saves order with PENDING_PAYMENT, publishes event, generates orderNo")
    void createOrderSavesWithPendingPaymentAndPublishesEvent() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        List<OrderApplicationService.CreateOrderItem> items = List.of(
            new OrderApplicationService.CreateOrderItem(
                1001L, "Test SKU", "Red/XL", new BigDecimal("99.50"), 2)
        );

        // Act
        Order result = service.createOrder(
            100L, items, "Receiver", "13800138000", "Address", "Note");

        // Assert
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getOrderNo(), "OrderNo should be generated");
        assertFalse(result.getOrderNo().isEmpty(), "OrderNo should not be empty");
        assertEquals(OrderStatus.PENDING_PAYMENT.getCode(), result.getStatus(),
            "Status should be PENDING_PAYMENT");
        assertEquals(100L, result.getUserId());

        // Verify stock deducted
        verify(stockDeductionService).deductWithRetry(eq(items), eq(3));

        // Verify order saved with correct status
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals(OrderStatus.PENDING_PAYMENT.getCode(), savedOrder.getStatus());
        assertNotNull(savedOrder.getOrderNo());

        // Verify order items saved
        verify(orderItemRepository).saveBatch(anyList());

        // Verify event persisted
        verify(orderEventRepository).save(any());

        // Verify Kafka event published
        verify(orderEventPublisher).publishOrderCreated(any(Order.class), anyList());

        // Verify operate log inserted
        verify(orderOperateLogMapper).insert(any(OrderOperateLog.class));
    }

    @Test
    @DisplayName("createOrder: totalAmount is calculated from items")
    void createOrderCalculatesCorrectTotalAmount() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(2L);
            return o;
        });

        List<OrderApplicationService.CreateOrderItem> items = List.of(
            new OrderApplicationService.CreateOrderItem(
                1001L, "SKU A", "M", new BigDecimal("100.00"), 2),
            new OrderApplicationService.CreateOrderItem(
                1002L, "SKU B", "L", new BigDecimal("50.00"), 1)
        );

        // Act
        Order result = service.createOrder(
            100L, items, "Receiver", "13800138000", "Address", null);

        // Assert
        assertEquals(0, new BigDecimal("250.00").compareTo(result.getTotalAmount()),
            "Total should be 100*2 + 50*1 = 250.00");
    }

    // ---- payOrder ----

    @Test
    @DisplayName("payOrder: transitions to PAID, saves, publishes event")
    void payOrderTransitionsToPaidAndPublishesEvent() {
        // Arrange: order is PENDING_PAYMENT
        sampleOrder.setUserId(100L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        // Act
        service.payOrder(1L, 100L, 2);

        // Assert
        assertEquals(OrderStatus.PAID.getCode(), sampleOrder.getStatus(),
            "Order should be PAID after payOrder");
        assertEquals(2, sampleOrder.getPaymentMethod());
        assertNotNull(sampleOrder.getPaymentTime());

        verify(orderRepository).save(sampleOrder);
        verify(orderEventRepository).save(any());
        verify(orderEventPublisher).publishOrderPaid(sampleOrder);
        verify(orderOperateLogMapper).insert(any(OrderOperateLog.class));
    }

    @Test
    @DisplayName("payOrder with wrong userId throws FORBIDDEN")
    void payOrderWithWrongUserThrowsForbidden() {
        // Arrange: order belongs to userId 100
        sampleOrder.setUserId(100L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
            () -> service.payOrder(1L, 999L, 2),
            "Paying with wrong userId should throw");

        assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode(),
            "Error code should be FORBIDDEN");

        // Verify order NOT saved
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("payOrder with order not found throws ORDER_NOT_FOUND")
    void payOrderNotFoundThrowsOrderNotFound() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
            () -> service.payOrder(999L, 100L, 1),
            "Paying non-existent order should throw");

        assertEquals(ErrorCode.ORDER_NOT_FOUND.getCode(), ex.getCode(),
            "Error code should be ORDER_NOT_FOUND");
    }

    // ---- cancelOrder ----

    @Test
    @DisplayName("cancelOrder: calls order.cancel(), saves, restores stock")
    void cancelOrderCancelsAndRestoresStock() {
        // Arrange: order is PENDING_PAYMENT
        sampleOrder.setUserId(100L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        // Act
        service.cancelOrder(1L, 100L, "不想要了");

        // Assert
        assertEquals(OrderStatus.CANCELLED.getCode(), sampleOrder.getStatus(),
            "Order should be CANCELLED after cancelOrder");

        verify(orderRepository).save(sampleOrder);
        verify(stockDeductionService).restoreStock(1L);
        verify(orderEventRepository).save(any());
        verify(orderEventPublisher).publishOrderCancelled(sampleOrder, "不想要了");
        verify(orderOperateLogMapper).insert(any(OrderOperateLog.class));
    }

    @Test
    @DisplayName("cancelOrder with wrong userId throws FORBIDDEN")
    void cancelOrderWithWrongUserThrowsForbidden() {
        // Arrange
        sampleOrder.setUserId(100L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
            () -> service.cancelOrder(1L, 999L, "reason"));

        assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
        verify(stockDeductionService, never()).restoreStock(anyLong());
    }

    @Test
    @DisplayName("cancelOrder: order not found throws ORDER_NOT_FOUND")
    void cancelOrderNotFoundThrowsOrderNotFound() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
            () -> service.cancelOrder(999L, 100L, "reason"));

        assertEquals(ErrorCode.ORDER_NOT_FOUND.getCode(), ex.getCode());
    }

    // ---- shipOrder ----

    @Test
    @DisplayName("shipOrder: transitions PAID to SHIPPED, saves, publishes event")
    void shipOrderTransitionsPaidToShipped() {
        // Arrange
        sampleOrder.setStatus(OrderStatus.PAID.getCode());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        // Act
        service.shipOrder(1L);

        // Assert
        assertEquals(OrderStatus.SHIPPED.getCode(), sampleOrder.getStatus(),
            "Order should be SHIPPED after shipOrder");
        assertNotNull(sampleOrder.getDeliveryTime());

        verify(orderRepository).save(sampleOrder);
        verify(orderEventRepository).save(any());
        verify(orderOperateLogMapper).insert(any(OrderOperateLog.class));
    }

    // ---- receiveOrder ----

    @Test
    @DisplayName("receiveOrder: transitions SHIPPED to RECEIVED")
    void receiveOrderTransitionsShippedToReceived() {
        // Arrange
        sampleOrder.setStatus(OrderStatus.SHIPPED.getCode());
        sampleOrder.setUserId(100L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        // Act
        service.receiveOrder(1L, 100L);

        // Assert
        assertEquals(OrderStatus.RECEIVED.getCode(), sampleOrder.getStatus(),
            "Order should be RECEIVED after receiveOrder");
        assertNotNull(sampleOrder.getReceiveTime());

        verify(orderRepository).save(sampleOrder);
        verify(orderOperateLogMapper).insert(any(OrderOperateLog.class));
    }

    @Test
    @DisplayName("receiveOrder with wrong userId throws FORBIDDEN")
    void receiveOrderWithWrongUserThrowsForbidden() {
        // Arrange
        sampleOrder.setUserId(100L);
        sampleOrder.setStatus(OrderStatus.SHIPPED.getCode());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
            () -> service.receiveOrder(1L, 999L));

        assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
    }
}
