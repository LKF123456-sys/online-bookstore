# BookVerse 在线书店

> 基于 Spring Cloud 微服务架构的在线图书销售平台

## 项目简介

BookVerse 是一个功能完整的在线图书销售平台，采用 Spring Cloud 微服务架构设计。项目包含用户管理、商品管理、订单处理、营销活动、消息通知等核心业务模块，并提供管理后台进行数据可视化和运营管控。

### 核心特性

- **微服务架构**：8 个独立服务模块，松耦合高内聚
- **统一网关**：Spring Cloud Gateway 统一路由和认证
- **服务发现**：Nacos 注册中心实现服务自动发现
- **远程调用**：OpenFeign 声明式服务间通信
- **全文搜索**：Elasticsearch 支持商品搜索
- **消息队列**：RabbitMQ 异步消息处理
- **数据缓存**：Redis 缓存加速
- **API 文档**：SpringDoc OpenAPI 自动生成交互式文档
- **监控体系**：Actuator + Prometheus 指标采集

---

## 技术栈

### 核心框架

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | 编程语言 |
| Spring Boot | 3.2.5 | 应用开发框架 |
| Spring Cloud | 2023.0.1 | 微服务框架 |
| Spring Cloud Alibaba | 2023.0.1.0 | 阿里巴巴微服务组件 |
| MyBatis-Plus | 3.5.7 | ORM 持久层框架 |
| Spring Security | 内置 | 安全认证框架 |
| JWT (jjwt) | 0.12.6 | Token 认证 |
| SpringDoc OpenAPI | 2.6.0 | API 文档生成 |

### 微服务基础设施

| 组件 | 用途 |
|------|------|
| Spring Cloud Gateway | API 网关，统一路由和鉴权 |
| Nacos | 服务注册发现 + 配置中心 |
| OpenFeign | 声明式 HTTP 远程调用 |
| LoadBalancer | 客户端负载均衡 |

### 数据存储与中间件

| 组件 | 用途 |
|------|------|
| MySQL 8.0 | 关系型数据库 |
| Redis | 缓存服务（Lettuce 客户端） |
| Elasticsearch 8.x | 全文搜索引擎 |
| RabbitMQ | 消息队列（AMQP 协议） |

### 其他依赖

| 依赖 | 用途 |
|------|------|
| Lombok | 代码简化（自动生成 getter/setter） |
| Jackson | JSON 序列化 |
| HikariCP | 数据库连接池 |
| Micrometer + Prometheus | 监控指标采集 |
| Spring Boot Actuator | 应用健康检查和监控 |
| JSTL + Tomcat JSP | JSP 视图渲染 |
| Font Awesome | 图标库 |

---

## 项目结构

