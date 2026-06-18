# Blitz 智能商城 —— 毕业设计答辩 PPT 大纲

> 版本: 1.0 | 日期: 2026-06-18 | 预计时长: 15-20 分钟 (20 页)

---

## Slide 1: 封面

**标题:** Blitz 智能商城 —— 基于 Java 21 + Spring AI 的高并发智能电商平台

**副标题:** 计算机科学与技术专业 毕业设计答辩

**信息:** 姓名 / 学号 / 导师 / 答辩日期

**建议视觉:** 深色科技风背景 + 项目 Logo 或闪电图标 (Blitz = 闪电突袭)

**答辩 Q&A 准备:**
- 无 (封面页)

---

## Slide 2: 项目背景与选题意义

**标题:** 为什么做"电商 + AI"？

**Key Points:**
- 电商是高并发场景的经典练兵场——秒杀、订单、支付、库存，覆盖后端核心能力
- AI 大模型时代，智能客服从"关键词匹配"升级为 RAG 语义理解，是面试加分项
- 市面上电商项目同质化严重 (SSM 三板斧)，新技���栈项目稀缺——毕业设计 + 求职简历双重价值

**建议视觉:** 左边电商痛点头 (高并发 / 库存超卖 / 客服低效)，右边 AI 赋能方案

**答辩 Q&A 准备:**
- Q: 电商项目很多，你的项目有什么不同?
  - A: 两点差异。一是技术栈——Java 21 虚拟线程 + Spring Boot 4.0 + Spring AI 2.0，这是 2025-2026 年面试主流；二是深度——DDD 充血模型 + CQRS 秒杀 + 混合 RAG 检索，不是 CRUD 堆砌。每个核心链路都能经得起深挖。

---

## Slide 3: 技术选型对比

**标题:** 为什么选择这些技术？——站在巨人肩膀上

**Key Points:**
- 参考 4 个开源标杆: mall (60K★) / miaosha (26K★) / mall-swarm (11K★) / interview-guide (2.4K★)
- 吸收其架构思想，全新代码实现——包名 com.blitz，DDD 四层架构，零复制粘贴
- 技术选型对比表: JDK 8→21, SSM→Spring Boot 4.0, RabbitMQ→Kafka, 无 AI→Spring AI + 混合检索
- 选型原则: 每个组件都能在答辩中解释"为什么不用 XXX"

**建议视觉:** 大表格——参考项目 vs Blitz 的技术栈列对比 (JDK / 框架 / ORM / MQ / 缓存 / AI)

**答辩 Q&A 准备:**
- Q: 你参考了哪些项目？会不会是抄袭?
  - A: 参考不等于抄袭。参考项目用的是 JDK 8/17 + Spring Boot 2.x + SSM 三层架构，我的项目是 JDK 21 + Spring Boot 4.0 + DDD 四层架构 + CQRS + Spring AI。所有代码手写，包名 com.blitz，架构完全不同。参考的是"电商该有哪些模块"这类领域知识，不是代码。

---

## Slide 4: 系统架构总览

**标题:** 模块化单体 + DDD 四层架构

**Key Points:**
- 9 个 Maven 模块: blitz-common / blitz-data / blitz-security (基础设施) + blitz-user / blitz-product / blitz-order / blitz-seckill / blitz-ai (业务) + blitz-server (启动器)
- DDD 四层内部结构: api/ (接口层) → domain/ (领域层) → application/ (应用层) → infrastructure/ (基础设施层)
- 模块间通过 API 接口通信，不跨 Schema JOIN——为微服务拆分预留空间
- 141 个 Java 源文件，完整编译通过 (BUILD SUCCESS)

**建议视觉:** 分层架构图——顶部 blitz-server，中间 5 个业务模块，底部基础设施模块；每个模块内部展开 DDD 四层

**答辩 Q&A 准备:**
- Q: 为什么用模块化单体而不是直接上微服务?
  - A: 毕业设计阶段，微服务的运维复杂度 (服务发现 / 配置中心 / 分布式事务) 会分散精力，不利于深挖核心链路。模块化单体 + API 隔离通信，架构上已经为微服务拆分做好了边界划分，未来拆成独立服务只需改部署方式，代码层面不需要重构。

