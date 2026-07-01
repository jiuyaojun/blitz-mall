package org.springframework.boot.web.context;

import org.springframework.context.ApplicationEvent;

/**
 * Spring Boot 4.0 compatibility shim.
 * Spring Cloud Discovery references this; in SB 4.0 moved to modular package.
 */
public class WebServerInitializedEvent extends ApplicationEvent {
    public WebServerInitializedEvent(Object s) { super(s); }
    public Object getWebServer() { return getSource(); }
    public int getWebServerPort() { return 8080; }
    public Object getApplicationContext() { return null; }
}