```
online-bookstore/
├── pom.xml                    # 父工程 POM（统一依赖管理）
├── README.md                  # 项目文档
├── checkstyle.xml             # 代码风格检查配置
│
├── bookstore-common/          # 公共模块（被所有业务模块依赖）
│   └── src/main/java/com/bookstore/common/
│       ├── api/               # API 层
│       │   ├── Result.java    # 统一返回结果包装
│       │   ├── dto/           # 数据传输对象（请求参数）
│       │   └── vo/            # 视图对象（响应数据）
│       ├── entity/            # 数据库实体类（对应表结构）
│       ├── config/            # 全局配置（异常处理、Redis、MyBatis-Plus、Jackson）
│       ├── security/          # 安全组件（JWT 工具、用户ID过滤器）
│       └── util/              # 工具类（密码加密、响应工具）
│
├── bookstore-gateway/         # API 网关服务（端口 8080）
│   └── src/main/java/com/bookstore/gateway/
│       ├── GatewayApplication.java   # 网关启动类
│       └── filter/AuthFilter.java    # 认证过滤器（JWT 验证）
│
├── bookstore-user/            # 用户服务（端口 8081）
│   └── src/main/java/com/bookstore/user/
│       ├── controller/        # 控制器（认证、用户API、管理端）
│       ├── service/           # 业务逻辑（账户服务）
│       └── mapper/            # 数据访问层
│
├── bookstore-product/         # 商品服务（端口 8082）
│   └── src/main/java/com/bookstore/product/
│       ├── controller/        # 控制器（商品API、分类、搜索、管理端）
│       ├── service/           # 业务逻辑（商品服务、ES搜索服务）
│       ├── mapper/            # 数据访问层
│       ├── repository/        # Elasticsearch 仓库
│       └── document/          # ES 文档对象
│
├── bookstore-order/           # 订单服务（端口 8083）
│   └── src/main/java/com/bookstore/order/
│       ├── controller/        # 控制器（订单API、购物车API、管理端）
│       ├── service/           # 业务逻辑（订单服务、购物车服务）
│       ├── mapper/            # 数据访问层
│       └── feign/             # Feign 远程调用客户端（调用商品服务）
│
├── bookstore-promotion/       # 营销服务（端口 8085）
│   └── src/main/java/com/bookstore/promotion/
│       ├── controller/        # 控制器（优惠券、评价、公告 API + 管理端）
│       ├── service/           # 业务逻辑（优惠券、评价、公告服务）
│       └── mapper/            # 数据访问层
│
├── bookstore-message/         # 消息服务（端口 8087）
│   └── src/main/java/com/bookstore/message/
│       ├── controller/        # 控制器（消息API + 管理端）
│       ├── service/           # 业务逻辑（消息服务）
│       └── mapper/            # 数据访问层
│
├── bookstore-admin/           # 管理后台服务（端口 8086）
│   └── src/main/
│       ├── java/com/bookstore/admin/
│       │   ├── controller/    # 控制器（页面控制器、API代理、管理API）
│       │   ├── service/       # 业务逻辑（操作日志服务）
│       │   ├── mapper/        # 数据访问层
│       │   ├── interceptor/   # 拦截器（操作日志记录）
│       │   └── config/        # 配置（安全配置、OpenAPI配置）
│       └── resources/
│           ├── application.yml
│           ├── static/        # 静态资源（CSS、JS、图片）
│           └── META-INF/resources/WEB-INF/views/  # JSP 页面
│
├── docker/                    # Docker 构建文件
├── docker-compose.yml         # Docker Compose 编排
├── Dockerfile                 # Docker 镜像构建
├── Jenkinsfile                # Jenkins CI/CD 流水线
├── elk/                       # ELK 日志系统配置
├── k8s/                       # Kubernetes 部署配置
├── nginx/                     # Nginx 反向代理配置
└── .github/workflows/ci.yml   # GitHub Actions CI 配置
```

---

## 端口分配

| 服务 | 端口 | 说明 |
|------|------|------|
| **bookstore-gateway** | 8080 | API 网关（统一入口） |
| **bookstore-user** | 8081 | 用户服务 |
| **bookstore-product** | 8082 | 商品服务 |
| **bookstore-order** | 8083 | 订单服务 |
| **bookstore-promotion** | 8085 | 营销服务 |
| **bookstore-admin** | 8086 | 管理后台（前端页面） |
| **bookstore-message** | 8087 | 消息服务 |
| Nacos | 8848 | 注册中心 / 配置中心 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Elasticsearch | 9200 | 搜索引擎 |
| RabbitMQ | 5672 | 消息队列 |

---

## 数据库设计

数据库名：`bookstore`（所有服务共用）

### 数据表

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| `account` | 用户账户 | userid, email, firstname, lastname, password, role, status, 地址信息 |
| `product` | 商品 | productid, category, name, descn, author, price, stock, sales, status |
| `category` | 商品分类 | categoryid, categoryname, categorydesc |
| `product_sku` | 商品SKU | id, product_id, sku_name, specs(JSON), price, stock |
| `product_spec` | 商品规格 | id, product_id, spec_name, spec_values(JSON) |
| `cart` | 购物车 | cartid, userid |
| `cartitem` | 购物车项 | itemid, cartid, productid, quantity |
| `orders` | 订单 | orderid, userid, totalprice, status, 账单/收货地址, 快递信息 |
| `order_item` | 订单项 | id, order_id, product_id, quantity, price |
| `coupon` | 优惠券 | id, name, type, threshold, discount, total_count, start_time, end_time |
| `user_coupon` | 用户优惠券 | id, user_id, coupon_id, is_used |
| `book_review` | 图书评价 | id, product_id, user_id, rating, content, is_top, blocked |
| `message` | 站内消息 | id, sender_id, receiver_id, content, read_status |
| `announcement` | 系统公告 | id, title, content, status |
| `admin_log` | 操作日志 | id, admin_name, operation, target, detail, ip |

