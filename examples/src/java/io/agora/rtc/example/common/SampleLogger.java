package io.agora.rtc.example.common;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import io.agora.rtc.example.utils.Utils;

public class SampleLogger {
    private static boolean enableLog = true;

    private static final Logger logger = Logger.getLogger("AgoraServiceSample");

    static {
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new CustomFormatter());
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
    }

    public static void enableLog(boolean enable) {
        enableLog = enable;
    }

    public static void log(String message) {
        if (enableLog) {
            synchronized (logger) {
                logger.info("[" + Utils.getCurrentTime() + "] " + message);
            }
        }
    }

    public static void info(String message) {
        if (enableLog) {
            synchronized (logger) {
                logger.info("[" + Utils.getCurrentTime() + "] " + message);
            }
        }
    }

    private static class CustomFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return record.getLevel().getName() + record.getMessage() + "\n";
        }
    }

}
