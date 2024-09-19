package io.agora.rtc.test.ai;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;

public class SingleChannelYuvPcmLoopSendTest extends AgoraAiTest {

    public static void main(String[] args) {
        startTest(args, new SingleChannelYuvPcmLoopSendTest());
    }

    public void setup() {
        super.setup();
        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(1);
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);

        createConnectionAndTest(ccfg, channelId, userId, TestTask.SEND_PCM, testTime);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        createConnectionAndTest(ccfg, channelId, userId + "0", TestTask.SEND_YUV, testTime);
    }
}