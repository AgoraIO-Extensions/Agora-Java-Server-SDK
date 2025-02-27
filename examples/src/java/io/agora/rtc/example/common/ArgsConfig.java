package io.agora.rtc.example.common;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;

import io.agora.rtc.Constants;

public class ArgsConfig {
    public static String appId = "";
    public static String token = "";

    public static String userId = "0";
    public static String channelId = "aga";
    public static String remoteUserId = "";
    public static String streamType = "high";
    public static String audioFile = "test_data/send_audio_16k_1ch.pcm";
    public static String audioExpectedFile = "test_data/vad_test_expected.pcm";
    public static String audioOutFile = "test_data_out/vad_test_out.pcm";
    public static String videoFile = "test_data/send_video.h264";
    public static String highVideoFile = "test_data/send_video.h264";
    public static String lowVideoFile = "test_data/send_video.h264";
    public static String videoOutFile = "test_data_out/received_video";
    public static int sampleRate = 16000;
    public static int numOfChannels = 1;
    public static int bitDepth = 16;
    public static int height = 320;
    public static int width = 640;
    public static int fps = 30;
    public static int frameCount = 30;
    public static boolean enableStringUid = false;
    public static int connectionCount = 1;
    public static String fileType = "pcm";
    public static int testTime = 0;
    public static int sleepTime = 1;
    public static boolean enableLog = true;
    public static int enableEncryptionMode = 0;
    public static int encryptionMode = Constants.ENCRYPTION_MODE_SM4_128_ECB;
    public static String encryptionKey = "";
    public static int enableAudioLabel = 0;
    public static int enableCloudProxy = 0;
    public static int enableSimulcastStream = 0;
    public static int enableSaveFile = 1;
    public static int enableAudioCache = 1;
    public static int enableAlpha = 0;
    public static int enableVad = 0;
    public static int enableSendAudioMetaData = 0;
    public static int singleChannel = 0;
    public static int isStressTest = 0;

