package io.agora.rtc.example.common;

import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnPublishConfig;
import io.agora.rtc.SenderOptions;
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

    private final static String TEST_TASK_SEND_PCM_AI_WITH_PTS = "send_pcm_ai_with_pts";
    private final static String TEST_TASK_RECEIVE_PCM_AI_WITH_PTS = "receive_pcm_ai_with_pts";

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
        SEND_PCM_AI,
        SEND_PCM_AI_WITH_PTS,
        RECEIVE_PCM,
        RECEIVE_YUV,
        RECEIVE_H264,
        RECEIVE_PCM_H264,
        RECEIVE_MIXED_AUDIO,
        RECEIVE_ENCODED_AUDIO,
        SEND_RECEIVE_PCM_YUV,
        RECEIVE_DATA_STREAM,
        RECEIVE_PCM_AI,
        RECEIVE_PCM_AI_WITH_PTS
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
            final CountDownLatch userJoinedLatch = new CountDownLatch(1);
            final CountDownLatch testStartLatch = new CountDownLatch(1);
            AtomicInteger leftTestTaskCount = new AtomicInteger(0);
            AgoraConnectionTask connTask = new AgoraConnectionTask(AgoraServiceInitializer.getService(), argsConfig);
            connTasksList.add(connTask);
            connTask.setCallback(new AgoraConnectionTask.TaskCallback() {
                @Override
                public void onConnected(String userId) {
                    threadLocalUserId.set(userId);
                    connectedLatch.countDown();
                    if (agoraTaskListener != null) {
                        agoraTaskListener.onConnected(channelId, threadLocalUserId.get());
                    }
                }

                @Override
                public void onUserJoined(String userId) {
                    userJoinedLatch.countDown();
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

                @Override
                public void onTestTaskStart() {
                    testStartLatch.countDown();
                }
            });

            boolean needConnect = true;
            RtcConnPublishConfig publishConfig = new RtcConnPublishConfig();
            publishConfig.setAudioScenario(Constants.AUDIO_SCENARIO_AI_SERVER);
            publishConfig.setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT);
            if (argsConfig.getAudioScenario() != -1) {
                publishConfig.setAudioScenario(argsConfig.getAudioScenario());
            }
            switch (testTask) {
                case SEND_PCM:
                    publishConfig.setIsPublishAudio(true);
                    publishConfig.setIsPublishVideo(false);
                    publishConfig.setAudioPublishType(Constants.AudioPublishType.PCM);
                    publishConfig.setVideoPublishType(Constants.VideoPublishType.NO_PUBLISH);
                    break;
                case SEND_PCM_YUV:
                    publishConfig.setIsPublishAudio(true);
                    publishConfig.setIsPublishVideo(true);
                    publishConfig.setAudioPublishType(Constants.AudioPublishType.PCM);
                    publishConfig.setVideoPublishType(Constants.VideoPublishType.YUV);
                    break;
                case SEND_PCM_H264:
                    publishConfig.setIsPublishAudio(true);
                    publishConfig.setIsPublishVideo(true);
                    publishConfig.setAudioPublishType(Constants.AudioPublishType.PCM);
                    publishConfig.setVideoPublishType(Constants.VideoPublishType.ENCODED_IMAGE);
                    publishConfig.getSenderOptions().setCcMode(Constants.TCC_ENABLED);
                    break;
                case SEND_AAC:
                    publishConfig.setIsPublishAudio(true);
                    publishConfig.setIsPublishVideo(false);
                    publishConfig.setAudioPublishType(Constants.AudioPublishType.ENCODED_PCM);
                    publishConfig.setVideoPublishType(Constants.VideoPublishType.NO_PUBLISH);
                    break;
                case SEND_OPUS:
                    publishConfig.setIsPublishAudio(true);
                    publishConfig.setIsPublishVideo(false);
                    publishConfig.setAudioPublishType(Constants.AudioPublishType.ENCODED_PCM);
                    publishConfig.setVideoPublishType(Constants.VideoPublishType.NO_PUBLISH);
                    break;
                case SEND_YUV:
                case SEND_YUV_DUAL_STREAM:
                    publishConfig.setIsPublishAudio(false);
                    publishConfig.setIsPublishVideo(true);
                    publishConfig.setAudioPublishType(Constants.AudioPublishType.NO_PUBLISH);
                    publishConfig.setVideoPublishType(Constants.VideoPublishType.YUV);
                    break;
                case SEND_H264:
                    publishConfig.setIsPublishAudio(false);
                    publishConfig.setIsPublishVideo(true);
                    publishConfig.setAudioPublishType(Constants.AudioPublishType.NO_PUBLISH);
                    publishConfig.setVideoPublishType(Constants.VideoPublishType.ENCODED_IMAGE);
                    publishConfig.getSenderOptions().setCcMode(Constants.TCC_ENABLED);
                    break;
                case SEND_H264_DUAL_STREAM:
                    publishConfig.setIsPublishAudio(false);
                    publishConfig.setIsPublishVideo(true);
                    publishConfig.setAudioPublishType(Constants.AudioPublishType.NO_PUBLISH);
                    publishConfig.setVideoPublishType(Constants.VideoPublishType.ENCODED_IMAGE);
                    publishConfig.getSenderOptions().setCcMode(Constants.TCC_ENABLED);
                    break;
                case SEND_RGBA:
                    publishConfig.setIsPublishAudio(false);
                    publishConfig.setIsPublishVideo(true);
                    publishConfig.setAudioPublishType(Constants.AudioPublishType.NO_PUBLISH);
                    publishConfig.setVideoPublishType(Constants.VideoPublishType.YUV);
                    break;
                case SEND_RGBA_PCM:
                    publishConfig.setIsPublishAudio(true);
                    publishConfig.setIsPublishVideo(true);
                    publishConfig.setAudioPublishType(Constants.AudioPublishType.PCM);
                    publishConfig.setVideoPublishType(Constants.VideoPublishType.YUV);
                    break;
                case SEND_VP8:
                    publishConfig.setIsPublishAudio(false);
                    publishConfig.setIsPublishVideo(true);
                    publishConfig.setAudioPublishType(Constants.AudioPublishType.NO_PUBLISH);
                    publishConfig.setVideoPublishType(Constants.VideoPublishType.ENCODED_IMAGE);
                    publishConfig.getSenderOptions().setCcMode(Constants.TCC_ENABLED);
                    break;
                case SEND_VP8_PCM:
                    publishConfig.setIsPublishAudio(true);
                    publishConfig.setIsPublishVideo(true);
                    publishConfig.setAudioPublishType(Constants.AudioPublishType.PCM);
                    publishConfig.setVideoPublishType(Constants.VideoPublishType.ENCODED_IMAGE);
                    publishConfig.getSenderOptions().setCcMode(Constants.TCC_ENABLED);
                    break;
                case SEND_MP4:
                    publishConfig.setIsPublishAudio(true);
                    publishConfig.setIsPublishVideo(true);
                    publishConfig.setAudioPublishType(Constants.AudioPublishType.PCM);
                    publishConfig.setVideoPublishType(Constants.VideoPublishType.YUV);
                    break;
                case SEND_PCM_AI:
                case SEND_PCM_AI_WITH_PTS:
                    publishConfig.setIsPublishAudio(true);
                    publishConfig.setIsPublishVideo(false);
                    publishConfig.setAudioPublishType(Constants.AudioPublishType.PCM);
                    publishConfig.setVideoPublishType(Constants.VideoPublishType.NO_PUBLISH);
                    break;
                case SEND_DATA_STREAM:
                case RECEIVE_DATA_STREAM:
                    publishConfig.setIsPublishAudio(false);
                    publishConfig.setIsPublishVideo(false);
                    publishConfig.setAudioPublishType(Constants.AudioPublishType.NO_PUBLISH);
                    publishConfig.setVideoPublishType(Constants.VideoPublishType.NO_PUBLISH);
                    break;
                case RECEIVE_PCM:
                case RECEIVE_PCM_H264:
                case RECEIVE_MIXED_AUDIO:
                case RECEIVE_YUV:
                case RECEIVE_H264:
                case RECEIVE_ENCODED_AUDIO:
                    publishConfig.setIsPublishAudio(false);
                    publishConfig.setIsPublishVideo(false);
                    publishConfig.setAudioPublishType(Constants.AudioPublishType.NO_PUBLISH);
                    publishConfig.setVideoPublishType(Constants.VideoPublishType.NO_PUBLISH);
                    needConnect = false;
                    break;
                case SEND_RECEIVE_PCM_YUV:
                    break;
                case RECEIVE_PCM_AI:
                    publishConfig.setIsPublishAudio(false);
                    publishConfig.setIsPublishVideo(false);
                    publishConfig.setAudioPublishType(Constants.AudioPublishType.NO_PUBLISH);
                    publishConfig.setVideoPublishType(Constants.VideoPublishType.NO_PUBLISH);
                    needConnect = false;
                    break;
                case RECEIVE_PCM_AI_WITH_PTS:
                    publishConfig.setIsPublishAudio(false);
                    publishConfig.setIsPublishVideo(false);
                    publishConfig.setAudioPublishType(Constants.AudioPublishType.NO_PUBLISH);
                    publishConfig.setVideoPublishType(Constants.VideoPublishType.NO_PUBLISH);
                    needConnect = true;
                    break;
                case NONE:
                default:
                    // No specific task
                    break;
            }

            try {
                connTask.createConnection(ccfg, publishConfig, needConnect);
            } catch (Exception e) {
                e.printStackTrace();
                SampleLogger.log("createConnection failed, exit");
            }

            if (needConnect) {
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
                    // wait user join
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (argsConfig.isEnableAssistantDevice()) {
                try {
                    userJoinedLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            switch (testTask) {
                case SEND_PCM_AI_WITH_PTS:
                    connTask.sendTestTaskMessage(TEST_TASK_SEND_PCM_AI_WITH_PTS);
                    break;
                case RECEIVE_PCM_AI_WITH_PTS:
                    connTask.sendTestTaskMessage(TEST_TASK_RECEIVE_PCM_AI_WITH_PTS);
                    break;
            }

            if (argsConfig.isEnableAssistantDevice()) {
                try {
                    testStartLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // onConnected(connTask.getConn(), channelId, threadLocalUserId.get(),
            // testTask);

            switch (testTask) {
                case SEND_PCM:
                    connTask.sendPcmTask(true, false);
                    break;
                case SEND_PCM_YUV:
                    connTask.sendYuvTask(false);
                    connTask.sendPcmTask(true, false);
                    break;
                case SEND_PCM_H264:
                    connTask.sendH264Task(false);
                    connTask.sendPcmTask(true, false);
                    break;
                case SEND_AAC:
                    connTask.sendAacTask(true);
                    break;
                case SEND_OPUS:
                    connTask.sendOpusTask(true);
                    break;
                case SEND_YUV:
                case SEND_YUV_DUAL_STREAM:
                    connTask.sendYuvTask(true);
                    break;
                case SEND_H264:
                case SEND_H264_DUAL_STREAM:
                    connTask.sendH264Task(true);
                    break;
                case SEND_RGBA:
                    connTask.sendRgbaTask(true);
                    break;
                case SEND_RGBA_PCM:
                    connTask.sendRgbaTask(false);
                    connTask.sendPcmTask(true, false);
                    break;
                case SEND_VP8:
                    connTask.sendVp8Task(true);
                    break;
                case SEND_VP8_PCM:
                    connTask.sendPcmTask(false, false);
                    connTask.sendVp8Task(true);
                    break;
                case SEND_MP4:
                    connTask.sendAvMediaTask();
                    break;
                case SEND_DATA_STREAM:
                    connTask.sendDataStreamTask(50, true);
                    break;
                case SEND_PCM_AI:
                case SEND_PCM_AI_WITH_PTS:
                    connTask.sendPcmTask(true, true);
                    break;
                case RECEIVE_PCM:
                    connTask.registerPcmObserverTask(true, false);
                    break;
                case RECEIVE_PCM_H264:
                    connTask.registerPcmObserverTask(false, false);
                    connTask.registerH264ObserverTask(true);
                    break;
                case RECEIVE_MIXED_AUDIO:
                    connTask.registerMixedAudioObserverTask(true);
                    break;
                case RECEIVE_YUV:
                    connTask.registerYuvObserverTask(true);
                    break;
                case RECEIVE_H264:
                    connTask.registerH264ObserverTask(true);
                    break;
                case RECEIVE_ENCODED_AUDIO:
                    connTask.registerEncodedAudioObserverTask(true);
                    break;
                case SEND_RECEIVE_PCM_YUV:
                    connTask.sendPcmTask(false, false);
                    connTask.sendYuvTask(false);
                    connTask.registerPcmObserverTask(false, false);
                    connTask.registerYuvObserverTask(true);
                    break;
                case RECEIVE_DATA_STREAM:
                    connTask.recvDataStreamTask(true);
                    break;
                case RECEIVE_PCM_AI:
                case RECEIVE_PCM_AI_WITH_PTS:
                    connTask.registerPcmObserverTask(true, true);
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