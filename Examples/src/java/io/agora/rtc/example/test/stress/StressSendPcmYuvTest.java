package io.agora.rtc.example.test.stress;

import java.util.Random;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.example.common.AgoraTest;
import io.agora.rtc.example.common.ArgsConfig;

public class StressSendPcmYuvTest extends AgoraTest {
    private long testStartTime;
    private final ThreadPoolExecutor testTaskExecutorService;

    public StressSendPcmYuvTest() {
        this.testTaskExecutorService = new ThreadPoolExecutor(
                0,
                Integer.MAX_VALUE,
                1L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>());
    }

    public static void main(String[] args) {
        startTest(args, new StressSendPcmYuvTest());
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

        for (int i = 0; i < ArgsConfig.connectionCount; i++) {
            final int threadId = i;
            testTaskExecutorService.execute(() -> {
                while (checkTestTime()) {
                    int t1 = random.nextInt((int) (ArgsConfig.sleepTime - ArgsConfig.timeForStressLeave)) + 1;
                    String channel = ArgsConfig.connectionCount == 1 ? ArgsConfig.channelId
                            : ArgsConfig.channelId + threadId;
                    String connUserId = ArgsConfig.userId.equals("0") ? ArgsConfig.userId
                            : ArgsConfig.userId + threadId;
                    createConnectionAndTest(ccfg, channel, connUserId, TestTask.SEND_PCM_YUV, t1);

                    try {
                        Thread.sleep((long) (ArgsConfig.sleepTime * 1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        try {
            Thread.sleep(ArgsConfig.testTime * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean checkTestTime() {
        long currentTime = System.currentTimeMillis();
        long testCostTime = currentTime - testStartTime;
        if (testCostTime >= ArgsConfig.testTime * 1000) {
            return false;
        }
        return true;
    }
}