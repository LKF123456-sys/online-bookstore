package com.bookstore.order.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bookstore.common.entity.CompensationRecord;
import com.bookstore.order.feign.ProductFeignClient;
import com.bookstore.order.mapper.CompensationRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 补偿记录定时恢复任务
 *
 * 设计模式：Saga 模式中的补偿事务
 * 定期扫描 PENDING 状态的补偿记录，执行重试操作。
 * 采用指数退避策略避免对下游服务造成过大压力。
 *
 * 执行策略：
 *   - 每 30 秒执行一次扫描
 *   - 每次最多处理 50 条记录
 *   - 根据重试次数计算退避间隔（retryCount^2 秒）
 *   - 超过最大重试次数标记为 FAILED
 *
 * 面试亮点：
 *   1. Saga 分布式事务补偿模式
 *   2. 指数退避重试策略
 *   3. 定时任务 + 数据库持久化（比纯内存重试更可靠）
 *   4. 最终一致性保证
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompensationRecoveryTask {

    private final CompensationRecordMapper compensationRecordMapper;
    private final ProductFeignClient productFeignClient;

    /**
     * 定时扫描并处理待补偿记录
     * fixedDelay = 30000ms（30秒）：上一次执行完成后等待30秒再执行下一次
     * 使用 fixedDelay 而非 fixedRate，避免任务堆积
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 60000)
    public void processCompensationRecords() {
        // 查询 PENDING 状态的补偿记录，按创建时间升序（先处理旧的）
        List<CompensationRecord> pendingRecords = compensationRecordMapper.selectList(
                new LambdaQueryWrapper<CompensationRecord>()
                        .eq(CompensationRecord::getStatus, "PENDING")
                        .orderByAsc(CompensationRecord::getCreateTime)
                        .last("LIMIT 50"));

        if (pendingRecords.isEmpty()) {
            return;
        }

        log.info("补偿任务扫描到 {} 条待处理记录", pendingRecords.size());

        for (CompensationRecord record : pendingRecords) {
            // 指数退避：根据重试次数判断是否到执行时间
            // 第 1 次重试：等 1 秒，第 2 次：等 4 秒，第 3 次：等 9 秒...
            long backoffSeconds = (long) Math.pow(record.getRetryCount(), 2);
            if (record.getUpdateTime() != null) {
                LocalDateTime nextRetryTime = record.getUpdateTime().plusSeconds(backoffSeconds);
                if (nextRetryTime.isAfter(LocalDateTime.now())) {
                    continue;  // 还未到重试时间，跳过
                }
            }

            try {
                // 根据补偿类型执行对应操作
                if ("COMPENSATION".equals(record.getType())) {
                    // 创建订单失败后的库存回补：扣减库存（反向操作）
                    productFeignClient.updateStock(record.getProductId(), -record.getQuantity());
                } else if ("RESTORE".equals(record.getType())) {
                    // 取消订单后的库存恢复：增加库存
                    productFeignClient.updateStock(record.getProductId(), -record.getQuantity());
                }

                // 补偿成功，更新状态
                compensationRecordMapper.update(null,
                        new LambdaUpdateWrapper<CompensationRecord>()
                                .eq(CompensationRecord::getId, record.getId())
                                .set(CompensationRecord::getStatus, "SUCCESS")
                                .set(CompensationRecord::getUpdateTime, LocalDateTime.now()));

                log.info("补偿记录处理成功: id={}, orderId={}, productId={}, type={}",
                        record.getId(), record.getOrderId(), record.getProductId(), record.getType());

            } catch (Exception e) {
                // 补偿失败，更新重试次数和错误信息
                int newRetryCount = record.getRetryCount() + 1;
                String newStatus = newRetryCount >= record.getMaxRetries() ? "FAILED" : "PENDING";

                compensationRecordMapper.update(null,
                        new LambdaUpdateWrapper<CompensationRecord>()
                                .eq(CompensationRecord::getId, record.getId())
                                .set(CompensationRecord::getRetryCount, newRetryCount)
                                .set(CompensationRecord::getStatus, newStatus)
                                .set(CompensationRecord::getErrorMessage,
                                        e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 500)) : "unknown")
                                .set(CompensationRecord::getUpdateTime, LocalDateTime.now()));

                if ("FAILED".equals(newStatus)) {
                    log.error("【严重】补偿记录超过最大重试次数，需人工介入: id={}, orderId={}, productId={}, " +
                                    "retryCount={}, error={}",
                            record.getId(), record.getOrderId(), record.getProductId(),
                            newRetryCount, e.getMessage());
                } else {
                    log.warn("补偿记录重试失败 (第{}次): id={}, productId={}, error={}",
                            newRetryCount, record.getId(), record.getProductId(), e.getMessage());
                }
            }
        }
    }
}
