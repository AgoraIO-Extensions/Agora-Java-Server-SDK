package io.agora.rtc.example.controller;

import io.agora.rtc.AgoraService;
import io.agora.rtc.Constants;
import io.agora.rtc.example.basic.ExternalAudioProcessorTest;
import io.agora.rtc.example.common.AgoraTaskControl;
import io.agora.rtc.example.common.AgoraTaskManager;
import io.agora.rtc.example.common.ArgsConfig;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.common.TaskLauncher;
import io.agora.rtc.example.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api/server")
public class ServerController implements DisposableBean, ApplicationContextAware {
    private ApplicationContext applicationContext;

    // AgoraTaskManager is used to manage the Agora tasks
    private AgoraTaskManager agoraTaskManager;

    // SseEmitter for current request - will be recreated for each request
    private SseEmitter sseEmitter;

    private CountDownLatch taskFinishLatch;

    private final ExecutorService taskExecutorService = Executors.newCachedThreadPool();

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private AgoraTaskManager.AgoraTaskListener agoraTaskListener = new AgoraTaskManager.AgoraTaskListener() {
        @Override
        public void onTaskStart(
                String taskName, String channelId, String userId, String configFileName) {
            sendSseEvent(
                    String.format("Task started: %s on channel: %s with user: %s and config: %s",
                            taskName, channelId, userId, configFileName));
        }

        @Override
        public void onConnected(String channelId, String userId) {
            sendSseEvent(
                    String.format("Connected to channel: %s with user: %s", channelId, userId));
        }

        @Override
        public void onTestFinished(String channelId, String userId) {
            sendSseEvent(
                    String.format("Test finished on channel: %s with user: %s", channelId, userId));
        }

        @Override
        public void onAllTaskFinished() {
            sendSseEvent("Test tasks finished");
            if (taskFinishLatch != null) {
                taskFinishLatch.countDown();
            }
        }
    };

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.agoraTaskManager = new AgoraTaskManager(agoraTaskListener);
        // Initialize the ThreadLocal SseEmitter
        // currentSseEmitter.set(new SseEmitter(Long.MAX_VALUE)); // Removed as per edit
        // hint
    }

    public ServerController() {
        log.info("ServerController initialized successfully.");
    }

    @GetMapping(value = "/start")
    public SseEmitter startServer(@RequestParam(required = false) String configFileName) {
        log.info("=== START SSE SERVER REQUEST ===");
        log.info("Received configFileName parameter: {}", configFileName);

        // Create a new SseEmitter for each request
        sseEmitter = new SseEmitter(Long.MAX_VALUE);

        sendSseEvent("SSE connection established, starting tasks...");

        // return sseEmitter immediately to client to avoid timeout
        taskExecutorService.execute(() -> {
            try {
                if (configFileName == null || configFileName.trim().isEmpty()) {
                    log.info("ConfigFileName is empty, will start recording for all config files "
                            + "via SSE");
                    startAllServersTasks(sseEmitter);
                } else {
                    log.info("ConfigFileName provided: {}, starting single recording via SSE",
                            configFileName);
                    startSingleServerSse(configFileName.trim(), sseEmitter);
                }
                sseEmitter.complete();
            } catch (Exception e) {
                log.error("Error in SSE task for startRecording", e);
                try {
                    sendSseEvent("Error in SSE task: " + e.getMessage());
                } catch (Exception ex) {
                    log.warn("Failed to send SSE error message to client: {}", ex.getMessage());
                }
                sseEmitter.completeWithError(e);
            }
        });

        log.info("SseEmitter returned to client for /start endpoint.");
        return sseEmitter;
    }

    @GetMapping(value = "/basic")
    public SseEmitter startBasicServer(String taskName) {
        log.info("=== START SSE SERVER REQUEST ===");
        log.info("Received taskName parameter: {}", taskName);

        // Create a new SseEmitter for each request
        sseEmitter = new SseEmitter(Long.MAX_VALUE);

        sendSseEvent("SSE connection established, starting tasks...");

        // return sseEmitter immediately to client to avoid timeout
        taskExecutorService.execute(() -> {
            try {
                // Use TaskLauncher to start the basic task
                boolean success = TaskLauncher.startBasicTask(taskName);
                if (!success) {
                    sendSseEvent("Failed to start basic task: " + taskName);
                }

                sseEmitter.complete();
            } catch (Exception e) {
                log.error("Error in SSE task for startBasicServer", e);
                try {
                    sendSseEvent("Error in SSE task: " + e.getMessage());
                } catch (Exception ex) {
                    log.warn("Failed to send SSE error message to client: {}", ex.getMessage());
                }
                sseEmitter.completeWithError(e);
            }
        });

        log.info("SseEmitter returned to client for /basic endpoint.");
        return sseEmitter;
    }

    private void startAllServersTasks(SseEmitter sseEmitter) {
        sendSseEvent("--- Starting all servers via SSE ---", sseEmitter);

        long startTime = System.currentTimeMillis();
        try {
            if (!testBasicTask(sseEmitter)) {
                sendSseEvent("Error: test basic task failed", sseEmitter);
                return;
            }

            Thread.sleep(5 * 1000);

            if (!testAiPcmTask(sseEmitter)) {
                sendSseEvent("Error: test ai pcm task failed", sseEmitter);
                return;
            }

            Thread.sleep(5 * 1000);

            if (!testPcmTask(sseEmitter)) {
                sendSseEvent("Error: test pcm task failed", sseEmitter);
                return;
            }

            Thread.sleep(5 * 1000);

            if (!testEncodedAudioTask(sseEmitter)) {
                sendSseEvent("Error: test encoded audio task failed", sseEmitter);
                return;
            }

            Thread.sleep(5 * 1000);

            if (!testMixedAudioTask(sseEmitter)) {
                sendSseEvent("Error: test mixed audio task failed", sseEmitter);
                return;
            }

            Thread.sleep(5 * 1000);

            if (!testYuvTask(sseEmitter)) {
                sendSseEvent("Error: test yuv task failed", sseEmitter);
                return;
            }

            Thread.sleep(5 * 1000);

            if (!testH264Task(sseEmitter)) {
                sendSseEvent("Error: test h264 task failed", sseEmitter);
                return;
            }

            Thread.sleep(5 * 1000);

            if (!testAgoraParameterTask(sseEmitter)) {
                sendSseEvent("Error: test agora parameter task failed", sseEmitter);
                return;
            }

            Thread.sleep(5 * 1000);

            if (!testDataStreamTask(sseEmitter)) {
                sendSseEvent("Error: test data stream task failed", sseEmitter);
                return;
            }

            Thread.sleep(5 * 1000);

            if (!testRgbaTask(sseEmitter)) {
                sendSseEvent("Error: test rgba task failed", sseEmitter);
                return;
            }

            Thread.sleep(5 * 1000);

            if (!testVp8Task(sseEmitter)) {
                sendSseEvent("Error: test vp8 task failed", sseEmitter);
                return;
            }

            Thread.sleep(5 * 1000);

            if (!testH265Task(sseEmitter)) {
                sendSseEvent("Error: test h265 task failed", sseEmitter);
                return;
            }

            Thread.sleep(5 * 1000);

            if (!testApmPcmTask(sseEmitter)) {
                sendSseEvent("Error: test apm pcm task failed", sseEmitter);
                return;
            }

            Thread.sleep(5 * 1000);

            long endTime = System.currentTimeMillis();
            long timeCostMs = endTime - startTime;
            double timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("All tasks finished, time cost: " + String.format("%.2f", timeCostMinutes)
                    + " minutes (" + timeCostMs + "ms) and releasing AgoraService resources...",
                    sseEmitter);

            agoraTaskManager.cleanup();
            sendSseEvent("AgoraService resources released successfully.", sseEmitter);
            destroyApplication();
        } catch (Exception e) {
            log.error("Error in SSE task for startAllServersTasks", e);
            sendSseEvent(
                    "Error in SSE task for startAllServersTasks: " + e.getMessage(), sseEmitter);
        }
    }

    private boolean testPcmTask(SseEmitter sseEmitter) {
        try {
            agoraTaskManager.releaseAgoraService();

            long startTime = System.currentTimeMillis();
            taskFinishLatch = new CountDownLatch(1);

            String recvConfigFileName = "pcm_recv.json";
            ArgsConfig recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            recvArgsConfig.setAutoTest(true);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM);

            String sendConfigFileName = "pcm_send.json";
            ArgsConfig sendArgsConfig = agoraTaskManager.parseArgsConfig(sendConfigFileName);
            agoraTaskManager.startTask(
                    sendConfigFileName, sendArgsConfig, AgoraTaskControl.TestTask.SEND_PCM);

            taskFinishLatch.await();
            if (!Utils.checkFileExists(recvArgsConfig.getAudioOutFile())) {
                sendSseEvent("Error: recv pcm audioOutFile does not exist: "
                        + recvArgsConfig.getAudioOutFile(),
                        sseEmitter);
                return false;
            }

            if (!Utils.checkFileExists(recvArgsConfig.getAudioOutFile() + "_pts.txt")) {
                sendSseEvent("Error: recv pcm pts file does not exist: "
                        + recvArgsConfig.getAudioOutFile() + "_pts.txt",
                        sseEmitter);
                return false;
            }

            long endTime = System.currentTimeMillis();
            long timeCostMs = endTime - startTime;
            double timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("recv and send pcm task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);

            Thread.sleep(5 * 1000);

            agoraTaskManager.releaseAgoraService();

            taskFinishLatch = new CountDownLatch(1);
            startTime = System.currentTimeMillis();

            sendConfigFileName = "pcm_send_ai_with_pts.json";
            sendArgsConfig = agoraTaskManager.parseArgsConfig(sendConfigFileName);
            agoraTaskManager.startTask(
                    sendConfigFileName, sendArgsConfig, AgoraTaskControl.TestTask.SEND_PCM_AI_WITH_PTS);

            taskFinishLatch.await();

            endTime = System.currentTimeMillis();
            timeCostMs = endTime - startTime;
            timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("send pcm with pts task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);

            Thread.sleep(5 * 1000);

            taskFinishLatch = new CountDownLatch(1);
            startTime = System.currentTimeMillis();

            recvConfigFileName = "pcm_recv_ai_with_pts.json";
            recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM_AI_WITH_PTS);

            taskFinishLatch.await();

            endTime = System.currentTimeMillis();
            timeCostMs = endTime - startTime;
            timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("send pcm with pts task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);

            Thread.sleep(5 * 1000);

            taskFinishLatch = new CountDownLatch(1);
            startTime = System.currentTimeMillis();

            recvConfigFileName = "pcm_recv_encrypted.json";
            recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM);

            sendConfigFileName = "pcm_send_encrypted.json";
            sendArgsConfig = agoraTaskManager.parseArgsConfig(sendConfigFileName);
            agoraTaskManager.startTask(
                    sendConfigFileName, sendArgsConfig, AgoraTaskControl.TestTask.SEND_PCM);

            taskFinishLatch.await();
            if (!Utils.checkFileExists(recvArgsConfig.getAudioOutFile())) {
                sendSseEvent("Error: recv pcm with encrypted audioOutFile does not exist: "
                        + recvArgsConfig.getAudioOutFile(),
                        sseEmitter);
                return false;
            }
            endTime = System.currentTimeMillis();
            timeCostMs = endTime - startTime;
            timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent(
                    "recv and send encrypted pcm task finished, time cost: " + String.format("%.2f", timeCostMinutes)
                            + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);

            Thread.sleep(5 * 1000);

            agoraTaskManager.releaseAgoraService();

            taskFinishLatch = new CountDownLatch(1);
            startTime = System.currentTimeMillis();

            recvConfigFileName = "pcm_recv_with_remote_user_id.json";
            recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM);

            sendConfigFileName = "pcm_send_with_remote_user_id.json";
            sendArgsConfig = agoraTaskManager.parseArgsConfig(sendConfigFileName);
            agoraTaskManager.startTask(
                    sendConfigFileName, sendArgsConfig, AgoraTaskControl.TestTask.SEND_PCM);

            taskFinishLatch.await();
            if (!Utils.checkFileExists(recvArgsConfig.getAudioOutFile())) {
                sendSseEvent("Error: recv pcm with remote user id audioOutFile does not exist: "
                        + recvArgsConfig.getAudioOutFile(), sseEmitter);
                return false;
            }

            if (recvArgsConfig.isEnableVad()) {
                if (!Utils.checkFileExists(recvArgsConfig.getAudioOutFile(), "_vad.pcm")) {
                    sendSseEvent("Error: recv pcm with remote user id audioOutFile vad pcm does not exist: "
                            + recvArgsConfig.getAudioOutFile(), sseEmitter);
                    return false;
                }
            }

            if (sendArgsConfig.isEnableSendAudioMetaData()) {
                if (!Utils.checkFileExists(
                        recvArgsConfig.getAudioOutFile(), "_audio_meta_data.txt")) {
                    sendSseEvent(
                            "Error: recv pcm with remote user id audio meta data file does not exist: "
                                    + recvArgsConfig.getAudioOutFile(),
                            sseEmitter);
                    return false;
                }
            }

            endTime = System.currentTimeMillis();
            timeCostMs = endTime - startTime;
            timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("recv and send pcm with remote user id task finished, time cost:"
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);

            Thread.sleep(5 * 1000);

            agoraTaskManager.releaseAgoraService();
            Utils.deleteAllFilesInDirectory("logs/agora_logs/");

            taskFinishLatch = new CountDownLatch(1);
            startTime = System.currentTimeMillis();

            recvArgsConfig.setAudioOutFile(recvArgsConfig.getAudioOutFile() + "_error");
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM);

            sendArgsConfig.setUserId("0");
            agoraTaskManager.startTask(
                    sendConfigFileName, sendArgsConfig, AgoraTaskControl.TestTask.SEND_PCM);

            taskFinishLatch.await();
            if (Utils.checkFileExists(recvArgsConfig.getAudioOutFile())) {
                sendSseEvent("Error: recv pcm with not exist remote user id audioOutFile exists: "
                        + recvArgsConfig.getAudioOutFile(),
                        sseEmitter);
                return false;
            }

            endTime = System.currentTimeMillis();
            timeCostMs = endTime - startTime;
            timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("recv and send pcm with not exist remote user id task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);

            Thread.sleep(5 * 1000);

            taskFinishLatch = new CountDownLatch(1);
            startTime = System.currentTimeMillis();

            recvConfigFileName = "pcm_recv_by_cloud_proxy.json";
            recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM);

            sendConfigFileName = "pcm_send_by_cloud_proxy.json";
            sendArgsConfig = agoraTaskManager.parseArgsConfig(sendConfigFileName);
            agoraTaskManager.startTask(
                    sendConfigFileName, sendArgsConfig, AgoraTaskControl.TestTask.SEND_PCM);

            taskFinishLatch.await();
            if (!Utils.checkFileExists(recvArgsConfig.getAudioOutFile())) {
                sendSseEvent("Error: recv pcm with cloud proxy audioOutFile does not exist: "
                        + recvArgsConfig.getAudioOutFile(),
                        sseEmitter);
                return false;
            }

            endTime = System.currentTimeMillis();
            timeCostMs = endTime - startTime;
            timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("recv and send pcm with cloud proxy task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);
        } catch (Exception e) {
            log.error("Error in SSE task for testPcmTask", e);
            return false;
        }
        return true;
    }

    private boolean testEncodedAudioTask(SseEmitter sseEmitter) {
        try {
            long startTime = System.currentTimeMillis();
            taskFinishLatch = new CountDownLatch(1);

            String recvConfigFileName = "encoded_audio_recv.json";
            ArgsConfig recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            recvArgsConfig.setAutoTest(true);
            agoraTaskManager.startTask(recvConfigFileName, recvArgsConfig,
                    AgoraTaskControl.TestTask.RECEIVE_ENCODED_AUDIO);

            String sendConfigFileName = "opus_send.json";
            ArgsConfig sendArgsConfig = agoraTaskManager.parseArgsConfig(sendConfigFileName);
            agoraTaskManager.startTask(
                    sendConfigFileName, sendArgsConfig, AgoraTaskControl.TestTask.SEND_OPUS);

            taskFinishLatch.await();

            if (!Utils.checkFileExists(recvArgsConfig.getAudioOutFile(),
                    recvArgsConfig.getFileType(), recvArgsConfig.getExpectedFile())) {
                sendSseEvent("Error: recv encoded audio audioOutFile does not exist: "
                        + recvArgsConfig.getAudioOutFile()
                        + " or not match expectedFile: " + recvArgsConfig.getExpectedFile(),
                        sseEmitter);
                return false;
            }

            if (!Utils.checkFileExists(recvArgsConfig.getAudioOutFile() + "_pts.txt")) {
                sendSseEvent("Error: recv encoded audio pts file does not exist: "
                        + recvArgsConfig.getAudioOutFile() + "_pts.txt",
                        sseEmitter);
                return false;
            }

            long endTime = System.currentTimeMillis();
            long timeCostMs = endTime - startTime;
            double timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("recv and send encoded audio task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);
        } catch (Exception e) {
            log.error("Error in SSE task for testEncodedAudioTask", e);
            return false;
        }
        return true;
    }

    private boolean testMixedAudioTask(SseEmitter sseEmitter) {
        try {
            long startTime = System.currentTimeMillis();
            taskFinishLatch = new CountDownLatch(1);

            String recvConfigFileName = "mixed_audio_recv.json";
            ArgsConfig recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_MIXED_AUDIO);

            String sendConfigFileName = "aac_send.json";
            ArgsConfig sendArgsConfig = agoraTaskManager.parseArgsConfig(sendConfigFileName);
            agoraTaskManager.startTask(
                    sendConfigFileName, sendArgsConfig, AgoraTaskControl.TestTask.SEND_AAC);

            taskFinishLatch.await();

            if (!Utils.checkFileExists(recvArgsConfig.getAudioOutFile())) {
                sendSseEvent("Error: recv mixed audio audioOutFile does not exist: "
                        + recvArgsConfig.getAudioOutFile(),
                        sseEmitter);
                return false;
            }

            long endTime = System.currentTimeMillis();
            long timeCostMs = endTime - startTime;
            double timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("recv and send mixed audio task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);
        } catch (Exception e) {
            log.error("Error in SSE task for testMixedAudioTask", e);
            return false;
        }
        return true;
    }

    private boolean testYuvTask(SseEmitter sseEmitter) {
        try {
            long startTime = System.currentTimeMillis();
            taskFinishLatch = new CountDownLatch(1);

            String recvConfigFileName = "yuv_recv.json";
            ArgsConfig recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            recvArgsConfig.setAutoTest(true);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_YUV);

            String sendConfigFileName = "yuv_send.json";
            ArgsConfig sendArgsConfig = agoraTaskManager.parseArgsConfig(sendConfigFileName);
            agoraTaskManager.startTask(
                    sendConfigFileName, sendArgsConfig, AgoraTaskControl.TestTask.SEND_YUV);

            taskFinishLatch.await();

            if (!Utils.checkFileExists(recvArgsConfig.getVideoOutFile())) {
                sendSseEvent("Error: recv yuv videoOutFile does not exist: "
                        + recvArgsConfig.getVideoOutFile(),
                        sseEmitter);
                return false;
            }

            if (!Utils.checkFileExists(recvArgsConfig.getVideoOutFile() + "_pts.txt")) {
                sendSseEvent("Error: recv yuv pts file does not exist: "
                        + recvArgsConfig.getVideoOutFile() + "_pts.txt",
                        sseEmitter);
                return false;
            }

            long endTime = System.currentTimeMillis();
            long timeCostMs = endTime - startTime;
            double timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("recv and send yuv task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);

            Thread.sleep(5 * 1000);

            taskFinishLatch = new CountDownLatch(1);
            startTime = System.currentTimeMillis();

            recvConfigFileName = "yuv_recv_with_remote_user_id.json";
            recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_YUV);

            sendConfigFileName = "yuv_send_with_remote_user_id.json";
            sendArgsConfig = agoraTaskManager.parseArgsConfig(sendConfigFileName);
            agoraTaskManager.startTask(
                    sendConfigFileName, sendArgsConfig, AgoraTaskControl.TestTask.SEND_YUV);

            taskFinishLatch.await();

            if (!Utils.checkFileExists(recvArgsConfig.getVideoOutFile())) {
                sendSseEvent("Error: recv yuv with remote user id videoOutFile does not exist: "
                        + recvArgsConfig.getVideoOutFile(),
                        sseEmitter);
                return false;
            }

            if (sendArgsConfig.isEnableSendVideoMetaData()) {
                if (!Utils.checkFileExists(recvArgsConfig.getVideoOutFile(), "_metaData.txt")) {
                    sendSseEvent(
                            "Error: recv yuv with remote user id video meta data file does not exist: "
                                    + recvArgsConfig.getVideoOutFile(),
                            sseEmitter);
                    return false;
                }
            }
            endTime = System.currentTimeMillis();
            timeCostMs = endTime - startTime;
            timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("recv and send yuv with remote user id task finished, time cost:"
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);

            Thread.sleep(5 * 1000);

            taskFinishLatch = new CountDownLatch(1);
            startTime = System.currentTimeMillis();

            recvArgsConfig.setVideoOutFile(recvArgsConfig.getVideoOutFile() + "_error");
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_YUV);

            sendArgsConfig.setUserId("0");
            agoraTaskManager.startTask(
                    sendConfigFileName, sendArgsConfig, AgoraTaskControl.TestTask.SEND_YUV);

            taskFinishLatch.await();

            if (Utils.checkFileExists(recvArgsConfig.getVideoOutFile())) {
                sendSseEvent("Error: recv yuv with not exist remote user id videoOutFile exists: "
                        + recvArgsConfig.getVideoOutFile(),
                        sseEmitter);
                return false;
            }

            endTime = System.currentTimeMillis();
            timeCostMs = endTime - startTime;
            timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("recv and send yuv with not exist remote user id task finished,time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);

            Thread.sleep(5 * 1000);

            taskFinishLatch = new CountDownLatch(1);
            startTime = System.currentTimeMillis();

            recvConfigFileName = "yuv_recv_with_alpha.json";
            recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_YUV);

            sendConfigFileName = "yuv_send_with_alpha.json";
            sendArgsConfig = agoraTaskManager.parseArgsConfig(sendConfigFileName);
            agoraTaskManager.startTask(
                    sendConfigFileName, sendArgsConfig, AgoraTaskControl.TestTask.SEND_YUV);

            taskFinishLatch.await();

            if (!Utils.checkFileExists(recvArgsConfig.getVideoOutFile())) {
                sendSseEvent("Error: recv yuv with alpha videoOutFile does not exist: "
                        + recvArgsConfig.getVideoOutFile(),
                        sseEmitter);
                return false;
            }

            if (sendArgsConfig.isEnableSendVideoMetaData()) {
                if (!Utils.checkFileExists(recvArgsConfig.getVideoOutFile(), "_alpha.raw")) {
                    sendSseEvent("Error: recv yuv with alpha video meta data file does not exist:"
                            + recvArgsConfig.getVideoOutFile(),
                            sseEmitter);
                    return false;
                }
            }

            endTime = System.currentTimeMillis();
            timeCostMs = endTime - startTime;
            timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("recv and send yuv with alpha task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);

            Thread.sleep(5 * 1000);

            taskFinishLatch = new CountDownLatch(1);
            startTime = System.currentTimeMillis();

            recvConfigFileName = "yuv_recv_dual_stream_high.json";
            recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_YUV);

            recvConfigFileName = "yuv_recv_dual_stream_low.json";
            ArgsConfig recvArgsConfigLow = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfigLow, AgoraTaskControl.TestTask.RECEIVE_YUV);

            sendConfigFileName = "yuv_send_dual_stream.json";
            sendArgsConfig = agoraTaskManager.parseArgsConfig(sendConfigFileName);
            agoraTaskManager.startTask(
                    sendConfigFileName, sendArgsConfig, AgoraTaskControl.TestTask.SEND_YUV_DUAL_STREAM);

            taskFinishLatch.await();

            if (!Utils.checkFileExists(recvArgsConfig.getVideoOutFile())) {
                sendSseEvent("Error: recv yuv dual stream high videoOutFile does not exist: "
                        + recvArgsConfig.getVideoOutFile(),
                        sseEmitter);
                return false;
            }

            if (!Utils.checkFileExists(recvArgsConfigLow.getVideoOutFile())) {
                sendSseEvent("Error: recv yuv dual stream low videoOutFile does not exist: "
                        + recvArgsConfigLow.getVideoOutFile(),
                        sseEmitter);
                return false;
            }

            endTime = System.currentTimeMillis();
            timeCostMs = endTime - startTime;
            timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("recv and send yuv dual stream task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);
        } catch (Exception e) {
            sendSseEvent("Error: test yuv task failed: " + e.getMessage(), sseEmitter);
            return false;
        }
        return true;
    }

    private boolean testH264Task(SseEmitter sseEmitter) {
        try {
            long startTime = System.currentTimeMillis();
            taskFinishLatch = new CountDownLatch(1);

            String recvConfigFileName = "h264_recv.json";
            ArgsConfig recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_H264);

            String sendConfigFileName = "h264_send.json";
            ArgsConfig sendArgsConfig = agoraTaskManager.parseArgsConfig(sendConfigFileName);
            agoraTaskManager.startTask(
                    sendConfigFileName, sendArgsConfig, AgoraTaskControl.TestTask.SEND_H264);

            taskFinishLatch.await();

            if (!Utils.checkFileExists(recvArgsConfig.getVideoOutFile())) {
                sendSseEvent("Error: recv h264 videoOutFile does not exist: "
                        + recvArgsConfig.getVideoOutFile(),
                        sseEmitter);
                return false;
            }

            long endTime = System.currentTimeMillis();
            long timeCostMs = endTime - startTime;
            double timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("recv and send h264 task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);

            Thread.sleep(5 * 1000);

            taskFinishLatch = new CountDownLatch(1);
            startTime = System.currentTimeMillis();

            recvConfigFileName = "h264_recv_with_remote_user_id.json";
            recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            recvArgsConfig.setAutoTest(true);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_H264);

            sendConfigFileName = "h264_send_with_remote_user_id.json";
            sendArgsConfig = agoraTaskManager.parseArgsConfig(sendConfigFileName);
            agoraTaskManager.startTask(
                    sendConfigFileName, sendArgsConfig, AgoraTaskControl.TestTask.SEND_H264);

            taskFinishLatch.await();

            if (!Utils.checkFileExists(recvArgsConfig.getVideoOutFile())) {
                sendSseEvent("Error: recv h264 with remote user id videoOutFile does not exist: "
                        + recvArgsConfig.getVideoOutFile(),
                        sseEmitter);
                return false;
            }

            if (!Utils.checkFileExists(recvArgsConfig.getVideoOutFile() + "_pts.txt")) {
                sendSseEvent("Error: recv h264 with remote user id pts file does not exist: "
                        + recvArgsConfig.getVideoOutFile() + "_pts.txt",
                        sseEmitter);
                return false;
            }

            endTime = System.currentTimeMillis();
            timeCostMs = endTime - startTime;
            timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("recv and send h264 with remote user id task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);

            Thread.sleep(5 * 1000);

            taskFinishLatch = new CountDownLatch(1);
            startTime = System.currentTimeMillis();

            recvArgsConfig.setVideoOutFile(recvArgsConfig.getVideoOutFile() + "_error");
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_H264);

            sendArgsConfig.setUserId("0");
            agoraTaskManager.startTask(
                    sendConfigFileName, sendArgsConfig, AgoraTaskControl.TestTask.SEND_H264);

            taskFinishLatch.await();

            if (Utils.checkFileExists(recvArgsConfig.getVideoOutFile())) {
                sendSseEvent("Error: recv h264 with not exist remote user id videoOutFile exists: "
                        + recvArgsConfig.getVideoOutFile(),
                        sseEmitter);
                return false;
            }

            endTime = System.currentTimeMillis();
            timeCostMs = endTime - startTime;
            timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent(
                    "recv and send h264 with not exist remote user id task finished, time cost: "
                            + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);

            Thread.sleep(5 * 1000);

            taskFinishLatch = new CountDownLatch(1);
            startTime = System.currentTimeMillis();

            recvConfigFileName = "h264_recv_encrypted.json";
            recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_H264);

            sendConfigFileName = "h264_send_encrypted.json";
            sendArgsConfig = agoraTaskManager.parseArgsConfig(sendConfigFileName);
            agoraTaskManager.startTask(
                    sendConfigFileName, sendArgsConfig, AgoraTaskControl.TestTask.SEND_H264);

            taskFinishLatch.await();

            if (!Utils.checkFileExists(recvArgsConfig.getVideoOutFile())) {
                sendSseEvent("Error: recv h264 encrypted videoOutFile does not exist: "
                        + recvArgsConfig.getVideoOutFile(),
                        sseEmitter);
                return false;
            }

            endTime = System.currentTimeMillis();
            timeCostMs = endTime - startTime;
            timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("recv and send h264 encrypted task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);

            Thread.sleep(5 * 1000);

            taskFinishLatch = new CountDownLatch(1);
            startTime = System.currentTimeMillis();

            recvConfigFileName = "h264_recv_dual_stream_high.json";
            recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_H264);

            recvConfigFileName = "h264_recv_dual_stream_low.json";
            ArgsConfig recvArgsConfigLow = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfigLow, AgoraTaskControl.TestTask.RECEIVE_H264);

            sendConfigFileName = "h264_send_dual_stream.json";
            sendArgsConfig = agoraTaskManager.parseArgsConfig(sendConfigFileName);
            agoraTaskManager.startTask(sendConfigFileName, sendArgsConfig,
                    AgoraTaskControl.TestTask.SEND_H264_DUAL_STREAM);

            taskFinishLatch.await();

            if (!Utils.checkFileExists(recvArgsConfig.getVideoOutFile())) {
                sendSseEvent("Error: recv h264 dual stream high videoOutFile does not exist: "
                        + recvArgsConfig.getVideoOutFile(),
                        sseEmitter);
                return false;
            }

            if (!Utils.checkFileExists(recvArgsConfigLow.getVideoOutFile())) {
                sendSseEvent("Error: recv h264 dual stream low videoOutFile does not exist: "
                        + recvArgsConfigLow.getVideoOutFile(),
                        sseEmitter);
                return false;
            }

            endTime = System.currentTimeMillis();
            timeCostMs = endTime - startTime;
            timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("recv and send h264 dual stream task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);
        } catch (Exception e) {
            log.error("Error in SSE task for testH264Task", e);
            return false;
        }
        return true;
    }

    private boolean testAgoraParameterTask(SseEmitter sseEmitter) {
        try {
            long startTime = System.currentTimeMillis();
            taskFinishLatch = new CountDownLatch(1);

            String configFileName = "agora_parameter_test.json";
            ArgsConfig argsConfig = agoraTaskManager.parseArgsConfig(configFileName);
            agoraTaskManager.testAgoraParameterTask(configFileName, argsConfig);

            taskFinishLatch.await();

            long endTime = System.currentTimeMillis();
            long timeCostMs = endTime - startTime;
            double timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("test agora parameter task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);
        } catch (Exception e) {
            sendSseEvent("Error: test agora parameter task failed: " + e.getMessage(), sseEmitter);
            return false;
        }
        return true;
    }

    private boolean testDataStreamTask(SseEmitter sseEmitter) {
        try {
            long startTime = System.currentTimeMillis();
            taskFinishLatch = new CountDownLatch(1);

            String configFileName = "data_stream_send.json";
            ArgsConfig argsConfig = agoraTaskManager.parseArgsConfig(configFileName);
            agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_DATA_STREAM);

            String recvConfigFileName = "data_stream_recv.json";
            ArgsConfig recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_DATA_STREAM);

            taskFinishLatch.await();

            if (!Utils.checkFileExists(recvArgsConfig.getAudioOutFile())) {
                sendSseEvent("Error: recv data stream audioOutFile does not exist: "
                        + recvArgsConfig.getAudioOutFile(),
                        sseEmitter);
                return false;
            }

            long endTime = System.currentTimeMillis();
            long timeCostMs = endTime - startTime;
            double timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("test data stream recv task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);
        } catch (Exception e) {
            sendSseEvent("Error: test data stream task failed: " + e.getMessage(), sseEmitter);
            return false;
        }
        return true;
    }

    private boolean testRgbaTask(SseEmitter sseEmitter) {
        try {
            long startTime = System.currentTimeMillis();
            taskFinishLatch = new CountDownLatch(1);

            String configFileName = "rgba_send.json";
            ArgsConfig argsConfig = agoraTaskManager.parseArgsConfig(configFileName);
            agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_RGBA_PCM);

            String recvConfigFileName = "rgba_recv.json";
            ArgsConfig recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_H264);

            taskFinishLatch.await();

            if (!Utils.checkFileExists(recvArgsConfig.getVideoOutFile())) {
                sendSseEvent("Error: recv rgba videoOutFile does not exist: "
                        + recvArgsConfig.getVideoOutFile(),
                        sseEmitter);
                return false;
            }

            long endTime = System.currentTimeMillis();
            long timeCostMs = endTime - startTime;
            double timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("test rgba task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);
        } catch (Exception e) {
            sendSseEvent("Error: test rgba task failed: " + e.getMessage(), sseEmitter);
            return false;
        }
        return true;
    }

    private boolean testVp8Task(SseEmitter sseEmitter) {
        try {
            long startTime = System.currentTimeMillis();
            taskFinishLatch = new CountDownLatch(1);

            String configFileName = "vp8_send.json";
            ArgsConfig argsConfig = agoraTaskManager.parseArgsConfig(configFileName);
            agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_VP8);

            String recvConfigFileName = "vp8_recv.json";
            ArgsConfig recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_YUV);

            taskFinishLatch.await();

            if (!Utils.checkFileExists(recvArgsConfig.getVideoOutFile())) {
                sendSseEvent("Error: recv vp8 videoOutFile does not exist: "
                        + recvArgsConfig.getVideoOutFile(),
                        sseEmitter);
                return false;
            }

            long endTime = System.currentTimeMillis();
            long timeCostMs = endTime - startTime;
            double timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("Test vp8 task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);
        } catch (Exception e) {
            sendSseEvent("Error: test vp8 task failed: " + e.getMessage(), sseEmitter);
            return false;
        }
        return true;
    }

    private boolean testBasicTask(SseEmitter sseEmitter) {
        try {
            String version = AgoraService.getSdkVersion();
            if (version == null || version.equalsIgnoreCase("Unknown") || version.isEmpty()) {
                sendSseEvent("Error: test getSdkVersion failed: version is " + version, sseEmitter);
                return false;
            } else {
                sendSseEvent("Test getSdkVersion finished, version: " + version, sseEmitter);
            }

            sendSseEvent("Test basic task finished", sseEmitter);
            return true;
        } catch (Exception e) {
            sendSseEvent("Error: test getSdkVersion failed: " + e.getMessage(), sseEmitter);
            return false;
        }
    }

    private boolean testAiPcmTask(SseEmitter sseEmitter) {
        try {
            long startTime = System.currentTimeMillis();
            taskFinishLatch = new CountDownLatch(1);

            String configFileName = "ai_pcm_send.json";
            ArgsConfig argsConfig = agoraTaskManager.parseArgsConfig(configFileName);
            agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_PCM_AI);

            String recvConfigFileName = "ai_pcm_recv.json";
            ArgsConfig recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            recvArgsConfig.setAutoTest(true);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM_AI);

            taskFinishLatch.await();

            if (!Utils.checkFileExists(recvArgsConfig.getAudioOutFile())) {
                sendSseEvent("Error: recv ai pcm audioOutFile does not exist: "
                        + recvArgsConfig.getAudioOutFile(),
                        sseEmitter);
                return false;
            }

            List<File> files = Utils.getFilesInDirectory(recvArgsConfig.getAudioOutFile(), ".pcm");
            if (files.size() != 1) {
                sendSseEvent("Error: recv ai pcm audioOutFile does not exist: "
                        + recvArgsConfig.getAudioOutFile(),
                        sseEmitter);
                return false;
            }
            File sendFile = new File(argsConfig.getAudioFile());
            String recvAudioBytes = Utils
                    .readStringFromFile(recvArgsConfig.getAudioOutFile() + "_receiveAudioBytes.txt");
            if (recvAudioBytes == null || recvAudioBytes.isEmpty()) {
                sendSseEvent("Error: recv ai pcm receiveAudioBytes file does not exist: "
                        + recvArgsConfig.getAudioOutFile() + "_receiveAudioBytes.txt",
                        sseEmitter);
                return false;
            }
            int recvAudioBytesInt = Integer.parseInt(recvAudioBytes);
            File recvFile = files.get(0);
            SampleLogger.log("sendFile.length():" + sendFile.length() + " recvFile.length():" + recvFile.length()
                    + " recvAudioBytesInt:" + recvAudioBytesInt);
            int bytesPerMs = argsConfig.getNumOfChannels() * (argsConfig.getSampleRate() / 1000) * 2;
            if (sendFile.exists() && recvFile.exists()
                    && sendFile.length() - recvAudioBytesInt > 10 * bytesPerMs) {
                sendSseEvent(
                        "Error: recv ai pcm audioOutFile length does not correct,send file length: "
                                + sendFile.length() + " recv file length: " + recvFile.length()
                                + " diff: " + (recvFile.length() - sendFile.length())
                                + " bytes and more than 10ms(" + 10 * bytesPerMs + " bytes)",
                        sseEmitter);
                return false;
            }

            long endTime = System.currentTimeMillis();
            long timeCostMs = endTime - startTime;
            double timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("test ai pcm task send recv finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);

            Thread.sleep(5 * 1000);

            taskFinishLatch = new CountDownLatch(1);
            startTime = System.currentTimeMillis();

            configFileName = "ai_pcm_send.json";
            argsConfig = agoraTaskManager.parseArgsConfig(configFileName);
            agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_PCM_AI);

            recvConfigFileName = "ai_pcm_recv.json";
            recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            recvArgsConfig.setAudioScenario(Constants.AUDIO_SCENARIO_CHORUS);
            recvArgsConfig.setAudioOutFile(recvArgsConfig.getAudioOutFile() + "_chorus");
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM_AI);

            taskFinishLatch.await();

            if (!Utils.checkFileExists(recvArgsConfig.getAudioOutFile())) {
                sendSseEvent("Error: recv ai pcm with chorus audioOutFile does not exist: "
                        + recvArgsConfig.getAudioOutFile(),
                        sseEmitter);
                return false;
            }

            files = Utils.getFilesInDirectory(recvArgsConfig.getAudioOutFile(), ".pcm");
            if (files.size() != 1) {
                sendSseEvent("Error: recv ai pcm audioOutFile does not exist: "
                        + recvArgsConfig.getAudioOutFile(),
                        sseEmitter);
                return false;
            }
            recvFile = files.get(0);

            if (recvFile.exists()
                    && recvFile.length() > (argsConfig.getTestTime() * 1000 + 20) * bytesPerMs) {
                sendSseEvent(
                        "Error: recv ai pcm with chorus audioOutFile length is more than test time: "
                                + recvArgsConfig.getAudioOutFile()
                                + " test time: " + (argsConfig.getTestTime() * 1000 + 20) + "ms"
                                + " recv file length: " + recvFile.length()
                                + " bytesPerMs: " + bytesPerMs,
                        sseEmitter);
                return false;
            }

            endTime = System.currentTimeMillis();
            timeCostMs = endTime - startTime;
            timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("test ai pcm task send recv chorus finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);

            Thread.sleep(5 * 1000);

            taskFinishLatch = new CountDownLatch(1);
            startTime = System.currentTimeMillis();

            recvConfigFileName = "ai_pcm_recv.json";
            recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            recvArgsConfig.setAudioOutFile(recvArgsConfig.getAudioOutFile() + "_fallback");
            recvArgsConfig.setEnableLog(false);
            agoraTaskManager.startTask(
                    recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM_AI);

            sendSseEvent("Please join channel:" + recvArgsConfig.getChannelId()
                    + " with webrtc client and wait for a moment and leave the channel",
                    sseEmitter);

            taskFinishLatch.await();

            if (!Utils.checkFileExists(recvArgsConfig.getAudioOutFile())) {
                sendSseEvent("Error: recv ai pcm with fallback audioOutFile does not exist: "
                        + recvArgsConfig.getAudioOutFile(),
                        sseEmitter);
                return false;
            }

            if (!Utils.checkFileExists(
                    recvArgsConfig.getAudioOutFile(), "_ai_qos_capability_missing.txt")) {
                sendSseEvent("Error: recv ai pcm with fallback audioOutFile does not exist: "
                        + recvArgsConfig.getAudioOutFile() + "_ai_qos_capability_missing.txt",
                        sseEmitter);
                return false;
            }

            endTime = System.currentTimeMillis();
            timeCostMs = endTime - startTime;
            timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("test ai pcm task send recv fallback finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);

            taskFinishLatch = new CountDownLatch(1);
            startTime = System.currentTimeMillis();

            configFileName = "ai_pcm_send_incremental.json";
            argsConfig = agoraTaskManager.parseArgsConfig(configFileName);
            agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_PCM_INCREMENTAL_MODE);

            taskFinishLatch.await();

            endTime = System.currentTimeMillis();
            timeCostMs = endTime - startTime;
            timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("test ai pcm task with send incremental mode finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);
        } catch (Exception e) {
            sendSseEvent("Error: test ai pcm task failed: " + e.getMessage(), sseEmitter);
            return false;
        }
        return true;
    }

    private boolean testH265Task(SseEmitter sseEmitter) {
        try {
            long startTime = System.currentTimeMillis();
            taskFinishLatch = new CountDownLatch(1);

            String configFileName = "h265_send.json";
            ArgsConfig argsConfig = agoraTaskManager.parseArgsConfig(configFileName);
            agoraTaskManager.startTask(configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_H265);

            String recvConfigFileName = "h265_recv.json";
            ArgsConfig recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            agoraTaskManager.startTask(recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_H265);

            taskFinishLatch.await();

            if (!Utils.checkFileExists(recvArgsConfig.getVideoOutFile())) {
                sendSseEvent("Error: recv h265 videoOutFile does not exist: "
                        + recvArgsConfig.getVideoOutFile(),
                        sseEmitter);
                return false;
            }

            long endTime = System.currentTimeMillis();
            long timeCostMs = endTime - startTime;
            double timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("Test h265 task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);
            return true;
        } catch (Exception e) {
            sendSseEvent("Error: test h265 task failed: " + e.getMessage(), sseEmitter);
            return false;
        }
    }

    private boolean testApmPcmTask(SseEmitter sseEmitter) {
        try {
            String basePath = "logs/agora_logs/";
            Utils.deleteAllFilesInDirectory(basePath);

            agoraTaskManager.releaseAgoraService();

            long startTime = System.currentTimeMillis();
            taskFinishLatch = new CountDownLatch(1);

            String recvConfigFileName = "apm_pcm_recv.json";
            ArgsConfig recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
            agoraTaskManager.startTask(recvConfigFileName, recvArgsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM);

            String configFileName = "apm_pcm_send.json";
            ArgsConfig argsConfig = agoraTaskManager.parseArgsConfig(configFileName);
            agoraTaskManager.startTask(configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_PCM);

            taskFinishLatch.await();

            String[] apmDumpFiles = new String[] { "af_agc_", "af_ed_", "af_ns_", "af_ps_", "af_sfnlp_" };

            for (String apmDumpFile : apmDumpFiles) {
                if (!Utils.checkFileExists(basePath + apmDumpFile)) {
                    sendSseEvent("Error: apm dump file does not exist: " + basePath + apmDumpFile,
                            sseEmitter);
                    return false;
                }
            }

            List<String> apmVadDumpFiles = Utils.findFilePathsRecursively(basePath + "vad_dump", 2, "vad_session",
                    ".pcm");
            SampleLogger.log("apmVadDumpFiles: " + apmVadDumpFiles.toString());
            if (apmVadDumpFiles.size() != 2) {
                sendSseEvent("Error: apm vad dump file count is not 2: " + apmVadDumpFiles.size(),
                        sseEmitter);
                return false;
            }

            sendSseEvent("Test remote APM Pass, waiting for 5 seconds...", sseEmitter);
            Thread.sleep(5 * 1000);

            Utils.deleteAllFilesInDirectory(basePath);

            agoraTaskManager.releaseAgoraService();

            ExternalAudioProcessorTest externalAudioProcessorTest = new ExternalAudioProcessorTest();
            externalAudioProcessorTest.setEnableApmDump(false);
            externalAudioProcessorTest.setEnableApm3A(false);
            externalAudioProcessorTest.setForceExit(false);
            externalAudioProcessorTest.start();

            File expectedFile = new File("test_data/vad_test_16k_1ch_expected.pcm");
            File outFile = new File(externalAudioProcessorTest.getDefaultVadOutFilePath());
            if (expectedFile.exists() && outFile.exists()
                    && Utils.areFilesIdentical(expectedFile.getAbsolutePath(), outFile.getAbsolutePath())) {
                sendSseEvent("Test remote APM Pass, vad out file is correct", sseEmitter);
            } else {
                sendSseEvent("Test remote APM Pass, vad out file is incorrect", sseEmitter);
                return false;
            }

            externalAudioProcessorTest = new ExternalAudioProcessorTest();
            externalAudioProcessorTest.setEnableApmDump(true);
            externalAudioProcessorTest.setEnableApm3A(true);
            externalAudioProcessorTest.setForceExit(false);
            externalAudioProcessorTest.start();

            expectedFile = new File("test_data/vad_test_16k_1ch_3A_expected.pcm");
            File expectedFile2 = new File("test_data/vad_test_16k_1ch_3A_expected_2.pcm");
            outFile = new File(externalAudioProcessorTest.getDefaultVadOutFilePath());
            if (expectedFile.exists() && outFile.exists()
                    && (Utils.areFilesIdentical(expectedFile.getAbsolutePath(), outFile.getAbsolutePath())
                            || Utils.areFilesIdentical(expectedFile2.getAbsolutePath(), outFile.getAbsolutePath()))) {
                sendSseEvent("Test remote APM Pass with 3A, vad out file is correct", sseEmitter);
            } else {
                sendSseEvent("Test remote APM Pass with 3A, vad out file is incorrect", sseEmitter);
                return false;
            }

            for (String apmDumpFile : apmDumpFiles) {
                if (!Utils.checkFileExists(basePath + apmDumpFile)) {
                    sendSseEvent("Error: apm dump file does not exist with 3A: " + basePath + apmDumpFile,
                            sseEmitter);
                    return false;
                }
            }

            List<String> apmVadDumpFiles3A = Utils.findFilePathsRecursively(basePath + "vad_dump", 2, "vad_session",
                    ".pcm");
            SampleLogger.log("apmVadDumpFiles3A: " + apmVadDumpFiles3A.toString());
            if (apmVadDumpFiles3A.size() != 2) {
                sendSseEvent("Error: apm vad dump file count is not 2 with 3A: " + apmVadDumpFiles3A.size(),
                        sseEmitter);
                return false;
            }

            long endTime = System.currentTimeMillis();
            long timeCostMs = endTime - startTime;
            double timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("Test apm pcm task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);
            return true;
        } catch (Exception e) {
            sendSseEvent("Error: test apm pcm task failed: " + e.getMessage(), sseEmitter);
            return false;
        }
    }

    private boolean startSingleServerSse(String configFileName, SseEmitter sseEmitter) {
        sendSseEvent(String.format(
                "--- Starting single server (SSE) --- ConfigFileName: %s", configFileName),
                sseEmitter);
        try {
            long startTime = System.currentTimeMillis();
            taskFinishLatch = new CountDownLatch(1);

            // Use TaskLauncher to start the JSON task
            boolean success = TaskLauncher.startJsonTask(configFileName, agoraTaskManager);
            if (!success) {
                sendSseEvent("Error: Failed to start task with config: " + configFileName, sseEmitter);
                return false;
            }

            taskFinishLatch.await();

            long endTime = System.currentTimeMillis();
            long timeCostMs = endTime - startTime;
            double timeCostMinutes = timeCostMs / 1000.0 / 60.0;
            sendSseEvent("Single server task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                    sseEmitter);
        } catch (Exception e) {
            sendSseEvent("Error: Single server task failed: " + e.getMessage(), sseEmitter);
            return false;
        }
        return true;
    }

    // Helper method to send SSE events - original method
    private void sendSseEvent(String data) {
        SampleLogger.log(data);

        String timestamp = LocalDateTime.now().format(timeFormatter);
        String timestampedData = String.format("[%s] %s", timestamp, data);
        String eventName = "log";
        try {
            sseEmitter.send(SseEmitter.event().name(eventName).data(timestampedData));
        } catch (IOException e) {
            // Log error or handle client disconnects
            log.warn("Failed to send SSE event: {} - {}, error: {}", eventName, timestampedData,
                    e.getMessage());
            // Consider if we should throw a runtime exception to stop the SSE stream
            // For now, just log and continue if possible.
        }
    }

    // Helper method to send SSE events - overloaded method with specific SseEmitter
    private void sendSseEvent(String data, SseEmitter sseEmitter) {
        SampleLogger.log(data);

        String timestamp = LocalDateTime.now().format(timeFormatter);
        String timestampedData = String.format("[%s] %s", timestamp, data);
        String eventName = "log";
        try {
            sseEmitter.send(SseEmitter.event().name(eventName).data(timestampedData));
        } catch (IOException e) {
            // Log error or handle client disconnects
            log.warn("Failed to send SSE event: {} - {}, error: {}", eventName, timestampedData,
                    e.getMessage());
            // Consider if we should throw a runtime exception to stop the SSE stream
            // For now, just log and continue if possible.
        }
    }

    @GetMapping(value = "/destroy")
    public SseEmitter destroyApplication() {
        log.info("=== SSE DESTROY APPLICATION REQUEST RECEIVED ===");

        // Create a new SseEmitter for this request
        sseEmitter = new SseEmitter(Long.MAX_VALUE);

        taskExecutorService.shutdown();
        try {
            try {
                this.destroy(); // Calls the DisposableBean destroy()
            } catch (Exception e) {
                log.error(
                        "Error during resource cleanup phase of application destruction (SSE)", e);
                sendSseEvent("ERROR during resource cleanup: " + e.getMessage());
                // Continue with shutdown attempt
            }

            sendSseEvent("Step 2: Initiating Spring Boot application shutdown...");
            if (this.applicationContext != null) {
                sendSseEvent("Application shutdown process will be initiated shortly. This might "
                        + "be the last message.");
                sseEmitter.complete(); // Complete SSE stream before starting shutdown thread

                new Thread(() -> {
                    try {
                        Thread.sleep(500); // Short delay
                        log.info("Executing asynchronous application shutdown (from SSE "
                                + "/destroy)...");
                        int exitCode = SpringApplication.exit(applicationContext, () -> 0);
                        log.info("SpringApplication.exit() called via SSE /destroy. Exiting with "
                                + "code: {}",
                                exitCode);
                        System.exit(exitCode);
                    } catch (InterruptedException e) {
                        log.warn("Shutdown thread interrupted while waiting to exit (SSE "
                                + "/destroy). Forcing System.exit(1).",
                                e);
                        Thread.currentThread().interrupt();
                        System.exit(1);
                    } catch (Exception e) {
                        log.error("Error during SpringApplication.exit() (SSE /destroy). Forcing "
                                + "System.exit(1).",
                                e);
                        System.exit(1);
                    }
                }, "AppShutdownThread-SSE").start();
                log.info("Application shutdown thread started via SSE /destroy. SSE stream "
                        + "completed.");

            } else {
                log.error("ApplicationContext is not available. Cannot programmatically shut down "
                        + "the application (SSE).");
                sendSseEvent("ERROR: ApplicationContext not found. Shutdown failed.");
                sseEmitter.completeWithError(
                        new IllegalStateException("ApplicationContext not found, cannot shut down."));
            }

        } catch (Exception e) {
            log.error("Error in SSE task for destroyApplication", e);
            try {
                sendSseEvent("Critical error in SSE task for destroy: " + e.getMessage());
            } catch (Exception ex) {
                log.warn("Failed to send critical SSE error message to client for destroy: {}",
                        ex.getMessage());
            }
            sseEmitter.completeWithError(e);
        }
        log.info("SseEmitter returned to client for /destroy endpoint.");
        return sseEmitter;
    }

    @Override
    public void destroy() throws Exception {
        log.info("=== DESTROYING SERVER CONTROLLER ===");
    }
}