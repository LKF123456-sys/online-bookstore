# BookVerse 在线书店

> 基于 Spring Cloud 微服务架构 + Vue 3 前后端分离的在线图书销售平台

## 项目简介

BookVerse 是一个功能完整的在线图书销售平台，采用 Spring Cloud 微服务架构 + Vue 3 前后端分离设计。项目包含用户管理、商品管理、订单处理、营销活动、消息通知等核心业务模块，并提供独立的管理后台进行数据可视化和运营管控。

### 核心特性

- **前后端分离**：Vue 3 + TypeScript 构建用户前台和管理后台，Naive UI 组件库
- **微服务架构**：8 个独立服务模块，松耦合高内聚
- **安全认证**：JJWT 签名验证 + RBAC 角色鉴权，网关统一拦截
- **服务发现**：Nacos 注册中心实现服务自动发现
- **远程调用**：OpenFeign 声明式服务间通信 + Fallback 降级
- **全文搜索**：Elasticsearch 支持商品搜索
- **消息队列**：RabbitMQ 异步消息处理
- **数据缓存**：Redis 缓存加速（SCAN 替代 KEYS）
- **分布式 ID**：Snowflake 算法生成全局唯一订单号
- **全链路日志**：SLF4J 结构化日志覆盖所有核心业务操作
- **监控体系**：Actuator + Prometheus 指标采集
- **容器化部署**：Docker Compose 一键启动全部 12 个服务

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
│   └── filter/AuthFilter.java       # JJWT 签名验证 + RBAC 角色鉴权
│
├── bookstore-user/                  # 用户服务（端口 8081）
├── bookstore-product/               # 商品服务（端口 8082）
├── bookstore-order/                 # 订单服务（端口 8083）
├── bookstore-promotion/             # 营销服务（端口 8085）
├── bookstore-admin/                 # 管理后台 BFF（端口 8086）
├── bookstore-message/               # 消息服务（端口 8087）
│
├── bookstore-frontend/              # 用户前台（Vue 3 + TS + Naive UI）
│   └── src/
│       ├── api/                     # 8 个 API 模块
│       ├── components/              # AppHeader、AppFooter、ProductCard
│       ├── layouts/                 # DefaultLayout
│       ├── pages/                   # 13 个页面（首页、商品、购物车、订单、个人中心...）
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
├── Jenkinsfile                      # Jenkins CI/CD 流水线
└── .github/workflows/ci.yml         # GitHub Actions CI 配置
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
5. Gateway AuthFilter 拦截 → JJWT 验签（签名 + 过期时间）
6. 提取 userId 和 role，注入 X-User-Id / X-User-Role 请求头
7. /admin/** 路径额外校验 role == "admin"（RBAC）
8. 转发到目标微服务
9. 下游服务通过 UserIdFilter 获取当前用户身份
```

### 服务间调用关系

| 调用方 | 被调用方 | 方式 | 说明 |
|--------|----------|------|------|
| Gateway | 所有服务 | 路由转发 | JWT 签名验证 + RBAC 后转发 |
| Admin BFF | User / Product / Order / Promotion / Message | OpenFeign | API 代理层 |
| Order | Product | OpenFeign | 查询商品信息、扣减库存 |
| Order | Product | FallbackFactory | 服务降级（商品服务不可用时） |

---

## 数据库设计

数据库名：`bookstore`，初始化脚本：`sql/init.sql`（15 张表 + 种子数据）

### 数据表

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| `account` | 用户账户 | userid, email, password, role, status, 地址信息 |
| `product` | 商品 | productid, category, name, author, price, stock, sales |
| `category` | 商品分类 | categoryid, categoryname, categorydesc |
| `product_sku` | 商品 SKU | product_id, sku_name, specs(JSON), price, stock |
| `product_spec` | 商品规格 | product_id, spec_name, spec_values(JSON) |
| `cart` | 购物车 | cartid, userid |
| `cartitem` | 购物车项 | cartid, productid, quantity |
| `orders` | 订单 | orderid, userid, totalprice, status, 收货地址 |
| `order_item` | 订单项 | order_id, product_id, quantity, price |
| `coupon` | 优惠券 | name, type, threshold, discount, total_count |
| `user_coupon` | 用户优惠券 | user_id, coupon_id, is_used |
| `book_review` | 图书评价 | product_id, user_id, rating, content, reply |
| `message` | 站内消息 | sender_id, receiver_id, content, read_status |
| `announcement` | 系统公告 | title, content, status |
| `admin_log` | 操作日志 | admin_name, operation, target, detail, ip |

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
kubectl apply -f k8s/
kubectl get pods -l app=bookverse
kubectl get svc
kubectl get ingress
```

| 文件 | 说明 |
|------|------|
| `deployment.yaml` | 后端部署（2 副本，资源限制 512Mi-1024Mi） |
| `frontend-deployment.yaml` | 前端部署 |
| `service.yaml` | ClusterIP 服务 |
| `ingress.yaml` | Ingress 路由 |
| `configmap.yaml` | 配置映射 |

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

流水线：JDK 21 → Maven 编译 → 测试 → 打包 → Docker 镜像（仅 main）。

### Jenkins

Jenkinsfile 定义完整流程：代码检出 → 后端构建 → 前端构建 → Docker 构建推送 → K8s 滚动更新 → 健康检查。

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
    └── bookstore-gateway（独立，不依赖 common）
```

---

## 许可证

本项目仅供学习交流使用。

---

## 致谢

- Spring Boot / Spring Cloud / Spring Cloud Alibaba
- MyBatis-Plus / Nacos / Elasticsearch
- Vue.js / Naive UI / ECharts / Pinia
