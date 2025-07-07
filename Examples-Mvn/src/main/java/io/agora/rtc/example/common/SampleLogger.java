package io.agora.rtc.example.common;

import lombok.extern.slf4j.Slf4j;

/**
 * High-performance logging utility class based on Log4j2
 * Provides asynchronous logging and rate limiting functionality
 */
@Slf4j
public class SampleLogger {
    private static boolean enableLog = true;

    /**
     * Enable or disable logging
     */
    public static void enableLog(boolean enable) {
        enableLog = enable;
    }

    public static boolean isEnableLog() {
        return enableLog;
    }

    public static void release() {
    }

    /**
     * Normal log output
     */
    public static void log(String message) {
        if (enableLog) {
            log.info(message);
        }
    }

    /**
     * Info level logging
     */
    public static void info(String message) {
        if (enableLog) {
            log.info(message);
        }
    }

    /**
     * Error level logging
     */
    public static void error(String message) {
        if (enableLog) {
            log.error(message);
        }
    }

    /**
     * Warning level logging
     */
    public static void warn(String message) {
        if (enableLog) {
            log.warn(message);
        }
    }

    /**
     * Debug level logging
     */
    public static void debug(String message) {
        if (enableLog) {
            log.debug(message);
        }
    }
}