package io.agora.rtc.example.test.mixedaudio;

import io.agora.rtc.AudioSubscriptionOptions;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.example.common.AgoraTest;
import io.agora.rtc.example.common.ArgsConfig;

public class MultipleConnectionMixedAudioReceiveTest extends AgoraTest {

    public static void main(String[] args) {
        startTest(args, new MultipleConnectionMixedAudioReceiveTest());
    }

    @Override
    public void setup() {
        super.setup();

        // Create Agora connection
        AudioSubscriptionOptions audioSubOpt = new AudioSubscriptionOptions();
        audioSubOpt.setBytesPerSample(2 * ArgsConfig.numOfChannels);
        audioSubOpt.setNumberOfChannels(ArgsConfig.numOfChannels);
        audioSubOpt.setSampleRateHz(ArgsConfig.sampleRate);

        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_AUDIENCE);
        ccfg.setAudioSubsOptions(audioSubOpt);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setEnableAudioRecordingOrPlayout(1);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        if (ArgsConfig.connectionCount == 1) {
            createConnectionAndTest(ccfg, ArgsConfig.channelId, ArgsConfig.userId, TestTask.RECEIVE_MIXED_AUDIO,
                    ArgsConfig.testTime);
        } else {
            for (int i = 0; i < ArgsConfig.connectionCount; i++) {
                createConnectionAndTest(ccfg, ArgsConfig.channelId + i, ArgsConfig.userId,
                        TestTask.RECEIVE_MIXED_AUDIO, ArgsConfig.testTime);
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
