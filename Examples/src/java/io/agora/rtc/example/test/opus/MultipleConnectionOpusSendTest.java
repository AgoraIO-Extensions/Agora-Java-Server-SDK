package io.agora.rtc.example.test.opus;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.example.common.AgoraTest;
import io.agora.rtc.example.common.ArgsConfig;

public class MultipleConnectionOpusSendTest extends AgoraTest {

    public static void main(String[] args) {
        startTest(args, new MultipleConnectionOpusSendTest());
    }

    @Override
    public void setup() {
        super.setup();

        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);

        if (ArgsConfig.connectionCount == 1) {
            createConnectionAndTest(ccfg, ArgsConfig.channelId, ArgsConfig.userId, TestTask.SEND_OPUS,
                    ArgsConfig.testTime);
        } else {
            for (int i = 0; i < ArgsConfig.connectionCount; i++) {
                createConnectionAndTest(ccfg, ArgsConfig.channelId + i, ArgsConfig.userId, TestTask.SEND_OPUS,
                        ArgsConfig.testTime);
            }
        }

    }

}
