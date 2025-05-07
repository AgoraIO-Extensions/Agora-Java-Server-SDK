package io.agora.rtc.example.test.pcm;

import io.agora.rtc.AudioSubscriptionOptions;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.example.common.AgoraTest;
import io.agora.rtc.example.common.ArgsConfig;

public class MultipleConnectionPcmSendReceiveTest extends AgoraTest {

    public static void main(String[] args) {
        startTest(args, new MultipleConnectionPcmSendReceiveTest());
    }

    @Override
    public void setup() {
        super.setup();

        AudioSubscriptionOptions audioSubOpt = new AudioSubscriptionOptions();
        audioSubOpt.setBytesPerSample(2 * ArgsConfig.numOfChannels);
        audioSubOpt.setNumberOfChannels(ArgsConfig.numOfChannels);
        audioSubOpt.setSampleRateHz(ArgsConfig.sampleRate);

        // Create Agora connection
        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAudioSubsOptions(audioSubOpt);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setEnableAudioRecordingOrPlayout(1); // Subscribe audio but without playback
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        ArgsConfig.maxTestTaskCount = ArgsConfig.connectionCount * 2;

        if (ArgsConfig.connectionCount == 1) {
            ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
            createConnectionAndTest(ccfg, ArgsConfig.channelId, ArgsConfig.userId, TestTask.SEND_PCM,
                    ArgsConfig.testTime);
            String connUserId = ArgsConfig.userId.equals("0") ? ArgsConfig.userId : ArgsConfig.userId + "1";
            ccfg.setClientRoleType(Constants.CLIENT_ROLE_AUDIENCE);
            createConnectionAndTest(ccfg, ArgsConfig.channelId, connUserId, TestTask.RECEIVE_PCM,
                    ArgsConfig.testTime);
        } else {
            for (int i = 0; i < ArgsConfig.connectionCount; i++) {
                ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
                createConnectionAndTest(ccfg, ArgsConfig.channelId + i, ArgsConfig.userId, TestTask.SEND_PCM,
                        ArgsConfig.testTime);
                String connUserId = ArgsConfig.userId.equals("0") ? ArgsConfig.userId : ArgsConfig.userId + i;
                ccfg.setClientRoleType(Constants.CLIENT_ROLE_AUDIENCE);
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