### ER 关系

```
account (用户)
  ├── 1:N → orders (订单)
  ├── 1:N → cart (购物车)
  ├── 1:N → user_coupon (优惠券)
  ├── 1:N → book_review (评价)
  └── 1:N → message (消息)

product (商品)
  ├── N:1 → category (分类)
  ├── 1:N → product_sku (SKU)
  ├── 1:N → product_spec (规格)
  ├── 1:N → cartitem (购物车项)
  ├── 1:N → order_item (订单项)
  └── 1:N → book_review (评价)

orders (订单)
  └── 1:N → order_item (订单项)

cart (购物车)
  └── 1:N → cartitem (购物车项)

coupon (优惠券)
  └── 1:N → user_coupon (用户优惠券)
```

---

## 微服务架构

### 架构总览

```
                         ┌─────────────┐
                         │   浏览器     │
                         └──────┬──────┘
                                │
                         ┌──────▼──────┐
                         │   Nginx     │ :80
                         └──────┬──────┘
                                │
                         ┌──────▼──────┐
                         │   Gateway   │ :8080
                         │  路由+鉴权   │
                         └──────┬──────┘
                                │
          ┌─────────┬───────────┼───────────┬─────────┐
          │         │           │           │         │
    ┌─────▼────┐ ┌──▼───┐ ┌────▼────┐ ┌────▼───┐ ┌──▼──────┐
    │  User    │ │Product│ │  Order  │ │Promotion│ │ Message │
    │  :8081   │ │ :8082 │ │  :8083  │ │  :8085  │ │  :8087  │
    └──────────┘ └───┬───┘ └────┬────┘ └────────┘ └─────────┘
                     │          │
                     │   Feign  │
                     │◄─────────┘
                     │
              ┌──────┴──────┐
              │   Admin     │ :8086
              │ RestTemplate│──► 调用所有微服务
              └─────────────┘
```

### 服务间调用关系

| 调用方 | 被调用方 | 方式 | 说明 |
|--------|----------|------|------|
| Gateway | 所有服务 | 路由转发 | 统一入口，JWT 认证后转发 |
| Admin (PageController) | User | RestTemplate | 用户登录、注册、信息查询 |
| Admin (PageController) | Product | RestTemplate | 商品列表、详情、分类、搜索 |
| Admin (PageController) | Order | RestTemplate | 购物车操作、订单创建、订单查询 |
| Admin (PageController) | Promotion | RestTemplate | 优惠券、评价、公告 |
| Admin (PageController) | Message | RestTemplate | 消息列表、未读数 |
| Admin (AuthProxyController) | User/Promotion/Message | RestTemplate | API 代理转发 |
| Order | Product | OpenFeign | 查询商品信息、扣减库存 |
| Order | Product | FeignFallback | 服务降级（商品服务不可用时） |

### 认证流程

```
1. 用户登录 → POST /api/auth/login
2. User 服务验证密码（BCrypt），生成 JWT Token
3. 返回 Token 给客户端
4. 后续请求携带 Token（Header: Authorization: Bearer xxx）
5. Gateway 的 AuthFilter 拦截请求
6. 解析 JWT，提取 userId，放入请求头 X-User-Id
7. 转发到目标微服务
8. 目标服务通过 X-User-Id 获取当前用户
```

---

## API 接口文档

### 用户服务 (bookstore-user :8081)

#### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 用户登录（返回 JWT Token） |
| POST | `/api/auth/register` | 用户注册 |

#### 用户接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/{id}` | 查询用户信息 |
| PUT | `/api/user/{id}/password` | 修改密码 |
| PUT | `/api/user/{id}/profile` | 修改个人资料 |

#### 管理端接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/user/list` | 分页查询用户列表 |
| PUT | `/admin/user/{id}/status` | 启用/禁用用户 |
| DELETE | `/admin/user/{id}` | 删除用户 |

### 商品服务 (bookstore-product :8082)

#### 商品接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/product/list` | 商品列表（分页、筛选、排序） |
| GET | `/api/product/{id}` | 商品详情 |
| GET | `/api/product/recommend` | 推荐商品 |
| GET | `/api/product/hot` | 热门商品 |
| PUT | `/api/product/{id}/stock` | 更新库存 |

