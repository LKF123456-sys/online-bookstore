# BookVerse 运维手册（Runbook）

## 服务不可用

### 症状
- Actuator health check 返回 DOWN
- Nacos 中服务实例数为 0
- 前端请求返回 502/503

### 排查步骤
1. **检查进程状态**
   ```bash
   docker ps | grep bookstore-{service_name}
   # 或 k8s
   kubectl get pods -n bookstore
   ```

2. **检查日志**
   ```bash
   docker logs bookstore-{service_name} --tail 200
   ```

3. **检查 JVM 状态**
   ```bash
   # 获取 Heap Dump（需要时）
   jmap -dump:live,format=b,file=/tmp/heap.hprof <pid>
   ```

4. **检查下游依赖**
   - MySQL: `docker exec bookstore-mysql mysqladmin ping`
   - Redis: `docker exec bookstore-redis redis-cli ping`
   - Nacos: `curl http://localhost:8848/nacos/v1/console/health/readiness`

### 恢复
- 普通重启：`docker-compose restart {service_name}`
- 完全重建：`docker-compose up -d --force-recreate {service_name}`

## 高延迟

### 症状
- Grafana 面板显示 P99 > 500ms
- 用户反馈页面加载慢
- API 调用超时

### 排查步骤
1. **检查慢查询**
   ```sql
   -- MySQL 慢查询日志（需开启）
   SHOW FULL PROCESSLIST;
   SELECT * FROM information_schema.processlist WHERE time > 5;
   ```

2. **检查 Redis 缓存命中率**
   ```bash
   # Redis 监控
   redis-cli INFO stats | grep hit
   ```

3. **检查 GC 情况**
   ```bash
   jstat -gcutil <pid> 1000 10
   ```

4. **检查 Feign 调用链路**
   - SkyWalking 中查看调用链
   - Grafana 中查看 Feign 成功率面板

### 恢复
- 缓存预热：重启相关服务，缓存自动加载
- 扩容：`docker-compose scale {service_name}=2`

## 错误率飙升

### 症状
- Grafana 5xx 面板异常上升
- 告警规则触发
- 用户反馈操作失败

### 排查步骤
1. **按错误码分类查看**
   ```bash
   docker logs bookstore-{service_name} --tail 500 | grep ERROR
   ```

2. **检查 BusinessException 频次**
   - 关注 401/403/404/409 等业务错误码

3. **检查 Feign 调用失败率**
   - 下游服务可能宕机或高负载
   - 查看 Circuit Breaker 状态

## 数据库故障

### 症状
- SQL 执行超时
- Connection pool 耗尽
- Deadlock 错误

### 排查步骤
1. **检查连接池**
   ```bash
   curl http://localhost:{port}/actuator/metrics/hikaricp.connections.active
   ```

2. **检查慢查询**
   ```sql
   EXPLAIN SELECT ... ;  -- 分析执行计划
   SHOW INDEX FROM table_name;  -- 检查索引
   ```

3. **查看锁情况**
   ```sql
   SELECT * FROM information_schema.INNODB_LOCKS;
   SELECT * FROM information_schema.INNODB_LOCK_WAITS;
   ```

### 恢复
- 连接池耗尽：重启服务或增大 hikari.maximum-pool-size
- 死锁：MySQL 会自动回滚事务，重试即可

## Redis 故障

### 症状
- Redis 连接超时
- 缓存未命中率飙升
- 令牌黑名单失效

### 排查步骤
1. **检查 Redis 状态**
   ```bash
   redis-cli INFO server
   redis-cli INFO memory
   redis-cli INFO stats
   ```

2. **检查大 Key**
   ```bash
   redis-cli --bigkeys
   ```

3. **检查内存使用**
   ```bash
   redis-cli INFO memory | grep used_memory_human
   ```

### 恢复
- 内存不足：增大 maxmemory 或清理过期 key
- 连接超时：检查网络和 maxclients

## RabbitMQ 积压

### 症状
- MQ 队列深度持续增长
- 订单处理延迟
- 补偿记录大量堆积

### 排查步骤
1. **检查队列**
   ```bash
   # RabbitMQ Management UI: http://localhost:15672
   docker exec bookstore-rabbitmq rabbitmqctl list_queues
   ```

2. **检查消费者**
   ```bash
   docker exec bookstore-rabbitmq rabbitmqctl list_consumers
   ```

### 恢复
- 消费者跟不上：增加消费者并发数
- 消息消费失败：查看死信队列（DLQ）
- 手动清除积压：清除队列或重启消费者
