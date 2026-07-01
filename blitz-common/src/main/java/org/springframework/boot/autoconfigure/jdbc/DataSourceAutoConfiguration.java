package org.springframework.boot.autoconfigure.jdbc;

/**
 * Spring Boot 4.0 compatibility shim.
 * <p>
 * Spring Boot 4.0 moved DataSourceAutoConfiguration to a new module/path,
 * but MyBatis-Plus 3.5.8 and Druid 1.2.24 still reference the old path.
 * This dummy class satisfies the class reference so those libraries can load.
 */
public class DataSourceAutoConfiguration {
}