    public static void handleOptions(String[] args) {
        SampleLogger.log(Arrays.toString(args));
        ArrayList<String> filteredArgsList = new ArrayList<>();

        for (String arg : args) {
            if (!"-asan".equals(arg)) {
                filteredArgsList.add(arg);
            }
        }

        String[] filteredArgs = filteredArgsList.toArray(new String[0]);

        org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
        org.apache.commons.cli.Option optToken = new org.apache.commons.cli.Option("token", true,
                "The token for authentication");
        org.apache.commons.cli.Option optChannelId = new org.apache.commons.cli.Option("channelId", true, "Channel Id");
        org.apache.commons.cli.Option optUserId = new org.apache.commons.cli.Option("userId", true,
                "User Id / default is 0");
        org.apache.commons.cli.Option optRemoteUserId = new org.apache.commons.cli.Option("remoteUserId", true,
                "The remote user to receive stream from");
        org.apache.commons.cli.Option optAudioFile = new org.apache.commons.cli.Option("audioFile", true,
                "Output audio file");
        org.apache.commons.cli.Option optAudioExpectedFile = new org.apache.commons.cli.Option("audioExpectedFile",
                true, "Output audio file");
        org.apache.commons.cli.Option optAudioOutFile = new org.apache.commons.cli.Option("audioOutFile", true,
                "Output audio file");
        org.apache.commons.cli.Option optVideoFile = new org.apache.commons.cli.Option("videoFile", true,
                "Output video file");
        org.apache.commons.cli.Option optHighVideoFile = new org.apache.commons.cli.Option("highVideoFile", true,
                "Output video file");
        org.apache.commons.cli.Option optLowVideoFile = new org.apache.commons.cli.Option("lowVideoFile", true,
                "Output video file");
        org.apache.commons.cli.Option optVideoOutFile = new org.apache.commons.cli.Option("videoOutFile", true,
                "Output video file");
        org.apache.commons.cli.Option optSampleRate = new org.apache.commons.cli.Option("sampleRate", true,
                "Sample rate for received audio");
        org.apache.commons.cli.Option optNumOfChannels = new org.apache.commons.cli.Option("numOfChannels", true,
                "Number of channels for received audio");
        org.apache.commons.cli.Option optStreamType = new org.apache.commons.cli.Option("streamType", true,
                "the stream  type");
        org.apache.commons.cli.Option optStringUid = new org.apache.commons.cli.Option("stringUid", false,
                "whether to have string uid");
        org.apache.commons.cli.Option optConnectionCount = new org.apache.commons.cli.Option("connectionCount", true,
                "connectionCount");
        org.apache.commons.cli.Option optFps = new org.apache.commons.cli.Option("fps", true,
                "Target frame rate for sending the video stream");
        org.apache.commons.cli.Option optHeight = new org.apache.commons.cli.Option("height", true, "video height");
        org.apache.commons.cli.Option optWidth = new org.apache.commons.cli.Option("width", true, "video width");
        org.apache.commons.cli.Option optFrameCount = new org.apache.commons.cli.Option("frameCount", true,
                "frameCount");
        org.apache.commons.cli.Option optFileType = new org.apache.commons.cli.Option("fileType", true, "fileType");
        org.apache.commons.cli.Option optTestTime = new org.apache.commons.cli.Option("testTime", true, "testTime");
        org.apache.commons.cli.Option optSleepTime = new org.apache.commons.cli.Option("sleepTime", true, "sleepTime");
        org.apache.commons.cli.Option optEnableLog = new org.apache.commons.cli.Option("enableLog", true, "enableLog");
        org.apache.commons.cli.Option optEnableEncryptionMode = new org.apache.commons.cli.Option(
                "enableEncryptionMode", true,
                "enableEncryptionMode");
        org.apache.commons.cli.Option optEncryptionMode = new org.apache.commons.cli.Option("encryptionMode", true,
                "encryptionMode");
        org.apache.commons.cli.Option optEncryptionKey = new org.apache.commons.cli.Option("encryptionKey", true,
                "encryptionKey");
        org.apache.commons.cli.Option optEnableAudioLabel = new org.apache.commons.cli.Option("enableAudioLabel", true,
                "enableAudioLabel");
        org.apache.commons.cli.Option optEnableCloudProxy = new org.apache.commons.cli.Option("enableCloudProxy", true,
                "enableCloudProxy");
        org.apache.commons.cli.Option optEnableSimulcastStream = new org.apache.commons.cli.Option(
                "enableSimulcastStream", true,
                "enableSimulcastStream");
        org.apache.commons.cli.Option optEnableSaveFile = new org.apache.commons.cli.Option("enableSaveFile", true,
                "enableSaveFile");
        org.apache.commons.cli.Option optEnableAudioCache = new org.apache.commons.cli.Option("enableAudioCache", true,
                "enableAudioCache");
        org.apache.commons.cli.Option optEnableAlpha = new org.apache.commons.cli.Option("enableAlpha", true,
                "enableAlpha");
        org.apache.commons.cli.Option optEnableVad = new org.apache.commons.cli.Option("enableVad", true,
                "enableVad");
        org.apache.commons.cli.Option optEnableSendAudioMetaData = new org.apache.commons.cli.Option(
                "enableSendAudioMetaData", true,
                "enableSendAudioMetaData");
        org.apache.commons.cli.Option optSingleChannel = new org.apache.commons.cli.Option("singleChannel", true,
                "singleChannel");
        org.apache.commons.cli.Option optIsStressTest = new org.apache.commons.cli.Option("isStressTest", true,
                "isStressTest");

        options.addOption(optToken);
        options.addOption(optChannelId);
        options.addOption(optUserId);
        options.addOption(optRemoteUserId);
        options.addOption(optAudioFile);
        options.addOption(optAudioExpectedFile);
        options.addOption(optAudioOutFile);
        options.addOption(optVideoFile);
        options.addOption(optHighVideoFile);
        options.addOption(optLowVideoFile);
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
        options.addOption(optEnableAudioLabel);
        options.addOption(optEnableCloudProxy);
        options.addOption(optEnableSimulcastStream);
        options.addOption(optEnableSaveFile);
        options.addOption(optEnableAudioCache);
        options.addOption(optEnableAlpha);
        options.addOption(optEnableVad);
        options.addOption(optEnableSendAudioMetaData);
        options.addOption(optSingleChannel);
        options.addOption(optIsStressTest);

        CommandLine commandLine = null;
        CommandLineParser parser = new DefaultParser();
        try {
            commandLine = parser.parse(options, filteredArgs);
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

        if (commandLine.hasOption(optHighVideoFile)) {
            String o_highVideoFile = commandLine.getOptionValue("highVideoFile");
            if (o_highVideoFile != null) {
                highVideoFile = o_highVideoFile;
            }
        }

        if (commandLine.hasOption(optLowVideoFile)) {
            String o_lowVideoFile = commandLine.getOptionValue("lowVideoFile");
            if (o_lowVideoFile != null) {
                lowVideoFile = o_lowVideoFile;
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

        if (commandLine.hasOption(optEnableAudioLabel)) {
            try {
                enableAudioLabel = Integer.parseInt(commandLine.getOptionValue("enableAudioLabel"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optEnableCloudProxy)) {
            try {
                enableCloudProxy = Integer.parseInt(commandLine.getOptionValue("enableCloudProxy"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optEnableSimulcastStream)) {
            try {
                enableSimulcastStream = Integer.parseInt(commandLine.getOptionValue("enableSimulcastStream"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optEnableSaveFile)) {
            try {
                enableSaveFile = Integer.parseInt(commandLine.getOptionValue("enableSaveFile"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optEnableAudioCache)) {
            try {
                enableAudioCache = Integer.parseInt(commandLine.getOptionValue("enableAudioCache"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optEnableAlpha)) {
            try {
                enableAlpha = Integer.parseInt(commandLine.getOptionValue("enableAlpha"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optEnableVad)) {
            try {
                enableVad = Integer.parseInt(commandLine.getOptionValue("enableVad"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optEnableSendAudioMetaData)) {
            try {
                enableSendAudioMetaData = Integer.parseInt(commandLine.getOptionValue("enableSendAudioMetaData"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optSingleChannel)) {
            try {
                singleChannel = Integer.parseInt(commandLine.getOptionValue("singleChannel"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption(optIsStressTest)) {
            try {
                isStressTest = Integer.parseInt(commandLine.getOptionValue("isStressTest"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
