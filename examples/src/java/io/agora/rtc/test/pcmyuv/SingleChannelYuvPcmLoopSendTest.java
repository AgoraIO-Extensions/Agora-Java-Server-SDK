package io.agora.rtc.test.pcmyuv;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.test.common.AgoraTest;

public class SingleChannelYuvPcmLoopSendTest extends AgoraTest {

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

        createConnectionAndTest(ccfg, channelId, userId, TestTask.SEND_PCM_YUV, testTime);
    }
}