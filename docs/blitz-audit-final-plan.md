# Blitz 智能商城 — 全面审查报告 & 修复计划（最终版）

> **审计来源**：管家 Claude Code（终端侧）+ 星云天空 Hermes（服务器侧）
> **审计范围**：服务器部署代码（119.28.151.191:/home/ubuntu/blitz）+ 本地源码
> **审计日期**：2026-06-19
> **版本**：v1.0 Final

---

## 📊 总览

| 级别 | 数量 | 说明 |
|------|------|------|
| 🔴 严重（P0） | **13** | 安全漏洞 + 功能损坏 + 数据一致性 |
| 🟡 警告（P1） | **12** | 安全风险 + 功能缺陷 |
| 🟢 建议（P2） | **8** | 代码质量 + 技术增强 |
| **合计** | **33** | |

---

## 🔴 P0 — 严重问题（13 个）

### 安全漏洞（6 个）

| # | 问题 | 位置 | 影响 | 来源 |
|---|------|------|------|------|
| **S1** | Docker 端口全暴露在 0.0.0.0 | docker-compose / 容器实际绑定 | MySQL/Redis/Kafka/PG 公网可访问 | 管家 |
| **S2** | Redis 无密码认证 | `.env` REDIS_PASSWORD=空 | 公网可读写 Redis，含秒杀库存 | 管家 |
| **S3** | 订单查询无用户归属校验 → 水平越权 | `OrderController.java:40-48` | 任意用户可遍历查看所有订单（含地址/电话）🔥 | Hermes |
| **S4** | Swagger API 文档公开暴露 | `SecurityConfig.java:35-39` | `/doc.html` + `/v3/api-docs` 无需认证 | Hermes |
| **S5** | 秒杀路径 HMAC 密钥硬编码默认值 | `SeckillPathService.java:26` | 源码泄露即可伪造秒杀路径 | Hermes |
| **S6** | 硬编码凭证（4 处） | YML 配置文件 | MySQL/Redis/PG/JWT 密钥明文 | 双方 |

### 数据一致性（3 个）

| # | 问题 | 位置 | 影响 | 来源 |
|---|------|------|------|------|
| **D1** | Kafka 异步发送 → 库存丢失 | `SeckillApplicationService.java:95-101` | `send()` 立即返回，发送失败不回滚 Redis → 用户扣了库存但订单丢失 🔥 | Hermes |
| **D2** | 秒杀防重与库存扣减非原子 | `SeckillOrderConsumer.java:38-64` | 查重 + 扣库存两步之间有竞态窗口 | Hermes |
| **D3** | 库存恢复非原子操作 | `StockDeductionService.java:65-76` | 恢复用 read-modify-write 无乐观锁，并发取消互相覆盖 | Hermes |

### 功能损坏（4 个）

| # | 问题 | 位置 | 影响 | 来源 |
|---|------|------|------|------|
| **F1** | 前端认证守卫拦截所有页面 | `common.js` PUBLIC_PAGES 白名单不全 | 商城首页/商品/秒杀全部不可浏览，必须登录 | 管家 |
| **F2** | AI 功能完全不可用 | AI 模块无 Controller + API Key = `sk-disabled` | RAG 问答/SSE 流式输出/混合检索全部无法演示 | 管家 |
| **F3** | WebSocket 秒杀结果推送无法工作 | `SeckillWebSocketHandler.java` 认证缺失 | userId 永远为 null，秒杀结果无法推送 | 管家 |
| **F4** | Seata 错误日志风暴（1773 ERROR） | Seata Netty 客户端持续重连 | 日志噪音+CPU 空转，每 10 秒 2 条 ERROR | 管家 |

---

## 🟡 P1 — 警告（12 个）

