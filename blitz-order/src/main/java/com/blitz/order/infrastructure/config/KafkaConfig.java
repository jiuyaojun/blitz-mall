package com.blitz.order.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@org.springframework.context.annotation.Profile("!minimal")
public class KafkaConfig {

    @Bean
    public NewTopic orderEventTopic() {
        return new NewTopic("blitz-order-event", 3, (short) 1);
    }
}
