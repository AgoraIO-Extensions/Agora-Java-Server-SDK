package io.agora.rtc.test.ai;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;

public class DataStreamReceiveTest extends AgoraAiTest {
    private int receiveCount = 0;

    public static void main(String[] args) {
        startTest(args, new DataStreamReceiveTest());
    }

    @Override
    public void setup() {
        super.setup();

        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(1);
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);

        createConnectionAndTest(ccfg, channelId, userId, TestTask.NONE, testTime);
    }

    protected void onStreamMessageReceive(String userId, int streamId, String data, long length) {
        super.onStreamMessageReceive(userId, streamId, data, length);

    }

}