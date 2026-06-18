# Phase 2: 用户 + 商品模块 Implementation Plan

> **For agentic workers:** Use superpowers:subagent-driven-development to implement this plan task-by-task.

**Goal:** 用户注册登录（BCrypt + JWT）全链路 + 商品 SPU/SKU CRUD + 分类树 + ES 搜索

**Architecture:** DDD 四层（api/domain/application/infrastructure），所有 API 通过 JWT 鉴权

**Tech Stack:** Java 21, Spring Boot 4.0, MyBatis-Plus, Spring Security, JJWT, Elasticsearch

## Global Constraints

- Package: com.blitz.user.* / com.blitz.product.*
- 密码 BCrypt 加密，禁止明文存储
- JWT Token 24h 过期
- 所有接口统一返回 Result<T>
- 模块间通过 Feign 接口通信
- 每个模块独立 Maven 编译

---

### Task 1: 用户领域层（domain + infrastructure）

- Create: `blitz-user/src/main/java/com/blitz/user/domain/model/User.java` — 充血实体
- Create: `blitz-user/src/main/java/com/blitz/user/domain/model/Address.java`
- Create: `blitz-user/src/main/java/com/blitz/user/domain/port/UserRepository.java` — 仓储端口
- Create: `blitz-user/src/main/java/com/blitz/user/infrastructure/mapper/UserMapper.java` — MyBatis-Plus
- Create: `blitz-user/src/main/java/com/blitz/user/infrastructure/mapper/AddressMapper.java`
- Create: `blitz-user/src/main/java/com/blitz/user/infrastructure/repository/UserRepositoryImpl.java`
- Create: `blitz-user/src/main/java/com/blitz/user/api/dto/LoginRequest.java` — Record
- Create: `blitz-user/src/main/java/com/blitz/user/api/dto/RegisterRequest.java` — Record
- Create: `blitz-user/src/main/java/com/blitz/user/api/dto/UserVO.java` — Record

- [ ] Step 1: Write User.java (充血实体，含 register/login 行为)
- [ ] Step 2: Write Address.java
- [ ] Step 3: Write UserRepository port interface
- [ ] Step 4: Write UserMapper (MyBatis-Plus BaseMapper)
- [ ] Step 5: Write AddressMapper
- [ ] Step 6: Write UserRepositoryImpl
- [ ] Step 7: Write DTO Records (LoginRequest, RegisterRequest, UserVO)
- [ ] Step 8: `mvn compile -pl blitz-user -q` → commit

---

### Task 2: 用户应用层 + 接口层（application + api controller）

- Create: `blitz-user/src/main/java/com/blitz/user/application/service/UserApplicationService.java`
- Create: `blitz-user/src/main/java/com/blitz/user/application/service/AddressApplicationService.java`
- Create: `blitz-user/src/main/java/com/blitz/user/api/controller/UserController.java`
- Create: `blitz-user/src/main/java/com/blitz/user/api/controller/AddressController.java`

- [ ] Step 1: UserApplicationService — register(RegisterRequest): UserVO, login(LoginRequest): String(token), getCurrentUser(userId): UserVO
- [ ] Step 2: AddressApplicationService — CRUD
- [ ] Step 3: UserController — POST /user/register, POST /user/login, GET /user/current
- [ ] Step 4: AddressController — GET/POST/PUT/DELETE /user/address
- [ ] Step 5: `mvn compile -pl blitz-user -q` → commit

---

### Task 3: 商品领域层 + 基础设施

- Create: `blitz-product/src/main/java/com/blitz/product/domain/model/Product.java`
- Create: `blitz-product/src/main/java/com/blitz/product/domain/model/Sku.java`
- Create: `blitz-product/src/main/java/com/blitz/product/domain/model/Category.java`
- Create: `blitz-product/src/main/java/com/blitz/product/domain/model/SkuStock.java`
- Create: `blitz-product/src/main/java/com/blitz/product/domain/port/ProductRepository.java`
- Create: `blitz-product/src/main/java/com/blitz/product/infrastructure/mapper/ProductMapper.java`
- Create: `blitz-product/src/main/java/com/blitz/product/infrastructure/mapper/SkuMapper.java`
- Create: `blitz-product/src/main/java/com/blitz/product/infrastructure/mapper/CategoryMapper.java`
- Create: `blitz-product/src/main/java/com/blitz/product/infrastructure/mapper/SkuStockMapper.java`
- Create: `blitz-product/src/main/java/com/blitz/product/infrastructure/repository/ProductRepositoryImpl.java`
- Create: `blitz-product/src/main/java/com/blitz/product/api/dto/ProductDTO.java`

- [ ] Step 1-5: 创建所有 domain model（充血实体，Product 含 publish/withdraw 行为）
- [ ] Step 6-9: 创建所有 Mapper
- [ ] Step 10: 创建 ProductRepositoryImpl
- [ ] Step 11: `mvn compile -pl blitz-product -q` → commit

---

### Task 4: 商品应用层 + 接口层 + ES 搜索

- Create: `blitz-product/src/main/java/com/blitz/product/application/service/ProductApplicationService.java`
- Create: `blitz-product/src/main/java/com/blitz/product/application/service/CategoryApplicationService.java`
- Create: `blitz-product/src/main/java/com/blitz/product/infrastructure/search/ProductSearchService.java` — ES
- Create: `blitz-product/src/main/java/com/blitz/product/api/controller/ProductController.java`
- Create: `blitz-product/src/main/java/com/blitz/product/api/controller/CategoryController.java`

- [ ] Step 1: ProductApplicationService — create/update/delete/getById/search/listByCategory
- [ ] Step 2: CategoryApplicationService — getTree/getChildren
- [ ] Step 3: ProductSearchService — search(ES 全文检索)
- [ ] Step 4: ProductController — CRUD + GET /product/search?keyword=&page=&size=
- [ ] Step 5: CategoryController — GET /product/category/tree, GET /product/category/{id}/children
- [ ] Step 6: `mvn compile -pl blitz-product -q` → commit

---

### Task 5: 集成验证

- [ ] Step 1: `mvn clean install -DskipTests -q` 全量编译
- [ ] Step 2: 启动应用验证 health endpoint
- [ ] Step 3: curl 测试 user/register → user/login → user/current
- [ ] Step 4: curl 测试 product CRUD + search
- [ ] Step 5: commit