---

## Slide 5: DDD 领域驱动设计

**标题:** 充血模型 vs 贫血模型——让代码有灵魂

**Key Points:**
- 传统贫血模型: POJO 只有 getter/setter，业务逻辑散落在 Service 层，像"数据容器"
- 充血模型: 领域对象自带行为方法——Order.cancel() / SeckillActivity.isInProgress() / OrderStatus.canTransitionTo()
- 每个业务模块 = 一个限界上下文 (Bounded Context)，有独立的 domain/port 接口
- 基础设施层通过依赖倒置实现 port 接口——MyBatis-Plus Mapper → RepositoryImpl

**建议视觉:** 对比图——左边贫血模型 (POJO + Service 大泥球)，右边充血模型 (Domain 对象 + Port/Adapter)

**答辩 Q&A 准备:**
- Q: DDD 会不会过度设计？毕业设计需要吗?
  - A: DDD 不是大项目的专利。我的 DDD 是"轻量级"的——没有 Event Storming / 聚合根 / 值对象全套概念，核心只有两点: (1) 充血模型——领域对象自带行为；(2) Port/Adapter——接口在 domain 层，实现在 infrastructure 层。这两点已经是面试高频考点，值得做。

---

## Slide 6: 数据库设计

**标题:** 5 Schema × 20 表 × Flyway 版本迁移

**Key Points:**
- 每个模块独立数据库 Schema: blitz_user (3表) / blitz_product (5表) / blitz_order (4表) / blitz_seckill (4表) / blitz_ai (4表)
- 额外: PostgreSQL + pgvector 扩展用于 AI 向量存储
- Flyway 管理所有 DDL 迁移脚本——版本可追溯，环境可复现
- 关键设计: 秒杀库存流水分表 (seckill_stock_flow) 记录每一笔库存变更

**建议视觉:** ER 图——5 个 Schema 的核心表关系，标注外键和索引

**答辩 Q&A 准备:**
- Q: 为什么用分 Schema 而不是分库?
  - A: 分 Schema 是分库的中间态。开发阶段单体数据库够用，但 Schema 隔离保证模块间不会跨 JOIN，数据边界清晰。将来拆微服务时，每个 Schema 独立迁移到自己的数据库实例即可，零 SQL 改动。

---

## Slide 7: 核心链路一 —— 秒杀引擎 (8 步全链路)

**标题:** 从点击到下单——秒杀的 8 个关卡

**Key Points:**
- ① PoW 验证码 → ② HMAC 动态秒杀路径 → ③ Sentinel 入口限流 → ④ Redisson 用户级限流 (1 QPS/人) → ⑤ Redis Lua 原子预减库存 → ⑥ Kafka 异步下单 → ⑦ Consumer 创建订单 (乐观锁兜底) → ⑧ WebSocket 推送结果
- 每一步都在"拦截无效请求"——越靠近用户端，拦截成本越低
- 核心思想: 前端防抖 → 网关限流 → Redis 原子化 → Kafka 削峰 → MySQL 最终一致性

**建议视觉:** 横向流程图——8 个节点从左到右，标注每一步的技术组件和拦截比例

**答辩 Q&A 准备:**
- Q: 为什么是 8 步？能不能减少?
  - A: 8 步中真正操作库存的只有第⑤步 Redis Lua，其他 7 步都是"拦截层"——验证码防脚本、HMAC 防 URL 猜测、限流防刷、Kafka 削峰保稳。每一步去掉都会增加下游压力。面试中常问"秒杀系统怎么设计"，8 步拆解就是把抽象问题具象化的方式。

---

## Slide 8: 三层防超卖架构

**标题:** 库存扣减的三道保险——Lua + Kafka + 乐观锁

