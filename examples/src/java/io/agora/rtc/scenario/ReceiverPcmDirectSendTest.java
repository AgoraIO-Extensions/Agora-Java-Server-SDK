package io.agora.rtc.scenario;

import io.agora.rtc.AgoraAudioPcmDataSender;
import io.agora.rtc.AgoraLocalAudioTrack;
import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AgoraMediaNodeFactory;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.Constants;
import io.agora.rtc.DefaultLocalUserObserver;
import io.agora.rtc.DefaultRtcConnObserver;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.VadProcessResult;
import io.agora.rtc.common.SampleAudioFrameObserver;
import io.agora.rtc.common.SampleLocalUserObserver;
import io.agora.rtc.common.SampleLogger;
import io.agora.rtc.common.Utils;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceiverPcmDirectSendTest {
    private static String appId;
    private static String token;
    private static String DEFAULT_LOG_PATH = "agora_logs/agorasdk.log";
    private static int DEFAULT_LOG_SIZE = 512 * 1024; // default log size is 512 kb

    private static AgoraService service;
    private static AgoraRtcConn conn;
    private static SampleLocalUserObserver localUserObserver;
    private static AgoraMediaNodeFactory mediaNodeFactory;

    private static AgoraAudioPcmDataSender audioFrameSender;
    private static AgoraLocalAudioTrack customAudioTrack;

    private static CountDownLatch exitLatch;

    private static String channelId = "agaa";
    private static String userId = "12345";
    private static String audioOutFile = "test_data_out/receiver_audio_out";
    private static int numOfChannels = 1;
    private static int sampleRate = 16000;

    private static final ExecutorService testTaskExecutorService = Executors.newCachedThreadPool();
    private static final ExecutorService logExecutorService = Executors.newCachedThreadPool();
    private static final ExecutorService senderExecutorService = Executors.newSingleThreadExecutor();

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

        if (parsedArgs.containsKey("-audioOutFile")) {
            audioOutFile = parsedArgs.get("-audioOutFile");
        }

        if (parsedArgs.containsKey("-numOfChannels")) {
            numOfChannels = Integer.parseInt(parsedArgs.get("-numOfChannels"));
        }

        if (parsedArgs.containsKey("-sampleRate")) {
            sampleRate = Integer.parseInt(parsedArgs.get("-sampleRate"));
        }
    }

    public static void main(String[] args) {
        parseArgs(args);
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
                SampleLogger
                        .log("onVideoPublishStateChanged channel:" + channel + " old_state:" + old_state + " new_state:"
                                + new_state + " userRole:" + agora_local_user.getUserRole());
            }

            public void onUserVideoTrackStateChanged(AgoraLocalUser agora_local_user, String user_id,
                    io.agora.rtc.AgoraRemoteVideoTrack agora_remote_video_track, int state, int reason, int elapsed) {
                SampleLogger.log("onUserVideoTrackStateChanged user_id:" + user_id + " state:" + state + " reason:"
                        + reason + " elapsed:" + elapsed);
            };
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
        SampleLogger.log("onConnConnected channelId :" + connInfo.getChannelId() + " reason:" + reason);

        audioFrameSender = mediaNodeFactory.createAudioPcmDataSender();
        // Create audio track
        customAudioTrack = service.createCustomAudioTrackPcm(audioFrameSender);
        conn.getLocalUser().publishAudio(customAudioTrack);

        conn.getLocalUser().subscribeAllAudio();
        // Register local user observer
        if (null == localUserObserver) {
            localUserObserver = new SampleLocalUserObserver(conn.getLocalUser());
        }
        conn.getLocalUser().registerObserver(localUserObserver);

        int ret = conn.getLocalUser().setPlaybackAudioFrameBeforeMixingParameters(numOfChannels, sampleRate);
        SampleLogger.log("setPlaybackAudioFrameBeforeMixingParameters numOfChannels:" + numOfChannels + " sampleRate:"
                + sampleRate);
        if (ret > 0) {
            SampleLogger.log("setPlaybackAudioFrameBeforeMixingParameters fail ret=" + ret);
            return;
        }

        localUserObserver.setAudioFrameObserver(
                new SampleAudioFrameObserver(audioOutFile + "_" + channelId + "_" + userId + ".pcm") {
                    @Override
                    public int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agora_local_user, String channel_id,
                            String uid,
                            AudioFrame frame, VadProcessResult vadResult) {
                        if (null == frame) {
                            return 0;
                        }
                        logExecutorService.execute(() -> {
                            SampleLogger.log("onPlaybackAudioFrameBeforeMixing frame:" + frame);
                            SampleLogger.log(
                                    "onPlaybackAudioFrameBeforeMixing audioFrame size " + frame.getBuffer().capacity()
                                            + " channel_id:"
                                            + channel_id + " uid:" + uid + " with current channelId:"
                                            + channelId
                                            + "  userId:" + userId);
                        });

                        byte[] byteArray = new byte[frame.getBuffer().remaining()];
                        frame.getBuffer().get(byteArray);
                        frame.getBuffer().rewind();

                        writeAudioFrameToFile(byteArray);

                        senderExecutorService.execute(() -> {
                            audioFrameSender.send(byteArray, 0,
                                    byteArray.length / 2 / numOfChannels, 2,
                                    numOfChannels,
                                    sampleRate);
                        });
                        return 1;
                    }

                });

        // if (null != exitLatch) {
        // exitLatch.countDown();
        // }
    }

    private static void releaseConn() {
        SampleLogger.log("releaseConn for channelId:" + channelId + " userId:" + userId);
        if (conn == null) {
            return;
        }

        if (null != mediaNodeFactory) {
            mediaNodeFactory.destroy();
        }

        if (null != audioFrameSender) {
            audioFrameSender.destroy();
        }

        if (null != customAudioTrack) {
            customAudioTrack.clearSenderBuffer();
            conn.getLocalUser().unpublishAudio(customAudioTrack);
            customAudioTrack.destroy();
        }

        if (null != localUserObserver) {
            localUserObserver.unregisterAudioFrameObserver();
        }

        int ret = conn.disconnect();
        if (ret != 0) {
            SampleLogger.log("conn.disconnect fail ret=" + ret);
        }

        // Unregister connection observer
        conn.unregisterObserver();
        conn.getLocalUser().unregisterObserver();

        conn.destroy();

        localUserObserver = null;

        conn = null;

        testTaskExecutorService.shutdown();
        logExecutorService.shutdown();
        senderExecutorService.shutdown();

        SampleLogger.log("Disconnected from Agora channel successfully");
    }

}
