package io.agora.rtc.example.scenario;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AgoraLocalVideoTrack;
import io.agora.rtc.AgoraMediaNodeFactory;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.AgoraVideoEncodedImageSender;
import io.agora.rtc.Constants;
import io.agora.rtc.DefaultLocalUserObserver;
import io.agora.rtc.DefaultRtcConnObserver;
import io.agora.rtc.EncodedVideoFrameInfo;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.SenderOptions;
import io.agora.rtc.SimulcastStreamConfig;
import io.agora.rtc.VideoDimensions;
import io.agora.rtc.example.common.FileSender;
import io.agora.rtc.example.common.SampleLocalUserObserver;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.mediautils.H264Reader;
import io.agora.rtc.example.utils.Utils;

public class SendH264Test {
    static {
        System.loadLibrary("media_utils");
    }

    private static String appId;
    private static String token;
    private static String DEFAULT_LOG_PATH = "agora_logs/agorasdk.log";
    private static int DEFAULT_LOG_SIZE = 512 * 1024; // default log size is 512 kb

    private static AgoraService service;
    private static AgoraRtcConn conn;
    private static SampleLocalUserObserver localUserObserver;
    private static AgoraMediaNodeFactory mediaNodeFactory;

    private static AgoraVideoEncodedImageSender customEncodedImageSender;
    private static AgoraLocalVideoTrack customEncodedVideoTrack;

    private static CountDownLatch exitLatch;

    private static String channelId = "agaa";
    private static String userId = "0";
    private static int width = 360;
    private static int height = 640;
    private static int fps = 15;
    private static String streamType = "high";
    private static boolean enableSimulcastStream = false;
    private static String videoFile = "test_data/send_video.h264";

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

        if (parsedArgs.containsKey("-width")) {
            width = Integer.parseInt(parsedArgs.get("-width"));
        }

        if (parsedArgs.containsKey("-height")) {
            height = Integer.parseInt(parsedArgs.get("-height"));
        }

        if (parsedArgs.containsKey("-fps")) {
            fps = Integer.parseInt(parsedArgs.get("-fps"));
        }

        if (parsedArgs.containsKey("-streamType")) {
            streamType = parsedArgs.get("-streamType");
        }

        if (parsedArgs.containsKey("-enableSimulcastStream")) {
            enableSimulcastStream = Boolean.parseBoolean(parsedArgs.get("-enableSimulcastStream"));
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
        final String currentUserId = connInfo.getLocalUserId();
        final String currentChannelId = connInfo.getChannelId();

        // Register local user observer
        if (null == localUserObserver) {
            localUserObserver = new SampleLocalUserObserver(conn.getLocalUser());
        }
        conn.getLocalUser().registerObserver(localUserObserver);

        customEncodedImageSender = mediaNodeFactory.createVideoEncodedImageSender();
        // Create video track
        SenderOptions option = new SenderOptions();
        option.setCcMode(Constants.TCC_ENABLED);
        customEncodedVideoTrack = service.createCustomVideoTrackEncoded(customEncodedImageSender, option);

        if (enableSimulcastStream) {
            SimulcastStreamConfig lowStreamConfig = new SimulcastStreamConfig();
            lowStreamConfig.setBitrate(65);
            lowStreamConfig.setFramerate(fps);
            VideoDimensions dimensions = new VideoDimensions(width, height);
            lowStreamConfig.setDimensions(dimensions);
            customEncodedVideoTrack.enableSimulcastStream(1, lowStreamConfig);
        }

        // Publish video track
        int ret = conn.getLocalUser().publishVideo(customEncodedVideoTrack);
        SampleLogger.log("sendH264Task publishVideo ret:" + ret);

        H264Reader h264Reader = new H264Reader(videoFile);

        FileSender h264SendThread = new FileSender(videoFile, 1000 / fps) {
            int lastFrameType = 0;
            int frameIndex = 0;

            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                if (data == null) {
                    return;
                }
                EncodedVideoFrameInfo info = new EncodedVideoFrameInfo();
                long currentTime = timestamp;
                info.setFrameType(lastFrameType);
                info.setStreamType(
                        streamType.equals("high") ? Constants.VIDEO_STREAM_HIGH : Constants.VIDEO_STREAM_LOW);
                info.setWidth(width);
                info.setHeight(height);
                info.setCodecType(Constants.VIDEO_CODEC_H264);
                info.setCaptureTimeMs(currentTime);
                info.setDecodeTimeMs(currentTime);
                info.setFramesPerSecond(fps);
                info.setRotation(0);

                customEncodedImageSender.send(data, data.length, info);
                frameIndex++;

                logExecutorService.execute(() -> {
                    SampleLogger.log("send h264 frame data size:" + data.length +
                            " timestamp:" + timestamp + " frameIndex:" + frameIndex
                            + " from channelId:" + currentChannelId + " userId:" + currentUserId);
                });
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                H264Reader.H264Frame frame = h264Reader.readNextFrame();
                if (frame == null) {
                    h264Reader.reset();
                    reset();
                    return null;
                }

                lastFrameType = frame.frameType;
                return frame.data;
            }

            @Override
            public void release(boolean withJoin) {
                super.release(withJoin);
                if (null != h264Reader) {
                    h264Reader.close();
                }
            }
        };

        h264SendThread.start();

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

        if (null != customEncodedVideoTrack) {
            customEncodedVideoTrack.setEnabled(0);
            conn.getLocalUser().unpublishVideo(customEncodedVideoTrack);
            customEncodedVideoTrack.destroy();
            customEncodedVideoTrack = null;
        }

        if (null != customEncodedImageSender) {
            customEncodedImageSender.destroy();
            customEncodedImageSender = null;
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
