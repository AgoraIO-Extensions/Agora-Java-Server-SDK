package io.agora.rtc.example.scenario;

import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.AgoraVideoFrameObserver2;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.Constants;
import io.agora.rtc.DefaultRtcConnObserver;
import io.agora.rtc.IAudioFrameObserver;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.RtcConnInfo;
import io.agora.rtc.VadProcessResult;
import io.agora.rtc.VideoFrame;
import io.agora.rtc.VideoSubscriptionOptions;
import io.agora.rtc.example.common.SampleAudioFrameObserver;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.common.SampleVideFrameObserver;
import io.agora.rtc.example.utils.Utils;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceiverPcmYuvTest {
    private static String appId;
    private static String token;
    private final static String DEFAULT_LOG_PATH = "logs/agora_logs/agorasdk.log";
    private final static int DEFAULT_LOG_SIZE = 5 * 1024; // default log size is 5 mb

    private static AgoraService service;
    private static AgoraRtcConn conn;

    private static IAudioFrameObserver audioFrameObserver;
    private static AgoraVideoFrameObserver2 videoFrameObserver;

    private static String channelId = "agaa";
    private static String userId = "0";
    private static String audioOutFile = "test_data_out/receiver_audio_out";
    private static String videoOutFile = "test_data_out/receiver_video_out";
    private static int numOfChannels = 1;
    private static int sampleRate = 16000;
    private static String streamType = "high";
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

        if (parsedArgs.containsKey("-videoOutFile")) {
            videoOutFile = parsedArgs.get("-videoOutFile");
        }

        if (parsedArgs.containsKey("-streamType")) {
            streamType = parsedArgs.get("-streamType");
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
                SampleLogger.log("onConnected channelId :" + connInfo.getChannelId() + " reason:" + reason);
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

        // receiver pcm
        if (remoteUserId != null && !remoteUserId.isEmpty()) {
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
                    SampleLogger.log(
                            "onPlaybackAudioFrameBeforeMixing frame:" + frame + " audioFrame size " + byteArray.length
                                    + " channelId:"
                                    + channelId + " userId:" + userId);
                    writeAudioFrameToFile(byteArray);
                });

                return 1;
            }

        };

        conn.getLocalUser().registerAudioFrameObserver(audioFrameObserver, false, null);

        // receiver yuv
        VideoSubscriptionOptions subscriptionOptions = new VideoSubscriptionOptions();
        switch (streamType) {
            case "high":
                subscriptionOptions.setType(Constants.VIDEO_STREAM_HIGH);
                break;
            case "low":
                subscriptionOptions.setType(Constants.VIDEO_STREAM_LOW);
                break;
            default:
                return;
        }
        if (remoteUserId != null && !remoteUserId.isEmpty()) {
            conn.getLocalUser().subscribeVideo(remoteUserId, subscriptionOptions);
        } else {
            conn.getLocalUser().subscribeAllVideo(subscriptionOptions);
        }

        videoFrameObserver = new AgoraVideoFrameObserver2(
                new SampleVideFrameObserver(videoOutFile + "_" + channelId + "_" + userId + ".yuv") {
                    @Override
                    public void onFrame(AgoraVideoFrameObserver2 agoraVideoFrameObserver2, String channelId,
                            String remoteUserId, VideoFrame frame) {
                        if (frame == null) {
                            return;
                        }

                        int ylength = frame.getYBuffer().remaining();
                        int ulength = frame.getUBuffer().remaining();
                        int vlength = frame.getVBuffer().remaining();

                        byte[] data = new byte[ylength + ulength + vlength];
                        ByteBuffer buffer = ByteBuffer.wrap(data);
                        buffer.put(frame.getYBuffer()).put(frame.getUBuffer()).put(frame.getVBuffer());

                        final byte[] metaDataBufferData = io.agora.rtc.utils.Utils.getBytes(frame.getMetadataBuffer());
                        final byte[] alphaBufferData = io.agora.rtc.utils.Utils.getBytes(frame.getAlphaBuffer());

                        singleExecutorService.execute(() -> {
                            SampleLogger.log(String.format(
                                    "onFrame width:%d height:%d channelId:%s remoteUserId:%s frame size:%d %d %d with  channelId:%s userId:%s",
                                    frame.getWidth(), frame.getHeight(), channelId, remoteUserId, ylength, ulength,
                                    vlength,
                                    channelId, userId));

                            if (metaDataBufferData != null) {
                                SampleLogger.log("onFrame metaDataBuffer :" + new String(metaDataBufferData));
                            }

                            if (alphaBufferData != null) {
                                SampleLogger.log("onFrame getAlphaBuffer size:" + alphaBufferData.length + " mode:"
                                        + frame.getAlphaMode());
                            }

                            writeVideoFrameToFile(data);
                        });
                    }
                });
        conn.getLocalUser().registerVideoFrameObserver(videoFrameObserver);

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

        if (null != audioFrameObserver) {
            conn.getLocalUser().unregisterAudioFrameObserver();
            audioFrameObserver = null;
        }

        if (null != videoFrameObserver) {
            conn.getLocalUser().unregisterVideoFrameObserver(videoFrameObserver);
            videoFrameObserver.destroy();
            videoFrameObserver = null;
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
