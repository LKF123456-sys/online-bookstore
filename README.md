# BookVerse 在线书店

> 基于 Spring Cloud 微服务架构 + Vue 3 前后端分离的在线图书销售平台

## 项目简介

BookVerse 是一个功能完整的在线图书销售平台，采用 Spring Cloud 微服务架构 + Vue 3 前后端分离设计。项目包含用户管理、商品管理、订单处理、营销活动、消息通知等核心业务模块，集成 AI 多智能体系统提供智能客服与推荐能力，并提供独立的管理后台进行数据可视化和运营管控。

### 核心特性

- **前后端分离**：Vue 3 + TypeScript 构建用户前台和管理后台，Naive UI 组件库
- **微服务架构**：9 个独立服务模块（含 AI 智能体服务），松耦合高内聚
- **AI 多智能体**：Spring AI 集成，4 Agent 协作架构（客服/推荐/评价/编排），Tool Calling + SSE 流式对话
- **RAG 检索增强**：VectorStore 语义搜索 + 领域知识库，关键词搜索与意图理解双通道
- **MCP 协议服务**：手写 JSON-RPC 2.0 over SSE 实现，10 个工具暴露给 Claude Desktop 等外部客户端
- **多模型切换**：OpenAI / Ollama / DashScope 三种 LLM 通过配置一键切换
- **安全认证**：JJWT 签名验证 + RBAC 角色鉴权，网关统一拦截
- **Token 黑名单**：基于 Redis 实现 JWT 登出即失效，Gateway 实时校验
- **接口限流**：Spring Cloud Gateway + Redis 令牌桶，登录/注册接口防暴力破解
- **服务发现**：Nacos 注册中心实现服务自动发现
- **远程调用**：OpenFeign 声明式服务间通信 + Fallback 降级
- **全文搜索**：Elasticsearch 支持商品搜索
- **消息队列**：RabbitMQ 异步消息处理
- **数据缓存**：Redis 缓存加速（SCAN 替代 KEYS）
- **分布式 ID**：Snowflake 算法生成全局唯一 ID（订单号、购物车项 ID）
- **统一异常处理**：BusinessException + HTTP 状态码语义正确化（401/403/404/409）
- **单元测试**：JUnit 5 + Mockito，覆盖核心业务逻辑（登录、下单、库存扣减等）
- **数据库约束**：13 条外键约束保障数据完整性，类型一致性修复
- **全链路日志**：SLF4J 结构化日志覆盖所有核心业务操作
- **监控体系**：Actuator + Prometheus 指标采集
- **容器化部署**：Docker Compose 一键启动全部 12 个服务
- **K8s 就绪**：7 个微服务独立 Deployment/Service，Secret 敏感配置分离
- **CI/CD**：Jenkins + GitHub Actions 双流水线，per-service 镜像构建

---

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | 编程语言 |
| Spring Boot | 3.2.5 | 应用开发框架 |
| Spring Cloud | 2023.0.1 | 微服务框架 |
| Spring Cloud Alibaba | 2023.0.1.0 | 阿里巴巴微服务组件 |
| MyBatis-Plus | 3.5.7 | ORM 持久层框架 |
| JJWT | 0.12.6 | JWT 令牌生成与验证 |
| SpringDoc OpenAPI | 2.6.0 | API 文档生成 |
| Spring AI | 1.0.0-M6 | LLM 集成框架（ChatModel、Tool Calling、SSE 流式） |
| Resilience4j | 2.2.0 | 熔断器 + 重试 + 限流 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4+ | 前端框架（Composition API） |
| TypeScript | 5.4+ | 类型安全 |
| Vite | 5+ | 构建工具 |
| Pinia | 2.1+ | 状态管理 |
| Vue Router | 4.3+ | 路由管理（路由守卫） |
| Axios | 1.7+ | HTTP 客户端（拦截器） |
| Naive UI | latest | UI 组件库 |
| ECharts | 5+ | 管理后台数据可视化 |
| vue-i18n | 9.14+ | 国际化（zh-CN/en-US） |
| Playwright | 1.50+ | E2E 端到端测试 |

### 微服务基础设施

| 组件 | 用途 |
|------|------|
| Spring Cloud Gateway | API 网关，JWT 签名验证 + RBAC 鉴权 + 路由转发 |
| Nacos | 服务注册发现 + 配置中心 |
| OpenFeign | 声明式 HTTP 远程调用 |
| LoadBalancer | 客户端负载均衡 |

### 数据存储与中间件

| 组件 | 用途 |
|------|------|
| MySQL 8.0 | 关系型数据库 |
| Redis | 缓存服务（Lettuce 客户端，SCAN 批量清除） |
| Elasticsearch 8.x | 全文搜索引擎 |
| RabbitMQ | 消息队列（AMQP 协议） |

---

## 项目结构

