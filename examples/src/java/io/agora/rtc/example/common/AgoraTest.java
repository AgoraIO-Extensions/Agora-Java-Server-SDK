package io.agora.rtc.example.common;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.example.utils.Utils;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class AgoraTest {
    protected final ExecutorService singleExecutorService = Executors.newSingleThreadExecutor();
    private final ThreadPoolExecutor testTaskExecutorService = new ThreadPoolExecutor(
            0,
            Integer.MAX_VALUE,
            1L,
            TimeUnit.SECONDS,
            new SynchronousQueue<>());

    protected static volatile AgoraService service;

    protected AtomicInteger testTaskCount = new AtomicInteger(0);

    class UserIdHolder {
        private volatile String userId;

        void set(String id) {
            this.userId = id;
        }

        String get() {
            return this.userId;
        }
    }

    public enum TestTask {
        NONE, SEND_PCM, SEND_YUV, SEND_YUV_DUAL_STREAM, SEND_H264, SEND_H264_DUAL_STREAM, SEND_AAC, SEND_OPUS, SEND_MP4,
        SEND_RGBA_PCM,
        SEND_VP8_PCM,
        SEND_DATA_STREAM,
        SEND_PCM_YUV,
        SEND_PCM_H264,
        RECEIVE_PCM, RECEIVE_YUV,
        RECEIVE_H264, RECEIVE_PCM_H264, RECEIVE_MIXED_AUDIO, RECEIVE_ENCODED_AUDIO,
        SEND_RECEIVE_PCM_YUV
    }

    class SignalFunc implements SignalHandler {
        public void handle(Signal arg0) {
            SampleLogger.log("catch signal " + arg0);
            testTaskCount.set(0);
        }
    }

    public AgoraTest() {
        String[] keys = Utils.readAppIdAndToken(".keys");
        ArgsConfig.appId = keys[0];
        ArgsConfig.token = keys[1];
        SampleLogger.log("read APPID: " + ArgsConfig.appId + " TOKEN: " + ArgsConfig.token + " from .keys");
    }

    protected static void startTest(String[] args, AgoraTest test) {
        ArgsConfig.handleOptions(args);
        test.sdkTest();
    }

    public void sdkTest() {
        SignalFunc handler = new SignalFunc();
        Signal.handle(new Signal("ABRT"), handler);
        Signal.handle(new Signal("INT"), handler);
        setup();
        while (testTaskCount.get() != 0) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cleanup();
        System.exit(0);
    }

    public void setup() {
        SampleLogger.log("setup");
        File testDataOutFile = new File("test_data_out/");
        if (!testDataOutFile.exists()) {
            testDataOutFile.mkdirs();
        }

        SampleLogger.enableLog(ArgsConfig.enableLog);

        if (ArgsConfig.connectionCount <= 0) {
            ArgsConfig.connectionCount = 1;
        }

        SampleLogger.log(
                "connectionCount:" + ArgsConfig.connectionCount + " enableStringUid:" + ArgsConfig.enableStringUid);

        service = SampleCommon.createAndInitAgoraService(0, 1, 1, ArgsConfig.enableStringUid ? 1 : 0, ArgsConfig.appId);

        // AgoraParameter parameter = service.getAgoraParameter();
        // parameter.setParameters("{\"che.audio.custom_payload_type\":78}");
        // parameter.setParameters("{\"che.audio.aec.enable\":false}");
        // parameter.setParameters("{\"che.audio.ans.enable\":false}");
        // parameter.setParameters("{\"che.audio.agc.enable\":false}");
        // parameter.setParameters("{\"che.audio.custom_bitrate\":128000}");
        // parameter.setParameters(
        // "{\"che.audio.frame_dump\":{\"location\":\"all\",\"action\":\"start\",\"max_size_bytes\":\"100000000\",\"uuid\":\"123456789\",
        // \"duration\": \"150000\"}}");
    }

    protected boolean createConnectionAndTest(RtcConnConfig ccfg, String channelId, String userId, TestTask testTask,
            long testTime) {
        SampleLogger.log("createConnectionAndTest start ccfg:" + ccfg + " channelId:" + channelId + " userId:" + userId
                + " testTask:" + testTask + " testTime:" + testTime);
        testTaskCount.incrementAndGet();
        SampleLogger.log("test start testTaskCount:" + testTaskCount.get());
        testTaskExecutorService.execute(() -> {
            UserIdHolder threadLocalUserId = new UserIdHolder();
            final CountDownLatch testFinishLatch = new CountDownLatch(1);
            final CountDownLatch connectedLatch = new CountDownLatch(1);
            AgoraConnectionTask connTask = new AgoraConnectionTask(service, testTime);
            connTask.setCallback(new AgoraConnectionTask.TaskCallback() {
                @Override
                public void onConnected(String userId) {
                    threadLocalUserId.set(userId);
                    connectedLatch.countDown();
                }

                @Override
                public void onTestFinished() {
                    testTaskCount.decrementAndGet();
                    testFinishLatch.countDown();
                }

                @Override
                public void onStreamMessage(String userId, int streamId, String data, long length) {
                    onStreamMessageReceive(userId, streamId, data, length);
                }
            });
            connTask.createConnection(ccfg, channelId, userId);

            try {
                connectedLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onConnected(connTask.getConn(), channelId, threadLocalUserId.get(), testTask);

            if (TestTask.SEND_PCM == testTask) {
                connTask.sendPcmTask(ArgsConfig.audioFile, 10, ArgsConfig.numOfChannels, ArgsConfig.sampleRate, true);
            } else if (TestTask.SEND_PCM_YUV == testTask) {
                connTask.sendPcmTask(ArgsConfig.audioFile, 10, ArgsConfig.numOfChannels, ArgsConfig.sampleRate, false);
                connTask.sendYuvTask(ArgsConfig.videoFile, 1000 / ArgsConfig.fps, ArgsConfig.height, ArgsConfig.width,
                        ArgsConfig.fps, Constants.VIDEO_STREAM_HIGH, true);
            } else if (TestTask.SEND_PCM_H264 == testTask) {
                connTask.sendPcmTask(ArgsConfig.audioFile, 10, ArgsConfig.numOfChannels, ArgsConfig.sampleRate, false);
                connTask.sendH264Task(ArgsConfig.videoFile, 1000 / ArgsConfig.fps, 0, 0,
                        Constants.VIDEO_STREAM_HIGH, true);
            } else if (TestTask.SEND_AAC == testTask) {
                connTask.sendAacTask(ArgsConfig.audioFile, 20, ArgsConfig.numOfChannels, ArgsConfig.sampleRate, true);
            } else if (TestTask.SEND_OPUS == testTask) {
                connTask.sendOpusTask(ArgsConfig.audioFile, 20, true);
            } else if (TestTask.SEND_YUV == testTask) {
                connTask.sendYuvTask(ArgsConfig.videoFile, 1000 / ArgsConfig.fps, ArgsConfig.height, ArgsConfig.width,
                        ArgsConfig.fps, Constants.VIDEO_STREAM_HIGH,
                        true);
            } else if (TestTask.SEND_YUV_DUAL_STREAM == testTask) {
                connTask.sendYuvTask(ArgsConfig.videoFile, 1000 / ArgsConfig.fps, ArgsConfig.height, ArgsConfig.width,
                        ArgsConfig.fps, Constants.VIDEO_STREAM_HIGH, true);
            } else if (TestTask.SEND_H264 == testTask) {
                connTask.sendH264Task(ArgsConfig.videoFile, 1000 / ArgsConfig.fps, 0, 0,
                        Constants.VIDEO_STREAM_HIGH, true);
            } else if (TestTask.SEND_H264_DUAL_STREAM == testTask) {
                connTask.sendH264Task(ArgsConfig.highVideoFile, 1000 / ArgsConfig.fps, 0, 0,
                        Constants.VIDEO_STREAM_HIGH, false);
                connTask.sendH264Task(ArgsConfig.lowVideoFile, 1000 / ArgsConfig.fps, 0, 0,
                        Constants.VIDEO_STREAM_LOW, true);
            } else if (TestTask.SEND_RGBA_PCM == testTask) {
                connTask.sendPcmTask(ArgsConfig.audioFile, 10, ArgsConfig.numOfChannels, ArgsConfig.sampleRate, false);
                connTask.sendRgbaTask(ArgsConfig.videoFile, 1000 / ArgsConfig.fps, ArgsConfig.height, ArgsConfig.width,
                        ArgsConfig.fps, true);
            } else if (TestTask.SEND_VP8_PCM == testTask) {
                connTask.sendPcmTask(ArgsConfig.audioFile, 10, ArgsConfig.numOfChannels, ArgsConfig.sampleRate, false);
                connTask.sendVp8Task(ArgsConfig.videoFile, 1000 / ArgsConfig.fps, ArgsConfig.height, ArgsConfig.width,
                        ArgsConfig.fps, Constants.VIDEO_STREAM_HIGH, true);
            } else if (TestTask.SEND_MP4 == testTask) {
                connTask.sendAvMediaTask(ArgsConfig.audioFile, 50);
            } else if (TestTask.SEND_DATA_STREAM == testTask) {
                connTask.sendDataStreamTask(1, 50, true);
            } else if (TestTask.RECEIVE_PCM == testTask) {
                connTask.registerPcmObserverTask(ArgsConfig.remoteUserId,
                        ("".equals(ArgsConfig.audioOutFile)) ? ""
                                : (ArgsConfig.audioOutFile + "_" + channelId + "_" + threadLocalUserId.get() + ".pcm"),
                        ArgsConfig.numOfChannels,
                        ArgsConfig.sampleRate, true);
            } else if (TestTask.RECEIVE_PCM_H264 == testTask) {
                connTask.registerPcmObserverTask(ArgsConfig.remoteUserId,
                        ("".equals(ArgsConfig.audioOutFile)) ? ""
                                : (ArgsConfig.audioOutFile + "_" + channelId + "_" + threadLocalUserId.get() + ".pcm"),
                        ArgsConfig.numOfChannels,
                        ArgsConfig.sampleRate, false);
                connTask.registerH264ObserverTask(ArgsConfig.remoteUserId,
                        ("".equals(ArgsConfig.videoOutFile)) ? ""
                                : (ArgsConfig.videoOutFile + "_" + channelId + "_" + threadLocalUserId.get() + ".h264"),
                        ArgsConfig.streamType, true);
            } else if (TestTask.RECEIVE_MIXED_AUDIO == testTask) {
                connTask.registerMixedAudioObserverTask(ArgsConfig.remoteUserId,
                        ("".equals(ArgsConfig.audioOutFile)) ? ""
                                : (ArgsConfig.audioOutFile + "_" + channelId + "_" + threadLocalUserId.get() + ".pcm"),
                        ArgsConfig.numOfChannels,
                        ArgsConfig.sampleRate, true);
            } else if (TestTask.RECEIVE_YUV == testTask) {
                connTask.registerYuvObserverTask(ArgsConfig.remoteUserId,
                        ("".equals(ArgsConfig.videoOutFile)) ? ""
                                : (ArgsConfig.videoOutFile + "_" + channelId + "_" + threadLocalUserId.get() + ".yuv"),
                        ArgsConfig.streamType, true);
            } else if (TestTask.RECEIVE_H264 == testTask) {
                connTask.registerH264ObserverTask(ArgsConfig.remoteUserId,
                        ("".equals(ArgsConfig.videoOutFile)) ? ""
                                : (ArgsConfig.videoOutFile + "_" + channelId + "_" + threadLocalUserId.get() + ".h264"),
                        ArgsConfig.streamType, true);
            } else if (TestTask.RECEIVE_ENCODED_AUDIO == testTask) {
                connTask.registerEncodedAudioObserverTask("",
                        ("".equals(ArgsConfig.audioOutFile)) ? "" : ArgsConfig.audioOutFile, ArgsConfig.fileType,
                        true);
            } else if (TestTask.SEND_RECEIVE_PCM_YUV == testTask) {
                connTask.sendPcmTask(ArgsConfig.audioFile, 10, ArgsConfig.numOfChannels, ArgsConfig.sampleRate, false);
                connTask.sendYuvTask(ArgsConfig.videoFile, 1000 / ArgsConfig.fps, ArgsConfig.height, ArgsConfig.width,
                        ArgsConfig.fps, Constants.VIDEO_STREAM_HIGH, false);
                connTask.registerPcmObserverTask(ArgsConfig.remoteUserId,
                        ("".equals(ArgsConfig.audioOutFile)) ? ""
                                : (ArgsConfig.audioOutFile + "_" + channelId + "_" + threadLocalUserId.get() + ".pcm"),
                        ArgsConfig.numOfChannels,
                        ArgsConfig.sampleRate, false);
                connTask.registerYuvObserverTask(ArgsConfig.remoteUserId,
                        ("".equals(ArgsConfig.videoOutFile)) ? ""
                                : (ArgsConfig.videoOutFile + "_" + channelId + "_" + threadLocalUserId.get() + ".yuv"),
                        ArgsConfig.streamType, true);
            }

            if (TestTask.NONE != testTask) {
                try {
                    testFinishLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(testTime * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                connTask.releaseConn();
            }

            connTask = null;
            System.gc();
            SampleLogger.log("test finished for connTask:" + testTask + " channelId:" + channelId + " userId:"
                    + threadLocalUserId.get() + " with left testTaskCount:" + testTaskCount.get() + "\n");
        });
        return true;
    }

    protected void onConnected(AgoraRtcConn conn, String channelId, String userId, TestTask testTask) {
        SampleLogger.log("onConnected for task:" + testTask + " channelId:" + channelId + " userId:" + userId);
    }

    protected void onStreamMessageReceive(String userId, int streamId, String data, long length) {
    }

    public void cleanup() {
        SampleLogger.log("cleanup");
        singleExecutorService.shutdown();
        testTaskExecutorService.shutdown();
        // connPool.releaseAllConn();
        // Destroy Agora Service
        if (null != service) {
            service.destroy();
            service = null;
        }
    }
}