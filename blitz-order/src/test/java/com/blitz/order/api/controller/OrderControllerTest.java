package com.blitz.order.api.controller;

import com.blitz.common.result.PageResult;
import com.blitz.order.api.dto.CreateOrderRequest;
import com.blitz.order.api.dto.OrderItemVO;
import com.blitz.order.api.dto.OrderVO;
import com.blitz.order.application.service.OrderApplicationService;
import com.blitz.order.application.service.OrderQueryService;
import com.blitz.order.domain.model.Order;
import com.blitz.order.domain.model.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc tests for {@link OrderController} using standalone setup.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController MockMvc tests")
class OrderControllerTest {

    @Mock
    private OrderApplicationService orderApplicationService;

    @Mock
    private OrderQueryService orderQueryService;

    @InjectMocks
    private OrderController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private OrderVO sampleOrderVO;
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        // Set up SecurityContext with a Long principal (matches getCurrentUserId)
        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken(1L, null, "ROLE_USER"));

        var now = LocalDateTime.of(2024, 6, 18, 12, 0);

        var itemVO = new OrderItemVO(10L, 1001L, "Test SKU", "Red/XL",
            new BigDecimal("99.00"), 2);

        sampleOrderVO = new OrderVO(
            1L, "SN2024061800001", 1L,
            OrderStatus.PENDING_PAYMENT.getCode(),
            new BigDecimal("198.00"), BigDecimal.ZERO, BigDecimal.ZERO,
            null, null, null, null, null,
            "Receiver", "13800138000", "Address", null,
            now, List.of(itemVO)
        );

        sampleOrder = new Order();
        sampleOrder.setId(1L);
        sampleOrder.setOrderNo("SN2024061800001");
        sampleOrder.setUserId(1L);
        sampleOrder.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        sampleOrder.setTotalAmount(new BigDecimal("198.00"));
        sampleOrder.setFreightAmount(BigDecimal.ZERO);
        sampleOrder.setDiscountAmount(BigDecimal.ZERO);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ---- POST /order ----

    @Test
    @DisplayName("POST /order → 200 with OrderVO")
    void createOrderReturnsOk() throws Exception {
        when(orderApplicationService.createOrder(anyLong(), anyList(),
            anyString(), anyString(), anyString(), any()))
            .thenReturn(sampleOrder);
        when(orderQueryService.getById(1L)).thenReturn(sampleOrderVO);

        var item = new CreateOrderRequest.OrderItemRequest(
            1001L, "Test SKU", "Red/XL", new BigDecimal("99.00"), 2);
        var request = new CreateOrderRequest(
            List.of(item), "Receiver", "13800138000", "Address", null);

        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.orderNo").value("SN2024061800001"))
            .andExpect(jsonPath("$.data.status").value(0));

        verify(orderApplicationService).createOrder(
            anyLong(), anyList(), eq("Receiver"), eq("13800138000"), eq("Address"), eq(null));
    }

    // ---- GET /order/{id} ----

    @Test
    @DisplayName("GET /order/{id} → 200")
    void getOrderDetailReturnsOk() throws Exception {
        when(orderQueryService.getById(1L)).thenReturn(sampleOrderVO);

        mockMvc.perform(get("/order/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.orderNo").value("SN2024061800001"));
    }

    // ---- GET /order/list ----

    @Test
    @DisplayName("GET /order/list → 200 with list")
    void listOrdersReturnsOk() throws Exception {
        when(orderQueryService.listByUser(anyLong()))
            .thenReturn(List.of(sampleOrderVO));

        mockMvc.perform(get("/order/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data[0].orderNo").value("SN2024061800001"));
    }

    // ---- GET /order/page ----

    @Test
    @DisplayName("GET /order/page → 200 with page result")
    void pageOrdersReturnsOk() throws Exception {
        var pageResult = new PageResult<>(
            List.of(sampleOrderVO), 1L, 1, 10);
        when(orderQueryService.pageByUser(anyLong(), eq(1), eq(10)))
            .thenReturn(pageResult);

        mockMvc.perform(get("/order/page")
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.records[0].orderNo").value("SN2024061800001"))
            .andExpect(jsonPath("$.data.total").value(1));
    }

    // ---- POST /order/{id}/pay ----

    @Test
    @DisplayName("POST /order/{id}/pay → 200")
    void payOrderReturnsOk() throws Exception {
        mockMvc.perform(post("/order/1/pay")
                .param("paymentMethod", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        verify(orderApplicationService).payOrder(1L, 1L, 2);
    }

    // ---- POST /order/{id}/cancel ----

    @Test
    @DisplayName("POST /order/{id}/cancel → 200")
    void cancelOrderReturnsOk() throws Exception {
        mockMvc.perform(post("/order/1/cancel")
                .param("reason", "不想要了"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        verify(orderApplicationService).cancelOrder(1L, 1L, "不想要了");
    }

    // ---- POST /order/{id}/receive ----

    @Test
    @DisplayName("POST /order/{id}/receive → 200")
    void receiveOrderReturnsOk() throws Exception {
        mockMvc.perform(post("/order/1/receive"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        verify(orderApplicationService).receiveOrder(1L, 1L);
    }

    // ---- POST /order/{id}/ship ----

    @Test
    @DisplayName("POST /order/{id}/ship (admin) → 200")
    void shipOrderReturnsOk() throws Exception {
        mockMvc.perform(post("/order/1/ship"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        verify(orderApplicationService).shipOrder(1L);
    }
}
