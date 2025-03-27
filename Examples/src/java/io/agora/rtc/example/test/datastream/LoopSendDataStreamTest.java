package io.agora.rtc.example.test.datastream;

import io.agora.rtc.AgoraAudioPcmDataSender;
import io.agora.rtc.AgoraLocalAudioTrack;
import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AgoraMediaNodeFactory;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.Constants;
import io.agora.rtc.DefaultLocalUserObserver;
import io.agora.rtc.DefaultRtcConnObserver;
import io.agora.rtc.Out;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.utils.Utils;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoopSendDataStreamTest {
    private static String appId;
    private static String token;
    private static String DEFAULT_LOG_PATH = "agora_logs/agorasdk.log";
    private static int DEFAULT_LOG_SIZE = 512 * 1024; // default log size is 512 kb
    private static String channelId = "dataStreamChannel";
    private static String userId = "0";

    private static AgoraService service;
    private static AgoraRtcConn conn;

    private static final ExecutorService testTaskExecutorService = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        String[] keys = Utils.readAppIdAndToken(".keys");
        appId = keys[0];
        token = keys[1];
        SampleLogger.log("read appId: " + appId + " token: " + token + " from .keys");

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
            return;
        }

        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        conn = service.agoraRtcConnCreate(ccfg);
        if (conn == null) {
            SampleLogger.log("AgoraService.agoraRtcConnCreate fail\n");
            return;
        }

        ret = conn.registerObserver(new DefaultRtcConnObserver() {
            @Override
            public void onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason) {
                super.onConnected(agoraRtcConn, connInfo, reason);
                testTaskExecutorService.execute(() -> onConnConnected(agoraRtcConn, connInfo, reason));
            }

            @Override
            public void onDisconnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason) {
                super.onDisconnected(agoraRtcConn, connInfo, reason);
                testTaskExecutorService.execute(() -> onConnDisconnected(agoraRtcConn, connInfo, reason));
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

        conn.getLocalUser().registerObserver(new DefaultLocalUserObserver() {
            @Override
            public void onStreamMessage(AgoraLocalUser agoraLocalUser, String userId, int streamId, String data,
                    long length) {
                SampleLogger
                        .log("onStreamMessage: userid " + userId + " streamId " + streamId + "  data " + data);
            }
        });

        conn.getLocalUser().setAudioScenario(Constants.AUDIO_SCENARIO_CHORUS);

        int currentIndex = 0;
        while (currentIndex < 100) {
            currentIndex++;
            ret = conn.connect(token, channelId, userId);
            SampleLogger.log("Connecting to Agora channel " + channelId + " with userId " + userId + " ret:" + ret);
            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Out<Integer> streamId = new Out<Integer>();

            int result = conn.createDataStream(streamId, 0, 0);
            if (result != 0) {
                SampleLogger.log("createDataStream failed, result: " + result);
                return;
            }
            String data = Utils.getCurrentTime() + " hello world from channelId:"
                    + channelId + " userId:" + userId;
            ret = conn.sendStreamMessage(streamId.get(), data.getBytes());
            SampleLogger.log("sendStreamMessage index:" + currentIndex + " " + data + " done ret:" + ret);

            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            conn.disconnect();

            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        releaseConn();

        System.exit(0);
    }

    private static void onConnConnected(AgoraRtcConn conn, RtcConnInfo connInfo, int reason) {
        SampleLogger
                .log("onConnConnected channelId:" + connInfo.getChannelId() + " userId:" + connInfo.getLocalUserId());
    }

    private static void onConnDisconnected(AgoraRtcConn conn, RtcConnInfo connInfo, int reason) {
        SampleLogger
                .log("onConnDisconnected channelId:" + connInfo.getChannelId() + " userId:"
                        + connInfo.getLocalUserId());
    }

    private static void releaseConn() {
        SampleLogger.log("releaseConn for channelId:" + channelId + " userId:" + userId);
        if (conn == null) {
            return;
        }

        // if (null != mediaNodeFactory) {
        // mediaNodeFactory.destroy();
        // }

        // if (null != audioFrameSender) {
        // audioFrameSender.destroy();
        // }

        // if (null != customAudioTrack) {
        // customAudioTrack.clearSenderBuffer();
        // conn.getLocalUser().unpublishAudio(customAudioTrack);
        // customAudioTrack.destroy();
        // }

        // if (null != customEncodedImageSender) {
        // customEncodedImageSender.destroy();
        // }

        // if (null != customEncodedVideoTrack) {
        // conn.getLocalUser().unpublishVideo(customEncodedVideoTrack);
        // customEncodedVideoTrack.destroy();
        // }

        // if (null != videoFrameSender) {
        // videoFrameSender.destroy();
        // }

        // if (null != customVideoTrack) {
        // conn.getLocalUser().unpublishVideo(customVideoTrack);
        // customVideoTrack.destroy();
        // }

        // if (null != audioEncodedFrameSender) {
        // audioEncodedFrameSender.destroy();
        // }

        // if (null != customEncodedAudioTrack) {
        // conn.getLocalUser().unpublishAudio(customEncodedAudioTrack);
        // customEncodedAudioTrack.destroy();
        // }

        // if (null != localUserObserver) {
        // localUserObserver.unregisterAudioFrameObserver();
        // localUserObserver.unregisterVideoFrameObserver();
        // }

        // int ret = conn.disconnect();
        // if (ret != 0) {
        // SampleLogger.log("conn.disconnect fail ret=" + ret);
        // }

        // Unregister connection observer
        conn.unregisterObserver();
        conn.getLocalUser().unregisterObserver();

        conn.destroy();

        // mediaNodeFactory = null;
        // audioFrameSender = null;
        // customAudioTrack = null;
        // customEncodedImageSender = null;
        // customEncodedVideoTrack = null;
        // videoFrameSender = null;
        // customVideoTrack = null;
        // audioEncodedFrameSender = null;
        // customEncodedAudioTrack = null;
        // localUserObserver = null;

        conn = null;

        SampleLogger.log("Disconnected from Agora channel successfully");

    }

}
