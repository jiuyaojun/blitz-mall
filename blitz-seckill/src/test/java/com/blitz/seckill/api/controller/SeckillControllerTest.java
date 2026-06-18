package com.blitz.seckill.api.controller;

import com.blitz.seckill.application.service.SeckillApplicationService;
import com.blitz.seckill.application.service.SeckillCacheService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeckillController — REST API endpoint tests")
class SeckillControllerTest {

    @Mock
    private SeckillApplicationService seckillApplicationService;

    @Mock
    private SeckillCacheService seckillCacheService;

    @InjectMocks
    private SeckillController controller;

    private MockMvc mockMvc;

    private static final Long USER_ID = 100L;
    private static final Long SP_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        // Set up SecurityContext with a mock principal (lenient — warmup endpoint doesn't use it)
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getPrincipal()).thenReturn(USER_ID);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /seckill/path?seckillProductId=1 -> 200 with path data")
    void getPathReturns200() throws Exception {
        when(seckillApplicationService.getSeckillPath(SP_ID, USER_ID))
            .thenReturn("mock-sig.9999999999");

        mockMvc.perform(get("/seckill/path")
                .param("seckillProductId", String.valueOf(SP_ID)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.path").value("mock-sig.9999999999"))
            .andExpect(jsonPath("$.data.expireSeconds").value(60));

        verify(seckillApplicationService).getSeckillPath(SP_ID, USER_ID);
    }

    @Test
    @DisplayName("POST /seckill/execute -> 200 with QUEUING status")
    void executeReturns200() throws Exception {
        Map<String, String> resultMap = Map.of("orderNo", "SN123456", "status", "QUEUING");
        when(seckillApplicationService.executeSeckill(eq(SP_ID), eq(USER_ID), anyString()))
            .thenReturn(resultMap);

        mockMvc.perform(post("/seckill/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"seckillProductId":1,"path":"valid-path.9999999999"}"""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.status").value("QUEUING"))
            .andExpect(jsonPath("$.data.orderNo").value("SN123456"));

        verify(seckillApplicationService)
            .executeSeckill(SP_ID, USER_ID, "valid-path.9999999999");
    }

    @Test
    @DisplayName("GET /seckill/result?seckillProductId=1 -> 200")
    void resultReturns200() throws Exception {
        when(seckillApplicationService.queryResult(SP_ID, USER_ID))
            .thenReturn("UNKNOWN");

        mockMvc.perform(get("/seckill/result")
                .param("seckillProductId", String.valueOf(SP_ID)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data").value("UNKNOWN"));

        verify(seckillApplicationService).queryResult(SP_ID, USER_ID);
    }

    @Test
    @DisplayName("POST /seckill/admin/warmup/1 -> 200 (admin)")
    void warmupReturns200() throws Exception {
        doNothing().when(seckillCacheService).warmUpActivity(1L);

        mockMvc.perform(post("/seckill/admin/warmup/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        verify(seckillCacheService).warmUpActivity(1L);
    }
}
