package io.agora.rtc.example.basic;

import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.Constants;
import io.agora.rtc.IAudioFrameObserver;
import io.agora.rtc.IRtcConnObserver;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.RtcConnPublishConfig;
import io.agora.rtc.VadProcessResult;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.utils.Utils;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReceiverPcmDirectSendTest {
    private String appId;
    private String token;
    private final String DEFAULT_LOG_PATH = "logs/agora_logs/agorasdk.log";
    private final int DEFAULT_LOG_SIZE = 5 * 1024; // default log size is 5 mb

    private static AgoraService service;

    private AgoraRtcConn conn;
    private IAudioFrameObserver audioFrameObserver;

    private String channelId = "agaa";
    private String audioOutFile = "test_data_out/receiver_audio_out";
    private int numOfChannels = 1;
    private int sampleRate = 16000;
    private String remoteUserId = "";
    private long testTime = 60 * 1000;
    private boolean forceExit = true;

    private final ExecutorService singleExecutorService = Executors.newSingleThreadExecutor();
    private final ExecutorService senderExecutorService = Executors.newSingleThreadExecutor();

    private final AtomicBoolean connConnected = new AtomicBoolean(false);
    private UserIdHolder userIdHolder = new UserIdHolder("0");

    public void setForceExit(boolean forceExit) {
        this.forceExit = forceExit;
    }

    class UserIdHolder {
        private volatile String userId;

        public UserIdHolder(String userId) {
            this.userId = userId;
        }

        void set(String id) {
            this.userId = id;
        }

        String get() {
            return this.userId;
        }
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
                SampleLogger.log("onConnected channelId:" + connInfo.getChannelId()
                        + " userId:" + connInfo.getLocalUserId() + " reason:" + reason);
                connConnected.set(true);
                userIdHolder.set(connInfo.getLocalUserId());
            }

            @Override
            public void onUserJoined(AgoraRtcConn agoraRtcConn, String userId) {
                SampleLogger.log("onUserJoined userId:" + userId);
            }

            @Override
            public void onUserLeft(AgoraRtcConn agoraRtcConn, String userId, int reason) {
                SampleLogger.log("onUserLeft userId:" + userId + " reason:" + reason);
            }
        });
        SampleLogger.log("registerObserver ret:" + ret);

        if (!remoteUserId.isEmpty()) {
            conn.getLocalUser().subscribeAudio(remoteUserId);
        } else {
            conn.getLocalUser().subscribeAllAudio();
        }

        ret = conn.getLocalUser().setPlaybackAudioFrameBeforeMixingParameters(
                numOfChannels, sampleRate);
        SampleLogger.log("setPlaybackAudioFrameBeforeMixingParameters numOfChannels:"
                + numOfChannels + " sampleRate:" + sampleRate);
        if (ret != 0) {
            SampleLogger.log("setPlaybackAudioFrameBeforeMixingParameters fail ret=" + ret);
            releaseConn();
            releaseAgoraService();
            return;
        }
        audioFrameObserver = new IAudioFrameObserver() {
            boolean isFirstAudioFrame = true;

            @Override
            public int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agoraLocalUser,
                    String channelId, String userId, AudioFrame frame, VadProcessResult vadResult) {
                if (null == frame) {
                    return 0;
                }
                // Note: To improve data transmission efficiency, the buffer of the frame
                // object is a DirectByteBuffer.
                // Be sure to extract the byte array value in the callback synchronously
                // and then transfer it to the asynchronous thread for processing.
                // You can refer to {@link io.agora.rtc.utils.Utils#getBytes(ByteBuffer)}.
                byte[] byteArray = io.agora.rtc.utils.Utils.getBytes(frame.getBuffer());
                if (byteArray == null) {
                    return 0;
                }

                String finalAudioOutFile = audioOutFile + "_" + channelId + "_l" + userIdHolder.get() + "_r"
                        + userId + "_s" + sampleRate + "_c"
                        + numOfChannels + ".pcm";
                if (isFirstAudioFrame) {
                    isFirstAudioFrame = false;
                    new File(finalAudioOutFile).delete();
                }

                singleExecutorService.execute(() -> {
                    SampleLogger.log("onPlaybackAudioFrameBeforeMixing frame:" + frame
                            + "audioFrame size " + byteArray.length + " channelId:" + channelId
                            + " userId:" + userId);
                    Utils.writeBytesToFile(byteArray, finalAudioOutFile);
                });

                if (connConnected.get() && null != conn) {
                    senderExecutorService.execute(
                            () -> {
                                conn.pushAudioPcmData(byteArray, sampleRate, numOfChannels);
                            });
                }
                return 1;
            }
        };

        conn.registerAudioFrameObserver(audioFrameObserver, false, null);

        ret = conn.connect(token, channelId, userIdHolder.get());
        SampleLogger.log(
                "Connecting to Agora channel " + channelId + " with userId " + userIdHolder.get() + " ret:" + ret);
        if (ret != 0) {
            SampleLogger.log("conn.connect fail ret=" + ret);
            releaseConn();
            releaseAgoraService();
            return;
        }

        // Create audio track
        conn.publishAudio();

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < testTime) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        releaseConn();

        releaseAgoraService();

        if (forceExit) {
            System.exit(0);
        }
    }

    private void releaseConn() {
        SampleLogger.log("releaseConn for channelId:" + channelId + " userId:" + userIdHolder.get());
        if (conn == null) {
            return;
        }

        connConnected.set(false);

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
        singleExecutorService.shutdown();
        senderExecutorService.shutdown();

        SampleLogger.log("releaseAgoraService");
    }
}