| # | 问题 | 位置 | 建议 | 来源 |
|---|------|------|------|------|
| **W1** | JWT 异常未处理 → HTTP 500 堆栈泄露 | `JwtAuthFilter.java:31` | try-catch 包裹 parseToken | Hermes |
| **W2** | IP 限流可被 Header 伪造绕过 | `RateLimitFilter.java:156-163` | 信任 X-Forwarded-For 但无受信代理白名单 | Hermes |
| **W3** | 登录异常捕获过宽 → 误锁合法用户 | `UserController.java:48` | `catch(Exception)` → `catch(BusinessException)` | Hermes |
| **W4** | getCurrentUserId() 无 null 保护 | 4 个 Controller | Authentication 为 null → HTTP 500 NPE | Hermes |
| **W5** | Spring Boot 版本混乱 | POM 4.0.0 vs JAR 3.4.5 vs Java 21 | Shutdown 时 NoClassDefFoundError | 管家 |
| **W6** | Flyway 迁移未执行 | `flyway_schema_history` 不存在 | 20 张表手动创建，版本管理失效 | 管家 |
| **W7** | 所有表在单一 blitz 库 | 实际 vs 设计（5 schema） | 与 DDD 限界上下文设计矛盾 | 管家 |
| **W8** | nginx 安全加固未完成 | TLSv1 + server_tokens on | 安全基线不达标 | 管家 |
| **W9** | CORS `allowedHeaders("*")` + `allowCredentials(true)` | `SecurityConfig.java:69` | 限定具体 header | Hermes |
| **W10** | XSS 正则过滤不可靠 | `XssFilter.java:67-70` | 应依赖 CSP + 输出编码 | Hermes |
| **W11** | LoginAttemptService 无过期清理 | ConcurrentHashMap 无限增长 | 内存泄漏 | Hermes |
| **W12** | run.sh 暴力 kill（无优雅关闭） | `run.sh` | 每次重启 shutdown 异常 | 管家 |

---

## 🟢 P2 — 建议（8 个）

| # | 问题 | 建议 | 来源 |
|---|------|------|------|
| G1 | `favicon.ico` 缺失 → 每次请求 ERROR 日志 | 添加文件或排除异常 | 管家 |
| G2 | DDD 充血模型被绕过 | Consumer 直接调 Repository 而非领域对象方法 | 管家 |
| G3 | `queryResult()` 只返回 SOLD_OUT/UNKNOWN | 应查询订单实际状态 | Hermes |
| G4 | `OrderEvent` JSON 拼字符串 | 改用 ObjectMapper/Jackson | Hermes |
| G5 | MySQL LIKE 全文搜索效率低 | 迁移到 Elasticsearch | Hermes |
| G6 | Sentinel 限流规则硬编码 | 改为配置项 | Hermes |
| G7 | `rollbackStock` 用 INCR 未检查售罄标记 | 售罄后回滚可能错误重置状态 | Hermes |
| G8 | 无 favicon 错误被记录为 ERROR 级别 | 降级为 WARN 或排除 | 管家 |

---

## 📋 修复计划（4 个 Phase）

### Phase 1 — 止血（1-2 天）

**目标**：消除安全漏洞，恢复基本功能

| 优先级 | 任务 | 修复内容 |
|--------|------|----------|
| 1 | **S1+S2** | `docker-compose down && docker-compose up -d` 重建所有容器 |
| | | 修复 `.env` 中 REDIS_PASSWORD=BlitzRedis2026! |
| | | 重建后确认端口绑定 127.0.0.1 |
| 2 | **F1** | `common.js` PUBLIC_PAGES 增加 `/`, `index.html`, `products.html`, `detail.html`, `seckill.html` |
| 3 | **S3** | `OrderController.detail()` 增加 userId 校验 |
| 4 | **S4** | 从 PUBLIC_PATHS 移除 `/doc.html`, `/v3/api-docs`，加 `@Profile("dev")` |
| 5 | **S5+S6** | 移除所有 YML 中的默认密钥，全部改为 `${ENV_VAR}`（启动时校验非空） |
| 6 | **S5** | `SeckillPathService` 移除 HMAC 默认值，改为纯环境变量注入 |

### Phase 2 — 修核心（3-5 天）

**目标**：修复数据一致性 + 功能缺陷