#### 搜索与分类

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/search` | 全文搜索（Elasticsearch） |
| GET | `/api/category/list` | 所有分类 |

#### 管理端接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/admin/product` | 新增商品 |
| PUT | `/admin/product/{id}` | 修改商品 |
| DELETE | `/admin/product/{id}` | 删除商品 |
| PUT | `/admin/product/{id}/status` | 上架/下架 |

### 订单服务 (bookstore-order :8083)

#### 订单接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/order` | 创建订单 |
| GET | `/api/order/{id}` | 订单详情 |
| GET | `/api/order/list` | 订单列表 |
| POST | `/api/order/{id}/pay` | 支付订单 |
| POST | `/api/order/{id}/cancel` | 取消订单 |
| POST | `/api/order/{id}/confirm` | 确认收货 |

#### 购物车接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/cart` | 获取购物车 |
| POST | `/api/cart` | 添加商品到购物车 |
| PUT | `/api/cart/item` | 更新商品数量 |
| DELETE | `/api/cart/item/{productId}` | 移除商品 |
| DELETE | `/api/cart/clear` | 清空购物车 |

#### 管理端接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/order/list` | 所有订单列表 |
| GET | `/admin/order/{id}` | 订单详情 |
| POST | `/admin/order/{id}/ship` | 订单发货 |

### 营销服务 (bookstore-promotion :8085)

#### 优惠券接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/coupon/list` | 可用优惠券列表 |
| POST | `/api/coupon/{id}/claim` | 领取优惠券 |
| GET | `/api/coupon/my` | 我的优惠券 |

#### 评价接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/review/product/{productId}` | 商品评价列表 |
| POST | `/api/review` | 提交评价 |
| GET | `/api/review/my` | 我的评价 |

#### 公告接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/announcement/active` | 活跃公告 |

#### 管理端接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST/PUT/DELETE | `/admin/coupon/**` | 优惠券管理 |
| GET | `/admin/review/list` | 评价列表 |
| POST | `/admin/review/{id}/block` | 屏蔽评价 |
| POST | `/admin/review/{id}/reply` | 回复评价 |
| DELETE | `/admin/review/{id}` | 删除评价 |
| GET/POST/PUT/DELETE | `/admin/announcement/**` | 公告管理 |

### 消息服务 (bookstore-message :8087)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/message/list` | 消息列表 |
| PUT | `/api/message/{id}/read` | 标记已读 |
| PUT | `/api/message/read-all` | 全部已读 |
| GET | `/api/message/unread-count` | 未读数量 |
| POST | `/admin/message/broadcast` | 系统广播 |

### 管理后台 (bookstore-admin :8086)

