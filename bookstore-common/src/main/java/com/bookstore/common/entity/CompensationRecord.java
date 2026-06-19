package com.bookstore.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分布式事务补偿记录实体
 * 对应数据库表：compensation_record
 *
 * 设计目的：
 *   在微服务架构中，Feign 远程调用（如扣减库存）不受本地 @Transactional 管理。
 *   当本地事务回滚但远程调用已成功时，需要记录补偿信息并异步重试，
 *   确保数据的最终一致性（Saga 模式）。
 *
 * 补偿流程：
 *   1. 创建订单时，Feign 扣减库存成功但订单入库失败 → 记录 COMPENSATION 类型记录
 *   2. 取消订单时，Feign 恢复库存失败 → 记录 RESTORE 类型记录
 *   3. 定时任务扫描 PENDING 状态的记录，执行重试
 *   4. 重试成功标记 SUCCESS，超过最大重试次数标记 FAILED 等待人工介入
 */
@Data
@TableName("compensation_record")
public class CompensationRecord {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的订单ID */
    private String orderId;

    /** 关联的商品ID（库存操作的目标商品） */
    private String productId;

    /** 操作数量（正数） */
    private Integer quantity;

    /**
     * 补偿类型：
     * - COMPENSATION：创建订单失败后的库存回补（反向扣减）
     * - RESTORE：取消订单后的库存恢复
     */
    private String type;

    /**
     * 补偿状态：
     * - PENDING：待处理（新创建时的初始状态）
     * - SUCCESS：补偿成功
     * - FAILED：补偿失败（超过最大重试次数，需人工介入）
     */
    private String status;

    /** 已重试次数 */
    private Integer retryCount;

    /** 最大重试次数（默认 5 次） */
    private Integer maxRetries;

    /** 最近一次失败原因 */
    private String errorMessage;

    /** 记录创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 记录更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
