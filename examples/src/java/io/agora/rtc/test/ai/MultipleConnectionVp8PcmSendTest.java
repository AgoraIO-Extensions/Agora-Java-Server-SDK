package io.agora.rtc.test.ai;

import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.Constants;

public class MultipleConnectionVp8PcmSendTest extends AgoraAiTest {

    public static void main(String[] args) {
        startTest(args, new MultipleConnectionVp8PcmSendTest());
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
            createConnectionAndTest(ccfg, channelId, userId, TestTask.SEND_VP8_PCM, testTime);
        } else {
            for (int i = 0; i < connectionCount; i++) {
                createConnectionAndTest(ccfg, channelId + i, userId, TestTask.SEND_VP8_PCM, testTime);
            }
        }
    }
}
