package io.agora.rtc.example.basic;

import io.agora.rtc.AgoraAudioPcmDataSender;
import io.agora.rtc.AgoraLocalAudioTrack;
import io.agora.rtc.AgoraMediaNodeFactory;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.Constants;
import io.agora.rtc.DefaultRtcConnObserver;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.utils.Utils;
import io.agora.rtc.utils.AudioConsumerUtils;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SendPcmFileTest {
    private String appId;
    private String token;
    private final String DEFAULT_LOG_PATH = "logs/agora_logs/agorasdk.log";
    private final int DEFAULT_LOG_SIZE = 5 * 1024; // default log size is 5 mb
    private String channelId = "agaa";
    private String userId = "0";

    private static AgoraService service;
    private static AgoraMediaNodeFactory mediaNodeFactory;

    private AgoraRtcConn conn;
    private AgoraAudioPcmDataSender audioFrameSender;
    private AgoraLocalAudioTrack customAudioTrack;

    private AudioConsumerUtils audioConsumerUtils;

    private String audioFilePath = "test_data/tts_ai_16k_1ch.pcm";
    private int numOfChannels = 1;
    private int sampleRate = 16000;

    private final AtomicBoolean connConnected = new AtomicBoolean(false);

    private final ExecutorService testTaskExecutorService = Executors.newCachedThreadPool();

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
            config.setAudioScenario(Constants.AUDIO_SCENARIO_CHORUS);
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
            mediaNodeFactory = service.createMediaNodeFactory();
        }

        // Create a connection for each channel
        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        conn = service.agoraRtcConnCreate(ccfg);
        if (conn == null) {
            SampleLogger.log("AgoraService.agoraRtcConnCreate fail\n");
            releaseAgoraService();
            return;
        }

        ret = conn.registerObserver(new DefaultRtcConnObserver() {
            @Override
            public void onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason) {
                super.onConnected(agoraRtcConn, connInfo, reason);
                SampleLogger.log("onConnected chennalId:" + connInfo.getChannelId()
                    + " userId:" + connInfo.getLocalUserId());
                connConnected.set(true);
                userId = connInfo.getLocalUserId();
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

        audioFrameSender = mediaNodeFactory.createAudioPcmDataSender();
        // Create audio track
        customAudioTrack = service.createCustomAudioTrackPcm(audioFrameSender);
        // up to 16min,100000 frames, 10000 frames is enough
        customAudioTrack.setMaxBufferedAudioFrameNumber(100000);
        conn.getLocalUser().publishAudio(customAudioTrack);

        byte[] pcmData = Utils.readPcmFromFile(audioFilePath);
        // The data length must be an integer multiple of the data length of 1ms.If not,
        // then the remaining audio needs to be saved and sent together with the next audio.
        // Assuming 16-bit samples (2 bytes per sample).
        int bytesPerMs = numOfChannels * (sampleRate / 1000) * 2;
        if (bytesPerMs > 0 && pcmData.length % bytesPerMs != 0) {
            int newLength = (pcmData.length / bytesPerMs) * bytesPerMs;
            SampleLogger.log(String.format("sendPcmTask: pcmData length is not a multiple of "
                    + "1ms data bytes. Truncating from %d to %d bytes.",
                pcmData.length, newLength));
            pcmData = Arrays.copyOf(pcmData, newLength);
        }

        final byte[] finalPcmData = pcmData;
        long pcmDataTimeMs = finalPcmData.length / bytesPerMs;

        testTaskExecutorService.execute(() -> {
            AudioFrame audioFrame = new AudioFrame();
            audioFrame.setBuffer(ByteBuffer.wrap(finalPcmData));
            audioFrame.setRenderTimeMs(0);
            audioFrame.setSamplesPerChannel(finalPcmData.length / (2 * numOfChannels));
            audioFrame.setBytesPerSample(2);
            audioFrame.setChannels(numOfChannels);
            audioFrame.setSamplesPerSec(sampleRate);
            audioFrameSender.sendAudioPcmData(audioFrame);
            SampleLogger.log("sendAudioPcmData " + finalPcmData.length);
        });

        try {
            Thread.sleep(pcmDataTimeMs + 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        releaseConn();
        releaseAgoraService();
        System.exit(0);
    }

    private void releaseConn() {
        SampleLogger.log("releaseConn for channelId:" + channelId + " userId:" + userId);
        if (conn == null) {
            return;
        }

        connConnected.set(false);

        if (audioConsumerUtils != null) {
            audioConsumerUtils.release();
            audioConsumerUtils = null;
        }

        if (null != audioFrameSender) {
            audioFrameSender.destroy();
            audioFrameSender = null;
        }

        if (null != customAudioTrack) {
            customAudioTrack.clearSenderBuffer();
            conn.getLocalUser().unpublishAudio(customAudioTrack);
            customAudioTrack.destroy();
            customAudioTrack = null;
        }

        int ret = conn.disconnect();
        if (ret != 0) {
            SampleLogger.log("conn.disconnect fail ret=" + ret);
        }

        // Unregister connection observer
        conn.unregisterObserver();
        conn.getLocalUser().unregisterObserver();

        conn.destroy();
        conn = null;

        testTaskExecutorService.shutdown();

        SampleLogger.log("Disconnected from Agora channel successfully");
    }

    private void releaseAgoraService() {
        if (null != mediaNodeFactory) {
            mediaNodeFactory.destroy();
            mediaNodeFactory = null;
        }

        if (service != null) {
            service.destroy();
            service = null;
        }
        SampleLogger.log("releaseAgoraService");
    }
}