**Key Points:**
- 第一层: Redis Lua 原子预减——单线程执行，库存从 N 到 0 的每一减都是原子的
- 第二层: Kafka 串行化——同一商品的消息发到同一 Partition，Consumer 单线程消费，天然排队
- 第三层: MySQL 乐观锁 (version 字段)——UPDATE ... SET stock = stock - 1, version = version + 1 WHERE version = ?
- 三层递进: 快 (Redis ns 级) → 稳 (Kafka 削峰) → 准 (MySQL 最终一致)

**建议视觉:** 三层金字塔图——底层最宽 (MySQL, 100% 准确) → 中层 (Kafka, 削峰) → 顶层 (Redis, 极速)

**答辩 Q&A 准备:**
- Q: 为什么不能只用 Redis 扣库存? 为什么还需要 MySQL 乐观锁?
  - A: Redis 快但不持久——宕机会丢数据。Kafka 异步消费时可能有消息重复或乱序，version 乐观锁保证"库存绝对不会扣成负数"。这是最终一致性的经典实践: Redis 做快速预扣 (最终可能回滚)，MySQL 做最终确认 (绝对准确)。

---

## Slide 9: Redis Lua 原子操作

**标题:** 一个脚本完成"判断 + 扣减 + 防重"

**Key Points:**
- Lua 脚本三合一: 检查售罄标记 → 检查用户重复购买 → 原子扣减库存 → 库存归零时打标记
- 返回值语义: >=0 剩余库存 / -1 售罄 / -2 重复购买 / -3 库存不足
- Redisson 加载 + SHA 调用 (非 eval 每次传脚本，减少网络开销)
- 单次操作 O(3) = 3 次 Redis 命令 (GET stock + DECR + SADD user set)

**建议视觉:** Lua 伪代码流程图——4 个 if 判断分支，标注返回值含义

**答辩 Q&A 准备:**
- Q: Lua 脚本为什么是原子的？Redis 不是单线程吗?
  - A: Redis 单线程执行命令，但多个命令之间有网络往返，存在竞态窗口。Lua 脚本把多个命令打包成一个执行单元，在脚本执行期间其他命令排队等待——这就是"原子性"的来源。不依赖 MULTI/EXEC 事务 (不支持回滚)，Lua 是秒杀场景的最优解。

---

## Slide 10: Kafka 异步削峰 + CAS 最终一致

**标题:** 从"峰"到"谷"——消息队列的艺术

**Key Points:**
- Producer: 秒杀成功后同步发送 SeckillOrderMessage → 发送失败立即回滚 Redis 库存
- Consumer: 单线程消费 → 查询 SeckillProduct (version) → 乐观锁扣减 → 创建 SeckillOrder + StockFlow
- CAS (Compare-And-Swap): UPDATE 的 affected_rows = 0 表示版本冲突 → 重试或回滚
- Kafka 消息幂等: 消费前检查 orderNo 是否已存在

**建议视觉:** 时序图——Producer → Kafka Topic (Partition 按 productId) → Consumer → MySQL

**答辩 Q&A 准备:**
- Q: Kafka 消息丢失怎么办?
  - A: 三个层面保障——(1) Producer 端 acks=all，发送失败 catch 异常并回滚 Redis；(2) Broker 端 replication-factor=3，min.insync.replicas=2；(3) Consumer 端手动提交 offset，消费成功后才 commit。如果 Consumer 宕机，offset 未提交，Rebalance 后重新消费——配合乐观锁 version 保证幂等。

---

## Slide 11: 核心链路二 —— AI 智能问答 (10 步 RAG)

**标题:** 让 AI 读懂商城——RAG 检索增强生成

**Key Points:**
- ① 用户提问 → ② Query Rewrite (规则优先 + LLM 异步改写) → ③ Embedding 向量化 (text-embedding-3-small) → ④ pgvector 向量检索 topK=10 → ⑤ ILIKE 关键词全文检索 topK=10 → ⑥ RRF 融合排序 topK=5 → ⑦ Prompt 模板组装 (system + context + question) → ⑧ LLM 流式生成 → ⑨ SSE 逐字输出 + 探测窗口防截断 → ⑩ 对话历史持久化到 chat_message 表
- 核心价值: 商品咨询 / 订单查询 / 售后政策——替代传统 FAQ 关键词匹配

