package io.agora.rtc.example.scenario;

import io.agora.rtc.AgoraAudioVadConfigV2;
import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.Constants;
import io.agora.rtc.DefaultRtcConnObserver;
import io.agora.rtc.IAudioFrameObserver;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.VadProcessResult;
import io.agora.rtc.example.common.SampleAudioFrameObserver;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.utils.Utils;
import io.agora.rtc.utils.VadDumpUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceiverPcmVadTest {
    private static String appId;
    private static String token;
    private final static String DEFAULT_LOG_PATH = "logs/agora_logs/agorasdk.log";
    private final static int DEFAULT_LOG_SIZE = 5 * 1024; // default log size is 5 mb

    private static AgoraService service;
    private static AgoraRtcConn conn;
    private static IAudioFrameObserver audioFrameObserver;

    private static VadDumpUtils vadDumpUtils;

    private static String channelId = "agaa";
    private static String userId = "0";
    private static String audioOutFile = "test_data_out/receiver_audio_out";
    private static int numOfChannels = 1;
    private static int sampleRate = 16000;
    private static String remoteUserId = "";
    private static long testTime = 60 * 1000;

    private static final ExecutorService singleExecutorService = Executors.newSingleThreadExecutor();

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

        if (parsedArgs.containsKey("-audioOutFile")) {
            audioOutFile = parsedArgs.get("-audioOutFile");
        }

        if (parsedArgs.containsKey("-numOfChannels")) {
            numOfChannels = Integer.parseInt(parsedArgs.get("-numOfChannels"));
        }

        if (parsedArgs.containsKey("-sampleRate")) {
            sampleRate = Integer.parseInt(parsedArgs.get("-sampleRate"));
        }

        if (parsedArgs.containsKey("-remoteUserId")) {
            remoteUserId = parsedArgs.get("-remoteUserId");
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
        config.setLogFilePath(DEFAULT_LOG_PATH);
        config.setLogFileSize(DEFAULT_LOG_SIZE);
        config.setLogFilters(Constants.LOG_FILTER_DEBUG);

        int ret = service.initialize(config);
        if (ret != 0) {
            SampleLogger.log("createAndInitAgoraService AgoraService.initialize fail ret:" + ret);
            return;
        }

        // Create a connection for each channel
        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_AUDIENCE);
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
                SampleLogger.log("onConnected connInfo :" + connInfo + " reason:" + reason);
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

        if (!remoteUserId.isEmpty()) {
            conn.getLocalUser().subscribeAudio(remoteUserId);
        } else {
            conn.getLocalUser().subscribeAllAudio();
        }

        ret = conn.getLocalUser().setPlaybackAudioFrameBeforeMixingParameters(numOfChannels, sampleRate);
        SampleLogger.log("setPlaybackAudioFrameBeforeMixingParameters numOfChannels:" + numOfChannels + " sampleRate:"
                + sampleRate);
        if (ret != 0) {
            SampleLogger.log("setPlaybackAudioFrameBeforeMixingParameters fail ret=" + ret);
            releaseConn();
            releaseAgoraService();
            return;
        }

        vadDumpUtils = new VadDumpUtils("test_data_out/vad_dump");
        audioFrameObserver = new SampleAudioFrameObserver(
                audioOutFile + "_" + channelId + "_" + userId + ".pcm") {
            @Override
            public int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agoraLocalUser, String channelId,
                    String userId,
                    AudioFrame frame, VadProcessResult vadResult) {
                if (null == frame) {
                    return 0;
                }
                // Note: To improve data transmission efficiency, the buffer of the frame
                // object is a DirectByteBuffer.
                // Be sure to extract the byte array value in the callback synchronously
                // and then transfer it to the asynchronous thread for processing.
                // You can refer to {@link io.agora.rtc.utils.Utils#getBytes(ByteBuffer)}.
                byte[] byteArray = io.agora.rtc.utils.Utils.getBytes(frame.getBuffer());
                if (byteArray == null) {
                    return 0;
                }
                singleExecutorService.execute(() -> {
                    SampleLogger
                            .log("onPlaybackAudioFrameBeforeMixing frame:" + frame + " vadResult:" + vadResult
                                    + "audioFrame size " + byteArray.length
                                    + " channelId:"
                                    + channelId + " userId:" + userId);
                    writeAudioFrameToFile(byteArray);
                });

                if (null != vadResult) {
                    singleExecutorService.execute(() -> {
                        SampleLogger.log("onPlaybackAudioFrameBeforeMixing vadResult:" + vadResult);
                        if (vadResult.getState() == Constants.VadState.START_SPEAKING
                                || vadResult.getState() == Constants.VadState.SPEAKING
                                || vadResult.getState() == Constants.VadState.STOP_SPEAKING) {
                            writeAudioFrameToFile(vadResult.getOutFrame(),
                                    audioOutFile + "_" + channelId + "_" + userId + "_vad.pcm");
                        }
                    });

                    vadDumpUtils.write(frame, vadResult.getOutFrame(), vadResult.getState());
                }
                return 1;
            }

        };

        conn.getLocalUser().registerAudioFrameObserver(audioFrameObserver, true, new AgoraAudioVadConfigV2());

        ret = conn.connect(token, channelId, userId);
        SampleLogger.log("Connecting to Agora channel " + channelId + " with userId " + userId + " ret:" + ret);
        if (ret != 0) {
            SampleLogger.log("conn.connect fail ret=" + ret);
            releaseConn();
            releaseAgoraService();
            return;
        }

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

        if (null != vadDumpUtils) {
            vadDumpUtils.release();
            vadDumpUtils = null;
        }

        if (null != audioFrameObserver) {
            conn.getLocalUser().unregisterAudioFrameObserver();
            audioFrameObserver = null;
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

        singleExecutorService.shutdown();

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
