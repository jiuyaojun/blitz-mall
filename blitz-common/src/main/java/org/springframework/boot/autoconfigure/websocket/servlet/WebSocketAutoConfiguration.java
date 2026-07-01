package org.springframework.boot.autoconfigure.websocket.servlet;

/**
 * Spring Boot 4.0 compatibility shim.
 * In SB 4.0, WebSocket auto-configuration was moved to a modular package.
 * Spring WebSocket/STOMP starters may still reference the old package path.
 * This empty class satisfies those classpath references.
 */
public class WebSocketAutoConfiguration {}