**建议视觉:** 横向流程图——10 个步骤节点，不同颜色区分: 预处理 (蓝) / 检索 (绿) / 生成 (橙) / 输出 (红)

**答辩 Q&A 准备:**
- Q: 为什么要做 RAG，直接用大模型不行吗?
  - A: 大模型有两大局限——(1) 知识截止日期，不知道商城最新的商品、价格、库存；(2) 幻觉问题，可能编造不存在的商品。RAG 把"商城知识库"作为外部上下文注入 Prompt，让 LLM 基于事实回答——这是企业级 AI 应用的标准范式。

---

## Slide 12: 混合检索架构

**标题:** 向量 + 关键词 → RRF 融合——取两者之长

**Key Points:**
- 向量检索 (pgvector + HNSW 索引): 语义相似——"怎么退货" 能匹配 "退款流程"
- 关键词检索 (PostgreSQL ILIKE + tsvector): 精确匹配——"iPhone 15 价格" 不会被向量误导
- RRF (Reciprocal Rank Fusion) 融合: score = 1/(K+rank_vector) + 1/(K+rank_keyword)，K=60
- 为什么混合？——纯向量会漏掉专有名词 (型号 SKU)，纯关键词不懂同义词

**建议视觉:** 双路检索汇合图——左路向量 (蓝色)，右路关键词 (绿色)，中间 RRF 融合 (橙色)，输出 topK=5

**答辩 Q&A 准备:**
- Q: RRF 比直接加权求和的优势是什么?
  - A: 向量相似度 (0-1 cosine) 和关键词 BM25 分数的量纲不同，无法直接加权。RRF 只关心"排名"而非"分数绝对值"，天然跨检索器可比。K=60 是经验值，论文推荐 K ∈ [60, 100]，效果稳定。

---

## Slide 13: SSE 流式输出

**标题:** 一字一字"吐"出来——ChatClient Streaming + SseEmitter

**Key Points:**
- Spring AI ChatClient 流式模式 → Flux<String> → SseEmitter 桥接
- 前端 EventSource 消费 SSE 事件——无需 WebSocket，单向推送足够
- 探测窗口机制: 120 字符缓冲区，检测到完整句子才输出——防止"断词"
- 对比: 非流式响应耗时 5-10s 用户等待 → 流式 0.5s 首字延迟，体验提升 10x

**建议视觉:** 时序图——前端 (EventSource) ↔ Controller (SseEmitter) ↔ ChatClient (Flux) ↔ LLM API

**答辩 Q&A 准备:**
- Q: 为什么用 SSE 而不是 WebSocket?
  - A: SSE 是单向的 (服务端→客户端)，AI 问答恰好只需要单向推送。WebSocket 是双向全双工，握手开销更大，且需要额外的连接管理。SSE 基于 HTTP，天然支持 HTTP/2 多路复用和自动重连——更适合这个场景。

---

## Slide 14: 订单状态机

**标题:** 6 种状态 × 严格流转校验——状态即文档

**Key Points:**
- 6 种状态: PENDING_PAYMENT → PAID → SHIPPED → RECEIVED (正向) / PENDING_PAYMENT → CANCELLED (取消) / PAID → REFUNDED (退款)
- 充血枚举: OrderStatus.canTransitionTo(target) 内聚所有流转规则
- 非法流转直接抛 IllegalStateException——不会出现"已取消→已发货"的 BUG
- 订单事件溯源 (order_event 表): 每次状态变更记录一条事件，可回溯

**建议视觉:** 状态机图——6 个圆角矩形 + 带标签的箭头，终点状态用灰色

