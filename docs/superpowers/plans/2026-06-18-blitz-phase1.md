# Phase 1: 基础骨架 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 搭建 Blitz 项目完整骨架——所有模块可编译、启动类可运行、数据库表就绪、Docker Compose 一键启动开发环境。

**Architecture:** Maven 多模块，父 POM 管理版本，框架模块（common/data/security）+ 业务模块（product/order/user/seckill/ai）+ 启动模块（server）。

**Tech Stack:** Java 21, Spring Boot 4.0, MyBatis-Plus 3.5.10, Spring Security 6.x, JJWT 0.12.6, Druid 1.2.24, Flyway, Docker Compose

## Global Constraints

- Java 21 with preview features enabled
- Package: com.blitz.*
- All modules compile with `mvn clean install -DskipTests`
- Port 8080 for server
- MySQL 8.0, Redis 7.2, Kafka 3.6 available via Docker Compose

---

### Task 1: 补全缺失模块 POM

**Files:**
- Create: `C:/watasinokoto/blitz/blitz-user/pom.xml`
- Create: `C:/watasinokoto/blitz/blitz-admin/pom.xml`

**Interfaces:**
- Consumes: Parent POM `com.blitz:blitz:1.0.0-SNAPSHOT`
- Produces: Two compilable Maven modules

- [ ] **Step 1: Write blitz-user/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.blitz</groupId>
        <artifactId>blitz</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>blitz-user</artifactId>
    <packaging>jar</packaging>
    <description>用户限界上下文——注册、登录、RBAC 权限</description>

    <dependencies>
        <dependency>
            <groupId>com.blitz</groupId>
            <artifactId>blitz-common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.blitz</groupId>
            <artifactId>blitz-data</artifactId>
        </dependency>
        <dependency>
            <groupId>com.blitz</groupId>
            <artifactId>blitz-security</artifactId>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 2: Write blitz-admin/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.blitz</groupId>
        <artifactId>blitz</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>blitz-admin</artifactId>
    <packaging>jar</packaging>
    <description>Blitz 服务器——单体启动器</description>

    <dependencies>
        <dependency>
            <groupId>com.blitz</groupId>
            <artifactId>blitz-common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.blitz</groupId>
            <artifactId>blitz-data</artifactId>
        </dependency>
        <dependency>
            <groupId>com.blitz</groupId>
            <artifactId>blitz-security</artifactId>
        </dependency>
        <dependency>
            <groupId>com.blitz</groupId>
            <artifactId>blitz-user</artifactId>
        </dependency>
        <dependency>
            <groupId>com.blitz</groupId>
            <artifactId>blitz-product</artifactId>
        </dependency>
        <dependency>
            <groupId>com.blitz</groupId>
            <artifactId>blitz-order</artifactId>
        </dependency>
        <dependency>
            <groupId>com.blitz</groupId>
            <artifactId>blitz-seckill</artifactId>
        </dependency>
        <dependency>
            <groupId>com.blitz</groupId>
            <artifactId>blitz-ai</artifactId>
        </dependency>
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: 验证两个 POM 语法正确**

```bash
cd /c/watasinokoto/blitz
mvn validate -pl blitz-user,blitz-admin -q
```

Expected: BUILD SUCCESS

---

### Task 2: blitz-common 基础类

**Files:**
- Create: `blitz-common/src/main/java/com/blitz/common/result/Result.java`
- Create: `blitz-common/src/main/java/com/blitz/common/exception/ErrorCode.java`
- Create: `blitz-common/src/main/java/com/blitz/common/exception/BusinessException.java`
- Create: `blitz-common/src/main/java/com/blitz/common/exception/GlobalExceptionHandler.java`

**Interfaces:**
- Produces: `Result<T>` unified response, `ErrorCode` enum, `BusinessException`, `GlobalExceptionHandler`

- [ ] **Step 1: Write Result.java**

```java
package com.blitz.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(0, "success", data);
    }

    public static <T> Result<T> success() {
        return new Result<>(0, "success", null);
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> fail(String msg) {
        return new Result<>(-1, msg, null);
    }
}
```

- [ ] **Step 2: Write ErrorCode.java**

