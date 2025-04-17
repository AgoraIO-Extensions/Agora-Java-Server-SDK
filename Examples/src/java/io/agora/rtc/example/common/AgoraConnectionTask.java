package io.agora.rtc.example.common;

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
import java.util.concurrent.locks.ReentrantLock;

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
    private CountDownLatch taskFinishLatch;
    private CountDownLatch connDisconnectedLatch;
    private final AtomicBoolean taskStarted = new AtomicBoolean(false);

    private final AtomicBoolean isReleasing = new AtomicBoolean(false);

    private final ReentrantLock pcmLock = new ReentrantLock();
    private final ReentrantLock yuvLock = new ReentrantLock();
    private final ReentrantLock h264Lock = new ReentrantLock();
    private final ReentrantLock vp8Lock = new ReentrantLock();
    private final ReentrantLock rgbaLock = new ReentrantLock();
    private final ReentrantLock aacLock = new ReentrantLock();
    private final ReentrantLock opusLock = new ReentrantLock();

    private static final ThreadLocal<H264Reader> threadLocalH264Reader = new ThreadLocal<>();
    private static final ThreadLocal<Vp8Reader> threadLocalVp8Reader = new ThreadLocal<>();
    private static final ThreadLocal<OpusReader> threadLocalOpusReader = new ThreadLocal<>();
    private static final ThreadLocal<AacReader> threadLocalAacReader = new ThreadLocal<>();

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

    public AgoraConnectionTask(AgoraService service, AgoraMediaNodeFactory mediaNodeFactory, long testTime) {
        this.service = service;
        this.mediaNodeFactory = mediaNodeFactory;
        this.testTime = testTime;
        this.testStartTime = System.currentTimeMillis();
        this.singleExecutorService = Executors.newSingleThreadExecutor();
        this.testTaskExecutorService = new ThreadPoolExecutor(
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
        if (conn == null || mediaNodeFactory == null) {
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

            @Override
            public void onEncryptionError(AgoraRtcConn agoraRtcConn, int errorType) {
                SampleLogger.log("onEncryptionError errorType:" + errorType);
            }
        });
        SampleLogger.log("registerObserver channelId:" + currentChannelId + " userId:" + currentUserId
                + " ret:" + ret);

        ret = conn.registerNetworkObserver(new INetworkObserver() {
            @Override
            public void onUplinkNetworkInfoUpdated(AgoraRtcConn agoraRtcConn, UplinkNetworkInfo info) {
                if (ArgsConfig.isStressTest == 0) {
                    SampleLogger.log("onUplinkNetworkInfoUpdated channelId:"
                            + agoraRtcConn.getConnInfo().getChannelId() + " userId:"
                            + agoraRtcConn.getConnInfo().getLocalUserId() + " info:" + info);
                }
            }

            @Override
            public void onDownlinkNetworkInfoUpdated(AgoraRtcConn agoraRtcConn, DownlinkNetworkInfo info) {
                if (ArgsConfig.isStressTest == 0) {
                    SampleLogger.log("onDownlinkNetworkInfoUpdated channelId:"
                            + agoraRtcConn.getConnInfo().getChannelId() + " userId:"
                            + agoraRtcConn.getConnInfo().getLocalUserId() + " info:" + info);
                }
            }
        });
        SampleLogger.log("registerNetworkObserver channelId:" + currentChannelId + " userId:" + currentUserId
                + " ret:" + ret);

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
            sampleLocalUserObserver = new SampleLocalUserObserver();
        }
        conn.getLocalUser().registerObserver(sampleLocalUserObserver);

        taskStarted.set(true);
    }

    public void releaseConn() {
        if (!isReleasing.compareAndSet(false, true)) {
            SampleLogger.log("releaseConn already in progress, skipping");
            return;
        }

        pcmLock.lock();
        yuvLock.lock();
        h264Lock.lock();
        vp8Lock.lock();
        rgbaLock.lock();
        aacLock.lock();
        opusLock.lock();

        try {
            SampleLogger.log("releaseConn for channelId:" + currentChannelId + " userId:" + currentUserId);
            if (conn == null) {
                return;
            }

            taskStarted.set(false);

            try {
                if (null != audioFrameSender) {
                    try {
                        audioFrameSender.destroy();
                    } catch (Exception e) {
                        SampleLogger.error("Error destroying audioFrameSender: " + e.getMessage());
                    } finally {
                        audioFrameSender = null;
                    }
                }

                if (null != customAudioTrack) {
                    try {
                        customAudioTrack.clearSenderBuffer();
                        conn.getLocalUser().unpublishAudio(customAudioTrack);
                        customAudioTrack.destroy();
                    } catch (Exception e) {
                        SampleLogger.error("Error destroying customAudioTrack: " + e.getMessage());
                    } finally {
                        customAudioTrack = null;
                    }
                }

                if (null != customEncodedImageSender) {
                    try {
                        customEncodedImageSender.destroy();
                    } catch (Exception e) {
                        SampleLogger.error("Error destroying customEncodedImageSender: " + e.getMessage());
                    } finally {
                        customEncodedImageSender = null;
                    }
                }

                if (null != customEncodedVideoTrack) {
                    try {
                        conn.getLocalUser().unpublishVideo(customEncodedVideoTrack);
                        customEncodedVideoTrack.destroy();
                    } catch (Exception e) {
                        SampleLogger.error("Error destroying customEncodedVideoTrack: " + e.getMessage());
                    } finally {
                        customEncodedVideoTrack = null;
                    }
                }

                if (null != videoFrameSender) {
                    try {
                        videoFrameSender.destroy();
                    } catch (Exception e) {
                        SampleLogger.error("Error destroying videoFrameSender: " + e.getMessage());
                    } finally {
                        videoFrameSender = null;
                    }
                }

                if (null != customVideoTrack) {
                    try {
                        conn.getLocalUser().unpublishVideo(customVideoTrack);
                        customVideoTrack.destroy();
                    } catch (Exception e) {
                        SampleLogger.error("Error destroying customVideoTrack: " + e.getMessage());
                    } finally {
                        customVideoTrack = null;
                    }
                }

                if (null != audioEncodedFrameSender) {
                    try {
                        audioEncodedFrameSender.destroy();
                    } catch (Exception e) {
                        SampleLogger.error("Error destroying audioEncodedFrameSender: " + e.getMessage());
                    } finally {
                        audioEncodedFrameSender = null;
                    }
                }

                if (null != customEncodedAudioTrack) {
                    try {
                        conn.getLocalUser().unpublishAudio(customEncodedAudioTrack);
                        customEncodedAudioTrack.destroy();
                    } catch (Exception e) {
                        SampleLogger.error("Error destroying customEncodedAudioTrack: " + e.getMessage());
                    } finally {
                        customEncodedAudioTrack = null;
                    }
                }

                if (null != audioFrameObserver) {
                    try {
                        conn.getLocalUser().unregisterAudioFrameObserver();
                    } catch (Exception e) {
                        SampleLogger.error("Error unregistering audioFrameObserver: " + e.getMessage());
                    } finally {
                        audioFrameObserver = null;
                    }
                }

                if (null != audioEncodedFrameObserver) {
                    try {
                        conn.getLocalUser().unregisterAudioEncodedFrameObserver(audioEncodedFrameObserver);
                    } catch (Exception e) {
                        SampleLogger.error("Error unregistering audioEncodedFrameObserver: " + e.getMessage());
                    } finally {
                        audioEncodedFrameObserver = null;
                    }
                }

                if (null != videoFrameObserver) {
                    try {
                        conn.getLocalUser().unregisterVideoFrameObserver(videoFrameObserver);
                        videoFrameObserver.destroy();
                    } catch (Exception e) {
                        SampleLogger.error("Error destroying videoFrameObserver: " + e.getMessage());
                    } finally {
                        videoFrameObserver = null;
                    }
                }

                if (null != videoEncodedFrameObserver) {
                    try {
                        conn.getLocalUser().unregisterVideoEncodedFrameObserver(videoEncodedFrameObserver);
                        videoEncodedFrameObserver.destroy();
                    } catch (Exception e) {
                        SampleLogger.error("Error destroying videoEncodedFrameObserver: " + e.getMessage());
                    } finally {
                        videoEncodedFrameObserver = null;
                    }
                }

                try {
                    int ret = conn.disconnect();
                    if (ret != 0) {
                        SampleLogger.log("conn.disconnect fail ret=" + ret);
                    }

                    if (null == connDisconnectedLatch) {
                        connDisconnectedLatch = new CountDownLatch(1);
                    }

                    boolean waitResult = connDisconnectedLatch.await(1, TimeUnit.SECONDS);
                    if (!waitResult) {
                        SampleLogger.error("conn disconnected wait timeout");
                    }
                } catch (InterruptedException e) {
                    SampleLogger.error("Interrupted while waiting for disconnect: " + e.getMessage());
                } catch (Exception e) {
                    SampleLogger.error("Error during disconnect: " + e.getMessage());
                }

                try {
                    conn.unregisterObserver();
                    conn.getLocalUser().unregisterObserver();
                } catch (Exception e) {
                    SampleLogger.error("Error unregistering observers: " + e.getMessage());
                }

                try {
                    conn.destroy();
                } catch (Exception e) {
                    SampleLogger.error("Error destroying connection: " + e.getMessage());
                } finally {
                    conn = null;
                }

                sampleLocalUserObserver = null;
                taskFinishLatch = null;

                try {
                    singleExecutorService.shutdownNow();
                    testTaskExecutorService.shutdownNow();
                } catch (Exception e) {
                    SampleLogger.error("Error shutting down executor services: " + e.getMessage());
                }

                SampleLogger.log("destroy conn for channelId:" + currentChannelId + " userId:" + currentUserId);
            } catch (Exception e) {
                SampleLogger.error("Exception during resource cleanup: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            try {
                opusLock.unlock();
            } catch (Exception e) {
            }
            try {
                aacLock.unlock();
            } catch (Exception e) {
            }
            try {
                rgbaLock.unlock();
            } catch (Exception e) {
            }
            try {
                vp8Lock.unlock();
            } catch (Exception e) {
            }
            try {
                h264Lock.unlock();
            } catch (Exception e) {
            }
            try {
                yuvLock.unlock();
            } catch (Exception e) {
            }
            try {
                pcmLock.unlock();
            } catch (Exception e) {
            }

            isReleasing.set(false);

            if (null != callback) {
                try {
                    callback.onTestFinished();
                } catch (Exception e) {
                    SampleLogger.error("Error in onTestFinished callback: " + e.getMessage());
                }
            }
        }
    }

    public void sendPcmTask(String filePath, int interval, int numOfChannels, int sampleRate,
            boolean waitRelease) {
        long remainingTime = getRemainingTime();
        boolean enableAudioCache = ArgsConfig.enableAudioCache == 1;
        boolean enableSendAudioMetaData = ArgsConfig.enableSendAudioMetaData == 1;
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        SampleLogger
                .log("sendPcmTask filePath:" + filePath + " interval:" + interval + " numOfChannels:" + numOfChannels
                        + " sampleRate:" + sampleRate + " enableAudioCache:" + enableAudioCache
                        + " enableSendAudioMetaData:"
                        + enableSendAudioMetaData + " remainingTime:" + remainingTime);

        if (remainingTime == -1) {
            if (waitRelease) {
                releaseConn();
            }
            return;
        }

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
                pcmLock.lock();
                try {
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
                            SampleLogger.log("send pcm " + consumeFrameCount + " frame data to channelId:"
                                    + currentChannelId + " from userId:" + currentUserId);
                            if (isStressTest) {
                                canLog = false;
                            }
                        }

                        if (enableSendAudioMetaData) {
                            String audioMetaData = "testSendAudioMetaData " + timestamp;
                            int ret = conn.getLocalUser()
                                    .sendAudioMetaData(audioMetaData.getBytes());
                            if (canLog) {
                                SampleLogger.log("sendAudioMetaData: " + audioMetaData + " ret:" + ret);
                                if (isStressTest) {
                                    canLog = false;
                                }
                            }
                        }
                    } else {
                        release(false);
                    }
                } finally {
                    pcmLock.unlock();
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
                pcmLock.lock();
                try {
                    if (null != audioConsumerUtils) {
                        audioConsumerUtils.release();
                        audioConsumerUtils = null;
                    }
                } finally {
                    pcmLock.unlock();
                }
            }

        };

        pcmSendThread.start();
        SampleLogger.log("sendPcmTask start");
        if (waitRelease) {
            try {
                handleWaitRelease(remainingTime);
                pcmSendThread.release(false);
                SampleLogger.log("sendPcmTask end");
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendAacTask(String filePath, int interval, int numOfChannels, int sampleRate, boolean waitRelease) {
        long remainingTime = getRemainingTime();
        SampleLogger
                .log("sendAacTask filePath:" + filePath + " interval:" + interval + " numOfChannels:" + numOfChannels
                        + " sampleRate:" + sampleRate + " remainingTime:" + remainingTime);

        if (remainingTime == -1) {
            if (waitRelease) {
                releaseConn();
            }
            return;
        }

        boolean isStressTest = ArgsConfig.isStressTest == 1;
        // Create audio track
        audioEncodedFrameSender = mediaNodeFactory.createAudioEncodedFrameSender();
        customEncodedAudioTrack = service.createCustomAudioTrackEncoded(audioEncodedFrameSender,
                Constants.TMixMode.MIX_DISABLED.value);
        conn.getLocalUser().publishAudio(customEncodedAudioTrack);

        boolean isLoopSend = testTime > 0;
        FileSender aacSendThread = new FileSender(filePath, interval) {
            private boolean canLog = true;
            private EncodedAudioFrameInfo encodedInfo = new EncodedAudioFrameInfo();
            private AacReader aacReader;

            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                aacLock.lock();
                try {
                    if (data == null) {
                        return;
                    }
                    if (!taskStarted.get()) {
                        release(false);
                        return;
                    }
                    int ret = audioEncodedFrameSender.sendEncodedAudioFrame(data, encodedInfo);
                    if (canLog) {
                        SampleLogger.log("send aac frame data size:" + data.length + " timestamp:"
                                + timestamp + " encodedInfo:" + encodedInfo
                                + " to channelId:"
                                + currentChannelId + " from userId:" + currentUserId + " ret:" + ret
                                + " testStartTime:"
                                + Utils.formatTimestamp(testStartTime)
                                + " testTime:" + testTime);
                        if (isStressTest) {
                            canLog = false;
                        }
                    }
                } finally {
                    aacLock.unlock();
                }
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                if (aacReader == null) {
                    aacReader = threadLocalAacReader.get();
                    if (aacReader == null) {
                        aacReader = new AacReader(filePath);
                        threadLocalAacReader.set(aacReader);
                    }
                }
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
                aacLock.lock();
                try {
                    if (null != aacReader) {
                        aacReader.close();
                    }
                    threadLocalAacReader.remove();
                    aacReader = null;
                    encodedInfo = null;
                } finally {
                    aacLock.unlock();
                }
            }
        };

        aacSendThread.start();

        SampleLogger.log("sendAacTask start");

        if (waitRelease) {
            try {
                handleWaitRelease(remainingTime);
                aacSendThread.release(false);
                SampleLogger.log("sendAacTask end");
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendOpusTask(String filePath, int interval, boolean waitRelease) {
        long remainingTime = getRemainingTime();
        SampleLogger
                .log("sendOpusTask filePath:" + filePath + " interval:" + interval + " remainingTime:" + remainingTime);

        if (remainingTime == -1) {
            if (waitRelease) {
                releaseConn();
            }
            return;
        }

        boolean isStressTest = ArgsConfig.isStressTest == 1;
        // Create audio track
        audioEncodedFrameSender = mediaNodeFactory.createAudioEncodedFrameSender();
        customEncodedAudioTrack = service.createCustomAudioTrackEncoded(audioEncodedFrameSender,
                Constants.TMixMode.MIX_DISABLED.value);
        conn.getLocalUser().publishAudio(customEncodedAudioTrack);

        boolean isLoopSend = testTime > 0;
        FileSender opusSendThread = new FileSender(filePath, interval) {
            private boolean canLog = true;
            private EncodedAudioFrameInfo encodedInfo = new EncodedAudioFrameInfo();
            private OpusReader opusReader;

            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                opusLock.lock();
                try {
                    if (data == null) {
                        return;
                    }

                    if (null == data || data.length == 0) {
                        return;
                    }

                    if (!taskStarted.get()) {
                        release(false);
                        return;
                    }

                    int ret = audioEncodedFrameSender.sendEncodedAudioFrame(data, encodedInfo);
                    if (canLog) {
                        SampleLogger.log("send opus frame data size:" + data.length + " timestamp:"
                                + timestamp + " encodedInfo:" + encodedInfo
                                + " to channelId:"
                                + currentChannelId + " from userId:" + currentUserId + " ret:" + ret
                                + " testStartTime:"
                                + Utils.formatTimestamp(testStartTime)
                                + " testTime:" + testTime);
                        if (isStressTest) {
                            canLog = false;
                        }
                    }
                } finally {
                    opusLock.unlock();
                }
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                if (opusReader == null) {
                    opusReader = threadLocalOpusReader.get();
                    if (opusReader == null) {
                        opusReader = new OpusReader(filePath);
                        threadLocalOpusReader.set(opusReader);
                    }
                }
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
                opusLock.lock();
                try {
                    if (null != opusReader) {
                        opusReader.close();
                    }
                    threadLocalOpusReader.remove();
                    opusReader = null;
                    encodedInfo = null;
                } finally {
                    opusLock.unlock();
                }
            }
        };
        opusSendThread.start();

        SampleLogger.log("sendOpusTask start");

        if (waitRelease) {
            try {
                handleWaitRelease(remainingTime);
                opusSendThread.release(false);
                SampleLogger.log("sendOpusTask end");
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendYuvTask(String filePath, int interval, int height, int width, int fps, int streamType,
            boolean waitRelease) {
        long remainingTime = getRemainingTime();
        boolean enableSimulcastStream = ArgsConfig.enableSimulcastStream == 1;
        boolean enableAlpha = ArgsConfig.enableAlpha == 1;
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        SampleLogger.log("sendYuvTask filePath:" + filePath + " interval:" + interval + " height:" + height + " width:"
                + width + " streamType:" + streamType + " enableSimulcastStream:" + enableSimulcastStream
                + " enableAlpha:" + enableAlpha + " remainingTime:" + remainingTime);

        if (remainingTime == -1) {
            if (waitRelease) {
                releaseConn();
            }
            return;
        }

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
            private ExternalVideoFrame externalVideoFrame = new ExternalVideoFrame();

            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                yuvLock.lock();
                try {
                    if (data == null) {
                        return;
                    }

                    if (!taskStarted.get()) {
                        release(false);
                        return;
                    }

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
                    externalVideoFrame.setTimestamp(timestamp);

                    String testMetaData = "testMetaData";
                    if (null == matedataByteBuffer) {
                        matedataByteBuffer = ByteBuffer.allocateDirect(testMetaData.getBytes().length);
                    }
                    if (matedataByteBuffer == null || matedataByteBuffer.limit() < testMetaData.getBytes().length) {
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

                    int ret = videoFrameSender.sendVideoFrame(externalVideoFrame);
                    frameIndex++;

                    if (canLog) {
                        SampleLogger.log("send yuv frame data size:" + data.length + "  ret:" + ret +
                                " timestamp:" + timestamp + " frameIndex:" + frameIndex + " testStartTime:"
                                + Utils.formatTimestamp(testStartTime)
                                + " testTime:" +
                                testTime + " from channelId:" + currentChannelId + " userId:" + currentUserId);
                        if (isStressTest) {
                            canLog = false;
                        }
                    }
                } finally {
                    yuvLock.unlock();
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
                yuvLock.lock();
                try {
                    DirectBufferCleaner.release(byteBuffer);
                    DirectBufferCleaner.release(matedataByteBuffer);
                    DirectBufferCleaner.release(alphaByteBuffer);
                    externalVideoFrame = null;
                } finally {
                    yuvLock.unlock();
                }
            }

        };
        yuvSender.start();

        SampleLogger.log("sendYuvTask start");

        if (waitRelease) {
            try {
                handleWaitRelease(remainingTime);
                yuvSender.release(false);
                SampleLogger.log("sendYuvTask end");
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendH264Task(String filePath, int interval, int height, int width, int streamType,
            boolean waitRelease) {
        long remainingTime = getRemainingTime();
        boolean enableSimulcastStream = ArgsConfig.enableSimulcastStream == 1;
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        SampleLogger.log("sendH264Task filePath:" + filePath + " interval:" + interval + " height:" + height + " width:"
                + width + " streamType:" + streamType + " enableSimulcastStream:" + enableSimulcastStream
                + " remainingTime:" + remainingTime);

        if (remainingTime == -1) {
            if (waitRelease) {
                releaseConn();
            }
            return;
        }

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
        boolean isLoopSend = testTime > 0;
        FileSender h264SendThread = new FileSender(filePath, interval) {
            int lastFrameType = 0;
            int frameIndex = 0;
            private boolean canLog = true;
            private EncodedVideoFrameInfo info = new EncodedVideoFrameInfo();
            private H264Reader localH264Reader;

            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                h264Lock.lock();
                try {
                    if (data == null) {
                        return;
                    }
                    if (!taskStarted.get()) {
                        release(false);
                        return;
                    }

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

                    int ret = customEncodedImageSender.sendEncodedVideoImage(data, info);
                    frameIndex++;
                    if (canLog) {
                        SampleLogger.log("send h264 frame data size:" + data.length + " ret:" + ret +
                                " timestamp:" + timestamp + " frameIndex:" + frameIndex + " testStartTime:"
                                + Utils.formatTimestamp(testStartTime)
                                + " testTime:" +
                                testTime + " from channelId:" + currentChannelId + " userId:" + currentUserId);
                        if (isStressTest) {
                            canLog = false;
                        }
                    }
                } finally {
                    h264Lock.unlock();
                }
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                if (localH264Reader == null) {
                    localH264Reader = threadLocalH264Reader.get();
                    if (localH264Reader == null) {
                        localH264Reader = new H264Reader(filePath);
                        threadLocalH264Reader.set(localH264Reader);
                    }
                }

                H264Reader.H264Frame frame = localH264Reader.readNextFrame();
                if (frame == null) {
                    if (isLoopSend) {
                        localH264Reader.reset();
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
                h264Lock.lock();
                try {
                    if (localH264Reader != null) {
                        localH264Reader.close();
                        threadLocalH264Reader.remove();
                        localH264Reader = null;
                    }
                    info = null;
                } finally {
                    h264Lock.unlock();
                }
            }
        };

        h264SendThread.start();

        SampleLogger.log("sendH264Task start");

        if (waitRelease) {
            try {
                handleWaitRelease(remainingTime);
                h264SendThread.release(false);
                SampleLogger.log("sendH264Task end");
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendRgbaTask(String filePath, int interval, int height, int width, int fps, boolean waitRelease) {
        long remainingTime = getRemainingTime();
        SampleLogger.log("sendRgbaTask filePath:" + filePath + " interval:" + interval + " height:" + height + " width:"
                + width + " remainingTime:" + remainingTime);

        if (remainingTime == -1) {
            if (waitRelease) {
                releaseConn();
            }
            return;
        }

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
            private ExternalVideoFrame externalVideoFrame = new ExternalVideoFrame();

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
                rgbaLock.lock();
                try {
                    if (data == null) {
                        return;
                    }

                    if (!taskStarted.get()) {
                        release(false);
                        return;
                    }

                    // 通过ByteBuffer直接分配内存,确保其为DirectByteBuffer,满足Agora SDK要求
                    if (byteBuffer == null) {
                        byteBuffer = ByteBuffer.allocateDirect(data.length);
                    }
                    if (byteBuffer == null || byteBuffer.limit() < data.length) {
                        return;
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

                    int ret = videoFrameSender.sendVideoFrame(externalVideoFrame);
                    frameIndex++;

                    if (canLog) {
                        SampleLogger.log("send rgba frame data size:" + data.length + " ret:" + ret +
                                " timestamp:" + timestamp + " frameIndex:" + frameIndex + " testStartTime:"
                                + Utils.formatTimestamp(testStartTime)
                                + " testTime:" + testTime);
                        if (isStressTest) {
                            canLog = false;
                        }
                    }
                } finally {
                    rgbaLock.unlock();
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
                rgbaLock.lock();
                try {
                    DirectBufferCleaner.release(byteBuffer);
                    DirectBufferCleaner.release(alphaBuffer);
                    externalVideoFrame = null;
                } finally {
                    rgbaLock.unlock();
                }
            }
        };
        rgbaSender.start();

        SampleLogger.log("sendRgbaTask start");

        if (waitRelease) {
            try {
                handleWaitRelease(remainingTime);
                rgbaSender.release(false);
                SampleLogger.log("sendRgbaTask end");
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendVp8Task(String filePath, int interval, int height, int width, int fps, int streamType,
            boolean waitRelease) {
        long remainingTime = getRemainingTime();
        boolean enableSimulcastStream = ArgsConfig.enableSimulcastStream == 1;
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        SampleLogger.log("sendVp8Task filePath:" + filePath + " interval:" + interval + " height:" + height + " width:"
                + width + " fps:" + fps + " streamType:" + streamType + " enableSimulcastStream:"
                + enableSimulcastStream + " remainingTime:" + remainingTime);

        if (remainingTime == -1) {
            if (waitRelease) {
                releaseConn();
            }
            return;
        }
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

        boolean isLoopSend = testTime > 0;
        FileSender vp8SendThread = new FileSender(filePath, interval, false) {
            private int lastFrameType = 0;
            private int frameIndex = 0;
            private int height;
            private int width;
            private boolean canLog = true;
            private EncodedVideoFrameInfo info = new EncodedVideoFrameInfo();
            private Vp8Reader vp8Reader;

            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                vp8Lock.lock();
                try {
                    if (data == null) {
                        return;
                    }

                    if (!taskStarted.get()) {
                        release(false);
                        return;
                    }

                    long currentTime = timestamp;
                    info.setFrameType(lastFrameType);
                    info.setWidth(width);
                    info.setHeight(height);
                    info.setCodecType(Constants.VIDEO_CODEC_VP8);
                    info.setCaptureTimeMs(currentTime);
                    info.setDecodeTimeMs(currentTime);
                    info.setFramesPerSecond(fps);
                    info.setRotation(0);

                    int ret = customEncodedImageSender.sendEncodedVideoImage(data, info);
                    frameIndex++;
                    if (canLog) {
                        SampleLogger
                                .log("send vp8 ret:" + ret + " frame data size:" + data.length + " width:"
                                        + width
                                        + " height:" + height + " lastFrameType:" + lastFrameType + " fps:" + fps +
                                        " timestamp:" + timestamp + " frameIndex:" + frameIndex + " testStartTime:"
                                        + Utils.formatTimestamp(testStartTime)
                                        + " testTime:" + testTime + " from channelId:" + currentChannelId
                                        + " userId:"
                                        + currentUserId);
                        if (isStressTest) {
                            canLog = false;
                        }
                    }
                } finally {
                    vp8Lock.unlock();
                }
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                if (vp8Reader == null) {
                    vp8Reader = threadLocalVp8Reader.get();
                    if (vp8Reader == null) {
                        vp8Reader = new Vp8Reader(filePath);
                        threadLocalVp8Reader.set(vp8Reader);
                    }
                }
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
                vp8Lock.lock();
                try {
                    if (null != vp8Reader) {
                        vp8Reader.close();
                        threadLocalVp8Reader.remove();
                        vp8Reader = null;
                    }
                    info = null;
                } finally {
                    vp8Lock.unlock();
                }
            }
        };

        vp8SendThread.start();

        SampleLogger.log("sendVp8Task start");

        if (waitRelease) {
            try {
                handleWaitRelease(remainingTime);
                vp8SendThread.release(false);
                SampleLogger.log("sendVp8Task end");
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendAvMediaTask(String filePath, int interval) {
        long remainingTime = getRemainingTime();
        SampleLogger.log("sendAvMediaTask filePath:" + filePath + " interval:" + interval + " remainingTime:"
                + remainingTime);

        if (remainingTime == -1) {
            releaseConn();
            return;
        }

        MediaDecodeUtils mediaDecodeUtils = new MediaDecodeUtils();

        boolean initRet = mediaDecodeUtils.init(filePath, interval, -1,
                MediaDecodeUtils.DecodedMediaType.PCM_YUV,
                new MediaDecodeUtils.MediaDecodeCallback() {
                    private ByteBuffer byteBuffer;
                    private final AudioFrame audioFrame = new AudioFrame();

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

                        audioFrame.setBuffer(ByteBuffer.wrap(frame.buffer));
                        audioFrame.setRenderTimeMs(frame.pts);
                        audioFrame.setSamplesPerChannel(frame.samples);
                        audioFrame.setBytesPerSample(frame.bytesPerSample);
                        audioFrame.setChannels(frame.channels);
                        audioFrame.setSamplesPerSec(frame.sampleRate);
                        int ret = audioFrameSender.sendAudioPcmData(audioFrame);
                        SampleLogger.log("SendPcmData frame.pts:" + frame.pts + " ret:" + ret);
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
                        if (!taskStarted.get()) {
                            return;
                        }
                        int ret = videoFrameSender.sendVideoFrame(externalVideoFrame);
                        SampleLogger.log("SendVideoFrame frame.pts:" + frame.pts + " ret:" + ret);
                    }
                });

        SampleLogger.log("sendAvMediaTask initRet:" + initRet);
        if (initRet) {
            mediaDecodeUtils.start();
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
                        SampleLogger.log("sendStreamMessage: " + data + " done ret:" + ret);

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
        long remainingTime = getRemainingTime();
        boolean enableSaveFile = ArgsConfig.enableSaveFile == 1;
        boolean enableVad = ArgsConfig.enableVad == 1;
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        SampleLogger.log("registerPcmObserverTask remoteUserId:" + remoteUserId + " audioOutFile:" + audioOutFile
                + " numOfChannels:"
                + numOfChannels + " sampleRate:" + sampleRate + " enableSaveFile:" + enableSaveFile + " enableVad:"
                + enableVad + " remainingTime:" + remainingTime);

        if (remainingTime == -1) {
            releaseConn();
            return;
        }

        if (!Utils.isNullOrEmpty(remoteUserId)) {
            conn.getLocalUser().subscribeAudio(remoteUserId);
        } else {
            conn.getLocalUser().subscribeAllAudio();
        }

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
                    SampleLogger.log("onPlaybackAudioFrameBeforeMixing frame:" + frame);
                    SampleLogger.log("onPlaybackAudioFrameBeforeMixing audioFrame size " + byteArray.length
                            + " channelId:"
                            + channelId + " userId:" + userId + " with current channelId:"
                            + currentChannelId
                            + " currentUserId:" + currentUserId);
                    if (isStressTest) {
                        canLog = false;
                    }
                }

                if (enableSaveFile) {
                    singleExecutorService.execute(() -> {
                        writeAudioFrameToFile(byteArray);
                    });
                }
                if (null != vadResult) {
                    if (canLog) {
                        SampleLogger.log("onPlaybackAudioFrameBeforeMixing vadResult:" + vadResult);
                        if (isStressTest) {
                            canLog = false;
                        }
                    }
                    singleExecutorService.execute(() -> {
                        writeAudioFrameToFile(vadResult.getOutFrame(), audioOutFile + "_vad.pcm");
                    });
                }
                return 1;
            }

        };
        conn.getLocalUser().registerAudioFrameObserver(audioFrameObserver, enableVad, new AgoraAudioVadConfigV2());

        if (waitRelease) {
            try {
                handleWaitRelease(remainingTime);
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void registerMixedAudioObserverTask(String remoteUserId, String audioOutFile, int numOfChannels,
            int sampleRate,
            boolean waitRelease) {
        long remainingTime = getRemainingTime();
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        SampleLogger.log("registerMixedAudioObserverTask remoteUserId:" + remoteUserId + " audioOutFile:" + audioOutFile
                + " numOfChannels:"
                + numOfChannels + " sampleRate:" + sampleRate + " remainingTime:" + remainingTime);

        if (remainingTime == -1) {
            releaseConn();
            return;
        }

        if (!Utils.isNullOrEmpty(remoteUserId)) {
            conn.getLocalUser().subscribeAudio(remoteUserId);
        } else {
            conn.getLocalUser().subscribeAllAudio();
        }

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
                    SampleLogger.log("onPlaybackAudioFrame frame:" + frame);
                    SampleLogger.log("onPlaybackAudioFrame audioFrame size " + byteArray.length
                            + " channelId:"
                            + channelId + " with current channelId:"
                            + currentChannelId
                            + "  userId:" + currentUserId);
                    if (isStressTest) {
                        canLog = false;
                    }
                }

                singleExecutorService.execute(() -> {
                    writeAudioFrameToFile(byteArray);
                });
                return 1;
            }

        };
        conn.getLocalUser().registerAudioFrameObserver(audioFrameObserver, false, null);

        if (waitRelease) {
            try {
                handleWaitRelease(remainingTime);
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void registerYuvObserverTask(String remoteUserId, String videoOutFile, String streamType,
            boolean waitRelease) {
        long remainingTime = getRemainingTime();
        boolean enableSaveFile = ArgsConfig.enableSaveFile == 1;
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        SampleLogger.log(
                String.format(
                        "registerYuvObserverTask remoteUserId:%s videoOutFile:%s streamType:%s enableSaveFile:%b remainingTime:%d",
                        remoteUserId, videoOutFile, streamType, enableSaveFile, remainingTime));

        if (remainingTime == -1) {
            releaseConn();
            return;
        }

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
        if (!Utils.isNullOrEmpty(remoteUserId)) {
            conn.getLocalUser().subscribeVideo(remoteUserId, subscriptionOptions);
        } else {
            conn.getLocalUser().subscribeAllVideo(subscriptionOptions);
        }

        videoFrameObserver = new AgoraVideoFrameObserver2(new SampleVideFrameObserver(videoOutFile) {
            private boolean canLog = true;
            private int frameCount = 0;
            private long firstFrameTime = 0;

            @Override
            public void onFrame(AgoraVideoFrameObserver2 agoraVideoFrameObserver2, String channelId,
                    String remoteUserId, VideoFrame frame) {
                if (frame == null) {
                    return;
                }

                if (firstFrameTime == 0) {
                    firstFrameTime = System.currentTimeMillis();
                }
                frameCount++;
                long currentTime = System.currentTimeMillis();
                if (currentTime - firstFrameTime >= 2 * 1000) {
                    int fps = (int) (frameCount / ((currentTime - firstFrameTime) / 1000.0));
                    SampleLogger.log("onFrame fps:" + fps
                            + " with current channelId:" + currentChannelId + " with current userId:"
                            + currentUserId);
                    frameCount = 0;
                    firstFrameTime = currentTime;
                }

                int ylength = frame.getYBuffer().remaining();
                int ulength = frame.getUBuffer().remaining();
                int vlength = frame.getVBuffer().remaining();
                if (enableSaveFile) {
                    byte[] data = new byte[ylength + ulength + vlength];
                    ByteBuffer buffer = ByteBuffer.wrap(data);
                    buffer.put(frame.getYBuffer()).put(frame.getUBuffer()).put(frame.getVBuffer());
                    singleExecutorService.execute(() -> {
                        writeVideoFrameToFile(data);
                    });
                }

                final byte[] metaDataBufferData = io.agora.rtc.utils.Utils.getBytes(frame.getMetadataBuffer());
                final byte[] alphaBufferData = io.agora.rtc.utils.Utils.getBytes(frame.getAlphaBuffer());

                if (canLog) {
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
                    if (isStressTest) {
                        canLog = false;
                    }
                }
            }
        });
        conn.getLocalUser().registerVideoFrameObserver(videoFrameObserver);

        if (waitRelease) {
            try {
                handleWaitRelease(remainingTime);
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void registerH264ObserverTask(String remoteUserId, String videoOutFile, String streamType,
            boolean waitRelease) {
        long remainingTime = getRemainingTime();
        boolean enableSaveFile = ArgsConfig.enableSaveFile == 1;
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        SampleLogger.log("registerH264ObserverTask remoteUserId:" + remoteUserId + " videoOutFile:" + videoOutFile +
                " streamType:" + streamType
                + " enableSaveFile:" + enableSaveFile + " remainingTime:" + remainingTime);

        if (remainingTime == -1) {
            releaseConn();
            return;
        }

        VideoSubscriptionOptions subscriptionOptions = new VideoSubscriptionOptions();
        subscriptionOptions.setEncodedFrameOnly(1);
        if ("high".equals(streamType)) {
            subscriptionOptions.setType(Constants.VIDEO_STREAM_HIGH);
        } else if ("low".equals(streamType)) {
            subscriptionOptions.setType(Constants.VIDEO_STREAM_LOW);
        } else {
            return;
        }
        if (!Utils.isNullOrEmpty(remoteUserId)) {
            conn.getLocalUser().subscribeVideo(remoteUserId, subscriptionOptions);
        } else {
            conn.getLocalUser().subscribeAllVideo(subscriptionOptions);
        }

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
                            SampleLogger.log("onEncodedVideoFrame userId:" + userId + " length "
                                    + byteArray.length
                                    + " with current channelId:"
                                    + currentChannelId
                                    + " current userId:" + currentUserId + " info:" + info);
                            if (isStressTest) {
                                canLog = false;
                            }
                        }

                        if (enableSaveFile) {
                            singleExecutorService.execute(() -> {
                                writeVideoDataToFile(byteArray);
                            });
                        }

                        return 1;
                    }
                });
        conn.getLocalUser().registerVideoEncodedFrameObserver(videoEncodedFrameObserver);

        if (waitRelease) {
            try {
                handleWaitRelease(remainingTime);
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void registerEncodedAudioObserverTask(String remoteUserId, String audioOutFile, String fileType,
            boolean waitRelease) {
        long remainingTime = getRemainingTime();
        boolean isStressTest = ArgsConfig.isStressTest == 1;
        SampleLogger
                .log("registerEncodedAudioObserverTask remoteUserId:" + remoteUserId + " audioOutFile:" + audioOutFile +
                        " fileType:" + fileType + " remainingTime:" + remainingTime);

        if (remainingTime == -1) {
            releaseConn();
            return;
        }

        if (!Utils.isNullOrEmpty(remoteUserId)) {
            conn.getLocalUser().subscribeAudio(remoteUserId);
        } else {
            conn.getLocalUser().subscribeAllAudio();
        }

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
                    SampleLogger.log("onEncodedAudioFrameReceived buffer size:" + byteArray.length +
                            " info:" + info + " remoteUserId:" + remoteUserId +
                            " with current channelId:" + currentChannelId);
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
            try {
                handleWaitRelease(remainingTime);
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private long getRemainingTime() {
        if (ArgsConfig.isStressTest == 1) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - testStartTime;
            long maxStressTime = (ArgsConfig.sleepTime - ArgsConfig.timeForStressLeave) * 1000;

            if (elapsedTime < maxStressTime) {
                synchronized (this) {
                    long remainingTime;
                    if (elapsedTime + testTime * 1000 < maxStressTime) {
                        remainingTime = testTime * 1000;
                    } else {
                        remainingTime = maxStressTime - elapsedTime;
                    }
                    return remainingTime;
                }
            } else {
                SampleLogger.log(String.format(
                        "handleWaitRelease testTime:%d testStartTime:%d currentTime:%d " +
                                "timeForStressLeave:%d and now release conn",
                        testTime, testStartTime, currentTime, ArgsConfig.timeForStressLeave));
                return -1;
            }
        } else {
            return testTime * 1000;
        }
    }

    private synchronized void handleWaitRelease(long remainingTime) {
        try {
            if (testTime > 0) {
                wait(remainingTime);
            } else {
                if (null == taskFinishLatch) {
                    taskFinishLatch = new CountDownLatch(1);
                }
                taskFinishLatch.await();
            }
            taskStarted.set(false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}