```
online-bookstore/
├── pom.xml                          # 父工程 POM（统一依赖管理）
├── sql/
│   └── init.sql                     # 数据库初始化脚本（15 张表 + 种子数据）
│
├── bookstore-common/                # 公共模块
│   └── src/main/java/.../common/
│       ├── api/                     # Result 统一返回、DTO、VO
│       ├── entity/                  # 15 个数据库实体类
│       ├── config/                  # 全局配置（CORS、Redis、MyBatis-Plus、Jackson、异常处理）
│       ├── exception/               # BusinessException 业务异常
│       ├── security/                # JwtUtil（签名验证）、UserIdFilter
│       └── util/                    # PasswordUtil、SnowflakeIdGenerator
│
├── bookstore-gateway/               # API 网关（端口 8080）
│   ├── filter/AuthFilter.java       # JJWT 签名验证 + RBAC 鉴权 + Token 黑名单检查
│   └── config/RateLimiterConfig.java # 基于 Redis 的接口限流（IP 维度令牌桶）
│
├── bookstore-user/                  # 用户服务（端口 8081）
├── bookstore-product/               # 商品服务（端口 8082）
├── bookstore-order/                 # 订单服务（端口 8083）
├── bookstore-promotion/             # 营销服务（端口 8085）
├── bookstore-admin/                 # 管理后台 BFF（端口 8086）
├── bookstore-message/               # 消息服务（端口 8087）
│
├── bookstore-agent/                 # AI 智能体服务（端口 8089）
│   └── src/main/java/.../agent/
│       ├── config/                  # AiModelConfig（多模型切换）、RagConfig（向量存储）、SecurityConfig
│       ├── agent/                   # 4 Agent（客服、推荐、评价、编排）
│       ├── tools/                   # Tool Calling 工具集（@Tool 注解）
│       ├── rag/                     # RAG 检索增强（KnowledgeBaseService、RagSearchTool）
│       ├── mcp/                     # MCP 协议服务（JSON-RPC 2.0 over SSE）
│       ├── feign/                   # Feign 客户端（调用现有微服务）
│       ├── controller/              # ChatController（SSE 流式 + 同步对话）
│       ├── service/                 # ChatMemoryService（Redis + MySQL 双层记忆）
│       ├── entity/                  # ChatHistory、ChatSession
│       └── mapper/                  # MyBatis-Plus Mapper
│
├── bookstore-frontend/              # 用户前台（Vue 3 + TS + Naive UI）
│   └── src/
│       ├── api/                     # 9 个 API 模块（含 agent.ts AI 对话接口）
│       ├── components/              # AppHeader、AppFooter、ProductCard
│       ├── layouts/                 # DefaultLayout
│       ├── pages/                   # 14 个页面（首页、商品、购物车、订单、个人中心、智能助手...）
│       ├── router/                  # 路由配置（懒加载 + 路由守卫）
│       ├── stores/                  # Pinia 状态管理（auth、cart、message）
│       └── types/                   # TypeScript 类型定义
│
├── bookstore-admin-frontend/        # 管理后台（Vue 3 + TS + Naive UI + ECharts）
│   └── src/
│       ├── api/                     # 10 个 API 模块
│       ├── views/                   # 12 个页面（Dashboard、商品/订单/用户/评价管理...）
│       ├── router/                  # 路由配置（管理员权限守卫）
│       ├── stores/                  # Pinia 状态管理
│       └── types/                   # TypeScript 类型定义
│
├── docker/                          # Docker 构建文件
│   ├── Dockerfile.service           # 通用微服务 Dockerfile（多阶段构建）
│   ├── Dockerfile.frontend          # 用户前端 Dockerfile
│   ├── Dockerfile.admin-frontend    # 管理后台前端 Dockerfile
│   ├── nginx-frontend.conf          # 用户前端 Nginx 配置
│   └── nginx-admin.conf             # 管理后台 Nginx 配置
├── docker-compose.yml               # 12 服务编排（微服务 + 前端 + 基础设施）
├── start-dev.sh                     # Linux/Mac 本地开发启动脚本
├── start-dev.bat                    # Windows 本地开发启动脚本
├── elk/                             # ELK 日志系统配置
├── k8s/                             # Kubernetes 部署配置
│   ├── namespace.yaml               # 命名空间
│   ├── secret.yaml                  # 敏感配置（DB密码、JWT密钥）
│   ├── configmap.yaml               # 非敏感配置（Nacos地址、DB URL）
│   ├── deployment.yaml              # 7个后端微服务 Deployment + Service
│   ├── frontend-deployment.yaml     # 前端 Deployment + Service
│   └── ingress.yaml                 # Ingress 路由规则
├── Jenkinsfile                      # Jenkins CI/CD 流水线（per-service 镜像构建 + 自动 rollback）
├── docs/                            # 项目文档
│   ├── grafana/                     # Grafana Dashboard JSON（AI Agent 监控面板）
│   ├── prometheus/                  # Prometheus 告警规则 YAML
│   └── benchmark/                   # 性能基准测试脚本 + 参考数据
└── .github/workflows/ci.yml         # GitHub Actions CI（后端+前端并行构建 + per-service Docker）
```

---

## 端口分配

| 服务 | 端口 | 说明 |
|------|------|------|
| **用户前台** | 5173 | Vue 开发服务器（生产环境由 Nginx 托管 :80） |
| **管理后台** | 5173 | Vue 开发服务器（生产环境由 Nginx 托管 :81） |
| **bookstore-gateway** | 8080 | API 网关（统一入口） |
| **bookstore-user** | 8081 | 用户服务 |
| **bookstore-product** | 8082 | 商品服务 |
| **bookstore-order** | 8083 | 订单服务 |
| **bookstore-promotion** | 8085 | 营销服务 |
| **bookstore-agent** | 8089 | AI 智能体服务（SSE 流式对话） |
| **bookstore-admin** | 8086 | 管理后台 BFF（API 代理 + 操作日志） |
| **bookstore-message** | 8087 | 消息服务 |
| Nacos | 8848 | 注册中心 / 配置中心 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Elasticsearch | 9200 | 搜索引擎 |
| RabbitMQ | 5672 | 消息队列 |

---

## 微服务架构

### 架构总览

