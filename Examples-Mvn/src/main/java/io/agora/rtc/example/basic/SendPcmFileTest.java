package io.agora.rtc.example.basic;

import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcConnObserver;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.RtcConnPublishConfig;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.utils.Utils;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SendPcmFileTest {
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
    private int pushMaxCount = 100;

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
            byte[] pcmData = Utils.readPcmFromFile(audioFilePath);
            // The data length must be an integer multiple of the data length of 1ms.If not,
            // then the remaining audio needs to be saved and sent together with the next
            // audio.
            // Assuming 16-bit samples (2 bytes per sample).
            int bytesPerMs = numOfChannels * (sampleRate / 1000) * 2;
            if (bytesPerMs > 0 && pcmData.length % bytesPerMs != 0) {
                int newLength = (pcmData.length / bytesPerMs) * bytesPerMs;
                SampleLogger.log(String.format("sendPcmTask: pcmData length is not a multiple of "
                        + "1ms data bytes. Truncating from %d to %d bytes.",
                        pcmData.length, newLength));
                pcmData = Arrays.copyOf(pcmData, newLength);
            }

            SampleLogger.log("pushPcmData start pcm data length:" + pcmData.length + " time:"
                    + (pcmData.length / bytesPerMs) + "ms");

            int count = 1;
            while (count <= pushMaxCount) {
                int ret = conn.pushAudioPcmData(pcmData, sampleRate, numOfChannels);
                SampleLogger.log("pushAudioPcmData " + pcmData.length + " ret: " + ret);

                while (!conn.isPushToRtcCompleted()) {
                    // SampleLogger.log("pushAudioPcmData not completed");
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                SampleLogger.log("pushAudioPcmData completed count:" + count);
                count++;
            }
            taskFinishLatch.countDown();
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
