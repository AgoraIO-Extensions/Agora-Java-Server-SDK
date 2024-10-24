package io.agora.rtc.test.h264;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.test.common.AgoraTest;

public class MultipleConnectionH264ReceiveTest extends AgoraTest {

    public static void main(String[] args) {
        startTest(args, new MultipleConnectionH264ReceiveTest());
    }

    @Override
    public void setup() {
        super.setup();
        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(1);
        ccfg.setChannelProfile(1);

        if (connectionCount == 1) {
            createConnectionAndTest(ccfg, channelId, userId, TestTask.RECEIVE_H264, testTime);
        } else {
            for (int i = 0; i < connectionCount; i++) {
                createConnectionAndTest(ccfg, channelId + i, userId, TestTask.RECEIVE_H264, testTime);
            }
        }

    }

}
