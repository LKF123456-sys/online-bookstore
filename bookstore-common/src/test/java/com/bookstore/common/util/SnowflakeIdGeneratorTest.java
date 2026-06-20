package com.bookstore.common.util;

import org.junit.jupiter.api.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

class SnowflakeIdGeneratorTest {
    private SnowflakeIdGenerator generator = new SnowflakeIdGenerator();

    @Test void nextId_Unique() {
        assertNotEquals(generator.nextId(), generator.nextId());
    }
    @Test void nextId_10000Calls_AllUnique() {
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        for (int i = 0; i < 10000; i++) ids.add(generator.nextId());
        assertEquals(10000, ids.size());
    }
    @Test void nextId_Concurrent_AllUnique() throws Exception {
        int threads = 10, calls = 1000;
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        var executor = Executors.newFixedThreadPool(threads);
        var latch = new CountDownLatch(threads);
        for (int t = 0; t < threads; t++) executor.submit(() -> {
            for (int i = 0; i < calls; i++) ids.add(generator.nextId());
            latch.countDown();
        });
        latch.await(); executor.shutdown();
        assertEquals(threads * calls, ids.size());
    }
    @Test void nextOrderId_Prefix() {
        assertTrue(generator.nextOrderId("ORD").startsWith("ORD"));
    }
    @Test void nextId_MonotonicIncreasing() {
        long prev = generator.nextId();
        for (int i = 0; i < 100; i++) { long cur = generator.nextId(); assertTrue(cur > prev); prev = cur; }
    }
}