```java
package com.blitz.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // System errors 1xxx
    SYSTEM_ERROR(1000, "系统内部错误"),
    PARAM_INVALID(1001, "参数校验失败"),
    NOT_FOUND(1002, "资源不存在"),
    METHOD_NOT_ALLOWED(1003, "请求方法不允许"),

    // Auth errors 2xxx
    UNAUTHORIZED(2000, "未登录或 Token 已过期"),
    FORBIDDEN(2001, "无权限访问"),
    TOKEN_EXPIRED(2002, "Token 已过期"),
    LOGIN_FAILED(2003, "用户名或密码错误"),
    ACCOUNT_DISABLED(2004, "账号已被禁用"),

    // Business errors 3xxx
    STOCK_INSUFFICIENT(3000, "库存不足"),
    ORDER_NOT_FOUND(3001, "订单不存在"),
    SECKILL_OVER(3002, "秒杀已结束"),
    SECKILL_REPEAT(3003, "你已经参与过该秒杀"),
    PAY_FAILED(3004, "支付失败"),

    // AI errors 4xxx
    AI_SERVICE_ERROR(4000, "AI 服务暂时不可用"),
    KNOWLEDGE_BASE_EMPTY(4001, "知识库为空");

    private final int code;
    private final String msg;
}
```

- [ ] **Step 3: Write BusinessException.java**

```java
package com.blitz.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;
    private final String msg;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
        this.msg = errorCode.getMsg();
    }

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
```

- [ ] **Step 4: Write GlobalExceptionHandler.java**

```java
package com.blitz.common.exception;

import com.blitz.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("Business exception: code={}, msg={}", e.getCode(), e.getMsg());
        return Result.fail(e.getCode(), e.getMsg());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return Result.fail(ErrorCode.PARAM_INVALID.getCode(), msg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleUnknown(Exception e) {
        log.error("Unknown error", e);
        return Result.fail(ErrorCode.SYSTEM_ERROR.getCode(), "系统内部异常，请稍后重试");
    }
}
```

- [ ] **Step 5: 验证编译**

```bash
cd /c/watasinokoto/blitz
mvn compile -pl blitz-common -q
```

Expected: BUILD SUCCESS

---

### Task 3: blitz-data 配置

**Files:**
- Create: `blitz-data/src/main/java/com/blitz/data/config/MyBatisPlusConfig.java`
- Create: `blitz-data/src/main/java/com/blitz/data/base/BaseEntity.java`
- Create: `blitz-data/src/main/resources/application-data.yml`

**Interfaces:**
- Produces: MyBatis-Plus pagination plugin, MetaObjectHandler auto-fill, BaseEntity base class

- [ ] **Step 1: Write BaseEntity.java**

```java
package com.blitz.data.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public abstract class BaseEntity {

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
```

- [ ] **Step 2: Write MyBatisPlusConfig.java**

```java
package com.blitz.data.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
```

- [ ] **Step 3: Write application-data.yml**

```yaml
# blitz-data default config — overridden by blitz-server per schema
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      test-while-idle: true
      validation-query: SELECT 1

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

- [ ] **Step 4: 验证编译**

```bash
cd /c/watasinokoto/blitz
mvn compile -pl blitz-data -q
```

Expected: BUILD SUCCESS

---

### Task 4: blitz-security 配置

**Files:**
- Create: `blitz-security/src/main/java/com/blitz/security/util/JwtUtil.java`
- Create: `blitz-security/src/main/java/com/blitz/security/filter/JwtAuthFilter.java`
- Create: `blitz-security/src/main/java/com/blitz/security/config/SecurityConfig.java`
- Create: `blitz-security/src/main/java/com/blitz/security/annotation/RequireRole.java`
- Create: `blitz-security/src/main/java/com/blitz/security/aspect/RequireRoleAspect.java`

**Interfaces:**
- Produces: JwtUtil (create/parse/validate token), JwtAuthFilter (OncePerRequestFilter), SecurityConfig, @RequireRole annotation

- [ ] **Step 1: Write JwtUtil.java**

```java
package com.blitz.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiration;

    public JwtUtil(
            @Value("${blitz.jwt.secret:Blitz2026SecretKeyForJWTTokenGenerationAtLeast256Bits}") String secret,
            @Value("${blitz.jwt.expiration:86400000}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String createToken(Long userId, String username, String role) {
        return Jwts.builder()
                .claims(Map.of("userId", userId, "username", username, "role", role))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }
}
```

- [ ] **Step 2: Write JwtAuthFilter.java**

```java
package com.blitz.security.filter;