#### 操作日志

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/api/log/list` | 日志列表 |
| GET | `/admin/api/log/{id}` | 日志详情 |

#### API 文档

| 路径 | 说明 |
|------|------|
| `/api-docs.html` | Swagger UI 交互式 API 文档 |
| `/v3/api-docs` | OpenAPI JSON 规范 |

---

## 前端页面

### 前台用户页面

| 页面 | 路径 | 说明 |
|------|------|------|
| 首页 | `/` | 分类导航、推荐商品、热销排行、商品列表 |
| 登录 | `/login` | 用户登录（赛博朋克风格） |
| 注册 | `/register` | 用户注册 |
| 商品详情 | `/product/detail?id=xxx` | 商品信息、规格、评价、推荐 |
| 购物车 | `/cart` | 购物车管理、数量调整、优惠券 |
| 确认订单 | `/order` | 收货地址、支付方式、订单确认 |
| 订单历史 | `/order/history` | 用户所有订单列表 |
| 订单详情 | `/order/detail?orderId=xxx` | 订单状态、物流、商品信息 |
| 支付 | `/payment?orderId=xxx` | 多种支付方式选择 |
| 商品评价 | `/review` | 评价提交 |
| 个人中心 | `/user/profile` | 个人信息、订单、消息 |
| 帮助中心 | `/help` | 常见问题 |
| 关于我们 | `/about` | 公司介绍 |

### 管理后台页面

| 页面 | 路径 | 说明 |
|------|------|------|
| 管理员登录 | `/admin/login` | 管理员专用登录 |
| 管理首页 | `/admin/index` | 管理后台首页 |
| 数据大屏 | `/admin/dashboard` | 统计可视化（订单、收入、热销、库存预警） |
| 商品管理 | `/admin/product/list` | 商品增删改查、上下架 |
| 库存管理 | `/admin/product/stock` | 库存查看和调整 |
| 热销排行 | `/admin/product/bestseller` | 销量排行 |
| 订单管理 | `/admin/order/list` | 订单查看、发货、状态管理 |
| 用户管理 | `/admin/user/list` | 用户增删改查、启用禁用 |
| 分类管理 | `/admin/category/list` | 商品分类管理 |
| 优惠券管理 | `/admin/coupon/list` | 优惠券创建、发放 |
| 评价管理 | `/admin/review/list` | 评价审核、屏蔽、回复 |
| 公告管理 | `/admin/announcement/list` | 公告发布管理 |
| 消息管理 | `/admin/message/list` | 站内信管理 |
| 操作日志 | `/admin/log/list` | 管理员操作记录 |

---

## 快速开始

### 环境要求

- **JDK** 21+
- **Maven** 3.9+
- **MySQL** 8.0+
- **Redis** 6.0+
- **Nacos** 2.3+

### 可选组件

- **Elasticsearch** 8.x（商品搜索功能）
- **RabbitMQ** 3.x（消息队列功能）

### 1. 准备数据库

```sql
-- 创建数据库
CREATE DATABASE bookstore DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 导入表结构（如果有的话）
-- USE bookstore;
-- SOURCE bookstore.sql;
```

### 2. 启动基础设施

```bash
# 启动 Nacos（注册中心）
sh startup.sh -m standalone

# 启动 Redis
redis-server

# 启动 MySQL
mysql.server start
```

### 3. 编译项目

```bash
# 在项目根目录执行
mvn clean package -DskipTests
```

### 4. 启动服务（按顺序）

```bash
# 1. 启动网关
cd bookstore-gateway && mvn spring-boot:run

# 2. 启动用户服务
cd bookstore-user && mvn spring-boot:run

# 3. 启动商品服务
cd bookstore-product && mvn spring-boot:run

# 4. 启动订单服务
cd bookstore-order && mvn spring-boot:run

# 5. 启动营销服务
cd bookstore-promotion && mvn spring-boot:run

# 6. 启动消息服务
cd bookstore-message && mvn spring-boot:run

# 7. 启动管理后台
cd bookstore-admin && mvn spring-boot:run
```

### 5. 访问系统

| 地址 | 说明 |
|------|------|
| http://localhost:8086/admin | 管理后台（前台用户页面） |
| http://localhost:8086/admin/login | 管理员登录 |
| http://localhost:8086/api-docs.html | API 文档（Swagger UI） |
| http://localhost:8080 | API 网关入口 |
| http://localhost:8848/nacos | Nacos 控制台 |

### 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin |
| 测试用户 | 用户注册页面自行注册 | - |

---

## Docker 部署

### Docker Compose 一键启动

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看运行状态
docker-compose ps

# 查看日志
docker-compose logs -f app

# 停止所有服务
docker-compose down
```

### Docker 镜像构建

```bash
# 多阶段构建
docker build -t bookverse/backend:latest .

# 运行
docker run -d -p 8080:8080 --name bookverse bookverse/backend:latest
```

### 镜像特性

- 基础镜像：Eclipse Temurin 21 JRE Alpine（体积小）
- 非 root 用户运行（安全性）
- 时区：Asia/Shanghai
- 健康检查：`/actuator/health`
- JVM 参数：`-Xms512m -Xmx1024m -XX:+UseG1GC`

---

## Kubernetes 部署

```bash
# 部署到 K8s
kubectl apply -f k8s/

# 查看部署状态
kubectl get pods -l app=bookverse

# 查看服务
kubectl get svc

# 查看 Ingress
kubectl get ingress
```

### K8s 配置说明

| 文件 | 说明 |
|------|------|
| `deployment.yaml` | 后端部署（2 副本，资源限制 512Mi-1024Mi） |
| `frontend-deployment.yaml` | 前端部署 |
| `service.yaml` | ClusterIP 服务 |
| `ingress.yaml` | Ingress 路由（域名：bookverse.example.com） |
| `configmap.yaml` | 配置映射（数据库、Redis 等地址） |