```
    ┌──────────────────┐     ┌──────────────────┐
    │  用户前台 (Vue)   │     │  管理后台 (Vue)   │
    │  :5173 / :80     │     │  :5173 / :81     │
    └────────┬─────────┘     └────────┬─────────┘
             │                        │
             └───────────┬────────────┘
                         │
                  ┌──────▼──────┐
                  │   Gateway   │ :8080
                  │ JWT + RBAC  │
                  └──────┬──────┘
                         │
       ┌────────┬────────┼────────┬────────┐
       │        │        │        │        │
  ┌────▼───┐ ┌──▼────┐ ┌─▼─────┐ ┌▼───────┐ ┌▼───────┐
  │  User  │ │Product│ │ Order │ │Promot. │ │Message │
  │  :8081 │ │ :8082 │ │ :8083 │ │ :8085  │ │ :8087  │
  └────────┘ └───┬───┘ └───┬───┘ └────────┘ └────────┘
                 │         │
                 │  Feign  │
                 │◄────────┘
                 │
          ┌──────┴──────┐
          │   Admin BFF  │ :8086
          │  API 代理     │
          └─────────────┘
```

### 认证与鉴权流程

```
1. 用户登录 → POST /api/auth/login
2. User 服务验证密码（BCrypt），使用 JJWT 生成签名 Token
3. Token 包含 userId、username、role（user/admin）
4. 后续请求携带 Authorization: Bearer <token>
5. Gateway AuthFilter 拦截：
   a. JJWT 验签（签名 + 过期时间）
   b. 检查 Redis 黑名单（jwt:blacklist:{token}）→ 已登出则返回 401
6. 提取 userId 和 role，注入 X-User-Id / X-User-Role 请求头
7. /admin/** 路径额外校验 role == "admin"（RBAC）
8. 转发到目标微服务
9. 下游服务通过 UserIdFilter 获取当前用户身份

登出流程：
1. 用户登出 → POST /api/auth/logout（携带 Token）
2. User 服务将 Token 写入 Redis 黑名单，TTL = Token 剩余有效期
3. 后续该 Token 的任何请求都会被 Gateway 黑名单检查拦截
```

### 接口限流

Gateway 对认证类接口实施基于 Redis 令牌桶的 IP 级限流：

| 接口 | 限流策略 | 说明 |
|------|----------|------|
| `/api/auth/login` | 10 req/s per IP | 防暴力破解密码 |
| `/api/auth/register` | 5 req/s per IP | 防批量注册 |

超限请求返回 `429 Too Many Requests`。

### 服务间调用关系

| 调用方 | 被调用方 | 方式 | 说明 |
|--------|----------|------|------|
| Gateway | 所有服务 | 路由转发 | JWT 签名验证 + RBAC 后转发 |
| Admin BFF | User / Product / Order / Promotion / Message | OpenFeign | API 代理层 |
| Order | Product | OpenFeign | 查询商品信息、扣减库存 |
| Order | Product | FallbackFactory | 服务降级（商品服务不可用时） |

---

## 数据库设计

数据库名：`bookstore`，初始化脚本：`sql/init.sql`（15 张表 + 种子数据 + 外键约束）

### 数据表

| 表名 | 说明 | 主要字段 | 外键约束 |
|------|------|----------|----------|
| `account` | 用户账户 | userid, email, password, role, status, 地址信息 | — |
| `product` | 商品 | productid, category, name, author, price, stock, sales | category → category |
| `category` | 商品分类 | categoryid, categoryname, categorydesc | — |
| `product_sku` | 商品 SKU | product_id, sku_name, specs(JSON), price, stock | product_id → product |
| `product_spec` | 商品规格 | product_id, spec_name, spec_values(JSON) | product_id → product |
| `cart` | 购物车 | cartid, userid | userid → account |
| `cartitem` | 购物车项 | cartid, productid, quantity | cartid → cart, productid → product |
| `orders` | 订单 | orderid, userid, totalprice, status, 收货地址 | userid → account |
| `order_item` | 订单项 | order_id, product_id, quantity, price | order_id → orders, product_id → product |
| `coupon` | 优惠券 | name, type, threshold, discount, total_count | — |
| `user_coupon` | 用户优惠券 | user_id, coupon_id(BIGINT), is_used | user_id → account, coupon_id → coupon |
| `book_review` | 图书评价 | product_id, user_id, rating, content, reply | product_id → product, user_id → account |
| `message` | 站内消息 | sender_id, receiver_id, content, read_status | — |
| `announcement` | 系统公告 | title, content, status | — |
| `admin_log` | 操作日志 | admin_name, operation, target, detail, ip | — |

### 种子数据

初始化脚本包含以下测试数据：

| 数据 | 内容 |
|------|------|
| 管理员账号 | admin / admin123 |
| 测试用户 | testuser / 123456, zhangsan / 123456 |
| 商品 | 15 本图书，覆盖 6 个分类 |
| 分类 | 文学小说、科技计算机、历史人文、经济管理、生活百科、少儿读物 |
| 优惠券 | 3 张（满减券、折扣券、大额券） |
| 公告 | 2 条系统公告 |
| 订单 | 3 个测试订单（含订单项） |
| 评价 | 3 条图书评价（含商家回复） |
| 消息 | 2 条站内消息 |

---

## API 接口

所有 API 通过网关（:8080）统一暴露，返回格式：

```json
{ "code": 200, "message": "success", "data": {} }
```

### 用户服务 (:8081)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/login` | 用户登录 | 无需 |
| POST | `/api/auth/register` | 用户注册 | 无需 |
| POST | `/api/auth/logout` | 用户登出（Token 加入黑名单） | 需要 |
| GET | `/api/user/{id}` | 查询用户信息 | 需要 |
| PUT | `/api/user/{id}/password` | 修改密码 | 需要 |
| PUT | `/api/user/{id}/profile` | 修改个人资料 | 需要 |
| GET | `/admin/user/list` | 用户列表（分页） | admin |
| PUT | `/admin/user/{id}/status` | 启用/禁用用户 | admin |
| DELETE | `/admin/user/{id}` | 删除用户 | admin |

