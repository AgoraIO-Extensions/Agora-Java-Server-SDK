package io.agora.rtc.example.test.pcm;

import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.Constants;
import io.agora.rtc.example.common.AgoraTest;

public class MultipleConnectionPcmSendTest extends AgoraTest {

    public static void main(String[] args) {
        startTest(args, new MultipleConnectionPcmSendTest());
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
            createConnectionAndTest(ccfg, channelId, userId, TestTask.SEND_PCM, testTime);
        } else {
            if (singleChannel == 1) {
                for (int i = 0; i < connectionCount; i++) {
                    createConnectionAndTest(ccfg, channelId, userId + i, TestTask.SEND_PCM, testTime);
                }
            } else {
                for (int i = 0; i < connectionCount; i++) {
                    createConnectionAndTest(ccfg, channelId + i, userId, TestTask.SEND_PCM, testTime);
                }
            }
        }
    }
}
