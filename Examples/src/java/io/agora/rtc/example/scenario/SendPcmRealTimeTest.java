package io.agora.rtc.example.scenario;

import io.agora.rtc.AgoraAudioPcmDataSender;
import io.agora.rtc.AgoraLocalAudioTrack;
import io.agora.rtc.AgoraMediaNodeFactory;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.Constants;
import io.agora.rtc.DefaultRtcConnObserver;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.utils.AudioConsumerUtils;
import io.agora.rtc.example.common.FileSender;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.utils.Utils;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SendPcmRealTimeTest {
    private static String appId;
    private static String token;
    private final static String DEFAULT_LOG_PATH = "logs/agora_logs/agorasdk.log";
    private final static int DEFAULT_LOG_SIZE = 5 * 1024 * 1024; // default log size is 5 mb

    private static AgoraService service;
    private static AgoraRtcConn conn;
    private static AgoraMediaNodeFactory mediaNodeFactory;

    private static AgoraAudioPcmDataSender audioFrameSender;
    private static AgoraLocalAudioTrack customAudioTrack;

    private static String channelId = "agaa";
    private static String userId = "0";
    private static String audioFilePath = "test_data/send_audio_16k_1ch.pcm";
    private static int numOfChannels = 1;
    private static int sampleRate = 16000;
    private static long testTime = 60 * 1000;

    private final static AtomicBoolean connConnected = new AtomicBoolean(false);

    private static void parseArgs(String[] args) {
        SampleLogger.log("parseArgs args:" + Arrays.toString(args));
        if (args == null || args.length == 0) {
            return;
        }

        Map<String, String> parsedArgs = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 < args.length) {
                parsedArgs.put(args[i], args[i + 1]);
            } else {
                SampleLogger.log("Missing value for argument: " + args[i]);
            }
        }

        if (parsedArgs.containsKey("-channelId")) {
            channelId = parsedArgs.get("-channelId");
        }

        if (parsedArgs.containsKey("-userId")) {
            userId = parsedArgs.get("-userId");
        }

        if (parsedArgs.containsKey("-audioFilePath")) {
            audioFilePath = parsedArgs.get("-audioFilePath");
        }

        if (parsedArgs.containsKey("-testTime")) {
            testTime = Long.parseLong(parsedArgs.get("-testTime"));
        }
    }

    public static void main(String[] args) {
        parseArgs(args);
        String[] keys = Utils.readAppIdAndToken(".keys");
        appId = keys[0];
        token = keys[1];
        SampleLogger.log("read appId: " + appId + " token: " + token + " from .keys");

        // Initialize Agora service globally once
        service = new AgoraService();
        AgoraServiceConfig config = new AgoraServiceConfig();
        config.setAppId(appId);
        config.setEnableAudioDevice(0);
        config.setEnableAudioProcessor(1);
        config.setEnableVideo(1);
        config.setUseStringUid(0);
        config.setAudioScenario(Constants.AUDIO_SCENARIO_CHORUS);

        int ret = service.initialize(config);
        if (ret != 0) {
            SampleLogger.log("createAndInitAgoraService AgoraService.initialize fail ret:" + ret);
            releaseAgoraService();
            return;
        }

        ret = service.setLogFile(DEFAULT_LOG_PATH, DEFAULT_LOG_SIZE);
        service.setLogFilter(Constants.LOG_FILTER_DEBUG);
        if (ret != 0) {
            SampleLogger.log("createAndInitAgoraService AgoraService.setLogFile fail ret:" + ret);
            releaseAgoraService();
            return;
        }

        // Create a connection for each channel
        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        conn = service.agoraRtcConnCreate(ccfg);
        if (conn == null) {
            SampleLogger.log("AgoraService.agoraRtcConnCreate fail\n");
            releaseAgoraService();
            return;
        }

        ret = conn.registerObserver(new DefaultRtcConnObserver() {
            @Override
            public void onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason) {
                super.onConnected(agoraRtcConn, connInfo, reason);
                SampleLogger.log(
                        "onConnected chennalId:" + connInfo.getChannelId() + " userId:" + connInfo.getLocalUserId());
                connConnected.set(true);
                userId = connInfo.getLocalUserId();
            }

            @Override
            public void onUserJoined(AgoraRtcConn agoraRtcConn, String userId) {
                super.onUserJoined(agoraRtcConn, userId);
                SampleLogger.log("onUserJoined userId:" + userId);

            }

            @Override
            public void onUserLeft(AgoraRtcConn agoraRtcConn, String userId, int reason) {
                super.onUserLeft(agoraRtcConn, userId, reason);
                SampleLogger.log("onUserLeft userId:" + userId + " reason:" + reason);

            }
        });
        SampleLogger.log("registerObserver ret:" + ret);

        ret = conn.connect(token, channelId, userId);
        SampleLogger.log("Connecting to Agora channel " + channelId + " with userId " + userId + " ret:" + ret);

        if (ret != 0) {
            SampleLogger.log("conn.connect fail ret=" + ret);
            releaseConn();
            releaseAgoraService();
            return;
        }

        mediaNodeFactory = service.createMediaNodeFactory();
        audioFrameSender = mediaNodeFactory.createAudioPcmDataSender();
        // Create audio track
        customAudioTrack = service.createCustomAudioTrackPcm(audioFrameSender);
        conn.getLocalUser().publishAudio(customAudioTrack);

        int interval = 10;// ms

        int bufferSize = numOfChannels * (sampleRate / 1000) * interval * 2;
        byte[] buffer = new byte[bufferSize];

        FileSender pcmSendThread = new FileSender(audioFilePath, interval) {
            private AudioConsumerUtils audioConsumerUtils = null;

            @Override
            public void sendOneFrame(byte[] data, long timestamp) {
                if (data == null) {
                    return;
                }

                if (null != audioFrameSender) {
                    int consumeFrameCount = audioConsumerUtils.consume();
                    SampleLogger.log("send pcm " + consumeFrameCount + " frame data to channelId:"
                            + channelId + " from userId:" + userId);
                } else {
                    release(false);
                }
            }

            @Override
            public byte[] readOneFrame(FileInputStream fos) {
                if (fos != null) {
                    try {
                        int size = fos.read(buffer, 0, bufferSize);
                        if (size < 0) {
                            reset();
                            return null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (null == audioConsumerUtils) {
                    audioConsumerUtils = new AudioConsumerUtils(audioFrameSender, numOfChannels,
                            sampleRate);
                }
                audioConsumerUtils.pushPcmData(buffer);
                return buffer;
            }

            @Override
            public void release(boolean withJoin) {
                super.release(withJoin);
                if (null != audioConsumerUtils) {
                    audioConsumerUtils.release();
                    audioConsumerUtils = null;
                }
            }
        };

        while (!connConnected.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        pcmSendThread.start();

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < testTime) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        releaseConn();
        releaseAgoraService();
        System.exit(0);
    }

    private static void releaseConn() {
        SampleLogger.log("releaseConn for channelId:" + channelId + " userId:" + userId);
        if (conn == null) {
            return;
        }

        connConnected.set(false);

        if (null != mediaNodeFactory) {
            mediaNodeFactory.destroy();
            mediaNodeFactory = null;
        }

        if (null != audioFrameSender) {
            audioFrameSender.destroy();
            audioFrameSender = null;
        }

        if (null != customAudioTrack) {
            customAudioTrack.clearSenderBuffer();
            conn.getLocalUser().unpublishAudio(customAudioTrack);
            customAudioTrack.destroy();
            customAudioTrack = null;
        }

        int ret = conn.disconnect();
        if (ret != 0) {
            SampleLogger.log("conn.disconnect fail ret=" + ret);
        }

        // Unregister connection observer
        conn.unregisterObserver();
        conn.getLocalUser().unregisterObserver();

        conn.destroy();
        conn = null;

        SampleLogger.log("Disconnected from Agora channel successfully");
    }

    private static void releaseAgoraService() {
        if (service != null) {
            service.destroy();
            service = null;
        }
        SampleLogger.log("releaseAgoraService");
    }

}