### 商品服务 (:8082)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/product/list` | 商品列表（分页/筛选/排序） | 无需 |
| GET | `/api/product/{id}` | 商品详情 | 无需 |
| GET | `/api/product/recommend` | 推荐商品 | 无需 |
| GET | `/api/product/hot` | 热门商品 | 无需 |
| GET | `/api/search` | 全文搜索（Elasticsearch） | 无需 |
| GET | `/api/category/list` | 分类列表 | 无需 |
| POST | `/admin/product` | 新增商品 | admin |
| PUT | `/admin/product/{id}` | 修改商品 | admin |
| DELETE | `/admin/product/{id}` | 删除商品 | admin |

### 订单服务 (:8083)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/order` | 创建订单 | 需要 |
| GET | `/api/order/{id}` | 订单详情 | 需要 |
| GET | `/api/order/list` | 订单列表 | 需要 |
| POST | `/api/order/{id}/pay` | 支付订单 | 需要 |
| POST | `/api/order/{id}/cancel` | 取消订单 | 需要 |
| POST | `/api/order/{id}/confirm` | 确认收货 | 需要 |
| GET | `/api/cart` | 获取购物车 | 需要 |
| POST | `/api/cart` | 添加商品 | 需要 |
| PUT | `/api/cart/item` | 更新数量 | 需要 |
| DELETE | `/api/cart/item/{productId}` | 移除商品 | 需要 |
| GET | `/admin/order/list` | 全部订单 | admin |
| POST | `/admin/order/{id}/ship` | 订单发货 | admin |

### 营销服务 (:8085)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/coupon/list` | 可用优惠券 | 无需 |
| POST | `/api/coupon/{id}/claim` | 领取优惠券 | 需要 |
| GET | `/api/coupon/my` | 我的优惠券 | 需要 |
| GET | `/api/review/product/{productId}` | 商品评价 | 无需 |
| POST | `/api/review` | 提交评价 | 需要 |
| GET | `/api/review/my` | 我的评价 | 需要 |
| GET | `/api/announcement/active` | 活跃公告 | 无需 |
| GET/POST/PUT/DELETE | `/admin/coupon/**` | 优惠券管理 | admin |
| GET/POST/DELETE | `/admin/review/**` | 评价管理 | admin |
| GET/POST/PUT/DELETE | `/admin/announcement/**` | 公告管理 | admin |

### 消息服务 (:8087)

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/message/list` | 消息列表 | 需要 |
| PUT | `/api/message/{id}/read` | 标记已读 | 需要 |
| PUT | `/api/message/read-all` | 全部已读 | 需要 |
| GET | `/api/message/unread-count` | 未读数量 | 需要 |
| POST | `/admin/message/broadcast` | 系统广播 | admin |

---

## 前端页面

### 用户前台（bookstore-frontend）

技术栈：Vue 3 + TypeScript + Vite + Pinia + Naive UI

| 页面 | 路由 | 说明 |
|------|------|------|
| 首页 | `/` | 公告轮播、分类导航、热门商品、推荐商品 |
| 商品列表 | `/products` | 搜索、分类筛选、排序、分页、商品网格 |
| 商品详情 | `/product/:id` | 商品信息、评价列表、相关推荐、加入购物车 |
| 登录 | `/login` | 用户名密码登录 |
| 注册 | `/register` | 新用户注册 |
| 购物车 | `/cart` | 商品数量调整、删除、清空、合计、去结算 |
| 结算 | `/checkout` | 收货地址、优惠券选择、订单确认、提交 |
| 订单列表 | `/orders` | 状态筛选标签页、支付/取消/确认收货 |
| 订单详情 | `/order/:id` | 订单信息、商品列表、支付明细 |
| 个人中心 | `/profile` | 查看/编辑个人信息、修改密码 |
| 消息中心 | `/messages` | 消息列表、标记已读、消息详情 |
| 优惠券 | `/coupons` | 可领优惠券、我的优惠券 |
| 我的评价 | `/reviews` | 评价列表、删除评价 |

### 管理后台（bookstore-admin-frontend）

技术栈：Vue 3 + TypeScript + Vite + Pinia + Naive UI + ECharts

| 页面 | 路由 | 说明 |
|------|------|------|
| 管理员登录 | `/admin/login` | 管理员身份验证（role=admin） |
| 数据大屏 | `/admin/dashboard` | 统计卡片、订单状态饼图、热销 TOP10 柱状图、低库存预警、操作日志 |
| 商品管理 | `/admin/products` | 数据表格、搜索、新增/编辑弹窗、上下架、删除 |
| 订单管理 | `/admin/orders` | 状态筛选、详情抽屉、发货操作 |
| 用户管理 | `/admin/users` | 搜索、启用/禁用、删除 |
| 分类管理 | `/admin/categories` | 分类列表 |
| 优惠券管理 | `/admin/coupons` | 新增/编辑、启用/禁用、删除 |
| 评价管理 | `/admin/reviews` | 屏蔽/取消屏蔽、置顶、回复、删除 |
| 公告管理 | `/admin/announcements` | 新增/编辑/删除 |
| 消息管理 | `/admin/messages` | 消息列表、系统广播 |
| 操作日志 | `/admin/logs` | 只读日志表格、搜索、详情 |

---

## 前端工程化

### 国际化

前后端分离架构下，管理后台基于 vue-i18n 9.x 提供完整国际化支持：

```typescript
// 组件内使用
const { t } = useI18n()
{{ t('nav.dashboard') }}

