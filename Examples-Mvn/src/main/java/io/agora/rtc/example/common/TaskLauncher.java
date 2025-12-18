package io.agora.rtc.example.common;

import io.agora.rtc.example.basic.AIReceiverSendPcmTest;
import io.agora.rtc.example.basic.ExternalAudioProcessorTest;
import io.agora.rtc.example.basic.LoopConnSendPcmTest;
import io.agora.rtc.example.basic.ReceiverPcmDirectSendTest;
import io.agora.rtc.example.basic.ReceiverPcmH264Test;
import io.agora.rtc.example.basic.ReceiverPcmVadTest;
import io.agora.rtc.example.basic.ReceiverPcmYuvTest;
import io.agora.rtc.example.basic.SendAacTest;
import io.agora.rtc.example.basic.SendAv1Test;
import io.agora.rtc.example.basic.SendH264Test;
import io.agora.rtc.example.basic.SendH265Test;
import io.agora.rtc.example.basic.SendMp4Test;
import io.agora.rtc.example.basic.SendOpusTest;
import io.agora.rtc.example.basic.SendPcmFileTest;
import io.agora.rtc.example.basic.SendPcmRealTimeTest;
import io.agora.rtc.example.basic.SendReceiverStreamMessageTest;
import io.agora.rtc.example.basic.SendYuvTest;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified task launcher for both JSON configuration files and basic test
 * classes.
 * Eliminates code duplication between CliLauncher and ServerController.
 */
public class TaskLauncher {

    /**
     * Start a basic test task by class name
     *
     * @param taskName The name of the test class (e.g., "SendH264Test")
     * @return true if task started successfully, false otherwise
     */
    public static boolean startBasicTask(String taskName) {
        return startBasicTask(taskName, true);
    }

