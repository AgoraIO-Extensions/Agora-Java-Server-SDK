package io.agora.rtc.test;

import io.agora.rtc.SDK;
import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.Constants;
import io.agora.rtc.DefaultLocalUserObserver;
import io.agora.rtc.DefaultRtcConnObserver;
import io.agora.rtc.Out;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.common.SampleLogger;
import io.agora.rtc.common.Utils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SendReceiverStreamMessageTest {
    private static String appId;
    private static String token;
    private final static String DEFAULT_LOG_PATH = "logs/agora_logs/agorasdk.log";
    private final static int DEFAULT_LOG_SIZE = 5 * 1024 * 1024; // default log size is 5 mb

    private static AgoraService service;

    private static String channelId = "agaa";
    private static String userId = "0";
    private static long testTime = 60 * 1000;

    private static CountDownLatch connectedLatch = null;

    private static final ExecutorService testTaskExecutorService = Executors.newCachedThreadPool();

    private static AtomicInteger testTaskCount = new AtomicInteger(0);

    private static void parseArgs(String[] args) {
        SampleLogger.log("parseArgs args:" + Arrays.toString(args));
        if (args == null || args.length == 0) {
            return;
        }

        Map<String, String> parsedArgs = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 < args.length) {
                parsedArgs.put(args[i], args[i + 1]);
            } else {
                SampleLogger.log("Missing value for argument: " + args[i]);
            }
        }

        if (parsedArgs.containsKey("-channelId")) {
            channelId = parsedArgs.get("-channelId");
        }

        if (parsedArgs.containsKey("-userId")) {
            userId = parsedArgs.get("-userId");
        }

        if (parsedArgs.containsKey("-testTime")) {
            testTime = Long.parseLong(parsedArgs.get("-testTime"));
        }
    }

    public static void main(String[] args) {
        parseArgs(args);
        String[] keys = Utils.readAppIdAndToken(".keys");
        appId = keys[0];
        token = keys[1];
        SampleLogger.log("read appId: " + appId + " token: " + token + " from .keys");

        // Initialize Agora service globally once
        SDK.load();
        service = new AgoraService();
        AgoraServiceConfig config = new AgoraServiceConfig();
        config.setAppId(appId);
        config.setEnableAudioDevice(0);
        config.setEnableAudioProcessor(1);
        config.setEnableVideo(1);
        config.setUseStringUid(0);
        config.setAudioScenario(Constants.AUDIO_SCENARIO_CHORUS);

        int ret = service.initialize(config);
        if (ret != 0) {
            SampleLogger.log("createAndInitAgoraService AgoraService.initialize fail ret:" + ret);
            return;
        }

        ret = service.setLogFile(DEFAULT_LOG_PATH, DEFAULT_LOG_SIZE);
        service.setLogFilter(Constants.LOG_FILTER_DEBUG);
        if (ret != 0) {
            SampleLogger.log("createAndInitAgoraService AgoraService.setLogFile fail ret:" + ret);
            releaseAgoraService();
            return;
        }

        testTaskExecutorService.execute(() -> {
            testTaskCount.incrementAndGet();
            startSendStreamMessage();
        });

        testTaskExecutorService.execute(() -> {
            testTaskCount.incrementAndGet();
            startReceiveStreamMessage();
        });

        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (testTaskCount.get() != 0);

        releaseAgoraService();
        System.exit(0);
    }

    private static void startSendStreamMessage() {
        SampleLogger.log("startSendStreamMessage");
        // Create a connection for each channel
        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        AgoraRtcConn conn = service.agoraRtcConnCreate(ccfg);
        if (conn == null) {
            SampleLogger.log("AgoraService.agoraRtcConnCreate fail\n");
            return;
        }

        int ret = conn.registerObserver(new DefaultRtcConnObserver() {
            @Override
            public void onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason) {
                super.onConnected(agoraRtcConn, connInfo, reason);
                SampleLogger.log("onConnected connInfo :" + connInfo + " reason:" + reason);
                if (connectedLatch != null) {
                    connectedLatch.countDown();
                }
            }

            @Override
            public void onUserJoined(AgoraRtcConn agoraRtcConn, String userId) {
                super.onUserJoined(agoraRtcConn, userId);
                SampleLogger.log("onUserJoined userId:" + userId);

            }

            @Override
            public void onUserLeft(AgoraRtcConn agoraRtcConn, String userId, int reason) {
                super.onUserLeft(agoraRtcConn, userId, reason);
                SampleLogger.log("onUserLeft userId:" + userId + " reason:" + reason);

            }
        });
        SampleLogger.log("registerObserver ret:" + ret);

        ret = conn.connect(token, channelId, userId);
        SampleLogger.log("Connecting to Agora channel " + channelId + " with userId " + userId + " ret:" + ret);
        if (ret != 0) {
            SampleLogger.log("conn.connect fail ret=" + ret);
            releaseConn(conn);
            return;
        }

        if (connectedLatch == null) {
            connectedLatch = new CountDownLatch(1);
        }

        try {
            connectedLatch.await();
        } catch (InterruptedException e) {
            SampleLogger.log("startSendStreamMessage connectedLatch.await() interrupted");
        }

        AtomicBoolean sendStreamMessageDone = new AtomicBoolean(false);

        testTaskExecutorService.execute(() -> {
            Out<Integer> streamId = new Out<>();
            int result = conn.createDataStream(streamId, 0, 0);
            SampleLogger
                    .log("sendDataStream create DataStream result " + result + " stream id " + streamId.get());

            while (!sendStreamMessageDone.get()) {
                String data = Utils.getCurrentTime() + " hello world from channelId:"
                        + channelId + " userId:" + userId;
                int sendRet = conn.sendStreamMessage(streamId.get(), data, data.length());
                SampleLogger.log("sendStreamMessage: " + data + " done ret:" + sendRet);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < testTime) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        sendStreamMessageDone.set(true);

        releaseConn(conn);
        testTaskCount.decrementAndGet();
    }

    private static void startReceiveStreamMessage() {
        SampleLogger.log("startReceiveStreamMessage");

        // Create a connection for each channel
        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        AgoraRtcConn conn = service.agoraRtcConnCreate(ccfg);
        if (conn == null) {
            SampleLogger.log("AgoraService.agoraRtcConnCreate fail\n");
            return;
        }

        int ret = conn.registerObserver(new DefaultRtcConnObserver() {
            @Override
            public void onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason) {
                super.onConnected(agoraRtcConn, connInfo, reason);
                SampleLogger.log("onConnected connInfo :" + connInfo + " reason:" + reason);

            }

            @Override
            public void onUserJoined(AgoraRtcConn agoraRtcConn, String userId) {
                super.onUserJoined(agoraRtcConn, userId);
                SampleLogger.log("onUserJoined userId:" + userId);

            }

            @Override
            public void onUserLeft(AgoraRtcConn agoraRtcConn, String userId, int reason) {
                super.onUserLeft(agoraRtcConn, userId, reason);
                SampleLogger.log("onUserLeft userId:" + userId + " reason:" + reason);

            }
        });
        SampleLogger.log("registerObserver ret:" + ret);

        conn.getLocalUser().registerObserver(new DefaultLocalUserObserver() {
            @Override
            public void onStreamMessage(AgoraLocalUser agoraLocalUser, String userId, int streamId, String data,
                    long length) {
                SampleLogger.log("onStreamMessage userId:" + userId + " streamId:" + streamId + " data:" + data
                        + " length:" + length);
            }
        });

        ret = conn.connect(token, channelId, userId);
        SampleLogger.log("Connecting to Agora channel " + channelId + " with userId " + userId + " ret:" + ret);
        if (ret != 0) {
            SampleLogger.log("conn.connect fail ret=" + ret);
            releaseConn(conn);
        }

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < testTime) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        releaseConn(conn);
        testTaskCount.decrementAndGet();
    }

    private static void releaseConn(AgoraRtcConn conn) {
        if (conn == null) {
            return;
        }

        SampleLogger.log("releaseConn for channelId:" + conn.getConnInfo().getChannelId() + " userId:"
                + conn.getConnInfo().getLocalUserId());

        int ret = conn.disconnect();
        if (ret != 0) {
            SampleLogger.log("conn.disconnect fail ret=" + ret);
        }

        // Unregister connection observer
        conn.unregisterObserver();
        conn.getLocalUser().unregisterObserver();

        conn.destroy();

        conn = null;

        SampleLogger.log("Disconnected from Agora channel successfully");
    }

    private static void releaseAgoraService() {
        if (service != null) {
            service.destroy();
            service = null;
        }
        SampleLogger.log("releaseAgoraService");
    }

}