import com.blitz.security.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (StringUtils.hasText(token) && !jwtUtil.isTokenExpired(token)) {
            Claims claims = jwtUtil.parseToken(token);
            String role = claims.get("role", String.class);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    claims.get("userId"), null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role)));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
```

- [ ] **Step 3: Write SecurityConfig.java**

```java
package com.blitz.security.config;

import com.blitz.security.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/user/login", "/user/register", "/doc.html", "/webjars/**",
                                "/v3/api-docs/**", "/actuator/health", "/api/health").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 4: Write @RequireRole annotation**

```java
package com.blitz.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    String[] value() default {};
}
```

- [ ] **Step 5: Write RequireRoleAspect.java**

```java
package com.blitz.security.aspect;

import com.blitz.common.exception.BusinessException;
import com.blitz.common.exception.ErrorCode;
import com.blitz.security.annotation.RequireRole;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class RequireRoleAspect {

    private final HttpServletRequest request;

    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {
        String role = (String) request.getAttribute("role");
        if (role == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (requireRole.value().length > 0 && !Arrays.asList(requireRole.value()).contains(role)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
```

- [ ] **Step 6: 验证编译**

```bash
cd /c/watasinokoto/blitz
mvn compile -pl blitz-security -q
```

Expected: BUILD SUCCESS

---

### Task 5: blitz-server 启动模块

**Files:**
- Create: `blitz-server/src/main/java/com/blitz/BlitzApplication.java`
- Create: `blitz-server/src/main/java/com/blitz/controller/HealthController.java`
- Create: `blitz-server/src/main/resources/application.yml`
- Create: `blitz-server/src/main/resources/application-dev.yml`
- Create: `docker-compose.yml` (项目根目录)

**Interfaces:**
- Produces: Runnable Spring Boot application, health check endpoint, Docker Compose dev environment

- [ ] **Step 1: Write BlitzApplication.java**

```java
package com.blitz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.blitz")
public class BlitzApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlitzApplication.class, args);
    }
}
```

- [ ] **Step 2: Write HealthController.java**

```java
package com.blitz.controller;

import com.blitz.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Result<Map<String, Object>> health() {
        return Result.success(Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "app", "Blitz 智能商城",
                "version", "1.0.0"
        ));
    }
}
```

- [ ] **Step 3: Write application.yml**

```yaml
server:
  port: 8080

spring:
  application:
    name: blitz
  profiles:
    active: dev
  threads:
    virtual:
      enabled: true
```

- [ ] **Step 4: Write application-dev.yml**

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blitz_product?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      filter:
        stat:
          enabled: true
        wall:
          enabled: true
      stat-view-servlet:
        enabled: false
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    schemas:
      - blitz_product
      - blitz_order
      - blitz_user
      - blitz_seckill
      - blitz_ai
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: blitz
      auto-offset-reset: earliest
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

  data:
    redis:
      host: localhost
      port: 6379

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

blitz:
  jwt:
    secret: Blitz2026SecretKeyForJWTTokenGenerationAtLeast256Bits
    expiration: 86400000

springdoc:
  swagger-ui:
    path: /doc.html
  api-docs:
    path: /v3/api-docs

logging:
  level:
    com.blitz: debug
    org.springframework.security: warn
```

- [ ] **Step 5: Write docker-compose.yml (project root)**

```yaml
version: "3.9"
services:
  mysql:
    image: mysql:8.0
    container_name: blitz-mysql
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root
    volumes:
      - ./docker/mysql/init:/docker-entrypoint-initdb.d
      - blitz-mysql-data:/var/lib/mysql
    command: --default-authentication-plugin=mysql_native_password
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 5s
      timeout: 3s
      retries: 10

  redis:
    image: redis:7.2-alpine
    container_name: blitz-redis
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 3s
      retries: 10

  kafka:
    image: bitnami/kafka:3.6
    container_name: blitz-kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_CFG_NODE_ID: 1
      KAFKA_CFG_PROCESS_ROLES: broker,controller
      KAFKA_CFG_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093
      KAFKA_CFG_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
      KAFKA_CFG_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_CFG_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT

  postgres:
    image: pgvector/pgvector:pg16
    container_name: blitz-postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_USER: blitz
      POSTGRES_PASSWORD: blitz
      POSTGRES_DB: blitz_ai
    volumes:
      - blitz-pgdata:/var/lib/postgresql/data

volumes:
  blitz-mysql-data:
  blitz-pgdata:
```

---

### Task 6: 数据库建表 SQL

**Files:**
- Create: `blitz-server/src/main/resources/db/migration/V1__init_blitz_product.sql`
- Create: `blitz-server/src/main/resources/db/migration/V2__init_blitz_user.sql`
- Create: `blitz-server/src/main/resources/db/migration/V3__init_blitz_order.sql`
- Create: `blitz-server/src/main/resources/db/migration/V4__init_blitz_seckill.sql`
- Create: `blitz-server/src/main/resources/db/migration/V5__init_blitz_ai.sql`
- Create: `docker/mysql/init/01-create-schemas.sql`

**Interfaces:**
- Produces: All 5 schemas with 20 tables total, Flyway-managed

- [ ] **Step 1: Write 01-create-schemas.sql**

```sql
CREATE DATABASE IF NOT EXISTS blitz_product CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS blitz_user CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS blitz_order CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS blitz_seckill CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS blitz_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

- [ ] **Step 2: Write V1__init_blitz_product.sql**

```sql
CREATE TABLE IF NOT EXISTS product_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    level INT DEFAULT 1,
    sort_order INT DEFAULT 0,
    icon VARCHAR(255) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category_id BIGINT NOT NULL,
    brand VARCHAR(128) DEFAULT NULL,
    description TEXT,
    main_image VARCHAR(512) DEFAULT NULL,
    images JSON DEFAULT NULL,
    status INT DEFAULT 1 COMMENT '1-上架 0-下架',
    sale_count INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_category_id (category_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sku (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    spec JSON DEFAULT NULL COMMENT '规格: {"颜色":"黑色","尺寸":"42"}',
    price DECIMAL(10,2) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sku_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_id BIGINT NOT NULL UNIQUE,
    stock INT DEFAULT 0 COMMENT '可用库存',
    lock_stock INT DEFAULT 0 COMMENT '锁定库存(秒杀预扣)',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_attribute (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    value VARCHAR(255) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 3: Write V2__init_blitz_user.sql**

```sql
CREATE TABLE IF NOT EXISTS role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE,
    permissions JSON DEFAULT NULL COMMENT '["product:read","product:write"]',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO role (name, permissions) VALUES
('ADMIN', '["*:*"]'),
('USER', '["order:read","order:write","product:read"]')
ON DUPLICATE KEY UPDATE name=name;

CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(32) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    phone VARCHAR(16) DEFAULT NULL,
    email VARCHAR(64) DEFAULT NULL,
    avatar VARCHAR(512) DEFAULT NULL,
    role_id BIGINT DEFAULT 2,
    status INT DEFAULT 1 COMMENT '1-正常 0-禁用',
    last_login DATETIME DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_username (username),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(32) NOT NULL,
    receiver_phone VARCHAR(16) NOT NULL,
    province VARCHAR(32) NOT NULL,
    city VARCHAR(32) NOT NULL,
    district VARCHAR(32) DEFAULT NULL,
    detail VARCHAR(255) NOT NULL,
    is_default INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 4: Write V3__init_blitz_order.sql**

```sql
CREATE TABLE IF NOT EXISTS `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号(雪花ID)',
    user_id BIGINT NOT NULL,
    status INT DEFAULT 0 COMMENT '0-待支付 1-已支付 2-已发货 3-已收货 4-已取消 5-已退款',
    total_amount DECIMAL(10,2) NOT NULL,
    freight_amount DECIMAL(10,2) DEFAULT 0,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    payment_method INT DEFAULT NULL COMMENT '1-支付宝 2-微信',
    payment_time DATETIME DEFAULT NULL,
    delivery_time DATETIME DEFAULT NULL,
    receive_time DATETIME DEFAULT NULL,
    cancel_time DATETIME DEFAULT NULL,
    receiver_name VARCHAR(32) DEFAULT NULL,
    receiver_phone VARCHAR(16) DEFAULT NULL,
    receiver_address VARCHAR(255) DEFAULT NULL,
    note VARCHAR(512) DEFAULT NULL,
    version INT DEFAULT 0 COMMENT '乐观锁',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    sku_name VARCHAR(255) NOT NULL,
    sku_spec JSON DEFAULT NULL,
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    event_type VARCHAR(32) NOT NULL COMMENT 'ORDER_CREATED,ORDER_PAID,ORDER_SHIPPED,ORDER_CANCELLED',
    event_data JSON DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_event_type (event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_operate_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    operate_type VARCHAR(32) NOT NULL,
    operator VARCHAR(64) DEFAULT NULL,
    note VARCHAR(512) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 5: Write V4__init_blitz_seckill.sql**

```sql
CREATE TABLE IF NOT EXISTS seckill_activity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status INT DEFAULT 0 COMMENT '0-未开始 1-进行中 2-已结束',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_start_end (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS seckill_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    activity_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    seckill_price DECIMAL(10,2) NOT NULL,
    seckill_stock INT NOT NULL COMMENT '秒杀总库存',
    limit_per_user INT DEFAULT 1 COMMENT '每人限购',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_activity_id (activity_id),
    INDEX idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS seckill_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    seckill_product_id BIGINT NOT NULL,
    status INT DEFAULT 1 COMMENT '1-已创建 2-已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_seckill (user_id, seckill_product_id),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS seckill_stock_flow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seckill_product_id BIGINT NOT NULL,
    delta INT NOT NULL COMMENT '变化量(-1表示扣减)',
    source VARCHAR(32) NOT NULL COMMENT 'REDIS_PRE_DECR, MYSQL_CONFIRM, COMPENSATE_ROLLBACK',
    order_no VARCHAR(32) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_seckill_product (seckill_product_id),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 6: Write V5__init_blitz_ai.sql**

```sql
CREATE TABLE IF NOT EXISTS kb_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content LONGTEXT NOT NULL,
    file_type VARCHAR(16) DEFAULT 'TXT',
    status VARCHAR(16) DEFAULT 'PENDING' COMMENT 'PENDING, VECTORIZING, VECTORIZED, FAILED',
    chunk_count INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS kb_chunk (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    chunk_text TEXT NOT NULL,
    chunk_index INT DEFAULT 0,
    vector_id VARCHAR(64) DEFAULT NULL COMMENT 'pgvector 关联ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_document_id (document_id),
    INDEX idx_vector_id (vector_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT DEFAULT NULL,
    title VARCHAR(255) DEFAULT '新对话',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    role VARCHAR(16) NOT NULL COMMENT 'USER / AI',
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

### Task 7: Maven 编译验证 + Docker 启动验证

**Files:** None new.

**Interfaces:** None.

- [ ] **Step 1: 全量编译**

```bash
cd /c/watasinokoto/blitz
mvn clean install -DskipTests
```

Expected: `BUILD SUCCESS` — all 9 modules compile and install to local repo.

- [ ] **Step 2: 启动 Docker 环境**

```bash
cd /c/watasinokoto/blitz
docker compose up -d
```

Expected: 4 containers running (mysql, redis, kafka, postgres).

- [ ] **Step 3: 等待 MySQL 就绪后启动应用**

```bash
cd /c/watasinokoto/blitz
mvn spring-boot:run -pl blitz-server
```

Expected: Application starts on port 8080.

- [ ] **Step 4: 健康检查**

```bash
curl http://localhost:8080/api/health
```

Expected: `{"code":0,"msg":"success","data":{"status":"UP","app":"Blitz 智能商城","version":"1.0.0"}}`

- [ ] **Step 5: Commit**

```bash
cd /c/watasinokoto/blitz
git init
git add -A
git commit -m "feat: Phase 1 complete — Blitz project skeleton with 9 modules, DDD layers, JWT auth, Flyway schemas, Docker Compose"
```
