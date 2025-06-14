package io.agora.rtc.example.common;

import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.Constants;

public class SampleCommon {

    public static String DEFAULT_LOG_PATH = "logs/agora_logs/agorasdk.log";
    public static int DEFAULT_LOG_SIZE = 5 * 1024; // default log size is 5 mb

    public static AgoraService createAndInitAgoraService(int enableAudioDevice, int enableAudioProcessor,
            int enableVideo,
            int useStringUid, String appId) {
        SampleLogger.log("createAndInitAgoraService AgoraService.getSdkVersion=" + AgoraService.getSdkVersion());
        AgoraService service = new AgoraService();
        AgoraServiceConfig config = new AgoraServiceConfig();
        config.setAppId(appId);
        config.setEnableAudioDevice(enableAudioDevice);
        config.setEnableAudioProcessor(enableAudioProcessor);
        config.setEnableVideo(enableVideo);
        config.setUseStringUid(useStringUid);
        config.setAudioScenario(Constants.AUDIO_SCENARIO_CHORUS);
        
        config.setLogFilePath(DEFAULT_LOG_PATH);
        config.setLogFileSize(DEFAULT_LOG_SIZE);
        config.setLogFilters(ArgsConfig.logFilter);

        int ret = service.initialize(config);
        if (ret != 0) {
            SampleLogger.log("createAndInitAgoraService AgoraService.initialize fail ret=" + ret);
            return null;
        }

        return service;
    }
}