    /**
     * Start a basic test task by class name with forceExit control
     *
     * @param taskName  The name of the test class (e.g., "SendH264Test")
     * @param forceExit Whether to force exit after test completion
     * @return true if task started successfully, false otherwise
     */
    public static boolean startBasicTask(String taskName, boolean forceExit) {
        try {
            if (taskName == null || taskName.trim().isEmpty()) {
                SampleLogger.error("Task name is empty");
                return false;
            }

            SampleLogger.log("Starting basic task: " + taskName);

            // Handle basic test classes
            if (taskName.equals("ReceiverPcmDirectSendTest")) {
                ReceiverPcmDirectSendTest test = new ReceiverPcmDirectSendTest();
                test.setForceExit(forceExit);
                test.start();
            } else if (taskName.equals("ReceiverPcmH264Test")) {
                ReceiverPcmH264Test test = new ReceiverPcmH264Test();
                test.setForceExit(forceExit);
                test.start();
            } else if (taskName.equals("ReceiverPcmVadTest")) {
                ReceiverPcmVadTest test = new ReceiverPcmVadTest();
                test.setForceExit(forceExit);
                test.start();
            } else if (taskName.equals("ReceiverPcmYuvTest")) {
                ReceiverPcmYuvTest test = new ReceiverPcmYuvTest();
                test.setForceExit(forceExit);
                test.start();
            } else if (taskName.equals("SendH264Test")) {
                SendH264Test test = new SendH264Test();
                test.setForceExit(forceExit);
                test.start();
            } else if (taskName.equals("SendH265Test")) {
                SendH265Test test = new SendH265Test();
                test.setForceExit(forceExit);
                test.start();
            } else if (taskName.equals("SendAv1Test")) {
                SendAv1Test test = new SendAv1Test();
                test.setForceExit(forceExit);
                test.start();
            } else if (taskName.equals("SendMp4Test")) {
                SendMp4Test test = new SendMp4Test();
                test.setForceExit(forceExit);
                test.start();
            } else if (taskName.equals("SendOpusTest")) {
                SendOpusTest test = new SendOpusTest();
                test.setForceExit(forceExit);
                test.start();
            } else if (taskName.equals("SendAacTest")) {
                SendAacTest test = new SendAacTest();
                test.setForceExit(forceExit);
                test.start();
            } else if (taskName.equals("SendPcmFileTest")) {
                SendPcmFileTest test = new SendPcmFileTest();
                test.setForceExit(forceExit);
                test.start();
            } else if (taskName.equals("SendPcmRealTimeTest")) {
                SendPcmRealTimeTest test = new SendPcmRealTimeTest();
                test.setForceExit(forceExit);
                test.start();
            } else if (taskName.equals("SendReceiverStreamMessageTest")) {
                SendReceiverStreamMessageTest test = new SendReceiverStreamMessageTest();
                test.setForceExit(forceExit);
                test.start();
            } else if (taskName.equals("SendYuvTest")) {
                SendYuvTest test = new SendYuvTest();
                test.setForceExit(forceExit);
                test.start();
                // } else if (taskName.equals("VadV1Test")) {
                // try {
                // Class<?> clazz = Class.forName("io.agora.rtc.example.basic.VadV1Test");
                // Object instance = clazz.getDeclaredConstructor().newInstance();
                // clazz.getMethod("start").invoke(instance);
                // } catch (ClassNotFoundException e) {
                // SampleLogger.error("VadV1Test class not found - possibly disabled in build
                // configuration");
                // return false;
                // } catch (Exception e) {
                // SampleLogger.error("Error executing VadV1Test: " + e.getMessage());
                // return false;
                // }
                // } else if (taskName.equals("Audio3aTest")) {
                // try {
                // Class<?> clazz = Class.forName("io.agora.rtc.example.basic.Audio3aTest");
                // Object instance = clazz.getDeclaredConstructor().newInstance();
                // clazz.getMethod("start").invoke(instance);
                // } catch (ClassNotFoundException e) {
                // SampleLogger.error("Audio3aTest class not found - possibly disabled in build
                // configuration");
                // return false;
                // } catch (Exception e) {
                // SampleLogger.error("Error executing Audio3aTest: " + e.getMessage());
                // return false;
                // }
            } else if (taskName.equals("AIReceiverSendPcmTest")) {
                AIReceiverSendPcmTest test = new AIReceiverSendPcmTest();
                test.setForceExit(forceExit);
                test.start();
            } else if (taskName.equals("ExternalAudioProcessorTest")) {
                ExternalAudioProcessorTest test = new ExternalAudioProcessorTest();
                test.setForceExit(forceExit);
                test.start();
            } else if (taskName.equals("LoopConnSendPcmTest")) {
                LoopConnSendPcmTest test = new LoopConnSendPcmTest();
                test.setForceExit(forceExit);
                test.start();
            } else {
                SampleLogger.error("Invalid or unknown taskName: " + taskName);
                return false;
            }

            return true;
        } catch (Exception e) {
            SampleLogger.error("Error starting basic task '" + taskName + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Start a JSON configuration task
     *
     * @param configFileName   The JSON configuration file name
     * @param agoraTaskManager The task manager to use for starting the task
     * @return true if task started successfully, false otherwise
     */
    public static boolean startJsonTask(String configFileName, AgoraTaskManager agoraTaskManager) {
        try {
            if (configFileName == null || configFileName.trim().isEmpty()) {
                SampleLogger.error("Config file name is empty");
                return false;
            }

            if (agoraTaskManager == null) {
                SampleLogger.error("AgoraTaskManager is null");
                return false;
            }

            SampleLogger.log("Starting JSON task with config: " + configFileName);

            ArgsConfig argsConfig = agoraTaskManager.parseArgsConfig(configFileName);

            // Stress related config files
            if ("stress_recv_yuv.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startStressTask(
                        false, configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_YUV);
            } else if ("stress_recv_pcm.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startStressTask(
                        false, configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM);
            } else if ("stress_recv_h264.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startStressTask(
                        false, configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_H264);
            } else if ("stress_recv_pcm_h264.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startStressTask(
                        false, configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM_H264);
            } else if ("stress_send_yuv.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startStressTask(
                        true, configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_YUV);
            } else if ("stress_send_pcm.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startStressTask(
                        true, configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_PCM);
            } else if ("stress_send_h264.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startStressTask(
                        true, configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_H264);
            } else if ("stress_send_pcm_h264.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startStressTask(
                        true, configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_PCM_H264);
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
            } else if ("pcm_send_ai_with_pts.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                        configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_PCM_AI_WITH_PTS);
            } else if ("pcm_recv_ai_with_pts.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                        configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM_AI_WITH_PTS);
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

            // AI PCM related config files
            else if ("ai_pcm_send.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                        configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_PCM_AI);
            } else if ("ai_pcm_recv.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                        configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_PCM_AI);
            }

            // H265 related config files
            else if ("h265_recv.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                        configFileName, argsConfig, AgoraTaskControl.TestTask.RECEIVE_H265);
            } else if ("h265_send.json".equalsIgnoreCase(configFileName)) {
                agoraTaskManager.startTask(
                        configFileName, argsConfig, AgoraTaskControl.TestTask.SEND_H265);
            }

            // Default case for unknown config files
            else {
                SampleLogger.error("Unknown or unsupported config file: " + configFileName);
                return false;
            }

            return true;
        } catch (Exception e) {
            SampleLogger.error("Error starting JSON task '" + configFileName + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Start all basic test cases sequentially
     * Each test will run with forceExit=false to allow all tests to complete
     *
     * @return true if all tasks started successfully, false if any task failed
     */
    public static boolean startAllBasicCase() {
        SampleLogger.log("========================================");
        SampleLogger.log("Starting ALL Basic Test Cases");
        SampleLogger.log("========================================");

        // Check FFmpeg availability
        boolean ffmpegAvailable = isFFmpegAvailable();
        if (ffmpegAvailable) {
            SampleLogger.log("✓ FFmpeg is available - SendMp4Test will be included");
        } else {
            SampleLogger.log("⚠ FFmpeg is not available - SendMp4Test will be skipped");
            SampleLogger.log("  To run SendMp4Test, please install FFmpeg:");
            SampleLogger.log("  - Ubuntu/Debian: sudo apt-get install ffmpeg libavcodec-dev");
            SampleLogger.log("  - Or compile from source");
        }

        // Build test case list dynamically
        List<String> testCaseList = new ArrayList<>();
        testCaseList.add("AIReceiverSendPcmTest");
        testCaseList.add("ExternalAudioProcessorTest");
        testCaseList.add("LoopConnSendPcmTest");
        testCaseList.add("ReceiverPcmDirectSendTest");
        testCaseList.add("ReceiverPcmH264Test");
        testCaseList.add("ReceiverPcmVadTest");
        testCaseList.add("ReceiverPcmYuvTest");
        testCaseList.add("SendAacTest");
        testCaseList.add("SendAv1Test");
        testCaseList.add("SendH264Test");
        testCaseList.add("SendH265Test");
        testCaseList.add("SendOpusTest");
        testCaseList.add("SendPcmFileTest");
        testCaseList.add("SendPcmRealTimeTest");
        testCaseList.add("SendReceiverStreamMessageTest");
        testCaseList.add("SendYuvTest");

        // Only add SendMp4Test if FFmpeg is available
        if (ffmpegAvailable) {
            testCaseList.add("SendMp4Test");
        }

        String[] allTestCases = testCaseList.toArray(new String[0]);

        int successCount = 0;
        int failCount = 0;
        int skippedCount = ffmpegAvailable ? 0 : 1;

        for (int i = 0; i < allTestCases.length; i++) {
            String testName = allTestCases[i];
            SampleLogger.log("");
            SampleLogger.log("========================================");
            SampleLogger.log("Running Test " + (i + 1) + "/" + allTestCases.length + ": " + testName);
            SampleLogger.log("========================================");

            try {
                boolean success = startBasicTask(testName, false); // forceExit = false
                if (success) {
                    successCount++;
                    SampleLogger.log("✅ Test " + testName + " completed successfully");
                } else {
                    failCount++;
                    SampleLogger.error("❌ Test " + testName + " failed");
                }
            } catch (Exception e) {
                failCount++;
                SampleLogger.error("❌ Test " + testName + " threw exception: " + e.getMessage());
                e.printStackTrace();
            }

            // Wait a bit between tests to ensure cleanup
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        SampleLogger.log("");
        SampleLogger.log("========================================");
        SampleLogger.log("All Basic Tests Completed");
        SampleLogger.log("========================================");
        int totalTests = allTestCases.length + skippedCount;
        SampleLogger.log("Total: " + totalTests + " tests");
        SampleLogger.log("Executed: " + allTestCases.length);
        if (skippedCount > 0) {
            SampleLogger.log("Skipped: " + skippedCount + " (SendMp4Test - FFmpeg not available)");
        }
        SampleLogger.log("Success: " + successCount);
        SampleLogger.log("Failed: " + failCount);
        SampleLogger.log("========================================");

        return failCount == 0;
    }

    /**
     * Determine if the input is a JSON file or a basic test class name
     *
     * @param input The input string to check
     * @return true if it's a JSON file (ends with .json), false otherwise
     */
    public static boolean isJsonFile(String input) {
        return input != null && input.trim().toLowerCase().endsWith(".json");
    }

    /**
     * Check if FFmpeg is available on the system
     *
     * @return true if FFmpeg is available, false otherwise
     */
    private static boolean isFFmpegAvailable() {
        try {
            Process process = Runtime.getRuntime().exec(new String[] { "ffmpeg", "-version" });
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            // FFmpeg not found or error executing
            return false;
        }
    }
}
