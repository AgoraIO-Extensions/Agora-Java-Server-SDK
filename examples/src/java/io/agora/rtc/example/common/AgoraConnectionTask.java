package io.agora.rtc.example.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.agora.rtc.AgoraAudioEncodedFrameSender;
import io.agora.rtc.AgoraAudioPcmDataSender;
import io.agora.rtc.AgoraAudioVadConfigV2;
import io.agora.rtc.AgoraLocalAudioTrack;
import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AgoraLocalVideoTrack;
import io.agora.rtc.AgoraMediaNodeFactory;
import io.agora.rtc.AgoraParameter;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraVideoEncodedFrameObserver;
import io.agora.rtc.AgoraVideoEncodedImageSender;
import io.agora.rtc.AgoraVideoFrameObserver2;
import io.agora.rtc.AgoraVideoFrameSender;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.Constants;
import io.agora.rtc.DefaultRtcConnObserver;
import io.agora.rtc.DownlinkNetworkInfo;
import io.agora.rtc.EncodedAudioFrameInfo;
import io.agora.rtc.EncodedAudioFrameReceiverInfo;
import io.agora.rtc.EncodedVideoFrameInfo;
import io.agora.rtc.EncryptionConfig;
import io.agora.rtc.ExternalVideoFrame;
import io.agora.rtc.IAudioEncodedFrameObserver;
import io.agora.rtc.IAudioFrameObserver;
import io.agora.rtc.INetworkObserver;
import io.agora.rtc.Out;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.SenderOptions;
import io.agora.rtc.SimulcastStreamConfig;
import io.agora.rtc.UplinkNetworkInfo;
import io.agora.rtc.VadProcessResult;
import io.agora.rtc.VideoDimensions;
import io.agora.rtc.VideoEncoderConfig;
import io.agora.rtc.VideoFrame;
import io.agora.rtc.VideoSubscriptionOptions;
import io.agora.rtc.example.ffmpegutils.MediaDecode;
import io.agora.rtc.example.ffmpegutils.MediaDecodeUtils;
import io.agora.rtc.example.mediautils.AacReader;
import io.agora.rtc.example.mediautils.H264Reader;
import io.agora.rtc.example.mediautils.OpusReader;
import io.agora.rtc.example.mediautils.Vp8Reader;
import io.agora.rtc.example.utils.DirectBufferCleaner;
import io.agora.rtc.example.utils.Utils;
import io.agora.rtc.utils.AudioConsumerUtils;

public class AgoraConnectionTask {
    static {
        System.loadLibrary("media_utils");
    }

    private final AgoraService service;
    // in seconds
    private final long testTime;
    private final long testStartTime;

    private TaskCallback callback;
    private AgoraRtcConn conn;
    private AgoraMediaNodeFactory mediaNodeFactory;

    private AgoraAudioPcmDataSender audioFrameSender;
    private AgoraLocalAudioTrack customAudioTrack;

    private AgoraVideoEncodedImageSender customEncodedImageSender;
    private AgoraLocalVideoTrack customEncodedVideoTrack;

    private AgoraVideoFrameSender videoFrameSender;
    private AgoraLocalVideoTrack customVideoTrack;

    private AgoraAudioEncodedFrameSender audioEncodedFrameSender;
    private AgoraLocalAudioTrack customEncodedAudioTrack;

    private SampleLocalUserObserver sampleLocalUserObserver;

    private IAudioFrameObserver audioFrameObserver;
    private IAudioEncodedFrameObserver audioEncodedFrameObserver;
    private AgoraVideoFrameObserver2 videoFrameObserver;
    private AgoraVideoEncodedFrameObserver videoEncodedFrameObserver;

    private String currentChannelId;
    private String currentUserId;
    private final ExecutorService singleExecutorService;
    private final ThreadPoolExecutor testTaskExecutorService;
    private final ThreadPoolExecutor logExecutorService;
    private CountDownLatch taskFinishLatch;
    private CountDownLatch connDisconnectedLatch;
    private AtomicBoolean taskStarted = new AtomicBoolean(false);

    public interface TaskCallback {
        default void onConnected(String userId) {

        }

        default void onUserJoined(String userId) {

        }

        default void onUserLeft(String userId) {

        }

        default void onStreamMessage(String userId, int streamId, String data, long length) {

        }

        default void onTestFinished() {

        }
    }

    public AgoraConnectionTask(AgoraService service, long testTime) {
        this.service = service;
        this.testTime = testTime;
        this.testStartTime = System.currentTimeMillis();
        this.singleExecutorService = Executors.newSingleThreadExecutor();
        this.testTaskExecutorService = new ThreadPoolExecutor(
                0,
                Integer.MAX_VALUE,
                1L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>());
        this.logExecutorService = new ThreadPoolExecutor(
                0,
                Integer.MAX_VALUE,
                1L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>());
    }

    public void setCallback(TaskCallback callback) {
        this.callback = callback;
    }

    public AgoraRtcConn getConn() {
        return conn;
    }

