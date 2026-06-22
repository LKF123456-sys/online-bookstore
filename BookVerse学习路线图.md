## BookVerse 学习路线图 —— 从小白到能讲清楚这个项目

> 适用人群：有 Java 基础语法知识，没用过 Spring Boot / 微服务，想通过这个项目系统学习后端开发。
>
> 核心原则：**每个阶段只学一个新概念，其余全部用已经掌握的东西。** 不跳步、不并行。

---

### 前置准备（第 0 阶段 · 1~2 天）

开始之前确保你装好了这些工具，并且能成功启动项目：

JDK 21、Maven 3.9+、Docker Desktop、VS Code 或 IntelliJ IDEA、Git。

然后做三件事：用 `docker-compose up -d` 把 MySQL、Redis、Nacos 跑起来；用 IDEA 打开项目根目录等 Maven 下载完依赖；分别启动 `bookstore-user` 和 `bookstore-product`，用浏览器访问 `http://localhost:8081` 确认服务正常。

如果这一步就卡住了，先别往下走，把环境问题解决掉。

---

### 第 1 阶段：Spring Boot 基础（1~2 周）

**学的东西：** Spring Boot 是什么、Controller / Service / Repository 三层结构、RESTful API、MyBatis-Plus 操作数据库。

**对应模块：** `bookstore-common` + `bookstore-user`

**怎么学：**

先看 `bookstore-common` 里的 `Result.java` 和 `BusinessException.java`。这两个文件加起来不到 200 行，但你会理解"统一返回格式"和"全局异常处理"这两个在生产项目中极其重要的概念。然后看 `GlobalExceptionHandler.java`，理解 `@RestControllerAdvice` 是怎么兜底所有异常的。

接着进入 `bookstore-user`。从 `UserController.java` 开始，跟着一个注册请求走完整个链路：接收参数 → 参数校验 → 调用 Service → Service 里 BCrypt 加密密码 → MyBatis-Plus 写入数据库。登录流程类似，但多了 JWT 生成这一步——看 `JwtUtil.java`，理解 token 里放了什么信息、怎么签名、怎么设置过期时间。

**本阶段验收标准：** 不看代码，能自己用 Spring Boot + MyBatis-Plus 写一个用户注册登录接口，返回统一格式的 JSON。

**推荐阅读文件（按顺序）：**

1. `bookstore-common/.../entity/Result.java` — 统一返回体
2. `bookstore-common/.../exception/BusinessException.java` — 业务异常
3. `bookstore-common/.../handler/GlobalExceptionHandler.java` — 全局异常处理
4. `bookstore-user/.../controller/UserController.java` — 注册/登录接口
5. `bookstore-user/.../service/UserService.java` — 业务逻辑
6. `bookstore-common/.../util/JwtUtil.java` — JWT 工具类
7. `bookstore-common/.../util/PasswordUtil.java` — BCrypt 加密

---

### 第 2 阶段：完整 CRUD + 前端联调（1~2 周）

**学的东西：** 完整的增删改查、分页查询、参数校验、前后端联调。

**对应模块：** `bookstore-product`（基础 CRUD 部分）+ `bookstore-frontend`

**怎么学：**

`ProductController.java` 里有完整的商品管理接口。先从最简单的列表查询开始，理解分页是怎么做的（PageHelper 或 MyBatis-Plus 的 `Page` 对象）。然后看新增、修改、删除接口，注意 `@Validated` 注解和 DTO 上的校验规则（`@NotBlank`、`@Positive` 等）。

接着打开 `bookstore-frontend`，找到商品列表页面，看前端是怎么调接口、展示数据、处理分页的。试着在浏览器 F12 的 Network 面板里观察请求和响应，把前端发的 JSON 和后端接收的 DTO 对照起来看。

**本阶段验收标准：** 能独立给 `bookstore-product` 加一个新字段（比如"出版社"），从数据库到后端到前端全部跑通。

**推荐阅读文件（按顺序）：**

1. `bookstore-product/.../controller/ProductController.java` — CRUD 接口
2. `bookstore-product/.../dto/ProductDTO.java` — 数据传输对象 + 校验规则
3. `bookstore-product/.../service/ProductService.java` — 业务逻辑（先跳过缓存部分）
4. `bookstore-product/.../mapper/ProductMapper.java` — MyBatis 接口
5. `bookstore-frontend/src/views/ProductList.vue` — 前端商品列表页

---

### 第 3 阶段：微服务入门 —— 服务注册与网关（1 周）

**学的东西：** 微服务是什么、为什么要拆、Nacos 服务注册发现、Spring Cloud Gateway。

**对应模块：** `bookstore-gateway` + Nacos

**怎么学：**

