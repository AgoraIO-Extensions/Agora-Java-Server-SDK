package io.agora.rtc.example.scenario;

import io.agora.rtc.AgoraAudioEncodedFrameSender;
import io.agora.rtc.AgoraLocalAudioTrack;
import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AgoraMediaNodeFactory;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.Constants;
import io.agora.rtc.DefaultLocalUserObserver;
import io.agora.rtc.DefaultRtcConnObserver;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.example.utils.AudioSenderHelper;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.common.Utils;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SendOpusTest {
    static {
        System.loadLibrary("media_utils");
    }

    private static String appId;
    private static String token;
    private static final String DEFAULT_LOG_PATH = "agora_logs/agorasdk.log";
    private static final int DEFAULT_LOG_SIZE = 512 * 1024; // default log size is 512 kb
    private static final String channelId = "agaa";
    private static final String userId = "12345";
    private static final String filePath = "test_data/send_audio.opus";

    private static AgoraService service;
    private static AgoraRtcConn conn;
    private static AgoraMediaNodeFactory mediaNodeFactory;

    private static AgoraAudioEncodedFrameSender audioEncodedFrameSender;
    private static AgoraLocalAudioTrack customEncodedAudioTrack;

    private static AudioSenderHelper audioSenderHelper;

    private static CountDownLatch exitLatch;

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
            public void onConnected(AgoraRtcConn agora_rtc_conn, RtcConnInfo conn_info, int reason) {
                super.onConnected(agora_rtc_conn, conn_info, reason);
                SampleLogger.log(
                        "onConnected chennalId:" + conn_info.getChannelId() + " userId:" + conn_info.getLocalUserId());
                testTaskExecutorService.execute(() -> onConnConnected(agora_rtc_conn, conn_info, reason));
            }

            @Override
            public void onUserJoined(AgoraRtcConn agora_rtc_conn, String user_id) {
                super.onUserJoined(agora_rtc_conn, user_id);
                SampleLogger.log("onUserJoined user_id:" + user_id);

            }

            @Override
            public void onUserLeft(AgoraRtcConn agora_rtc_conn, String user_id, int reason) {
                super.onUserLeft(agora_rtc_conn, user_id, reason);
                SampleLogger.log("onUserLeft user_id:" + user_id + " reason:" + reason);

            }

            @Override
            public void onChangeRoleSuccess(AgoraRtcConn agora_rtc_conn, int old_role, int new_role) {
                SampleLogger.log("onChangeRoleSuccess old_role:" + old_role + " new_role:" + new_role);
            }

            @Override
            public void onChangeRoleFailure(AgoraRtcConn agora_rtc_conn) {
                SampleLogger.log("onChangeRoleFailure");
            }
        });
        SampleLogger.log("registerObserver ret:" + ret);

        ret = conn.connect(token, channelId, userId);
        SampleLogger.log("Connecting to Agora channel " + channelId + " with userId " + userId + " ret:" + ret);

        conn.getLocalUser().registerObserver(new DefaultLocalUserObserver() {
            @Override
            public void onStreamMessage(AgoraLocalUser agora_local_user, String user_id, int stream_id, String data,
                    long length) {
                SampleLogger.log("onStreamMessage: userid " + user_id + " stream_id " + stream_id + "  data " + data);
            }

            @Override
            public void onAudioPublishStateChanged(AgoraLocalUser agora_local_user, String channel, int old_state,
                    int new_state, int elapse_since_last_state) {
                SampleLogger
                        .log("onAudioPublishStateChanged channel:" + channel + " old_state:" + old_state + " new_state:"
                                + new_state + " userRole:" + agora_local_user.getUserRole());
            }

            @Override
            public void onVideoPublishStateChanged(AgoraLocalUser agora_local_user, String channel, int old_state,
                    int new_state, int elapse_since_last_state) {
                // TODO Auto-generated method stub
                SampleLogger
                        .log("onVideoPublishStateChanged channel:" + channel + " old_state:" + old_state + " new_state:"
                                + new_state + " userRole:" + agora_local_user.getUserRole());
            }
        });

        conn.getLocalUser().setAudioScenario(Constants.AUDIO_SCENARIO_CHORUS);

        mediaNodeFactory = service.createMediaNodeFactory();

        exitLatch = new CountDownLatch(1);
        try {
            exitLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        releaseConn();
    }

    private static void onConnConnected(AgoraRtcConn conn, RtcConnInfo connInfo, int reason) {
        // Create audio track
        audioEncodedFrameSender = mediaNodeFactory.createAudioEncodedFrameSender();
        customEncodedAudioTrack = service.createCustomAudioTrackEncoded(audioEncodedFrameSender,
                Constants.TMixMode.MIX_DISABLED.value);
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

        audioSenderHelper.send(
                new AudioSenderHelper.TaskInfo(
                        channelId,
                        userId,
                        filePath,
                        AudioSenderHelper.FileType.OPUS,
                        audioEncodedFrameSender,
                        1),
                true);

        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        audioSenderHelper.send(
                new AudioSenderHelper.TaskInfo(
                        channelId,
                        userId,
                        filePath,
                        AudioSenderHelper.FileType.OPUS,
                        audioEncodedFrameSender,
                        1),
                true);

    }

    private static void releaseConn() {
        SampleLogger.log("releaseConn for channelId:" + channelId + " userId:" + userId);
        if (conn == null) {
            return;
        }

        if (null != audioSenderHelper) {
            audioSenderHelper.destroy();
        }

        if (null != mediaNodeFactory) {
            mediaNodeFactory.destroy();
        }

        if (null != audioEncodedFrameSender) {
            audioEncodedFrameSender.destroy();
        }

        if (null != customEncodedAudioTrack) {
            conn.getLocalUser().unpublishAudio(customEncodedAudioTrack);
            customEncodedAudioTrack.destroy();
        }

        int ret = conn.disconnect();
        if (ret != 0) {
            SampleLogger.log("conn.disconnect fail ret=" + ret);
        }

        // Unregister connection observer
        conn.unregisterObserver();
        conn.getLocalUser().unregisterObserver();

        conn.destroy();

        mediaNodeFactory = null;
        audioEncodedFrameSender = null;
        customEncodedAudioTrack = null;

        conn = null;

        SampleLogger.log("Disconnected from Agora channel successfully");
    }

}
