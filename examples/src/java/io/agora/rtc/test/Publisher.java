package io.agora.rtc.test;

import io.agora.rtc.common.Utils;
import java.io.File;
import java.util.Scanner;
import io.agora.rtc.SDK;
import io.agora.rtc.AgoraCameraCapturer;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.AgoraLocalAudioTrack;
import io.agora.rtc.AgoraLocalVideoTrack;
import io.agora.rtc.AgoraDeviceInfo;
import io.agora.rtc.DefaultRtcConnObserver;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.Out;
import io.agora.rtc.VideoEncoderConfig;
import io.agora.rtc.VideoFormat;
import io.agora.rtc.VideoDimensions;
import io.agora.rtc.AgoraMediaNodeFactory;

public class Publisher {
    public static class ConnObserver extends DefaultRtcConnObserver {
        private AgoraLocalAudioTrack localAudioTrack;
        private AgoraLocalVideoTrack cameraCaptureTrack;

        public ConnObserver(AgoraLocalAudioTrack localAudioTrack, AgoraLocalVideoTrack cameraCaptureTrack) {
            this.localAudioTrack = localAudioTrack;
            this.cameraCaptureTrack = cameraCaptureTrack;
        }

        @Override
        public void onConnected(AgoraRtcConn conn, RtcConnInfo rtcConnInfo, int reason) {
            System.out.println("join success");
            AgoraLocalUser localUser = conn.getLocalUser();

            this.localAudioTrack.setEnabled(1);
            localUser.publishAudio(this.localAudioTrack);

            this.cameraCaptureTrack.setEnabled(1);
            localUser.publishVideo(this.cameraCaptureTrack);
        }
    }

    public static AgoraLocalVideoTrack createCameraCaptureTrack(AgoraService service, AgoraMediaNodeFactory factory) {
        AgoraCameraCapturer cameraCapturer = factory.createCameraCapturer();
        AgoraDeviceInfo deviceInfo = cameraCapturer.createDeviceInfo();
        int nDevices = deviceInfo.numberOfDevices();
        System.out.println("detected " + nDevices + " camera(s)");

        Out<String> deviceName = new Out<String>();
        Out<String> deviceId = new Out<String>();
        Out<String> productUniqueId = new Out<String>();
        deviceInfo.getDeviceName(0, deviceName, deviceId, productUniqueId);
        int nCaps = deviceInfo.numberOfCapabilities(deviceId.get());
        System.out.println("using camera: " + deviceName.get() + ", device id: " + deviceId.get() + ", caps: " + nCaps);
        cameraCapturer.initWithDeviceId(deviceId.get());

        VideoFormat captureFormat = new VideoFormat(720, 480, /* fps */ 30);
        VideoEncoderConfig encoderConfig = new VideoEncoderConfig();
        encoderConfig.setCodecType(2); // h264
        encoderConfig.setDimensions(new VideoDimensions(1080, 720));
        encoderConfig.setFrameRate(30);
        encoderConfig.setBitrate(200 * 1000);
        encoderConfig.setMinBitrate(20 * 1000);
        encoderConfig.setOrientationMode(0); // adaptive

        AgoraLocalVideoTrack track = service.createCameraVideoTrack(cameraCapturer);
        track.setVideoEncoderConfig(encoderConfig);

        // Java code doesn't need these objects any more, so destroy them to avoid
        // leaks,
        // the underlying C++ object will be released when refcount becomes 0
        cameraCapturer.destroy();
        deviceInfo.destroy();

        return track;
    }

    public static void main(String[] args) throws Exception {
        SDK.load(); // ensure JNI library load
        AgoraService service = new AgoraService();
        AgoraServiceConfig config = new AgoraServiceConfig();
        config.setEnableAudioProcessor(1);
        config.setEnableAudioDevice(1);
        config.setEnableVideo(0);
        config.setContext(0);
        config.setAppId(null);
        config.setAudioScenario(0); // AUDIO_SCENARIO_DEFAULT
        service.initialize(config);

        AgoraRtcConn conn = service.agoraRtcConnCreate(null);

        AgoraLocalAudioTrack localAudioTrack = service.createLocalAudioTrack();
        AgoraMediaNodeFactory factory = service.createMediaNodeFactory();
        AgoraLocalVideoTrack cameraCaptureTrack = createCameraCaptureTrack(service, factory);
        conn.registerObserver(new ConnObserver(localAudioTrack, cameraCaptureTrack));

        String token = Utils.readAppIdAndToken(".keys")[1];
        conn.connect(token, "JAVATEST", "1");

        Thread.sleep(15000);
        localAudioTrack.setEnabled(0);
        cameraCaptureTrack.setEnabled(0);
        conn.disconnect();
        conn.destroy();
        factory.destroy();
        localAudioTrack.destroy();
        cameraCaptureTrack.destroy();
        service.destroy();
    }
}
