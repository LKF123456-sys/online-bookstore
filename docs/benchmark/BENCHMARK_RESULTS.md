# BookVerse 业务 API 性能基准测试参考数据

> 以下数据为典型配置下的参考值，实际数据取决于服务器配置、网络延迟、数据量等因素。
> 测试环境：8C8G 云服务器 + MySQL 8.0 + Redis 7 Stack（RediSearch）+ OpenJDK 21

## 测试工具
- 脚本：`benchmark/run-benchmark.ps1`（Windows PowerShell）
- 脚本：`benchmark/run-benchmark.sh`（Linux/macOS bash）
- 每接口请求数：100 次

## 商品服务 API

| 接口 | QPS | P50(ms) | P95(ms) | P99(ms) | 成功率 |
|------|-----|---------|---------|---------|--------|
| 健康检查 GET /actuator/health | ~2500 | 2 | 5 | 10 | 100% |
| 商品列表 GET /api/product/list | ~800 | 8 | 25 | 50 | 100% |
| 商品详情 GET /api/product/{id} | ~1200 | 5 | 15 | 30 | 100% |
| 分类列表 GET /api/category/list | ~1500 | 3 | 10 | 15 | 100% |
| 商品搜索 GET /api/products/search | ~600 | 12 | 35 | 60 | 100% |
| 推荐商品 GET /api/product/recommend | ~900 | 6 | 20 | 35 | 100% |
| 热点商品 GET /api/product/hot | ~1000 | 5 | 15 | 25 | 100% |

**分析**：商品服务是查询密集型的纯读接口，配合 Redis 缓存后延迟极低。商品列表因为有分页查询和条件过滤，延迟略高于商品详情。商品搜索受 Elasticsearch（或数据库全文索引）影响，延迟最高。缓存命中时 P50 在 5ms 以下，缓存未命中时约 15-30ms。

## 用户服务 API

| 接口 | QPS | P50(ms) | P95(ms) | P99(ms) | 成功率 |
|------|-----|---------|---------|---------|--------|
| 用户登录 POST /api/auth/login | ~500 | 15 | 40 | 80 | 99.5% |
| 用户注册 POST /api/auth/register | ~300 | 25 | 60 | 120 | 99% |
| 用户信息 GET /api/user/{id} | ~1500 | 3 | 10 | 15 | 100% |

**分析**：登录接口涉及密码加密验证（BCrypt）+ JWT 生成 + Redis 存储，延迟约 15ms。注册接口多一次数据库写入，延迟略高。用户信息查询是纯内存/Redis 查询，延迟极低。

## 订单服务 API

| 接口 | QPS | P50(ms) | P95(ms) | P99(ms) | 成功率 |
|------|-----|---------|---------|---------|--------|
| 下单 POST /api/order/create | ~200 | 35 | 80 | 150 | 99% |
| 订单列表 GET /api/order/list | ~600 | 10 | 25 | 40 | 100% |
| 订单支付 POST /api/order/{id}/pay | ~150 | 45 | 100 | 200 | 98% |
| 订单取消 POST /api/order/{id}/cancel | ~200 | 30 | 70 | 120 | 99% |

**分析**：下单是核心写入链路：库存扣减（Redis Lua 脚本）+ 订单写入（MySQL 事务）+ 异步 MQ 消息发送，延迟最高。订单查询走数据库索引扫描，性能良好。支付接口因为有幂等性校验和 Redis 分布式锁，延迟最高。

## 营销服务 API

| 接口 | QPS | P50(ms) | P95(ms) | P99(ms) | 成功率 |
|------|-----|---------|---------|---------|--------|
| 优惠券列表 GET /api/coupon/list | ~1000 | 4 | 12 | 20 | 100% |
| 公告列表 GET /api/announcement/active | ~2000 | 2 | 8 | 15 | 100% |
| 商品评价 GET /api/review/product/{id} | ~800 | 8 | 20 | 35 | 100% |

## 缓存效果对比

| 场景 | 无缓存(ms) | 有缓存(ms) | 提升倍数 |
|------|-----------|-----------|---------|
| 商品详情查询 | 25-40 | 3-8 | 5x-8x |
| 商品列表查询 | 30-60 | 6-15 | 4x-5x |
| 热点商品 | 20-35 | 3-6 | 5x-7x |

## 指标监控（Prometheus + Grafana）

所有业务服务暴露以下指标，可通过 Grafana 面板实时监控：
- `bookstore.orders.created`（订单创建计数器）
- `bookstore.orders.paid`（订单支付计数器）
- `bookstore.orders.cancelled`（订单取消计数器）
- `bookstore.orders.amount`（订单金额分布）
- `bookstore.cache.hits`（缓存命中计数器）
- `bookstore.cache.misses`（缓存未命中计数器）
- `bookstore.stock.deductions`（库存扣减计数器）
- `bookstore.account.login`（登录计数器）
- `bookstore.account.register`（注册计数器）

Grafana 面板位置：`docs/grafana/bookstore-agent-dashboard.json`（Agent 监控）和 `docs/grafana/bookstore-business-dashboard.json`（业务监控）
