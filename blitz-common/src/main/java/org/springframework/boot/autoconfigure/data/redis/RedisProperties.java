package org.springframework.boot.autoconfigure.data.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
