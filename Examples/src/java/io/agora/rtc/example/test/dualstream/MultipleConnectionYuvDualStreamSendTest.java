package io.agora.rtc.example.test.dualstream;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.example.common.AgoraTest;
import io.agora.rtc.example.common.ArgsConfig;

public class MultipleConnectionYuvDualStreamSendTest extends AgoraTest {

    public static void main(String[] args) {
        startTest(args, new MultipleConnectionYuvDualStreamSendTest());
    }

    @Override
    public void setup() {
        super.setup();

        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        if (ArgsConfig.connectionCount == 1) {
            createConnectionAndTest(ccfg, ArgsConfig.channelId, ArgsConfig.userId, TestTask.SEND_YUV_DUAL_STREAM,
                    ArgsConfig.testTime);
        } else {
            for (int i = 0; i < ArgsConfig.connectionCount; i++) {
                createConnectionAndTest(ccfg, ArgsConfig.channelId + i, ArgsConfig.userId,
                        TestTask.SEND_YUV_DUAL_STREAM, ArgsConfig.testTime);
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}