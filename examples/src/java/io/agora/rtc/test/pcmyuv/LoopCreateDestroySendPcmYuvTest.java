package io.agora.rtc.test.pcmyuv;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.common.SampleLogger;
import io.agora.rtc.test.common.AgoraTest;
import java.util.Random;

public class LoopCreateDestroySendPcmYuvTest extends AgoraTest {
    private long testStartTime;

    public static void main(String[] args) {
        startTest(args, new LoopCreateDestroySendPcmYuvTest());
    }

    public void setup() {
        super.setup();
        testStartTime = System.currentTimeMillis();

        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(1);
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);

        int taskCount = 0;
        Random random = new Random();

        while (checkTestTime()) {
            int t1 = random.nextInt(5);

            // 0 is send full file and set to 1 second
            if (t1 == 0) {
                t1 = 1;
            }
            for (int i = 0; i < connectionCount; i++) {
                createConnectionAndTest(ccfg, channelId + taskCount, userId + i, TestTask.SEND_PCM_YUV, t1);
            }

            taskCount++;
            SampleLogger.log("taskCount: " + taskCount);

            try {
                Thread.sleep(sleepTime * 1000);
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