package io.agora.rtc.test.ai;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.common.SampleLogger;
import java.util.Random;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class StressReceiverPcmH264Test extends AgoraAiTest {
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
                    int t1 = random.nextInt(10) + 5;

                    createConnectionAndTest(ccfg, channelId, userId + threadId + (index++), TestTask.RECEIVE_PCM_H264,
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