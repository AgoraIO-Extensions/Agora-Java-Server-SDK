package io.agora.rtc.example.common;

import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.example.utils.Utils;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AgoraTaskControl {
    private final ExecutorService singleExecutorService = Executors.newSingleThreadExecutor();
    private final ThreadPoolExecutor testTaskExecutorService = new ThreadPoolExecutor(
        0, Integer.MAX_VALUE, 1L, TimeUnit.SECONDS, new SynchronousQueue<>());

    private AtomicInteger testTaskCount = new AtomicInteger(0);
    private final Object taskCountLock = new Object();
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
        NONE,
        SEND_PCM,
        SEND_YUV,
        SEND_YUV_DUAL_STREAM,
        SEND_H264,
        SEND_H264_DUAL_STREAM,
        SEND_AAC,
        SEND_OPUS,
        SEND_MP4,
        SEND_RGBA_PCM,
        SEND_RGBA,
        SEND_VP8_PCM,
        SEND_VP8,
        SEND_DATA_STREAM,
        SEND_PCM_YUV,
        SEND_PCM_H264,
        RECEIVE_PCM,
        RECEIVE_YUV,
        RECEIVE_H264,
        RECEIVE_PCM_H264,
        RECEIVE_MIXED_AUDIO,
        RECEIVE_ENCODED_AUDIO,
        SEND_RECEIVE_PCM_YUV,
        RECEIVE_DATA_STREAM
    }

    private AgoraTaskManager.AgoraTaskListener agoraTaskListener;

    public AgoraTaskControl(AgoraTaskManager.AgoraTaskListener agoraTaskListener) {
        this.agoraTaskListener = agoraTaskListener;
    }

    public boolean createConnectionAndTest(
        RtcConnConfig ccfg, ArgsConfig argsConfig, TestTask testTask) {
        String channelId = argsConfig.getChannelId();
        String userId = argsConfig.getUserId();
        long testTime = argsConfig.getTestTime();
        SampleLogger.log("createConnectionAndTest start ccfg:" + ccfg + " channelId:" + channelId
            + " userId:" + userId + " testTask:" + testTask + " testTime:" + testTime);
        synchronized (taskCountLock) {
            testTaskCount.incrementAndGet();
            System.out.println(Utils.formatTimestamp(System.currentTimeMillis())
                + " testTaskCount:" + testTaskCount.get() + " testTask:" + testTask
                + " channelId:" + channelId + " userId:" + userId);
            SampleLogger.log("test start testTaskCount:" + testTaskCount.get());
        }
        AgoraServiceInitializer.initAgoraService(0, 1, 1, argsConfig);
        testTaskExecutorService.execute(() -> {
            UserIdHolder threadLocalUserId = new UserIdHolder();
            final CountDownLatch testFinishLatch = new CountDownLatch(1);
            final CountDownLatch connectedLatch = new CountDownLatch(1);
            AtomicInteger leftTestTaskCount = new AtomicInteger(0);
            AgoraConnectionTask connTask =
                new AgoraConnectionTask(AgoraServiceInitializer.getService(),
                    AgoraServiceInitializer.getMediaNodeFactory(), argsConfig);
            connTasksList.add(connTask);
            connTask.setCallback(new AgoraConnectionTask.TaskCallback() {
                @Override
                public void onConnected(String userId) {
                    threadLocalUserId.set(userId);
                    if (agoraTaskListener != null) {
                        agoraTaskListener.onConnected(channelId, threadLocalUserId.get());
                    }
                    connectedLatch.countDown();
                }

                @Override
                public void onUserJoined(String userId) {
                }

                @Override
                public void onTestFinished() {
                    if (agoraTaskListener != null) {
                        agoraTaskListener.onTestFinished(channelId, threadLocalUserId.get());
                    }
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
            }

            try {
                boolean connected = connectedLatch.await(5, TimeUnit.SECONDS);
                if (!connected) {
                    SampleLogger.error("createConnectionAndTest connect timeout for testTask:"
                        + testTask + " channelId:" + channelId + " userId:" + userId
                        + " and exit current test");
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
                    connTask.sendPcmTask(argsConfig.getAudioFile(), 10,
                        argsConfig.getNumOfChannels(), argsConfig.getSampleRate(), true);
                    break;
                case SEND_PCM_YUV:
                    connTask.sendPcmTask(argsConfig.getAudioFile(), 10,
                        argsConfig.getNumOfChannels(), argsConfig.getSampleRate(), false);
                    connTask.sendYuvTask(argsConfig.getVideoFile(), 1000 / argsConfig.getFps(),
                        argsConfig.getHeight(), argsConfig.getWidth(), argsConfig.getFps(),
                        Constants.VIDEO_STREAM_HIGH, true);
                    break;
                case SEND_PCM_H264:
                    connTask.sendPcmTask(argsConfig.getAudioFile(), 10,
                        argsConfig.getNumOfChannels(), argsConfig.getSampleRate(), false);
                    connTask.sendH264Task(argsConfig.getVideoFile(), 1000 / argsConfig.getFps(), 0,
                        0, Constants.VIDEO_STREAM_HIGH, true);
                    break;
                case SEND_AAC:
                    connTask.sendAacTask(argsConfig.getAudioFile(), 20,
                        argsConfig.getNumOfChannels(), argsConfig.getSampleRate(), true);
                    break;
                case SEND_OPUS:
                    connTask.sendOpusTask(argsConfig.getAudioFile(), 20, true);
                    break;
                case SEND_YUV:
                    connTask.sendYuvTask(argsConfig.getVideoFile(), 1000 / argsConfig.getFps(),
                        argsConfig.getHeight(), argsConfig.getWidth(), argsConfig.getFps(),
                        Constants.VIDEO_STREAM_HIGH, true);
                    break;
                case SEND_YUV_DUAL_STREAM:
                    connTask.sendYuvTask(argsConfig.getVideoFile(), 1000 / argsConfig.getFps(),
                        argsConfig.getHeight(), argsConfig.getWidth(), argsConfig.getFps(),
                        Constants.VIDEO_STREAM_HIGH, true);
                    break;
                case SEND_H264:
                    connTask.sendH264Task(argsConfig.getVideoFile(), 1000 / argsConfig.getFps(),
                        argsConfig.getHeight(), argsConfig.getWidth(), Constants.VIDEO_STREAM_HIGH,
                        true);
                    break;
                case SEND_H264_DUAL_STREAM:
                    connTask.sendH264Task(argsConfig.getLowVideoFile(),
                        1000 / argsConfig.getLowFps(), argsConfig.getLowHeight(),
                        argsConfig.getLowWidth(), Constants.VIDEO_STREAM_LOW, false);
                    connTask.sendH264Task(argsConfig.getVideoFile(), 1000 / argsConfig.getFps(),
                        argsConfig.getHeight(), argsConfig.getWidth(), Constants.VIDEO_STREAM_HIGH,
                        true);
                    break;
                case SEND_RGBA:
                    connTask.sendRgbaTask(argsConfig.getVideoFile(), 1000 / argsConfig.getFps(),
                        argsConfig.getHeight(), argsConfig.getWidth(), argsConfig.getFps(), true);
                    break;
                case SEND_RGBA_PCM:
                    connTask.sendPcmTask(argsConfig.getAudioFile(), 10,
                        argsConfig.getNumOfChannels(), argsConfig.getSampleRate(), false);
                    connTask.sendRgbaTask(argsConfig.getVideoFile(), 1000 / argsConfig.getFps(),
                        argsConfig.getHeight(), argsConfig.getWidth(), argsConfig.getFps(), true);
                    break;
                case SEND_VP8:
                    connTask.sendVp8Task(argsConfig.getVideoFile(), 1000 / argsConfig.getFps(),
                        argsConfig.getHeight(), argsConfig.getWidth(), argsConfig.getFps(),
                        Constants.VIDEO_STREAM_HIGH, true);
                    break;
                case SEND_VP8_PCM:
                    connTask.sendPcmTask(argsConfig.getAudioFile(), 10,
                        argsConfig.getNumOfChannels(), argsConfig.getSampleRate(), false);
                    connTask.sendVp8Task(argsConfig.getVideoFile(), 1000 / argsConfig.getFps(),
                        argsConfig.getHeight(), argsConfig.getWidth(), argsConfig.getFps(),
                        Constants.VIDEO_STREAM_HIGH, true);
                    break;
                case SEND_MP4:
                    connTask.sendAvMediaTask(argsConfig.getVideoFile(), 50);
                    break;
                case SEND_DATA_STREAM:
                    connTask.sendDataStreamTask(1, 50, true);
                    break;
                case RECEIVE_PCM:
                    connTask.registerPcmObserverTask(argsConfig.getRemoteUserId(),
                        ("".equals(argsConfig.getAudioOutFile()))
                            ? ""
                            : (argsConfig.getAudioOutFile() + "_" + channelId + "_"
                                  + threadLocalUserId.get() + ".pcm"),
                        argsConfig.getNumOfChannels(), argsConfig.getSampleRate(), true);
                    break;
                case RECEIVE_PCM_H264:
                    connTask.registerPcmObserverTask(argsConfig.getRemoteUserId(),
                        ("".equals(argsConfig.getAudioOutFile()))
                            ? ""
                            : (argsConfig.getAudioOutFile() + "_" + channelId + "_"
                                  + threadLocalUserId.get() + ".pcm"),
                        argsConfig.getNumOfChannels(), argsConfig.getSampleRate(), false);
                    connTask.registerH264ObserverTask(argsConfig.getRemoteUserId(),
                        ("".equals(argsConfig.getVideoOutFile()))
                            ? ""
                            : (argsConfig.getVideoOutFile() + "_" + channelId + "_"
                                  + threadLocalUserId.get() + ".h264"),
                        argsConfig.getStreamType(), true);
                    break;
                case RECEIVE_MIXED_AUDIO:
                    connTask.registerMixedAudioObserverTask(argsConfig.getRemoteUserId(),
                        ("".equals(argsConfig.getAudioOutFile()))
                            ? ""
                            : (argsConfig.getAudioOutFile() + "_" + channelId + "_"
                                  + threadLocalUserId.get() + ".pcm"),
                        argsConfig.getNumOfChannels(), argsConfig.getSampleRate(), true);
                    break;
                case RECEIVE_YUV:
                    connTask.registerYuvObserverTask(argsConfig.getRemoteUserId(),
                        ("".equals(argsConfig.getVideoOutFile()))
                            ? ""
                            : (argsConfig.getVideoOutFile() + "_" + channelId + "_"
                                  + threadLocalUserId.get() + ".yuv"),
                        argsConfig.getStreamType(), true);
                    break;
                case RECEIVE_H264:
                    connTask.registerH264ObserverTask(argsConfig.getRemoteUserId(),
                        ("".equals(argsConfig.getVideoOutFile()))
                            ? ""
                            : (argsConfig.getVideoOutFile() + "_" + channelId + "_"
                                  + threadLocalUserId.get() + ".h264"),
                        argsConfig.getStreamType(), true);
                    break;
                case RECEIVE_ENCODED_AUDIO:
                    connTask.registerEncodedAudioObserverTask(argsConfig.getRemoteUserId(),
                        ("".equals(argsConfig.getAudioOutFile())) ? ""
                                                                    : argsConfig.getAudioOutFile(),
                        argsConfig.getFileType(), true);
                    break;
                case SEND_RECEIVE_PCM_YUV:
                    connTask.sendPcmTask(argsConfig.getAudioFile(), 10,
                        argsConfig.getNumOfChannels(), argsConfig.getSampleRate(), false);
                    connTask.sendYuvTask(argsConfig.getVideoFile(), 1000 / argsConfig.getFps(),
                        argsConfig.getHeight(), argsConfig.getWidth(), argsConfig.getFps(),
                        Constants.VIDEO_STREAM_HIGH, false);
                    connTask.registerPcmObserverTask(argsConfig.getRemoteUserId(),
                        ("".equals(argsConfig.getAudioOutFile()))
                            ? ""
                            : (argsConfig.getAudioOutFile() + "_" + channelId + "_"
                                  + threadLocalUserId.get() + ".pcm"),
                        argsConfig.getNumOfChannels(), argsConfig.getSampleRate(), false);
                    connTask.registerYuvObserverTask(argsConfig.getRemoteUserId(),
                        ("".equals(argsConfig.getVideoOutFile()))
                            ? ""
                            : (argsConfig.getVideoOutFile() + "_" + channelId + "_"
                                  + threadLocalUserId.get() + ".yuv"),
                        argsConfig.getStreamType(), true);
                    break;
                case RECEIVE_DATA_STREAM:
                    connTask.recvDataStreamTask(true);
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
            if (argsConfig.isEnableStressTest()) {
                // stress test force gc
                System.gc();
            }
            if (leftTestTaskCount.get() == 0) {
                agoraTaskListener.onAllTaskFinished();
            }
            SampleLogger.log("test finished for connTask:" + testTask + " channelId:" + channelId
                + " userId:" + threadLocalUserId.get()
                + " with left testTaskCount:" + leftTestTaskCount.get() + "\n");
        });
        return true;
    }

    protected void onConnected(
        AgoraRtcConn conn, String channelId, String userId, TestTask testTask) {
        SampleLogger.log(
            "onConnected for task:" + testTask + " channelId:" + channelId + " userId:" + userId);
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
        releaseAgoraService();

        SampleLogger.release();
    }

    public void releaseAgoraService() {
        AgoraServiceInitializer.destroyAgoraService();
    }
}