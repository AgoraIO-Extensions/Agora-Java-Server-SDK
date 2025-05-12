package io.agora.rtc.example.test.h264;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.example.common.AgoraTest;
import io.agora.rtc.example.common.ArgsConfig;

public class MultipleConnectionH264SendTest extends AgoraTest {
    public static void main(String[] args) {
        startTest(args, new MultipleConnectionH264SendTest());
    }

    @Override
    public void setup() {
        super.setup();
        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        if (ArgsConfig.connectionCount == 1) {
            createConnectionAndTest(ccfg, ArgsConfig.channelId, ArgsConfig.userId, TestTask.SEND_H264,
                    ArgsConfig.testTime);
        } else {
            if (ArgsConfig.singleChannel == 1) {
                for (int i = 0; i < ArgsConfig.connectionCount; i++) {
                    String connUserId = ArgsConfig.userId.equals("0") ? ArgsConfig.userId : ArgsConfig.userId + i;
                    createConnectionAndTest(ccfg, ArgsConfig.channelId, connUserId, TestTask.SEND_H264,
                            ArgsConfig.testTime);
                    try {
                        Thread.sleep((long) (ArgsConfig.sleepTime * 1000));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                for (int i = 0; i < ArgsConfig.connectionCount; i++) {
                    String connUserId = ArgsConfig.userId.equals("0") ? ArgsConfig.userId : ArgsConfig.userId + i;
                    createConnectionAndTest(ccfg, ArgsConfig.channelId + i, connUserId, TestTask.SEND_H264,
                            ArgsConfig.testTime);
                    try {
                        Thread.sleep((long) (ArgsConfig.sleepTime * 1000));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}
