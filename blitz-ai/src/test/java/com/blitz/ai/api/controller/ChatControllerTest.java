package com.blitz.ai.api.controller;

import com.blitz.ai.api.dto.ChatRequest;
import com.blitz.ai.application.service.ChatApplicationService;
import com.blitz.ai.domain.model.ChatMessage;
import com.blitz.ai.domain.model.ChatSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatController")
class ChatControllerTest {

    @Mock
    private ChatApplicationService chatApplicationService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ChatController controller = new ChatController(chatApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken(1L, null, Collections.emptyList()));
    }

    @Nested
    @DisplayName("POST /ai/session")
    class CreateSession {

        @Test
        @DisplayName("returns 200 with sessionId")
        void returns200WithSessionId() throws Exception {
            ChatSession session = new ChatSession();
            session.setId(42L);
            when(chatApplicationService.createSession(eq(1L), eq("新对话"))).thenReturn(session);

            mockMvc.perform(post("/ai/session")
                    .param("title", "新对话"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(42));
        }

        @Test
        @DisplayName("returns 200 with custom title")
        void returns200WithCustomTitle() throws Exception {
            ChatSession session = new ChatSession();
            session.setId(99L);
            when(chatApplicationService.createSession(eq(1L), eq("客服咨询"))).thenReturn(session);

            mockMvc.perform(post("/ai/session")
                    .param("title", "客服咨询"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(99));
        }
    }

    @Nested
    @DisplayName("GET /ai/session/{id}/messages")
    class GetMessages {

        @Test
        @DisplayName("returns 200 with message list")
        void returns200WithMessageList() throws Exception {
            ChatMessage msg1 = ChatMessage.of(1L, ChatMessage.ROLE_USER, "hello");
            msg1.setId(1L);
            ChatMessage msg2 = ChatMessage.of(1L, ChatMessage.ROLE_AI, "hi");
            msg2.setId(2L);
            when(chatApplicationService.getMessages(1L)).thenReturn(List.of(msg1, msg2));

            mockMvc.perform(get("/ai/session/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(jsonPath("$[0].content").value("hello"))
                .andExpect(jsonPath("$[1].role").value("AI"))
                .andExpect(jsonPath("$[1].content").value("hi"));
        }

        @Test
        @DisplayName("returns 200 with empty list when no messages")
        void returns200WithEmptyListWhenNoMessages() throws Exception {
            when(chatApplicationService.getMessages(1L)).thenReturn(List.of());

            mockMvc.perform(get("/ai/session/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("POST /ai/chat/stream (SSE)")
    class ChatStream {

        @Test
        @DisplayName("returns 200 with existing sessionId")
        void returns200WithExistingSessionId() throws Exception {
            when(chatApplicationService.chat(eq(1L), eq(1L), eq("hello")))
                .thenReturn(Flux.just("Hello", " response"));

            String requestBody = objectMapper.writeValueAsString(
                new ChatRequest("hello", 1L));

            mockMvc.perform(post("/ai/chat/stream")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns 200 with null sessionId (creates new session)")
        void returns200WithNullSessionId() throws Exception {
            ChatSession newSession = new ChatSession();
            newSession.setId(10L);
            when(chatApplicationService.createSession(eq(1L), isNull())).thenReturn(newSession);
            when(chatApplicationService.chat(eq(10L), eq(1L), eq("first message")))
                .thenReturn(Flux.just("reply"));

            String requestBody = objectMapper.writeValueAsString(
                new ChatRequest("first message", null));

            mockMvc.perform(post("/ai/chat/stream")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk());
        }
    }
}