先理解"为什么"：现在你有 user、product 两个服务，它们各跑在不同的端口。如果以后还有 order、promotion……你不可能让用户记住每个端口号，也不可能在每个服务里都写一遍登录校验。这就是网关和服务注册要解决的问题。

看 Nacos 控制台（`http://localhost:8848/nacos`），观察各个服务是怎么注册上来的。然后重点读 `bookstore-gateway` 的 `AuthFilter.java`——这是整个系统的"门卫"，所有请求先到这里，校验 JWT 是否合法，把用户 ID 塞进 Header 里再转发给下游服务。

同时看一下限流：`RateLimiterConfig.java` 用 Redis 做了 IP 级别的令牌桶限流，代码很短但概念很重要。

**本阶段验收标准：** 能画出请求从浏览器到网关再到具体微服务的完整流程图，说清楚每一步做了什么。

**推荐阅读文件（按顺序）：**

1. `bookstore-gateway/.../filter/AuthFilter.java` — JWT 校验 + 请求转发
2. `bookstore-gateway/.../config/RateLimiterConfig.java` — IP 限流
3. `bookstore-gateway/.../GatewayApplication.java` — 启动类，看 `@EnableDiscoveryClient`
4. 各服务的 `application.yml` 中 `spring.cloud.nacos` 配置 — 理解服务注册

---

### 第 4 阶段：服务间通信 —— Feign + 降级（1 周）

**学的东西：** 微服务之间怎么互相调用、Feign 声明式客户端、服务降级。

**对应模块：** `bookstore-order`（Feign 调用 product 服务）+ `bookstore-admin`（BFF 聚合）

**怎么学：**

创建订单时需要检查商品是否存在、库存是否充足，但商品数据在 product 服务里。看 `ProductFeignClient.java`，理解 Feign 是怎么把一个 HTTP 调用伪装成一个接口方法的。然后重点看 `ProductFeignFallbackFactory.java`——当 product 服务挂了怎么办？读操作返回"服务暂不可用"，写操作（扣库存）直接抛异常防止数据不一致。这个"差异化降级"的设计思路很值得学习。

`bookstore-admin` 是另一种用法：它作为 BFF（Backend-For-Frontend），用 Feign 同时调多个服务，把数据聚合好再返回给前端。

**本阶段验收标准：** 关掉 product 服务，然后尝试创建订单，观察 Fallback 是怎么触发的，日志里打印了什么。

**推荐阅读文件（按顺序）：**

1. `bookstore-order/.../feign/ProductFeignClient.java` — Feign 客户端声明
2. `bookstore-order/.../feign/ProductFeignFallbackFactory.java` — 降级策略
3. `bookstore-admin/.../controller/` — BFF 聚合层
4. `bookstore-admin/.../feign/` — Admin 模块的 Feign 客户端

---

### 第 5 阶段：Redis 缓存三件套（1~2 周）

**学的东西：** 缓存穿透、缓存击穿、缓存雪崩的原理和解决方案。

**对应模块：** `bookstore-product` 的 `ProductService.java`

**怎么学：**

这是面试必考内容，而这个项目里三种问题全解决了。打开 `ProductService.java`（约 566 行），找到查商品的逻辑：

**穿透**（查一个根本不存在的 ID）：代码里用 `##NULL##` 哨兵值缓存空结果，设 30 秒过期。这样第二次查同一个不存在的 ID 时直接返回空，不会打到数据库。

**击穿**（热点 key 过期瞬间大量请求涌入）：代码里用 Redis `SETNX` 做分布式锁，抢到锁的线程去查数据库并回填缓存，其他线程等待。锁释放用了 Lua 脚本保证原子性。

**雪崩**（大量 key 同时过期）：TTL 不是固定的 5 分钟，而是 5 分钟 + 随机 0~60 秒，让过期时间分散开。

**本阶段验收标准：** 关掉 Redis，再访问商品页面，观察系统是优雅降级（直接查数据库）还是直接崩溃。然后能口头解释三种问题的区别和解决方案。

**推荐阅读文件（按顺序）：**

1. `bookstore-product/.../service/ProductService.java` — 缓存核心逻辑（重点看 `getProductById` 方法）
2. `bookstore-common/.../config/RedisConfig.java` — Redis 配置
3. `bookstore-product/.../config/MetricsConfig.java` — 缓存命中率埋点

---

### 第 6 阶段：消息队列 RabbitMQ（1~2 周）

**学的东西：** 异步解耦、消息可靠投递、死信队列、幂等消费。

**对应模块：** `bookstore-order` 的 MQ 部分

**怎么学：**

订单创建成功后需要通知其他模块（比如积分、通知），但不可能一直同步等着。看 `OrderMessageProducer.java` 是怎么发消息的，`OrderMessageConsumer.java` 是怎么收消息的。

