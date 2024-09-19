package io.agora.rtc.test;

import io.agora.rtc.common.SampleLogger;
import io.agora.rtc.common.Utils;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Map;

import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public abstract class AgoraTest {
    protected static String APPID;
    protected static String TOKEN;

    // ctrl signal
    public static boolean exitFlag = false;
    protected static AgoraService service;
    protected AgoraRtcConn conn;

    public AgoraTest() {
        String[] keys = Utils.readAppIdAndToken(".keys");
        APPID = keys[0];
        TOKEN = keys[1];
        SampleLogger.log("read APPID: " + APPID + " TOKEN: " + TOKEN + " from .keys");
    }

    class SignalFunc implements SignalHandler {
        public void handle(Signal arg0) {
            System.out.println("catch signal " + arg0);
            exitFlag = true;
        }
    }

    public abstract void setup();

    public abstract void cleanup();

    public void sdkTest() {
        SignalFunc handler = new SignalFunc();
        Signal.handle(new Signal("ABRT"), handler);
        Signal.handle(new Signal("INT"), handler);
        setup();
        while (!exitFlag) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cleanup();
        System.exit(0);
    }
}