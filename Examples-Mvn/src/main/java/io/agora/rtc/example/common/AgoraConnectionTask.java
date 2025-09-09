package io.agora.rtc.example.common;

import io.agora.rtc.AgoraAudioVadConfigV2;
import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AgoraParameter;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraVideoEncodedFrameObserver;
import io.agora.rtc.AgoraVideoFrameObserver2;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.Constants;
import io.agora.rtc.DownlinkNetworkInfo;
import io.agora.rtc.EncodedAudioFrameInfo;
import io.agora.rtc.EncodedAudioFrameReceiverInfo;
import io.agora.rtc.EncodedVideoFrameInfo;
import io.agora.rtc.EncryptionConfig;
import io.agora.rtc.ExternalVideoFrame;
import io.agora.rtc.IAudioEncodedFrameObserver;
import io.agora.rtc.IAudioFrameObserver;
import io.agora.rtc.ILocalUserObserver;
import io.agora.rtc.INetworkObserver;
import io.agora.rtc.IRtcConnObserver;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.RtcConnPublishConfig;
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
import io.agora.rtc.example.utils.AudioFrameManager;
import io.agora.rtc.example.utils.DirectBufferCleaner;
import io.agora.rtc.example.utils.Utils;
import java.io.FileInputStream;
import java.io.IOException;
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
    private final long testStartTime;

    private TaskCallback callback;
    private AgoraRtcConn conn;

    private IAudioFrameObserver audioFrameObserver;
    private IAudioEncodedFrameObserver audioEncodedFrameObserver;
    private AgoraVideoFrameObserver2 videoFrameObserver;
    private AgoraVideoEncodedFrameObserver videoEncodedFrameObserver;

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
    private static final ThreadLocal<H264Reader> threadLocalH264ReaderLow = new ThreadLocal<>();
    private static final ThreadLocal<Vp8Reader> threadLocalVp8Reader = new ThreadLocal<>();
    private static final ThreadLocal<OpusReader> threadLocalOpusReader = new ThreadLocal<>();
    private static final ThreadLocal<AacReader> threadLocalAacReader = new ThreadLocal<>();

    private ArgsConfig argsConfig;
    private AudioFrameManager audioFrameManager;

    private static final String TEST_PASS = "pass";
    private static final String TEST_START = "start";

    public interface TaskCallback {
        default void onConnected(String userId) {
        }

        default void onUserJoined(String userId) {
        }

        default void onUserLeft(String userId) {
        }

        default void onStreamMessage(String userId, int streamId, byte[] data) {
        }

        default void onTestFinished() {
        }

        default void onTestTaskStart() {

        }
    }

    public AgoraConnectionTask(AgoraService service, ArgsConfig argsConfig) {
        this.service = service;
        this.argsConfig = argsConfig.deepClone();
        this.testStartTime = System.currentTimeMillis();
        this.singleExecutorService = Executors.newSingleThreadExecutor();
        this.testTaskExecutorService = new ThreadPoolExecutor(
                0, Integer.MAX_VALUE, 1L, TimeUnit.SECONDS, new SynchronousQueue<>());
    }

    public void setCallback(TaskCallback callback) {
        this.callback = callback;
    }

    public AgoraRtcConn getConn() {
        return conn;
    }

    public void createConnection(
            RtcConnConfig ccfg, RtcConnPublishConfig publishConfig, boolean needConnect) {
        SampleLogger.log("createConnection ccfg:" + ccfg + " publishConfig:" + publishConfig
                + " needConnect:" + needConnect);
        if (null == service) {
            SampleLogger.log("createAndInitAgoraService fail");
            return;
        }
        taskStarted.set(false);

        audioFrameManager = new AudioFrameManager(new AudioFrameManager.ICallback() {
            @Override
            public void onSessionEnd(int sessionId, AudioFrameManager.SessionEndReason reason) {
                SampleLogger.log("onSessionEnd sessionId:" + sessionId + " reason:" + reason);
                if (argsConfig.isEnableAssistantDevice()) {
                    if (null != taskFinishLatch) {
                        taskFinishLatch.countDown();
                    }
                }
            }
        });

        conn = service.agoraRtcConnCreate(ccfg, publishConfig);
        if (conn == null) {
            SampleLogger.log("AgoraService.agoraRtcConnCreate fail");
            return;
        }

        int ret = conn.registerObserver(new IRtcConnObserver() {
            @Override
            public void onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason) {
                taskStarted.set(true);
                String currentUserId = connInfo.getLocalUserId();
                argsConfig.setUserId(currentUserId);
                SampleLogger.log("onConnected reason:" + reason + " in channelId:"
                        + argsConfig.getChannelId() + " userId:" + argsConfig.getUserId());
                if (null != callback) {
                    callback.onConnected(currentUserId);
                }
            }

            @Override
            public void onDisconnected(
                    AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason) {
                if (null != connDisconnectedLatch) {
                    connDisconnectedLatch.countDown();
                }
            }

            @Override
            public void onUserJoined(AgoraRtcConn agoraRtcConn, String userId) {
                SampleLogger.log("onUserJoined userId:" + userId + " in channelId:"
                        + argsConfig.getChannelId() + " userId:" + argsConfig.getUserId());
                if (null != callback) {
                    callback.onUserJoined(userId);
                }
            }

            @Override
            public void onUserLeft(AgoraRtcConn agoraRtcConn, String userId, int reason) {
                SampleLogger.log("onUserLeft userId:" + userId + " reason:" + reason);
                if (argsConfig.getTestTime() == 0) {
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

            @Override
            public int onAIQoSCapabilityMissing(
                    AgoraRtcConn agoraRtcConn, int defaultFallbackSenario) {
                SampleLogger.log(
                        "onAIQoSCapabilityMissing defaultFallbackSenario:" + defaultFallbackSenario);

                if (argsConfig.getAudioOutFile() != null
                        && !argsConfig.getAudioOutFile().isEmpty()) {
                    testTaskExecutorService.execute(() -> {
                        Utils.appendStringToFile("onAIQoSCapabilityMissing defaultFallbackSenario:"
                                + defaultFallbackSenario,
                                argsConfig.getAudioOutFile() + "_ai_qos_capability_missing.txt");
                    });
                }
                return defaultFallbackSenario;
            }
        });
        SampleLogger.log("registerObserver ret:" + ret + " for channelId:"
                + argsConfig.getChannelId() + " userId:" + argsConfig.getUserId());

        ret = conn.registerNetworkObserver(new INetworkObserver() {
            @Override
            public void onUplinkNetworkInfoUpdated(
                    AgoraRtcConn agoraRtcConn, UplinkNetworkInfo info) {
                if (agoraRtcConn == null || agoraRtcConn.getConnInfo() == null) {
                    return;
                }
                if (!argsConfig.isEnableStressTest()) {
                    SampleLogger.log("onUplinkNetworkInfoUpdated info:" + info);
                }
            }

            @Override
            public void onDownlinkNetworkInfoUpdated(
                    AgoraRtcConn agoraRtcConn, DownlinkNetworkInfo info) {
                if (agoraRtcConn == null || agoraRtcConn.getConnInfo() == null) {
                    return;
                }
                if (!argsConfig.isEnableStressTest()) {
                    SampleLogger.log("onDownlinkNetworkInfoUpdated info:" + info);
                }
            }
        });
        SampleLogger.log("registerNetworkObserver ret:" + ret + " for channelId:"
                + argsConfig.getChannelId() + " userId:" + argsConfig.getUserId());

        if (argsConfig.isEnableEncryptionMode()
                && !Utils.isNullOrEmpty(argsConfig.getEncryptionKey())) {
            EncryptionConfig encryptionConfig = new EncryptionConfig();
            encryptionConfig.setEncryptionMode(argsConfig.getEncryptionMode());
            encryptionConfig.setEncryptionKey(argsConfig.getEncryptionKey());
            ret = conn.enableEncryption(
                    argsConfig.isEnableEncryptionMode() ? 1 : 0, encryptionConfig);
            if (ret < 0) {
                SampleLogger.log("Failed to enable encryption ret:" + ret);
                return;
            }
            SampleLogger.log("Enable encryption successfully! for channelId:"
                    + argsConfig.getChannelId() + " userId:" + argsConfig.getUserId());
        }

        if (argsConfig.isEnableCloudProxy()) {
            AgoraParameter agoraParameter = conn.getAgoraParameter();
            ret = agoraParameter.setBool("rtc.enable_proxy", true);
            SampleLogger.log("setBool rtc.enable_proxy ret:" + ret + " for channelId:"
                    + argsConfig.getChannelId() + " userId:" + argsConfig.getUserId());
        }
        // conn.getLocalUser().setAudioVolumeIndicationParameters(50, 3, true);

        // Register local user observer
        conn.registerLocalUserObserver(new ILocalUserObserver() {
            @Override
            public void onStreamMessage(
                    AgoraLocalUser agoraLocalUser, String userId, int streamId, byte[] data) {
                if (!argsConfig.isEnableStressTest()) {
                    SampleLogger.log(
                            "onStreamMessage success userId:" + userId + " data:" + new String(data));
                    if (argsConfig.isEnableRecvDataStream() && argsConfig.getAudioOutFile() != null
                            && !argsConfig.getAudioOutFile().isEmpty()) {
                        singleExecutorService.execute(() -> {
                            Utils.appendStringToFile(new String(data) + "\n",
                                    argsConfig.getAudioOutFile() + "_" + argsConfig.getChannelId() + ".txt");
                        });
                    } else {
                        String streamMessage = new String(data);
                        SampleLogger.log("onStreamMessage receive message: " + streamMessage);
                        if (streamMessage.equalsIgnoreCase(TEST_PASS)) {
                            if (null != taskFinishLatch) {
                                taskFinishLatch.countDown();
                            }
                        } else if (streamMessage.equalsIgnoreCase(TEST_START)) {
                            if (null != callback) {
                                callback.onTestTaskStart();
                            }
                        }
                    }
                }
            }

            @Override
            public void onAudioMetaDataReceived(
                    AgoraLocalUser agoraLocalUser, String userId, byte[] metaData) {
                if (!argsConfig.isEnableStressTest()) {
                    SampleLogger.log("onAudioMetaDataReceived userId:" + userId
                            + " metaData:" + new String(metaData) + " " + argsConfig.getAudioOutFile());
                    if (argsConfig.getAudioOutFile() != null && !argsConfig.getAudioOutFile().isEmpty()) {
                        singleExecutorService.execute(() -> {
                            Utils.appendStringToFile(new String(metaData),
                                    argsConfig.getAudioOutFile() + "_audio_meta_data.txt");
                        });
                    }
                }
            }
        });

        if (needConnect) {
            connConnect();
        }
    }

    public void sendTestTaskMessage(String testTaskName) {
        SampleLogger.log("sendTestTaskMessage testTaskName:" + testTaskName);
        if (null != conn) {
            conn.sendStreamMessage(testTaskName.getBytes());
        }
    }

    private void connConnect() {
        if (taskStarted.get()) {
            SampleLogger.log("connConnect already started, skipping");
            return;
        }
        int ret = conn.connect(argsConfig.getToken(), argsConfig.getChannelId(), argsConfig.getUserId());
        SampleLogger.log("Connecting to Agora channel " + argsConfig.getChannelId()
                + " with userId " + argsConfig.getUserId() + " ret:" + ret);
        if (ret != 0) {
            SampleLogger.log("conn.connect fail ret=" + ret);
            releaseConn();
            return;
        }
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
            SampleLogger.log("releaseConn for channelId:" + argsConfig.getChannelId()
                    + " userId:" + argsConfig.getUserId());
            if (conn == null) {
                return;
            }

            taskStarted.set(false);

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
                conn.destroy();
            } catch (Exception e) {
                SampleLogger.error("Error destroying connection: " + e.getMessage());
            } finally {
                conn = null;
            }

            taskFinishLatch = null;

            if (null != audioFrameManager) {
                audioFrameManager.release();
                audioFrameManager = null;
            }

            try {
                singleExecutorService.shutdownNow();
                testTaskExecutorService.shutdownNow();
            } catch (Exception e) {
                SampleLogger.error("Error shutting down executor services: " + e.getMessage());
            }

            SampleLogger.log("destroy conn for channelId:" + argsConfig.getChannelId()
                    + " userId:" + argsConfig.getUserId());
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

    public void sendPcmTask(boolean waitRelease, boolean isAiServerMode) {
        int intervalMs = 10; // ms
        long remainingTime = getRemainingTime();
        SampleLogger.log("sendPcmTask remainingTime:" + remainingTime
                + " isAiServerMode:" + isAiServerMode + " argsConfig:" + argsConfig);

        if (remainingTime == -1) {
            if (waitRelease) {
                releaseConn();
            }
            return;
        }

        conn.publishAudio();
        waitUntilPublishSuccess();
        FileSender pcmSendThread = null;
        SampleLogger.log("sendPcmTask start");
        if (isAiServerMode) {
            byte[] pcmData = Utils.readPcmFromFile(argsConfig.getAudioFile());
            // The data length must be an integer multiple of the data length of 1ms.
            // Assuming 16-bit samples (2 bytes per sample).
            int bytesPerMs = argsConfig.getNumOfChannels() * (argsConfig.getSampleRate() / 1000) * 2;
            if (bytesPerMs > 0 && pcmData.length % bytesPerMs != 0) {
                int newLength = (pcmData.length / bytesPerMs) * bytesPerMs;
                SampleLogger.log(String.format("sendPcmTask: pcmData length is not a multiple of "
                        + "1ms data bytes. Truncating from %d to %d bytes.",
                        pcmData.length, newLength));
                pcmData = Arrays.copyOf(pcmData, newLength);
            }

            long pts = 0;
            if (argsConfig.isEnableAssistantDevice()) {
                pts = audioFrameManager.generateDownlinkPts(pcmData, argsConfig.getSampleRate(),
                        argsConfig.getNumOfChannels(), true);
            }
            int ret = conn.pushAudioPcmData(pcmData, argsConfig.getSampleRate(),
                    argsConfig.getNumOfChannels(), pts);
            if (ret < 0) {
                SampleLogger.log("sendPcmTask pushAudioPcmData fail ret:" + ret);
            } else {
                SampleLogger.log("sendPcmTask pushAudioPcmData with pts:" + pts + " success ret:" + ret);
            }
        } else {
            int oneFramePcmDataSize = argsConfig.getNumOfChannels()
                    * (argsConfig.getSampleRate() / 1000) * intervalMs * 2;
            // one frame pcm data size
            byte[] buffer = new byte[oneFramePcmDataSize];

            // first cache 10 frames data for 10ms interval, avoid sending silence packet
            // due to
            // insufficient data in sdk
            ByteBuffer cachePcmDataBuffer = ByteBuffer.allocate(oneFramePcmDataSize * 10);
            boolean isLoopSend = argsConfig.getTestTime() > 0 || !waitRelease;
            pcmSendThread = new FileSender(argsConfig.getAudioFile(), intervalMs) {
                private boolean canLog = argsConfig.isEnableLog();

                @Override
                public void sendOneFrame(byte[] data, long timestamp) {
                    pcmLock.lock();
                    try {
                        if (taskStarted.get()) {
                            if (null != data) {
                                int ret = conn.pushAudioPcmData(data, argsConfig.getSampleRate(),
                                        argsConfig.getNumOfChannels());
                                if (ret != 0) {
                                    SampleLogger.log("pushAudioPcmData fail ret:" + ret);
                                }
                                if (canLog) {
                                    SampleLogger.log("send pcm data size:" + data.length
                                            + " timestamp:" + timestamp
                                            + " to channelId:" + argsConfig.getChannelId()
                                            + " from userId:" + argsConfig.getUserId());
                                    if (argsConfig.isEnableStressTest()) {
                                        canLog = false;
                                    }
                                }
                            }

                            if (argsConfig.isEnableSendAudioMetaData()) {
                                String audioMetaData = "testSendAudioMetaData " + timestamp;
                                int ret = conn.sendAudioMetaData(audioMetaData.getBytes());
                                if (canLog) {
                                    SampleLogger.log(
                                            "sendAudioMetaData: " + audioMetaData + " ret:" + ret);
                                    if (argsConfig.isEnableStressTest()) {
                                        canLog = false;
                                    }
                                }
                            }
                        } else {
                            release();
                        }
                    } finally {
                        pcmLock.unlock();
                    }
                }

                @Override
                public byte[] readOneFrame(FileInputStream fos) {
                    if (fos != null) {
                        try {
                            int size = fos.read(buffer, 0, oneFramePcmDataSize);
                            if (size < 0) {
                                if (isLoopSend) {
                                    reset();
                                } else {
                                    if (null != taskFinishLatch) {
                                        taskFinishLatch.countDown();
                                    }
                                }
                                return null;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (cachePcmDataBuffer.position() < cachePcmDataBuffer.capacity()) {
                        cachePcmDataBuffer.put(buffer);
                        if (cachePcmDataBuffer.position() < cachePcmDataBuffer.capacity()) {
                            return null;
                        } else {
                            return cachePcmDataBuffer.array();
                        }
                    } else {
                        return buffer;
                    }
                }

                @Override
                public void release() {
                    super.release();
                    pcmLock.lock();
                    try {
                        cachePcmDataBuffer.clear();
                    } finally {
                        pcmLock.unlock();
                    }
                }
            };

            testTaskExecutorService.execute(pcmSendThread);
        }

        if (waitRelease) {
            try {
                handleWaitRelease(remainingTime);
                if (pcmSendThread != null) {
                    pcmSendThread.release();
                }
                SampleLogger.log("sendPcmTask end");
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendAacTask(boolean waitRelease) {
        int intervalMs = 20; // ms
        long remainingTime = getRemainingTime();
        SampleLogger.log(
                "sendAacTask remainingTime:" + remainingTime + " argsConfig:" + argsConfig);

        if (remainingTime == -1) {
            if (waitRelease) {
                releaseConn();
            }
            return;
        }

        // Create audio track
        conn.publishAudio();
        waitUntilPublishSuccess();
        boolean isLoopSend = argsConfig.getTestTime() > 0 || !waitRelease;
        FileSender aacSendThread = new FileSender(argsConfig.getAudioFile(), intervalMs) {
            private boolean canLog = argsConfig.isEnableLog();
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
                        release();
                        return;
                    }
                    int ret = conn.pushAudioEncodedData(data, encodedInfo);
                    if (canLog) {
                        SampleLogger.log("send aac frame data size:" + data.length
                                + " timestamp:" + timestamp + " encodedInfo:" + encodedInfo
                                + " to channelId:" + argsConfig.getChannelId()
                                + " from userId:" + argsConfig.getUserId() + " ret:" + ret
                                + " testStartTime:" + Utils.formatTimestamp(testStartTime)
                                + " testTime:" + argsConfig.getTestTime());
                        if (argsConfig.isEnableStressTest()) {
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
                        aacReader = new AacReader(argsConfig.getAudioFile());
                        threadLocalAacReader.set(aacReader);
                    }
                }
                AacReader.AacFrame aacFrame = aacReader.getAudioFrame(intervalMs);
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
            public void release() {
                super.release();
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

        testTaskExecutorService.execute(aacSendThread);

        SampleLogger.log("sendAacTask start");

        if (waitRelease) {
            try {
                handleWaitRelease(remainingTime);
                aacSendThread.release();
                SampleLogger.log("sendAacTask end");
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendOpusTask(boolean waitRelease) {
        int intervalMs = 20; // ms
        long remainingTime = getRemainingTime();
        SampleLogger.log(
                "sendOpusTask remainingTime:" + remainingTime + " argsConfig:" + argsConfig);

        if (remainingTime == -1) {
            if (waitRelease) {
                releaseConn();
            }
            return;
        }

        // Create audio track
        conn.publishAudio();
        waitUntilPublishSuccess();
        boolean isLoopSend = argsConfig.getTestTime() > 0 || !waitRelease;
        FileSender opusSendThread = new FileSender(argsConfig.getAudioFile(), intervalMs) {
            private boolean canLog = argsConfig.isEnableLog();
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
                        release();
                        return;
                    }

                    int ret = conn.pushAudioEncodedData(data, encodedInfo);
                    if (canLog) {
                        SampleLogger.log("send opus frame data size:" + data.length
                                + " timestamp:" + timestamp + " encodedInfo:" + encodedInfo
                                + " to channelId:" + argsConfig.getChannelId()
                                + " from userId:" + argsConfig.getUserId() + " ret:" + ret
                                + " testStartTime:" + Utils.formatTimestamp(testStartTime)
                                + " testTime:" + argsConfig.getTestTime());
                        if (argsConfig.isEnableStressTest()) {
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
                        opusReader = new OpusReader(argsConfig.getAudioFile());
                        threadLocalOpusReader.set(opusReader);
                    }
                }
                io.agora.rtc.example.mediautils.AudioFrame opusFrame = opusReader.getAudioFrame(intervalMs);
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
            public void release() {
                super.release();
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
        testTaskExecutorService.execute(opusSendThread);

        SampleLogger.log("sendOpusTask start");

        if (waitRelease) {
            try {
                handleWaitRelease(remainingTime);
                opusSendThread.release();
                SampleLogger.log("sendOpusTask end");
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendYuvTask(boolean waitRelease) {
        int intervalMs = 1000 / argsConfig.getFps(); // ms
        long remainingTime = getRemainingTime();
        SampleLogger.log("sendYuvTask remainingTime:" + remainingTime + " argsConfig:" + argsConfig
                + " intervalMs:" + intervalMs);

        if (remainingTime == -1) {
            if (waitRelease) {
                releaseConn();
            }
            return;
        }

        VideoEncoderConfig config = new VideoEncoderConfig();
        config.setCodecType(Constants.VIDEO_CODEC_H264);
        config.setDimensions(new VideoDimensions(argsConfig.getWidth(), argsConfig.getHeight()));
        config.setFrameRate(argsConfig.getFps());
        config.setEncodeAlpha(argsConfig.isEnableAlpha() ? 1 : 0);
        conn.setVideoEncoderConfig(config);
        if (argsConfig.isEnableSimulcastStream()) {
            VideoDimensions lowDimensions = new VideoDimensions(argsConfig.getLowWidth(), argsConfig.getLowHeight());
            SimulcastStreamConfig lowStreamConfig = new SimulcastStreamConfig();
            lowStreamConfig.setDimensions(lowDimensions);
            lowStreamConfig.setFramerate(argsConfig.getLowFps());
            // lowStreamConfig.setBitrate(targetBitrate/2);
            int ret = conn.enableSimulcastStream(1, lowStreamConfig);
            SampleLogger.log("sendYuvTask enableSimulcastStream ret:" + ret);
        }

        // Publish video track
        conn.publishVideo();
        waitUntilPublishSuccess();

        int bufferLen = (int) (argsConfig.getHeight() * argsConfig.getWidth() * 1.5);
        byte[] buffer = new byte[bufferLen];

        boolean isLoopSend = argsConfig.getTestTime() > 0 || !waitRelease;
        FileSender yuvSender = new FileSender(argsConfig.getVideoFile(), intervalMs) {
            private int frameIndex = 0;
            private ByteBuffer byteBuffer;
            private ByteBuffer matedataByteBuffer;
            private ByteBuffer alphaByteBuffer;
            private boolean canLog = argsConfig.isEnableLog();
            private ExternalVideoFrame externalVideoFrame = new ExternalVideoFrame();

            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                yuvLock.lock();
                try {
                    if (data == null) {
                        return;
                    }

                    if (!taskStarted.get()) {
                        release();
                        return;
                    }

                    externalVideoFrame.setHeight(argsConfig.getHeight());
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
                    externalVideoFrame.setStride(argsConfig.getWidth());
                    externalVideoFrame.setType(Constants.EXTERNAL_VIDEO_FRAME_BUFFER_TYPE_RAW_DATA);

                    if (argsConfig.isEnableSendVideoMetaData()) {
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
                    }

                    if (argsConfig.isEnableAlpha()) {
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

                    if (canLog) {
                        SampleLogger.log("send yuv frame data size:" + data.length + "  ret:" + ret
                                + " timestamp:" + timestamp + " frameIndex:" + frameIndex
                                + " testStartTime:" + Utils.formatTimestamp(testStartTime)
                                + " testTime:" + argsConfig.getTestTime() + " from channelId:"
                                + argsConfig.getChannelId() + " userId:" + argsConfig.getUserId());
                        if (argsConfig.isEnableStressTest()) {
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
            public void release() {
                super.release();
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
        testTaskExecutorService.execute(yuvSender);

        SampleLogger.log("sendYuvTask start");

        if (waitRelease) {
            try {
                handleWaitRelease(remainingTime);
                yuvSender.release();
                SampleLogger.log("sendYuvTask end");
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendH264Task(boolean waitRelease) {
        int intervalMs = 1000 / argsConfig.getFps(); // ms
        long remainingTime = getRemainingTime();
        SampleLogger.log("sendH264Task remainingTime:" + remainingTime + " argsConfig:" + argsConfig
                + " intervalMs:" + intervalMs);

        if (remainingTime == -1) {
            if (waitRelease) {
                releaseConn();
            }
            return;
        }

        if (argsConfig.isEnableSimulcastStream()) {
            SimulcastStreamConfig lowStreamConfig = new SimulcastStreamConfig();
            // lowStreamConfig.setBitrate(65);
            lowStreamConfig.setFramerate(argsConfig.getLowFps());
            VideoDimensions dimensions = new VideoDimensions(argsConfig.getLowWidth(), argsConfig.getLowHeight());
            lowStreamConfig.setDimensions(dimensions);
            int ret = conn.enableSimulcastStream(1, lowStreamConfig);
            SampleLogger.log("sendH264Task enableSimulcastStream ret:" + ret);
        }

        // Publish video track
        int ret = conn.publishVideo();
        SampleLogger.log("sendH264Task publishVideo ret:" + ret);
        waitUntilPublishSuccess();

        final int streamType = getStreamType(argsConfig.getStreamType());

        boolean isLoopSend = argsConfig.getTestTime() > 0 || !waitRelease;
        FileSender h264SendThread = new FileSender(argsConfig.getVideoFile(), intervalMs) {
            int lastFrameType = 0;
            int frameIndex = 0;
            private boolean canLog = argsConfig.isEnableLog();
            private EncodedVideoFrameInfo info = new EncodedVideoFrameInfo();
            private H264Reader localH264Reader;
            private int h264Width = 0;
            private int h264Height = 0;

            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                h264Lock.lock();
                try {
                    if (data == null) {
                        return;
                    }
                    if (!taskStarted.get()) {
                        release();
                        return;
                    }

                    info.setFrameType(lastFrameType);
                    info.setStreamType(streamType);
                    info.setWidth(h264Width);
                    info.setHeight(h264Height);
                    info.setCodecType(Constants.VIDEO_CODEC_H264);
                    info.setFramesPerSecond(argsConfig.getFps());
                    info.setRotation(0);

                    int ret = conn.pushVideoEncodedData(data, info);
                    frameIndex++;
                    if (canLog) {
                        SampleLogger.log("send h264 frame data size:" + data.length + " info:"
                                + info + " ret:" + ret + " timestamp:" + timestamp + " frameIndex:"
                                + frameIndex + " testStartTime:" + Utils.formatTimestamp(testStartTime)
                                + " testTime:" + argsConfig.getTestTime() + " from channelId:"
                                + argsConfig.getChannelId() + " userId:" + argsConfig.getUserId());
                        if (argsConfig.isEnableStressTest()) {
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
                        localH264Reader = new H264Reader(argsConfig.getVideoFile());
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
                h264Width = frame.width;
                h264Height = frame.height;
                return frame.data;
            }

            @Override
            public void release() {
                super.release();
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

        FileSender h264SendThreadLow = null;
        if (argsConfig.isEnableSimulcastStream() && argsConfig.getLowVideoFile() != null
                && argsConfig.getLowVideoFile().length() > 0) {
            h264SendThreadLow = new FileSender(argsConfig.getLowVideoFile(), intervalMs) {
                int lastFrameType = 0;
                int frameIndex = 0;
                private boolean canLog = argsConfig.isEnableLog();
                private EncodedVideoFrameInfo info = new EncodedVideoFrameInfo();
                private H264Reader localH264Reader;
                private int h264Width = 0;
                private int h264Height = 0;

                @Override
                public void sendOneFrame(byte[] data, long timestamp) {
                    h264Lock.lock();
                    try {
                        if (data == null) {
                            return;
                        }
                        if (!taskStarted.get()) {
                            release();
                            return;
                        }

                        info.setFrameType(lastFrameType);
                        info.setStreamType(Constants.VIDEO_STREAM_LOW);
                        info.setWidth(h264Width);
                        info.setHeight(h264Height);
                        info.setCodecType(Constants.VIDEO_CODEC_H264);
                        info.setFramesPerSecond(argsConfig.getLowFps());
                        info.setRotation(0);

                        int ret = conn.pushVideoEncodedData(data, info);
                        frameIndex++;
                        if (canLog) {
                            SampleLogger.log("send h264 low frame data size:" + data.length
                                    + " info:" + info + " ret:" + ret + " timestamp:" + timestamp
                                    + " frameIndex:" + frameIndex
                                    + " testStartTime:" + Utils.formatTimestamp(testStartTime)
                                    + " testTime:" + argsConfig.getTestTime() + " from channelId:"
                                    + argsConfig.getChannelId() + " userId:" + argsConfig.getUserId());
                            if (argsConfig.isEnableStressTest()) {
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
                        localH264Reader = threadLocalH264ReaderLow.get();
                        if (localH264Reader == null) {
                            localH264Reader = new H264Reader(argsConfig.getLowVideoFile());
                            threadLocalH264ReaderLow.set(localH264Reader);
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
                    h264Width = frame.width;
                    h264Height = frame.height;
                    return frame.data;
                }

                @Override
                public void release() {
                    super.release();
                    h264Lock.lock();
                    try {
                        if (localH264Reader != null) {
                            localH264Reader.close();
                            threadLocalH264ReaderLow.remove();
                            localH264Reader = null;
                        }
                        info = null;
                    } finally {
                        h264Lock.unlock();
                    }
                }
            };
        }

        testTaskExecutorService.execute(h264SendThread);
        if (h264SendThreadLow != null) {
            testTaskExecutorService.execute(h264SendThreadLow);
        }

        SampleLogger.log("sendH264Task start");

        if (waitRelease) {
            try {
                handleWaitRelease(remainingTime);
                h264SendThread.release();
                if (h264SendThreadLow != null) {
                    h264SendThreadLow.release();
                }
                SampleLogger.log("sendH264Task end");
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendRgbaTask(boolean waitRelease) {
        int intervalMs = 1000 / argsConfig.getFps(); // ms
        long remainingTime = getRemainingTime();
        SampleLogger.log("sendRgbaTask remainingTime:" + remainingTime + " argsConfig:" + argsConfig
                + " intervalMs:" + intervalMs);

        if (remainingTime == -1) {
            if (waitRelease) {
                releaseConn();
            }
            return;
        }

        VideoEncoderConfig config = new VideoEncoderConfig();
        config.setCodecType(Constants.VIDEO_CODEC_H264);
        config.setDimensions(new VideoDimensions(argsConfig.getWidth(), argsConfig.getHeight()));
        config.setFrameRate(argsConfig.getFps());
        conn.setVideoEncoderConfig(config);
        conn.publishVideo();
        waitUntilPublishSuccess();

        boolean isLoopSend = argsConfig.getTestTime() > 0 || !waitRelease;
        FileSender rgbaSender = new FileSender(argsConfig.getVideoFile(), intervalMs) {
            // For RGBA, each pixel takes 4 bytes.
            int bufferLen = argsConfig.getHeight() * argsConfig.getWidth() * 4;
            byte[] buffer = new byte[bufferLen];

            private int frameIndex = 0;
            ByteBuffer byteBuffer;
            ByteBuffer alphaBuffer;
            byte[] alphadata;
            private boolean canLog = argsConfig.isEnableLog();
            private ExternalVideoFrame externalVideoFrame = new ExternalVideoFrame();

            public byte[] extractAlphaChannel(byte[] rgbaData, int width, int height) {
                int pixelCount = width * height;
                byte[] alphaData = new byte[pixelCount]; // Alpha channel data, 1 byte per pixel

                for (int i = 0; i < pixelCount; i++) {
                    alphaData[i] = rgbaData[4 * i + 3]; // The A component in RGBA
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
                        release();
                        return;
                    }

                    // Allocate memory directly through ByteBuffer, ensure it is a
                    // DirectByteBuffer, meet the requirements of Agora SDK
                    if (byteBuffer == null) {
                        byteBuffer = ByteBuffer.allocateDirect(data.length);
                    }
                    if (byteBuffer == null || byteBuffer.limit() < data.length) {
                        return;
                    }
                    byteBuffer.put(data);
                    byteBuffer.flip(); // Reset position, so that Agora SDK can start reading
                                       // data from the beginning

                    // Extract Alpha channel data
                    byte[] alphaData = extractAlphaChannel(data, argsConfig.getWidth(), argsConfig.getHeight());
                    // Create a new DirectByteBuffer to store Alpha channel data
                    if (alphaBuffer == null) {
                        alphaBuffer = ByteBuffer.allocateDirect(alphaData.length);
                    }
                    alphaBuffer.put(alphaData);
                    alphaBuffer.flip(); // Reset position, so that Agora SDK can start reading
                                        // data from the beginning

                    externalVideoFrame.setType(Constants.EXTERNAL_VIDEO_FRAME_BUFFER_TYPE_RAW_DATA);
                    externalVideoFrame.setFormat(Constants.EXTERNAL_VIDEO_FRAME_PIXEL_FORMAT_RGBA);
                    externalVideoFrame.setStride(argsConfig.getWidth());
                    externalVideoFrame.setHeight(argsConfig.getHeight());
                    externalVideoFrame.setBuffer(byteBuffer);
                    externalVideoFrame.setAlphaBuffer(alphaBuffer);
                    externalVideoFrame.setRotation(0);
                    // ColorSpace colorSpace = new ColorSpace();
                    // colorSpace.setPrimaries(1);
                    // colorSpace.setTransfer(1);
                    // colorSpace.setMatrix(5);
                    // colorSpace.setRange(1);
                    // externalVideoFrame.setColorSpace(colorSpace);

                    int ret = conn.pushVideoFrame(externalVideoFrame);
                    frameIndex++;

                    if (canLog) {
                        SampleLogger.log("send rgba frame data size:" + data.length + " ret:" + ret
                                + " timestamp:" + timestamp + " frameIndex:" + frameIndex
                                + " testStartTime:" + Utils.formatTimestamp(testStartTime)
                                + " testTime:" + argsConfig.getTestTime());
                        if (argsConfig.isEnableStressTest()) {
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
            public void release() {
                super.release();
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
        testTaskExecutorService.execute(rgbaSender);

        SampleLogger.log("sendRgbaTask start");

        if (waitRelease) {
            try {
                handleWaitRelease(remainingTime);
                rgbaSender.release();
                SampleLogger.log("sendRgbaTask end");
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendVp8Task(boolean waitRelease) {
        int intervalMs = 1000 / argsConfig.getFps(); // ms
        long remainingTime = getRemainingTime();
        SampleLogger.log("sendVp8Task remainingTime:" + remainingTime + " argsConfig:" + argsConfig
                + " intervalMs:" + intervalMs);

        if (remainingTime == -1) {
            if (waitRelease) {
                releaseConn();
            }
            return;
        }

        if (argsConfig.isEnableSimulcastStream()) {
            SimulcastStreamConfig lowStreamConfig = new SimulcastStreamConfig();
            // lowStreamConfig.setBitrate(65);
            lowStreamConfig.setFramerate(argsConfig.getLowFps());
            VideoDimensions dimensions = new VideoDimensions(argsConfig.getLowWidth(), argsConfig.getLowHeight());
            lowStreamConfig.setDimensions(dimensions);
            conn.enableSimulcastStream(1, lowStreamConfig);
        }

        VideoEncoderConfig config = new VideoEncoderConfig();
        config.setCodecType(Constants.VIDEO_CODEC_VP8);
        conn.setVideoEncoderConfig(config);

        // Publish video track
        int ret = conn.publishVideo();
        waitUntilPublishSuccess();
        SampleLogger.log("sendVp8Task publishVideo ret:" + ret);

        boolean isLoopSend = argsConfig.getTestTime() > 0 || !waitRelease;
        FileSender vp8SendThread = new FileSender(argsConfig.getVideoFile(), intervalMs, false) {
            private int lastFrameType = 0;
            private int frameIndex = 0;
            private int height;
            private int width;
            private boolean canLog = argsConfig.isEnableLog();
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
                        release();
                        return;
                    }

                    long currentTime = timestamp;
                    info.setFrameType(lastFrameType);
                    info.setWidth(width);
                    info.setHeight(height);
                    info.setCodecType(Constants.VIDEO_CODEC_VP8);
                    info.setFramesPerSecond(argsConfig.getFps());
                    info.setRotation(0);

                    int ret = conn.pushVideoEncodedData(data, info);
                    frameIndex++;
                    if (canLog) {
                        SampleLogger.log("send vp8 ret:" + ret + " frame data size:" + data.length
                                + " width:" + width + " height:" + height
                                + " lastFrameType:" + lastFrameType + " fps:" + argsConfig.getFps()
                                + " timestamp:" + timestamp + " frameIndex:" + frameIndex
                                + " testStartTime:" + Utils.formatTimestamp(testStartTime)
                                + " testTime:" + argsConfig.getTestTime() + " from channelId:"
                                + argsConfig.getChannelId() + " userId:" + argsConfig.getUserId());
                        if (argsConfig.isEnableStressTest()) {
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
                        vp8Reader = new Vp8Reader(argsConfig.getVideoFile());
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
            public void release() {
                super.release();
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

        testTaskExecutorService.execute(vp8SendThread);

        SampleLogger.log("sendVp8Task start");

        if (waitRelease) {
            try {
                handleWaitRelease(remainingTime);
                vp8SendThread.release();
                SampleLogger.log("sendVp8Task end");
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendAvMediaTask() {
        int intervalMs = 50; // ms
        long remainingTime = getRemainingTime();
        SampleLogger.log("sendAvMediaTask remainingTime:" + remainingTime
                + " argsConfig:" + argsConfig + " intervalMs:" + intervalMs);

        if (remainingTime == -1) {
            releaseConn();
            return;
        }

        MediaDecodeUtils mediaDecodeUtils = new MediaDecodeUtils();

        boolean initRet = mediaDecodeUtils.init(argsConfig.getVideoFile(), intervalMs, -1,
                MediaDecodeUtils.DecodedMediaType.PCM_YUV, new MediaDecodeUtils.MediaDecodeCallback() {
                    private ByteBuffer byteBuffer;
                    private boolean publishAudio = false;
                    private boolean publishVideo = false;

                    @Override
                    public void onAudioFrame(MediaDecode.MediaFrame frame) {
                        if (!publishAudio) {
                            conn.publishAudio();
                            waitUntilPublishSuccess();
                            publishAudio = true;
                        }
                        if (!taskStarted.get()) {
                            return;
                        }
                        int ret = conn.pushAudioPcmData(frame.buffer, frame.sampleRate, frame.channels);
                        SampleLogger.log("SendPcmData frame.buffer size:" + frame.buffer.length
                                + " frame.sampleRate:" + frame.sampleRate + " frame.channels:"
                                + frame.channels + " frame.samples:" + frame.samples + " ret:" + ret);
                    }

                    @Override
                    public void onVideoFrame(MediaDecode.MediaFrame frame) {
                        if (!publishVideo) {
                            VideoEncoderConfig config = new VideoEncoderConfig();
                            config.setCodecType(Constants.VIDEO_CODEC_H264);
                            config.setDimensions(new VideoDimensions(frame.width, frame.height));
                            config.setFrameRate(frame.fps);
                            conn.setVideoEncoderConfig(config);
                            // Publish video track
                            conn.publishVideo();
                            waitUntilPublishSuccess();
                            publishVideo = true;
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
                        int ret = conn.pushVideoFrame(externalVideoFrame);
                        SampleLogger.log("SendVideoFrame frame.pts:" + frame.pts + " ret:" + ret);
                    }
                });

        SampleLogger.log("sendAvMediaTask initRet:" + initRet);
        if (initRet) {
            mediaDecodeUtils.start();
        }

        handleWaitRelease(remainingTime);
        mediaDecodeUtils.stop();

        releaseConn();
    }

    public void sendDataStreamTask(int sendStreamMessageCount, boolean waitRelease) {
        if (conn == null) {
            SampleLogger.log("sendDataStream conn is null");
            return;
        }

        final CountDownLatch testFinishLatch = new CountDownLatch(1);
        try {
            testTaskExecutorService.execute(() -> {
                for (int i = 0; i < sendStreamMessageCount; i++) {
                    String data = Utils.getCurrentTime() + " hello world from channelId:"
                            + argsConfig.getChannelId() + " userId:" + argsConfig.getUserId();
                    int ret = conn.sendStreamMessage(data.getBytes());
                    SampleLogger.log("sendStreamMessage: " + data + " done ret:" + ret);

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                testFinishLatch.countDown();
            });
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

    public void registerPcmObserverTask(boolean waitRelease, boolean isAiServerMode) {
        long remainingTime = getRemainingTime();
        String finalAudioOutFile = argsConfig.getAudioOutFile() + "_" + argsConfig.getChannelId()
                + "_" + argsConfig.getUserId() + ".pcm";
        SampleLogger.log("registerPcmObserverTask audioOutFile:" + finalAudioOutFile
                + " remainingTime:" + remainingTime + " isAiServerMode:" + isAiServerMode
                + " argsConfig:" + argsConfig);

        if (remainingTime == -1) {
            releaseConn();
            return;
        }

        if (!Utils.isNullOrEmpty(argsConfig.getRemoteUserId())) {
            conn.getLocalUser().subscribeAudio(argsConfig.getRemoteUserId());
        } else {
            conn.getLocalUser().subscribeAllAudio();
        }

        // Register audio frame observer to receive audio stream
        int ret = conn.getLocalUser().setPlaybackAudioFrameBeforeMixingParameters(
                argsConfig.getNumOfChannels(), argsConfig.getSampleRate());
        SampleLogger.log("setPlaybackAudioFrameBeforeMixingParameters numOfChannels:"
                + argsConfig.getNumOfChannels() + " sampleRate:" + argsConfig.getSampleRate());
        if (ret > 0) {
            SampleLogger.log("setPlaybackAudioFrameBeforeMixingParameters fail ret=" + ret);
            return;
        }

        audioFrameObserver = new SampleAudioFrameObserver(finalAudioOutFile) {
            private boolean canLog = argsConfig.isEnableLog();

            @Override
            public int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agoraLocalUser,
                    String channelId, String userId, AudioFrame frame, VadProcessResult vadResult) {
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

                if (isAiServerMode && argsConfig.isEnableAssistantDevice()) {
                    audioFrameManager.processUplinkAudioFrame(byteArray, argsConfig.getSampleRate(),
                            argsConfig.getNumOfChannels(), frame.getPresentationMs());
                }

                if (canLog) {
                    testTaskExecutorService.execute(() -> {
                        String vadResultStr = null;
                        if (vadResult != null) {
                            vadResultStr = vadResult.getState().name();
                            if (vadResult.getOutFrame() != null) {
                                vadResultStr += " vadOutFrame size:" + vadResult.getOutFrame().length;
                            }
                        }
                        SampleLogger.log("onPlaybackAudioFrameBeforeMixing frame:" + frame
                                + " audioFrame size " + byteArray.length + " vadResult:"
                                + vadResultStr + " channelId:" + channelId + " userId:" + userId
                                + " with current channelId:" + argsConfig.getChannelId()
                                + " currentUserId:" + argsConfig.getUserId());
                        if (argsConfig.isEnableStressTest()) {
                            canLog = false;
                        }
                    });
                }

                if (argsConfig.isEnableSaveFile()) {
                    singleExecutorService.execute(() -> {
                        writeAudioFrameToFile(byteArray);
                    });
                }
                if (null != vadResult) {
                    if (argsConfig.isEnableSaveFile()) {
                        singleExecutorService.execute(() -> {
                            writeAudioFrameToFile(
                                    vadResult.getOutFrame(), finalAudioOutFile + "_vad.pcm");
                        });
                    }
                }
                return 1;
            }
        };
        conn.registerAudioFrameObserver(
                audioFrameObserver, argsConfig.isEnableVad(), new AgoraAudioVadConfigV2());

        if (waitRelease) {
            connConnect();
            try {
                handleWaitRelease(remainingTime);
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void registerMixedAudioObserverTask(boolean waitRelease) {
        String finalAudioOutFile = argsConfig.getAudioOutFile() + "_" + argsConfig.getChannelId()
                + "_" + argsConfig.getUserId() + ".pcm";
        long remainingTime = getRemainingTime();
        SampleLogger.log("registerMixedAudioObserverTask audioOutFile:" + finalAudioOutFile
                + " remainingTime:" + remainingTime + " argsConfig:" + argsConfig);

        if (remainingTime == -1) {
            releaseConn();
            return;
        }

        if (!Utils.isNullOrEmpty(argsConfig.getRemoteUserId())) {
            conn.getLocalUser().subscribeAudio(argsConfig.getRemoteUserId());
        } else {
            conn.getLocalUser().subscribeAllAudio();
        }

        // Register audio frame observer to receive audio stream
        int ret = conn.getLocalUser().setPlaybackAudioFrameParameters(argsConfig.getNumOfChannels(),
                argsConfig.getSampleRate(), 0,
                argsConfig.getSampleRate() / 100 * argsConfig.getNumOfChannels());
        SampleLogger.log("setPlaybackAudioFrameParameters numOfChannels:"
                + argsConfig.getNumOfChannels() + " sampleRate:" + argsConfig.getSampleRate());
        if (ret > 0) {
            SampleLogger.log("setPlaybackAudioFrameParameters fail ret=" + ret);
            return;
        }

        audioFrameObserver = new SampleAudioFrameObserver(finalAudioOutFile) {
            private boolean canLog = argsConfig.isEnableLog();

            @Override
            public int onPlaybackAudioFrame(
                    AgoraLocalUser agoraLocalUser, String channelId, AudioFrame frame) {
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
                            + " channelId:" + channelId + " with current channelId:"
                            + argsConfig.getChannelId() + "  userId:" + argsConfig.getUserId());
                    if (argsConfig.isEnableStressTest()) {
                        canLog = false;
                    }
                }

                singleExecutorService.execute(() -> {
                    writeAudioFrameToFile(byteArray);
                });
                return 1;
            }
        };
        conn.registerAudioFrameObserver(audioFrameObserver, false, null);

        if (waitRelease) {
            connConnect();
            try {
                handleWaitRelease(remainingTime);
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void registerYuvObserverTask(boolean waitRelease) {
        String finalVideoOutFile = argsConfig.getVideoOutFile() + "_" + argsConfig.getChannelId()
                + "_" + argsConfig.getUserId() + ".yuv";
        long remainingTime = getRemainingTime();
        SampleLogger.log("registerYuvObserverTask videoOutFile:" + finalVideoOutFile
                + " remainingTime:" + remainingTime + " argsConfig:" + argsConfig);

        if (remainingTime == -1) {
            releaseConn();
            return;
        }

        VideoSubscriptionOptions subscriptionOptions = new VideoSubscriptionOptions();
        subscriptionOptions.setType(getStreamType(argsConfig.getStreamType()));
        if (!Utils.isNullOrEmpty(argsConfig.getRemoteUserId())) {
            conn.getLocalUser().subscribeVideo(argsConfig.getRemoteUserId(), subscriptionOptions);
        } else {
            conn.getLocalUser().subscribeAllVideo(subscriptionOptions);
        }

        videoFrameObserver = new AgoraVideoFrameObserver2(new SampleVideFrameObserver(
                finalVideoOutFile) {
            private boolean canLog = argsConfig.isEnableLog();
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
                            + " with current channelId:" + argsConfig.getChannelId()
                            + " with current userId:" + argsConfig.getUserId());
                    frameCount = 0;
                    firstFrameTime = currentTime;
                }

                // Calculate actual data size without padding
                int width = frame.getWidth();
                int height = frame.getHeight();
                int yStride = frame.getYStride();
                int uStride = frame.getUStride();
                int vStride = frame.getVStride();

                // YUV420P format: Y plane full size, U/V planes quarter size
                int yDataSize = width * height;
                int uvDataSize = (width / 2) * (height / 2);

                // Check if stride and buffer are valid
                if (yStride < width || uStride < (width / 2) || vStride < (width / 2)) {
                    SampleLogger.log("Invalid stride: yStride=" + yStride +
                            ", uStride=" + uStride + ", vStride=" + vStride +
                            ", width=" + width + ", height=" + height);
                    return;
                }

                if (frame.getYBuffer().remaining() < yStride * height ||
                        frame.getUBuffer().remaining() < uStride * (height / 2) ||
                        frame.getVBuffer().remaining() < vStride * (height / 2)) {
                    SampleLogger.log("YUV buffer size insufficient for stride data");
                    return;
                }

                byte[] data = new byte[yDataSize + uvDataSize + uvDataSize];
                int dataOffset = 0;

                // Copy Y plane line by line to remove padding
                byte[] yBuffer = io.agora.rtc.utils.Utils.getBytes(frame.getYBuffer());
                for (int row = 0; row < height; row++) {
                    System.arraycopy(yBuffer, row * yStride, data, dataOffset, width);
                    dataOffset += width;
                }

                // Copy U plane line by line to remove padding
                byte[] uBuffer = io.agora.rtc.utils.Utils.getBytes(frame.getUBuffer());
                int uvWidth = width / 2;
                int uvHeight = height / 2;
                for (int row = 0; row < uvHeight; row++) {
                    System.arraycopy(uBuffer, row * uStride, data, dataOffset, uvWidth);
                    dataOffset += uvWidth;
                }

                // Copy V plane line by line to remove padding
                byte[] vBuffer = io.agora.rtc.utils.Utils.getBytes(frame.getVBuffer());
                for (int row = 0; row < uvHeight; row++) {
                    System.arraycopy(vBuffer, row * vStride, data, dataOffset, uvWidth);
                    dataOffset += uvWidth;
                }

                if (argsConfig.isEnableSaveFile()) {
                    singleExecutorService.execute(() -> {
                        writeVideoFrameToFile(data);
                    });
                }

                final byte[] metaDataBufferData = io.agora.rtc.utils.Utils.getBytes(frame.getMetadataBuffer());
                final byte[] alphaBufferData = io.agora.rtc.utils.Utils.getBytes(frame.getAlphaBuffer());

                if (canLog) {
                    testTaskExecutorService.execute(() -> {
                        SampleLogger.log(String.format(
                                "onFrame width:%d height:%d channelId:%s remoteUserId:%s "
                                        + "frame size:%d %d %d with current channelId:%s userId:%s",
                                frame.getWidth(), frame.getHeight(), channelId, remoteUserId, yDataSize,
                                uvDataSize, uvDataSize, argsConfig.getChannelId(), argsConfig.getUserId()));

                        if (metaDataBufferData != null) {
                            SampleLogger.log(
                                    "onFrame metaDataBuffer :" + new String(metaDataBufferData));
                            if (!Utils.isNullOrEmpty(argsConfig.getVideoOutFile())) {
                                singleExecutorService.execute(() -> {
                                    Utils.writeBytesToFile(metaDataBufferData,
                                            argsConfig.getVideoOutFile() + "_"
                                                    + argsConfig.getChannelId() + "_"
                                                    + argsConfig.getUserId() + "_metaData.txt");
                                });
                            }
                        }

                        if (alphaBufferData != null) {
                            SampleLogger.log("onFrame getAlphaBuffer size:" + alphaBufferData.length
                                    + " mode:" + frame.getAlphaMode());
                            if (!Utils.isNullOrEmpty(argsConfig.getVideoOutFile())) {
                                singleExecutorService.execute(() -> {
                                    Utils.writeBytesToFile(alphaBufferData,
                                            argsConfig.getVideoOutFile() + "_"
                                                    + argsConfig.getChannelId() + "_"
                                                    + argsConfig.getUserId() + "_alpha.raw");
                                });
                            }
                        }
                        if (argsConfig.isEnableStressTest()) {
                            canLog = false;
                        }
                    });
                }
            }
        });
        conn.registerVideoFrameObserver(videoFrameObserver);

        if (waitRelease) {
            connConnect();
            try {
                handleWaitRelease(remainingTime);
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void registerH264ObserverTask(boolean waitRelease) {
        String finalVideoOutFile = argsConfig.getVideoOutFile() + "_" + argsConfig.getChannelId()
                + "_" + argsConfig.getUserId() + ".h264";
        long remainingTime = getRemainingTime();
        SampleLogger.log("registerH264ObserverTask videoOutFile:" + finalVideoOutFile
                + " remainingTime:" + remainingTime + " argsConfig:" + argsConfig);

        if (remainingTime == -1) {
            releaseConn();
            return;
        }

        VideoSubscriptionOptions subscriptionOptions = new VideoSubscriptionOptions();
        subscriptionOptions.setEncodedFrameOnly(1);
        subscriptionOptions.setType(getStreamType(argsConfig.getStreamType()));
        SampleLogger.log("registerH264ObserverTask subscriptionOptions:" + subscriptionOptions);
        if (!Utils.isNullOrEmpty(argsConfig.getRemoteUserId())) {
            conn.getLocalUser().subscribeVideo(argsConfig.getRemoteUserId(), subscriptionOptions);
        } else {
            conn.getLocalUser().subscribeAllVideo(subscriptionOptions);
        }

        videoEncodedFrameObserver = new AgoraVideoEncodedFrameObserver(
                new SampleVideoEncodedFrameObserver(finalVideoOutFile) {
                    private boolean canLog = argsConfig.isEnableLog();

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
                            testTaskExecutorService.execute(() -> {
                                SampleLogger.log("onEncodedVideoFrame userId:" + userId + " length "
                                        + byteArray.length
                                        + " with current channelId:" + argsConfig.getChannelId()
                                        + " current userId:" + argsConfig.getUserId() + " info:" + info);
                                if (argsConfig.isEnableStressTest()) {
                                    canLog = false;
                                }
                            });
                        }

                        if (argsConfig.isEnableSaveFile()) {
                            singleExecutorService.execute(() -> {
                                writeVideoDataToFile(byteArray);
                            });
                        }

                        return 1;
                    }
                });
        conn.registerVideoEncodedFrameObserver(videoEncodedFrameObserver);

        if (waitRelease) {
            connConnect();
            try {
                handleWaitRelease(remainingTime);
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void registerEncodedAudioObserverTask(boolean waitRelease) {
        String finalAudioOutFile = argsConfig.getAudioOutFile() + "_" + argsConfig.getChannelId()
                + "_" + argsConfig.getUserId() + "." + argsConfig.getFileType();
        long remainingTime = getRemainingTime();
        SampleLogger.log("registerEncodedAudioObserverTask audioOutFile:" + finalAudioOutFile
                + " remainingTime:" + remainingTime + " argsConfig:" + argsConfig);

        if (remainingTime == -1) {
            releaseConn();
            return;
        }

        if (!Utils.isNullOrEmpty(argsConfig.getRemoteUserId())) {
            conn.getLocalUser().subscribeAudio(argsConfig.getRemoteUserId());
        } else {
            conn.getLocalUser().subscribeAllAudio();
        }

        audioEncodedFrameObserver = new SampleAudioEncodedFrameObserver(finalAudioOutFile) {
            private boolean canLog = argsConfig.isEnableLog();

            @Override
            public int onEncodedAudioFrameReceived(
                    String remoteUserId, ByteBuffer buffer, EncodedAudioFrameReceiverInfo info) {
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
                    SampleLogger.log("onEncodedAudioFrameReceived buffer size:" + byteArray.length
                            + " info:" + info + " remoteUserId:" + remoteUserId
                            + " with current channelId:" + argsConfig.getChannelId());
                    if (argsConfig.isEnableStressTest()) {
                        canLog = false;
                    }
                }

                writeAudioFrameToFile(byteArray, finalAudioOutFile);
                return 1;
            }
        };

        conn.registerAudioEncodedFrameObserver(audioEncodedFrameObserver);

        if (waitRelease) {
            connConnect();
            try {
                handleWaitRelease(remainingTime);
                releaseConn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void recvDataStreamTask(boolean waitRelease) {
        long remainingTime = getRemainingTime();
        SampleLogger.log("recvDataStreamTask remainingTime:" + remainingTime);

        if (remainingTime == -1) {
            releaseConn();
            return;
        }

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
        if (argsConfig.isEnableStressTest()) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - testStartTime;
            long maxStressTime = (long) ((argsConfig.getSleepTime() - argsConfig.getTimeForStressLeave()) * 1000);

            if (elapsedTime < maxStressTime) {
                synchronized (this) {
                    long remainingTime;
                    if (elapsedTime + argsConfig.getTestTime() * 1000 < maxStressTime) {
                        remainingTime = argsConfig.getTestTime() * 1000;
                    } else {
                        remainingTime = maxStressTime - elapsedTime;
                    }
                    return remainingTime;
                }
            } else {
                SampleLogger.log(
                        String.format("handleWaitRelease testTime:%d testStartTime:%d currentTime:%d "
                                + "timeForStressLeave:%d and now release conn",
                                argsConfig.getTestTime(), testStartTime, currentTime,
                                argsConfig.getTimeForStressLeave()));
                return -1;
            }
        } else {
            return argsConfig.getTestTime() * 1000;
        }
    }

    private synchronized void handleWaitRelease(long remainingTime) {
        try {
            if (argsConfig.getTestTime() > 0) {
                wait(remainingTime);
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

    private void waitUntilPublishSuccess() {
        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int getStreamType(String streamType) {
        switch (streamType) {
            case "high":
                return Constants.VIDEO_STREAM_HIGH;
            case "low":
                return Constants.VIDEO_STREAM_LOW;
            default:
                return Constants.VIDEO_STREAM_HIGH;
        }
    }
}