// 切换语言
const { locale } = useI18n()
locale.value = 'en-US'
```

| 语言 | 文件 | 覆盖范围 |
|------|------|---------|
| zh-CN | `src/i18n/locales/zh-CN.ts` | 导航、登录、仪表盘、通用操作 |
| en-US | `src/i18n/locales/en-US.ts` | 导航、登录、仪表盘、通用操作 |

### 环境变量配置

前端服务地址通过 Vite 环境变量统一管理，不再硬编码端口：

| 变量 | 用途 | 开发环境默认值 |
|------|------|---------------|
| `VITE_API_GATEWAY_URL` | API 网关地址 | `http://localhost:8080` |
| `VITE_ADMIN_FRONTEND_URL` | 管理后台前端地址 | `http://localhost:5174` |
| `VITE_USER_FRONTEND_URL` | 用户前端地址 | `http://localhost:5173` |

配置位于各前端项目的 `.env.development`（开发环境）和 `.env.production`（生产环境）文件中。

### API 文档

管理后台侧边栏「API 文档」入口，通过 iframe 嵌入 Swagger UI。也可直接访问网关地址：

```
http://localhost:8080/swagger-ui.html
```


## 快速开始

### 环境要求

| 工具 | 版本 | 用途 |
|------|------|------|
| JDK | 21+ | 后端编译运行 |
| Maven | 3.9+ | 后端构建 |
| Node.js | 18+ | 前端构建 |
| npm | 9+ | 前端包管理 |
| MySQL | 8.0+ | 数据库 |
| Redis | 6.0+ | 缓存 |
| Nacos | 2.3+ | 服务注册与配置 |

### 可选组件

- Elasticsearch 8.x（商品全文搜索）
- RabbitMQ 3.x（消息队列）

### 必需环境变量

启动前必须设置以下环境变量（无默认值，未设置将导致启动失败）：

```bash
# JWT 签名密钥（至少 256 位，所有服务必须使用相同密钥）
export JWT_SECRET=your-secret-key-at-least-256-bits-long-here-for-hs256
```

可选环境变量（有默认值）：

```bash
export DB_URL=jdbc:mysql://localhost:3306/bookstore?...
export DB_USERNAME=root
export DB_PASSWORD=root
export REDIS_HOST=localhost
export REDIS_PORT=6379
export NACOS_SERVER_ADDR=localhost:8848
export JWT_EXPIRATION=86400000    # Token 有效期，默认 24 小时
```

### 方式一：本地开发

#### 1. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

#### 2. 启动基础设施

```bash
# 启动 Nacos（standalone 模式）
sh nacos/bin/startup.sh -m standalone

# 启动 Redis
redis-server

# 启动 MySQL（如未运行）
mysql.server start
```

#### 3. 编译并启动后端

```bash
# 编译所有模块
mvn clean package -DskipTests

# 按顺序启动服务（每个服务在独立终端运行）
java -jar bookstore-gateway/target/*.jar     # :8080
java -jar bookstore-user/target/*.jar        # :8081
java -jar bookstore-product/target/*.jar     # :8082
java -jar bookstore-order/target/*.jar       # :8083
java -jar bookstore-promotion/target/*.jar   # :8085
java -jar bookstore-message/target/*.jar     # :8087
java -jar bookstore-admin/target/*.jar       # :8086
```

或使用一键启动脚本：

```bash
# Linux / macOS
chmod +x start-dev.sh
./start-dev.sh

# Windows
start-dev.bat
```

#### 4. 启动前端

```bash
# 用户前台（新开终端）
cd bookstore-frontend
npm install
npm run dev          # http://localhost:5173

# 管理后台（新开终端）
cd bookstore-admin-frontend
npm install
npm run dev          # http://localhost:5173
```

#### 5. 访问系统

| 地址 | 说明 |
|------|------|
| http://localhost:5173 | 用户前台（bookstore-frontend） |
| http://localhost:5173 | 管理后台（bookstore-admin-frontend） |
| http://localhost:8080 | API 网关入口 |
| http://localhost:8848/nacos | Nacos 控制台 |
| http://localhost:8080/swagger-ui.html | Swagger API 文档 |
| http://localhost:5174/admin/api-docs | 管理后台 API 文档页 |

### 方式二：Docker Compose 一键部署

```bash
# 构建并启动全部 12 个服务
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f gateway user product order

# 停止所有服务
docker-compose down
```

Docker Compose 包含的服务：

| 服务 | 端口 | 说明 |
|------|------|------|
| mysql | 3306 | MySQL 8.0（自动导入 init.sql） |
| redis | 6379 | Redis 7 |
| nacos | 8848 | Nacos 2.3（standalone） |
| gateway | 8080 | API 网关 |
| user | 8081 | 用户服务 |
| product | 8082 | 商品服务 |
| order | 8083 | 订单服务 |
| promotion | 8085 | 营销服务 |
| admin | 8086 | 管理后台 BFF |
| message | 8087 | 消息服务 |
| frontend | 80 | 用户前台（Nginx） |
| admin-frontend | 81 | 管理后台（Nginx） |

### 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 测试用户 | testuser | 123456 |
| 测试用户 | zhangsan | 123456 |

---

## Kubernetes 部署

```bash
# 按顺序部署
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/frontend-deployment.yaml
kubectl apply -f k8s/ingress.yaml

# 查看状态
kubectl get pods -n bookverse
kubectl get svc -n bookverse
kubectl get ingress -n bookverse
```

| 文件 | 说明 |
|------|------|
| `namespace.yaml` | 命名空间 `bookverse` |
| `secret.yaml` | 敏感配置（DB 密码、JWT 密钥），部署前替换 base64 值 |
| `configmap.yaml` | 非敏感配置（Nacos 地址、DB URL） |
| `deployment.yaml` | 7 个后端微服务各自独立 Deployment + Service（2 副本，健康检查） |
| `frontend-deployment.yaml` | 用户前台 + 管理后台前端 Deployment + Service |
| `ingress.yaml` | Ingress 路由（API → Gateway，默认 → 前端） |

