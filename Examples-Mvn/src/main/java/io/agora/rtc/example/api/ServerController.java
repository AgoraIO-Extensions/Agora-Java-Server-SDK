package io.agora.rtc.example.api;

import io.agora.rtc.example.basic.ReceiverPcmDirectSendTest;
import io.agora.rtc.example.basic.ReceiverPcmH264Test;
import io.agora.rtc.example.basic.ReceiverPcmVadTest;
import io.agora.rtc.example.basic.ReceiverPcmYuvTest;
import io.agora.rtc.example.basic.SendH264Test;
import io.agora.rtc.example.basic.SendMp4Test;
import io.agora.rtc.example.basic.SendOpusTest;
import io.agora.rtc.example.basic.SendPcmFileTest;
import io.agora.rtc.example.basic.SendPcmRealTimeTest;
import io.agora.rtc.example.basic.SendReceiverStreamMessageTest;
import io.agora.rtc.example.basic.SendYuvTest;
import io.agora.rtc.example.common.AgoraTaskControl;
import io.agora.rtc.example.common.AgoraTaskManager;
import io.agora.rtc.example.common.ArgsConfig;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.utils.Utils;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private final DateTimeFormatter timeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private AgoraTaskManager.AgoraTaskListener agoraTaskListener =
        new AgoraTaskManager.AgoraTaskListener() {
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
        // currentSseEmitter.set(new SseEmitter(Long.MAX_VALUE)); // Removed as per edit hint
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
                if (taskName.equals("ReceiverPcmDirectSendTest")) {
                    ReceiverPcmDirectSendTest receiverPcmDirectSendTest =
                        new ReceiverPcmDirectSendTest();
                    receiverPcmDirectSendTest.start();
                } else if (taskName.equals("ReceiverPcmH264Test")) {
                    ReceiverPcmH264Test receiverPcmH264Test = new ReceiverPcmH264Test();
                    receiverPcmH264Test.start();
                } else if (taskName.equals("ReceiverPcmVadTest")) {
                    ReceiverPcmVadTest receiverPcmVadTest = new ReceiverPcmVadTest();
                    receiverPcmVadTest.start();
                } else if (taskName.equals("ReceiverPcmYuvTest")) {
                    ReceiverPcmYuvTest receiverPcmYuvTest = new ReceiverPcmYuvTest();
                    receiverPcmYuvTest.start();
                } else if (taskName.equals("SendH264Test")) {
                    SendH264Test sendH264Test = new SendH264Test();
                    sendH264Test.start();
                } else if (taskName.equals("SendMp4Test")) {
                    SendMp4Test sendMp4Test = new SendMp4Test();
                    sendMp4Test.start();
                } else if (taskName.equals("SendOpusTest")) {
                    SendOpusTest sendOpusTest = new SendOpusTest();
                    sendOpusTest.start();
                } else if (taskName.equals("SendPcmFileTest")) {
                    SendPcmFileTest sendPcmFileTest = new SendPcmFileTest();
                    sendPcmFileTest.start();
                } else if (taskName.equals("SendPcmRealTimeTest")) {
                    SendPcmRealTimeTest sendPcmRealTimeTest = new SendPcmRealTimeTest();
                    sendPcmRealTimeTest.start();
                } else if (taskName.equals("SendReceiverStreamMessageTest")) {
                    SendReceiverStreamMessageTest sendReceiverStreamMessageTest =
                        new SendReceiverStreamMessageTest();
                    sendReceiverStreamMessageTest.start();
                } else if (taskName.equals("SendYuvTest")) {
                    SendYuvTest sendYuvTest = new SendYuvTest();
                    sendYuvTest.start();
                } else if (taskName.equals("VadV1Test")) {
                    try {
                        Class<?> clazz = Class.forName("io.agora.rtc.example.basic.VadV1Test");
                        Object instance = clazz.getDeclaredConstructor().newInstance();
                        clazz.getMethod("start").invoke(instance);
                    } catch (ClassNotFoundException e) {
                        log.error(
                            "VadV1Test class not found - possibly disabled in build configuration");
                        sendSseEvent("VadV1Test is not available in this build");
                    } catch (Exception e) {
                        log.error("Error executing VadV1Test", e);
                        sendSseEvent("Error executing VadV1Test: " + e.getMessage());
                    }
                } else if (taskName.equals("Audio3aTest")) {
                    try {
                        Class<?> clazz = Class.forName("io.agora.rtc.example.basic.Audio3aTest");
                        Object instance = clazz.getDeclaredConstructor().newInstance();
                        clazz.getMethod("start").invoke(instance);
                    } catch (ClassNotFoundException e) {
                        log.error("Audio3aTest class not found - possibly disabled in build "
                            + "configuration");
                        sendSseEvent("Audio3aTest is not available in this build");
                    } catch (Exception e) {
                        log.error("Error executing Audio3aTest", e);
                        sendSseEvent("Error executing Audio3aTest: " + e.getMessage());
                    }
                } else {
                    log.error("Invalid taskName: {}", taskName);
                    sendSseEvent("Invalid taskName: " + taskName);
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

        log.info("SseEmitter returned to client for /basic endpoint.");
        return sseEmitter;
    }

    private void startAllServersTasks(SseEmitter sseEmitter) {
        sendSseEvent("--- Starting all servers via SSE ---", sseEmitter);

        long startTime = System.currentTimeMillis();
        try {
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
            long startTime = System.currentTimeMillis();
            taskFinishLatch = new CountDownLatch(1);

            String recvConfigFileName = "pcm_recv.json";
            ArgsConfig recvArgsConfig = agoraTaskManager.parseArgsConfig(recvConfigFileName);
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
            sendSseEvent("recv and send encrypted pcm task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                sseEmitter);

            Thread.sleep(5 * 1000);

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
                        + recvArgsConfig.getAudioOutFile(),
                    sseEmitter);
                return false;
            }

            if (recvArgsConfig.isEnableVad()) {
                if (!Utils.checkFileExists(recvArgsConfig.getAudioOutFile(), "_vad.pcm")) {
                    sendSseEvent("Error: recv pcm with remote user id audioOutFile does not exist: "
                            + recvArgsConfig.getAudioOutFile(),
                        sseEmitter);
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
            sendSseEvent("recv and send pcm with remote user id task finished, time cost: "
                    + String.format("%.2f", timeCostMinutes) + " minutes (" + timeCostMs + "ms)",
                sseEmitter);

            Thread.sleep(5 * 1000);

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
            sendSseEvent(
                "recv and send pcm with not exist remote user id  task finished, time cost: "
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
                configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_VP8_PCM);

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

    private boolean startSingleServerSse(String configFileName, SseEmitter sseEmitter) {
        sendSseEvent(String.format(
                         "--- Starting single server (SSE) --- ConfigFileName: %s", configFileName),
            sseEmitter);
        try {
            long startTime = System.currentTimeMillis();
            taskFinishLatch = new CountDownLatch(1);

            ArgsConfig argsConfig = agoraTaskManager.parseArgsConfig(configFileName);

            // Stress related config files
            if ("stress_recv_pcm_h264.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startStressTask(
                    false, configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM_H264);
            } else if ("stress_recv_yuv.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startStressTask(
                    false, configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_YUV);
            } else if ("stress_send_pcm_h264.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startStressTask(
                    true, configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_PCM_H264);
            } else if ("stress_send_yuv.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startStressTask(
                    true, configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_YUV);
            }

            // PCM related config files
            else if ("pcm_recv.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM);
            } else if ("pcm_send.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_PCM);
            } else if ("pcm_yuv_send.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_PCM_YUV);
            } else if ("pcm_h264_send.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_PCM_H264);
            } else if ("pcm_recv_encrypted.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM);
            } else if ("pcm_send_encrypted.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_PCM);
            } else if ("pcm_recv_with_remote_user_id.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM);
            } else if ("pcm_send_with_remote_user_id.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_PCM);
            } else if ("pcm_recv_by_cloud_proxy.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM);
            } else if ("pcm_send_by_cloud_proxy.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_PCM);
            } else if ("pcm_h264_recv.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM_H264);
            }

            // Encoded Audio related config files
            else if ("encoded_audio_recv.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_ENCODED_AUDIO);
            } else if ("opus_send.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_OPUS);
            }

            // Mixed Audio related config files
            else if ("mixed_audio_recv.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_MIXED_AUDIO);
            } else if ("aac_send.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_AAC);
            }

            // YUV related config files
            else if ("yuv_recv.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_YUV);
            } else if ("yuv_send.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_YUV);
            } else if ("yuv_recv_with_remote_user_id.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_YUV);
            } else if ("yuv_send_with_remote_user_id.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_YUV);
            } else if ("yuv_recv_with_alpha.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_YUV);
            } else if ("yuv_send_with_alpha.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_YUV);
            } else if ("yuv_recv_dual_stream_high.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_YUV);
            } else if ("yuv_recv_dual_stream_low.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_YUV);
            } else if ("yuv_send_dual_stream.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_YUV_DUAL_STREAM);
            }

            // H264 related config files
            else if ("h264_recv.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_H264);
            } else if ("h264_send.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_H264);
            } else if ("h264_recv_with_remote_user_id.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_H264);
            } else if ("h264_send_with_remote_user_id.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_H264);
            } else if ("h264_recv_encrypted.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_H264);
            } else if ("h264_send_encrypted.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_H264);
            } else if ("h264_recv_dual_stream_high.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_H264);
            } else if ("h264_recv_dual_stream_low.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_H264);
            } else if ("h264_send_dual_stream.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_H264_DUAL_STREAM);
            }

            // Agora Parameter related config files
            else if ("agora_parameter_test.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.testAgoraParameterTask(configFileName, argsConfig);
            }

            // Data Stream related config files
            else if ("data_stream_send.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_DATA_STREAM);
            } else if ("data_stream_recv.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_DATA_STREAM);
            }

            // RGBA related config files
            else if ("rgba_send.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_RGBA);
            } else if ("rgba_pcm_send.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_RGBA_PCM);
            } else if ("rgba_recv.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_H264);
            }

            // VP8 related config files
            else if ("vp8_send.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_VP8);
            } else if ("vp8_pcm_send.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_VP8_PCM);
            } else if ("vp8_recv.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_YUV);
            }

            // MP4 related config files
            else if ("mp4_send.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                    configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_MP4);
            }

            // Default case for unknown config files
            else {
                sendSseEvent("Error: Unknown config file: " + configFileName, sseEmitter);
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
                        log.info(
                            "Executing asynchronous application shutdown (from SSE /destroy)...");
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
                log.info(
                    "Application shutdown thread started via SSE /destroy. SSE stream completed.");

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