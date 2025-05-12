package io.agora.rtc.example.test.encoded_audio;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.example.common.AgoraTest;
import io.agora.rtc.example.common.ArgsConfig;

public class MultipleConnectionReceiveEncodedAudioTest extends AgoraTest {

    public static void main(String[] args) {
        startTest(args, new MultipleConnectionReceiveEncodedAudioTest());
    }

    @Override
    public void setup() {
        super.setup();

        // Create Agora connection
        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_AUDIENCE);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setEnableAudioRecordingOrPlayout(0);
        ccfg.setAudioRecvEncodedFrame(1);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        if (ArgsConfig.connectionCount == 1) {
            createConnectionAndTest(ccfg, ArgsConfig.channelId, ArgsConfig.userId, TestTask.RECEIVE_ENCODED_AUDIO,
                    ArgsConfig.testTime);
        } else {
            for (int i = 0; i < ArgsConfig.connectionCount; i++) {
                createConnectionAndTest(ccfg, ArgsConfig.channelId + i, ArgsConfig.userId,
                        TestTask.RECEIVE_ENCODED_AUDIO, ArgsConfig.testTime);
                try {
                    Thread.sleep((long) (ArgsConfig.sleepTime * 1000));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