---

## ELK 日志系统

```bash
cd elk && docker-compose -f docker-compose-elk.yml up -d
```

| 组件 | 端口 | 说明 |
|------|------|------|
| Elasticsearch | 9200 | 日志存储和搜索 |
| Logstash | 5044 | 日志处理管道 |
| Kibana | 5601 | 日志可视化 |
| Filebeat | - | 日志采集器 |

---

## CI/CD

### GitHub Actions

触发条件：Push 到 `main`/`develop` 或 PR 到 `main`。

流水线分 3 个 Job 并行/串行执行：

1. **backend-build**：JDK 21 → Maven 编译 → 测试 → 打包 → 上传 JAR
2. **frontend-build**：Node.js 18 → npm ci → lint → build（user + admin 两个项目 matrix 并行）
3. **docker**（仅 main 分支）：为 9 个服务（7 后端 + 2 前端）分别构建 Docker 镜像

### Jenkins

Jenkinsfile 定义完整流程：

1. 代码检出
2. 后端构建与测试（JUnit 报告收集）
3. 前端构建与检查（user + admin）
4. 9 个服务分别构建 Docker 镜像并推送到 Registry
5. K8s 滚动更新（`kubectl set image` + `rollout status`）
6. 健康检查（重试机制，最多 5 次，间隔 10 秒）
7. 失败时自动 rollback 所有 Deployment

---

## 常见问题

### Q: 前端页面访问后端接口报 401？

A: 确认用户已登录并携带有效的 JWT Token。前端 Axios 拦截器会自动在请求头添加 `Authorization: Bearer <token>`。如果 Token 过期，会自动跳转到登录页。

### Q: 管理后台登录后提示权限不足？

A: 确认登录的用户 role 为 `admin`。网关对 `/admin/**` 路径进行 RBAC 校验，非管理员角色会被返回 403。

### Q: 提交订单失败？

A: 确认 bookstore-order 和 bookstore-product 服务都在运行，订单服务通过 OpenFeign 调用商品服务扣减库存。

### Q: Elasticsearch 搜索无结果？

A: 确认 Elasticsearch 服务已启动，并且商品数据已同步到 ES 索引。可通过 `/api/search?keyword=xxx` 测试。

### Q: Docker Compose 启动后服务不健康？

A: 各 Java 服务依赖 MySQL、Redis、Nacos 的健康检查。可通过 `docker-compose logs <service>` 查看具体日志排查。Nacos 启动较慢，可能需要等待 30-60 秒。

### Q: 本地开发时前端接口 404？

A: Vite 开发服务器已配置代理，`/api` 请求会转发到网关 `http://localhost:8080`。确认网关已启动。

### Q: 启动时报 `jwt.secret` 属性缺失？

A: 项目已移除 JWT 密钥的硬编码默认值。启动前必须设置 `JWT_SECRET` 环境变量：

```bash
export JWT_SECRET=your-secret-key-at-least-256-bits-long-here-for-hs256
```

### Q: 登出后 Token 仍然有效？

A: 确认 Gateway 和 User 服务都连接了同一个 Redis 实例。登出时 Token 会被写入 Redis 黑名单（`jwt:blacklist:{token}`），Gateway 在每次请求时检查该 key。如果 Redis 不可用，Gateway 会降级为不检查黑名单。

### Q: 登录接口返回 429？

A: 网关对 `/api/auth/login` 和 `/api/auth/register` 实施了基于 IP 的令牌桶限流。登录限制每秒 10 次，注册限制每秒 5 次。如需调整限流参数，修改 `bookstore-gateway/application.yml` 中的 `redis-rate-limiter` 配置。

---

## 模块依赖关系

```
bookstore-common（基础层）
    ↑
    ├── bookstore-user
    ├── bookstore-product
    ├── bookstore-order（依赖 product 的 Feign 接口）
    ├── bookstore-promotion
    ├── bookstore-message
    ├── bookstore-admin（依赖所有服务的 Feign 接口）
    └── bookstore-gateway（独立，依赖 common 中的 JwtUtil）
```

---

## 单元测试

项目使用 JUnit 5 + Mockito 编写单元测试，覆盖核心业务逻辑。

### 测试文件

| 模块 | 测试类 | 用例数 | 覆盖范围 |
|------|--------|--------|----------|
| bookstore-user | `AccountServiceTest` | 16 | 登录、注册、登出(Token黑名单)、查询、改密、修改资料、管理员功能 |
| bookstore-order | `OrderServiceTest` | 14 | 创建订单、支付、取消(库存恢复)、确认收货、发货、订单查询 |
| bookstore-order | `CartServiceTest` | 13 | 获取购物车、添加商品(新增/累加)、更新数量、删除、清空 |
| bookstore-product | `ProductServiceTest` | 15 | 列表查询(分页/筛选)、详情(缓存命中/未命中)、推荐热门、库存扣减、增删改 |
| bookstore-gateway | `AuthFilterTest` | 13 | 白名单放行、Token验证(有效/过期/无效)、黑名单拦截、RBAC鉴权 |
| bookstore-common | `ResultTest` | 5 | 统一返回结果封装 |
| bookstore-common | `BusinessExceptionTest` | 3 | 业务异常类 |
| bookstore-admin | `AuthRestControllerTest` | 9 | 管理后台认证接口 |
| bookstore-admin | `UserServiceTest` | 5 | 管理后台用户服务(Feign代理) |
| bookstore-agent | `OrderToolsTest` | 8 | 订单工具集（查询详情/列表/取消 + 异常降级） |
| bookstore-agent | `ProductToolsTest` | 6 | 商品工具集（搜索/详情/推荐/热销 + 空结果处理） |
| bookstore-agent | `ReviewToolsTest` | 4 | 评价工具集（获取评价/平均分计算/降级） |
| bookstore-agent | `ChatMemoryServiceTest` | 6 | 对话记忆（Redis+MySQL双层存储/降级/会话管理） |
| bookstore-agent | `ChatControllerTest` | 6 | 对话控制器（同步/SSE流式/Agent路由/历史管理） |
| bookstore-agent | `AgentNameTest` | 3 | Agent 名称标识验证 |
| bookstore-agent | `RagSearchToolTest` | 5 | RAG 语义搜索/知识库搜索/空结果/异常降级 |
| bookstore-agent | `McpServerServiceTest` | 5 | MCP 协议：initialize/tools-list/tools-call/ping/错误处理 |

