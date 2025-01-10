package io.agora.rtc.common;

import io.agora.rtc.AgoraAudioVadConfigV2;
import io.agora.rtc.ColorSpace;
import io.agora.rtc.DownlinkNetworkInfo;
import io.agora.rtc.EncodedAudioFrameReceiverInfo;
import io.agora.rtc.UplinkNetworkInfo;
import io.agora.rtc.VadProcessResult;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.agora.rtc.AgoraAudioEncodedFrameSender;
import io.agora.rtc.AgoraAudioPcmDataSender;
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
import io.agora.rtc.AudioVolumeInfo;
import io.agora.rtc.Constants;
import io.agora.rtc.DefaultLocalUserObserver;
import io.agora.rtc.DefaultRtcConnObserver;
import io.agora.rtc.EncodedAudioFrameInfo;
import io.agora.rtc.EncodedVideoFrameInfo;
import io.agora.rtc.EncryptionConfig;
import io.agora.rtc.ExternalVideoFrame;
import io.agora.rtc.INetworkObserver;
import io.agora.rtc.Out;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.SenderOptions;
import io.agora.rtc.SimulcastStreamConfig;
import io.agora.rtc.VideoDimensions;
import io.agora.rtc.VideoEncoderConfig;
import io.agora.rtc.VideoFrame;
import io.agora.rtc.VideoSubscriptionOptions;
import io.agora.rtc.mediautils.AacReader;
import io.agora.rtc.mediautils.H264Reader;
import io.agora.rtc.ffmpegutils.MediaDecode;
import io.agora.rtc.ffmpegutils.MediaDecodeUtils;
import io.agora.rtc.mediautils.OpusReader;
import io.agora.rtc.mediautils.Vp8Reader;

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

    private String channelId;
    private String userId;
    private final Random random;
    private final ExecutorService singleExecutorService;
    private final ThreadPoolExecutor testTaskExecutorService;
    private final ThreadPoolExecutor logExecutorService;
    private CountDownLatch userLeftLatch;
    private AtomicBoolean taskStarted = new AtomicBoolean(false);

    public interface TaskCallback {
        default void onConnected() {

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
        this.random = new Random();
    }

    public void setCallback(TaskCallback callback) {
        this.callback = callback;
    }

    public AgoraRtcConn getConn() {
        return conn;
    }

    public void createConnectionAndTest(RtcConnConfig ccfg, String token, String channelId, String userId,
            int enableEncryptionMode, int encryptionMode, String encryptionKey, boolean enableCloudProxy) {
        SampleLogger
                .log("createConnectionAndTest token:" + token + " channelId:" + channelId + " userId:" + userId
                        + " enableEncryptionMode:"
                        + enableEncryptionMode + " encryptionMode:" + encryptionMode + " encryptionKey:"
                        + encryptionKey + " enableCloudProxy:" + enableCloudProxy);
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
        this.channelId = channelId;
        this.userId = userId;

        int ret = conn.registerObserver(new DefaultRtcConnObserver() {
            @Override
            public void onConnected(AgoraRtcConn agora_rtc_conn, RtcConnInfo conn_info, int reason) {
                super.onConnected(agora_rtc_conn, conn_info, reason);
                SampleLogger.log(
                        "onConnected chennalId:" + conn_info.getChannelId() + " userId:" + conn_info.getLocalUserId());
                if (null != callback) {
                    callback.onConnected();
                }
            }

            @Override
            public void onUserJoined(AgoraRtcConn agora_rtc_conn, String user_id) {
                super.onUserJoined(agora_rtc_conn, user_id);
                SampleLogger.log("onUserJoined user_id:" + user_id);
                if (null != callback) {
                    callback.onUserJoined(user_id);
                }

            }

            @Override
            public void onUserLeft(AgoraRtcConn agora_rtc_conn, String user_id, int reason) {
                super.onUserLeft(agora_rtc_conn, user_id, reason);
                SampleLogger.log("onUserLeft user_id:" + user_id + " reason:" + reason);
                if (testTime == 0) {
                    if (null != userLeftLatch) {
                        userLeftLatch.countDown();
                    }
                }

                if (null != callback) {
                    callback.onUserLeft(user_id);
                }
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

        ret = conn.registerNetworkObserver(new INetworkObserver() {
            @Override
            public void onUplinkNetworkInfoUpdated(AgoraRtcConn agora_rtc_conn, UplinkNetworkInfo info) {
                SampleLogger.log("onUplinkNetworkInfoUpdated info:" + info);
            }

            @Override
            public void onDownlinkNetworkInfoUpdated(AgoraRtcConn agora_rtc_conn, DownlinkNetworkInfo info) {
                SampleLogger.log("onDownlinkNetworkInfoUpdated info:" + info);
            }
        });
        SampleLogger.log("registerNetworkObserver ret:" + ret);

        if (enableEncryptionMode == 1 && !Utils.isNullOrEmpty(encryptionKey)) {
            EncryptionConfig encryptionConfig = new EncryptionConfig();
            encryptionConfig.setEncryptionMode(encryptionMode);
            encryptionConfig.setEncryptionKey(encryptionKey);
            ret = conn.enableEncryption(enableEncryptionMode, encryptionConfig);
            if (ret < 0) {
                SampleLogger.log("Failed to enable encryption ret:" + ret);
                return;
            }
            SampleLogger.log("Enable encryption successfully!");
        }

        if (enableCloudProxy) {
            AgoraParameter agoraParameter = conn.getAgoraParameter();
            ret = agoraParameter.setBool("rtc.enable_proxy", true);
            SampleLogger.log("setBool rtc.enable_proxy ret:" + ret);
        }
        // conn.getLocalUser().setAudioVolumeIndicationParameters(50, 3, true);

        ret = conn.connect(token, channelId, userId);
        SampleLogger.log("Connecting to Agora channel " + channelId + " with userId " + userId + " ret:" + ret);

        // Register local user observer
        if (null == sampleLocalUserObserver) {
            sampleLocalUserObserver = new SampleLocalUserObserver(conn.getLocalUser());
        }
        conn.getLocalUser().registerObserver(sampleLocalUserObserver);

        mediaNodeFactory = service.createMediaNodeFactory();
    }

    public synchronized void releaseConn() {
        taskStarted.set(false);
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

        if (null != audioEncodedFrameSender) {
            audioEncodedFrameSender.destroy();
        }

        if (null != customEncodedAudioTrack) {
            conn.getLocalUser().unpublishAudio(customEncodedAudioTrack);
            customEncodedAudioTrack.destroy();
        }

        if (null != sampleLocalUserObserver) {
            sampleLocalUserObserver.unregisterAudioFrameObserver();
            sampleLocalUserObserver.unregisterVideoFrameObserver();
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
        audioEncodedFrameSender = null;
        customEncodedAudioTrack = null;
        sampleLocalUserObserver = null;

        conn = null;

        singleExecutorService.shutdown();
        testTaskExecutorService.shutdown();
        logExecutorService.shutdown();

        SampleLogger.log("Disconnected from Agora channel successfully\n");
    }

    public void sendPcmTask(String filePath, int interval, int numOfChannels, int sampleRate,
            boolean waitRelease, boolean enableAudioCache, boolean enableSendAudioMetaData) {
        SampleLogger
                .log("sendPcmTask filePath:" + filePath + " interval:" + interval + " numOfChannels:" + numOfChannels
                        + " sampleRate:" + sampleRate + " enableAudioCache:" + enableAudioCache
                        + " enableSendAudioMetaData:"
                        + enableSendAudioMetaData);
        audioFrameSender = mediaNodeFactory.createAudioPcmDataSender();
        // Create audio track
        customAudioTrack = service.createCustomAudioTrackPcm(audioFrameSender);
        // customAudioTrack.setMaxBufferedAudioFrameNumber(1000);
        conn.getLocalUser().publishAudio(customAudioTrack);

        int bufferSize = numOfChannels * (sampleRate / 1000) * interval * 2;
        byte[] buffer = new byte[bufferSize];

        FileSender pcmSendThread = new FileSender(filePath, interval) {
            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                if (data == null) {
                    return;
                }

                int samplesPerChannel = data.length / 2 / numOfChannels;

                if (null != audioFrameSender && taskStarted.get()) {
                    int ret = audioFrameSender.send(data, 0,
                            samplesPerChannel, 2,
                            numOfChannels,
                            sampleRate);
                    SampleLogger.log("send pcm frame data size:" + data.length + " sampleRate:" +
                            sampleRate
                            + " numOfChannels:" + numOfChannels
                            + " to channelId:"
                            + channelId + " from userId:" + userId + " ret:" + ret + " testStartTime:"
                            + Utils.formatTimestamp(testStartTime)
                            + " testTime:" + testTime);

                    if (enableSendAudioMetaData) {
                        ret = conn.getLocalUser()
                                .sendAudioMetaData(("testSendAudioMetaData " + timestamp).getBytes());
                        SampleLogger.log("sendAudioMetaData ret:" + ret);
                    }
                } else {
                    release(false);
                }
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                if (fos != null) {
                    try {
                        int size = fos.read(buffer, 0, bufferSize);
                        if (testTime > 0) {
                            if (System.currentTimeMillis() - testStartTime >= testTime * 1000) {
                                release(false);
                                return null;
                            } else {
                                if (size < 0) {
                                    reset();
                                    return null;
                                }
                            }
                        } else {
                            if (size < 0) {
                                release(false);
                                return null;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return buffer;
            }

            @Override
            public void release(boolean withJoin) {
                super.release(withJoin);
            }
        };

        pcmSendThread.start();

        if (!taskStarted.get()) {
            taskStarted.set(true);
        }

        SampleLogger.log("sendPcmTask start");
        if (waitRelease) {
            try {
                pcmSendThread.join();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SampleLogger.log("sendPcmTask end");

            releaseConn();
            if (null != callback) {
                callback.onTestFinished();
            }
        }
    }

    public void sendAacTask(String filePath, int interval, int numOfChannels, int sampleRate, boolean waitRelease) {
        SampleLogger
                .log("sendAacTask filePath:" + filePath + " interval:" + interval + " numOfChannels:" + numOfChannels
                        + " sampleRate:" + sampleRate);
        // Create audio track
        audioEncodedFrameSender = mediaNodeFactory.createAudioEncodedFrameSender();
        customEncodedAudioTrack = service.createCustomAudioTrackEncoded(audioEncodedFrameSender,
                Constants.TMixMode.MIX_DISABLED.value);
        conn.getLocalUser().publishAudio(customEncodedAudioTrack);

        AacReader aacReader = new AacReader(filePath);
        EncodedAudioFrameInfo encodedInfo = new EncodedAudioFrameInfo();

        FileSender aacSendThread = new FileSender(filePath, interval) {
            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                if (data == null) {
                    return;
                }
                if (timestamp == 0) {
                    timestamp = System.currentTimeMillis();
                }
                if (!taskStarted.get()) {
                    return;
                }
                int ret = audioEncodedFrameSender.send(data, data.length, encodedInfo);
                SampleLogger.log("send aac frame data size:" + data.length + " timestamp:"
                        + timestamp + " encodedInfo:" + encodedInfo
                        + " to channelId:"
                        + channelId + " from userId:" + userId + " ret:" + ret + " testStartTime:"
                        + Utils.formatTimestamp(testStartTime)
                        + " currentTime:" + Utils.getCurrentTime() + " testTime:" +
                        testTime);
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                AacReader.AacFrame aacFrame = aacReader.getAudioFrame(interval);
                if (testTime > 0) {
                    if (System.currentTimeMillis() - testStartTime >= testTime * 1000) {
                        release(false);
                        return null;
                    } else {
                        if (aacFrame == null) {
                            aacReader.reset();
                            reset();
                            return null;
                        }
                    }
                } else {
                    if (aacFrame == null) {
                        aacReader.reset();
                        release(false);
                        return null;
                    }
                }
                if (aacFrame != null) {
                    encodedInfo.setCodec(aacFrame.codec);
                    encodedInfo.setNumberOfChannels(aacFrame.numberOfChannels);
                    encodedInfo.setSampleRateHz(aacFrame.sampleRate);
                    encodedInfo.setSamplesPerChannel(aacFrame.samplesPerChannel);
                    return aacFrame.buffer;
                } else {
                    aacReader.reset();
                    release();
                    return null;
                }
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
            try {
                aacSendThread.join();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SampleLogger.log("sendAacTask end");

            releaseConn();
            if (null != callback) {
                callback.onTestFinished();
            }
        }
    }

    public void sendOpusTask(String filePath, int interval, boolean waitRelease) {
        SampleLogger
                .log("sendOpusTask filePath:" + filePath + " interval:" + interval);
        // Create audio track
        audioEncodedFrameSender = mediaNodeFactory.createAudioEncodedFrameSender();
        customEncodedAudioTrack = service.createCustomAudioTrackEncoded(audioEncodedFrameSender,
                Constants.TMixMode.MIX_DISABLED.value);
        conn.getLocalUser().publishAudio(customEncodedAudioTrack);

        OpusReader opusReader = new OpusReader(filePath);
        EncodedAudioFrameInfo encodedInfo = new EncodedAudioFrameInfo();

        FileSender opusSendThread = new FileSender(filePath, interval) {

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
                SampleLogger.log("send opus frame data size:" + data.length + " timestamp:"
                        + timestamp + " encodedInfo:" + encodedInfo
                        + " to channelId:"
                        + channelId + " from userId:" + userId + " ret:" + ret + " testStartTime:"
                        + Utils.formatTimestamp(testStartTime)
                        + " currentTime:" + Utils.getCurrentTime() + " testTime:" +
                        testTime);
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                io.agora.rtc.mediautils.AudioFrame opusFrame = opusReader.getAudioFrame(interval);
                if (testTime > 0) {
                    if (System.currentTimeMillis() - testStartTime >= testTime * 1000) {
                        release(false);
                        return null;
                    } else {
                        if (opusFrame == null) {
                            opusReader.reset();
                            reset();
                            return null;
                        }
                    }
                } else {
                    if (opusFrame == null) {
                        opusReader.reset();
                        release(false);
                        return null;
                    }
                }
                if (opusFrame != null) {
                    encodedInfo.setCodec(opusFrame.codec);
                    encodedInfo.setNumberOfChannels(opusFrame.numberOfChannels);
                    encodedInfo.setSampleRateHz(opusFrame.sampleRate);
                    encodedInfo.setSamplesPerChannel(opusFrame.samplesPerChannel);
                    return opusFrame.buffer;
                } else {
                    opusReader.reset();
                    release();
                    return null;
                }
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
            try {
                opusSendThread.join();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SampleLogger.log("sendAacTask end");

            releaseConn();
            if (null != callback) {
                callback.onTestFinished();
            }
        }
    }

    public void sendYuvTask(String filePath, int interval, int height, int width, int fps, int streamType,
            boolean enableSimulcastStream, boolean waitRelease, boolean enableAlpha) {
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

        FileSender yuvSender = new FileSender(filePath, interval) {
            private int frameIndex = 0;
            private ByteBuffer byteBuffer;
            private ByteBuffer matedataByteBuffer;
            private ByteBuffer alphaByteBuffer;

            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                if (data == null) {
                    return;
                }

                if (timestamp == 0) {
                    timestamp = System.currentTimeMillis();
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

                SampleLogger.log("send yuv frame data size:" + data.length + "  ret:" + ret +
                        " timestamp:" + timestamp + " frameIndex:" + frameIndex + " testStartTime:"
                        + Utils.formatTimestamp(testStartTime)
                        + " currentTime:" + Utils.getCurrentTime() + " testTime:" +
                        testTime + " from channelId:" + channelId + " userId:" + userId);

            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                if (fos == null) {
                    return null;
                }
                try {
                    int size = fos.read(buffer, 0, bufferLen);
                    if (testTime > 0) {
                        if (System.currentTimeMillis() - testStartTime >= testTime * 1000) {
                            release(false);
                            return null;
                        } else {
                            if (size < 0) {
                                reset();
                                frameIndex = 0;
                                return null;
                            }
                        }
                    } else {
                        if (size < 0) {
                            release(false);
                            return null;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return buffer;
            }

            @Override
            public void release(boolean withJoin) {
                super.release(withJoin);
                Utils.cleanDirectBuffer(byteBuffer);
                Utils.cleanDirectBuffer(matedataByteBuffer);
                Utils.cleanDirectBuffer(alphaByteBuffer);
            }

        };

        yuvSender.start();
        if (!taskStarted.get()) {
            taskStarted.set(true);
        }
        SampleLogger.log("sendYuvTask start");

        if (waitRelease) {
            try {
                yuvSender.join();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SampleLogger.log("sendYuvTask end");

            releaseConn();
            if (null != callback) {
                callback.onTestFinished();
            }
        }
    }

    public void sendH264Task(String filePath, int interval, int height, int width, int streamType,
            boolean enableSimulcastStream,
            boolean waitRelease) {
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

        FileSender h264SendThread = new FileSender(filePath, interval) {
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
                SampleLogger.log("send h264 frame data size:" + data.length +
                        " timestamp:" + timestamp + " frameIndex:" + frameIndex + " testStartTime:"
                        + Utils.formatTimestamp(testStartTime)
                        + " currentTime:" + Utils.getCurrentTime() + " testTime:" +
                        testTime + " from channelId:" + channelId + " userId:" + userId);
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                H264Reader.H264Frame frame = h264Reader.readNextFrame();
                if (testTime > 0) {
                    if (System.currentTimeMillis() - testStartTime >= testTime * 1000) {
                        release(false);
                        return null;
                    } else {
                        if (frame == null) {
                            reset();
                            if (null != h264Reader) {
                                h264Reader.reset();
                            }
                            frameIndex = 0;
                            return null;
                        }
                    }
                } else {
                    if (frame == null) {
                        release(false);
                        return null;
                    }
                }

                if (frame != null) {
                    lastFrameType = frame.frameType;
                    return frame.data;
                } else {
                    return null;
                }
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
            try {
                h264SendThread.join();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SampleLogger.log("sendH264Task end");

            releaseConn();
            if (null != callback) {
                callback.onTestFinished();
            }
        }
    }

    public void sendRgbaTask(String filePath, int interval, int height, int width, int fps, boolean waitRelease) {
        SampleLogger.log("sendRgbaTask filePath:" + filePath + " interval:" + interval + " height:" + height + " width:"
                + width);
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

        FileSender rgbaSender = new FileSender(filePath, interval) {
            // For RGBA, each pixel takes 4 bytes.
            int bufferLen = height * width * 4;
            byte[] buffer = new byte[bufferLen];

            private int frameIndex = 0;
            ByteBuffer byteBuffer;
            ByteBuffer alphaBuffer;
            byte[] alphadata;

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

                if (timestamp == 0) {
                    timestamp = System.currentTimeMillis();
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

                SampleLogger.log("send rgba frame data size:" + data.length +
                        " timestamp:" + timestamp + " frameIndex:" + frameIndex + " testStartTime:"
                        + Utils.formatTimestamp(testStartTime)
                        + " currentTime:" + Utils.getCurrentTime() + " testTime:" +
                        testTime);

            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                if (fos == null) {
                    return null;
                }
                int size = 0;
                try {
                    size = fos.read(buffer, 0, bufferLen);
                    if (testTime > 0) {
                        if (System.currentTimeMillis() - testStartTime >= testTime * 1000) {
                            release(false);
                            return null;
                        } else {
                            if (size < 0) {
                                reset();
                                frameIndex = 0;
                                return null;
                            }
                        }
                    } else {
                        if (size < 0) {
                            release(false);
                            return null;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Arrays.copyOf(buffer, size);
            }

            @Override
            public void release(boolean withJoin) {
                super.release(withJoin);
                Utils.cleanDirectBuffer(byteBuffer);
                Utils.cleanDirectBuffer(alphaBuffer);
            }
        };

        rgbaSender.start();
        if (!taskStarted.get()) {
            taskStarted.set(true);
        }
        SampleLogger.log("sendRgbaTask start");

        if (waitRelease) {
            try {
                rgbaSender.join();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SampleLogger.log("sendRgbaTask end");

            releaseConn();
            if (null != callback) {
                callback.onTestFinished();
            }
        }
    }

    public void sendVp8Task(String filePath, int interval, int height, int width, int fps, int streamType,
            boolean enableSimulcastStream,
            boolean waitRelease) {
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

        FileSender vp8SendThread = new FileSender(filePath, interval, false) {
            private int lastFrameType = 0;
            private int frameIndex = 0;
            private int height;
            private int width;

            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                if (data == null) {
                    return;
                }
                if (timestamp == 0) {
                    timestamp = System.currentTimeMillis();
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
                SampleLogger.log("send vp8 ret:" + ret + " frame data size:" + data.length + " width:" + width
                        + " height:" + height + " lastFrameType:" + lastFrameType + " fps:" + fps +
                        " timestamp:" + timestamp + " frameIndex:" + frameIndex + " testStartTime:"
                        + Utils.formatTimestamp(testStartTime)
                        + " currentTime:" + Utils.getCurrentTime() + " testTime:" +
                        testTime + " from channelId:" + channelId + " userId:" + userId);
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                io.agora.rtc.mediautils.VideoFrame frame = vp8Reader.readNextFrame();
                int retry = 0;
                while (frame == null && retry < 4) {
                    vp8Reader.reset();
                    frame = vp8Reader.readNextFrame();
                    retry++;
                }
                if (testTime > 0) {
                    if (System.currentTimeMillis() - testStartTime >= testTime * 1000) {
                        release(false);
                        return null;
                    } else {
                        if (frame == null) {
                            reset();
                            frameIndex = 0;
                            return null;
                        }
                    }
                } else {
                    if (frame == null) {
                        release(false);
                        return null;
                    }
                }

                if (frame != null) {
                    lastFrameType = frame.frameType;
                    width = frame.width;
                    height = frame.height;
                    return frame.data;
                } else {
                    return null;
                }
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
            try {
                vp8SendThread.join();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SampleLogger.log("sendVp8Task end");

            releaseConn();
            if (null != callback) {
                callback.onTestFinished();
            }
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
                        SampleLogger.log("SendVideoFrame frame.pts:" + frame.pts + " ret:" + ret);
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
        if (null != callback) {
            callback.onTestFinished();
        }
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
                int result = conn.createDataStream(streamIds[k], 1, 1);
                SampleLogger
                        .log("sendDataStream create DataStream result " + result + " stream id " + streamIds[k].get());

                final int index = k;
                testTaskExecutorService.execute(() -> {
                    for (int i = 0; i < sendStreamMessageCount; i++) {
                        String data = Utils.getCurrentTime() + " hello world from channelId:"
                                + channelId + " userId:" + userId;
                        int ret = conn.sendStreamMessage(streamIds[index].get(), data, data.length());
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
            if (null != callback) {
                callback.onTestFinished();
            }
        }
    }

    public void registerPcmObserverTask(String remoteUserId, String audioOutFile, int numOfChannels, int sampleRate,
            boolean waitRelease, boolean enableSaveFile, boolean enableVad) {
        SampleLogger.log("registerPcmObserverTask remoteUserId:" + remoteUserId + " audioOutFile:" + audioOutFile
                + " numOfChannels:"
                + numOfChannels + " sampleRate:" + sampleRate + " enableSaveFile:" + enableSaveFile + " enableVad:"
                + enableVad);
        if (waitRelease) {
            userLeftLatch = new CountDownLatch(1);
        }

        conn.getLocalUser().subscribeAllAudio();

        // Register audio frame observer to receive audio stream
        int ret = conn.getLocalUser().setPlaybackAudioFrameBeforeMixingParameters(numOfChannels, sampleRate);
        SampleLogger.log("setPlaybackAudioFrameBeforeMixingParameters numOfChannels:" + numOfChannels + " sampleRate:"
                + sampleRate);
        if (ret > 0) {
            SampleLogger.log("setPlaybackAudioFrameBeforeMixingParameters fail ret=" + ret);
            return;
        }

        sampleLocalUserObserver.setAudioFrameObserver(new SampleAudioFrameObserver(audioOutFile) {
            @Override
            public int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agora_local_user, String channel_id, String uid,
                    AudioFrame frame, VadProcessResult vadResult) {
                if (null == frame) {
                    return 0;
                }
                logExecutorService.execute(() -> {
                    SampleLogger.log("onPlaybackAudioFrameBeforeMixing frame:" + frame);
                    SampleLogger.log("onPlaybackAudioFrameBeforeMixing audioFrame size " +
                            frame.getBuffer().capacity()
                            + " channel_id:"
                            + channel_id + " uid:" + uid + " with current channelId:"
                            + channelId
                            + " userId:" + userId);
                });

                if (enableSaveFile) {
                    byte[] byteArray = new byte[frame.getBuffer().remaining()];
                    frame.getBuffer().get(byteArray);
                    frame.getBuffer().rewind();
                    writeAudioFrameToFile(byteArray);
                }
                if (null != vadResult) {
                    logExecutorService.execute(() -> {
                        SampleLogger.log("onPlaybackAudioFrameBeforeMixing vadResult:" + vadResult);
                    });
                    writeVadAudioToFile(vadResult.getOutFrame(), audioOutFile + "_vad.pcm");
                }
                if (testTime > 0 && System.currentTimeMillis() - testStartTime >= testTime * 1000
                        && null != userLeftLatch) {
                    userLeftLatch.countDown();
                }
                return 1;
            }

        }, enableVad, new AgoraAudioVadConfigV2());

        if (waitRelease) {
            try {
                userLeftLatch.await();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            releaseConn();
            if (null != callback) {
                callback.onTestFinished();
            }
        }
    }

    public void registerMixedAudioObserverTask(String remoteUserId, String audioOutFile, int numOfChannels,
            int sampleRate,
            boolean waitRelease) {
        SampleLogger.log("registerMixedAudioObserverTask remoteUserId:" + remoteUserId + " audioOutFile:" + audioOutFile
                + " numOfChannels:"
                + numOfChannels + " sampleRate:" + sampleRate);
        if (waitRelease) {
            userLeftLatch = new CountDownLatch(1);
        }

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
        sampleLocalUserObserver.setAudioFrameObserver(new SampleAudioFrameObserver(audioOutFile) {
            @Override
            public int onPlaybackAudioFrame(AgoraLocalUser agora_local_user, String channel_id, AudioFrame frame) {
                if (null == frame) {
                    return 0;
                }
                logExecutorService.execute(() -> {
                    SampleLogger.log("onPlaybackAudioFrame frame:" + frame);
                    SampleLogger.log("onPlaybackAudioFrame audioFrame size " + frame.getBuffer().capacity()
                            + " channel_id:"
                            + channel_id + " with current channelId:"
                            + channelId
                            + "  userId:" + userId);
                });
                byte[] byteArray = new byte[frame.getBuffer().remaining()];
                frame.getBuffer().get(byteArray);
                frame.getBuffer().rewind();

                writeAudioFrameToFile(byteArray);
                if (testTime > 0 && System.currentTimeMillis() - testStartTime >= testTime * 1000
                        && null != userLeftLatch) {
                    userLeftLatch.countDown();
                }
                return 1;
            }

        });

        if (waitRelease) {
            try {
                userLeftLatch.await();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            releaseConn();
            if (null != callback) {
                callback.onTestFinished();
            }
        }
    }

    public void registerYuvObserverTask(String remoteUserId, String videoOutFile, String streamType,
            boolean waitRelease, boolean enableSaveFile) {
        SampleLogger.log("registerYuvObserverTask remoteUserId:" + remoteUserId + " videoOutFile:" + videoOutFile
                + " streamType:" + streamType
                + " enableSaveFile:" + enableSaveFile);
        if (waitRelease) {
            userLeftLatch = new CountDownLatch(1);
        }

        VideoSubscriptionOptions subscriptionOptions = new VideoSubscriptionOptions();
        if ("high".equals(streamType)) {
            subscriptionOptions.setType(Constants.VIDEO_STREAM_HIGH);
        } else if ("low".equals(streamType)) {
            subscriptionOptions.setType(Constants.VIDEO_STREAM_LOW);
        } else {
            return;
        }
        conn.getLocalUser().subscribeAllVideo(subscriptionOptions);

        sampleLocalUserObserver.setVideoFrameObserver(new SampleVideFrameObserver(videoOutFile) {
            @Override
            public void onFrame(AgoraVideoFrameObserver2 agora_video_frame_observer2, String channel_id,
                    String remote_uid,
                    VideoFrame frame) {
                if (null == frame) {
                    return;
                }

                if (enableSaveFile) {
                    int ylength = frame.getYBuffer().remaining();
                    int ulength = frame.getUBuffer().remaining();
                    int vlength = frame.getVBuffer().remaining();
                    byte[] data = new byte[ylength + ulength + vlength];
                    ByteBuffer buffer = ByteBuffer.wrap(data);
                    buffer.put(frame.getYBuffer());
                    buffer.position(ylength);
                    buffer.put(frame.getUBuffer());
                    buffer.position(ylength + ulength);
                    buffer.put(frame.getVBuffer());
                    writeVideoFrameToFile(data);
                }

                byte[] metaDataBufferData = null;
                byte[] alphaBufferData = null;

                if (null != frame.getMetadataBuffer() && frame.getMetadataBuffer().capacity() > 0) {
                    metaDataBufferData = new byte[frame.getMetadataBuffer().capacity()];
                    frame.getMetadataBuffer().get(metaDataBufferData);
                }

                if (null != frame.getAlphaBuffer() && frame.getAlphaBuffer().capacity() > 0) {
                    alphaBufferData = new byte[frame.getAlphaBuffer().capacity()];
                    frame.getAlphaBuffer().get(alphaBufferData);
                }

                final byte[] finalMetaDataBufferData = metaDataBufferData;
                final byte[] finalAlphaBufferData = alphaBufferData;
                logExecutorService.execute(() -> {
                    SampleLogger
                            .log("onFrame width:" + frame.getWidth() + " height:" + frame.getHeight() + " channel_id:"
                                    + channel_id
                                    + " remote_uid:" + remote_uid + " frame size:"
                                    + frame.getYBuffer().remaining() + " " + frame.getUBuffer().remaining() + " " +
                                    +frame.getVBuffer().remaining()
                                    + " with current channelId:" + channelId
                                    + "  userId:" + userId);

                    if (null != finalMetaDataBufferData) {
                        SampleLogger.log("onFrame metaDataBuffer :" + new String(finalMetaDataBufferData));
                    }

                    if (null != finalAlphaBufferData) {
                        SampleLogger.log("onFrame getAlphaBuffer size:" + finalAlphaBufferData.length + " mode:"
                                + frame.getAlphaMode());
                    }
                });

                if (testTime > 0 && System.currentTimeMillis() - testStartTime >= testTime * 1000
                        && null != userLeftLatch) {
                    userLeftLatch.countDown();
                }
            }
        });

        if (waitRelease) {
            try {
                userLeftLatch.await();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            releaseConn();
            if (null != callback) {
                callback.onTestFinished();
            }
        }
    }

    public void registerH264ObserverTask(String remoteUserId, String videoOutFile, String streamType,
            boolean waitRelease, boolean enableSaveFile) {
        SampleLogger.log("registerH264ObserverTask remoteUserId:" + remoteUserId + " videoOutFile:" + videoOutFile +
                " streamType:" + streamType
                + " enableSaveFile:" + enableSaveFile);
        if (waitRelease) {
            userLeftLatch = new CountDownLatch(1);
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
        conn.getLocalUser().subscribeAllVideo(subscriptionOptions);

        conn.getLocalUser()
                .registerVideoEncodedFrameObserver(
                        new AgoraVideoEncodedFrameObserver(new SampleVideoEncodedFrameObserver(videoOutFile) {
                            @Override
                            public int onEncodedVideoFrame(AgoraVideoEncodedFrameObserver observer, int uid,
                                    ByteBuffer buffer, EncodedVideoFrameInfo info) {
                                logExecutorService.execute(() -> {
                                    SampleLogger.log("onEncodedVideoFrame uid:" + uid + " length " + buffer.remaining()
                                            + " with current channelId:"
                                            + channelId
                                            + "  userId:" + userId + " info:" + info);
                                });
                                if (enableSaveFile) {
                                    byte[] byteArray = new byte[buffer.remaining()];
                                    buffer.get(byteArray);
                                    buffer.rewind();
                                    writeVideoDataToFile(byteArray);
                                }

                                if (testTime > 0
                                        && System.currentTimeMillis() - testStartTime >= testTime * 1000
                                        && null != userLeftLatch) {
                                    userLeftLatch.countDown();
                                }

                                return 1;
                            }
                        }));

        if (waitRelease) {
            try {
                userLeftLatch.await();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            releaseConn();
            if (null != callback) {
                callback.onTestFinished();
            }
        }
    }

    public void registerEncodedAudioObserverTask(String remoteUserId, String audioOutFile, String fileType,
            boolean waitRelease) {
        SampleLogger
                .log("registerEncodedAudioObserverTask remoteUserId:" + remoteUserId + " audioOutFile:" + audioOutFile +
                        " fileType:" + fileType);
        if (waitRelease) {
            userLeftLatch = new CountDownLatch(1);
        }

        conn.getLocalUser().subscribeAllAudio();

        sampleLocalUserObserver.setAudioEncodedFrameObserver(new SampleAudioEncodedFrameObserver(audioOutFile) {
            @Override
            public int onEncodedAudioFrameReceived(String remoteUserId, ByteBuffer buffer,
                    EncodedAudioFrameReceiverInfo info) {
                if (buffer == null || buffer.remaining() == 0) {
                    return 0;
                }
                logExecutorService.execute(() -> {
                    SampleLogger.log("onEncodedAudioFrameReceived buffer size:" + buffer.remaining() +
                            " info:" + info + " remoteUserId:" + remoteUserId +
                            " with current channelId:" + channelId);
                });
                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);
                buffer.rewind();

                writeAudioFrameToFile(byteArray, audioOutFile + "-" + remoteUserId + "." + fileType);
                if (testTime > 0 && System.currentTimeMillis() - testStartTime >= testTime * 1000
                        && null != userLeftLatch) {
                    userLeftLatch.countDown();
                }
                return 1;
            }
        });

        if (waitRelease) {
            try {
                userLeftLatch.await();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            releaseConn();
            if (null != callback) {
                callback.onTestFinished();
            }
        }
    }
}