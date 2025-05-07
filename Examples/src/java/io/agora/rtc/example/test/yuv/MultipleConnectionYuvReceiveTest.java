package io.agora.rtc.example.test.yuv;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.example.common.AgoraTest;
import io.agora.rtc.example.common.ArgsConfig;

public class MultipleConnectionYuvReceiveTest extends AgoraTest {

    public static void main(String[] args) {
        startTest(args, new MultipleConnectionYuvReceiveTest());
    }

    @Override
    public void setup() {
        super.setup();

        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_AUDIENCE);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);

        if (ArgsConfig.connectionCount == 1) {
            createConnectionAndTest(ccfg, ArgsConfig.channelId, ArgsConfig.userId, TestTask.RECEIVE_YUV,
                    ArgsConfig.testTime);
        } else {
            for (int i = 0; i < ArgsConfig.connectionCount; i++) {
                createConnectionAndTest(ccfg, ArgsConfig.channelId + i, ArgsConfig.userId,
                        TestTask.RECEIVE_YUV, ArgsConfig.testTime);
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
