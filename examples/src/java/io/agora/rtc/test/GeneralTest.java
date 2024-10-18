package io.agora.rtc.test;

import io.agora.rtc.common.SampleLogger;
import io.agora.rtc.common.Utils;
import org.junit.Test;

import java.io.File;
import java.util.Scanner;
import io.agora.rtc.SDK;
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
        public void onUserJoined(AgoraRtcConn agora_rtc_conn, String user_id) {
            super.onUserJoined(agora_rtc_conn, user_id);
            SampleLogger.log("user join success: " + user_id);
        }

    }

    @Test
    public void joinChannelTest() throws Exception {
        String token = Utils.readAppIdAndToken(".keys")[1];
        SDK.load(); // ensure JNI library load
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
