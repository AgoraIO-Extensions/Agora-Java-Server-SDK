package io.agora.rtc.example.basic;

import io.agora.rtc.AgoraAudioEncodedFrameSender;
import io.agora.rtc.AgoraLocalAudioTrack;
import io.agora.rtc.AgoraMediaNodeFactory;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.Constants;
import io.agora.rtc.DefaultRtcConnObserver;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.utils.AudioSenderHelper;
import io.agora.rtc.example.utils.Utils;
import java.util.concurrent.atomic.AtomicBoolean;

public class SendOpusTest {
    static {
        System.loadLibrary("media_utils");
    }

    private String appId;
    private String token;
    private final String DEFAULT_LOG_PATH = "logs/agora_logs/agorasdk.log";
    private final int DEFAULT_LOG_SIZE = 5 * 1024; // default log size is 5 mb

    private static AgoraService service;
    private static AgoraMediaNodeFactory mediaNodeFactory;

    private AgoraRtcConn conn;
    private AgoraAudioEncodedFrameSender audioEncodedFrameSender;
    private AgoraLocalAudioTrack customEncodedAudioTrack;

    private AudioSenderHelper audioSenderHelper;

    private String channelId = "agaa";
    private String userId = "0";
    private String audioFilePath = "test_data/send_audio.opus";
    private long testTime = 60 * 1000;

    private final AtomicBoolean connConnected = new AtomicBoolean(false);

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

        audioEncodedFrameSender = mediaNodeFactory.createAudioEncodedFrameSender();
        customEncodedAudioTrack = service.createCustomAudioTrackEncoded(
            audioEncodedFrameSender, Constants.TMixMode.MIX_DISABLED.value);
        conn.getLocalUser().publishAudio(customEncodedAudioTrack);

        audioSenderHelper = new AudioSenderHelper();

        audioSenderHelper.setCallback(new AudioSenderHelper.AudioSenderCallback() {
            @Override
            public void onTaskStart(AudioSenderHelper.TaskInfo task) {
                SampleLogger.log("onTaskStart for task:" + task);
            }

            @Override
            public void onTaskComplete(AudioSenderHelper.TaskInfo task) {
                SampleLogger.log("onTaskComplete for task:" + task);
            }

            @Override
            public void onTaskCancel(AudioSenderHelper.TaskInfo task) {
                SampleLogger.log("onTaskCancel for task:" + task);
            }
        });
        while (!connConnected.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        audioSenderHelper.send(new AudioSenderHelper.TaskInfo(channelId, userId, audioFilePath,
                                   AudioSenderHelper.FileType.OPUS, audioEncodedFrameSender, 1),
            true);

        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        audioSenderHelper.send(new AudioSenderHelper.TaskInfo(channelId, userId, audioFilePath,
                                   AudioSenderHelper.FileType.OPUS, audioEncodedFrameSender, 1),
            true);

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < testTime) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        audioSenderHelper.cleanupAllTask();

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

        if (null != audioSenderHelper) {
            audioSenderHelper.destroy();
            audioSenderHelper = null;
        }

        if (null != audioEncodedFrameSender) {
            audioEncodedFrameSender.destroy();
            audioEncodedFrameSender = null;
        }

        if (null != customEncodedAudioTrack) {
            conn.getLocalUser().unpublishAudio(customEncodedAudioTrack);
            customEncodedAudioTrack.destroy();
            customEncodedAudioTrack = null;
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
