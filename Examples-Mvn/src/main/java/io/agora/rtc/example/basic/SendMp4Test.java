package io.agora.rtc.example.basic;

import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.Constants;
import io.agora.rtc.EncodedVideoFrameInfo;
import io.agora.rtc.ExternalVideoFrame;
import io.agora.rtc.IRtcConnObserver;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.RtcConnPublishConfig;
import io.agora.rtc.SenderOptions;
import io.agora.rtc.VideoDimensions;
import io.agora.rtc.VideoEncoderConfig;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.ffmpegutils.MediaDecode;
import io.agora.rtc.example.ffmpegutils.MediaDecodeUtils;
import io.agora.rtc.example.utils.Utils;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class SendMp4Test {
    private String appId;
    private String token;
    private final String DEFAULT_LOG_PATH = "logs/agora_logs/agorasdk.log";
    private final int DEFAULT_LOG_SIZE = 5 * 1024; // default log size is 5 mb
    private String channelId = "agaa";
    private String userId = "0";

    private String filePath = "test_data/test_avsync.mp4";

    private static AgoraService service;
    private AgoraRtcConn conn;

    private long testTime = 60 * 1000;

    private final AtomicBoolean connConnected = new AtomicBoolean(false);

    private final MediaDecodeUtils.DecodedMediaType decodedMediaType =
        MediaDecodeUtils.DecodedMediaType.PCM_H264;

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
        publishConfig.setIsPublishVideo(true);
        publishConfig.setAudioPublishType(Constants.AudioPublishType.PCM);
        if (decodedMediaType == MediaDecodeUtils.DecodedMediaType.PCM_H264) {
            publishConfig.setVideoPublishType(Constants.VideoPublishType.ENCODED_IMAGE);
            SenderOptions option = new SenderOptions();
            option.setCcMode(Constants.TCC_ENABLED);
            publishConfig.setSenderOptions(option);
        } else if (decodedMediaType == MediaDecodeUtils.DecodedMediaType.PCM_YUV) {
            publishConfig.setVideoPublishType(Constants.VideoPublishType.YUV);
        }
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
                connConnected.set(true);
                userId = connInfo.getLocalUserId();
            }

            @Override
            public void onUserJoined(AgoraRtcConn agoraRtcConn, String userId) {
                SampleLogger.log("onUserJoined userId:" + userId);
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

        MediaDecodeUtils mediaDecodeUtils = new MediaDecodeUtils();

        boolean initRet = mediaDecodeUtils.init(
            filePath, 50, -1, decodedMediaType, new MediaDecodeUtils.MediaDecodeCallback() {
                private ByteBuffer byteBuffer;
                private boolean isPublishAudio = false;
                private boolean isPublishVideo = false;

                @Override
                public void onAudioFrame(MediaDecode.MediaFrame frame) {
                    if (!isPublishAudio) {
                        conn.publishAudio();
                        isPublishAudio = true;
                    }
                    int ret = conn.pushAudioPcmData(frame.buffer, frame.sampleRate, frame.channels);
                    SampleLogger.log("SendPcmData frame.pts:" + frame.pts + " ret:" + ret
                        + " samples:" + frame.samples + " bytesPerSample:" + frame.bytesPerSample
                        + " channels:" + frame.channels + " sampleRate:" + frame.sampleRate
                        + " channelId:" + channelId + " userId:" + userId);
                }

                @Override
                public void onVideoFrame(MediaDecode.MediaFrame frame) {
                    SampleLogger.log("onVideoFrame frame:" + frame);
                    if (conn == null) {
                        return;
                    }
                    if (decodedMediaType == MediaDecodeUtils.DecodedMediaType.PCM_H264) {
                        // Publish video track
                        if (!isPublishVideo) {
                            conn.publishVideo();
                            // for wait video track ready
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            isPublishVideo = true;
                        }

                        EncodedVideoFrameInfo info = new EncodedVideoFrameInfo();
                        info.setFrameType(frame.isKeyFrame
                                ? Constants.VIDEO_FRAME_TYPE_KEY_FRAME
                                : Constants.VIDEO_FRAME_TYPE_DELTA_FRAME);
                        info.setWidth(frame.width);
                        info.setHeight(frame.height);
                        info.setCodecType(Constants.VIDEO_CODEC_H264);
                        info.setCaptureTimeMs(frame.pts);
                        info.setDecodeTimeMs(frame.pts);
                        info.setFramesPerSecond(frame.fps);
                        info.setRotation(0);
                        int ret = conn.pushVideoEncodedData(frame.buffer, info);
                        SampleLogger.log("SendEncodedVideoFrame frame.pts:" + frame.pts
                            + " ret:" + ret + " width:" + frame.width + " height:" + frame.height
                            + " fps:" + frame.fps + " isKeyFrame:" + frame.isKeyFrame
                            + " buffer:" + frame.buffer.length);

                    } else if (decodedMediaType == MediaDecodeUtils.DecodedMediaType.PCM_YUV) {
                        if (!isPublishVideo) {
                            VideoEncoderConfig config = new VideoEncoderConfig();
                            config.setCodecType(Constants.VIDEO_CODEC_H264);
                            config.setDimensions(new VideoDimensions(frame.width, frame.height));
                            config.setFrameRate(frame.fps);
                            conn.setVideoEncoderConfig(config);
                            // Publish video track
                            conn.publishVideo();
                            isPublishVideo = true;
                        }

                        ExternalVideoFrame externalVideoFrame = new ExternalVideoFrame();
                        if (null == byteBuffer) {
                            byteBuffer = ByteBuffer.allocateDirect(frame.buffer.length);
                        }
                        if (byteBuffer == null || byteBuffer.limit() < frame.buffer.length) {
                            return;
                        }
                        byteBuffer.put(frame.buffer);
                        byteBuffer.flip();
                        externalVideoFrame.setBuffer(byteBuffer);
                        externalVideoFrame.setHeight(frame.height);
                        externalVideoFrame.setStride(frame.stride);
                        externalVideoFrame.setRotation(0);
                        externalVideoFrame.setFormat(
                            Constants.EXTERNAL_VIDEO_FRAME_PIXEL_FORMAT_I420);
                        externalVideoFrame.setType(
                            Constants.EXTERNAL_VIDEO_FRAME_BUFFER_TYPE_RAW_DATA);
                        externalVideoFrame.setTimestamp(frame.pts);
                        int ret = conn.pushVideoFrame(externalVideoFrame);
                    }
                }
            });

        SampleLogger.log("send mp4 initRet:" + initRet);

        while (!connConnected.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (initRet) {
            mediaDecodeUtils.start();
        }

        try {
            Thread.sleep(testTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mediaDecodeUtils.stop();

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
