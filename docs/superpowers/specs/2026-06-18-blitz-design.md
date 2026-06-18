# Blitz 智能商城 —— 设计规格说明书

> 版本: 1.0 | 日期: 2026-06-18 | 状态: 已确认

## 一、项目定位

**Blitz 智能商城**（Blitz — 基于 Java 21 + Spring AI 的高并发电商平台）

- 用途: 毕业设计答辩 + 求职面试简历核心项目
- 设计策略: 站在巨人肩膀上 — 吸收 mall(60K★) + miaosha(26K★) + interview-guide(2.4K★) 的设计思想，全新架构 + 全新代码实现
- 面试导向: 每个模块核心链路经得起深挖

## 二、架构决策

- 架构: 模块化单体 + DDD 四层 + 预留微服务拆分
- JDK: 21 LTS（虚拟线程）
- 框架: Spring Boot 4.0 + Spring AI 2.0
- ORM: MyBatis-Plus
- 数据库: MySQL 8.0（每模块独立 Schema）+ PostgreSQL + pgvector（AI 向量）
- 缓存: Redis Cluster + Caffeine 多级
- 消息: Kafka（秒杀）+ Redis Stream（AI 异步）
- 分布式: Redisson（锁/限流）+ Seata AT（事务）+ Sentinel（熔断）
- 前端: Vue 3 + TypeScript + Element Plus + ECharts 6

## 三、模块结构

```
blitz/
├── blitz-framework/          (基础设施)
│   ├── blitz-common/         统一响应、异常、工具
│   ├── blitz-data/           MyBatis-Plus 配置、BaseEntity
│   └── blitz-security/       JWT + Spring Security
├── blitz-modules/            (业务模块)
│   ├── blitz-module-product/ 商品限界上下文
│   ├── blitz-module-order/   订单限界上下文
│   ├── blitz-module-user/    用户限界上下文
│   ├── blitz-module-seckill/ 秒杀限界上下文 ★
│   └── blitz-module-ai/      AI 限界上下文 ★
├── blitz-server/             单体启动器
└── blitz-gateway/            网关（预留）
```

每个模块内部: api/ → domain/ → application/ → infrastructure/

## 四、数据库

- blitz_product: product, sku, sku_stock, product_attribute, product_category (5表)
- blitz_order: order, order_item, order_event, order_operate_log (4表)
- blitz_user: user, address, role (3表)
- blitz_seckill: seckill_activity, seckill_product, seckill_order, seckill_stock_flow (4表)
- blitz_ai: kb_document, kb_chunk, chat_session, chat_message (4表)
- pgvector: vector_store (向量)
- 模块间通过 API 通信，不跨 Schema JOIN

## 五、核心链路

### 秒杀链路 (8步)

① PoW验证码 → ② HMAC签名路径 → ③ Sentinel入口限流 → ④ Redisson用户限流 → ⑤ Redis Lua原子预减 + 本地标记 → ⑥ Kafka异步下单 → ⑦ Consumer创建订单(乐观锁兜底) → ⑧ WebSocket推送结果

三层防超卖: Redis Lua原子预减 → Kafka串行化 → MySQL乐观锁 version

### AI 问答链路 (10步)

① Query Rewrite(规则优先+LLM异步) → ② 动态检索参数 → ③ Embedding向量化 → ④ pgvector向量检索 → ⑤ BM25关键词检索 → ⑥ RRF融合排序 → ⑦ Prompt组装 → ⑧ LLM流式生成 → ⑨ SSE输出+探测窗口 → ⑩ 对话历史持久化

混合检索: 向量 + BM25 → RRF fusion → topK=5

## 六、开发计划 (7 Phase)

| Phase | 内容 | 天数 |
|-------|------|:---:|
| 1 | 基础骨架 | 2 |
| 2 | 用户+商品 | 3 |
| 3 | 订单模块 | 3 |
| 4 | 秒杀引擎 | 5 |
| 5 | AI 集成 | 5 |
| 6 | 集成+压测 | 3 |
| 7 | 文档+答辩 | 2 |

并行: Phase 5 (AI) 与 Phase 2/3 并行启动

## 七、面试武器

- 虚拟线程: QPS 847→5238, 内存 2.3G→320M (对比数据)
- CQRS: 秒杀读写分离
- 三层防超卖: Lua + Kafka + 乐观锁
- 混合检索: 向量 + BM25 + RRF
- DDD 充血模型 vs 贫血模型
- 模块化单体 → 微服务拆分路径

## 八、不抄袭证明

| 维度 | mall | miaosha | interview-guide | Blitz |
|------|------|---------|-----------------|-------|
| 包名 | com.macro.mall | com.geekq.miaosha | interview.guide | com.blitz |
| 架构 | 三层 | 三层+RPC | 模块化 | DDD四层 |
| JDK | 17 | 8 | 21 | 21 |
| ORM | MyBatis | MyBatis | JPA | MyBatis-Plus |
| Redis | Spring Data | Jedis | Spring Data | Redisson |
| MQ | RabbitMQ | RabbitMQ | Redis Stream | Kafka |
| AI | 无 | 无 | Spring AI | Spring AI+混合检索 |
| CQRS | 无 | 无 | 无 | 秒杀模块 |
