package io.agora.rtc.example.test.pcm;

import io.agora.rtc.AudioSubscriptionOptions;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.example.common.AgoraTest;
import io.agora.rtc.example.common.ArgsConfig;

public class MultipleConnectionPcmReceiveTest extends AgoraTest {

    public static void main(String[] args) {
        startTest(args, new MultipleConnectionPcmReceiveTest());
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
            createConnectionAndTest(ccfg, ArgsConfig.channelId, ArgsConfig.userId, TestTask.RECEIVE_PCM,
                    ArgsConfig.testTime);
        } else {
            if (ArgsConfig.singleChannel == 1) {
                for (int i = 0; i < ArgsConfig.connectionCount; i++) {
                    String connUserId = ArgsConfig.userId.equals("0") ? ArgsConfig.userId : ArgsConfig.userId + i;
                    createConnectionAndTest(ccfg, ArgsConfig.channelId, connUserId, TestTask.RECEIVE_PCM,
                            ArgsConfig.testTime);
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                for (int i = 0; i < ArgsConfig.connectionCount; i++) {
                    String connUserId = ArgsConfig.userId.equals("0") ? ArgsConfig.userId : ArgsConfig.userId + i;
                    createConnectionAndTest(ccfg, ArgsConfig.channelId + i, connUserId, TestTask.RECEIVE_PCM,
                            ArgsConfig.testTime);
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

}
