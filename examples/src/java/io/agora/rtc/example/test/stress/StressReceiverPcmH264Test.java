package io.agora.rtc.example.test.stress;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.common.AgoraTest;
import java.util.Random;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class StressReceiverPcmH264Test extends AgoraTest {
    private long testStartTime;
    private final ThreadPoolExecutor testTaskExecutorService;

    public StressReceiverPcmH264Test() {
        this.testTaskExecutorService = new ThreadPoolExecutor(
                0,
                Integer.MAX_VALUE,
                1L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>());
    }

    public static void main(String[] args) {
        startTest(args, new StressReceiverPcmH264Test());
    }

    public void setup() {
        super.setup();
        testStartTime = System.currentTimeMillis();

        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        Random random = new Random();

        for (int i = 0; i < connectionCount; i++) {
            final int threadId = i;
            testTaskExecutorService.execute(() -> {
                int taskCount = 0;
                int index = 0;
                while (checkTestTime()) {
                    int t1 = random.nextInt(sleepTime - 5) + 1;
                    String channel = connectionCount == 1 ? channelId : channelId + threadId;
                    String connUserId = userId.equals("0") ? userId : userId + threadId + (index++);
                    createConnectionAndTest(ccfg, channel, connUserId, TestTask.RECEIVE_PCM_H264,
                            t1);

                    taskCount++;
                    SampleLogger.log("taskCount: " + taskCount);

                    try {
                        Thread.sleep(sleepTime * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        try {
            Thread.sleep(testTime * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean checkTestTime() {
        long currentTime = System.currentTimeMillis();
        long testCostTime = currentTime - testStartTime;
        if (testCostTime >= testTime * 1000) {
            return false;
        }
        return true;
    }
}