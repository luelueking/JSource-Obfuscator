package org.vidar.utils;

import java.util.concurrent.atomic.AtomicLong;

public class RandomStrUtil {
    private static final long EPOCH = 1631395200000L; // 设置起始时间戳（2021-09-12 00:00:00）
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS;
    private static final AtomicLong sequence = new AtomicLong(0L);
    private static long lastTimestamp = -1L;


    public static String genRandomClzName() {
        return "Clz" + nextIdAsString();
    }

    public static String genRandomMethodName() {
        return "M" + nextIdAsString();
    }


    private static String nextIdAsString() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException("Clock moved backwards. Refusing to generate ID.");
        }

        if (timestamp == lastTimestamp) {
            long sequenceValue = sequence.getAndIncrement();
            if (sequenceValue > MAX_SEQUENCE) {
                // 当前毫秒内的序列号已经达到最大值，等待下一毫秒
                while (timestamp <= lastTimestamp) {
                    timestamp = System.currentTimeMillis();
                }
                sequence.set(0L);
            }
        } else {
            sequence.set(0L);
        }

        lastTimestamp = timestamp;

        long id = ((timestamp - EPOCH) << TIMESTAMP_SHIFT) | sequence.get();

        return String.valueOf(id);
    }
}
