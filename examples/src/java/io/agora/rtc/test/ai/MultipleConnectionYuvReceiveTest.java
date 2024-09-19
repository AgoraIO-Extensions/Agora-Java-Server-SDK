package io.agora.rtc.test.ai;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;

public class MultipleConnectionYuvReceiveTest extends AgoraAiTest {

    public static void main(String[] args) {
        startTest(args, new MultipleConnectionYuvReceiveTest());
    }

    @Override
    public void setup() {
        super.setup();

        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(1);

        if (connectionCount == 1) {
            createConnectionAndTest(ccfg, channelId, userId, TestTask.RECEIVE_YUV, testTime);
        } else {
            for (int i = 0; i < connectionCount; i++) {
                createConnectionAndTest(ccfg, channelId + i, userId, TestTask.RECEIVE_YUV, testTime);
            }
        }
    }
}
