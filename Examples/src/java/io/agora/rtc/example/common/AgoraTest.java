package io.agora.rtc.example.common;

import io.agora.rtc.AgoraMediaNodeFactory;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.AgoraParameter;

import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    protected static volatile AgoraMediaNodeFactory mediaNodeFactory;

    protected AtomicInteger testTaskCount = new AtomicInteger(0);
    private final Object taskCountLock = new Object();
    private volatile boolean exitTest = false;
    private final List<AgoraConnectionTask> connTasksList = new CopyOnWriteArrayList<>();

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
        String[] keys = io.agora.rtc.example.utils.Utils.readAppIdAndToken(".keys");
        ArgsConfig.appId = keys[0];
        ArgsConfig.token = keys[1];
        SampleLogger.log("read APPID: " + ArgsConfig.appId + " TOKEN: " + ArgsConfig.token + " from .keys");
    }

    protected static void startTest(String[] args, AgoraTest test) {
        SampleLogger.log("startTest:" + test.getClass().getName());
        ArgsConfig.handleOptions(args);
        test.sdkTest();
    }

    public void sdkTest() {
        exitTest = false;
        SignalFunc handler = new SignalFunc() {
            public void handle(Signal sig) {
                SampleLogger.error("Received SIGABRT signal");
                Thread.dumpStack();
            }
        };
        Signal.handle(new Signal("ABRT"), handler);
        Signal.handle(new Signal("INT"), handler);
        setup();
        // Start a new thread to listen for console input
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String input = scanner.nextLine();
                if ("1".equals(input)) {
                    SampleLogger.log("exit test");
                    exitTest = true;
                    break;
                }
            }
            scanner.close();
        }).start();

        while (testTaskCount.get() != 0 && !exitTest) {
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

        mediaNodeFactory = service.createMediaNodeFactory();

        // AgoraParameter parameter = service.getAgoraParameter();
        // parameter.setParameters("{\"rtc.enable_nasa2\":false}");
        // parameter.setParameters("{\"che.video.useSimpleParser\":true}");
        // parameter.setParameters("{\"rtc.video.enable_periodic_strategy\":false}");

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
        synchronized (taskCountLock) {
            testTaskCount.incrementAndGet();
            System.out.println(Utils.formatTimestamp(System.currentTimeMillis())
                    + " testTaskCount:" + testTaskCount.get() + " testTask:" + testTask + " channelId:" + channelId
                    + " userId:" + userId);
            SampleLogger.log("test start testTaskCount:" + testTaskCount.get());

            if (ArgsConfig.maxTestTaskCount != 0 && testTaskCount.get() > ArgsConfig.maxTestTaskCount) {
                testTaskCount.decrementAndGet();
                SampleLogger.log("testTaskCount:" + testTaskCount.get() + " is more than maxTestTaskCount:"
                        + ArgsConfig.maxTestTaskCount);
                System.exit(1);
                return false;
            }
        }
        testTaskExecutorService.execute(() -> {
            UserIdHolder threadLocalUserId = new UserIdHolder();
            final CountDownLatch testFinishLatch = new CountDownLatch(1);
            final CountDownLatch connectedLatch = new CountDownLatch(1);
            AtomicInteger leftTestTaskCount = new AtomicInteger(0);
            AgoraConnectionTask connTask = new AgoraConnectionTask(service, mediaNodeFactory, testTime);
            connTasksList.add(connTask);
            connTask.setCallback(new AgoraConnectionTask.TaskCallback() {
                @Override
                public void onConnected(String userId) {
                    threadLocalUserId.set(userId);
                    connectedLatch.countDown();
                }

                @Override
                public void onUserJoined(String userId) {
                }

                @Override
                public void onTestFinished() {
                    synchronized (taskCountLock) {
                        int remaining = testTaskCount.decrementAndGet();
                        leftTestTaskCount.set(remaining);
                        testFinishLatch.countDown();
                    }
                }

                @Override
                public void onStreamMessage(String userId, int streamId, byte[] data) {
                    onStreamMessageReceive(userId, streamId, data);
                }
            });
            try {
                connTask.createConnection(ccfg, channelId, userId);
            } catch (Exception e) {
                e.printStackTrace();
                SampleLogger.log("createConnection failed, exit");
                System.exit(1);
            }

            try {
                boolean connected = connectedLatch.await(5, TimeUnit.SECONDS);
                if (!connected) {
                    SampleLogger.error("createConnectionAndTest connect timeout for testTask:" + testTask
                            + " channelId:" + channelId + " userId:" + userId + " and exit current test");
                    connTask.releaseConn();
                    connTasksList.remove(connTask);
                    connTask = null;
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onConnected(connTask.getConn(), channelId, threadLocalUserId.get(), testTask);

            switch (testTask) {
                case SEND_PCM:
                    connTask.sendPcmTask(ArgsConfig.audioFile, 10, ArgsConfig.numOfChannels, ArgsConfig.sampleRate,
                            true);
                    break;
                case SEND_PCM_YUV:
                    connTask.sendPcmTask(ArgsConfig.audioFile, 10, ArgsConfig.numOfChannels, ArgsConfig.sampleRate,
                            false);
                    connTask.sendYuvTask(ArgsConfig.videoFile, 1000 / ArgsConfig.fps, ArgsConfig.height,
                            ArgsConfig.width,
                            ArgsConfig.fps, Constants.VIDEO_STREAM_HIGH, true);
                    break;
                case SEND_PCM_H264:
                    connTask.sendPcmTask(ArgsConfig.audioFile, 10, ArgsConfig.numOfChannels, ArgsConfig.sampleRate,
                            false);
                    connTask.sendH264Task(ArgsConfig.videoFile, 1000 / ArgsConfig.fps, 0, 0,
                            Constants.VIDEO_STREAM_HIGH, true);
                    break;
                case SEND_AAC:
                    connTask.sendAacTask(ArgsConfig.audioFile, 20, ArgsConfig.numOfChannels, ArgsConfig.sampleRate,
                            true);
                    break;
                case SEND_OPUS:
                    connTask.sendOpusTask(ArgsConfig.audioFile, 20, true);
                    break;
                case SEND_YUV:
                    connTask.sendYuvTask(ArgsConfig.videoFile, 1000 / ArgsConfig.fps, ArgsConfig.height,
                            ArgsConfig.width,
                            ArgsConfig.fps, Constants.VIDEO_STREAM_HIGH, true);
                    break;
                case SEND_YUV_DUAL_STREAM:
                    connTask.sendYuvTask(ArgsConfig.videoFile, 1000 / ArgsConfig.fps, ArgsConfig.height,
                            ArgsConfig.width,
                            ArgsConfig.fps, Constants.VIDEO_STREAM_HIGH, true);
                    break;
                case SEND_H264:
                    connTask.sendH264Task(ArgsConfig.videoFile, 1000 / ArgsConfig.fps, ArgsConfig.height,
                            ArgsConfig.width, Constants.VIDEO_STREAM_HIGH, true);
                    break;
                case SEND_H264_DUAL_STREAM:
                    connTask.sendH264Task(ArgsConfig.highVideoFile, 1000 / ArgsConfig.fps, ArgsConfig.height,
                            ArgsConfig.width, Constants.VIDEO_STREAM_HIGH, false);
                    connTask.sendH264Task(ArgsConfig.lowVideoFile, 1000 / ArgsConfig.fps, ArgsConfig.height,
                            ArgsConfig.width, Constants.VIDEO_STREAM_LOW, true);
                    break;
                case SEND_RGBA_PCM:
                    connTask.sendPcmTask(ArgsConfig.audioFile, 10, ArgsConfig.numOfChannels, ArgsConfig.sampleRate,
                            false);
                    connTask.sendRgbaTask(ArgsConfig.videoFile, 1000 / ArgsConfig.fps, ArgsConfig.height,
                            ArgsConfig.width,
                            ArgsConfig.fps, true);
                    break;
                case SEND_VP8_PCM:
                    connTask.sendPcmTask(ArgsConfig.audioFile, 10, ArgsConfig.numOfChannels, ArgsConfig.sampleRate,
                            false);
                    connTask.sendVp8Task(ArgsConfig.videoFile, 1000 / ArgsConfig.fps, ArgsConfig.height,
                            ArgsConfig.width,
                            ArgsConfig.fps, Constants.VIDEO_STREAM_HIGH, true);
                    break;
                case SEND_MP4:
                    connTask.sendAvMediaTask(ArgsConfig.audioFile, 50);
                    break;
                case SEND_DATA_STREAM:
                    connTask.sendDataStreamTask(1, 50, true);
                    break;
                case RECEIVE_PCM:
                    connTask.registerPcmObserverTask(ArgsConfig.remoteUserId,
                            ("".equals(ArgsConfig.audioOutFile)) ? ""
                                    : (ArgsConfig.audioOutFile + "_" + channelId + "_" + threadLocalUserId.get()
                                            + ".pcm"),
                            ArgsConfig.numOfChannels,
                            ArgsConfig.sampleRate, true);
                    break;
                case RECEIVE_PCM_H264:
                    connTask.registerPcmObserverTask(ArgsConfig.remoteUserId,
                            ("".equals(ArgsConfig.audioOutFile)) ? ""
                                    : (ArgsConfig.audioOutFile + "_" + channelId + "_" + threadLocalUserId.get()
                                            + ".pcm"),
                            ArgsConfig.numOfChannels,
                            ArgsConfig.sampleRate, false);
                    connTask.registerH264ObserverTask(ArgsConfig.remoteUserId,
                            ("".equals(ArgsConfig.videoOutFile)) ? ""
                                    : (ArgsConfig.videoOutFile + "_" + channelId + "_" + threadLocalUserId.get()
                                            + ".h264"),
                            ArgsConfig.streamType, true);
                    break;
                case RECEIVE_MIXED_AUDIO:
                    connTask.registerMixedAudioObserverTask(ArgsConfig.remoteUserId,
                            ("".equals(ArgsConfig.audioOutFile)) ? ""
                                    : (ArgsConfig.audioOutFile + "_" + channelId + "_" + threadLocalUserId.get()
                                            + ".pcm"),
                            ArgsConfig.numOfChannels,
                            ArgsConfig.sampleRate, true);
                    break;
                case RECEIVE_YUV:
                    connTask.registerYuvObserverTask(ArgsConfig.remoteUserId,
                            ("".equals(ArgsConfig.videoOutFile)) ? ""
                                    : (ArgsConfig.videoOutFile + "_" + channelId + "_" + threadLocalUserId.get()
                                            + ".yuv"),
                            ArgsConfig.streamType, true);
                    break;
                case RECEIVE_H264:
                    connTask.registerH264ObserverTask(ArgsConfig.remoteUserId,
                            ("".equals(ArgsConfig.videoOutFile)) ? ""
                                    : (ArgsConfig.videoOutFile + "_" + channelId + "_" + threadLocalUserId.get()
                                            + ".h264"),
                            ArgsConfig.streamType, true);
                    break;
                case RECEIVE_ENCODED_AUDIO:
                    connTask.registerEncodedAudioObserverTask(ArgsConfig.remoteUserId,
                            ("".equals(ArgsConfig.audioOutFile)) ? "" : ArgsConfig.audioOutFile, ArgsConfig.fileType,
                            true);
                    break;
                case SEND_RECEIVE_PCM_YUV:
                    connTask.sendPcmTask(ArgsConfig.audioFile, 10, ArgsConfig.numOfChannels, ArgsConfig.sampleRate,
                            false);
                    connTask.sendYuvTask(ArgsConfig.videoFile, 1000 / ArgsConfig.fps, ArgsConfig.height,
                            ArgsConfig.width,
                            ArgsConfig.fps, Constants.VIDEO_STREAM_HIGH, false);
                    connTask.registerPcmObserverTask(ArgsConfig.remoteUserId,
                            ("".equals(ArgsConfig.audioOutFile)) ? ""
                                    : (ArgsConfig.audioOutFile + "_" + channelId + "_" + threadLocalUserId.get()
                                            + ".pcm"),
                            ArgsConfig.numOfChannels,
                            ArgsConfig.sampleRate, false);
                    connTask.registerYuvObserverTask(ArgsConfig.remoteUserId,
                            ("".equals(ArgsConfig.videoOutFile)) ? ""
                                    : (ArgsConfig.videoOutFile + "_" + channelId + "_" + threadLocalUserId.get()
                                            + ".yuv"),
                            ArgsConfig.streamType, true);
                    break;
                case NONE:
                default:
                    // No specific task
                    break;
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

            connTasksList.remove(connTask);

            connTask = null;
            if (ArgsConfig.isStressTest == 1) {
                // stress test force gc
                System.gc();
            }
            SampleLogger.log("test finished for connTask:" + testTask + " channelId:" + channelId + " userId:"
                    + threadLocalUserId.get() + " with left testTaskCount:" + leftTestTaskCount.get() + "\n");
        });
        return true;
    }

    protected void onConnected(AgoraRtcConn conn, String channelId, String userId, TestTask testTask) {
        SampleLogger.log("onConnected for task:" + testTask + " channelId:" + channelId + " userId:" + userId);
    }

    protected void onStreamMessageReceive(String userId, int streamId, byte[] data) {
    }

    public void cleanup() {
        SampleLogger.log("cleanup");
        for (AgoraConnectionTask connTask : connTasksList) {
            connTask.releaseConn();
        }
        connTasksList.clear();

        singleExecutorService.shutdown();
        testTaskExecutorService.shutdown();
        // connPool.releaseAllConn();
        // Destroy Agora Service
        if (null != mediaNodeFactory) {
            mediaNodeFactory.destroy();
            mediaNodeFactory = null;
        }
        if (null != service) {
            service.destroy();
            service = null;
        }

        SampleLogger.release();
    }
}