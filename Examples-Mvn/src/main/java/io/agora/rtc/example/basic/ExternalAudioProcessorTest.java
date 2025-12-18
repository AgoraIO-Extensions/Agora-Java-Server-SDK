package io.agora.rtc.example.basic;

import io.agora.rtc.AgoraAudioVadConfigV2;
import io.agora.rtc.AgoraExternalAudioProcessor;
import io.agora.rtc.AgoraService;
import io.agora.rtc.AgoraServiceConfig;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.Constants;
import io.agora.rtc.IExternalAudioProcessorObserver;
import io.agora.rtc.VadProcessResult;
import io.agora.rtc.apm.AgoraApmConfig;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.utils.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExternalAudioProcessorTest {
    private String appId;
    private String token;
    private final String DEFAULT_LOG_PATH = "logs/agora_logs/agorasdk.log";
    private final int DEFAULT_LOG_SIZE = 5 * 1024; // default log size is 5 mb

    private int sampleRate = 16000;
    private int numOfChannels = 1;
    private String nearAudioFile = "test_data/vad_test_16k_1ch.pcm";
    private String audioOutFile = "test_data_out/external_audio_processor_test_out";
    // 10ms one frame
    private static final int INTERVAL = 10; // ms
    private static final int MAX_TASK_COUNT = 2;

    private final ExecutorService taskExecutor = Executors.newCachedThreadPool();
    private final ExecutorService audioWriteExecutor = Executors.newSingleThreadExecutor();

    private static AgoraService service;
    private static List<AgoraExternalAudioProcessor> audioProcessorList = new ArrayList<AgoraExternalAudioProcessor>(
            MAX_TASK_COUNT);

    private boolean enableApmDump = false;
    private boolean enableApm3A = true;
    private boolean forceExit = true;

    // frame processing latency statistics
    private long callbackCount = 0;
    private volatile long lastPushTimeNs = 0;
    private volatile long totalLatencyUs = 0;

    public void setEnableApmDump(boolean enableApmDump) {
        this.enableApmDump = enableApmDump;
    }

    public void setEnableApm3A(boolean enableApm3A) {
        this.enableApm3A = enableApm3A;
    }

    public void setForceExit(boolean forceExit) {
        this.forceExit = forceExit;
    }

    public String getDefaultVadOutFilePath() {
        return this.audioOutFile + "_vad_0.pcm";
    }

    public void start() {
        if (appId == null || token == null) {
            String[] keys = Utils.readAppIdAndToken(".keys");
            appId = keys[0];
            token = keys[1];
            SampleLogger.log("read appId: " + appId + " token: " + token + " from .keys");
        }
        int ret = 0;
        if (service == null) {
            try {
                SampleLogger.log("Creating AgoraService instance");
                // Initialize Agora service globally once
                service = new AgoraService();
                SampleLogger.log("AgoraService instance created");

                AgoraServiceConfig config = new AgoraServiceConfig();
                config.setAppId(appId);
                config.setEnableAudioDevice(0);
                config.setEnableAudioProcessor(1);
                config.setEnableVideo(1);
                config.setUseStringUid(0);
                config.setAudioScenario(Constants.AUDIO_SCENARIO_AI_SERVER);
                config.setLogFilePath(DEFAULT_LOG_PATH);
                config.setLogFileSize(DEFAULT_LOG_SIZE);
                config.setLogFilters(Constants.LOG_FILTER_DEBUG);
                config.setApmMode(Constants.ApmMode.ENABLE);

                SampleLogger.log("About to call service.initialize()");
                ret = service.initialize(config);
                SampleLogger.log("service.initialize() returned: " + ret);

                if (ret != 0) {
                    SampleLogger.log(
                            "createAndInitAgoraService AgoraService.initialize fail ret:" + ret);
                    release();
                    return;
                } else {
                    SampleLogger.log("AgoraService initialized successfully");
                }
            } catch (Throwable e) {
                e.printStackTrace();
                SampleLogger.log("Exception occurred while initializing AgoraService: " + e.getMessage());
                SampleLogger.log("Exception stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
                release();
                return;
            }
        } else {
            SampleLogger.log("AgoraService already exists, reusing it");
        }

        SampleLogger.log("Starting to create " + MAX_TASK_COUNT + " audio processors");
        for (int i = 0; i < MAX_TASK_COUNT; i++) {
            final int taskId = i;
            final String finalTaskAudioOutFile = this.audioOutFile + "_" + taskId + ".pcm";
            final String finalTaskAudioVadOutFile = this.audioOutFile + "_vad_" + taskId + ".pcm";

            File outFile = new File(finalTaskAudioOutFile);
            if (outFile.exists()) {
                outFile.delete();
            }

            File vadOutFile = new File(finalTaskAudioVadOutFile);
            if (vadOutFile.exists()) {
                vadOutFile.delete();
            }

            SampleLogger.log("Creating audio processor for task " + taskId);
            AgoraExternalAudioProcessor audioProcessor = service.createExternalAudioProcessor();
            if (audioProcessor == null) {
                SampleLogger.log("createExternalAudioProcessor failed for task " + taskId);
                continue;
            }
            SampleLogger.log("Audio processor created successfully for task " + taskId);

            audioProcessorList.add(audioProcessor);

            AgoraApmConfig apmConfig = null;
            if (enableApm3A) {
                apmConfig = new AgoraApmConfig();
                // just for test, should be false in production
                apmConfig.setEnableDump(enableApmDump);
            }

            AgoraAudioVadConfigV2 vadConfig = new AgoraAudioVadConfigV2();

            SampleLogger.log("Initializing audio processor for task " + taskId);
            ret = audioProcessor.initialize(apmConfig, sampleRate, numOfChannels, vadConfig,
                    new IExternalAudioProcessorObserver() {
                        private String taskAudioOutFile = finalTaskAudioOutFile;
                        private String taskAudioVadOutFile = finalTaskAudioVadOutFile;

                        @Override
                        public void onAudioFrame(AgoraExternalAudioProcessor processor, AudioFrame audioFrame,
                                VadProcessResult vadProcessResult) {
                            long callbackTimeNs = System.nanoTime();
                            if (lastPushTimeNs > 0) {
                                long latencyUs = (callbackTimeNs - lastPushTimeNs) / 1000;
                                totalLatencyUs += latencyUs;
                                callbackCount++;
                            }
                            SampleLogger.log("onAudioFrame: processor=" + processor + ", audioFrame=" + audioFrame
                                    + ", vadProcessResult=" + vadProcessResult + ", latency="
                                    + (callbackCount > 0 ? (totalLatencyUs / callbackCount) : 0) + "us");

                            if (audioFrame == null) {
                                SampleLogger.log("onAudioFrame: audioFrame is null");
                                return;
                            }

                            byte[] byteArray = io.agora.rtc.utils.Utils.getBytes(audioFrame.getBuffer());
                            if (byteArray == null) {
                                return;
                            }
                            audioWriteExecutor.execute(() -> {
                                Utils.writeBytesToFile(byteArray, taskAudioOutFile);
                            });
                            if (vadProcessResult != null && vadProcessResult.getOutFrame() != null
                                    && vadProcessResult.getOutFrame().length > 0) {
                                audioWriteExecutor.execute(() -> {
                                    Utils.writeBytesToFile(vadProcessResult.getOutFrame(), taskAudioVadOutFile);
                                });
                            }
                        }
                    });

            if (ret != 0) {
                SampleLogger.log("initialize failed for task " + taskId + ", ret: " + ret);
                audioProcessorList.remove(audioProcessor);
                continue;
            }
            SampleLogger.log("Audio processor initialized successfully for task " + taskId);
        }
        SampleLogger.log("Created " + audioProcessorList.size() + " audio processors");

        if (audioProcessorList.isEmpty()) {
            SampleLogger.log("No audio processors initialized, exiting");
            release();
            return;
        }

        // Check if audio file exists
        File audioFile = new File(nearAudioFile);
        if (!audioFile.exists()) {
            SampleLogger.log("Audio file does not exist: " + nearAudioFile);
            SampleLogger.log("Current working directory: " + System.getProperty("user.dir"));
            release();
            return;
        }
        SampleLogger.log("Audio file found: " + nearAudioFile + ", size: " + audioFile.length() + " bytes");

        CountDownLatch taskFinishLatch = new CountDownLatch(1);
        taskExecutor.execute(() -> {
            int bufferSize = sampleRate * numOfChannels * 2 / 1000 * INTERVAL;
            long presentationMs = 0;
            SampleLogger.log("Audio processing thread started, bufferSize: " + bufferSize);
            try (FileInputStream nearFis = new FileInputStream(nearAudioFile)) {
                byte[] nearBuffer = new byte[bufferSize];
                int readLen;

                SampleLogger.log("Starting to read and push audio data from: " + nearAudioFile);
                while ((readLen = nearFis.read(nearBuffer)) != -1) {
                    byte[] dataToPut;
                    if (readLen < bufferSize) {
                        dataToPut = new byte[bufferSize];
                        System.arraycopy(nearBuffer, 0, dataToPut, 0, readLen);
                        // Zero-pad the remaining bytes
                        for (int i = readLen; i < bufferSize; i++) {
                            dataToPut[i] = 0;
                        }
                    } else {
                        dataToPut = nearBuffer;
                    }

                    // Push audio data to all initialized audio processors
                    for (AgoraExternalAudioProcessor audioProcessor : audioProcessorList) {
                        lastPushTimeNs = System.nanoTime();
                        int pusRet = audioProcessor.pushAudioPcmData(dataToPut, sampleRate, numOfChannels,
                                presentationMs);
                        if (pusRet != 0) {
                            SampleLogger.log("pushAudioPcmData failed, ret: " + pusRet);
                        }
                    }

                    presentationMs += INTERVAL;

                    // Sleep to simulate real-time playback
                    try {
                        Thread.sleep(INTERVAL);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                SampleLogger.log("Finished reading and pushing audio data");
                taskFinishLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
                SampleLogger.log("Exception occurred while reading audio file: " + e);
                taskFinishLatch.countDown();
            }
        });

        SampleLogger.log("Waiting for audio processing task to complete...");
        try {
            taskFinishLatch.await();
            SampleLogger.log("Audio processing task completed");
        } catch (InterruptedException e) {
            SampleLogger.log("Task interrupted: " + e.getMessage());
            e.printStackTrace();
        }

        SampleLogger.log("Waiting 1 second for callbacks to complete...");
        try {
            Thread.sleep(1 * 1000);
        } catch (InterruptedException e) {
            SampleLogger.log("Sleep interrupted: " + e.getMessage());
            e.printStackTrace();
        }

        // avgLatency = totalLatency / callbackCount
        if (callbackCount > 0) {
            long avgLatencyUs = totalLatencyUs / callbackCount;
            SampleLogger.log("===== Frame Stats: callbackCount=" + callbackCount
                    + ", totalLatency=" + totalLatencyUs + "us"
                    + ", avgLatency=" + avgLatencyUs + "us =====");
        }

        SampleLogger.log("Starting cleanup...");
        release();
        SampleLogger.log("Cleanup completed, exiting");
        if (forceExit) {
            System.exit(0);
        }
    }

    private void release() {
        for (AgoraExternalAudioProcessor audioProcessor : audioProcessorList) {
            audioProcessor.destroy();
            audioProcessor = null;
        }
        audioProcessorList.clear();

        if (service != null) {
            service.destroy();
            service = null;
        }

        taskExecutor.shutdown();
        audioWriteExecutor.shutdown();

        SampleLogger.log("release");
    }
}
