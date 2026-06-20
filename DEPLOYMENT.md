# BookVerse 部署指南

> 适用版本：Spring Boot 3.2.5 + Spring Cloud 2023.0.1 + Spring AI 1.0.9

## 前置条件

| 组件 | 版本要求 | 用途 |
|------|---------|------|
| JDK | 21+ | 编译和运行 |
| Maven | 3.9+ | 构建 |
| Docker & Docker Compose | 最新 | 一键启动全部服务 |
| Kubernetes | 1.28+ | 生产部署（可选） |

## 快速启动（Docker Compose）

```bash
# 1. 配置环境变量
cp .env.example .env
# 编辑 .env，填入 AI_API_KEY 等关键配置

# 2. 一键启动全部服务（12 个容器）
docker-compose up -d

# 3. 验证服务状态
# 健康检查（全部 12 个服务通过后）
curl http://localhost:8080/actuator/health

# 4. 访问
# 用户端：http://localhost
# 管理后台：http://localhost:81
# Nacos 控制台：http://localhost:8848/nacos
# Grafana：http://localhost:3000
# SkyWalking UI：http://localhost:8888
```

## 手动启动（开发环境）

每个微服务独立启动，适合开发和调试。

### 1. 启动基础设施

```bash
# 启动 MySQL + Redis + Nacos + RabbitMQ + ES + SkyWalking
docker-compose up -d mysql redis nacos rabbitmq elasticsearch skywalking-oap skywalking-ui
```

### 2. 确认基础设施就绪

```bash
# MySQL
docker exec bookstore-mysql mysqladmin ping -h localhost -u root -p${DB_PASSWORD}

# Redis
docker exec bookstore-redis redis-cli ping  # 应返回 PONG

# Nacos
curl http://localhost:8848/nacos/v1/console/health/readiness  # 应返回 OK
```

### 3. 按顺序启动微服务（在 IntelliJ 中逐个运行）

| 顺序 | 模块 | 端口 | 命令 | 依赖说明 |
|------|------|------|------|---------|
| 1 | bookstore-gateway | 8080 | `mvn spring-boot:run -pl bookstore-gateway` | 无下游依赖 |
| 2 | bookstore-user | 8081 | `mvn spring-boot:run -pl bookstore-user` | MySQL + Redis |
| 3 | bookstore-product | 8082 | `mvn spring-boot:run -pl bookstore-product` | MySQL + Redis + ES |
| 4 | bookstore-order | 8083 | `mvn spring-boot:run -pl bookstore-order` | MySQL + Redis + RabbitMQ |
| 5 | bookstore-promotion | 8085 | `mvn spring-boot:run -pl bookstore-promotion` | MySQL + Redis |
| 6 | bookstore-message | 8087 | `mvn spring-boot:run -pl bookstore-message` | MySQL + RabbitMQ |
| 7 | bookstore-admin | 8086 | `mvn spring-boot:run -pl bookstore-admin` | 依赖所有上游服务 |
| 8 | bookstore-agent | 8089 | `mvn spring-boot:run -pl bookstore-agent` | 依赖所有服务 + AI API |

### 4. 启动前端

```bash
# 用户端
cd bookstore-frontend && npm ci && npm run dev

# 管理后台
cd bookstore-admin-frontend && npm ci && npm run dev
```

## K8s 部署

```bash
# 1. 创建命名空间
kubectl apply -f k8s/namespace.yaml

# 2. 创建 ConfigMap 和 Secret
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml

# 3. 部署基础设施（需提前准备 MySQL、Redis、Nacos 等）
# 建议使用 Helm Chart 或云服务

# 4. 部署微服务
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# 5. 配置 Ingress
kubectl apply -f k8s/ingress.yaml
```

## 多模型 AI 配置

编辑 .env 或直接设置环境变量：

```bash
# 切换模型提供方（openai / ollama / dashscope / deepseek / doubao）
export AI_PROVIDER=openai

# OpenAI（默认）
export OPENAI_API_KEY=sk-xxx
export OPENAI_BASE_URL=https://api.openai.com

# DeepSeek
export AI_PROVIDER=deepseek
export DEEPSEEK_API_KEY=sk-xxx

# 豆包（火山引擎）
export AI_PROVIDER=doubao
export DOUBAO_API_KEY=xxx
export DOUBAO_BASE_URL=https://ark.cn-beijing.volces.com/api/v3
```

## 监控告警

| 系统 | 访问地址 | 说明 |
|------|---------|------|
| Grafana | http://localhost:3000 | 指标可视化面板 |
| Prometheus | http://localhost:9090 | 指标采集 |
| SkyWalking | http://localhost:8888 | 分布式链路追踪 |
| ELK | http://localhost:5601 | 日志中心 |

## 验证清单

部署后执行以下检查确认系统正常：

```bash
# 1. 健康检查
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
# ... 逐个检查所有服务

# 2. 注册中心
curl http://localhost:8848/nacos/v1/ns/service/list

# 3. 核心业务流程
# 注册 → 登录 → 浏览商品 → 添加购物车 → 下单 → 支付
```

## 常见问题

| 问题 | 解决方案 |
|------|---------|
| Nacos 连接失败 | 等待 30s 让 Nacos 完全启动，或检查 nacos:8848 网络连通性 |
| Redis 认证失败 | 检查 .env 中的 REDIS_PASSWORD 是否匹配 docker-compose.yml |
| AI Agent 无响应 | 确认 AI_PROVIDER 对应的 API Key 已正确配置 |
| 前端页面白屏 | 确认后端 Gateway (8080) 已启动，检查 Nginx 配置 |
