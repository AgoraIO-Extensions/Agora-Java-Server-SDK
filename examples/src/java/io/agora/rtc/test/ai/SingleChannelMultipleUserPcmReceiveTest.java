package io.agora.rtc.test.ai;

import io.agora.rtc.AudioSubscriptionOptions;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;

public class SingleChannelMultipleUserPcmReceiveTest extends AgoraAiTest {

    public static void main(String[] args) {
        startTest(args, new SingleChannelMultipleUserPcmReceiveTest());
    }

    @Override
    public void setup() {
        super.setup();

        // Create Agora connection
        AudioSubscriptionOptions audioSubOpt = new AudioSubscriptionOptions();
        audioSubOpt.setBytesPerSample(2 * numOfChannels);
        audioSubOpt.setNumberOfChannels(numOfChannels);
        audioSubOpt.setSampleRateHz(sampleRate);

        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAudioSubsOptions(audioSubOpt);
        ccfg.setAutoSubscribeAudio(1);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setEnableAudioRecordingOrPlayout(1);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        try {
            int userIdInt = Integer.parseInt(userId);
            for (int i = 0; i < connectionCount; i++) {
                createConnectionAndTest(ccfg, channelId, String.valueOf(userIdInt + i),
                        TestTask.RECEIVE_PCM,
                        testTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
