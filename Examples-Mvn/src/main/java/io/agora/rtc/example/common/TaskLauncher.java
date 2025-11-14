package io.agora.rtc.example.common;

import io.agora.rtc.example.basic.AIReceiverSendPcmTest;
import io.agora.rtc.example.basic.ReceiverPcmDirectSendTest;
import io.agora.rtc.example.basic.ReceiverPcmH264Test;
import io.agora.rtc.example.basic.ReceiverPcmVadTest;
import io.agora.rtc.example.basic.ReceiverPcmYuvTest;
import io.agora.rtc.example.basic.SendAv1Test;
import io.agora.rtc.example.basic.SendH264Test;
import io.agora.rtc.example.basic.SendH265Test;
import io.agora.rtc.example.basic.SendMp4Test;
import io.agora.rtc.example.basic.SendOpusTest;
import io.agora.rtc.example.basic.SendPcmFileTest;
import io.agora.rtc.example.basic.SendPcmRealTimeTest;
import io.agora.rtc.example.basic.SendReceiverStreamMessageTest;
import io.agora.rtc.example.basic.SendYuvTest;

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
        try {
            if (taskName == null || taskName.trim().isEmpty()) {
                SampleLogger.error("Task name is empty");
                return false;
            }

            SampleLogger.log("Starting basic task: " + taskName);

            // Handle basic test classes
            if (taskName.equals("ReceiverPcmDirectSendTest")) {
                ReceiverPcmDirectSendTest test = new ReceiverPcmDirectSendTest();
                test.start();
            } else if (taskName.equals("ReceiverPcmH264Test")) {
                ReceiverPcmH264Test test = new ReceiverPcmH264Test();
                test.start();
            } else if (taskName.equals("ReceiverPcmVadTest")) {
                ReceiverPcmVadTest test = new ReceiverPcmVadTest();
                test.start();
            } else if (taskName.equals("ReceiverPcmYuvTest")) {
                ReceiverPcmYuvTest test = new ReceiverPcmYuvTest();
                test.start();
            } else if (taskName.equals("SendH264Test")) {
                SendH264Test test = new SendH264Test();
                test.start();
            } else if (taskName.equals("SendH265Test")) {
                SendH265Test test = new SendH265Test();
                test.start();
            } else if (taskName.equals("SendAv1Test")) {
                SendAv1Test test = new SendAv1Test();
                test.start();
            } else if (taskName.equals("SendMp4Test")) {
                SendMp4Test test = new SendMp4Test();
                test.start();
            } else if (taskName.equals("SendOpusTest")) {
                SendOpusTest test = new SendOpusTest();
                test.start();
            } else if (taskName.equals("SendPcmFileTest")) {
                SendPcmFileTest test = new SendPcmFileTest();
                test.start();
            } else if (taskName.equals("SendPcmRealTimeTest")) {
                SendPcmRealTimeTest test = new SendPcmRealTimeTest();
                test.start();
            } else if (taskName.equals("SendReceiverStreamMessageTest")) {
                SendReceiverStreamMessageTest test = new SendReceiverStreamMessageTest();
                test.start();
            } else if (taskName.equals("SendYuvTest")) {
                SendYuvTest test = new SendYuvTest();
                test.start();
            } else if (taskName.equals("VadV1Test")) {
                try {
                    Class<?> clazz = Class.forName("io.agora.rtc.example.basic.VadV1Test");
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    clazz.getMethod("start").invoke(instance);
                } catch (ClassNotFoundException e) {
                    SampleLogger.error("VadV1Test class not found - possibly disabled in build configuration");
                    return false;
                } catch (Exception e) {
                    SampleLogger.error("Error executing VadV1Test: " + e.getMessage());
                    return false;
                }
            } else if (taskName.equals("Audio3aTest")) {
                try {
                    Class<?> clazz = Class.forName("io.agora.rtc.example.basic.Audio3aTest");
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    clazz.getMethod("start").invoke(instance);
                } catch (ClassNotFoundException e) {
                    SampleLogger.error("Audio3aTest class not found - possibly disabled in build configuration");
                    return false;
                } catch (Exception e) {
                    SampleLogger.error("Error executing Audio3aTest: " + e.getMessage());
                    return false;
                }
            } else if (taskName.equals("AIReceiverSendPcmTest")) {
                AIReceiverSendPcmTest test = new AIReceiverSendPcmTest();
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
     * Determine if the input is a JSON file or a basic test class name
     *
     * @param input The input string to check
     * @return true if it's a JSON file (ends with .json), false otherwise
     */
    public static boolean isJsonFile(String input) {
        return input != null && input.trim().toLowerCase().endsWith(".json");
    }
}