    public void createConnection(RtcConnConfig ccfg, String channelId, String userId) {
        SampleLogger
                .log("createConnection token:" + ArgsConfig.token + " channelId:" + channelId + " userId:"
                        + userId
                        + " enableEncryptionMode:"
                        + ArgsConfig.enableEncryptionMode + " encryptionMode:" + ArgsConfig.encryptionMode
                        + " encryptionKey:"
                        + ArgsConfig.encryptionKey + " enableCloudProxy:" + ArgsConfig.enableCloudProxy);
        if (null == service) {
            SampleLogger.log("createAndInitAgoraService fail");
            return;
        }
        // this.conn = conn;
        conn = service.agoraRtcConnCreate(ccfg);
        if (conn == null) {
            SampleLogger.log("AgoraService.agoraRtcConnCreate fail");
            return;
        }
        this.currentChannelId = channelId;
        this.currentUserId = userId;

        int ret = conn.registerObserver(new DefaultRtcConnObserver() {
            @Override
            public void onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason) {
                super.onConnected(agoraRtcConn, connInfo, reason);
                currentUserId = connInfo.getLocalUserId();
                if (null != callback) {
                    callback.onConnected(currentUserId);
                }
            }

            @Override
            public void onDisconnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason) {
                super.onDisconnected(agoraRtcConn, connInfo, reason);
                if (null != connDisconnectedLatch) {
                    connDisconnectedLatch.countDown();
                }
            }

            @Override
            public void onUserJoined(AgoraRtcConn agoraRtcConn, String userId) {
                super.onUserJoined(agoraRtcConn, userId);
                SampleLogger.log("onUserJoined channelId:" + currentChannelId + " userId:" + userId);
                if (null != callback) {
                    callback.onUserJoined(userId);
                }

            }

            @Override
            public void onUserLeft(AgoraRtcConn agoraRtcConn, String userId, int reason) {
                super.onUserLeft(agoraRtcConn, userId, reason);
                SampleLogger.log("onUserLeft userId:" + userId + " reason:" + reason);
                if (testTime == 0) {
                    if (null != taskFinishLatch) {
                        taskFinishLatch.countDown();
                    }
                }

                if (null != callback) {
                    callback.onUserLeft(userId);
                }
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

        ret = conn.registerNetworkObserver(new INetworkObserver() {
            @Override
            public void onUplinkNetworkInfoUpdated(AgoraRtcConn agoraRtcConn, UplinkNetworkInfo info) {
                if (ArgsConfig.isStressTest == 0) {
                    logExecutorService.execute(() -> {
                        SampleLogger.log("onUplinkNetworkInfoUpdated info:" + info);
                    });
                }
            }

            @Override
            public void onDownlinkNetworkInfoUpdated(AgoraRtcConn agoraRtcConn, DownlinkNetworkInfo info) {
                if (ArgsConfig.isStressTest == 0) {
                    logExecutorService.execute(() -> {
                        SampleLogger.log("onDownlinkNetworkInfoUpdated info:" + info);
                    });
                }
            }
        });
        SampleLogger.log("registerNetworkObserver ret:" + ret);

        if (ArgsConfig.enableEncryptionMode == 1 && !Utils.isNullOrEmpty(ArgsConfig.encryptionKey)) {
            EncryptionConfig encryptionConfig = new EncryptionConfig();
            encryptionConfig.setEncryptionMode(ArgsConfig.encryptionMode);
            encryptionConfig.setEncryptionKey(ArgsConfig.encryptionKey);
            ret = conn.enableEncryption(ArgsConfig.enableEncryptionMode, encryptionConfig);
            if (ret < 0) {
                SampleLogger.log("Failed to enable encryption ret:" + ret);
                return;
            }
            SampleLogger.log("Enable encryption successfully!");
        }

        if (ArgsConfig.enableCloudProxy == 1) {
            AgoraParameter agoraParameter = conn.getAgoraParameter();
            ret = agoraParameter.setBool("rtc.enable_proxy", true);
            SampleLogger.log("setBool rtc.enable_proxy ret:" + ret);
        }
        // conn.getLocalUser().setAudioVolumeIndicationParameters(50, 3, true);

        ret = conn.connect(ArgsConfig.token, channelId, userId);
        SampleLogger.log("Connecting to Agora channel " + channelId + " with userId " + userId + " ret:" + ret);

        // Register local user observer
        if (null == sampleLocalUserObserver) {
            sampleLocalUserObserver = new SampleLocalUserObserver(conn.getLocalUser());
        }
        conn.getLocalUser().registerObserver(sampleLocalUserObserver);

        mediaNodeFactory = service.createMediaNodeFactory();
    }

    public void releaseConn() {
        synchronized (service) {
            taskStarted.set(false);
            SampleLogger.log("releaseConn for channelId:" + currentChannelId + " userId:" + currentUserId);
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

            if (null != audioEncodedFrameSender) {
                audioEncodedFrameSender.destroy();
            }

            if (null != customEncodedAudioTrack) {
                conn.getLocalUser().unpublishAudio(customEncodedAudioTrack);
                customEncodedAudioTrack.destroy();
            }

            if (null != audioFrameObserver) {
                conn.getLocalUser().unregisterAudioFrameObserver();
                audioFrameObserver = null;
            }

            if (null != audioEncodedFrameObserver) {
                conn.getLocalUser().unregisterAudioEncodedFrameObserver(audioEncodedFrameObserver);
                audioEncodedFrameObserver = null;
            }

            if (null != videoFrameObserver) {
                conn.getLocalUser().unregisterVideoFrameObserver(videoFrameObserver);
                videoFrameObserver.destroy();
                videoFrameObserver = null;
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

            if (null == connDisconnectedLatch) {
                connDisconnectedLatch = new CountDownLatch(1);
            }
            try {
                connDisconnectedLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
            audioEncodedFrameSender = null;
            customEncodedAudioTrack = null;
            sampleLocalUserObserver = null;

            conn = null;

            taskFinishLatch = null;

            singleExecutorService.shutdown();
            testTaskExecutorService.shutdown();
            logExecutorService.shutdown();

            SampleLogger.log("destroy conn for channelId:" + currentChannelId + " userId:" + currentUserId);

            if (null != callback) {
                callback.onTestFinished();
            }
        }
    }

    public void sendPcmTask(String filePath, int interval, int numOfChannels, int sampleRate,
            boolean waitRelease) {
        boolean enableAudioCache = ArgsConfig.enableAudioCache == 1;
        boolean enableSendAudioMetaData = ArgsConfig.enableSendAudioMetaData == 1;
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        SampleLogger
                .log("sendPcmTask filePath:" + filePath + " interval:" + interval + " numOfChannels:" + numOfChannels
                        + " sampleRate:" + sampleRate + " enableAudioCache:" + enableAudioCache
                        + " enableSendAudioMetaData:"
                        + enableSendAudioMetaData);
        audioFrameSender = mediaNodeFactory.createAudioPcmDataSender();
        // Create audio track
        customAudioTrack = service.createCustomAudioTrackPcm(audioFrameSender);
        conn.getLocalUser().publishAudio(customAudioTrack);

        int bufferSize = numOfChannels * (sampleRate / 1000) * interval * 2;
        byte[] buffer = new byte[bufferSize];

        boolean isLoopSend = testTime > 0;
        FileSender pcmSendThread = new FileSender(filePath, interval) {
            private AudioConsumerUtils audioConsumerUtils = null;
            private boolean canLog = true;

            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                if (null != audioFrameSender && taskStarted.get()) {
                    int consumeFrameCount = audioConsumerUtils.consume();
                    if (consumeFrameCount == 0) {
                        if (null != taskFinishLatch) {
                            taskFinishLatch.countDown();
                        }
                        return;
                    } else if (consumeFrameCount < 0) {
                        return;
                    }
                    if (canLog) {
                        logExecutorService.execute(() -> {
                            SampleLogger.log("send pcm " + consumeFrameCount + " frame data to channelId:"
                                    + currentChannelId + " from userId:" + currentUserId);
                        });
                        if (isStressTest) {
                            canLog = false;
                        }
                    }

                    if (enableSendAudioMetaData) {
                        String audioMetaData = "testSendAudioMetaData " + timestamp;
                        int ret = conn.getLocalUser()
                                .sendAudioMetaData(audioMetaData.getBytes());
                        if (canLog) {
                            logExecutorService.execute(() -> {
                                SampleLogger.log("sendAudioMetaData: " + audioMetaData + " ret:" + ret);
                            });
                            if (isStressTest) {
                                canLog = false;
                            }
                        }
                    }
                }
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                if (fos != null) {
                    try {
                        int size = fos.read(buffer, 0, bufferSize);
                        if (size < 0) {
                            if (isLoopSend) {
                                reset();
                            }
                            return null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (null == audioConsumerUtils) {
                    audioConsumerUtils = new AudioConsumerUtils(audioFrameSender, numOfChannels,
                            sampleRate);
                }
                audioConsumerUtils.pushPcmData(buffer);
                return buffer;
            }

            @Override
            public void release(boolean withJoin) {
                super.release(withJoin);
                if (null != audioConsumerUtils) {
                    audioConsumerUtils.release();
                    audioConsumerUtils = null;
                }
            }

        };

        pcmSendThread.start();

        if (!taskStarted.get()) {
            taskStarted.set(true);
        }

        SampleLogger.log("sendPcmTask start");
        if (waitRelease) {
            handleWaitRelease();
            pcmSendThread.release(false);
            SampleLogger.log("sendPcmTask end");
            releaseConn();
        }
    }

    public void sendAacTask(String filePath, int interval, int numOfChannels, int sampleRate, boolean waitRelease) {
        SampleLogger
                .log("sendAacTask filePath:" + filePath + " interval:" + interval + " numOfChannels:" + numOfChannels
                        + " sampleRate:" + sampleRate);
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        // Create audio track
        audioEncodedFrameSender = mediaNodeFactory.createAudioEncodedFrameSender();
        customEncodedAudioTrack = service.createCustomAudioTrackEncoded(audioEncodedFrameSender,
                Constants.TMixMode.MIX_DISABLED.value);
        conn.getLocalUser().publishAudio(customEncodedAudioTrack);

        AacReader aacReader = new AacReader(filePath);
        EncodedAudioFrameInfo encodedInfo = new EncodedAudioFrameInfo();
        boolean isLoopSend = testTime > 0;
        FileSender aacSendThread = new FileSender(filePath, interval) {
            private boolean canLog = true;

            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                if (data == null) {
                    return;
                }
                if (!taskStarted.get()) {
                    return;
                }
                int ret = audioEncodedFrameSender.send(data, data.length, encodedInfo);
                if (canLog) {
                    logExecutorService.execute(() -> {
                        SampleLogger.log("send aac frame data size:" + data.length + " timestamp:"
                                + timestamp + " encodedInfo:" + encodedInfo
                                + " to channelId:"
                                + currentChannelId + " from userId:" + currentUserId + " ret:" + ret + " testStartTime:"
                                + Utils.formatTimestamp(testStartTime)
                                + " testTime:" + testTime);
                    });
                    if (isStressTest) {
                        canLog = false;
                    }
                }
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                AacReader.AacFrame aacFrame = aacReader.getAudioFrame(interval);
                if (aacFrame == null) {
                    if (isLoopSend) {
                        aacReader.reset();
                        reset();
                    } else {
                        if (null != taskFinishLatch) {
                            taskFinishLatch.countDown();
                        }
                    }
                    return null;
                }
                encodedInfo.setCodec(aacFrame.codec);
                encodedInfo.setNumberOfChannels(aacFrame.numberOfChannels);
                encodedInfo.setSampleRateHz(aacFrame.sampleRate);
                encodedInfo.setSamplesPerChannel(aacFrame.samplesPerChannel);
                return aacFrame.buffer;
            }

            @Override
            public void release(boolean withJoin) {
                super.release(withJoin);
                if (null != aacReader) {
                    aacReader.close();
                }
            }
        };

        aacSendThread.start();
        if (!taskStarted.get()) {
            taskStarted.set(true);
        }
        SampleLogger.log("sendAacTask start");

        if (waitRelease) {
            handleWaitRelease();
            aacSendThread.release(false);
            SampleLogger.log("sendAacTask end");
            releaseConn();
        }
    }

    public void sendOpusTask(String filePath, int interval, boolean waitRelease) {
        SampleLogger
                .log("sendOpusTask filePath:" + filePath + " interval:" + interval);
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        // Create audio track
        audioEncodedFrameSender = mediaNodeFactory.createAudioEncodedFrameSender();
        customEncodedAudioTrack = service.createCustomAudioTrackEncoded(audioEncodedFrameSender,
                Constants.TMixMode.MIX_DISABLED.value);
        conn.getLocalUser().publishAudio(customEncodedAudioTrack);

        OpusReader opusReader = new OpusReader(filePath);
        EncodedAudioFrameInfo encodedInfo = new EncodedAudioFrameInfo();

        boolean isLoopSend = testTime > 0;
        FileSender opusSendThread = new FileSender(filePath, interval) {
            private boolean canLog = true;

            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                if (data == null) {
                    return;
                }

                if (null == data || data.length == 0) {
                    return;
                }

                if (!taskStarted.get()) {
                    return;
                }

                int ret = audioEncodedFrameSender.send(data, data.length, encodedInfo);
                if (canLog) {
                    logExecutorService.execute(() -> {
                        SampleLogger.log("send opus frame data size:" + data.length + " timestamp:"
                                + timestamp + " encodedInfo:" + encodedInfo
                                + " to channelId:"
                                + currentChannelId + " from userId:" + currentUserId + " ret:" + ret + " testStartTime:"
                                + Utils.formatTimestamp(testStartTime)
                                + " testTime:" + testTime);
                    });
                    if (isStressTest) {
                        canLog = false;
                    }
                }
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                io.agora.rtc.example.mediautils.AudioFrame opusFrame = opusReader.getAudioFrame(interval);
                if (opusFrame == null) {
                    if (isLoopSend) {
                        opusReader.reset();
                        reset();
                    } else {
                        if (null != taskFinishLatch) {
                            taskFinishLatch.countDown();
                        }
                    }
                    return null;
                }
                encodedInfo.setCodec(opusFrame.codec);
                encodedInfo.setNumberOfChannels(opusFrame.numberOfChannels);
                encodedInfo.setSampleRateHz(opusFrame.sampleRate);
                encodedInfo.setSamplesPerChannel(opusFrame.samplesPerChannel);
                return opusFrame.buffer;
            }

            @Override
            public void release(boolean withJoin) {
                super.release(withJoin);
                if (null != opusReader) {
                    opusReader.close();
                }
            }
        };

        opusSendThread.start();
        if (!taskStarted.get()) {
            taskStarted.set(true);
        }
        SampleLogger.log("sendOpusTask start");

        if (waitRelease) {
            handleWaitRelease();
            opusSendThread.release(false);
            SampleLogger.log("sendOpusTask end");
            releaseConn();
        }
    }

    public void sendYuvTask(String filePath, int interval, int height, int width, int fps, int streamType,
            boolean waitRelease) {
        boolean enableSimulcastStream = ArgsConfig.enableSimulcastStream == 1;
        boolean enableAlpha = ArgsConfig.enableAlpha == 1;
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        SampleLogger.log("sendYuvTask filePath:" + filePath + " interval:" + interval + " height:" + height + " width:"
                + width + " streamType:" + streamType + " enableSimulcastStream:" + enableSimulcastStream
                + " enableAlpha:" + enableAlpha);

        if (videoFrameSender == null) {
            videoFrameSender = mediaNodeFactory.createVideoFrameSender();

            // Create video track
            customVideoTrack = service.createCustomVideoTrackFrame(videoFrameSender);
            VideoEncoderConfig config = new VideoEncoderConfig();
            config.setCodecType(Constants.VIDEO_CODEC_H264);
            config.setDimensions(new VideoDimensions(width, height));
            config.setFrameRate(fps);
            config.setEncodeAlpha(enableAlpha ? 1 : 0);
            customVideoTrack.setVideoEncoderConfig(config);
            if (enableSimulcastStream) {
                VideoDimensions lowDimensions = new VideoDimensions(width / 2, height / 2);
                SimulcastStreamConfig lowStreamConfig = new SimulcastStreamConfig();
                lowStreamConfig.setDimensions(lowDimensions);
                // lowStreamConfig.setBitrate(targetBitrate/2);
                int ret = customVideoTrack.enableSimulcastStream(1, lowStreamConfig);
                SampleLogger.log("sendYuvTask enableSimulcastStream ret:" + ret);
            }

            customVideoTrack.setEnabled(1);
            // Publish video track
            conn.getLocalUser().publishVideo(customVideoTrack);
        }

        int bufferLen = (int) (height * width * 1.5);
        byte[] buffer = new byte[bufferLen];

        boolean isLoopSend = testTime > 0;
        FileSender yuvSender = new FileSender(filePath, interval) {
            private int frameIndex = 0;
            private ByteBuffer byteBuffer;
            private ByteBuffer matedataByteBuffer;
            private ByteBuffer alphaByteBuffer;
            private boolean canLog = true;

            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                if (data == null) {
                    return;
                }

                ExternalVideoFrame externalVideoFrame = new ExternalVideoFrame();
                externalVideoFrame.setHeight(height);
                if (null == byteBuffer) {
                    byteBuffer = ByteBuffer.allocateDirect(data.length);
                }
                byteBuffer.put(data);
                byteBuffer.flip();

                externalVideoFrame.setBuffer(byteBuffer);
                externalVideoFrame.setRotation(0);
                externalVideoFrame.setFormat(Constants.EXTERNAL_VIDEO_FRAME_PIXEL_FORMAT_I420);
                externalVideoFrame.setStride(width);
                externalVideoFrame.setType(Constants.EXTERNAL_VIDEO_FRAME_BUFFER_TYPE_RAW_DATA);
                externalVideoFrame.setTimestamp(timestamp);

                String testMetaData = "testMetaData";
                if (null == matedataByteBuffer) {
                    matedataByteBuffer = ByteBuffer.allocateDirect(testMetaData.getBytes().length);
                }
                matedataByteBuffer.put(testMetaData.getBytes());
                matedataByteBuffer.flip();
                externalVideoFrame.setMetadataBuffer(matedataByteBuffer);

                if (enableAlpha) {
                    if (null == alphaByteBuffer) {
                        alphaByteBuffer = ByteBuffer.allocateDirect(data.length);
                    }
                    alphaByteBuffer.put(data);
                    alphaByteBuffer.flip();
                    externalVideoFrame.setAlphaBuffer(alphaByteBuffer);
                    externalVideoFrame.setFillAlphaBuffer(1);
                }

                if (!taskStarted.get()) {
                    return;
                }
                int ret = videoFrameSender.send(externalVideoFrame);
                frameIndex++;

                if (canLog) {
                    logExecutorService.execute(() -> {
                        SampleLogger.log("send yuv frame data size:" + data.length + "  ret:" + ret +
                                " timestamp:" + timestamp + " frameIndex:" + frameIndex + " testStartTime:"
                                + Utils.formatTimestamp(testStartTime)
                                + " testTime:" +
                                testTime + " from channelId:" + currentChannelId + " userId:" + currentUserId);
                    });
                    if (isStressTest) {
                        canLog = false;
                    }
                }
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                if (fos == null) {
                    return null;
                }
                try {
                    int size = fos.read(buffer, 0, bufferLen);
                    if (size < 0) {
                        if (isLoopSend) {
                            reset();
                            frameIndex = 0;
                        } else {
                            if (null != taskFinishLatch) {
                                taskFinishLatch.countDown();
                            }
                        }
                        return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return buffer;
            }

            @Override
            public void release(boolean withJoin) {
                super.release(withJoin);
                DirectBufferCleaner.release(byteBuffer);
                DirectBufferCleaner.release(matedataByteBuffer);
                DirectBufferCleaner.release(alphaByteBuffer);
            }

        };

        yuvSender.start();
        if (!taskStarted.get()) {
            taskStarted.set(true);
        }
        SampleLogger.log("sendYuvTask start");

        if (waitRelease) {
            handleWaitRelease();
            yuvSender.release(false);
            SampleLogger.log("sendYuvTask end");
            releaseConn();
        }
    }

    public void sendH264Task(String filePath, int interval, int height, int width, int streamType,
            boolean waitRelease) {
        boolean enableSimulcastStream = ArgsConfig.enableSimulcastStream == 1;
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        SampleLogger.log("sendH264Task filePath:" + filePath + " interval:" + interval + " height:" + height + " width:"
                + width + " streamType:" + streamType + " enableSimulcastStream:" + enableSimulcastStream);
        int fps = 1000 / interval;
        if (customEncodedImageSender == null) {
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
        }

        H264Reader h264Reader = new H264Reader(filePath);

        boolean isLoopSend = testTime > 0;
        FileSender h264SendThread = new FileSender(filePath, interval) {
            int lastFrameType = 0;
            int frameIndex = 0;
            private boolean canLog = true;

            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                if (data == null) {
                    return;
                }
                EncodedVideoFrameInfo info = new EncodedVideoFrameInfo();
                long currentTime = timestamp;
                info.setFrameType(lastFrameType);
                info.setStreamType(streamType);
                info.setWidth(width);
                info.setHeight(height);
                info.setCodecType(Constants.VIDEO_CODEC_H264);
                info.setCaptureTimeMs(currentTime);
                info.setDecodeTimeMs(currentTime);
                info.setFramesPerSecond(fps);
                info.setRotation(0);
                if (!taskStarted.get()) {
                    return;
                }
                customEncodedImageSender.send(data, data.length, info);
                frameIndex++;
                if (canLog) {
                    logExecutorService.execute(() -> {
                        SampleLogger.log("send h264 frame data size:" + data.length +
                                " timestamp:" + timestamp + " frameIndex:" + frameIndex + " testStartTime:"
                                + Utils.formatTimestamp(testStartTime)
                                + " testTime:" +
                                testTime + " from channelId:" + currentChannelId + " userId:" + currentUserId);
                    });
                    if (isStressTest) {
                        canLog = false;
                    }
                }
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                H264Reader.H264Frame frame = h264Reader.readNextFrame();
                if (frame == null) {
                    if (isLoopSend) {
                        h264Reader.reset();
                        reset();
                    } else {
                        if (null != taskFinishLatch) {
                            taskFinishLatch.countDown();
                        }
                    }
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
        if (!taskStarted.get()) {
            taskStarted.set(true);
        }
        SampleLogger.log("sendH264Task start");

        if (waitRelease) {
            handleWaitRelease();
            h264SendThread.release(false);
            SampleLogger.log("sendH264Task end");
            releaseConn();
        }
    }

    public void sendRgbaTask(String filePath, int interval, int height, int width, int fps, boolean waitRelease) {
        SampleLogger.log("sendRgbaTask filePath:" + filePath + " interval:" + interval + " height:" + height + " width:"
                + width);
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        videoFrameSender = mediaNodeFactory.createVideoFrameSender();

        // Create video track
        customVideoTrack = service.createCustomVideoTrackFrame(videoFrameSender);
        VideoEncoderConfig config = new VideoEncoderConfig();
        config.setCodecType(Constants.VIDEO_CODEC_H264);
        config.setDimensions(new VideoDimensions(width, height));
        config.setFrameRate(fps);
        customVideoTrack.setVideoEncoderConfig(config);
        customVideoTrack.setEnabled(1);
        // Publish video track
        conn.getLocalUser().publishVideo(customVideoTrack);

        boolean isLoopSend = testTime > 0;
        FileSender rgbaSender = new FileSender(filePath, interval) {
            // For RGBA, each pixel takes 4 bytes.
            int bufferLen = height * width * 4;
            byte[] buffer = new byte[bufferLen];

            private int frameIndex = 0;
            ByteBuffer byteBuffer;
            ByteBuffer alphaBuffer;
            byte[] alphadata;
            private boolean canLog = true;

            public byte[] extractAlphaChannel(byte[] rgbaData, int width, int height) {
                int pixelCount = width * height;
                byte[] alphaData = new byte[pixelCount]; // Alpha 数据,每个像素1字节

                for (int i = 0; i < pixelCount; i++) {
                    alphaData[i] = rgbaData[4 * i + 3]; // RGBA中的A分量
                }

                return alphaData;
            }

            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                if (data == null) {
                    return;
                }

                // 通过ByteBuffer直接分配内存,确保其为DirectByteBuffer,满足Agora SDK要求
                if (byteBuffer == null) {
                    byteBuffer = ByteBuffer.allocateDirect(data.length);
                }
                byteBuffer.put(data);
                byteBuffer.flip(); // 重置position,以便Agora SDK能从头开始读取数据

                // 提取Alpha通道数据
                byte[] alphaData = extractAlphaChannel(data, width, height);
                // 创建一个新的DirectByteBuffer来存储Alpha通道数据
                if (alphaBuffer == null) {
                    alphaBuffer = ByteBuffer.allocateDirect(alphaData.length);
                }
                alphaBuffer.put(alphaData);
                alphaBuffer.flip(); // 重置position,以便从头开始读取

                ExternalVideoFrame externalVideoFrame = new ExternalVideoFrame();
                externalVideoFrame.setType(Constants.EXTERNAL_VIDEO_FRAME_BUFFER_TYPE_RAW_DATA);
                externalVideoFrame.setFormat(Constants.EXTERNAL_VIDEO_FRAME_PIXEL_FORMAT_RGBA);
                externalVideoFrame.setStride(width);
                externalVideoFrame.setHeight(height);
                externalVideoFrame.setBuffer(byteBuffer);
                externalVideoFrame.setAlphaBuffer(alphaBuffer);
                externalVideoFrame.setTimestamp(timestamp);
                externalVideoFrame.setRotation(0);
                // ColorSpace colorSpace = new ColorSpace();
                // colorSpace.setPrimaries(1);
                // colorSpace.setTransfer(1);
                // colorSpace.setMatrix(5);
                // colorSpace.setRange(1);
                // externalVideoFrame.setColorSpace(colorSpace);

                if (!taskStarted.get()) {
                    return;
                }
                videoFrameSender.send(externalVideoFrame);
                frameIndex++;

                if (canLog) {
                    logExecutorService.execute(() -> {
                        SampleLogger.log("send rgba frame data size:" + data.length +
                                " timestamp:" + timestamp + " frameIndex:" + frameIndex + " testStartTime:"
                                + Utils.formatTimestamp(testStartTime)
                                + " testTime:" + testTime);
                    });
                    if (isStressTest) {
                        canLog = false;
                    }
                }
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                if (fos == null) {
                    return null;
                }
                int size = 0;
                try {
                    size = fos.read(buffer, 0, bufferLen);

                    if (size < 0) {
                        if (isLoopSend) {
                            reset();
                            frameIndex = 0;
                        } else {
                            if (null != taskFinishLatch) {
                                taskFinishLatch.countDown();
                            }
                        }
                        return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Arrays.copyOf(buffer, size);
            }

            @Override
            public void release(boolean withJoin) {
                super.release(withJoin);
                DirectBufferCleaner.release(byteBuffer);
                DirectBufferCleaner.release(alphaBuffer);
            }
        };

        rgbaSender.start();
        if (!taskStarted.get()) {
            taskStarted.set(true);
        }
        SampleLogger.log("sendRgbaTask start");

        if (waitRelease) {
            handleWaitRelease();
            rgbaSender.release(false);
            SampleLogger.log("sendRgbaTask end");
            releaseConn();
        }
    }

    public void sendVp8Task(String filePath, int interval, int height, int width, int fps, int streamType,
            boolean waitRelease) {
        boolean enableSimulcastStream = ArgsConfig.enableSimulcastStream == 1;
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        SampleLogger.log("sendVp8Task filePath:" + filePath + " interval:" + interval + " height:" + height + " width:"
                + width + " fps:" + fps + " streamType:" + streamType + " enableSimulcastStream:"
                + enableSimulcastStream);
        if (customEncodedImageSender == null) {
            customEncodedImageSender = mediaNodeFactory.createVideoEncodedImageSender();
            // Create video track
            SenderOptions option = new SenderOptions();
            option.setCcMode(Constants.TCC_ENABLED);
            customEncodedVideoTrack = service.createCustomVideoTrackEncoded(customEncodedImageSender, option);

            if (enableSimulcastStream) {
                SimulcastStreamConfig lowStreamConfig = new SimulcastStreamConfig();
                // lowStreamConfig.setBitrate(65);
                lowStreamConfig.setFramerate(fps);
                VideoDimensions dimensions = new VideoDimensions(width, height);
                lowStreamConfig.setDimensions(dimensions);
                customEncodedVideoTrack.enableSimulcastStream(1, lowStreamConfig);
            }

            VideoEncoderConfig config = new VideoEncoderConfig();
            config.setCodecType(Constants.VIDEO_CODEC_VP8);
            customEncodedVideoTrack.setVideoEncoderConfig(config);

            // Publish video track
            int ret = conn.getLocalUser().publishVideo(customEncodedVideoTrack);
            // wait for 3 seconds to publish video for vp8
            try {
                Thread.sleep(3 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SampleLogger.log("sendVp8Task publishVideo ret:" + ret);
        }

        Vp8Reader vp8Reader = new Vp8Reader(filePath);

        boolean isLoopSend = testTime > 0;
        FileSender vp8SendThread = new FileSender(filePath, interval, false) {
            private int lastFrameType = 0;
            private int frameIndex = 0;
            private int height;
            private int width;
            private boolean canLog = true;

            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                if (data == null) {
                    return;
                }

                EncodedVideoFrameInfo info = new EncodedVideoFrameInfo();
                long currentTime = timestamp;
                info.setFrameType(lastFrameType);
                info.setWidth(width);
                info.setHeight(height);
                info.setCodecType(Constants.VIDEO_CODEC_VP8);
                info.setCaptureTimeMs(currentTime);
                info.setDecodeTimeMs(currentTime);
                info.setFramesPerSecond(fps);
                info.setRotation(0);

                if (!taskStarted.get()) {
                    return;
                }

                int ret = customEncodedImageSender.send(data, data.length, info);
                frameIndex++;
                if (canLog) {
                    logExecutorService.execute(() -> {
                        SampleLogger.log("send vp8 ret:" + ret + " frame data size:" + data.length + " width:" + width
                                + " height:" + height + " lastFrameType:" + lastFrameType + " fps:" + fps +
                                " timestamp:" + timestamp + " frameIndex:" + frameIndex + " testStartTime:"
                                + Utils.formatTimestamp(testStartTime)
                                + " testTime:" + testTime + " from channelId:" + currentChannelId + " userId:"
                                + currentUserId);
                    });
                    if (isStressTest) {
                        canLog = false;
                    }
                }
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                io.agora.rtc.example.mediautils.VideoFrame frame = vp8Reader.readNextFrame();
                int retry = 0;
                while (frame == null && retry < 4) {
                    vp8Reader.reset();
                    frame = vp8Reader.readNextFrame();
                    retry++;
                }

                if (frame == null) {
                    if (isLoopSend) {
                        reset();
                        frameIndex = 0;
                    } else {
                        if (null != taskFinishLatch) {
                            taskFinishLatch.countDown();
                        }
                    }
                    return null;
                }
                lastFrameType = frame.frameType;
                width = frame.width;
                height = frame.height;
                return frame.data;
            }

            @Override
            public void release(boolean withJoin) {
                super.release(withJoin);
                if (null != vp8Reader) {
                    vp8Reader.close();
                }
            }
        };

        vp8SendThread.start();
        if (!taskStarted.get()) {
            taskStarted.set(true);
        }
        SampleLogger.log("sendVp8Task start");

        if (waitRelease) {
            handleWaitRelease();
            vp8SendThread.release(false);
            SampleLogger.log("sendVp8Task end");
            releaseConn();
        }
    }

    public void sendAvMediaTask(String filePath, int interval) {
        SampleLogger.log("sendAvMediaTask filePath:" + filePath + " interval:" + interval);

        MediaDecodeUtils mediaDecodeUtils = new MediaDecodeUtils();

        boolean initRet = mediaDecodeUtils.init(filePath, interval, -1,
                MediaDecodeUtils.DecodedMediaType.PCM_YUV,
                new MediaDecodeUtils.MediaDecodeCallback() {
                    private ByteBuffer byteBuffer;

                    @Override
                    public void onAudioFrame(MediaDecode.MediaFrame frame) {
                        if (audioFrameSender == null) {
                            audioFrameSender = mediaNodeFactory.createAudioPcmDataSender();
                            // Create audio track
                            customAudioTrack = service.createCustomAudioTrackPcm(audioFrameSender);
                            // customAudioTrack.setMaxBufferedAudioFrameNumber(1000);
                            conn.getLocalUser().publishAudio(customAudioTrack);
                        }
                        if (!taskStarted.get()) {
                            return;
                        }
                        int ret = audioFrameSender.send(frame.buffer, (int) (frame.pts),
                                frame.samples, frame.bytesPerSample,
                                frame.channels,
                                frame.sampleRate);
                        logExecutorService.execute(() -> {
                            SampleLogger.log("SendPcmData frame.pts:" + frame.pts + " ret:" + ret);
                        });
                    }

                    @Override
                    public void onVideoFrame(MediaDecode.MediaFrame frame) {
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
                        byteBuffer.put(frame.buffer);
                        byteBuffer.flip();
                        externalVideoFrame.setBuffer(byteBuffer);
                        externalVideoFrame.setHeight(frame.height);
                        externalVideoFrame.setStride(frame.stride);
                        externalVideoFrame.setRotation(0);
                        externalVideoFrame.setFormat(Constants.EXTERNAL_VIDEO_FRAME_PIXEL_FORMAT_I420);
                        externalVideoFrame.setType(Constants.EXTERNAL_VIDEO_FRAME_BUFFER_TYPE_RAW_DATA);
                        externalVideoFrame.setTimestamp(frame.pts);
                        if (!taskStarted.get()) {
                            return;
                        }
                        int ret = videoFrameSender.send(externalVideoFrame);
                        logExecutorService.execute(() -> {
                            SampleLogger.log("SendVideoFrame frame.pts:" + frame.pts + " ret:" + ret);
                        });
                    }
                });

        SampleLogger.log("sendAvMediaTask initRet:" + initRet);
        if (initRet) {
            mediaDecodeUtils.start();
        }
        if (!taskStarted.get()) {
            taskStarted.set(true);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        releaseConn();
    }

    public void sendDataStreamTask(int createCount, int sendStreamMessageCount, boolean waitRelease) {
        if (conn == null) {
            SampleLogger.log("sendDataStream conn is null");
            return;
        }
        if (createCount < 1) {
            SampleLogger.log("sendDataStream createCount is less than 1");
            return;
        }

        final CountDownLatch testFinishLatch = new CountDownLatch(1);
        try {
            boolean[] sendStreamMessageDone = new boolean[createCount];
            @SuppressWarnings("unchecked")
            Out<Integer>[] streamIds = (Out<Integer>[]) Array.newInstance(Out.class, createCount);
            for (int k = 0; k < createCount; k++) {
                streamIds[k] = new Out<>();
                // 创建信令通道
                int result = conn.createDataStream(streamIds[k], 0, 0);
                SampleLogger
                        .log("sendDataStream create DataStream result " + result + " stream id " + streamIds[k].get());

                final int index = k;
                testTaskExecutorService.execute(() -> {
                    for (int i = 0; i < sendStreamMessageCount; i++) {
                        String data = Utils.getCurrentTime() + " hello world from channelId:"
                                + currentChannelId + " userId:" + currentUserId;
                        int ret = conn.sendStreamMessage(streamIds[index].get(), data.getBytes());
                        logExecutorService.execute(() -> {
                            SampleLogger.log("sendStreamMessage: " + data + " done ret:" + ret);
                        });

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    sendStreamMessageDone[index] = true;
                    boolean allDone = true;
                    for (int j = 0; j < createCount; j++) {
                        if (!sendStreamMessageDone[j]) {
                            allDone = false;
                            break;
                        }
                    }
                    if (allDone) {
                        testFinishLatch.countDown();
                    }

                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            testFinishLatch.countDown();
        }
        if (waitRelease) {
            try {
                testFinishLatch.await();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            releaseConn();
        }
    }

    public void registerPcmObserverTask(String remoteUserId, String audioOutFile, int numOfChannels, int sampleRate,
            boolean waitRelease) {
        boolean enableSaveFile = ArgsConfig.enableSaveFile == 1;
        boolean enableVad = ArgsConfig.enableVad == 1;
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        SampleLogger.log("registerPcmObserverTask remoteUserId:" + remoteUserId + " audioOutFile:" + audioOutFile
                + " numOfChannels:"
                + numOfChannels + " sampleRate:" + sampleRate + " enableSaveFile:" + enableSaveFile + " enableVad:"
                + enableVad);
        conn.getLocalUser().subscribeAllAudio();

        // Register audio frame observer to receive audio stream
        int ret = conn.getLocalUser().setPlaybackAudioFrameBeforeMixingParameters(numOfChannels, sampleRate);
        SampleLogger.log("setPlaybackAudioFrameBeforeMixingParameters numOfChannels:" + numOfChannels + " sampleRate:"
                + sampleRate);
        if (ret > 0) {
            SampleLogger.log("setPlaybackAudioFrameBeforeMixingParameters fail ret=" + ret);
            return;
        }

        audioFrameObserver = new SampleAudioFrameObserver(audioOutFile) {
            private boolean canLog = true;

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

                if (canLog) {
                    logExecutorService.execute(() -> {
                        SampleLogger.log("onPlaybackAudioFrameBeforeMixing frame:" + frame);
                        SampleLogger.log("onPlaybackAudioFrameBeforeMixing audioFrame size " + byteArray.length
                                + " channelId:"
                                + channelId + " userId:" + userId + " with current channelId:"
                                + currentChannelId
                                + " currentUserId:" + currentUserId);
                    });
                    if (isStressTest) {
                        canLog = false;
                    }
                }

                if (enableSaveFile) {
                    writeAudioFrameToFile(byteArray);
                }
                if (null != vadResult) {
                    if (canLog) {
                        logExecutorService.execute(() -> {
                            SampleLogger.log("onPlaybackAudioFrameBeforeMixing vadResult:" + vadResult);
                        });
                        if (isStressTest) {
                            canLog = false;
                        }
                    }
                    writeVadAudioToFile(vadResult.getOutFrame(), audioOutFile + "_vad.pcm");
                }
                return 1;
            }

        };
        conn.getLocalUser().registerAudioFrameObserver(audioFrameObserver, enableVad, new AgoraAudioVadConfigV2());

        if (waitRelease) {
            handleWaitRelease();
            releaseConn();
        }
    }

    public void registerMixedAudioObserverTask(String remoteUserId, String audioOutFile, int numOfChannels,
            int sampleRate,
            boolean waitRelease) {
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        SampleLogger.log("registerMixedAudioObserverTask remoteUserId:" + remoteUserId + " audioOutFile:" + audioOutFile
                + " numOfChannels:"
                + numOfChannels + " sampleRate:" + sampleRate);

        conn.getLocalUser().subscribeAllAudio();

        // Register audio frame observer to receive audio stream
        int ret = conn.getLocalUser().setPlaybackAudioFrameParameters(numOfChannels, sampleRate, 0,
                sampleRate / 100 * numOfChannels);
        SampleLogger.log("setPlaybackAudioFrameParameters numOfChannels:" + numOfChannels + " sampleRate:"
                + sampleRate);
        if (ret > 0) {
            SampleLogger.log("setPlaybackAudioFrameParameters fail ret=" + ret);
            return;
        }

        audioFrameObserver = new SampleAudioFrameObserver(audioOutFile) {
            private boolean canLog = true;

            @Override
            public int onPlaybackAudioFrame(AgoraLocalUser agoraLocalUser, String channelId, AudioFrame frame) {
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

                if (canLog) {
                    logExecutorService.execute(() -> {
                        SampleLogger.log("onPlaybackAudioFrame frame:" + frame);
                        SampleLogger.log("onPlaybackAudioFrame audioFrame size " + byteArray.length
                                + " channelId:"
                                + channelId + " with current channelId:"
                                + currentChannelId
                                + "  userId:" + currentUserId);
                    });
                    if (isStressTest) {
                        canLog = false;
                    }
                }

                writeAudioFrameToFile(byteArray);
                return 1;
            }

        };
        conn.getLocalUser().registerAudioFrameObserver(audioFrameObserver, false, null);

        if (waitRelease) {
            handleWaitRelease();
            releaseConn();
        }
    }

    public void registerYuvObserverTask(String remoteUserId, String videoOutFile, String streamType,
            boolean waitRelease) {
        boolean enableSaveFile = ArgsConfig.enableSaveFile == 1;
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        SampleLogger.log(
                String.format("registerYuvObserverTask remoteUserId:%s videoOutFile:%s streamType:%s enableSaveFile:%b",
                        remoteUserId, videoOutFile, streamType, enableSaveFile));

        VideoSubscriptionOptions subscriptionOptions = new VideoSubscriptionOptions();
        switch (streamType) {
            case "high":
                subscriptionOptions.setType(Constants.VIDEO_STREAM_HIGH);
                break;
            case "low":
                subscriptionOptions.setType(Constants.VIDEO_STREAM_LOW);
                break;
            default:
                return;
        }
        conn.getLocalUser().subscribeAllVideo(subscriptionOptions);

        videoFrameObserver = new AgoraVideoFrameObserver2(new SampleVideFrameObserver(videoOutFile) {
            private boolean canLog = true;

            @Override
            public void onFrame(AgoraVideoFrameObserver2 agoraVideoFrameObserver2, String channelId,
                    String remoteUserId, VideoFrame frame) {
                if (frame == null) {
                    return;
                }

                int ylength = frame.getYBuffer().remaining();
                int ulength = frame.getUBuffer().remaining();
                int vlength = frame.getVBuffer().remaining();
                if (enableSaveFile) {
                    byte[] data = new byte[ylength + ulength + vlength];
                    ByteBuffer buffer = ByteBuffer.wrap(data);
                    buffer.put(frame.getYBuffer()).put(frame.getUBuffer()).put(frame.getVBuffer());
                    writeVideoFrameToFile(data);
                }

                final byte[] metaDataBufferData = io.agora.rtc.utils.Utils.getBytes(frame.getMetadataBuffer());
                final byte[] alphaBufferData = io.agora.rtc.utils.Utils.getBytes(frame.getAlphaBuffer());

                if (canLog) {
                    logExecutorService.execute(() -> {
                        SampleLogger.log(String.format(
                                "onFrame width:%d height:%d channelId:%s remoteUserId:%s frame size:%d %d %d with current channelId:%s userId:%s",
                                frame.getWidth(), frame.getHeight(), channelId, remoteUserId, ylength, ulength, vlength,
                                currentChannelId, currentUserId));

                        if (metaDataBufferData != null) {
                            SampleLogger.log("onFrame metaDataBuffer :" + new String(metaDataBufferData));
                        }

                        if (alphaBufferData != null) {
                            SampleLogger.log("onFrame getAlphaBuffer size:" + alphaBufferData.length + " mode:"
                                    + frame.getAlphaMode());
                        }
                    });
                    if (isStressTest) {
                        canLog = false;
                    }
                }
            }
        });
        conn.getLocalUser().registerVideoFrameObserver(videoFrameObserver);

        if (waitRelease) {
            handleWaitRelease();
            releaseConn();
        }
    }

    public void registerH264ObserverTask(String remoteUserId, String videoOutFile, String streamType,
            boolean waitRelease) {
        boolean enableSaveFile = ArgsConfig.enableSaveFile == 1;
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        SampleLogger.log("registerH264ObserverTask remoteUserId:" + remoteUserId + " videoOutFile:" + videoOutFile +
                " streamType:" + streamType
                + " enableSaveFile:" + enableSaveFile);

        VideoSubscriptionOptions subscriptionOptions = new VideoSubscriptionOptions();
        subscriptionOptions.setEncodedFrameOnly(1);
        if ("high".equals(streamType)) {
            subscriptionOptions.setType(Constants.VIDEO_STREAM_HIGH);
        } else if ("low".equals(streamType)) {
            subscriptionOptions.setType(Constants.VIDEO_STREAM_LOW);
        } else {
            return;
        }
        conn.getLocalUser().subscribeAllVideo(subscriptionOptions);

        videoEncodedFrameObserver = new AgoraVideoEncodedFrameObserver(
                new SampleVideoEncodedFrameObserver(videoOutFile) {
                    private boolean canLog = true;

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

                        if (canLog) {
                            logExecutorService.execute(() -> {
                                SampleLogger.log("onEncodedVideoFrame userId:" + userId + " length "
                                        + byteArray.length
                                        + " with current channelId:"
                                        + currentChannelId
                                        + "  current userId:" + currentUserId + " info:" + info);
                            });
                            if (isStressTest) {
                                canLog = false;
                            }
                        }

                        if (enableSaveFile) {
                            writeVideoDataToFile(byteArray);
                        }

                        return 1;
                    }
                });
        conn.getLocalUser().registerVideoEncodedFrameObserver(videoEncodedFrameObserver);

        if (waitRelease) {
            handleWaitRelease();
            releaseConn();
        }
    }

    public void registerEncodedAudioObserverTask(String remoteUserId, String audioOutFile, String fileType,
            boolean waitRelease) {
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        SampleLogger
                .log("registerEncodedAudioObserverTask remoteUserId:" + remoteUserId + " audioOutFile:" + audioOutFile +
                        " fileType:" + fileType);

        conn.getLocalUser().subscribeAllAudio();

        audioEncodedFrameObserver = new SampleAudioEncodedFrameObserver(audioOutFile) {
            private boolean canLog = true;

            @Override
            public int onEncodedAudioFrameReceived(String remoteUserId, ByteBuffer buffer,
                    EncodedAudioFrameReceiverInfo info) {
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
                if (canLog) {
                    logExecutorService.execute(() -> {
                        SampleLogger.log("onEncodedAudioFrameReceived buffer size:" + byteArray.length +
                                " info:" + info + " remoteUserId:" + remoteUserId +
                                " with current channelId:" + currentChannelId);
                    });
                    if (isStressTest) {
                        canLog = false;
                    }
                }

                writeAudioFrameToFile(byteArray, audioOutFile + "-" + remoteUserId + "." + fileType);
                return 1;
            }
        };

        conn.getLocalUser().registerAudioEncodedFrameObserver(audioEncodedFrameObserver);

        if (waitRelease) {
            handleWaitRelease();
            releaseConn();
        }
    }

    private void handleWaitRelease() {
        try {
            if (testTime > 0) {
                synchronized (this) {
                    wait(testTime * 1000);
                }
            } else {
                if (null == taskFinishLatch) {
                    taskFinishLatch = new CountDownLatch(1);
                }
                taskFinishLatch.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}