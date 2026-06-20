# BookVerse SLO（Service Level Objectives）

> 定义各服务的可用性、延迟、错误率目标。基于 30 天滚动窗口计算。

## 核心 API 服务

| 指标 | 目标 | 测量方式 | 告警阈值 |
|------|------|---------|---------|
| 可用性 | >= 99.9% | up{application} |
: critical(0m)|
| P50 延迟 | <= 10ms | http_server_requests_seconds | warning(>50ms) |
| P95 延迟 | <= 50ms | http_server_requests_seconds | warning(>200ms) |
| P99 延迟 | <= 200ms | http_server_requests_seconds | critical(>500ms) |
| 5xx 错误率 | <= 0.1% | http status=5xx / total | critical(>5%) |
| 4xx 错误率 | <= 1% | http status=4xx / total | warning(>5%) |

## AI Agent 服务

| 指标 | 目标 | 测量方式 | 说明 |
|------|------|---------|------|
| 可用性 | >= 99.5% | up{application="bookstore-agent"} | 依赖 LLM API |
| SSE 首 token 延迟 | <= 2s | Time to First Token | 用户体验关键指标 |
| 完整回复延迟 | <= 10s | 同步聊天 P95 | 含 Tool Calling 耗时 |
| Agent 路由成功率 | >= 98% | agent_route_total / errors | 意图分类成功率 |

## 缓存服务

| 指标 | 目标 | 测量方式 |
|------|------|---------|
| 缓存命中率 | >= 90% | cache_hits / (cache_hits + cache_misses) |
| Redis 可用性 | >= 99.9% | redis up 状态 |

## 数据层

| 指标 | 目标 | 测量方式 |
|------|------|---------|
| MySQL 连接池使用率 | <= 80% | hikaricp_active / hikaricp_max |
| MySQL 查询 P99 | <= 100ms | 慢查询日志 |
| RabbitMQ 积压 | <= 1000 | queue depth per queue |

## 业务指标

| 指标 | 目标 | 说明 |
|------|------|------|
| 下单成功率 | >= 99% | orders_created / orders_error_total |
| 支付成功率 | >= 98% | orders_paid / orders_created |
| 订单取消率 | <= 10% | orders_cancelled / orders_created |
| 库存扣减一致性 | 100% | 补偿记录 0 异常 |