**答辩 Q&A 准备:**
- Q: 为什么把状态机逻辑放在枚举里而不是 Service 里?
  - A: 这就是"充血模型"的体现。状态和状态规则是领域知识，应该和状态定义放在一起——OrderStatus 枚举就是订单状态的唯一真相来源 (Single Source of Truth)。如果写在 Service 里，多个 Service 可能重复实现流转逻辑，且不一致。枚举自带编译期检查，IDE 重构时自动同步。

---

## Slide 15: 安全设计

**标题:** 四层防线——从认证到限流

**Key Points:**
- JWT 双 Token 机制: Access Token (15min) + Refresh Token (7d)，无状态认证
- Spring Security 6: 集成 JWT Filter + 路径权限配置 + CORS 跨域
- HMAC 动态秒杀路径: /seckill/{productId}/{hmac}——签名 60s 过期，防 URL 猜测
- 恶意请求拦截: Sentinel 网关限流 + Redisson 用户级限流 (1 QPS/用户)

**建议视觉:** 洋葱模型——4 层同心圆: 限流 (最外) → HMAC 签名 → JWT 认证 → 业务逻辑 (最内)

**答辩 Q&A 准备:**
- Q: JWT 无状态怎么实现踢人下线?
  - A: 两种方案——(1) Redis 黑名单: 登出时将 token 加入黑名单，Filter 检查；(2) 版本号: 用户表中存 token_version，JWT payload 携带，不一致则拒绝。我采用的是 Redis 黑名单，简单可靠。

---

## Slide 16: 性能测试

**标题:** JMeter 压测——虚拟线程 vs 传统线程池

**Key Points:**
- 测试环境: Windows 11 / 本地 Docker (Redis + Kafka + MySQL + PostgreSQL)
- 虚拟线程优势 (引用数据): QPS 847→5238 (6x), 内存 2.3G→320M (7x)
- 秒杀接口预期: Redis Lua 原子操作 → QPS 5000+ (本地单机)
- 瓶颈分析: 数据库连接池 (Druid) → Kafka 吞吐 → LLM API 延迟 (AI 问答)

**建议视觉:** 柱状图——传统线程池 vs 虚拟线程的 QPS 和内存对比；秒杀接口 RT 分布

**答辩 Q&A 准备:**
- Q: QPS 5000+ 在本地环境能达到吗？是不是太低了?
  - A: 这个数据是基于 Redis Lua 原子操作的理论预期——没有数据库 IO，单次操作 O(3) Redis 命令。本地 Docker 环境下 MySQL 是瓶颈，生产环境使用 RDS + 读写分离可以更高。毕业设计的压测重点是"证明架构设计合理"，不是追求极限数字。

---

## Slide 17: 项目亮点总结

**标题:** 为什么这个项目能打动面试官？

**Key Points:**
- 技术前沿: Java 21 虚拟线程 + Spring Boot 4.0 + Spring AI 2.0——2026 面试必备
- 不是 CRUD: DDD 充血模型 + CQRS 读写分离 + 混合 RAG 检索——有架构深度
- 不是抄的: 参考 4 个开源项目 (共 100K★) 的架构思想，141 文件全部手写
- 经得起问: 每条核心链路都有清晰的技术原理和选型依据
- 可演示: 前端 Vue 3 + 后端完整链路 + AI 智能问答——答辩时现场演示

**建议视觉:** 5 个亮点卡片排列，每个卡片配合图标

**答辩 Q&A 准备:**
- Q: 你觉得这个项目最大的创新点是什么?
  - A: 不是某个算法创新，而是"工程整合创新"——把 DDD 充血模型、CQRS 秒杀、混合 RAG 检索、SSE 流式输出四个技术点在一个项目中落地，且每个点都经得起深挖。这比做单个技术点的 demo 更有说服力，因为它证明了我能驾驭复杂系统的架构设计。

---

## Slide 18: 技术成长与收获

**标题:** 从 CRUD Boy 到工程思考者

**Key Points:**
- DDD 改变了编码思维——从"写 Service 方法"到"设计领域模型"
- 秒杀让我理解了"高并发不是加机器，是减少锁竞争"——Redis 原子化 > MySQL 事务
- AI 集成让我认识到 RAG 本质是"检索质量决定生成质量"——垃圾进垃圾出
- 开源项目阅读培养了架构品味——看 100K★ 代码后，自然知道什么是好架构
- 最大的收获: 敢于在简历上写"精通 Java 高并发"的底气

