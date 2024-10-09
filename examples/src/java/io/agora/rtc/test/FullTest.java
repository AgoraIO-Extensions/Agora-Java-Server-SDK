package io.agora.rtc.test;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.common.AgoraConnectionTask;
import io.agora.rtc.common.SampleLogger;
import io.agora.rtc.test.common.AgoraTest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FullTest extends AgoraTest {
    protected final ExecutorService executorService = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        startTest(args, new FullTest());
    }

    public void setup() {
        super.setup();

        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAutoSubscribeAudio(1);
        ccfg.setAutoSubscribeVideo(1);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                AgoraConnectionTask connTask = new AgoraConnectionTask(service, 0);
                connTask.setCallback(new AgoraConnectionTask.TaskCallback() {
                    @Override
                    public void onConnected() {
                        SampleLogger.log("FullTest onConnected");
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
                connTask.createConnectionAndTest(ccfg, token, channelId, userId, enableEncryptionMode, encryptionMode,
                        encryptionKey, enableCloudProxy == 1);
            }
        });
    }

}