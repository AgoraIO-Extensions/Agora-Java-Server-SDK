package io.agora.rtc.example.basic;

import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.Constants;
import io.agora.rtc.EncodedVideoFrameInfo;
import io.agora.rtc.IRtcConnObserver;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.RtcConnPublishConfig;
import io.agora.rtc.SimulcastStreamConfig;
import io.agora.rtc.VideoDimensions;
import io.agora.rtc.example.common.FileSender;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.mediautils.H264Reader;
import io.agora.rtc.example.utils.Utils;
import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SendH264Test {
    static {
        System.loadLibrary("media_utils");
    }

    private String appId;
    private String token;
    private final String DEFAULT_LOG_PATH = "logs/agora_logs/agorasdk.log";
    private final int DEFAULT_LOG_SIZE = 5 * 1024; // default log size is 5 mb

    private static AgoraService service;

    private AgoraRtcConn conn;

    private String channelId = "agaa";
    private String userId = "0";
    private int width = 360;
    private int height = 640;
    private int fps = 25;
    private String streamType = "high";
    private boolean enableSimulcastStream = false;
    private String videoFile = "test_data/send_video.h264";
    private long testTime = 60 * 1000;

    private final ExecutorService singleExecutorService = Executors.newSingleThreadExecutor();
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
        publishConfig.setIsPublishAudio(false);
        publishConfig.setIsPublishVideo(true);
        publishConfig.setAudioPublishType(Constants.AudioPublishType.NO_PUBLISH);
        publishConfig.setVideoPublishType(Constants.VideoPublishType.ENCODED_IMAGE);
        publishConfig.getSenderOptions().setCcMode(Constants.TCC_ENABLED);
        conn = service.agoraRtcConnCreate(ccfg, publishConfig);
        if (conn == null) {
            SampleLogger.log("AgoraService.agoraRtcConnCreate fail\n");
            releaseAgoraService();
            return;
        }

        ret = conn.registerObserver(new IRtcConnObserver() {
            @Override
            public void onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason) {
                SampleLogger.log(
                    "onConnected channelId :" + connInfo.getChannelId() + " reason:" + reason);
                userId = connInfo.getLocalUserId();
            }

            @Override
            public void onUserJoined(AgoraRtcConn agoraRtcConn, String userId) {
                SampleLogger.log("onUserJoined userId:" + userId);
                pushH264Data();
            }

            @Override
            public void onUserLeft(AgoraRtcConn agoraRtcConn, String userId, int reason) {
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

        if (enableSimulcastStream) {
            SimulcastStreamConfig lowStreamConfig = new SimulcastStreamConfig();
            lowStreamConfig.setBitrate(65);
            lowStreamConfig.setFramerate(fps);
            VideoDimensions dimensions = new VideoDimensions(width, height);
            lowStreamConfig.setDimensions(dimensions);
            conn.enableSimulcastStream(1, lowStreamConfig);
        }

        // Publish video track
        ret = conn.publishVideo();
        SampleLogger.log("sendH264Task publishVideo ret:" + ret);

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
        System.exit(0);
    }

    private void pushH264Data() {
        testTaskExecutorService.execute(() -> {
            H264Reader h264Reader = new H264Reader(videoFile);
            FileSender h264SendThread = new FileSender(videoFile, 1000 / fps) {
                int lastFrameType = 0;
                int frameIndex = 0;

                @Override
                public void sendOneFrame(byte[] data, long timestamp) {
                    if (data == null) {
                        return;
                    }
                    if (conn == null) {
                        release();
                        return;
                    }
                    EncodedVideoFrameInfo info = new EncodedVideoFrameInfo();
                    info.setFrameType(lastFrameType);
                    info.setStreamType(streamType.equals("high") ? Constants.VIDEO_STREAM_HIGH
                                                                 : Constants.VIDEO_STREAM_LOW);
                    info.setWidth(width);
                    info.setHeight(height);
                    info.setCodecType(Constants.VIDEO_CODEC_H264);
                    info.setFramesPerSecond(fps);
                    info.setRotation(0);

                    conn.pushVideoEncodedData(data, info);
                    frameIndex++;

                    singleExecutorService.execute(() -> {
                        SampleLogger.log("send h264 frame data size:" + data.length
                            + " timestamp:" + timestamp + " frameIndex:" + frameIndex
                            + " from channelId:" + channelId + " userId:" + userId);
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
                public void release() {
                    super.release();
                    if (null != h264Reader) {
                        h264Reader.close();
                    }
                }
            };

            testTaskExecutorService.execute(h264SendThread);
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
        singleExecutorService.shutdown();
        testTaskExecutorService.shutdown();
        SampleLogger.log("releaseAgoraService");
    }
}
