package io.agora.rtc.example.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * High-performance logging utility class based on Log4j2
 * Provides asynchronous logging and rate limiting functionality
 */
public class SampleLogger {
    private static boolean enableLog = true;

    // Use Log4j2 Logger instead of Java native Logger
    private static final Logger logger = LogManager.getLogger("agora");

    // Log counters by category
    private static final Map<String, LogCounter> logCounters = new ConcurrentHashMap<>();

    // Default log sampling rate and interval
    private static final int DEFAULT_LOG_SAMPLE_RATE = 100;
    private static final long DEFAULT_LOG_INTERVAL_MS = 1000;

    private final static ExecutorService singleExecutorService = Executors.newSingleThreadExecutor();

    // Log counter class
    private static class LogCounter {
        private final AtomicInteger counter = new AtomicInteger(0);
        private volatile long lastLogTime = 0;
        private final int sampleRate;
        private final long intervalMs;

        public LogCounter(int sampleRate, long intervalMs) {
            this.sampleRate = sampleRate;
            this.intervalMs = intervalMs;
        }

        public boolean shouldLog() {
            int count = counter.incrementAndGet();
            long currentTime = System.currentTimeMillis();

            if (count % sampleRate == 0 && (currentTime - lastLogTime >= intervalMs)) {
                lastLogTime = currentTime;
                return true;
            }
            return false;
        }
    }

    /**
     * Enable or disable logging
     */
    public static void enableLog(boolean enable) {
        enableLog = enable;
    }

    public static void release() {
        singleExecutorService.shutdown();
    }

    /**
     * Normal log output
     */
    public static void log(String message) {
        if (enableLog) {
            singleExecutorService.execute(() -> {
                logger.info(message);
            });
        }
    }

    /**
     * Info level logging
     */
    public static void info(String message) {
        if (enableLog) {
            singleExecutorService.execute(() -> {
                logger.info(message);
            });
        }
    }

    /**
     * Error level logging
     */
    public static void error(String message) {
        if (enableLog) {
            singleExecutorService.execute(() -> {
                logger.error(message);
            });
        }
    }

    /**
     * Warning level logging
     */
    public static void warn(String message) {
        if (enableLog) {
            singleExecutorService.execute(() -> {
                logger.warn(message);
            });
        }
    }

    /**
     * Debug level logging
     */
    public static void debug(String message) {
        if (enableLog) {
            singleExecutorService.execute(() -> {
                logger.debug(message);
            });
        }
    }

    /**
     * Rate-limited log output
     * 
     * @param category Log category to distinguish different types of logs
     * @param message  Log message
     * @return Whether the log was actually output
     */
    public static boolean logWithRateLimit(String category, String message) {
        return logWithRateLimit(category, message, DEFAULT_LOG_SAMPLE_RATE, DEFAULT_LOG_INTERVAL_MS);
    }

    /**
     * Rate-limited log output using thread pool for async processing
     * 
     * @param category Log category
     * @param message  Log message
     * @param executor Thread pool for executing log output
     * @return Whether the log was actually output
     */
    public static boolean logWithRateLimit(String category, String message, ThreadPoolExecutor executor) {
        return logWithRateLimit(category, message, executor, DEFAULT_LOG_SAMPLE_RATE, DEFAULT_LOG_INTERVAL_MS);
    }

    /**
     * Rate-limited log output with custom rate limiting parameters
     * 
     * @param category   Log category
     * @param message    Log message
     * @param sampleRate Sampling rate
     * @param intervalMs Time interval in milliseconds
     * @return Whether the log was actually output
     */
    public static boolean logWithRateLimit(String category, String message, int sampleRate, long intervalMs) {
        if (!enableLog) {
            return false;
        }

        LogCounter counter = logCounters.computeIfAbsent(category,
                k -> new LogCounter(sampleRate, intervalMs));

        if (counter.shouldLog()) {
            // Add context information
            ThreadContext.put("category", category);
            logger.info(message);
            ThreadContext.remove("category");
            return true;
        }
        return false;
    }

    /**
     * Rate-limited log output with custom parameters using thread pool for async
     * processing
     * 
     * @param category   Log category
     * @param message    Log message
     * @param executor   Thread pool for executing log output
     * @param sampleRate Sampling rate
     * @param intervalMs Time interval in milliseconds
     * @return Whether the log was actually output
     */
    public static boolean logWithRateLimit(String category, String message, ThreadPoolExecutor executor,
            int sampleRate, long intervalMs) {
        if (!enableLog) {
            return false;
        }

        LogCounter counter = logCounters.computeIfAbsent(category,
                k -> new LogCounter(sampleRate, intervalMs));

        if (counter.shouldLog()) {
            if (executor != null && executor.getQueue().size() < executor.getQueue().remainingCapacity() * 0.9) {
                final String finalCategory = category;
                executor.execute(() -> {
                    // Add context information
                    ThreadContext.put("category", finalCategory);
                    logger.info(message);
                    ThreadContext.remove("category");
                });
                return true;
            } else {
                // If thread pool queue is nearly full, output directly in current thread
                ThreadContext.put("category", category);
                logger.warn("[THREAD_POOL_FULL] " + message);
                ThreadContext.remove("category");
                return true;
            }
        }
        return false;
    }
}