package org.springframework.boot.autoconfigure.jdbc;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceProperties {
    private String url, username, password, driverClassName;
    private ClassLoader classLoader;

    public String getUrl() { return url; }
    public void setUrl(String u) { url = u; }
    public String getUsername() { return username; }
    public void setUsername(String u) { username = u; }
    public String getPassword() { return password; }
    public void setPassword(String p) { password = p; }
    public String getDriverClassName() { return driverClassName; }
    public void setDriverClassName(String d) { driverClassName = d; }
    public String determineDriverClassName() { return driverClassName != null ? driverClassName : "com.mysql.cj.jdbc.Driver"; }
    public String determineUrl() { return url; }
    public String determineUsername() { return username; }
    public String determinePassword() { return password; }
    public ClassLoader getClassLoader() { return classLoader; }
    public void setClassLoader(ClassLoader c) { classLoader = c; }
}
