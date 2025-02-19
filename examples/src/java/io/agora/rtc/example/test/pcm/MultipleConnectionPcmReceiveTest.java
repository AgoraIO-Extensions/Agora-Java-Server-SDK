package io.agora.rtc.example.test.pcm;

import io.agora.rtc.AudioSubscriptionOptions;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.example.common.AgoraTest;

public class MultipleConnectionPcmReceiveTest extends AgoraTest {

    public static void main(String[] args) {
        startTest(args, new MultipleConnectionPcmReceiveTest());
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

        if (connectionCount == 1) {
            createConnectionAndTest(ccfg, channelId, userId, TestTask.RECEIVE_PCM, testTime);
        } else {
            if (singleChannel == 1) {
                for (int i = 0; i < connectionCount; i++) {
                    String connUserId = userId.equals("0") ? userId : userId + i;
                    createConnectionAndTest(ccfg, channelId, connUserId, TestTask.RECEIVE_PCM, testTime);
                }
            } else {
                for (int i = 0; i < connectionCount; i++) {
                    String connUserId = userId.equals("0") ? userId : userId + i;
                    createConnectionAndTest(ccfg, channelId + i, connUserId, TestTask.RECEIVE_PCM, testTime);
                }
            }
        }

    }

}
