package io.agora.rtc.example.basic;

import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcConnObserver;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.RtcConnPublishConfig;
import io.agora.rtc.example.common.FileSender;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.utils.Utils;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoopConnSendPcmTest {
    private String appId;
    private String token;
    private final String DEFAULT_LOG_PATH = "logs/agora_logs/agorasdk.log";
    private final int DEFAULT_LOG_SIZE = 5 * 1024; // default log size is 5 mb
    private String channelId = "agaa";
    private String userId = "0";

    private static AgoraService service;

    private AgoraRtcConn conn;

    private String audioFilePath = "test_data/tts_ai_16k_1ch.pcm";
    private int numOfChannels = 1;
    private int sampleRate = 16000;
    private boolean forceExit = true;

    // Loop control parameters
    private final long TOTAL_TEST_TIME = 60 * 1000; // 1 minute total test time
    private final long CONN_ACTIVE_TIME = 20 * 1000; // 20 seconds per connection
    private final long SLEEP_BETWEEN_CONN = 5 * 1000; // 5 seconds sleep between connections

    private final ExecutorService testTaskExecutorService = Executors.newCachedThreadPool();

    private CountDownLatch taskFinishLatch;
    private AtomicBoolean isConnected = new AtomicBoolean(false);
    private volatile FileSender currentPcmSender;

    public void setForceExit(boolean forceExit) {
        this.forceExit = forceExit;
    }

    public void start() {
        if (appId == null || token == null) {
            String[] keys = Utils.readAppIdAndToken(".keys");
            appId = keys[0];
            token = keys[1];
            SampleLogger.log("read appId: " + appId + " token: " + token + " from .keys");
        }

        int ret = 0;
        if (service == null) {
            // Initialize Agora service globally once
            service = new AgoraService();
            AgoraServiceConfig config = new AgoraServiceConfig();
            config.setAppId(appId);
            config.setEnableAudioDevice(0);
            config.setEnableAudioProcessor(1);
            config.setEnableVideo(1);
            config.setUseStringUid(0);
            config.setAudioScenario(Constants.AUDIO_SCENARIO_AI_SERVER);
            config.setLogFilePath(DEFAULT_LOG_PATH);
            config.setLogFileSize(DEFAULT_LOG_SIZE);
            config.setLogFilters(Constants.LOG_FILTER_DEBUG);

            ret = service.initialize(config);
            if (ret != 0) {
                SampleLogger.log(
                        "createAndInitAgoraService AgoraService.initialize fail ret:" + ret);
                releaseAgoraService();
                return;
            }
            SampleLogger.log("✅ AgoraService initialized successfully");
        }

        long testStartTime = System.currentTimeMillis();
        int loopCount = 1;

        // Loop: create conn -> send pcm -> destroy conn -> sleep -> repeat
        while (System.currentTimeMillis() - testStartTime < TOTAL_TEST_TIME) {
            long remainingTime = TOTAL_TEST_TIME - (System.currentTimeMillis() - testStartTime);
            SampleLogger.log("========================================");
            SampleLogger.log("Loop " + loopCount + " started, remaining time: " + remainingTime + "ms");
            SampleLogger.log("========================================");

            // Create connection
            if (!createConnection()) {
                SampleLogger.log("❌ Failed to create connection in loop " + loopCount);
                break;
            }

            // Send PCM data for CONN_ACTIVE_TIME (20 seconds)
            long connStartTime = System.currentTimeMillis();
            taskFinishLatch = new CountDownLatch(1);

            // Wait for connection to be established
            int waitCount = 0;
            while (!isConnected.get() && waitCount < 100) { // Wait up to 10 seconds
                try {
                    Thread.sleep(100);
                    waitCount++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!isConnected.get()) {
                SampleLogger.log("⚠️ Connection not established within timeout, skipping to next loop");
                releaseConn();
                loopCount++;
                continue;
            }

            // Start sending PCM data
            pushPcmData();

            // Wait for CONN_ACTIVE_TIME or until task finishes
            try {
                boolean finished = taskFinishLatch.await(CONN_ACTIVE_TIME, TimeUnit.MILLISECONDS);
                long actualActiveTime = System.currentTimeMillis() - connStartTime;
                if (finished) {
                    SampleLogger.log("✅ PCM sending completed in " + actualActiveTime + "ms");
                } else {
                    SampleLogger.log(
                            "⏱️ Connection active time reached (" + actualActiveTime + "ms), stopping PCM sending");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Destroy connection
            releaseConn();
            SampleLogger.log("✅ Connection destroyed in loop " + loopCount);

            // Check if we have time for another loop
            remainingTime = TOTAL_TEST_TIME - (System.currentTimeMillis() - testStartTime);
            if (remainingTime < SLEEP_BETWEEN_CONN) {
                SampleLogger.log("⏱️ Not enough time for another loop, exiting");
                break;
            }

            // Sleep between connections
            SampleLogger.log("😴 Sleeping for " + SLEEP_BETWEEN_CONN + "ms before next loop...");
            try {
                Thread.sleep(SLEEP_BETWEEN_CONN);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            loopCount++;
        }

        long totalTime = System.currentTimeMillis() - testStartTime;
        SampleLogger.log("========================================");
        SampleLogger.log("Test completed!");
        SampleLogger.log("Total loops: " + (loopCount - 1));
        SampleLogger.log("Total time: " + totalTime + "ms");
        SampleLogger.log("========================================");

        releaseAgoraService();
        if (forceExit) {
            System.exit(0);
        }
    }

    private boolean createConnection() {
        // Create a connection for each channel
        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        RtcConnPublishConfig publishConfig = new RtcConnPublishConfig();
        publishConfig.setAudioScenario(Constants.AUDIO_SCENARIO_AI_SERVER);
        publishConfig.setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT);
        publishConfig.setIsPublishAudio(true);
        publishConfig.setIsPublishVideo(false);
        publishConfig.setAudioPublishType(Constants.AudioPublishType.PCM);
        publishConfig.setVideoPublishType(Constants.VideoPublishType.NO_PUBLISH);

        conn = service.agoraRtcConnCreate(ccfg, publishConfig);
        if (conn == null) {
            SampleLogger.log("AgoraService.agoraRtcConnCreate fail\n");
            return false;
        }

        isConnected.set(false);
        int ret = conn.registerObserver(new IRtcConnObserver() {
            @Override
            public void onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason) {
                SampleLogger.log("onConnected channelId:" + connInfo.getChannelId()
                        + " userId:" + connInfo.getLocalUserId());
                userId = connInfo.getLocalUserId();
                isConnected.set(true);
            }

            @Override
            public void onUserJoined(AgoraRtcConn agoraRtcConn, String userId) {
                SampleLogger.log("onUserJoined userId:" + userId);
            }

            @Override
            public void onUserLeft(AgoraRtcConn agoraRtcConn, String userId, int reason) {
                SampleLogger.log("onUserLeft userId:" + userId + " reason:" + reason);
            }

            @Override
            public void onChangeRoleSuccess(AgoraRtcConn agoraRtcConn, int oldRole, int newRole) {
                SampleLogger.log("onChangeRoleSuccess oldRole:" + oldRole + " newRole:" + newRole);
            }

            @Override
            public void onChangeRoleFailure(AgoraRtcConn agoraRtcConn) {
                SampleLogger.log("onChangeRoleFailure");
            }
        });
        SampleLogger.log("registerObserver ret:" + ret);

        ret = conn.connect(token, channelId, userId);
        SampleLogger.log(
                "Connecting to Agora channel " + channelId + " with userId " + userId + " ret:" + ret);

        if (ret != 0) {
            SampleLogger.log("conn.connect fail ret=" + ret);
            return false;
        }

        // Create audio track
        conn.publishAudio();
        SampleLogger.log("✅ Connection created successfully");

        return true;
    }

    private void pushPcmData() {
        testTaskExecutorService.execute(() -> {
            int interval = 10; // ms

            int oneFramePcmDataSize = numOfChannels * (sampleRate / 1000) * interval * 2;
            // one frame pcm data size
            byte[] buffer = new byte[oneFramePcmDataSize];

            // first cache 10 frames data for 10ms interval, avoid sending silence packet
            // due to insufficient data in sdk
            ByteBuffer cachePcmDataBuffer = ByteBuffer.allocate(oneFramePcmDataSize * 10);

            currentPcmSender = new FileSender(audioFilePath, interval) {
                private boolean isStartPushAudioData = false;
                private int frameCount = 0;

                @Override
                public void sendOneFrame(byte[] data, long timestamp) {
                    if (!isConnected.get()) {
                        SampleLogger.log("Connection lost, stopping PCM sending");
                        release();
                        taskFinishLatch.countDown();
                        return;
                    }

                    if (null != data) {
                        int ret = conn.pushAudioPcmData(data, sampleRate, numOfChannels);
                        if (ret != 0) {
                            SampleLogger.log("pushAudioPcmData fail ret:" + ret);
                        } else {
                            frameCount++;
                            if (frameCount % 100 == 0) { // Log every 100 frames (1 second)
                                SampleLogger.log("pushAudioPcmData frame:" + frameCount
                                        + " data length:" + data.length
                                        + " sampleRate:" + sampleRate
                                        + " numOfChannels:" + numOfChannels);
                            }
                        }
                        if (!isStartPushAudioData) {
                            SampleLogger.log("pushAudioPcmData start");
                            isStartPushAudioData = true;
                        }
                    }

                    if (isStartPushAudioData) {
                        if (conn.isPushToRtcCompleted()) {
                            SampleLogger.log("pushAudioPcmData completed, total frames sent: " + frameCount);
                            release();
                            taskFinishLatch.countDown();
                        }
                    }
                }

                @Override
                public byte[] readOneFrame(FileInputStream fos) {
                    if (fos != null) {
                        try {
                            int size = fos.read(buffer, 0, oneFramePcmDataSize);
                            if (size < 0) {
                                return null;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (cachePcmDataBuffer.position() < cachePcmDataBuffer.capacity()) {
                        cachePcmDataBuffer.put(buffer);
                        if (cachePcmDataBuffer.position() < cachePcmDataBuffer.capacity()) {
                            return null;
                        } else {
                            return cachePcmDataBuffer.array();
                        }
                    } else {
                        return buffer;
                    }
                }

                @Override
                public void release() {
                    super.release();
                    cachePcmDataBuffer.clear();
                }
            };

            testTaskExecutorService.execute(currentPcmSender);
        });
    }

    private void releaseConn() {
        SampleLogger.log("releaseConn for channelId:" + channelId + " userId:" + userId);
        if (conn == null) {
            return;
        }

        isConnected.set(false);

        // Stop PCM sender if it's running
        if (currentPcmSender != null) {
            currentPcmSender.release();
            currentPcmSender = null;
        }

        int ret = conn.disconnect();
        if (ret != 0) {
            SampleLogger.log("conn.disconnect fail ret=" + ret);
        }

        conn.destroy();
        conn = null;

        SampleLogger.log("Disconnected from Agora channel successfully");
    }

    private void releaseAgoraService() {
        if (service != null) {
            service.destroy();
            service = null;
            SampleLogger.log("✅ AgoraService destroyed successfully");
        }
        testTaskExecutorService.shutdown();
        try {
            if (!testTaskExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
                testTaskExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            testTaskExecutorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        SampleLogger.log("releaseAgoraService");
    }
}