### 运行测试

```bash
# 运行所有测试
export JWT_SECRET=BookVerseSecretKey2024ForJWTTokenGenerationMustBe256BitsLongEnough
mvn test

# 运行单个模块的测试
mvn test -pl bookstore-user
mvn test -pl bookstore-order
mvn test -pl bookstore-product
mvn test -pl bookstore-gateway
mvn test -pl bookstore-agent

# 查看测试报告
# 各模块 target/surefire-reports/ 目录下
```



## 配置治理

### 动态配置管理

所有熔断器、限流器、重试阈值可通过 Nacos 配置中心动态调整，无需重启服务：

```yaml
# Nacos 配置示例（bookstore-admin.yml）
bookstore:
  dynamic:
    circuit-breaker:
      product-service:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10
    feature-flags:
      new-recommend-engine: false
```

### 运行时配置 API

管理后台提供以下端点用于运行时配置管理：

| 端点 | 方法 | 说明 |
|------|------|------|
| `/admin/api/config` | GET | 查看当前运行态的全部动态配置 |
| `/admin/api/config/refresh` | POST | 手动触发 Nacos 配置刷新 |
| `/admin/api/config/status` | GET | 查看配置中心连接状态 |

### 变更审计

每次配置变更（包括 Nacos 自动推送和手动触发刷新）均通过 `AdminLogService` 记录审计日志，
可在管理后台「操作日志」中追溯：谁、什么时间、修改了什么配置。

### 覆盖率门禁

JaCoCo 覆盖率检查集成在 Maven 构建流程中，`mvn verify` 时自动执行：

| 指标 | 最低要求 | 排除项 |
|------|---------|--------|
| 指令覆盖率（INSTRUCTION） | >= 50% | Application、config、entity、dto、vo |
| 分支覆盖率（BRANCH） | >= 30% | Application、config、entity、dto、vo |

### 集成测试

基于 Testcontainers 的集成测试框架，子模块可按需引入：

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <scope>test</scope>
</dependency>
```

### E2E 测试

使用 Playwright 进行端到端测试（需先启动开发服务器）：

```bash
# 安装 Playwright（首次运行）
cd bookstore-admin-frontend
npm install
npx playwright install chromium

# 运行 E2E 测试
npx playwright test

```

| 测试场景 | 覆盖内容 |
|---------|---------|
| 登录页冒烟 | 页面渲染、标题检查 |
| 空表单校验 | 不填信息直接提交，验证仍在登录页 |
| API 健康检查 | 网关 /actuator/health 端点可达性 |

### 测试技术栈

- **JUnit 5**：测试框架，`@Test`、`@Nested`、`@DisplayName` 组织测试
- **Mockito**：Mock 框架，`@Mock`、`@InjectMocks`、`verify()` 验证交互
- **AssertJ / JUnit Assertions**：断言库
- **Spring MockMvc**：Controller 层 HTTP 请求模拟（admin 模块测试）
- **Reactor Test**：Gateway 响应式过滤器测试

---

## AI 多智能体系统

### 架构概览

```
用户 ──→ Chat.vue (SSE) ──→ Gateway ──→ ChatController
                                              │
                                    ┌─────────┴──────────┐
                                    │   OrchestratorAgent │ ← LLM 意图分类
                                    └─────────┬──────────┘
                           ┌──────────┬───────┴──────┬──────────┐
                    CustomerService  Product  ReviewAnalysis   通用对话
                       Agent       Recommend    Agent
                           │          │            │
                    ┌──────┴───┐ ┌────┴─────┐ ┌────┴────┐
                    OrderTools │ProductTools │ │ReviewTools│
                           │   │RagSearchTool│        │
                    ┌──────┴───┐ └──┬────┴──┐   ┌────┴────┐
                    bookstore-  VectorStore  │   bookstore-
                      order      (语义检索)  │    promotion
                                ┌──┴──┐     │
                         KnowledgeBaseService
                         (商品描述 + 领域知识)

                    ┌──────────────────────────────────┐
                    │         MCP Server (SSE)          │
                    │  GET  /api/agent/mcp/sse          │
                    │  POST /api/agent/mcp/message      │
                    │  10 个工具暴露给 Claude Desktop    │
                    └──────────────────────────────────┘
