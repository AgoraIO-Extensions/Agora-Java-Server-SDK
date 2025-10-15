package io.agora.rtc.example.basic;

import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.Constants;
import io.agora.rtc.ExternalVideoFrame;
import io.agora.rtc.IRtcConnObserver;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.RtcConnPublishConfig;
import io.agora.rtc.SimulcastStreamConfig;
import io.agora.rtc.VideoDimensions;
import io.agora.rtc.VideoEncoderConfig;
import io.agora.rtc.example.common.FileSender;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.utils.DirectBufferCleaner;
import io.agora.rtc.example.utils.Utils;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SendAv1Test {
    private String appId;
    private String token;
    private final String DEFAULT_LOG_PATH = "logs/agora_logs/agorasdk.log";
    private final int DEFAULT_LOG_SIZE = 5 * 1024; // default log size is 5 mb

    private static AgoraService service;

    private AgoraRtcConn conn;

    private String channelId = "agaa";
    private String userId = "0";
    private int width = 640;
    private int height = 360;
    private int fps = 15;
    private boolean enableAlpha = false;
    private boolean enableSimulcastStream = false;
    private String videoFile = "test_data/360p_I420.yuv";
    private long testTime = 60 * 1000;

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
        publishConfig.setVideoPublishType(Constants.VideoPublishType.YUV);
        conn = service.agoraRtcConnCreate(ccfg, publishConfig);
        if (conn == null) {
            SampleLogger.log("AgoraService.agoraRtcConnCreate fail\n");
            releaseAgoraService();
            return;
        }

        ret = conn.registerObserver(new IRtcConnObserver() {
            @Override
            public void onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason) {
                userId = connInfo.getLocalUserId();
            }

            @Override
            public void onUserJoined(AgoraRtcConn agoraRtcConn, String userId) {
                SampleLogger.log("onUserJoined userId:" + userId);
                pushAv1Data();
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

        VideoEncoderConfig VideoEncoderConfig = new VideoEncoderConfig();
        //Constants.VIDEO_CODEC_AV1
        VideoEncoderConfig.setCodecType(12);
        VideoEncoderConfig.setDimensions(new VideoDimensions(width, height));
        VideoEncoderConfig.setFrameRate(fps);
        VideoEncoderConfig.setEncodeAlpha(enableAlpha ? 1 : 0);
        conn.setVideoEncoderConfig(VideoEncoderConfig);

        if (enableSimulcastStream) {
            VideoDimensions lowDimensions = new VideoDimensions(width / 2, height / 2);
            SimulcastStreamConfig lowStreamConfig = new SimulcastStreamConfig();
            lowStreamConfig.setDimensions(lowDimensions);
            // lowStreamConfig.setBitrate(targetBitrate/2);
            ret = conn.enableSimulcastStream(1, lowStreamConfig);
            SampleLogger.log("sendAv1Task enableSimulcastStream ret:" + ret);
        }

        // Publish video track
        conn.publishVideo();

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

    private void pushAv1Data() {
        testTaskExecutorService.execute(() -> {
            int bufferLen = (int) (height * width * 1.5);
            byte[] buffer = new byte[bufferLen];

            FileSender av1Sender = new FileSender(videoFile, 1000 / fps) {
                private int frameIndex = 0;
                private ByteBuffer byteBuffer;
                private ByteBuffer matedataByteBuffer;
                private ByteBuffer alphaByteBuffer;

                @Override
                public void sendOneFrame(byte[] data, long timestamp) {
                    if (data == null) {
                        return;
                    }
                    if (conn == null) {
                        release();
                        return;
                    }

                    ExternalVideoFrame externalVideoFrame = new ExternalVideoFrame();
                    externalVideoFrame.setHeight(height);
                    if (null == byteBuffer) {
                        byteBuffer = ByteBuffer.allocateDirect(data.length);
                    }
                    if (byteBuffer == null || byteBuffer.limit() < data.length) {
                        return;
                    }
                    byteBuffer.put(data);
                    byteBuffer.flip();

                    externalVideoFrame.setBuffer(byteBuffer);
                    externalVideoFrame.setRotation(0);
                    externalVideoFrame.setFormat(Constants.EXTERNAL_VIDEO_FRAME_PIXEL_FORMAT_I420);
                    externalVideoFrame.setStride(width);
                    externalVideoFrame.setType(Constants.EXTERNAL_VIDEO_FRAME_BUFFER_TYPE_RAW_DATA);

                    String testMetaData = "testMetaData";
                    if (null == matedataByteBuffer) {
                        matedataByteBuffer = ByteBuffer.allocateDirect(testMetaData.getBytes().length);
                    }
                    if (matedataByteBuffer == null
                            || matedataByteBuffer.limit() < testMetaData.getBytes().length) {
                        return;
                    }
                    matedataByteBuffer.put(testMetaData.getBytes());
                    matedataByteBuffer.flip();
                    externalVideoFrame.setMetadataBuffer(matedataByteBuffer);

                    if (enableAlpha) {
                        if (null == alphaByteBuffer) {
                            alphaByteBuffer = ByteBuffer.allocateDirect(data.length);
                        }
                        if (alphaByteBuffer == null || alphaByteBuffer.limit() < data.length) {
                            return;
                        }
                        alphaByteBuffer.put(data);
                        alphaByteBuffer.flip();
                        externalVideoFrame.setAlphaBuffer(alphaByteBuffer);
                        externalVideoFrame.setFillAlphaBuffer(1);
                    }

                    int ret = conn.pushVideoFrame(externalVideoFrame);
                    frameIndex++;

                    SampleLogger.log("send av1 frame data size:" + data.length + " ret:" + ret
                            + " timestamp:" + timestamp + " frameIndex:" + frameIndex
                            + " from channelId:" + channelId + " userId:" + userId);
                }

                @Override
                public byte[] readOneFrame(FileInputStream fos) {
                    if (fos == null) {
                        return null;
                    }
                    try {
                        int size = fos.read(buffer, 0, bufferLen);
                        if (size < 0) {
                            reset();
                            frameIndex = 0;
                            return null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return buffer;
                }

                @Override
                public void release() {
                    super.release();
                    DirectBufferCleaner.release(byteBuffer);
                    DirectBufferCleaner.release(matedataByteBuffer);
                    DirectBufferCleaner.release(alphaByteBuffer);
                }
            };

            testTaskExecutorService.execute(av1Sender);
        });
    }

    private void releaseConn() {
        SampleLogger.log("releaseConn for channelId:" + channelId + " userId:" + userId);
        if (conn == null) {
            return;
        }

        testTaskExecutorService.shutdown();

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
        SampleLogger.log("releaseAgoraService");
    }
}
