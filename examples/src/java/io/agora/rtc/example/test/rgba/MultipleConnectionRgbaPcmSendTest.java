package io.agora.rtc.example.test.rgba;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.example.common.AgoraTest;

public class MultipleConnectionRgbaPcmSendTest extends AgoraTest {

    public static void main(String[] args) {
        startTest(args, new MultipleConnectionRgbaPcmSendTest());
    }

    @Override
    public void setup() {
        super.setup();

        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        if (connectionCount == 1) {
            createConnectionAndTest(ccfg, channelId, userId, TestTask.SEND_RGBA_PCM, testTime);
        } else {
            for (int i = 0; i < connectionCount; i++) {
                String connUserId = userId.equals("0") ? userId : userId + i;
                createConnectionAndTest(ccfg, channelId + i, connUserId, TestTask.SEND_RGBA_PCM, testTime);
            }
        }
    }
}