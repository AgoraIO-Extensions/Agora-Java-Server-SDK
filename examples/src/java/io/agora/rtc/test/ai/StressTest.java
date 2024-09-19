package io.agora.rtc.test.ai;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.common.SampleLogger;
import java.util.Random;

public class StressTest extends AgoraAiTest {
    private long testStartTime;

    public static void main(String[] args) {
        startTest(args, new StressTest());
    }

    public void setup() {
        super.setup();
        testStartTime = System.currentTimeMillis();

        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        int taskCount = 0;
        Random random = new Random();

        while (checkTestTime()) {
            int t1 = random.nextInt(5);
            int t2 = random.nextInt(500);
            int t3 = random.nextInt(5);

            createConnectionAndTest(ccfg, channelId + taskCount, userId, TestTask.SEND_PCM, t1);

            try {
                Thread.sleep(t2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (videoFile.endsWith(".h264")) {
                createConnectionAndTest(ccfg, channelId + taskCount, userId + "0", TestTask.SEND_H264, t3);
            } else {
                createConnectionAndTest(ccfg, channelId + taskCount, userId + "0", TestTask.SEND_YUV, t3);
            }
            taskCount++;
            SampleLogger.log("taskCount: " + taskCount);

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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