重点关注四个设计：手动 ACK（收到消息处理完才确认，崩了可以重发）、死信队列（重试 3 次还失败的消息进 DLQ，不会堵塞正常队列）、Redis 幂等消费（用消息 ID 做 SET，防止同一条消息被处理两次）、毒消息保护（`MAX_DELIVERY_COUNT=3`，超过直接丢弃）。

**本阶段验收标准：** 在消费者代码里故意抛一个异常，观察消息是怎么重试、最终进入死信队列的。

**推荐阅读文件（按顺序）：**

1. `bookstore-order/.../config/RabbitMQConfig.java` — 队列 + 交换机 + DLQ 配置
2. `bookstore-order/.../mq/OrderMessageProducer.java` — 发送消息
3. `bookstore-order/.../mq/OrderMessageConsumer.java` — 消费 + 手动 ACK + 幂等
4. `bookstore-order/.../service/OrderService.java` — 看订单创建后怎么触发消息

---

### 第 7 阶段：分布式事务 Saga（1~2 周）

**学的东西：** 分布式事务问题、Saga 补偿模式、幂等性。

**对应模块：** `bookstore-order` 的 `OrderService.java`

**怎么学：**

这是整个项目最复杂的一段业务逻辑（`OrderService.java` 约 685 行）。创建订单要做三件事：扣库存（远程调 product）、创建订单记录（本地数据库）、发消息（RabbitMQ）。任何一步失败都需要"补偿"——把之前做的操作撤销掉。

阅读顺序建议：先看正常流程（全成功的情况），再看幂等防重（Redis SETNX 用 MD5 哈希），然后看补偿逻辑（`deductedItems` 列表记录已扣的库存，失败时逐个恢复），最后看 `CompensationRecoveryTask.java`（补偿失败时持久化记录，定时任务重试）。

**本阶段验收标准：** 能画出订单创建的完整时序图，包括正常路径和每一种异常路径的处理方式。

**推荐阅读文件（按顺序）：**

1. `bookstore-order/.../service/OrderService.java` — 核心逻辑（分多次读，不要一次看完）
2. `bookstore-order/.../entity/CompensationRecord.java` — 补偿记录实体
3. `bookstore-order/.../task/CompensationRecoveryTask.java` — 补偿恢复定时任务
4. `bookstore-order/.../config/OrderConfig.java` — `@RefreshScope` 动态配置

---

### 第 8 阶段：Elasticsearch 搜索（1 周）

**学的东西：** ES 基本概念、全文搜索、策略模式做优雅降级。

**对应模块：** `bookstore-product` 的搜索部分

**怎么学：**

这个项目用了一个很巧妙的设计：`SearchService` 是一个接口，有两个实现——`ElasticsearchService`（ES 搜索）和 `DatabaseSearchService`（数据库 LIKE 搜索）。如果配了 ES 地址就用 ES，没配就自动降级到数据库。这是策略模式的实际应用，也是面试时很好的话题。

看 `ElasticsearchService.java` 里的 `multiMatchQuery`，理解 ES 是怎么在多个字段上做模糊搜索的。

**本阶段验收标准：** 启动和不启动 ES 两种情况下，搜索功能都能正常工作，并且你能解释 Spring 是怎么自动选择实现的。

**推荐阅读文件（按顺序）：**

1. `bookstore-product/.../service/SearchService.java` — 搜索接口
2. `bookstore-product/.../service/ElasticsearchService.java` — ES 实现
3. `bookstore-product/.../service/DatabaseSearchService.java` — 数据库降级实现
4. `bookstore-product/.../document/ProductDocument.java` — ES 文档类

---

### 第 9 阶段：DevOps 基础（1~2 周）

**学的东西：** Docker 容器化、docker-compose 编排、CI/CD 基本概念。

**对应模块：** 根目录 `Dockerfile`、`docker-compose.yml`、`.github/workflows/ci.yml`

**怎么学：**

先读根目录的 `Dockerfile`，这是一个多阶段构建：先用 Maven 镜像编译打包，再用 JRE Alpine 镜像运行。注意里面 baked 了 SkyWalking Agent、用了非 root 用户、配了 G1GC 和 OOM 堆转储——这些都是生产级实践。

然后用 `docker-compose.yml` 把所有服务跑起来，理解 14 个容器的依赖关系和启动顺序。

最后看 GitHub Actions 的 CI 配置，理解"提交代码 → 自动编译 → 自动测试 → 构建镜像 → 部署"的完整流水线。

**本阶段验收标准：** 用 `docker-compose up` 一键启动整个系统，所有服务正常访问。

**推荐阅读文件（按顺序）：**

