package io.agora.rtc.example.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.example.common.AgoraConnectionTask;
import io.agora.rtc.example.common.AgoraTest;
import io.agora.rtc.example.common.ArgsConfig;
import io.agora.rtc.example.common.SampleLogger;

public class FullTest extends AgoraTest {
    protected final ExecutorService executorService = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        startTest(args, new FullTest());
    }

    public void setup() {
        super.setup();

        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                AgoraConnectionTask connTask = new AgoraConnectionTask(service, mediaNodeFactory, 0);
                connTask.setCallback(new AgoraConnectionTask.TaskCallback() {
                    @Override
                    public void onConnected(String userId) {
                        SampleLogger.log("FullTest onConnected userId: " + userId);
                    }

                    @Override
                    public void onTestFinished() {
                        SampleLogger.log("FullTest test finished");

                    }

                    @Override
                    public void onStreamMessage(String userId, int streamId, String data, long length) {
                        SampleLogger.log("FullTest onStreamMessage userId: " + userId + " streamId: " + streamId
                                + " data: " + data + " length: " + length);
                    }
                });
                connTask.createConnection(ccfg, ArgsConfig.channelId, ArgsConfig.userId);
            }
        });
    }

}