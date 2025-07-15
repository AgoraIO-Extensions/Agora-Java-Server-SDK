package io.agora.rtc.example.common;

import io.agora.rtc.AgoraMediaNodeFactory;
import io.agora.rtc.AgoraParameter;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.Constants;
import io.agora.rtc.example.utils.Utils;
import java.io.File;

public class AgoraServiceInitializer {
    private static AgoraService service;
    private static AgoraMediaNodeFactory mediaNodeFactory;
    private static AgoraParameter parameter;

    public static String DEFAULT_LOG_PATH = "logs/agora_logs/agorasdk.log";
    public static int DEFAULT_LOG_SIZE = 5 * 1024; // default log size is 5 mb

    public static boolean isFirstInit = true;

    public synchronized static int initAgoraService(
        int enableAudioDevice, int enableAudioProcessor, int enableVideo, ArgsConfig argsConfig) {
        if (service != null) {
            return 0;
        }
        SampleLogger.log(
            "initAgoraService AgoraService.getSdkVersion=" + AgoraService.getSdkVersion());

        initData(argsConfig);

        if (isFirstInit) {
            isFirstInit = false;
        }

        SampleLogger.log("initAgoraService argsConfig=" + argsConfig);

        service = new AgoraService();
        AgoraServiceConfig config = new AgoraServiceConfig();
        config.setAppId(argsConfig.getAppId());
        config.setEnableAudioDevice(enableAudioDevice);
        config.setEnableAudioProcessor(enableAudioProcessor);
        config.setEnableVideo(enableVideo);
        config.setUseStringUid(argsConfig.isEnableStringUid() ? 1 : 0);
        config.setAudioScenario(Constants.AUDIO_SCENARIO_CHORUS);

        config.setLogFilePath(DEFAULT_LOG_PATH);
        config.setLogFileSize(DEFAULT_LOG_SIZE);
        config.setLogFilters(argsConfig.getLogFilter());

        SampleLogger.log("initAgoraService config=" + config);

        int ret = service.initialize(config);
        if (ret != 0) {
            SampleLogger.log("initAgoraService AgoraService.initialize fail ret=" + ret);
            return -1;
        }

        mediaNodeFactory = service.createMediaNodeFactory();

        initAgoraParameter();

        return 0;
    }

    private static void initData(ArgsConfig argsConfig) {
        //SampleLogger.enableLog(argsConfig.isEnableLog());

        if (argsConfig.getConnectionCount() <= 0) {
            argsConfig.setConnectionCount(1);
        }

        File testDataOutFile = new File("test_data_out/");
        if (!testDataOutFile.exists()) {
            testDataOutFile.mkdirs();
        } else {
            if (isFirstInit) {
                Utils.deleteAllFilesInDirectory(testDataOutFile.getAbsolutePath());
            }
        }

        if (argsConfig.getAppId() == null || argsConfig.getAppId().isEmpty()) {
            try {
                String[] appIdAndToken = Utils.readAppIdAndToken(".keys");
                argsConfig.setAppId(appIdAndToken[0]);
                argsConfig.setToken(appIdAndToken[1]);
            } catch (Exception e) {
                SampleLogger.error(
                    "Error reading appId and token from .keys file: " + e.getMessage());
            }
        }
    }

    private static void initAgoraParameter() {
        SampleLogger.log("initAgoraParameter service=" + service);
        parameter = service.getAgoraParameter();
        // parameter.setParameters("{\"rtc.enable_nasa2\":false}");
        // parameter.setParameters("{\"che.video.useSimpleParser\":true}");
        // parameter.setParameters("{\"rtc.video.enable_periodic_strategy\":false}");

        // AgoraParameter parameter = service.getAgoraParameter();
        // parameter.setParameters("{\"che.audio.custom_payload_type\":78}");
        // parameter.setParameters("{\"che.audio.aec.enable\":false}");
        // parameter.setParameters("{\"che.audio.ans.enable\":false}");
        // parameter.setParameters("{\"che.audio.agc.enable\":false}");
        // parameter.setParameters("{\"che.audio.custom_bitrate\":128000}");
        // parameter.setParameters(
        // "{\"che.audio.frame_dump\":{\"location\":\"all\",\"action\":\"start\",\"max_size_bytes\":\"100000000\",\"uuid\":\"123456789\",
        // \"duration\": \"150000\"}}");
    }

    public static AgoraService getService() {
        return service;
    }

    public static AgoraMediaNodeFactory getMediaNodeFactory() {
        return mediaNodeFactory;
    }

    public static AgoraParameter getParameter() {
        return parameter;
    }

    public static void destroyAgoraService() {
        if (parameter != null) {
            parameter = null;
        }
        if (mediaNodeFactory != null) {
            mediaNodeFactory.destroy();
            mediaNodeFactory = null;
        }

        if (service != null) {
            service.destroy();
            service = null;
        }
    }
}
