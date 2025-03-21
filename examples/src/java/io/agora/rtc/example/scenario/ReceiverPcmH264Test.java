package io.agora.rtc.example.scenario;

import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.AgoraVideoEncodedFrameObserver;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.Constants;
import io.agora.rtc.DefaultLocalUserObserver;
import io.agora.rtc.DefaultRtcConnObserver;
import io.agora.rtc.EncodedVideoFrameInfo;
import io.agora.rtc.IAudioFrameObserver;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.VadProcessResult;
import io.agora.rtc.VideoSubscriptionOptions;
import io.agora.rtc.example.common.SampleAudioFrameObserver;
import io.agora.rtc.example.common.SampleLocalUserObserver;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.common.SampleVideoEncodedFrameObserver;
import io.agora.rtc.example.utils.Utils;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceiverPcmH264Test {
    private static String appId;
    private static String token;
    private static String DEFAULT_LOG_PATH = "agora_logs/agorasdk.log";
    private static int DEFAULT_LOG_SIZE = 512 * 1024; // default log size is 512 kb
    private static String channelId = "agaa";
    private static String userId = "0";

    private static String audioOutFile = "test_data_out/receiver_audio_out";
    private static String videoOutFile = "test_data_out/receiver_video_out";

    private static AgoraService service;
    private static AgoraRtcConn conn;
    private static SampleLocalUserObserver localUserObserver;

    private static IAudioFrameObserver audioFrameObserver;
    private static AgoraVideoEncodedFrameObserver videoEncodedFrameObserver;

    private static CountDownLatch exitLatch;

    private static int numOfChannels = 1;
    private static int sampleRate = 16000;

    private static final ExecutorService testTaskExecutorService = Executors.newCachedThreadPool();
    private static final ExecutorService logExecutorService = Executors.newCachedThreadPool();

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

        if (parsedArgs.containsKey("-videoOutFile")) {
            videoOutFile = parsedArgs.get("-videoOutFile");
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
            public void onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason) {
                super.onConnected(agoraRtcConn, connInfo, reason);
                testTaskExecutorService.execute(() -> onConnConnected(agoraRtcConn, connInfo, reason));
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
        SampleLogger.log("Connecting to Agora channel " + channelId + " with userId " + userId + " ret:" + ret);

        conn.getLocalUser().registerObserver(new DefaultLocalUserObserver() {
            @Override
            public void onStreamMessage(AgoraLocalUser agoraLocalUser, String userId, int streamId, String data,
                    long length) {
                SampleLogger.log("onStreamMessage: userid " + userId + " streamId " + streamId + "  data " + data);
            }

            @Override
            public void onAudioPublishStateChanged(AgoraLocalUser agoraLocalUser, String channel, int oldState,
                    int newState, int elapseSinceLastState) {
                SampleLogger
                        .log("onAudioPublishStateChanged channel:" + channel + " oldState:" + oldState + " newState:"
                                + newState + " userRole:" + agoraLocalUser.getUserRole());
            }

            @Override
            public void onVideoPublishStateChanged(AgoraLocalUser agoraLocalUser, String channel, int oldState,
                    int newState, int elapseSinceLastState) {
                SampleLogger
                        .log("onVideoPublishStateChanged channel:" + channel + " oldState:" + oldState + " newState:"
                                + newState + " userRole:" + agoraLocalUser.getUserRole());
            }

            public void onUserVideoTrackStateChanged(AgoraLocalUser agoraLocalUser, String userId,
                    io.agora.rtc.AgoraRemoteVideoTrack agoraRemoteVideoTrack, int state, int reason, int elapsed) {
                SampleLogger.log("onUserVideoTrackStateChanged userId:" + userId + " state:" + state + " reason:"
                        + reason + " elapsed:" + elapsed);
            };
        });

        conn.getLocalUser().setAudioScenario(Constants.AUDIO_SCENARIO_CHORUS);

        exitLatch = new CountDownLatch(1);
        try {
            exitLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        releaseConn();
    }

    private static void onConnConnected(AgoraRtcConn conn, RtcConnInfo connInfo, int reason) {
        SampleLogger.log("onConnConnected connInfo :" + connInfo + " reason:" + reason);

        final String currentChannelId = connInfo.getChannelId();
        final String currentUserId = connInfo.getLocalUserId();

        conn.getLocalUser().subscribeAllAudio();
        // Register local user observer
        if (null == localUserObserver) {
            localUserObserver = new SampleLocalUserObserver();
        }
        conn.getLocalUser().registerObserver(localUserObserver);

        int ret = conn.getLocalUser().setPlaybackAudioFrameBeforeMixingParameters(numOfChannels, sampleRate);
        SampleLogger.log("setPlaybackAudioFrameBeforeMixingParameters numOfChannels:" + numOfChannels + " sampleRate:"
                + sampleRate);
        if (ret > 0) {
            SampleLogger.log("setPlaybackAudioFrameBeforeMixingParameters fail ret=" + ret);
            return;
        }

        audioFrameObserver = new SampleAudioFrameObserver(
                audioOutFile + "_" + currentChannelId + "_" + currentUserId + ".pcm") {
            @Override
            public int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agoraLocalUser, String channelId, String userId,
                    AudioFrame frame, VadProcessResult vadResult) {
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

                logExecutorService.execute(() -> {
                    SampleLogger.log("onPlaybackAudioFrameBeforeMixing frame:" + frame);
                    SampleLogger.log("onPlaybackAudioFrameBeforeMixing audioFrame size " + byteArray.length
                            + " channelId:"
                            + channelId + " userId:" + userId);
                });

                writeAudioFrameToFile(byteArray);
                return 1;
            }

        };
        conn.getLocalUser().registerAudioFrameObserver(audioFrameObserver, false, null);

        VideoSubscriptionOptions subscriptionOptions = new VideoSubscriptionOptions();
        subscriptionOptions.setEncodedFrameOnly(1);
        subscriptionOptions.setType(Constants.VIDEO_STREAM_HIGH);

        conn.getLocalUser().subscribeAllVideo(subscriptionOptions);

        videoEncodedFrameObserver = new AgoraVideoEncodedFrameObserver(
                new SampleVideoEncodedFrameObserver(
                        videoOutFile + "_" + currentChannelId + "_" + currentUserId + ".h264") {
                    @Override
                    public int onEncodedVideoFrame(AgoraVideoEncodedFrameObserver observer, int userId,
                            ByteBuffer buffer, EncodedVideoFrameInfo info) {
                        if (buffer == null || buffer.remaining() == 0) {
                            return 0;
                        }
                        // Note: To improve data transmission efficiency, the buffer of the frame
                        // object is a DirectByteBuffer.
                        // Be sure to extract the byte array value in the callback synchronously
                        // and then transfer it to the asynchronous thread for processing.
                        // You can refer to {@link io.agora.rtc.utils.Utils#getBytes(ByteBuffer)}.
                        byte[] byteArray = io.agora.rtc.utils.Utils.getBytes(buffer);
                        if (byteArray == null) {
                            return 0;
                        }
                        logExecutorService.execute(() -> {
                            SampleLogger.log("onEncodedVideoFrame userId:" + userId + " length " + byteArray.length
                                    + " with current channelId:"
                                    + channelId
                                    + "  userId:" + userId + " info:" + info);
                        });
                        writeVideoDataToFile(byteArray);
                        return 1;
                    }
                });
        conn.getLocalUser().registerVideoEncodedFrameObserver(videoEncodedFrameObserver);

        // if (null != exitLatch) {
        // exitLatch.countDown();
        // }
    }

    private static void releaseConn() {
        SampleLogger.log("releaseConn for channelId:" + channelId + " userId:" + userId);
        if (conn == null) {
            return;
        }

        if (null != audioFrameObserver) {
            conn.getLocalUser().unregisterAudioFrameObserver();
            audioFrameObserver = null;
        }

        if (null != videoEncodedFrameObserver) {
            conn.getLocalUser().unregisterVideoEncodedFrameObserver(videoEncodedFrameObserver);
            videoEncodedFrameObserver.destroy();
            videoEncodedFrameObserver = null;
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

        SampleLogger.log("Disconnected from Agora channel successfully");
    }

}
