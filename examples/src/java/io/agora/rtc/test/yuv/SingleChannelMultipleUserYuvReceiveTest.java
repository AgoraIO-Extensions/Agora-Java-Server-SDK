package io.agora.rtc.test.yuv;

import io.agora.rtc.AudioSubscriptionOptions;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.test.common.AgoraTest;

public class SingleChannelMultipleUserYuvReceiveTest extends AgoraTest {

    public static void main(String[] args) {
        startTest(args, new SingleChannelMultipleUserYuvReceiveTest());
    }

    @Override
    public void setup() {
        super.setup();

        // Create Agora connection
        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(1);

        try {
            int userIdInt = Integer.parseInt(userId);
            for (int i = 0; i < connectionCount; i++) {
                createConnectionAndTest(ccfg, channelId, String.valueOf(userIdInt + i),
                        TestTask.RECEIVE_YUV,
                        testTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
