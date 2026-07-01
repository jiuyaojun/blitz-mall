package org.springframework.boot.autoconfigure.kafka;

/**
 * Spring Boot 4.0 compatibility shim.
 * In SB 4.0, Kafka auto-configuration was moved to a modular package.
 * Spring Cloud Stream and other libraries still reference the old package path.
 * This empty class satisfies those classpath references.
 */
public class KafkaAutoConfiguration {}
