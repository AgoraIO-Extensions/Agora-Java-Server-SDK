package io.agora.rtc.example.scenario;

import io.agora.rtc.AgoraAudioPcmDataSender;
import io.agora.rtc.AgoraLocalAudioTrack;
import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AgoraLocalVideoTrack;
import io.agora.rtc.AgoraMediaNodeFactory;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.AgoraVideoFrameSender;
import io.agora.rtc.DefaultLocalUserObserver;
import io.agora.rtc.DefaultRtcConnObserver;
import io.agora.rtc.ExternalVideoFrame;
import io.agora.rtc.AgoraVideoEncodedImageSender;
import io.agora.rtc.SenderOptions;
import io.agora.rtc.EncodedVideoFrameInfo;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.VideoDimensions;
import io.agora.rtc.VideoEncoderConfig;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.utils.Utils;
import io.agora.rtc.example.ffmpegutils.MediaDecode;
import io.agora.rtc.example.ffmpegutils.MediaDecodeUtils;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SendMp4Test {
    private static String appId;
    private static String token;
    private static String DEFAULT_LOG_PATH = "agora_logs/agorasdk.log";
    private static int DEFAULT_LOG_SIZE = 512 * 1024; // default log size is 512 kb
    private static String channelId = "agaa";
    private static String userId = "0";

    private final static String FILE_PATH = "test_data/test_avsync.mp4";

    private static AgoraService service;
    private static AgoraRtcConn conn;
    private static AgoraMediaNodeFactory mediaNodeFactory;

    private static AgoraAudioPcmDataSender audioFrameSender;
    private static AgoraLocalAudioTrack customAudioTrack;

    private static AgoraVideoFrameSender videoFrameSender;
    private static AgoraLocalVideoTrack customVideoTrack;

    private static AgoraVideoEncodedImageSender customEncodedImageSender;
    private static AgoraLocalVideoTrack customEncodedVideoTrack;

    private static CountDownLatch exitLatch;

    private static final ExecutorService testTaskExecutorService = Executors.newCachedThreadPool();

    private static MediaDecodeUtils.DecodedMediaType decodedMediaType = MediaDecodeUtils.DecodedMediaType.PCM_H264;

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
                SampleLogger.log(
                        "onConnected chennalId:" + connInfo.getChannelId() + " userId:" + connInfo.getLocalUserId());
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
                // TODO Auto-generated method stub
                SampleLogger
                        .log("onVideoPublishStateChanged channel:" + channel + " oldState:" + oldState + " newState:"
                                + newState + " userRole:" + agoraLocalUser.getUserRole());
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
        final String currentChannelId = connInfo.getChannelId();
        final String currentUserId = connInfo.getLocalUserId();

        MediaDecodeUtils mediaDecodeUtils = new MediaDecodeUtils();

        boolean initRet = mediaDecodeUtils.init(FILE_PATH, 50, -1, decodedMediaType,
                new MediaDecodeUtils.MediaDecodeCallback() {
                    private ByteBuffer byteBuffer;

                    @Override
                    public void onAudioFrame(MediaDecode.MediaFrame frame) {
                        if (audioFrameSender == null) {
                            audioFrameSender = mediaNodeFactory.createAudioPcmDataSender();
                            // Create audio track
                            customAudioTrack = service.createCustomAudioTrackPcm(audioFrameSender);
                            conn.getLocalUser().publishAudio(customAudioTrack);
                        }

                        int ret = audioFrameSender.send(frame.buffer, (int) (frame.pts),
                                frame.samples, frame.bytesPerSample,
                                frame.channels,
                                frame.sampleRate);
                        SampleLogger.log("SendPcmData frame.pts:" + frame.pts + " ret:" + ret + " samples:"
                                + frame.samples
                                + " bytesPerSample:" + frame.bytesPerSample + " channels:" + frame.channels
                                + " sampleRate:" + frame.sampleRate + " channelId:" + channelId + " userId:" + userId);
                    }

                    @Override
                    public void onVideoFrame(MediaDecode.MediaFrame frame) {
                        SampleLogger.log("onVideoFrame frame:" + frame);
                        if (decodedMediaType == MediaDecodeUtils.DecodedMediaType.PCM_H264) {
                            if (customEncodedImageSender == null) {
                                customEncodedImageSender = mediaNodeFactory.createVideoEncodedImageSender();
                                // Create video track
                                SenderOptions option = new SenderOptions();
                                option.setCcMode(Constants.TCC_ENABLED);
                                customEncodedVideoTrack = service
                                        .createCustomVideoTrackEncoded(customEncodedImageSender, option);
                                // Publish video track
                                int ret = conn.getLocalUser().publishVideo(customEncodedVideoTrack);
                                // for wait video track ready
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            EncodedVideoFrameInfo info = new EncodedVideoFrameInfo();
                            info.setFrameType(frame.isKeyFrame ? Constants.VIDEO_FRAME_TYPE_KEY_FRAME
                                    : Constants.VIDEO_FRAME_TYPE_DELTA_FRAME);
                            info.setWidth(frame.width);
                            info.setHeight(frame.height);
                            info.setCodecType(Constants.VIDEO_CODEC_H264);
                            info.setCaptureTimeMs(frame.pts);
                            info.setDecodeTimeMs(frame.pts);
                            info.setFramesPerSecond(frame.fps);
                            info.setRotation(0);
                            int ret = customEncodedImageSender.send(frame.buffer, frame.bufferSize, info);
                            SampleLogger.log("SendEncodedVideoFrame frame.pts:" + frame.pts + " ret:" + ret + " width:"
                                    + frame.width + " height:" + frame.height + " fps:" + frame.fps + " isKeyFrame:"
                                    + frame.isKeyFrame + " buffer:" + frame.buffer.length);

                        } else if (decodedMediaType == MediaDecodeUtils.DecodedMediaType.PCM_YUV) {
                            if (videoFrameSender == null) {
                                videoFrameSender = mediaNodeFactory.createVideoFrameSender();

                                customVideoTrack = service.createCustomVideoTrackFrame(videoFrameSender);
                                VideoEncoderConfig config = new VideoEncoderConfig();
                                config.setCodecType(Constants.VIDEO_CODEC_H264);
                                config.setDimensions(new VideoDimensions(frame.width, frame.height));
                                config.setFrameRate(frame.fps);
                                customVideoTrack.setVideoEncoderConfig(config);
                                customVideoTrack.setEnabled(1);
                                // Publish video track
                                conn.getLocalUser().publishVideo(customVideoTrack);
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
                            externalVideoFrame.setFormat(Constants.EXTERNAL_VIDEO_FRAME_PIXEL_FORMAT_I420);
                            externalVideoFrame.setType(Constants.EXTERNAL_VIDEO_FRAME_BUFFER_TYPE_RAW_DATA);
                            externalVideoFrame.setTimestamp(frame.pts);
                            int ret = videoFrameSender.send(externalVideoFrame);

                        }
                    }
                });

        SampleLogger.log("send mp4 initRet:" + initRet);
        if (initRet) {
            mediaDecodeUtils.start();
        }
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

        if (null != customEncodedImageSender) {
            customEncodedImageSender.destroy();
        }

        if (null != customEncodedVideoTrack) {
            conn.getLocalUser().unpublishVideo(customEncodedVideoTrack);
            customEncodedVideoTrack.destroy();
        }

        if (null != videoFrameSender) {
            videoFrameSender.destroy();
        }

        if (null != customVideoTrack) {
            conn.getLocalUser().unpublishVideo(customVideoTrack);
            customVideoTrack.destroy();
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
        audioFrameSender = null;
        customAudioTrack = null;
        customEncodedImageSender = null;
        customEncodedVideoTrack = null;
        videoFrameSender = null;
        customVideoTrack = null;

        conn = null;

        SampleLogger.log("Disconnected from Agora channel successfully");
    }
}
