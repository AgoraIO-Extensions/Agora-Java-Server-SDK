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

public class SendPcmRealTimeTest {
    private String appId;
    private String token;
    private final String DEFAULT_LOG_PATH = "logs/agora_logs/agorasdk.log";
    private final int DEFAULT_LOG_SIZE = 5 * 1024; // default log size is 5 mb

    private static AgoraService service;

    private AgoraRtcConn conn;

    private String channelId = "agaa";
    private String userId = "0";
    private String audioFilePath = "test_data/tts_ai_16k_1ch.pcm";
    private int numOfChannels = 1;
    private int sampleRate = 16000;

    private final ExecutorService testTaskExecutorService = Executors.newCachedThreadPool();

    private CountDownLatch taskFinishLatch = new CountDownLatch(1);

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
        }

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
            releaseAgoraService();
            return;
        }

        ret = conn.registerObserver(new IRtcConnObserver() {
            @Override
            public void onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason) {
                SampleLogger.log("onConnected chennalId:" + connInfo.getChannelId()
                    + " userId:" + connInfo.getLocalUserId());
                userId = connInfo.getLocalUserId();
            }

            @Override
            public void onUserJoined(AgoraRtcConn agoraRtcConn, String userId) {
                SampleLogger.log("onUserJoined userId:" + userId);
                pushPcmData();
            }

            @Override
            public void onUserLeft(AgoraRtcConn agoraRtcConn, String userId, int reason) {
                SampleLogger.log("onUserLeft userId:" + userId + " reason:" + reason);
            }
        });
        SampleLogger.log("registerObserver ret:" + ret);

        ret = conn.connect(token, channelId, userId);
        SampleLogger.log(
            "Connecting to Agora channel " + channelId + " with userId " + userId + " ret:" + ret);

        if (ret != 0) {
            SampleLogger.log("conn.connect fail ret=" + ret);
            releaseConn();
            releaseAgoraService();
            return;
        }

        // Create audio track
        conn.publishAudio();

        try {
            taskFinishLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        releaseConn();
        releaseAgoraService();
        System.exit(0);
    }

    private void pushPcmData() {
        testTaskExecutorService.execute(() -> {
            int interval = 10; // ms

            int oneFramePcmDataSize = numOfChannels * (sampleRate / 1000) * interval * 2;
            // one frame pcm data size
            byte[] buffer = new byte[oneFramePcmDataSize];

            // first cache 10 frames data for 10ms interval, avoid sending silence packet due to
            // insufficient data in sdk
            ByteBuffer cachePcmDataBuffer = ByteBuffer.allocate(oneFramePcmDataSize * 10);

            FileSender pcmSendThread = new FileSender(audioFilePath, interval) {
                private boolean isStartPushAudioData = false;
                @Override
                public void sendOneFrame(byte[] data, long timestamp) {
                    if (null != data) {
                        int ret = conn.pushAudioPcmData(data, sampleRate, numOfChannels);
                        if (ret != 0) {
                            SampleLogger.log("pushAudioPcmData fail ret:" + ret);
                        }
                        if (!isStartPushAudioData) {
                            SampleLogger.log("pushAudioPcmData start");
                            isStartPushAudioData = true;
                        }
                    }

                    if (isStartPushAudioData) {
                        if (conn.isPushToRtcCompleted()) {
                            SampleLogger.log("pushAudioPcmData completed");
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

            testTaskExecutorService.execute(pcmSendThread);
        });
    }

    private void releaseConn() {
        SampleLogger.log("releaseConn for channelId:" + channelId + " userId:" + userId);
        if (conn == null) {
            return;
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
        }
        testTaskExecutorService.shutdown();

        SampleLogger.log("releaseAgoraService");
    }
}
