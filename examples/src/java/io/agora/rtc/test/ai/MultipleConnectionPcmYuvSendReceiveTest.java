package io.agora.rtc.test.ai;

import io.agora.rtc.AudioSubscriptionOptions;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;

public class MultipleConnectionPcmYuvSendReceiveTest extends AgoraAiTest {

    public static void main(String[] args) {
        startTest(args, new MultipleConnectionPcmYuvSendReceiveTest());
    }

    @Override
    public void setup() {
        super.setup();

        AudioSubscriptionOptions audioSubOpt = new AudioSubscriptionOptions();
        audioSubOpt.setBytesPerSample(2 * numOfChannels);
        audioSubOpt.setNumberOfChannels(numOfChannels);
        audioSubOpt.setSampleRateHz(sampleRate);

        // Create Agora connection
        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAutoSubscribeAudio(1);
        ccfg.setAutoSubscribeVideo(1);
        ccfg.setAudioSubsOptions(audioSubOpt);
        ccfg.setEnableAudioRecordingOrPlayout(1); // Subscribe audio but without playback
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        if (connectionCount == 1) {
            createConnectionAndTest(ccfg, channelId, userId, TestTask.SEND_RECEIVE_PCM_YUV, testTime);
        } else {
            for (int i = 0; i < connectionCount; i++) {
                createConnectionAndTest(ccfg, channelId + i, userId, TestTask.SEND_RECEIVE_PCM_YUV, testTime);
            }
        }

    }
}
