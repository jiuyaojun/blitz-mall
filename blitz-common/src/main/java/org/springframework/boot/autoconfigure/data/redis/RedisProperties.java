package org.springframework.boot.autoconfigure.data.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot 4.0 backward-compatible Redis property holder.
 * In SB 4.0, RedisProperties was moved to a modular package.
 * Redisson starter references the old package path, so this shim
 * provides the ConfigurationProperties binding that Redisson expects.
 */
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {
    private String host;
    private int port = 6379;
    private String password;

    public String getHost() { return host; }
    public void setHost(String h) { host = h; }
    public int getPort() { return port; }
    public void setPort(int p) { port = p; }
    public String getPassword() { return password; }
    public void setPassword(String p) { password = p; }
}
