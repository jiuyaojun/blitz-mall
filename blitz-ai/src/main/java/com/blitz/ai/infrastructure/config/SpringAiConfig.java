package com.blitz.ai.infrastructure.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI configuration.
 * Registers ChatClient bean backed by the auto-configured ChatModel
 * (OpenAiChatModel when spring.ai.openai is on the classpath).
 */
@Configuration
@org.springframework.context.annotation.Profile("!minimal")
public class SpringAiConfig {

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
