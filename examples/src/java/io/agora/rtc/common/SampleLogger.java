package io.agora.rtc.common;

public class SampleLogger {
    private static boolean enableLog = true;

    public static void enableLog(boolean enable) {
        enableLog = enable;
    }

    public static void log(String msg) {
        if (enableLog) {
            System.out.println(Utils.getCurrentTime() + " " + msg);
        }
    }

}
