package com.blitz.ai.application.service;

import com.blitz.ai.domain.model.ChatMessage;
import com.blitz.ai.domain.model.ChatSession;
import com.blitz.ai.domain.port.ChatMessageRepository;
import com.blitz.ai.domain.port.ChatSessionRepository;
import com.blitz.ai.infrastructure.llm.StreamingChatService;
import com.blitz.ai.infrastructure.rag.HybridRetrievalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatApplicationService")
class ChatApplicationServiceTest {

    @Mock
    private ChatSessionRepository sessionRepository;

    @Mock
    private ChatMessageRepository messageRepository;

    @Mock
    private HybridRetrievalService retrievalService;

    @Mock
    private StreamingChatService streamingChatService;

    @InjectMocks
    private ChatApplicationService chatApplicationService;

    @Nested
    @DisplayName("createSession")
    class CreateSession {

        @Test
        @DisplayName("saves with default title '新对话'")
        void savesWithDefaultTitle() {
            ArgumentCaptor<ChatSession> captor = ArgumentCaptor.forClass(ChatSession.class);

            chatApplicationService.createSession(1L, null);

            verify(sessionRepository).save(captor.capture());
            ChatSession saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(1L);
            assertThat(saved.getTitle()).isEqualTo("新对话");
        }

        @Test
        @DisplayName("saves with custom title")
        void savesWithCustomTitle() {
            ArgumentCaptor<ChatSession> captor = ArgumentCaptor.forClass(ChatSession.class);

            chatApplicationService.createSession(1L, "自定义标题");

            verify(sessionRepository).save(captor.capture());
            ChatSession saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(1L);
            assertThat(saved.getTitle()).isEqualTo("自定义标题");
        }

        @Test
        @DisplayName("returns the saved session")
        void returnsSavedSession() {
            when(sessionRepository.save(any())).thenAnswer(inv -> {
                ChatSession s = inv.getArgument(0);
                s.setId(100L);
                return s;
            });

            ChatSession result = chatApplicationService.createSession(1L, "test");

            assertThat(result.getId()).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("getMessages")
    class GetMessages {

        @Test
        @DisplayName("returns messages from repository")
        void returnsMessagesFromRepository() {
            // getMessages 现在带会话所有权校验，必须先让 findById 返回会话，
            // 否则会在 orElseThrow 处抛 "Session not found"。
            ChatSession session = new ChatSession();
            session.setId(1L);
            session.setUserId(1L);
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

            ChatMessage msg1 = ChatMessage.of(1L, ChatMessage.ROLE_USER, "hello");
            ChatMessage msg2 = ChatMessage.of(1L, ChatMessage.ROLE_AI, "hi there");
            when(messageRepository.findBySessionId(1L)).thenReturn(List.of(msg1, msg2));

            List<ChatMessage> result = chatApplicationService.getMessages(1L, 1L);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getContent()).isEqualTo("hello");
            assertThat(result.get(1).getRole()).isEqualTo(ChatMessage.ROLE_AI);
            verify(messageRepository).findBySessionId(1L);
        }

        @Test
        @DisplayName("拒绝读取他人的会话消息")
        void rejectsOtherUsersSession() {
            ChatSession session = new ChatSession();
            session.setId(1L);
            session.setUserId(999L);   // 会话属于别人
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

            assertThatThrownBy(() -> chatApplicationService.getMessages(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied");

            // 校验不通过时不应读消息
            verify(messageRepository, never()).findBySessionId(anyLong());
        }
    }

    @Nested
    @DisplayName("chat flow")
    class ChatFlow {

        @BeforeEach
        void setUp() {
            when(retrievalService.retrieve(anyString())).thenReturn(List.of("retrieved doc 1"));
            when(messageRepository.findRecentBySessionId(anyLong(), anyInt())).thenReturn(List.of());
            when(streamingChatService.streamRagChat(anyString(), anyString(), anyString()))
                .thenReturn(Flux.just("Hello", " world"));
        }

        @Test
        @DisplayName("verify user message saved before streaming")
        void verifyUserMessageSavedBeforeStreaming() {
            Flux<String> flux = chatApplicationService.chat(1L, 1L, "test question");

            // User message should be saved eagerly, before subscribing to the flux
            verify(messageRepository).save(argThat(m ->
                m.getRole().equals(ChatMessage.ROLE_USER) && m.getContent().equals("test question")));

            // Subscribe to trigger doOnComplete (which saves AI message)
            flux.collectList().block();

            // Now AI message should be saved too
            verify(messageRepository).save(argThat(m ->
                m.getRole().equals(ChatMessage.ROLE_AI)));
        }

        @Test
        @DisplayName("verify hybrid retrieval called with question")
        void verifyHybridRetrievalCalledWithQuestion() {
            Flux<String> flux = chatApplicationService.chat(1L, 1L, "test question");

            verify(retrievalService).retrieve("test question");

            // Need to consume flux to avoid hanging
            flux.collectList().block();
        }

        @Test
        @DisplayName("verify streamingChatService called with context + history")
        void verifyStreamingChatServiceCalledWithContextAndHistory() {
            when(retrievalService.retrieve("my question")).thenReturn(List.of("docA", "docB"));
            when(messageRepository.findRecentBySessionId(eq(1L), eq(10)))
                .thenReturn(List.of(
                    ChatMessage.of(1L, ChatMessage.ROLE_USER, "previous q"),
                    ChatMessage.of(1L, ChatMessage.ROLE_AI, "previous a")
                ));

            Flux<String> flux = chatApplicationService.chat(1L, 1L, "my question");

            verify(streamingChatService).streamRagChat(
                eq("my question"),
                argThat(ctx -> ctx.contains("docA") && ctx.contains("docB")),
                argThat(hist -> hist.contains("previous q") && hist.contains("previous a"))
            );

            flux.collectList().block();
        }

        @Test
        @DisplayName("flux returns the streaming chunks")
        void fluxReturnsStreamingChunks() {
            when(streamingChatService.streamRagChat(anyString(), anyString(), anyString()))
                .thenReturn(Flux.just("chunk1", "chunk2", "chunk3"));

            Flux<String> flux = chatApplicationService.chat(1L, 1L, "question");

            List<String> chunks = flux.collectList().block();
            assertThat(chunks).containsExactly("chunk1", "chunk2", "chunk3");
        }

        @Test
        @DisplayName("AI message saved on complete with full response")
        void aiMessageSavedOnCompleteWithFullResponse() {
            when(streamingChatService.streamRagChat(anyString(), anyString(), anyString()))
                .thenReturn(Flux.just("Hello", " ", "World"));

            Flux<String> flux = chatApplicationService.chat(1L, 1L, "test question");
            flux.collectList().block();

            ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
            verify(messageRepository, times(2)).save(captor.capture());

            List<ChatMessage> savedMessages = captor.getAllValues();
            ChatMessage aiMsg = savedMessages.get(1);
            assertThat(aiMsg.getRole()).isEqualTo(ChatMessage.ROLE_AI);
            assertThat(aiMsg.getContent()).isEqualTo("Hello World");
        }
    }
}
