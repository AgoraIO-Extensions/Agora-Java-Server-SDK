package io.agora.rtc.test.encoded_audio;

import io.agora.rtc.AudioSubscriptionOptions;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.test.common.AgoraTest;

public class MultipleConnectionReceiveEncodedAudioTest extends AgoraTest {

    public static void main(String[] args) {
        startTest(args, new MultipleConnectionReceiveEncodedAudioTest());
    }

    @Override
    public void setup() {
        super.setup();

        // Create Agora connection
        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAutoSubscribeAudio(1);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setEnableAudioRecordingOrPlayout(0);
        ccfg.setAudioRecvEncodedFrame(1);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        if (connectionCount == 1) {
            createConnectionAndTest(ccfg, channelId, userId, TestTask.RECEIVE_ENCODED_AUDIO, testTime);
        } else {
            for (int i = 0; i < connectionCount; i++) {
                createConnectionAndTest(ccfg, channelId + i, userId, TestTask.RECEIVE_ENCODED_AUDIO, testTime);
            }
        }

    }

}
