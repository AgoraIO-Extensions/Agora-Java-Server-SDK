package io.agora.rtc.example.test;

import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.utils.Utils;
import org.junit.Test;

import java.io.File;
import java.util.Scanner;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.DefaultRtcConnObserver;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.Out;
import io.agora.rtc.AgoraMediaNodeFactory;

public class GeneralTest {
    public static class ConnObserver extends DefaultRtcConnObserver {
        @Override
        public void onConnected(AgoraRtcConn conn, RtcConnInfo rtcConnInfo, int reason) {
            SampleLogger.log("join success");
        }

        @Override
        public void onUserJoined(AgoraRtcConn agoraRtcConn, String userId) {
            super.onUserJoined(agoraRtcConn, userId);
            SampleLogger.log("user join success: " + userId);
        }

    }

    @Test
    public void joinChannelTest() throws Exception {
        String token = Utils.readAppIdAndToken(".keys")[1];
        AgoraService service = new AgoraService();
        AgoraServiceConfig config = new AgoraServiceConfig();
        config.setEnableAudioProcessor(0);
        config.setEnableAudioDevice(0);
        config.setEnableVideo(0);
        config.setContext(0);
        config.setAppId(null);
        service.initialize(config);

        AgoraRtcConn conn = service.agoraRtcConnCreate(null);

        conn.registerObserver(new ConnObserver());

        conn.connect(token, "JAVATEST", "1");

        Thread.sleep(2000);
        conn.disconnect();
        conn.destroy();
        service.destroy();
    }
}