| 优先级 | 任务 | 修复内容 |
|--------|------|----------|
| 7 | **D1** | `orderProducer.send()` → `send().get(3, SECONDS)` 同步等待确认 |
| | | 或者改为 transactional outbox（Kafka 发送前先写 outbox 表） |
| 8 | **D2** | 秒杀消费者利用 MySQL UNIQUE KEY 直接 INSERT，失败回滚库存 |
| 9 | **D3** | `restoreStock()` 改为 CAS UPDATE |
| 10 | **F2** | (a) 补全 AI ChatController（SSE 流式端点 + 历史查询） |
| | | (b) `.env` 中配置 `DEEPSEEK_API_KEY` |
| | | (c) 验证 RAG 完整流程可用 |
| 11 | **F3** | WebSocket 握手时从 JWT Token 提取 userId（改为从请求参数 `?token=xxx` 获取） |
| 12 | **F4** | (a) 排除所有 Seata 相关 AutoConfiguration |
| | | (b) 或配置最小 Seata（如不需要分布式事务就完全移除依赖） |
| 13 | **W1-W4** | JWT 异常处理 + IP 限流加固 + 登录异常精准捕获 + getCurrentUserId null 保护 |
| 14 | **W5** | POM 统一：Spring Boot 3.4.5 + Java 21，移除 `--enable-preview` |
| 15 | **W6** | 修复 Flyway：统一 JDBC URL + Schema，重新初始化 baseline |

### Phase 3 — 技术增强（1-2 周）

**目标**：为答辩/面试增加技术亮点

| 优先级 | 技术 | 内容 |
|--------|------|------|
| 16 | **Elasticsearch** | 商品搜索引擎，替代 MySQL ILIKE，支持中文分词（IK Analyzer） |
| 17 | **Nacos** | 配置中心 + 服务注册，动态刷新配置无需重启 |
| 18 | **Prometheus + Grafana** | JVM/MySQL/Redis/Kafka 监控大盘，QPS/RT 可视化 |
| 19 | **MinIO** | 商品图片上传 + 对象存储，替代 emoji 占位图 |
| 20 | **XXL-Job** | 分布式定时任务：超时订单自动取消、秒杀活动自动开始/结束 |

### Phase 4 — 深度增强（可选，2-4 周）

| 优先级 | 技术 | 内容 |
|--------|------|------|
| 21 | **Spring Cloud Gateway** | 统一 API 网关，路由/限流/熔断 |
| 22 | **CQRS 读写分离** | 订单查询走 Elasticsearch 读模型，命令走 MySQL |
| 23 | **Canal + Binlog** | MySQL → ES/缓存数据同步 |
| 24 | **推荐引擎** | 协同过滤 + 基于内容推荐（用户行为 → 商品推荐） |
| 25 | **k3s / Kubernetes** | 轻量级容器编排，滚动更新、自动伸缩 |

---

## 🔑 配置安全整改清单

所有以下配置必须移到环境变量（`.env`），YML 中不留任何默认值：

| 配置项 | 当前状态 | 目标 |
|--------|----------|------|
| `blitz.jwt.secret` | 有默认值（minimal profile） | `${BLITZ_JWT_SECRET}` 无默认值 |
| `spring.datasource.password` | 硬编码 `root` | `${MYSQL_PASSWORD}` |
| `REDIS_PASSWORD` | 空字符串 | `BlitzRedis2026!`（通过 .env） |
| `spring.ai.openai.api-key` | `sk-disabled` | `${DEEPSEEK_API_KEY}` |
| `blitz.seckill.path-secret` | 硬编码默认值 | `${BLITZ_SECKILL_PATH_SECRET}` 无默认值 |
| PostgreSQL 密码 | 硬编码 `blitz` | `${POSTGRES_PASSWORD}` |

---

## 📈 修复进度追踪

```
Phase 1 ░░░░░░░░░░░░ 0/6   ← 建议立即开始
Phase 2 ░░░░░░░░░░░░ 0/9
Phase 3 ░░░░░░░░░░░░ 0/5
Phase 4 ░░░░░░░░░░░░ 0/5
        ─────────
总计    0/25
```

---

> **管家 + 星云天空 联合出具**
> 2026-06-19 · 版本 Final v1.0