```

### Agent 列表

| Agent | 职责 | Tools | 触发意图 |
|-------|------|-------|---------|
| **CustomerServiceAgent** | 订单查询、取消、状态跟踪 | `queryOrderDetail`, `queryOrderList`, `cancelOrder` | "我的订单"、"取消订单" |
| **ProductRecommendAgent** | 图书搜索、语义推荐、领域知识 | `searchProducts`, `semanticSearchBooks`, `searchKnowledge`, `getProductDetail`, `getRecommendProducts`, `getHotProducts` | "推荐好书"、"适合睡前看的书" |
| **ReviewAnalysisAgent** | 评价情感分析、口碑摘要 | `getProductReviews` | "这本书评价怎么样" |
| **OrchestratorAgent** | 意图分类 + 路由分发 | — | 所有用户消息的入口 |

### RAG 检索增强生成

| 组件 | 说明 |
|------|------|
| **EmbeddingModel** | OpenAI `text-embedding-3-small`（1536 维，自动配置） |
| **VectorStore** | SimpleVectorStore（内存，可替换为 Redis/PgVector/Milvus） |
| **KnowledgeBaseService** | 启动加载知识文档 + 商品数据，每 10 分钟增量同步 |
| **RagSearchTool** | `@Tool semanticSearchBooks()` + `@Tool searchKnowledge()` |
| **知识文档** | `resources/knowledge/BookCategoryKnowledge.md`（图书分类 + 阅读建议） |

RAG 流程：用户问题 → Embedding 向量化 → VectorStore 余弦相似度检索 → Top-K 文档注入 Prompt → LLM 生成基于上下文的回复。与 ProductTools 的关键词搜索互补：精确匹配书名/作者用关键词，模糊意图（如"适合睡前看的轻松读物"）用语义搜索。

### MCP 协议服务

MCP（Model Context Protocol）是 Anthropic 提出的 AI 工具互操作标准协议。BookVerse 手写实现了 MCP Server（JSON-RPC 2.0 over SSE），将 Agent 的全部能力暴露给外部 MCP 客户端。

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/agent/mcp/sse` | GET | SSE 长连接端点（MCP 客户端首先连接此处） |
| `/api/agent/mcp/message` | POST | JSON-RPC 请求端点（请求通过 SSE 通道返回） |
| `/api/agent/mcp/tools` | GET | 工具列表（REST 调试用） |
| `/api/agent/mcp/status` | GET | Server 状态 |

**已注册的 10 个 MCP 工具：**

| 工具名 | 来源 | 说明 |
|--------|------|------|
| `search_products` | ProductTools | 关键词搜索图书 |
| `get_product_detail` | ProductTools | 获取图书详情 |
| `get_recommend_products` | ProductTools | 系统推荐 |
| `get_hot_products` | ProductTools | 热销排行 |
| `query_order_detail` | OrderTools | 查询订单详情 |
| `query_order_list` | OrderTools | 查询订单列表 |
| `cancel_order` | OrderTools | 取消订单 |
| `get_product_reviews` | ReviewTools | 获取商品评价 |
| `semantic_search_books` | RagSearchTool | RAG 语义搜索 |
| `search_knowledge` | RagSearchTool | RAG 知识库检索 |

**Claude Desktop 接入配置：**

```json
{
  "mcpServers": {
    "bookstore": {
      "url": "http://localhost:8089/api/agent/mcp/sse"
    }
  }
}
```

### 对话 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/agent/chat` | 同步对话（完整回复） |
| GET | `/api/agent/chat/stream` | SSE 流式对话（逐 token 实时推送） |
| GET | `/api/agent/history` | 获取会话历史 |
| DELETE | `/api/agent/history` | 清空会话历史 |

### 模型配置

通过环境变量 `AI_PROVIDER` 切换底层 LLM：

```bash
# OpenAI（默认）
AI_PROVIDER=openai OPENAI_API_KEY=sk-xxx OPENAI_BASE_URL=https://api.openai.com

# Ollama（本地模型）
AI_PROVIDER=ollama OLLAMA_BASE_URL=http://localhost:11434 OLLAMA_MODEL=qwen2.5:7b

# DashScope（通义千问，OpenAI 兼容协议）
AI_PROVIDER=dashscope DASHSCOPE_API_KEY=sk-xxx
```

### 可观测性

- **Grafana Dashboard**：`docs/grafana/bookstore-agent-dashboard.json`（12 个面板：请求量、P99延迟、Agent路由分布、Feign成功率等）
- **Prometheus 告警规则**：`docs/prometheus/bookstore-agent-alerts.yml`（8 条告警：服务宕机、延迟过高、错误率、内存、连接池等）
- **压测脚本**：`docs/benchmark/benchmark.sh`（wrk 压测 + 参考数据文档）

---

## 许可证

本项目仅供学习交流使用。

---

## 致谢

- Spring Boot / Spring Cloud / Spring Cloud Alibaba
- MyBatis-Plus / Nacos / Elasticsearch
- Vue.js / Naive UI / ECharts / Pinia
- **前后端完全分离**：Vue 3 + TypeScript 完全替代 JSP，AdminPageController 统一重定向到 Vue 前端（bookstore-admin-frontend），用户端由 bookstore-frontend 覆盖
- **动态配置治理**：基于 Nacos 的熔断器/限流器/重试阈值动态刷新，运行态通过 API 查看和调整阈值，每次变更自动记录审计日志
- **国际化(i18n)**：vue-i18n 9.x 集成，内置 zh-CN/en-US 双语言，组件内通过 Composition API 使用
- **E2E 测试**：Playwright 测试框架，覆盖登录冒烟、表单校验、健康检查等核心场景
- **集成测试基础设施**：Testcontainers 1.20.4 统一管理，子模块可按需引入 MySQL/Redis/RabbitMQ 容器测试
- **代码覆盖率门禁**：JaCoCo 覆盖率检查（指令 ≥ 50%、分支 ≥ 30%），PR 构建时自动拦截覆盖率下降
- **API 文档页面**：管理后台集成 Swagger UI，支持分组切换，通过网关统一路由访问

