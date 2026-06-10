package com.bookstore.common.util;

import org.springframework.stereotype.Component;

/**
 * 分布式 ID 生成器（Snowflake 算法）
 *
 * 生成全局唯一的 Long 型 ID，适用于订单号、消息ID等场景。
 * 结构：1bit(符号位) + 41bit(时间戳) + 10bit(机器ID) + 12bit(序列号)
 * 每秒可生成约 409.6 万个不重复ID。
 */
@Component
public class SnowflakeIdGenerator {

    private static final long EPOCH = 1704067200000L; // 2024-01-01 00:00:00 UTC
    private static final long WORKER_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator() {
        this.workerId = getWorkerIdFromHost();
    }

    private static long getWorkerIdFromHost() {
        try {
            long hash = java.net.InetAddress.getLocalHost().getHostName().hashCode();
            return (hash & 0x7FFFFFFF) % (MAX_WORKER_ID + 1);
        } catch (Exception e) {
            return 1L;
        }
    }

    /**
     * 生成下一个唯一 ID
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id.");
        }
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 生成字符串形式的订单号：前缀 + Snowflake ID
     * @param prefix 前缀，如 "ORD"
     * @return 例如 "ORD178123456789012345"
     */
    public String nextOrderId(String prefix) {
        return prefix + nextId();
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
