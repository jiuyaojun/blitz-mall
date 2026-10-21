# Blitz 智能商城

> **AI-Integrated High-Concurrency E-Commerce Platform**  
> Java 17 + Spring Boot 4.0 + Spring AI 2.0 -- Blitz Smart Mall
[![CI](https://github.com/jiuyaojun/blitz-mall/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/jiuyaojun/blitz-mall/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-17-%23ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-%236DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0.0-%236DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-ai)
[![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.8-%23C70039)](https://baomidou.com/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-%234479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.2-%23DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![Kafka](https://img.shields.io/badge/Kafka-3.6-%23231F20?logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16+pgvector-%234169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-%232496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

---

## 1. Architecture Overview

```
                         +-----------------------------+
                         |       Blitz Server           |
                         |  (Monolithic Entry Point)    |
                         |  Spring Boot 4.0 + Virtual   |
                         |  Threads                     |
                         +---------+-------+-----------+
                                   |       |
              ┌────────────────────┘       └────────────────────┐
              v                                                  v
    +---------+---------+                          +-------------+-------------+
    |   API Layer       |                          |   Security Layer          |
    |   REST + SSE      |                          |   JWT + @RequireRole      |
    |   Knife4j /doc    |                          |   BCrypt + JJWT           |
    +---------+---------+                          +-------------+-------------+
              |                                                  |
              v                                                  v
    +---------+---------+        +---------+--------+  +---------+----------+
    |   User (DDD)      |        | Product (DDD)     |  |  Order (DDD)       |
    |   Reg/Login/Addr   |        | SPU/SKU/Category  |  |  State Machine     |
    |   JWT + BCrypt     |        | MySQL LIKE Search |  |  Kafka Events      |
    +---------+---------+        +---------+---------+  |  Seata AT          |
              |                          |              +---------+----------+
              v                          v                        |
    +---------+---------+        +---------+---------+           |
    |   blitz-common    |<------>|   blitz-data       |<---------+
    |   Result<T>       |        |   BaseEntity       |
    |   ErrorCode       |        |   MyBatis-Plus     |
    |   PageResult      |        |   Auto-fill+SD     |
    +-------------------+        +-------------------+
              |                          |
              v                          v
    +---------+-------------------------+---------+
    |              Middleware Layer                |
    |  MySQL 8.0  |  Redis 7.2  |  Kafka 3.6     |
    |  Redisson   |  Sentinel    |  Seata AT      |
    +---------+-------------------------+---------+
              |                          |
              v                          v
    +---------+---------+        +---------+----------+
    |  Seckill (DDD)    |        |    AI (DDD)         |
    |  8-step Chain     |        |    RAG 10-step      |
    |  Lua Atomic Deduct|        |   Hybrid Retrieval  |
    |  Kafka Async Ord. |        |   pgvector + ILIKE  |
    |  WebSocket Push   |        |   ChatClient SSE    |
    +-------------------+        +---------------------+
              |                          |
              v                          v
    +---------+---------+        +---------+----------+
    |      Redis         |        |   PostgreSQL 16    |
    |  Stock Cache       |        |   pgvector         |
    |  User Limit        |        |   Embedding Store  |
    |  Sold-out Flag     |        |   Chat History     |
    +-------------------+        +--------------------+
```

> ⚠️ **关于上面这张架构图**：它画的是**完整设计的中间件层**，不是当前运行的形态。
> 实际以 `application-minimal.yml` 的 `autoconfigure.exclude` 为准 ——
> 当前 **Sentinel、Seata、Redisson、Spring AI、PgVector 均未启用**，
> Kafka 链路在 minimal profile 下也是断开的（`go.sh` 把 `OrderEventPublisher`
> 改成了可空以容忍缺失）。
>
> 默认的 `minimal` profile 还会**整段关闭两条核心链路的 Controller/Service**：
> `SeckillController`、`SeckillApplicationService`、`ChatController`、
> `ChatApplicationService` 都标注了 `@Profile("!minimal")`。因此默认启动后：
> `/seckill/**` 不存在；`/ai/chat/stream` 由 `SimpleAiController` 提供
> （无 API key 时是规则兜底，不是 RAG）；完整 RAG 与 Lua/Kafka 秒杀链路只在
> `!minimal` profile 且 Redis/Kafka/PgVector 都可用时才成立。
>
> 判断某个能力能不能用，请以配置和代码为准，不要以架构图为依据。

---

## 2. Key Features

### 2.1 Flash Sale (Seckill) -- 8-Step Chain

| Step | Layer | Action | Tech |
|------|-------|--------|------|
| 1 | Client | Request dynamic path from server | REST |
| 2 | Server | HMAC-signed path generation + validation | HMAC-SHA256 |
| 3 | Server | Sentinel rate limiting at API level | Sentinel（⚠️ 规则已配，但全工程无 `@SentinelResource` 切点，实际不生效） |
| 4 | Server | Redisson per-user rate limit (1 QPS) | Redisson |
| 5 | Server | Redis fast-fail sold-out check | Redis GET |
| 6 | Server | **Lua atomic pre-deduct stock + duplicate prevention** | Redis Lua |
| 7 | Server | Generate Snowflake orderNo + send Kafka message | Kafka Producer |
| 8 | Consumer | CAS optimistic-lock confirm + WebSocket push result | Kafka Consumer + WebSocket |

**Lua script handles 4 states atomically:**
- `>= 0` -- deduction success (returns remaining stock)
- `-1` -- sold out
- `-2` -- repeat purchase detected
- `-3` -- stock exhausted mid-deduction (rolls back DECR)

### 2.2 AI Q&A (RAG) -- 10-Step Pipeline

| Step | Layer | Action | Tech |
|------|-------|--------|------|
| 1 | API | Accept user question via REST | Spring MVC |
| 2 | App | Save user message to history | MyBatis-Plus |
| 3 | Infra | **Vector retrieval** (pgvector cosine similarity) | PgVectorStore |
| 4 | Infra | **Keyword retrieval** (PostgreSQL ILIKE) | JDBC |
| 5 | Infra | **RRF (Reciprocal Rank Fusion)** merge top-K | In-memory |
| 6 | Infra | Fetch top-5 chunk content from DB | MyBatis-Plus |
| 7 | App | Load conversation history (last 10 turns) | MyBatis-Plus |
| 8 | Infra | Render prompt with `rag-system.st` template | Spring AI PromptTemplate |
| 9 | Infra | **ChatClient streaming** (model generates token-by-token) | Spring AI ChatClient |
| 10 | API | **SSE output** + save AI reply + auto-title session | SseEmitter |

**Document ingestion pipeline:** Upload → parse (TXT/MD) → split chunks (overlap 50) → embed (text-embedding-3-small) → store in pgvector.

---

## 3. Tech Stack

| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| **Language** | Java | 17 | 见 pom.xml 的 java.version；虚拟线程配置在 17 下不生效 |
| **Framework** | Spring Boot | 4.0.0 | Application framework |
| **AI** | Spring AI | 2.0.0 | ChatClient, VectorStore, Embedding |
| **Security** | Spring Security + JJWT | 6.x / 0.12.6 | JWT stateless auth |
| **ORM** | MyBatis-Plus | 3.5.8 | Code-first CRUD |
| **RDBMS** | MySQL | 8.0 | Primary business data |
| **Vector DB** | PostgreSQL + pgvector | 16 | AI embeddings + hybrid search |
| **Cache** | Redis + Redisson | 7.2 / 3.42 | Distributed lock, Lua, rate limit |
| **MQ** | Apache Kafka | 3.6 broker / 3.9.0 client | Async order events |
| **Flow Control** | Sentinel（未启用） | 2025.0.0.0 | 规则已配置，但全工程无 `@SentinelResource` 切点，限流不生效 |
| **Dist. TX** | Seata（未启用） | 2025.0.0.0 | 全 profile `seata.enabled: false`，无 `@GlobalTransactional` |
| **Migration** | Flyway | (Spring Boot managed) | Schema versioning |
| **API Docs** | Knife4j | 4.5.0 | OpenAPI 3 UI at /doc.html |
| **Utilities** | Hutool | 5.8.35 | All-in-one toolset |
| **Mapping** | MapStruct | 1.6.3 | Compile-time DTO mapping |
| **Connection Pool** | Druid + HikariCP | 1.2.24 | MySQL (Druid) + PG (Hikari) |
| **Container** | Docker Compose | v2 (no `version:` pin) | Infrastructure orchestration |
| **Build** | Maven | 3.x | Multi-module build |

---

## 4. Module Structure

```
blitz/                           (parent POM, version 1.0.0-SNAPSHOT)
├── blitz-common/               Shared: Result<T>, ErrorCode, BusinessException, PageResult
├── blitz-data/                 Persistence: MyBatis-Plus config, BaseEntity (auto-fill + soft-delete)
├── blitz-security/             Auth: JWT filter, Spring Security config, @RequireRole + Aspect
├── blitz-user/                 User bounded context -- DDD 4-layer
├── blitz-product/              Product bounded context -- DDD 4-layer
├── blitz-order/                Order bounded context -- DDD 4-layer
├── blitz-seckill/   ★          Seckill bounded context -- DDD 4-layer + Lua + Kafka + WebSocket
├── blitz-ai/        ★          AI bounded context -- DDD 4-layer + pgvector + RAG + SSE
├── blitz-server/               Monolithic startup: BlitzApplication + Flyway migrations
                                [planned, no directory in this repo] blitz-gateway
```

> 注：`blitz-server` 实际还包含 `merchant/`、`review/`（Review / Coupon / Refund /
> Shop / Filter / Admin / SimpleAi）等控制器；上面的树只列模块级目录，以
> `src/main/java` 下的实际文件为准。

### DDD 4-Layer Pattern (per bounded context)

```
src/main/java/com/blitz/<context>/
├── api/
│   ├── controller/    REST endpoints
│   └── dto/           Request/Response/VO objects
├── domain/
│   ├── model/         充血模型 (Rich Domain Model) with behavior methods
│   └── port/          Repository interfaces (Port)
├── application/
│   └── service/       Application services (orchestration)
└── infrastructure/
    ├── mapper/        MyBatis-Plus Mapper interfaces
    ├── repository/    Repository (Port) implementations
    ├── config/        Module-specific Spring config
    ├── kafka/         [seckill/order] Kafka producer/consumer
    ├── redis/         [seckill] Redis Lua executor
    ├── websocket/     [seckill] WebSocket push handler
    ├── llm/           [ai] ChatClient streaming service
    └── rag/           [ai] Embedding, keyword, RRF fusion, hybrid retrieval
```

---

## 5. Database Design

Flyway manages **one MySQL database** (`blitz`, configured in
`application-minimal.yml` / `application-dev.yml`) plus, only under the `ai`
profile, **one PostgreSQL database** (`blitz_ai`) for pgvector. Migrations
V1–V11 currently produce **23 MySQL tables** (V10 drops the unused
`product_attribute`):

| Database | Tables | Flyway Migration |
|----------|--------|------------------|
| MySQL `blitz` | product: `product_category`, `product`, `sku`, `sku_stock` (4) | V1 |
| MySQL `blitz` | user: `role`, `user`, `address` (3) | V2 |
| MySQL `blitz` | order: `order`, `order_item`, `order_event`, `order_operate_log` (4) | V3 |
| MySQL `blitz` | seckill: `seckill_activity`, `seckill_product`, `seckill_order`, `seckill_stock_flow` (4) | V4 |
| MySQL `blitz` | ai: `kb_document`, `kb_chunk`, `chat_session`, `chat_message` (4) | V5 |
| MySQL `blitz` | shop / review / coupon: `shop`, `blitz_review`, `coupon`, `user_coupon` (4) | V6–V9 |
| PostgreSQL `blitz_ai` | `vector_store` (pgvector extension) | Auto-managed by Spring AI; `!minimal` only |

**Architecture highlights:**
- Seckill read/write split: hot stock in Redis (Lua atomic pre-deduct), orders persisted in MySQL
- Repository Port pattern: domain defines interfaces, infrastructure implements
- Rich domain models: `Order.pay()`, `SeckillProduct.deductStock()`, `SeckillActivity.isInProgress()`
- Optimistic lock (`@Version`) on seckill product stock for MySQL CAS deduction
- Unique constraints (`uk_user_seckill`) for idempotent consumer deduplication

---

## 6. Quick Start

### Prerequisites

- JDK 17+
- Docker Desktop (or Docker Engine + Compose)
- Maven 3.8+
- OpenAI-compatible API key (for AI module)

### Step 1: Start Infrastructure

```bash
cd blitz
docker compose up -d
```

This starts:
- MySQL 8.0 on `:3306` (root/root)
- Redis 7.2 on `:6379`
- Kafka 3.6 on `:9092`
- PostgreSQL 16 + pgvector on `:5432` (blitz/blitz, database: blitz_ai)

> No Docker available? `mvn test` needs no middleware at all. For a UI-only
> local demo, a single MySQL 8.0 database named `blitz` is enough:
> run with `--spring.profiles.active=minimal`. In that profile `/seckill/**`
> and the RAG `/ai/**` implementation are not registered (see §1).

### Step 2: Build

```bash
# Normal build (with tests)
mvn clean install

# Development-only build (skip tests for faster iteration)
mvn clean install -DskipTests
```

### Step 3: Run

```bash
cd blitz-server
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Or via IDE: run `com.blitz.BlitzApplication` main class.

### Step 4: Verify

```bash
curl http://localhost:8080/api/health
# → {"code":0,"msg":"success","data":{"status":"UP","timestamp":"...","app":"Blitz 智能商城","version":"1.0.0"}}
```

Set the AI API key (required for RAG chat; `minimal` falls back to canned replies):

```bash
export OPENAI_API_KEY=sk-xxxxx
export OPENAI_BASE_URL=https://api.openai.com   # or your proxy
```

### Step 5: Explore APIs

Open **http://localhost:8080/doc.html** for Knife4j interactive API documentation.
`/doc.html` and `/v3/api-docs/**` are **not** in `PUBLIC_PATHS`; send a valid
`Authorization: Bearer <jwt>` header or log in first.

---

## 7. API Endpoints

| Module | Method | Path | Auth | Description |
|--------|--------|------|------|-------------|
| Health | GET | `/api/health` | No | Health check |
| User | POST | `/user/register` | No | Register (BCrypt) |
| User | POST | `/user/login` | No | Login → JWT |
| User | GET/POST | `/user/address/**` | Yes | Address CRUD |
| Product | GET | `/product/**` | No | SPU/SKU query (public read) |
| Product | GET | `/product/category/**` | No | Category tree |
| Order | POST | `/order` | Yes | Create order |
| Order | GET | `/order/**` | Yes | Order query, status |
| **Seckill** | GET | `/seckill/path` | Yes | Get HMAC dynamic path (`!minimal` only) |
| **Seckill** | POST | `/seckill/execute` | Yes | Execute seckill (`!minimal` only) |
| **Seckill** | GET | `/seckill/result` | Yes | Poll result (fallback, `!minimal` only) |
| **Seckill** | WS | `/ws/seckill` | Yes | WebSocket push (real-time, `!minimal` only) |
| Seckill | POST | `/seckill/admin/warmup/{id}` | ADMIN | Warmup stock to Redis (`!minimal` only) |
| **AI** | POST | `/ai/chat/stream` | Yes | RAG streaming chat (SSE, `!minimal`); canned fallback in `minimal` |
| AI | POST | `/ai/session` | Yes | Create chat session |
| AI | GET | `/ai/session/{id}/messages` | Yes | Get session history |
| AI | POST | `/ai/kb/document` | ADMIN | Upload knowledge document (`!minimal` only) |
| AI | DELETE | `/ai/kb/document/{id}` | ADMIN | Delete knowledge document (`!minimal` only) |

**Authentication:** Add `Authorization: Bearer <jwt_token>` header. Whitelisted paths (see `SecurityConfig.PUBLIC_PATHS`): `/`, `/*.html`, `/favicon.ico`, `/css/**`, `/js/**`, `/img/**`, `/fonts/**`, `/user/login`, `/user/register`, `/product/list`, `/product/category/**`, `/product/search`, `/product/*`, `/review/list/**`, `/review/stats/**`, `/shop/**`, `/coupon/official`, `/coupon/shop/**`, `/filter/**`, `/api/health`. `/doc.html` and `/v3/api-docs/**` are **not** public.

---

## 8. Core Flow Diagrams

### 8.1 Seckill -- Complete 8-Step Flow

```
Client                  Server                  Redis              Kafka              Consumer              MySQL            WebSocket
  │                       │                       │                  │                   │                    │                 │
  │  ① GET /seckill/path  │                       │                  │                   │                    │                 │
  │ ─────────────────────>│                       │                  │                   │                    │                 │
  │  ② HMAC path returned │                       │                  │                   │                    │                 │
  │ <─────────────────────│                       │                  │                   │                    │                 │
  │                       │                       │                  │                   │                    │                 │
  │  ③ POST /seckill/exec │                       │                  │                   │                    │                 │
  │ ─────────────────────>│                       │                  │                   │                    │                 │
  │                       │── ④ Sentinel off ────>│                  │                   │                    │                 │
  │                       │── ⑤ Redisson Rate ───>│                  │                   │                    │                 │
  │                       │── ⑥ Lua Deduct ──────>│                  │                   │                    │                 │
  │                       │     (KEYS: stock +    │                  │                   │                    │                 │
  │                       │      over + user)     │                  │                   │                    │                 │
  │                       │<── result: remaining ─│                  │                   │                    │                 │
  │                       │                       │                  │                   │                    │                 │
  │                       │── ⑦ Generate orderNo │                  │                   │                    │                 │
  │                       │── ⑧ Kafka send ──────────────────────>│                   │                    │                 │
  │  ⑨ {"orderNo","QUEUING"}                     │                  │                   │                    │                 │
  │ <─────────────────────│                       │                  │                   │                    │                 │
  │                       │                       │                  │  ⑩ Consume       │                    │                 │
  │                       │                       │                  │ ────────────────>│                    │                 │
  │                       │                       │                  │                   │ ⑪ CAS UPDATE      │                 │
  │                       │                       │                  │                   │  WHERE version=N  │                 │
  │                       │                       │                  │                   │<── affected rows ─│                 │
  │                       │                       │                  │                   │                    │                 │
  │                       │                       │                  │                   │ ⑫ INSERT order    │                 │
  │                       │                       │                  │                   │ ⑬ INSERT flow     │                 │
  │                       │                       │                  │                   │                    │                 │
  │                       │                       │                  │                   │── ⑭ Push result ────────────────────>│
  │ <── ⑮ WebSocket "success" ──────────────────────────────────────────────────────────────────────────────│
```

### 8.2 AI RAG -- Complete 10-Step Flow

```
Client              Controller          ChatAppService      HybridRetrieval        StreamingChat          LLM API           DB
  │                     │                     │                    │                      │                    │               │
  │  POST /ai/chat/stream│                    │                    │                      │                    │               │
  │ ────────────────────>│                    │                    │                      │                    │               │
  │                     │ chat(sessionId,     │                    │                      │                    │               │
  │                     │   userId, question) │                    │                      │                    │               │
  │                     │ ──────────────────>│                    │                      │                    │               │
  │                     │                    │ ① save user msg   │                      │                    │               │
  │                     │                    │ ────────────────────────────────────────────────────────────────>│
  │                     │                    │                    │                      │                    │               │
  │                     │                    │ ② retrieve(query)  │                      │                    │               │
  │                     │                    │ ──────────────────>│                      │                    │               │
  │                     │                    │                    │ ③ pgvector search    │                    │               │
  │                     │                    │                    │ ───────────────────────────────────────────>│
  │                     │                    │                    │ ④ ILIKE keyword      │                    │               │
  │                     │                    │                    │ ───────────────────────────────────────────>│
  │                     │                    │                    │ ⑤ RRF fusion top-5   │                    │               │
  │                     │                    │                    │ ⑥ fetch chunk texts  │                    │               │
  │                     │                    │                    │ ───────────────────────────────────────────>│
  │                     │                    │<── List<chunk> ────│                      │                    │               │
  │                     │                    │                    │                      │                    │               │
  │                     │                    │ ⑦ load history    │                      │                    │               │
  │                     │                    │ ────────────────────────────────────────────────────────────────>│
  │                     │                    │                    │                      │                    │               │
  │                     │                    │ ⑧ streamRagChat(q, │                      │                    │               │
  │                     │                    │    context, history)│                      │                    │               │
  │                     │                    │ ──────────────────────────────────────>│                    │               │
  │                     │                    │                    │                      │ ⑨ ChatClient Call │               │
  │                     │                    │                    │                      │ ──────────────────>│               │
  │                     │                    │                    │                      │<── SSE stream ────│               │
  │                     │                    │<── Flux<String> ─────────────────────────│                    │               │
  │                     │                    │                    │                      │                    │               │
  │ ⑩ SSE event stream │                    │                    │                      │                    │               │
  │ <───────────────────│                    │                    │                      │                    │               │
  │  SSE: "您", "好"... │                    │                    │                      │                    │               │
  │  SSE: done{sessionId}                    │                    │                      │                    │               │
  │                     │                    │                    │                      │                    │               │
  │                     │                    │ onComplete: ⑩ save AI reply + update title                    │               │
  │                     │                    │ ────────────────────────────────────────────────────────────────>│
```

---

## 9. Anti-Overselling Architecture (3-Layer Defense)

Overselling is the core challenge in flash sale systems. Blitz employs a **3-layer cascading defense**:

```
                         Layer 1: Redis Lua
               ┌─────────────────────────────────┐
               │  Atomic DECR + sold-out flag    │
               │  Duplicate prevention (user key)│
               │  Negative stock rollback        │
               │  FAIL FAST: ~1ms latency        │
               └──────────────┬──────────────────┘
                              │ Lua returns remaining
                              v
                         Layer 2: Kafka Serialization
               ┌─────────────────────────────────┐
               │  Single partition per product   │
               │  Consumer processes one-by-one  │
               │  Idempotency: UNIQUE(user,sp)   │
               │  Duplicate detect → ACK skip    │
               └──────────────┬──────────────────┘
                              │ Consumer confirms
                              v
                         Layer 3: MySQL Optimistic Lock
               ┌─────────────────────────────────┐
               │  UPDATE stock WHERE version=N   │
               │  affected_rows == 0 → ROLLBACK  │
               │  Compensates Redis cache drift  │
               │  FINAL GUARANTEE: ACID          │
               └─────────────────────────────────┘
```

**Failure handling at each layer:**

| Layer | Failure Scenario | Recovery Action |
|-------|-----------------|-----------------|
| Lua | DECR goes negative | INCR rollback + set sold-out flag |
| Kafka | Producer send fails | Redis stock INCR + user key DEL |
| Kafka | Consumer CAS conflict | Redis stock rollback + push failure via WebSocket |
| Kafka | Consumer crashes mid-process | Kafka offset not committed → re-delivery → idempotency check |
| MySQL | Optimistic lock failure | Redis stock rollback + WebSocket failure notification |

---

## 10. Order State Machine

```
    PENDING_PAYMENT ──(超时/手动)──> CANCELLED
           │
           │ (支付成功)
           v
         PAID ──────(退款)─────────> REFUNDED
           │
           │ (发货)
           v
        SHIPPED
           │
           │ (确认收货)
           v
        RECEIVED
```

Each transition is validated by `OrderStatus.canTransitionTo()` and executed via domain methods (`order.pay()`, `order.ship()`, `order.receive()`, `order.cancel()`, `order.refund()`). State changes are published as `OrderEvent` via Kafka for downstream consumers (notifications, analytics, etc.).

---

## 11. Security Model

- **JWT stateless auth**: `JwtAuthFilter` extracts token from `Authorization: Bearer <token>`, validates via JJWT, sets `SecurityContext`
- **BCrypt**: User passwords hashed with `BCryptPasswordEncoder` (cost factor = default 10)
- **@RequireRole**: Custom annotation + AOP aspect for method-level RBAC
- **Public endpoints**: Login, register, API docs, health check
- **All other endpoints**: Require valid JWT

---

## 12. Interview Talking Points

Use these to answer common defense questions:

### Q: Why DDD instead of traditional 3-layer (Controller-Service-DAO)?

A: DDD aligns code boundaries with business boundaries. The `domain/` layer contains pure business logic (`Order.pay()`, `SeckillActivity.isInProgress()`) with zero framework dependency -- making it portable and testable. The Repository Port pattern decouples domain from infrastructure, enabling technology swap (e.g., MySQL to TiDB) without touching domain code.

### Q: How do you guarantee zero overselling?

A: 3-layer defense. Layer 1 (Redis Lua) handles 99.9% of hot-path traffic atomically. Layer 2 (Kafka serialization) ensures ordered processing. Layer 3 (MySQL optimistic lock `UPDATE ... WHERE version = N`) is the final ACID guarantee. The Lua script even handles the edge case where concurrent DECR on last item produces a negative value -- it immediately rolls back with INCR.

### Q: How does the RAG pipeline avoid hallucination?

A: The prompt template (`rag-system.st`) explicitly instructs the model: "If the knowledge base lacks relevant information, honestly say you cannot answer -- do not fabricate." The hybrid retrieval (pgvector cosine + ILIKE keyword with RRF fusion) ensures the retrieved context is both semantically relevant and lexically precise. Top-5 chunks with overlap-50 chunking provide sufficient context without diluting relevance.

### Q: Why Kafka for seckill orders instead of direct MySQL insert?

A: Direct MySQL writes at seckill concurrency would saturate connection pools. Kafka acts as a shock absorber: the producer writes in ~1ms, while consumers drain at MySQL's natural throughput. If the consumer falls behind, Kafka buffers millions of messages without data loss. Partition ordering guarantees per-product ordering.

### Q: How do you handle Kafka consumer failure?

A: Manual offset commit (commit after successful MySQL write). If the consumer crashes mid-process, the message is re-delivered. Idempotency is guaranteed by MySQL `UNIQUE KEY uk_user_seckill (user_id, seckill_product_id)` -- duplicate inserts raise `DuplicateKeyException`, which the consumer catches and ACKs silently.

### Q: 配置里开了 `spring.threads.virtual.enabled: true`，虚拟线程起什么作用？

A: **在当前构建里这个配置不生效。** 虚拟线程需要 Java 21+，而工程实际的
`java.version` 是 17，Spring Boot 会静默忽略这个属性 —— 也就是说这条
"卖点"目前是空的。把基线升到 Java 21 之后再谈收益才成立；在此之前，
并发能力取决于平台线程池。本项目没有跑过秒杀并发压测，因此也没有
实测数据可以对比。

---

## 13. Project Directory Tree

```
blitz/
├── pom.xml                                     # Parent POM (Spring Boot 4.0 + Spring AI 2.0 BOM)
├── docker-compose.yml                          # MySQL + Redis + Kafka + PostgreSQL(pgvector)
├── .gitignore
├── README.md
├── docs/                                       # Project documentation
├── docker/
│   └── mysql/
│       └── init/                               # MySQL initialization scripts
│
├── blitz-common/                               # Shared module
│   └── src/main/java/com/blitz/common/
│       ├── result/
│       │   ├── Result.java                     # Unified response {code, msg, data}
│       │   └── PageResult.java                 # Paginated response
│       └── exception/
│           ├── ErrorCode.java                  # Error code enum (4 categories)
│           ├── BusinessException.java          # Business exception
│           └── GlobalExceptionHandler.java     # @RestControllerAdvice
│
├── blitz-data/                                 # Data infrastructure
│   ├── src/main/java/com/blitz/data/
│   │   ├── base/
│   │   │   └── BaseEntity.java                 # createTime, updateTime, deleted (auto-fill)
│   │   └── config/
│   │       └── MyBatisPlusConfig.java          # Pagination interceptor + auto-fill handler
│   └── src/main/resources/
│       └── application-data.yml                # Default datasource config
│
├── blitz-security/                             # Authentication & authorization
│   └── src/main/java/com/blitz/security/
│       ├── config/
│       │   └── SecurityConfig.java             # Spring Security filter chain
│       ├── filter/
│       │   └── JwtAuthFilter.java              # JWT extraction + validation
│       ├── util/
│       │   └── JwtUtil.java                    # JJWT token generation/parsing
│       ├── annotation/
│       │   └── RequireRole.java                # @RequireRole("ADMIN")
│       └── aspect/
│           └── RequireRoleAspect.java          # AOP role check
│
├── blitz-user/                                 # User bounded context
│   └── src/main/java/com/blitz/user/
│       ├── api/
│       │   ├── controller/
│       │   │   ├── UserController.java         # Register, login
│       │   │   └── AddressController.java      # Address CRUD
│       │   └── dto/
│       │       ├── LoginRequest.java
│       │       ├── RegisterRequest.java
│       │       ├── UserVO.java
│       │       ├── AddressDTO.java
│       │       └── AddressVO.java
│       ├── application/service/
│       │   ├── UserApplicationService.java     # Registration + login orchestration
│       │   └── AddressApplicationService.java  # Address business logic
│       ├── domain/
│       │   ├── model/
│       │   │   ├── User.java                   # BCrypt password handling
│       │   │   └── Address.java
│       │   └── port/
│       │       ├── UserRepository.java
│       │       └── AddressRepository.java
│       └── infrastructure/
│           ├── mapper/
│           │   ├── UserMapper.java
│           │   └── AddressMapper.java
│           └── repository/
│               ├── UserRepositoryImpl.java
│               └── AddressRepositoryImpl.java
│
├── blitz-product/                              # Product bounded context
│   └── src/main/java/com/blitz/product/
│       ├── api/
│       │   ├── controller/
│       │   │   ├── ProductController.java
│       │   │   └── CategoryController.java
│       │   └── dto/
│       │       ├── ProductDTO.java
│       │       ├── SkuDTO.java
│       │       ├── ProductVO.java
│       │       ├── CategoryTreeNode.java
│       │       └── PageResult.java
│       ├── application/service/
│       │   ├── ProductApplicationService.java
│       │   └── CategoryApplicationService.java
│       ├── domain/
│       │   ├── model/
│       │   │   ├── Product.java
│       │   │   ├── Sku.java
│       │   │   ├── SkuStock.java
│       │   │   └── Category.java
│       │   └── port/
│       │       ├── ProductRepository.java
│       │       ├── SkuRepository.java
│       │       ├── SkuStockRepository.java
│       │       └── CategoryRepository.java
│       └── infrastructure/
│           ├── mapper/
│           │   ├── ProductMapper.java
│           │   ├── SkuMapper.java
│           │   ├── SkuStockMapper.java
│           │   └── CategoryMapper.java
│           ├── repository/
│           │   ├── ProductRepositoryImpl.java
│           │   ├── SkuRepositoryImpl.java
│           │   ├── SkuStockRepositoryImpl.java
│           │   └── CategoryRepositoryImpl.java
│           └── search/
│               └── ProductSearchService.java    # MySQL LIKE search
│
├── blitz-order/                                # Order bounded context
│   └── src/main/java/com/blitz/order/
│       ├── api/
│       │   ├── controller/
│       │   │   └── OrderController.java
│       │   └── dto/
│       │       ├── CreateOrderRequest.java
│       │       ├── OrderVO.java
│       │       └── OrderItemVO.java
│       ├── application/service/
│       │   ├── OrderApplicationService.java
│       │   ├── StockDeductionService.java       # Optimistic lock stock deduction
│       │   └── OrderQueryService.java
│       ├── domain/
│       │   ├── model/
│       │   │   ├── Order.java                   # Rich model with pay/ship/receive/cancel/refund
│       │   │   ├── OrderItem.java
│       │   │   ├── OrderStatus.java             # State enum with canTransitionTo()
│       │   │   ├── OrderEvent.java              # Domain event
│       │   │   └── OrderOperateLog.java
│       │   └── port/
│       │       ├── OrderRepository.java
│       │       ├── OrderItemRepository.java
│       │       └── OrderEventRepository.java
│       └── infrastructure/
│           ├── mapper/
│           │   ├── OrderMapper.java
│           │   ├── OrderItemMapper.java
│           │   ├── OrderEventMapper.java
│           │   └── OrderOperateLogMapper.java
│           ├── repository/
│           │   ├── OrderRepositoryImpl.java
│           │   ├── OrderItemRepositoryImpl.java
│           │   └── OrderEventRepositoryImpl.java
│           ├── messaging/
│           │   └── OrderEventPublisher.java     # Kafka domain event publisher
│           └── config/
│               └── KafkaConfig.java
│
├── blitz-seckill/  ★                           # Seckill bounded context
│   ├── src/main/java/com/blitz/seckill/
│   │   ├── api/
│   │   │   ├── controller/
│   │   │   │   └── SeckillController.java       # path, execute, result, warmup
│   │   │   └── dto/
│   │   │       ├── SeckillPathVO.java
│   │   │       ├── SeckillExecuteRequest.java
│   │   │       └── SeckillActivityVO.java
│   │   ├── application/service/
│   │   │   ├── SeckillApplicationService.java   # 8-step chain orchestration
│   │   │   ├── SeckillCacheService.java         # Redis stock warmup
│   │   │   └── SeckillPathService.java          # HMAC path generation
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   ├── SeckillActivity.java         # Activity time + status
│   │   │   │   ├── SeckillProduct.java          # Stock + deductStock()
│   │   │   │   ├── SeckillOrder.java
│   │   │   │   └── SeckillStockFlow.java        # Stock change ledger
│   │   │   └── port/
│   │   │       ├── SeckillActivityRepository.java
│   │   │       ├── SeckillProductRepository.java
│   │   │       ├── SeckillOrderRepository.java
│   │   │       └── SeckillStockFlowRepository.java
│   │   └── infrastructure/
│   │       ├── mapper/
│   │       │   ├── SeckillActivityMapper.java
│   │       │   ├── SeckillProductMapper.java
│   │       │   ├── SeckillOrderMapper.java
│   │       │   └── SeckillStockFlowMapper.java
│   │       ├── repository/
│   │       │   ├── SeckillActivityRepositoryImpl.java
│   │       │   ├── SeckillProductRepositoryImpl.java
│   │       │   ├── SeckillOrderRepositoryImpl.java
│   │       │   └── SeckillStockFlowRepositoryImpl.java
│   │       ├── redis/
│   │       │   └── SeckillStockRedisService.java # Lua executor + rollback
│   │       ├── kafka/
│   │       │   ├── SeckillOrderMessage.java     # Kafka message record
│   │       │   ├── SeckillOrderProducer.java    # Async order producer
│   │       │   └── SeckillOrderConsumer.java    # CAS + MySQL confirm + WS push
│   │       ├── websocket/
│   │       │   ├── WebSocketConfig.java
│   │       │   └── SeckillWebSocketHandler.java # userId→session map + push
│   │       └── config/
│   │           └── SentinelConfig.java
│   └── src/main/resources/
│       └── lua/
│           └── seckill_deduct.lua               # Atomic stock deduction script
│
├── blitz-ai/  ★                                # AI bounded context
│   ├── src/main/java/com/blitz/ai/
│   │   ├── api/
│   │   │   ├── controller/
│   │   │   │   ├── ChatController.java          # Stream chat (SSE), session CRUD
│   │   │   │   └── KnowledgeBaseController.java # Document ingestion
│   │   │   └── dto/
│   │   │       ├── ChatRequest.java
│   │   │       ├── ChatSessionVO.java
│   │   │       └── KbDocumentVO.java
│   │   ├── application/service/
│   │   │   ├── ChatApplicationService.java      # 10-step RAG orchestration
│   │   │   └── KnowledgeBaseService.java        # Document parse→chunk→embed→store
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   ├── KbDocument.java
│   │   │   │   ├── KbChunk.java
│   │   │   │   ├── ChatSession.java
│   │   │   │   └── ChatMessage.java
│   │   │   └── port/
│   │   │       ├── KbDocumentRepository.java
│   │   │       ├── KbChunkRepository.java
│   │   │       ├── ChatSessionRepository.java
│   │   │       └── ChatMessageRepository.java
│   │   └── infrastructure/
│   │       ├── mapper/
│   │       │   ├── KbDocumentMapper.java
│   │       │   ├── KbChunkMapper.java
│   │       │   ├── ChatSessionMapper.java
│   │       │   └── ChatMessageMapper.java
│   │       ├── repository/
│   │       │   ├── KbDocumentRepositoryImpl.java
│   │       │   ├── KbChunkRepositoryImpl.java
│   │       │   ├── ChatSessionRepositoryImpl.java
│   │       │   └── ChatMessageRepositoryImpl.java
│   │       ├── config/
│   │       │   ├── SpringAiConfig.java          # ChatClient bean
│   │       │   └── PgVectorConfig.java          # pgvector DataSource
│   │       ├── llm/
│   │       │   └── StreamingChatService.java    # Prompt template + ChatClient streaming
│   │       └── rag/
│   │           ├── EmbeddingService.java        # text-embedding-3-small
│   │           ├── KeywordSearchService.java    # PostgreSQL ILIKE search
│   │           ├── RrfFusionService.java        # Reciprocal Rank Fusion
│   │           └── HybridRetrievalService.java  # Vector + keyword → RRF top-K
│   └── src/main/resources/
│       └── prompts/
│           └── rag-system.st                    # RAG system prompt template
│
└── blitz-server/                                # Monolithic startup
    ├── src/main/java/com/blitz/
    │   ├── BlitzApplication.java                # @SpringBootApplication entry
    │   └── controller/
    │       └── HealthController.java            # /api/health endpoint
    └── src/main/resources/
        ├── application.yml                      # 服务端口等通用配置（虚拟线程配置在 Java 17 下不生效）
        ├── application-dev.yml                  # Datasource, Flyway, Kafka, Redis, JWT, Knife4j
        ├── application-ai.yml                   # Spring AI OpenAI config + PG datasource
        └── db/migration/
            ├── V1__init_blitz_product.sql
            ├── V2__init_blitz_user.sql
            ├── V3__init_blitz_order.sql
            ├── V4__init_blitz_seckill.sql
            └── V5__init_blitz_ai.sql
```

> 该树为节选：`V6–V11` 迁移（shop/review/coupon/seed）、`application-minimal.yml` /
> `application-prod.yml`，以及 `blitz-server` 下的 `merchant/`、`review/`
> 控制器未展开；以实际目录为准。

---

## 14. Future Roadmap

- [ ] **blitz-gateway**: Spring Cloud Gateway for microservice migration
- [ ] **blitz-payment**: Payment bounded context (Alipay/WeChat Pay integration)
- [ ] **blitz-notification**: SMS/email/WebSocket push notification service
- [ ] **Elasticsearch**: Replace MySQL LIKE with full-text search for product search
- [ ] **Observability**: OpenTelemetry + Grafana stack (metrics, traces, logs)
- [ ] **Multi-level cache**: Caffeine L1 + Redis L2 for hot product data
- [ ] **AI enhancements**: Multi-turn memory, product recommendation embeddings, image search

---

## License

MIT License -- see [LICENSE](LICENSE) file for details.

---

**Author:** xuchenxiang  
**Project:** Blitz Smart Mall
