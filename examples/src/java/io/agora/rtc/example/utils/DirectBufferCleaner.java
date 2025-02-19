package io.agora.rtc.example.utils;

import io.agora.rtc.example.common.SampleLogger;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class DirectBufferCleaner {
    private static final int JAVA_VERSION = getJavaVersion();

    private static final Method cleanerMethod;
    private static final Method cleanMethod;
    private static final Field unsafeField;

    static {
        Method cleaner = null;
        Method clean = null;
        Field unsafe = null;

        try {
            if (JAVA_VERSION < 9) {
                cleaner = ByteBuffer.class.getDeclaredMethod("cleaner");
                cleaner.setAccessible(true);
                Class<?> cleanerClass = Class.forName("sun.misc.Cleaner");
                clean = cleanerClass.getDeclaredMethod("clean");
                clean.setAccessible(true);
            } else {
                Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                unsafe = unsafeClass.getDeclaredField("theUnsafe");
                unsafe.setAccessible(true);
                cleaner = ByteBuffer.class.getDeclaredMethod("cleaner");
                cleaner.setAccessible(true);
            }
        } catch (Exception e) {
            SampleLogger.info("Failed to initialize DirectBufferCleaner: " + e.getMessage());
        }

        cleanerMethod = cleaner;
        cleanMethod = clean;
        unsafeField = unsafe;
    }

    private static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }

    public static boolean release(ByteBuffer buffer) {
        if (buffer == null || !buffer.isDirect()) {
            return false;
        }

        try {
            if (JAVA_VERSION < 9) {
                return releaseJava8(buffer);
            } else {
                return releaseJava9(buffer);
            }
        } catch (Exception e) {
            SampleLogger.info("Failed to release direct buffer: " + e.getMessage());
            return false;
        }
    }

    private static boolean releaseJava8(ByteBuffer buffer) {
        if (cleanerMethod == null || cleanMethod == null) {
            return false;
        }

        try {
            Object cleaner = cleanerMethod.invoke(buffer);
            if (cleaner != null) {
                cleanMethod.invoke(cleaner);
                return true;
            }
        } catch (Exception e) {
            SampleLogger.info("Failed to clean buffer in Java 8: " + e.getMessage());
        }
        return false;
    }

    private static boolean releaseJava9(ByteBuffer buffer) {
        if (cleanerMethod == null || unsafeField == null) {
            return false;
        }

        try {
            Object cleaner = cleanerMethod.invoke(buffer);
            if (cleaner != null) {
                Object unsafe = unsafeField.get(null);
                Method clean = cleaner.getClass().getDeclaredMethod("clean");
                clean.setAccessible(true);
                clean.invoke(cleaner);
                return true;
            }
        } catch (Exception e) {
            SampleLogger.info("Failed to clean buffer in Java 9: " + e.getMessage());
        }
        return false;
    }
}
