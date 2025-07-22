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
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AIReceiverSendPcmTest {
    private String appId;
    private String token;
    private final String DEFAULT_LOG_PATH = "logs/agora_logs/agorasdk.log";
    private final int DEFAULT_LOG_SIZE = 5 * 1024; // default log size is 5 mb
    private String channelId = "agaa";
    private String remoteUserId = "";

    private static AgoraService service;

    private String audioFilePath = "test_data/send_audio_16k_1ch.pcm";
    private String audioOutFile = "test_data_out/receiver_audio_out_ai.pcm";
    private int numOfChannels = 1;
    private int sampleRate = 16000;
    private long testTime = 2 * 1000;

    private final ExecutorService testTaskExecutorService = Executors.newCachedThreadPool();
    private final ExecutorService fileWriteTaskExecutorService =
        Executors.newSingleThreadExecutor();

    private final AtomicInteger testTaskCount = new AtomicInteger(0);

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

        testTaskExecutorService.execute(() -> {
            testTaskCount.incrementAndGet();
            startReceivePcmData();
        });

        testTaskExecutorService.execute(() -> {
            testTaskCount.incrementAndGet();
            startSendPcmData();
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

    private void startSendPcmData() {
        AtomicBoolean connConnected = new AtomicBoolean(false);
        String userId = "0";

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
        AgoraRtcConn conn = service.agoraRtcConnCreate(ccfg, publishConfig);
        if (conn == null) {
            SampleLogger.log("AgoraService.agoraRtcConnCreate fail\n");
            releaseAgoraService();
            return;
        }

        int ret = conn.registerObserver(new IRtcConnObserver() {
            @Override
            public void onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason) {
                SampleLogger.log("onConnected chennalId:" + connInfo.getChannelId()
                    + " userId:" + connInfo.getLocalUserId());
                connConnected.set(true);
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
            public int onAIQoSCapabilityMissing(
                AgoraRtcConn agoraRtcConn, int defaultFallbackSenario) {
                SampleLogger.log(
                    "onAIQoSCapabilityMissing defaultFallbackSenario:" + defaultFallbackSenario);
                return defaultFallbackSenario;
            }
        });
        SampleLogger.log("registerObserver ret:" + ret);

        ret = conn.connect(token, channelId, userId);
        SampleLogger.log(
            "Connecting to Agora channel " + channelId + " with userId " + userId + " ret:" + ret);

        if (ret != 0) {
            SampleLogger.log("conn.connect fail ret=" + ret);
            releaseConn(conn, channelId, userId);
            releaseAgoraService();
            return;
        }

        // Create audio track
        conn.publishAudio();

        while (!connConnected.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        byte[] pcmData = Utils.readPcmFromFile(audioFilePath);
        // The data length must be an integer multiple of the data length of 1ms.
        // Assuming 16-bit samples (2 bytes per sample).
        int bytesPerMs = numOfChannels * (sampleRate / 1000) * 2;
        if (bytesPerMs > 0 && pcmData.length % bytesPerMs != 0) {
            int newLength = (pcmData.length / bytesPerMs) * bytesPerMs;
            SampleLogger.log(String.format("sendPcmTask: pcmData length is not a multiple of "
                    + "1ms data bytes. Truncating from %d to %d bytes.",
                pcmData.length, newLength));
            pcmData = Arrays.copyOf(pcmData, newLength);
        }

        ret = conn.pushAudioPcmData(pcmData, sampleRate, numOfChannels);
        if (ret < 0) {
            SampleLogger.log("sendPcmTask pushAudioPcmData fail ret:" + ret);
        }

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < testTime) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        releaseConn(conn, channelId, userId);
        testTaskCount.decrementAndGet();
    }

    private void startReceivePcmData() {
        String userId = "0";

        // Create a connection for each channel
        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_AUDIENCE);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        RtcConnPublishConfig publishConfig = new RtcConnPublishConfig();
        publishConfig.setAudioScenario(Constants.AUDIO_SCENARIO_AI_SERVER);
        publishConfig.setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT);
        publishConfig.setIsPublishAudio(false);
        publishConfig.setIsPublishVideo(false);
        publishConfig.setAudioPublishType(Constants.AudioPublishType.NO_PUBLISH);
        publishConfig.setVideoPublishType(Constants.VideoPublishType.NO_PUBLISH);
        AgoraRtcConn conn = service.agoraRtcConnCreate(ccfg, publishConfig);
        if (conn == null) {
            SampleLogger.log("AgoraService.agoraRtcConnCreate fail\n");
            releaseAgoraService();
            return;
        }

        AtomicBoolean connConnected = new AtomicBoolean(false);

        int ret = conn.registerObserver(new IRtcConnObserver() {
            @Override
            public void onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason) {
                SampleLogger.log("onConnected chennalId:" + connInfo.getChannelId()
                    + " userId:" + connInfo.getLocalUserId());
                connConnected.set(true);
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
            public int onAIQoSCapabilityMissing(
                AgoraRtcConn agoraRtcConn, int defaultFallbackSenario) {
                SampleLogger.log(
                    "onAIQoSCapabilityMissing defaultFallbackSenario:" + defaultFallbackSenario);
                return defaultFallbackSenario;
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
            releaseConn(conn, channelId, userId);
            releaseAgoraService();
            return;
        }

        File file = new File(audioOutFile);
        if (file.exists()) {
            file.delete();
        }

        IAudioFrameObserver audioFrameObserver = new IAudioFrameObserver() {
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

                SampleLogger.log("onPlaybackAudioFrameBeforeMixing frame:" + frame
                    + "audioFrame size " + byteArray.length + " channelId:" + channelId
                    + " userId:" + userId);

                fileWriteTaskExecutorService.execute(
                    () -> { Utils.writeBytesToFile(byteArray, audioOutFile); });
                return 1;
            }
        };

        conn.registerAudioFrameObserver(audioFrameObserver, false, null);

        ret = conn.connect(token, channelId, userId);
        SampleLogger.log(
            "Connecting to Agora channel " + channelId + " with userId " + userId + " ret:" + ret);
        if (ret != 0) {
            SampleLogger.log("conn.connect fail ret=" + ret);
            releaseConn(conn, channelId, userId);
            releaseAgoraService();
            return;
        }

        while (!connConnected.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < testTime) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        releaseConn(conn, channelId, userId);
        testTaskCount.decrementAndGet();
    }

    private void releaseConn(AgoraRtcConn conn, String channelId, String userId) {
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

        fileWriteTaskExecutorService.shutdown();
        testTaskExecutorService.shutdown();

        SampleLogger.log("releaseAgoraService");
    }
}