1. `Dockerfile` — 多阶段构建
2. `docker-compose.yml` — 服务编排（14 个容器）
3. `.github/workflows/ci.yml` — GitHub Actions CI
4. `Jenkinsfile` — Jenkins 流水线（含自动回滚）
5. `k8s/deployment.yaml` — K8s 部署配置

---

### 第 10 阶段：AI Agent 集成（1~2 周）

**学的东西：** 大模型 API 调用、RAG 检索增强生成、Tool Calling、SSE 流式输出。

**对应模块：** `bookstore-agent`

**怎么学：**

这是项目最前沿的部分。先看 `ChatController.java`，理解 SSE（Server-Sent Events）是怎么让后端实时"流式"推送文字给前端的。然后看 `OrchestratorAgent.java`，它用 LLM 做意图识别，把用户问题分发给三个专业 Agent（客服、推荐、评论分析）。

RAG 部分看 `KnowledgeBaseService.java`：读取知识文档 → 文本切片 → 向量化存入 Redis → 查询时做语义搜索。`McpServerService.java` 是手写的 MCP 协议实现，用 JSON-RPC 2.0 暴露了 10 个工具给外部调用。

**本阶段验收标准：** 启动 agent 服务，在前端 Chat 页面和一个 AI 客服对话，能说出请求经过了哪些组件。

**推荐阅读文件（按顺序）：**

1. `bookstore-agent/.../controller/ChatController.java` — SSE 流式接口
2. `bookstore-agent/.../agent/OrchestratorAgent.java` — 多 Agent 编排
3. `bookstore-agent/.../agent/CustomerServiceAgent.java` — 客服 Agent + Tool Calling
4. `bookstore-agent/.../service/KnowledgeBaseService.java` — RAG 管道
5. `bookstore-agent/.../service/McpServerService.java` — MCP 协议

---

### 第 11 阶段：可观测性（3~5 天）

**学的东西：** 链路追踪、指标监控、日志聚合。

**对应模块：** SkyWalking 配置、`MetricsConfig.java`、`elk/` 目录

**怎么学：**

看 `MetricsConfig.java` 里定义的 7 个自定义业务指标（订单创建数、支付数、取消数、金额分布、库存扣减、缓存命中/未命中），理解 Micrometer 是怎么把业务数据暴露给 Prometheus 的。

打开 SkyWalking UI（`http://localhost:8888`），发几个请求，看调用链是怎么串起来的。注意代码里的 `@Tag` 注解，它给关键方法加了追踪标签。

**推荐阅读文件（按顺序）：**

1. `bookstore-order/.../config/MetricsConfig.java` — 业务指标
2. `docs/grafana/bookstore-business-dashboard.json` — Grafana 看板
3. `docs/slo/README.md` — SLO 定义
4. `docs/ops/RUNBOOK.md` — 运维手册
5. `elk/` 目录 — ELK 日志配置

---

### 学习方法建议

**每个阶段遵循"3 遍阅读法"：**

第一遍：只看文件结构和类名、方法名，不看实现细节。目的是知道"有什么"。

第二遍：从一个 HTTP 请求入手，沿着调用链一路跟到底。比如从 `POST /api/orders` 开始，经过 Gateway → AuthFilter → OrderController → OrderService → Feign → RabbitMQ，把正常路径走通。

第三遍：关注异常路径。如果 Redis 挂了会怎样？如果 product 服务超时了会怎样？如果消息消费失败了会怎样？这些"如果……怎么办"才是微服务的精髓。

**调试技巧：** 在关键位置打断点（比如 `AuthFilter`、`OrderService.createOrder`、`ProductService.getProductById`），用 IDEA 的 Debug 模式一步步走，观察变量值和调用栈。比看代码效率高 10 倍。

**时间规划总览：**

| 阶段 | 内容 | 预估时间 |
|------|------|----------|
| 0 | 环境准备 | 1~2 天 |
| 1 | Spring Boot 基础 | 1~2 周 |
| 2 | CRUD + 前后端联调 | 1~2 周 |
| 3 | 微服务入门（网关 + 注册） | 1 周 |
| 4 | Feign 服务间通信 | 1 周 |
| 5 | Redis 缓存三件套 | 1~2 周 |
| 6 | RabbitMQ 消息队列 | 1~2 周 |
| 7 | Saga 分布式事务 | 1~2 周 |
| 8 | Elasticsearch 搜索 | 1 周 |
| 9 | DevOps（Docker + CI/CD） | 1~2 周 |
| 10 | AI Agent 集成 | 1~2 周 |
| 11 | 可观测性 | 3~5 天 |

总计大约 **3~5 个月**（按每天 2~3 小时学习算）。如果你已经有一定基础，前两个阶段可以快速过，把时间花在第 5~7 阶段，那里是面试的重灾区。