---

## ELK 日志系统

```bash
# 启动 ELK 栈
cd elk && docker-compose -f docker-compose-elk.yml up -d
```

| 组件 | 端口 | 说明 |
|------|------|------|
| Elasticsearch | 9200 | 日志存储和搜索 |
| Logstash | 5044 | 日志处理管道 |
| Kibana | 5601 | 日志可视化界面 |
| Filebeat | - | 日志文件采集器 |

---

## CI/CD

### GitHub Actions

自动触发条件：
- Push 到 `main` 或 `develop` 分支
- Pull Request 到 `main` 分支

流水线步骤：
1. 设置 JDK 21
2. Maven 编译
3. 运行测试
4. 打包
5. 构建 Docker 镜像（仅 main 分支）

### Jenkins

```bash
# 手动触发 Jenkins 流水线
# Jenkinsfile 定义了完整的 CI/CD 流程：
# 1. 代码检出
# 2. 后端 Maven 构建
# 3. 前端 npm 构建
# 4. Docker 镜像构建和推送
# 5. Kubernetes 滚动更新
# 6. 健康检查验证
```

---

## 项目特色

### 赛博朋克 UI 风格

前台页面采用赛博朋克/科幻风格设计：
- 深色主题背景
- 霓虹色彩渐变
- 发光边框效果
- 粒子动画装饰
- 玻璃质感卡片

### 数据大屏

管理后台提供数据可视化大屏：
- 顶部统计卡片（商品数、订单数、收入等）
- 订单状态分布圆环图
- 热销图书 TOP10 柱状图
- 低库存预警面板
- 最新操作日志
- 实时时钟

### 操作日志系统

通过 `LogInterceptor` 拦截器自动记录管理员操作：
- 请求路径解析为操作类型（新增/编辑/删除/查询）
- 记录操作人、操作对象、详情、IP 地址
- 支持按类型、关键词筛选

---

## 开发说明

### 代码规范

- 使用 Checkstyle 统一代码风格（`checkstyle.xml`）
- 所有源代码文件已添加详细中文注释（面向初学者）
- Java 代码：每行行尾注释 + 类/方法 Javadoc
- JSP 页面：HTML/CSS/JS 分区注释
- 配置文件：每项配置都有中文说明

### 模块依赖关系

```
bookstore-common（基础层）
    ↑
    ├── bookstore-user
    ├── bookstore-product
    ├── bookstore-order（依赖 product 的 Feign 接口）
    ├── bookstore-promotion
    ├── bookstore-message
    ├── bookstore-admin
    └── bookstore-gateway（独立，不依赖 common）
```

### 新增模块步骤

1. 在根 `pom.xml` 的 `<modules>` 中添加新模块
2. 创建模块目录和 `pom.xml`（继承父工程）
3. 依赖 `bookstore-common`
4. 创建 `application.yml`（配置端口、数据库、Nacos）
5. 创建启动类（`@SpringBootApplication`）
6. 在 Gateway 中添加路由规则

---

## 常见问题

### Q: 服务启动后访问页面显示 404？

A: 确认是否通过正确的端口访问。前台页面通过 `http://localhost:8086/` 访问，不是 8080（网关）。

### Q: 图片不显示？

A: 检查图片路径是否为 `/img/books/xxx.jpg`，确认 Gateway 已配置静态资源路由（`/img/**` → bookstore-admin）。

### Q: 购物车页面报 500 错误？

A: 检查 `cart.jsp` 是否导入了 `fn` 标签库：`<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>`。

### Q: 提交订单失败？

A: 确认 bookstore-order 和 bookstore-product 服务都在运行，订单服务需要通过 Feign 调用商品服务扣减库存。

### Q: 数据大屏数据不准确？

A: 数据大屏通过分页查询所有订单进行汇总，确保 `pageSize` 参数足够大（默认 10000）。

---

## 许可证

本项目仅供学习交流使用。

---

## 致谢

- Spring Boot / Spring Cloud 官方文档
- MyBatis-Plus 官方文档
- Nacos 官方文档
- Elasticsearch 官方文档
- Bootstrap / Font Awesome / jQuery
