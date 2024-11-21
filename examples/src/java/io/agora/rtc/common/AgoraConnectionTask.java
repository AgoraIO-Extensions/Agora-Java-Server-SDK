package io.agora.rtc.common;

import io.agora.rtc.AgoraAudioEncodedFrameSender;
import io.agora.rtc.AgoraAudioPcmDataSender;
import io.agora.rtc.AgoraLocalAudioTrack;
import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AgoraLocalVideoTrack;
import io.agora.rtc.AgoraMediaNodeFactory;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraVideoEncodedFrameObserver;
import io.agora.rtc.AgoraVideoEncodedImageSender;
import io.agora.rtc.AgoraVideoFrameObserver2;
import io.agora.rtc.AgoraVideoFrameSender;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.Constants;
import io.agora.rtc.DefaultAudioDeviceManagerObserver;
import io.agora.rtc.DefaultAudioFrameObserver;
import io.agora.rtc.DefaultLocalUserObserver;
import io.agora.rtc.DefaultRtcConnObserver;
import io.agora.rtc.EncodedAudioFrameInfo;
import io.agora.rtc.EncodedVideoFrameInfo;
import io.agora.rtc.EncryptionConfig;
import io.agora.rtc.ExternalVideoFrame;
import io.agora.rtc.Out;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.SenderOptions;
import io.agora.rtc.VideoDimensions;
import io.agora.rtc.VideoEncoderConfig;
import io.agora.rtc.VideoFrame;
import io.agora.rtc.VideoSubscriptionOptions;
import io.agora.rtc.mediautils.AacReader;
import io.agora.rtc.mediautils.H264Reader;
import io.agora.rtc.mediautils.MediaDecode;
import io.agora.rtc.mediautils.MediaDecodeUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AgoraConnectionTask {
    static {
        System.loadLibrary("mediautils");
    }

    private final AgoraService service;
    // in seconds
    private final long testDuration;
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

    private SampleLocalUserObserver localUserObserver;

    private String channelId;
    private String userId;
    private int userRole;
    private final Random random;
    private final ExecutorService singleExecutorService;
    private final ExecutorService testTaskExecutorService;
    private CountDownLatch userLeftLatch;

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

    public AgoraConnectionTask(AgoraService service, long testDuration) {
        this.service = service;
        this.testDuration = testDuration;
        this.testStartTime = System.currentTimeMillis();
        this.singleExecutorService = Executors.newSingleThreadExecutor();
        this.testTaskExecutorService = Executors.newCachedThreadPool();
        this.random = new Random();
    }

    public void setCallback(TaskCallback callback) {
        this.callback = callback;
    }

    public AgoraRtcConn getConn() {
        return conn;
    }

    public void createConnectionAndTest(RtcConnConfig ccfg, String token, String channelId, String userId,
            int enableEncryptionMode, int encryptionMode, String encryptionKey) {
        SampleLogger
                .log("createConnectionAndTest channelId:" + channelId + " userId:" + userId + " enableEncryptionMode:"
                        + enableEncryptionMode + " encryptionMode:" + encryptionMode + " encryptionKey:"
                        + encryptionKey);
        if (null == service) {
            SampleLogger.log("createAndInitAgoraService fail");
            return;
        }
        conn = service.agoraRtcConnCreate(ccfg);
        if (conn == null) {
            SampleLogger.log("AgoraService.agoraRtcConnCreate fail\n");
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
                if (testDuration == 0) {
                    if (null != userLeftLatch) {
                        userLeftLatch.countDown();
                    }
                }

                if (null != callback) {
                    callback.onUserLeft(user_id);
                }
            }
        });
        SampleLogger.log("registerObserver ret:" + ret);

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

        ret = conn.connect(token, channelId, userId);
        SampleLogger.log("Connecting to Agora channel " + channelId + " with userId " + userId + " ret:" + ret);

        conn.getLocalUser().registerObserver(new DefaultLocalUserObserver() {
            @Override
            public void onStreamMessage(AgoraLocalUser agora_local_user, String user_id, int stream_id, String data,
                    long length) {
                SampleLogger.log("onStreamMessage: userid " + user_id + " stream_id " + stream_id + "  data " + data);
                if (null != callback) {
                    callback.onStreamMessage(user_id, stream_id, data, length);
                }
            }

            @Override
            public void onAudioPublishStateChanged(AgoraLocalUser agora_local_user, String channel, int old_state,
                    int new_state, int elapse_since_last_state) {
                SampleLogger
                        .log("onAudioPublishStateChanged channel:" + channel + " old_state:" + old_state + " new_state:"
                                + new_state + " userRole:" + agora_local_user.getUserRole());
            }

            @Override
            public void onVideoPublishStateChanged(AgoraLocalUser agora_local_user, String channel, int old_state,
                    int new_state, int elapse_since_last_state) {
                // TODO Auto-generated method stub
                SampleLogger
                        .log("onVideoPublishStateChanged channel:" + channel + " old_state:" + old_state + " new_state:"
                                + new_state + " userRole:" + agora_local_user.getUserRole());
            }
        });

        mediaNodeFactory = service.createMediaNodeFactory();

        try {
            userRole = Integer.parseInt(userId);
        } catch (Exception e) {
            userRole = random.nextInt();
        }
        // conn.getLocalUser().setUserRole(userRole);

    }

    public void releaseConn() {
        SampleLogger.log("releaseConn for channelId:" + channelId + " userId:" + userId);
        if (conn == null) {
            return;
        }

        if (null != customAudioTrack && null != audioFrameSender) {
            customAudioTrack.clearBuffer();
            conn.getLocalUser().unpublishAudio(customAudioTrack);
        }

        if (null != customEncodedVideoTrack && null != customEncodedImageSender) {
            conn.getLocalUser().unpublishVideo(customEncodedVideoTrack);
        }

        if (null != customVideoTrack && null != videoFrameSender) {
            conn.getLocalUser().unpublishVideo(customVideoTrack);
        }

        if (null != customEncodedAudioTrack && null != audioEncodedFrameSender) {
            conn.getLocalUser().unpublishAudio(customEncodedAudioTrack);
        }

        if (null != localUserObserver) {
            localUserObserver.unsetAudioFrameObserver();
            localUserObserver.unsetVideoFrameObserver();
        }

        // Unregister connection observer
        conn.unregisterObserver();
        conn.getLocalUser().unregisterObserver();

        int ret = conn.disconnect();
        if (ret != 0) {
            SampleLogger.log("conn.disconnect fail ret=" + ret);
        }
        conn.destroy();
        conn = null;

        SampleLogger.log("Disconnected from Agora channel successfully\n");
    }

    public void sendPcmTask(String filePath, int interval, int numOfChannels, int sampleRate, boolean waitRelease) {
        SampleLogger
                .log("sendPcmTask filePath:" + filePath + " interval:" + interval + " numOfChannels:" + numOfChannels
                        + " sampleRate:" + sampleRate);
        audioFrameSender = mediaNodeFactory.createAudioPcmDataSender();
        // Create audio track
        customAudioTrack = service.createCustomAudioTrackPcm(audioFrameSender);
        customAudioTrack.setMaxBufferAudioFrameNumber(1000);
        conn.getLocalUser().publishAudio(customAudioTrack);
        AudioDataCache audioDataCache = new AudioDataCache(numOfChannels, sampleRate);

        int bufferSize = numOfChannels * (sampleRate / 1000) * interval * 2;
        byte[] buffer = new byte[bufferSize];

        FileSender pcmSendThread = new FileSender(filePath, interval) {
            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                if (data == null) {
                    return;
                }

                byte[] sendData = audioDataCache.getData();

                if (sendData == null) {
                    return;
                }

                int ret = audioFrameSender.send(sendData, 0,
                        audioDataCache.getSamplesPerChannel(sendData.length), 2,
                        numOfChannels,
                        sampleRate);
                SampleLogger.log("send pcm frame data size:" + sendData.length + " frame size:"
                        + (sendData.length / audioDataCache.getOneFrameSize()) + " sampleRate:" + sampleRate
                        + " numOfChannels:" + numOfChannels
                        + " to channelId:"
                        + channelId + " from userId:" + userId + " ret:" + ret + " testStartTime:"
                        + Utils.formatTimestamp(testStartTime)
                        + " testDuration:" + testDuration);
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                if (fos != null) {
                    try {
                        int size = fos.read(buffer, 0, bufferSize);
                        if (testDuration > 0) {
                            if (System.currentTimeMillis() - testStartTime >= testDuration * 1000) {
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
                audioDataCache.pushData(buffer);
                return buffer;
            }
        };

        pcmSendThread.start();

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

    public void sendH264Task(String filePath, int interval, int height, int width, boolean waitRelease) {
        SampleLogger.log("sendH264Task filePath:" + filePath + " interval:" + interval + " height:" + height + " width:"
                + width);
        customEncodedImageSender = mediaNodeFactory.createVideoEncodedImageSender();
        // Create video track
        SenderOptions option = new SenderOptions();
        option.setCcMode(1);
        customEncodedVideoTrack = service.createCustomVideoTrackEncoded(customEncodedImageSender, option);
        // Publish video track
        int ret = conn.getLocalUser().publishVideo(customEncodedVideoTrack);
        SampleLogger.log("sendH264Task publishVideo ret:" + ret);

        H264Reader h264Reader = new H264Reader(filePath);
        int fps = 1000 / interval;

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
                info.setWidth(width);
                info.setHeight(height);
                info.setCodecType(Constants.VIDEO_CODEC_H264);
                info.setCaptureTimeMs(currentTime);
                info.setDecodeTimeMs(currentTime);
                info.setFramesPerSecond(fps);
                info.setRotation(0);
                customEncodedImageSender.send(data, data.length, info);
                frameIndex++;
                SampleLogger.log("send h264 frame data size:" + data.length +
                        " timestamp:" + timestamp + " frameIndex:" + frameIndex + " testStartTime:"
                        + Utils.formatTimestamp(testStartTime)
                        + " currentTime:" + Utils.getCurrentTime() + " testDuration:" +
                        testDuration);
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                H264Reader.H264Frame frame = h264Reader.readNextFrame();
                if (testDuration > 0) {
                    if (System.currentTimeMillis() - testStartTime >= testDuration * 1000) {
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
                    return frame.data;
                } else {
                    return null;
                }
            }
        };

        h264SendThread.start();
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

    public void sendYuvTask(String filePath, int interval, int height, int width, int fps, boolean waitRelease) {
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

        int bufferLen = (int) (height * width * 1.5);
        byte[] buffer = new byte[bufferLen];

        FileSender yuvSender = new FileSender(filePath, interval) {
            private int frameIndex = 0;

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
                ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
                buffer.put(data);
                externalVideoFrame.setBuffer(buffer);
                externalVideoFrame.setRotation(0);
                externalVideoFrame.setFormat(Constants.EXTERNAL_VIDEO_FRAME_PIXEL_FORMAT_I420);
                externalVideoFrame.setStride(width);
                externalVideoFrame.setType(Constants.EXTERNAL_VIDEO_FRAME_BUFFER_TYPE_RAW_DATA);
                externalVideoFrame.setTimestamp(timestamp);
                String testMetaData = "testMetaData";
                externalVideoFrame.setMetadataBuffer(testMetaData.getBytes());
                externalVideoFrame.setMetadataSize(testMetaData.getBytes().length);
                videoFrameSender.send(externalVideoFrame);
                frameIndex++;

                SampleLogger.log("send yuv frame data size:" + data.length +
                        " timestamp:" + timestamp + " frameIndex:" + frameIndex + " testStartTime:"
                        + Utils.formatTimestamp(testStartTime)
                        + " currentTime:" + Utils.getCurrentTime() + " testDuration:" +
                        testDuration);

            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                if (fos == null) {
                    return null;
                }
                try {
                    int size = fos.read(buffer, 0, bufferLen);
                    if (testDuration > 0) {
                        if (System.currentTimeMillis() - testStartTime >= testDuration * 1000) {
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
        };

        yuvSender.start();
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

    public void sendAacTask(String filePath, int interval, int numOfChannels, int sampleRate, boolean waitRelease) {
        SampleLogger
                .log("sendAacTask filePath:" + filePath + " interval:" + interval + " numOfChannels:" + numOfChannels
                        + " sampleRate:" + sampleRate);
        // Create audio track
        audioEncodedFrameSender = mediaNodeFactory.createAudioEncodedFrameSender();
        customEncodedAudioTrack = service.createCustomAudioTrackEncoded(audioEncodedFrameSender, 0);
        conn.getLocalUser().publishAudio(customEncodedAudioTrack);

        int bufferSize = numOfChannels * sampleRate * 2 * interval / 1000;
        byte[] buffer = new byte[bufferSize];
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

                int ret = audioEncodedFrameSender.send(data, data.length, encodedInfo);
                SampleLogger.log("send aac frame data size:" + data.length + " timestamp:"
                        + timestamp + " sampleRate:" + sampleRate + " numOfChannels:" + numOfChannels
                        + " to channelId:"
                        + channelId + " from userId:" + userId + " ret:" + ret + " testStartTime:"
                        + Utils.formatTimestamp(testStartTime)
                        + " currentTime:" + Utils.getCurrentTime() + " testDuration:" +
                        testDuration);
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                AacReader.AacFrame aacFrame = aacReader.getAudioFrame(interval);
                if (testDuration > 0) {
                    if (System.currentTimeMillis() - testStartTime >= testDuration * 1000) {
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
        };

        aacSendThread.start();

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

    public void sendAvMediaTask(String filePath, int interval) {
        SampleLogger.log("sendAvMediaTask filePath:" + filePath + " interval:" + interval);
        MediaDecodeUtils mediaDecodeUtils = new MediaDecodeUtils();

        boolean initRet = mediaDecodeUtils.init(filePath, interval, (int) testDuration,
                new MediaDecodeUtils.MediaDecodeCallback() {
                    @Override
                    public void onAudioFrame(MediaDecode.MediaFrame frame, long basePts) {
                        if (audioFrameSender == null) {
                            audioFrameSender = mediaNodeFactory.createAudioPcmDataSender();
                            // Create audio track
                            customAudioTrack = service.createCustomAudioTrackPcm(audioFrameSender);
                            customAudioTrack.setMaxBufferAudioFrameNumber(1000);
                            conn.getLocalUser().publishAudio(customAudioTrack);
                        }

                        int ret = audioFrameSender.send(frame.buffer, (int) (frame.pts + basePts),
                                frame.samples, frame.bytesPerSample,
                                frame.channels,
                                frame.sampleRate);
                        SampleLogger.log("SendPcmData frame.pts:" + frame.pts + " ret:" + ret);
                    }

                    @Override
                    public void onVideoFrame(MediaDecode.MediaFrame frame, long basePts) {
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
                        ByteBuffer buffer = ByteBuffer.allocateDirect(frame.buffer.length);
                        buffer.put(frame.buffer);
                        externalVideoFrame.setBuffer(buffer);
                        externalVideoFrame.setHeight(frame.height);
                        externalVideoFrame.setStride(frame.stride);
                        externalVideoFrame.setRotation(0);
                        externalVideoFrame.setFormat(Constants.EXTERNAL_VIDEO_FRAME_PIXEL_FORMAT_I420);
                        externalVideoFrame.setType(Constants.EXTERNAL_VIDEO_FRAME_BUFFER_TYPE_RAW_DATA);
                        externalVideoFrame.setTimestamp(frame.pts + basePts);
                        int ret = videoFrameSender.send(externalVideoFrame);
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
            boolean waitRelease, boolean enableSaveFile) {
        SampleLogger.log("registerPcmObserverTask remoteUserId:" + remoteUserId + " audioOutFile:" + audioOutFile
                + " numOfChannels:"
                + numOfChannels + " sampleRate:" + sampleRate + " waitRelease:" + waitRelease + " enableSaveFile:"
                + enableSaveFile);
        if (waitRelease) {
            userLeftLatch = new CountDownLatch(1);
        }

        conn.getLocalUser().subscribeAllAudio();
        // Register local user observer
        if (null == localUserObserver) {
            localUserObserver = new SampleLocalUserObserver(conn.getLocalUser());
        }
        conn.getLocalUser().registerObserver(localUserObserver);

        // Register audio frame observer to receive audio stream
        int ret = conn.getLocalUser().setPlaybackAudioFrameBeforeMixingParameters(numOfChannels, sampleRate);
        SampleLogger.log("setPlaybackAudioFrameBeforeMixingParameters numOfChannels:" + numOfChannels + " sampleRate:"
                + sampleRate);
        if (ret > 0) {
            SampleLogger.log("setPlaybackAudioFrameBeforeMixingParameters fail ret=" + ret);
            return;
        }
        localUserObserver.setAudioFrameObserver(new SampleAudioFrameObserver(audioOutFile) {
            @Override
            public int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agora_local_user, String channel_id, String uid,
                    AudioFrame frame) {
                if (null == frame) {
                    return 0;
                }
                int writeBytes = frame.getSamplesPerChannel() * frame.getChannels() * 2;
                // Write PCM samples
                SampleLogger.log("onPlaybackAudioFrameBeforeMixing audioFrame size " + frame.getBuffer().capacity()
                        + " channel_id:"
                        + channel_id + " uid:" + uid + " writeBytes:" + writeBytes + " with current channelId:"
                        + channelId
                        + "  userId:" + userId);
                if (enableSaveFile) {
                    writeAudioFrameToFile(frame.getBuffer());
                }
                if (testDuration > 0 && System.currentTimeMillis() - testStartTime >= testDuration * 1000
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
            boolean waitRelease) {
        SampleLogger.log("registerYuvObserverTask remoteUserId:" + remoteUserId + " videoOutFile:" + videoOutFile);
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

        // Register local user observer
        if (null == localUserObserver) {
            localUserObserver = new SampleLocalUserObserver(conn.getLocalUser());
        }
        conn.getLocalUser().registerObserver(localUserObserver);

        localUserObserver.setVideoFrameObserver(new SampleVideFrameObserver(videoOutFile) {
            @Override
            public void onFrame(AgoraVideoFrameObserver2 agora_video_frame_observer2, String channel_id,
                    String remote_uid,
                    VideoFrame frame) {
                if (null == frame) {
                    return;
                }

                SampleLogger.log("onFrame width:" + frame.getWidth() + " height:" + frame.getHeight() + " channel_id:"
                        + channel_id
                        + " remote_uid:" + remote_uid + " frame size:"
                        + (frame.getYBuffer().remaining() + frame.getUBuffer().remaining()
                                + frame.getVBuffer().remaining())
                        + " with current channelId:" + channelId
                        + "  userId:" + userId);
                if (null != frame.getMetadataBuffer()) {
                    SampleLogger.log("onFrame metaDataBuffer:" + frame.getMetadataBuffer());
                }

                writeVideoFrameToFile(frame);

                if (testDuration > 0 && System.currentTimeMillis() - testStartTime >= testDuration * 1000
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
        SampleLogger.log("registerH264ObserverTask remoteUserId:" + remoteUserId + " videoOutFile:" + videoOutFile
                + " streamType:" + streamType + " waitRelease:" + waitRelease + " enableSaveFile:" + enableSaveFile);
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
        // Register local user observer
        if (null == localUserObserver) {
            localUserObserver = new SampleLocalUserObserver(conn.getLocalUser());
        }
        conn.getLocalUser().registerObserver(localUserObserver);

        conn.getLocalUser()
                .registerVideoEncodedFrameObserver(
                        new AgoraVideoEncodedFrameObserver(new SampleVideoEncodedFrameObserver(videoOutFile) {
                            @Override
                            public int onEncodedVideoFrame(AgoraVideoEncodedFrameObserver observer, int uid,
                                    ByteBuffer buffer, EncodedVideoFrameInfo info) {
                                SampleLogger.log("onEncodedVideoFrame uid:" + uid
                                        + " with current channelId:"
                                        + channelId
                                        + "  userId:" + userId);
                                if (enableSaveFile) {
                                    writeVideoDataToFile(buffer);
                                }

                                if (testDuration > 0
                                        && System.currentTimeMillis() - testStartTime >= testDuration * 1000
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
}