**建议视觉:** 时间线——Phase 1-7 的开发过程，标注每个阶段的关键收获

**答辩 Q&A 准备:**
- Q: 做完这个项目，你对哪个技术点最有感触?
  - A: 虚拟线程。Java 21 的 Virtual Threads 让"一请求一线程"模型变得可行——不需要 Reactive 编程的复杂度，就能获得接近异步的性能。这改变了 Java 后端的技术选型——以前高并发必须上 WebFlux，现在虚拟线程 + Tomcat 就能打。

---

## Slide 19: 未来展望

**标题:** 从毕业设计到生产级系统——还有多远？

**Key Points:**
- 短期 (1-3 月): 微服务拆分——Nacos 注册中心 + Gateway 网关 + Seata 分布式事务
- 中期 (3-6 月): Elasticsearch 商品搜索 (替代 ILIKE) + 推荐引擎 (协同过滤 + LLM)
- 长期 (6-12 月): Kubernetes 部署 + Prometheus/Grafana 监控 + CI/CD 流水线
- AI 增强: Multi-Agent 架构——订单 Agent / 商品 Agent / 售后 Agent 协作

**建议视觉:** 路线图——3 个里程碑节点 (v1.0 当前 / v2.0 微服务 / v3.0 云原生)

**答辩 Q&A 准备:**
- Q: 为什么一开始不做到这些?
  - A: 毕业设计的时间窗口是 4-6 周，必须做取舍。我选择在"深度"上发力——把秒杀和 AI 两个核心链路做到经得起深挖，而不是在"广度"上铺开但每个点都是皮毛。微服务和 K8s 是自然演进方向，但不会改变核心链路的实现。

---

## Slide 20: 致谢 & Q&A

**标题:** 感谢各位老师！欢迎提问

**Key Points:**
- 致谢: 导师 / 答辩委员会 / 同学
- 联系方式: 邮箱 / GitHub (如有)
- 项目仓库: (如有开源计划可放链接)
- "期待各位老师的宝贵意见"

**建议视觉:** 简洁收尾——深色背景 + 大号文字 + 联系信息

**答辩 Q&A 准备:**
- 无 (但准备好回答任何关于前面 19 页的技术问题)

---

## 附录: 答辩高频追问清单

以下问题建议额外准备，可能出现在任意环节:

1. **"你提到 141 个文件，有多少是自己写的？"** → 全部手写，参考的是架构思想而非代码。包名 com.blitz，可以逐文件检查。

2. **"虚拟线程和普通线程有什么区别？"** → 虚拟线程由 JVM 调度而非 OS 调度，创建成本极低 (~1KB vs ~1MB)，阻塞时自动让出载体线程。适合 IO 密集型场景。

3. **"Kafka 和 RabbitMQ 为什么选 Kafka？"** → Kafka 的 Partition 机制天然支持同一商品消息串行化，且吞吐量更高 (百万级 QPS)，适合秒杀场景的瞬时流量。

4. **"pgvector 和 Milvus 有什么优劣？"** → pgvector 嵌入 PostgreSQL，运维成本低 (无需额外服务)，适合中小规模 (百万级向量)。Milvus 专用向量数据库，十亿级用 Milvus。毕业设计规模选 pgvector 足够。

5. **"Seata AT 模式怎么保证分布式事务？"** → AT 基于两阶段提交: 一阶段执行 SQL + 记录 undo_log，二阶段根据协调器决定提交或回滚 (通过 undo_log 生成反向 SQL)。对业务代码无侵入。

6. **"如果 Redis 宕机了，秒杀怎么办？"** → 活动开始前预热库存到 Redis，如果 Redis 宕机则降级——直接查 MySQL 库存，牺牲性能保证可用性。实际生产用 Redis Cluster + Sentinel 保证高可用。
