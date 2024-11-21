package io.agora.rtc.test.ai;

import io.agora.rtc.AgoraService;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.common.SampleCommon;
import io.agora.rtc.common.SampleLogger;
import io.agora.rtc.common.Utils;
import io.agora.rtc.test.AgoraTest;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import io.agora.rtc.common.AgoraConnectionTask;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class AgoraAiTest {
    protected final ExecutorService singleExecutorService = Executors.newSingleThreadExecutor();
    protected final ExecutorService testTaskExecutorService = Executors.newCachedThreadPool();

    protected static String APPID;
    protected static String TOKEN;
    protected static volatile AgoraService service;

    protected String token = "";
    protected String userId = "";
    protected String channelId = "aga";
    protected String remoteUserId = "";
    protected String streamType = "high";
    protected String audioFile = "test_data/send_audio_16k_1ch.pcm";
    protected String audioExpectedFile = "test_data/vad_test_expected.pcm";
    protected String audioOutFile = "test_data_out/vad_test_out.pcm";
    protected String videoFile = "received_video.h264";
    protected String videoOutFile = "test_data_out/received_video";
    protected int sampleRate = 16000;
    protected int numOfChannels = 1;
    protected int bitDepth = 16;
    protected int height = 320;
    protected int width = 640;
    protected int fps = 30;
    protected int frameCount = 30;
    protected boolean enableStringUid = false;
    protected int connectionCount = 1;
    protected String fileType = "pcm";
    protected int testTime = 0;// s
    protected int sleepTime = 1;// s
    protected boolean enableLog = true;
    protected int enableEncryptionMode = 0;
    protected int encryptionMode = Constants.ENCRYPTION_MODE_SM4_128_ECB;
    protected String encryptionKey = "";
    protected int enableSaveFile = 1;

    protected int useStringUid = 0;
    private AtomicInteger testTaskCount = new AtomicInteger(0);

    public enum TestTask {
        NONE, SEND_PCM, SEND_YUV, SEND_H264, SEND_AAC, SEND_MP4, SEND_DATA_STREAM, RECEIVE_PCM, RECEIVE_YUV,
        RECEIVE_H264, RECEIVE_PCM_H264,
        SEND_RECEIVE_PCM_YUV
    }

    class SignalFunc implements SignalHandler {
        public void handle(Signal arg0) {
            SampleLogger.log("catch signal " + arg0);
            testTaskCount.set(0);
        }
    }

    public AgoraAiTest() {
        String[] keys = Utils.readAppIdAndToken(".keys");
        APPID = keys[0];
        TOKEN = keys[1];
        token = TOKEN;
        SampleLogger.log("read APPID: " + APPID + " TOKEN: " + TOKEN + " from .keys");
    }

    protected static void startTest(String[] args, AgoraAiTest test) {
        test.handleOptions(args);
        test.sdkTest();
    }

    public void sdkTest() {
        SignalFunc handler = new SignalFunc();
        Signal.handle(new Signal("ABRT"), handler);
        Signal.handle(new Signal("INT"), handler);
        setup();
        while (testTaskCount.get() != 0) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cleanup();
        System.exit(0);
    }

    public void handleOptions(String[] args) {
        SampleLogger.log(Arrays.toString(args));
        Options options = new Options();
        Option optToken = new Option("token", true, "The token for authentication");
        Option optChannelId = new Option("channelId", true, "Channel Id");
        Option optUserId = new Option("userId", true, "User Id / default is 0");
        Option optRemoteUserId = new Option("remoteUserId", true, "The remote user to receive stream from");
        Option optAudioFile = new Option("audioFile", true, "Output audio file");
        Option optAudioExpectedFile = new Option("audioExpectedFile", true, "Output audio file");
        Option optAudioOutFile = new Option("audioOutFile", true, "Output audio file");
        Option optVideoFile = new Option("videoFile", true, "Output video file");
        Option optVideoOutFile = new Option("videoOutFile", true, "Output video file");
        Option optSampleRate = new Option("sampleRate", true, "Sample rate for received audio");
        Option optNumOfChannels = new Option("numOfChannels", true, "Number of channels for received audio");
        Option optStreamType = new Option("streamType", true, "the stream  type");
        Option optStringUid = new Option("stringUid", false, "whether to have string uid");
        Option optConnectionCount = new Option("connectionCount", true, "connectionCount");
        Option optFps = new Option("fps", true, "Target frame rate for sending the video stream");
        Option optHeight = new Option("height", true, "video height");
        Option optWidth = new Option("width", true, "video width");
        Option optFrameCount = new Option("frameCount", true, "frameCount");
        Option optFileType = new Option("fileType", true, "fileType");
        Option optTestTime = new Option("testTime", true, "testTime");
        Option optSleepTime = new Option("sleepTime", true, "sleepTime");
        Option optEnableLog = new Option("enableLog", true, "enableLog");
        Option optEnableEncryptionMode = new Option("enableEncryptionMode", true,
                "enableEncryptionMode");
        Option optEncryptionMode = new Option("encryptionMode", true,
                "encryptionMode");
        Option optEncryptionKey = new Option("encryptionKey", true,
                "encryptionKey");
        Option ptEnableSaveFile = new Option("enableSaveFile", true,
                "enableSaveFile");

        options.addOption(optToken);
        options.addOption(optChannelId);
        options.addOption(optUserId);
        options.addOption(optRemoteUserId);
        options.addOption(optAudioFile);
        options.addOption(optAudioExpectedFile);
        options.addOption(optAudioOutFile);
        options.addOption(optVideoFile);
        options.addOption(optVideoOutFile);
        options.addOption(optSampleRate);
        options.addOption(optNumOfChannels);
        options.addOption(optStreamType);
        options.addOption(optStringUid);
        options.addOption(optConnectionCount);
        options.addOption(optFps);
        options.addOption(optHeight);
        options.addOption(optWidth);
        options.addOption(optFrameCount);
        options.addOption(optFileType);
        options.addOption(optTestTime);
        options.addOption(optSleepTime);
        options.addOption(optEnableLog);
        options.addOption(optEnableEncryptionMode);
        options.addOption(optEncryptionMode);
        options.addOption(optEncryptionKey);
        options.addOption(ptEnableSaveFile);

        CommandLine commandLine = null;
        CommandLineParser parser = new DefaultParser();
        try {
            commandLine = parser.parse(options, args);
        } catch (Exception e) {
            // e.printStackTrace();
            SampleLogger.log("unkown option: " + e.getMessage());
        }
        if (commandLine == null) {
            return;
        }

        if (commandLine.hasOption(optToken)) {
            String o_token = commandLine.getOptionValue("token");
            if (o_token != null) {
                token = o_token;
            }
        }

        if (commandLine.hasOption(optVideoFile)) {
            String o_videoFile = commandLine.getOptionValue("videoFile");
            if (o_videoFile != null) {
                videoFile = o_videoFile;
            }
        }

        if (commandLine.hasOption(optVideoOutFile)) {
            String o_videoOutFile = commandLine.getOptionValue("videoOutFile");
            if (o_videoOutFile != null) {
                videoOutFile = o_videoOutFile;
            }
        }

        if (commandLine.hasOption(optChannelId)) {
            channelId = commandLine.getOptionValue("channelId");
            if (channelId == null) {
                throw new IllegalArgumentException("no channeldId provided !!!");
            }
        }

        if (commandLine.hasOption(optUserId)) {
            String o_userId = commandLine.getOptionValue("userId");
            if (o_userId != null) {
                userId = o_userId;
            }
        }

        if (commandLine.hasOption(optRemoteUserId)) {
            String o_remoteUserId = commandLine.getOptionValue("remoteUserId");
            if (o_remoteUserId != null) {
                remoteUserId = o_remoteUserId;
            }
        }

        if (commandLine.hasOption(optAudioFile)) {
            String o_audioFile = commandLine.getOptionValue("audioFile");
            if (o_audioFile != null) {
                audioFile = o_audioFile;
            }
        }

        if (commandLine.hasOption(optAudioExpectedFile)) {
            String o_audioExpectedFile = commandLine.getOptionValue("audioExpectedFile");
            if (o_audioExpectedFile != null) {
                audioExpectedFile = o_audioExpectedFile;
            }
        }

        if (commandLine.hasOption(optAudioOutFile)) {
            String o_audioOutFile = commandLine.getOptionValue("audioOutFile");
            if (o_audioOutFile != null) {
                audioOutFile = o_audioOutFile;
            }
        }

        if (commandLine.hasOption(optSampleRate)) {
            try {
                sampleRate = Integer.valueOf(commandLine.getOptionValue("sampleRate"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optNumOfChannels)) {
            try {
                numOfChannels = Integer.valueOf(commandLine.getOptionValue("numOfChannels"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optStreamType)) {
            try {
                streamType = commandLine.getOptionValue("streamType");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            enableStringUid = commandLine.hasOption(optStringUid);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (commandLine.hasOption(optConnectionCount)) {
            try {
                connectionCount = Integer.parseInt(commandLine.getOptionValue("connectionCount"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optHeight)) {
            try {
                height = Integer.valueOf(commandLine.getOptionValue("height"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (commandLine.hasOption(optWidth)) {
            try {
                width = Integer.valueOf(commandLine.getOptionValue("width"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optFps)) {
            try {
                fps = Integer.valueOf(commandLine.getOptionValue("fps"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (commandLine.hasOption(optFrameCount)) {
            try {
                frameCount = Integer.parseInt(commandLine.getOptionValue("frameCount"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optFileType)) {
            try {
                fileType = commandLine.getOptionValue("fileType");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optTestTime)) {
            try {
                testTime = Integer.parseInt(commandLine.getOptionValue("testTime"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optSleepTime)) {
            try {
                sleepTime = Integer.parseInt(commandLine.getOptionValue("sleepTime"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optEnableLog)) {
            try {
                enableLog = Boolean.parseBoolean(commandLine.getOptionValue("enableLog"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optEnableEncryptionMode)) {
            try {
                enableEncryptionMode = Integer.parseInt(commandLine.getOptionValue("enableEncryptionMode"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optEncryptionMode)) {
            try {
                encryptionMode = Integer.parseInt(commandLine.getOptionValue("encryptionMode"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optEncryptionKey)) {
            try {
                encryptionKey = commandLine.getOptionValue("encryptionKey");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(ptEnableSaveFile)) {
            try {
                enableSaveFile = Integer.parseInt(commandLine.getOptionValue("enableSaveFile"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setup() {
        File testDataOutFile = new File("test_data_out/");
        if (!testDataOutFile.exists()) {
            testDataOutFile.mkdirs();
        }

        SampleLogger.enableLog(enableLog);

        if (enableStringUid) {
            useStringUid = 1;
        }

        if (connectionCount <= 0) {
            connectionCount = 1;
        }

        SampleLogger.log("connectionCount:" + connectionCount);

        service = SampleCommon.createAndInitAgoraService(0, 1, 1, useStringUid, APPID);

    }

    protected boolean createConnectionAndTest(RtcConnConfig ccfg, String channelId, String userId, TestTask testTask,
            long testDuration) {
        SampleLogger.log("createConnectionAndTest start ccfg:" + ccfg + " channelId:" + channelId + " userId:" + userId
                + " testTask:" + testTask + " testDuration:" + testDuration);
        testTaskCount.incrementAndGet();
        testTaskExecutorService.execute(() -> {
            final CountDownLatch testFinishLatch = new CountDownLatch(1);
            final CountDownLatch connectedLatch = new CountDownLatch(1);
            AgoraConnectionTask connTask = new AgoraConnectionTask(service, testDuration);
            connTask.setCallback(new AgoraConnectionTask.TaskCallback() {
                @Override
                public void onConnected() {
                    SampleLogger.log("onConnected for task:" + testTask);
                    connectedLatch.countDown();
                }

                @Override
                public void onTestFinished() {
                    SampleLogger.log("test finished for task:" + testTask);
                    testTaskCount.decrementAndGet();
                    testFinishLatch.countDown();
                }

                @Override
                public void onStreamMessage(String userId, int streamId, String data, long length) {
                    onStreamMessageReceive(userId, streamId, data, length);
                }
            });
            connTask.createConnectionAndTest(ccfg, token, channelId, userId, enableEncryptionMode, encryptionMode,
                    encryptionKey);

            try {
                connectedLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (TestTask.SEND_PCM == testTask) {
                connTask.sendPcmTask(audioFile, 10, numOfChannels, sampleRate, true);
            } else if (TestTask.SEND_H264 == testTask) {
                connTask.sendH264Task(videoFile, 1000 / fps, 0, 0, true);
            } else if (TestTask.SEND_YUV == testTask) {
                connTask.sendYuvTask(videoFile, 1000 / fps, height, width, fps, true);
            } else if (TestTask.SEND_AAC == testTask) {
                connTask.sendAacTask(audioFile, 20, numOfChannels, sampleRate, true);
            } else if (TestTask.SEND_MP4 == testTask) {
                connTask.sendAvMediaTask(audioFile, 50);
            } else if (TestTask.SEND_DATA_STREAM == testTask) {
                connTask.sendDataStreamTask(1, 50, true);
            } else if (TestTask.RECEIVE_PCM == testTask) {
                connTask.registerPcmObserverTask(remoteUserId,
                        ("".equals(audioOutFile)) ? "" : (audioOutFile + "_" + channelId + ".pcm"), numOfChannels,
                        sampleRate, true, enableSaveFile == 1);
            } else if (TestTask.RECEIVE_PCM_H264 == testTask) {
                connTask.registerPcmObserverTask(remoteUserId,
                        ("".equals(audioOutFile)) ? "" : (audioOutFile + "_" + channelId + ".pcm"), numOfChannels,
                        sampleRate, false, enableSaveFile == 1);
                connTask.registerH264ObserverTask(remoteUserId,
                        ("".equals(videoOutFile)) ? "" : (videoOutFile + "_" + channelId + ".h264"), streamType, true,
                        enableSaveFile == 1);
            } else if (TestTask.RECEIVE_YUV == testTask) {
                connTask.registerYuvObserverTask(remoteUserId,
                        ("".equals(videoOutFile)) ? "" : (videoOutFile + "_" + channelId + ".yuv"), streamType, true);
            } else if (TestTask.RECEIVE_H264 == testTask) {
                connTask.registerH264ObserverTask(remoteUserId,
                        ("".equals(videoOutFile)) ? "" : (videoOutFile + "_" + channelId + ".h264"), streamType, true,
                        enableSaveFile == 1);
            } else if (TestTask.SEND_RECEIVE_PCM_YUV == testTask) {
                connTask.sendPcmTask(audioFile, 10, numOfChannels, sampleRate, false);
                connTask.sendYuvTask(videoFile, 1000 / fps, height, width, fps, false);
                connTask.registerPcmObserverTask(remoteUserId,
                        ("".equals(audioOutFile)) ? "" : (audioOutFile + "_" + channelId + ".pcm"), numOfChannels,
                        sampleRate, false, enableSaveFile == 1);
                connTask.registerYuvObserverTask(remoteUserId,
                        ("".equals(videoOutFile)) ? "" : (videoOutFile + "_" + channelId + ".yuv"), streamType, true);
            }

            onConnected(connTask.getConn(), channelId, userId);

            if (TestTask.NONE != testTask) {
                try {
                    testFinishLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(testDuration * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                connTask.releaseConn();
                testTaskCount.decrementAndGet();
            }
            connTask = null;
            System.gc();
            SampleLogger.log("createConnectionAndTest done");
        });
        return true;
    }

    protected void onConnected(AgoraRtcConn conn, String channelId, String userId) {
        SampleLogger.log("onConnected channelId:" + channelId + " userId:" + userId);

    }

    protected void onStreamMessageReceive(String userId, int streamId, String data, long length) {
        SampleLogger
                .log("onStreamMessageReceive userId:" + userId + " streamId:" + streamId + " data:" + data + " length:"
                        + length);
    }

    public void cleanup() {
        singleExecutorService.shutdown();
        testTaskExecutorService.shutdown();
        // Destroy Agora Service
        if (null != service) {
            service.destroy();
            service = null;
        }
    }
}