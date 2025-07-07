package io.agora.rtc.example.common;

import com.google.gson.Gson;
import io.agora.rtc.AgoraParameter;
import io.agora.rtc.AudioSubscriptionOptions;
import io.agora.rtc.Constants;
import io.agora.rtc.Out;
import io.agora.rtc.RtcConnConfig;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AgoraTaskManager {
    private final AgoraTaskControl agoraTaskControl;
    private final Gson gson;
    private final AgoraTaskListener agoraTaskListener;
    private final Random random;
    private final ThreadPoolExecutor taskExecutorService;

    public interface AgoraTaskListener {
        default void onTaskStart(
            String taskName, String channelId, String userId, String configFileName) {
        }

        default void onConnected(String channelId, String userId) {
        }

        default void onTestFinished(String channelId, String userId) {
        }

        default void onAllTaskFinished() {
        }
    }

    public AgoraTaskManager(AgoraTaskListener agoraTaskListener) {
        this.agoraTaskListener = agoraTaskListener;
        this.agoraTaskControl = new AgoraTaskControl(agoraTaskListener);
        this.gson = new Gson();
        this.random = new Random();
        this.taskExecutorService = new ThreadPoolExecutor(
            0, Integer.MAX_VALUE, 1L, TimeUnit.SECONDS, new SynchronousQueue<>());
    }

    private String readFileFromResources(String fileName) {
        SampleLogger.log("Attempting to read file from resources: " + fileName);
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(fileName);

        if (resourceStream == null) {
            SampleLogger.error("Cannot find file in classpath: " + fileName);
            throw new RuntimeException("Cannot find file in classpath: " + fileName);
        }

        try (InputStream inputStream = resourceStream;
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            char[] buffer = new char[1024];
            StringBuilder stringBuilder = new StringBuilder();
            int numRead;
            while ((numRead = reader.read(buffer, 0, buffer.length)) != -1) {
                stringBuilder.append(buffer, 0, numRead);
            }
            SampleLogger.log("Successfully read file from resources: " + fileName
                + ", content length: " + stringBuilder.length());
            return stringBuilder.toString();
        } catch (Exception e) {
            SampleLogger.error("Error reading file from resources: " + fileName);
            throw new RuntimeException("Error reading file: " + fileName, e);
        }
    }

    public ArgsConfig parseArgsConfig(String configFileName) {
        if (configFileName == null || configFileName.isEmpty()) {
            SampleLogger.error("configFileName is null or empty");
            return null;
        }

        String configJson = readFileFromResources(configFileName);
        ArgsConfig argsConfig = gson.fromJson(configJson, ArgsConfig.class);
        return argsConfig;
    }

    public void startTask(
        String configFileName, ArgsConfig argsConfig, AgoraTaskControl.TestTask task) {
        if (argsConfig == null) {
            SampleLogger.error("argsConfig is null");
            return;
        }

        SampleLogger.log("sendPcmTask argsConfig: " + argsConfig);

        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        if (argsConfig.isSender()) {
            ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        } else {
            ccfg.setClientRoleType(Constants.CLIENT_ROLE_AUDIENCE);
        }
        if (argsConfig.isEncodedFrame()) {
            ccfg.setAudioRecvEncodedFrame(1);
        } else {
            ccfg.setAudioRecvEncodedFrame(0);
        }
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        if (!argsConfig.isSender() && argsConfig.getSampleRate() > 0) {
            AudioSubscriptionOptions audioSubOpt = new AudioSubscriptionOptions();
            audioSubOpt.setBytesPerSample(2 * argsConfig.getNumOfChannels());
            audioSubOpt.setNumberOfChannels(argsConfig.getNumOfChannels());
            audioSubOpt.setSampleRateHz(argsConfig.getSampleRate());
            ccfg.setAudioSubsOptions(audioSubOpt);
        }

        if (argsConfig.isEnableSingleChannel()) {
            String argsUserId = argsConfig.getUserId();
            for (int i = 0; i < argsConfig.getConnectionCount(); i++) {
                String connUserId = argsUserId.equals("0") ? argsUserId : argsUserId + i;
                argsConfig.setUserId(connUserId);
                agoraTaskControl.createConnectionAndTest(ccfg, argsConfig, task);
                agoraTaskListener.onTaskStart(
                    task.name(), argsConfig.getChannelId(), argsConfig.getUserId(), configFileName);
            }
        } else {
            String argsChannelId = argsConfig.getChannelId();
            for (int i = 0; i < argsConfig.getConnectionCount(); i++) {
                String connChannelId = argsChannelId;
                if (argsConfig.getConnectionCount() > 1) {
                    connChannelId = argsChannelId + i;
                }
                argsConfig.setChannelId(connChannelId);
                agoraTaskControl.createConnectionAndTest(ccfg, argsConfig, task);
                agoraTaskListener.onTaskStart(
                    task.name(), argsConfig.getChannelId(), argsConfig.getUserId(), configFileName);
                try {
                    Thread.sleep((long) (argsConfig.getSleepTime() * 1000));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void testAgoraParameterTask(String configFileName, ArgsConfig argsConfig) {
        if (argsConfig == null) {
            SampleLogger.error("argsConfig is null");
            return;
        }

        SampleLogger.log("testAgoraParameterTask argsConfig: " + argsConfig);

        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(1);
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);

        agoraTaskControl.createConnectionAndTest(ccfg, argsConfig, AgoraTaskControl.TestTask.NONE);
        agoraTaskListener.onTaskStart("testAgoraParameterTask", argsConfig.getChannelId(),
            argsConfig.getUserId(), configFileName);
        try {
            // sleep 2 seconds for rtc connection to be established
            Thread.sleep(2 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        AgoraParameter parameter = AgoraServiceInitializer.getParameter();
        if (parameter != null) {
            try {
                boolean testResultPass = false;
                int ret = -1;
                String key = "";

                key = "rtc.enable_nasa2";
                int intValue = 5;
                ret = parameter.setInt(key, intValue);
                SampleLogger.log("setInt with value " + intValue + " and ret:" + ret);
                Thread.sleep(100);
                Out<Integer> testInt = new Out<>();
                ret = parameter.getInt(key, testInt);
                SampleLogger.log("getInt ret:" + ret + " getInt Result:" + testInt.get());

                if (ret == 0 && testInt.get() == intValue && !testResultPass) {
                    testResultPass = true;
                } else {
                    SampleLogger.error("AgoraParameterTest setInt test fail for channelId:"
                        + argsConfig.getChannelId() + " userId:" + argsConfig.getUserId());
                    return;
                }

                key = "rtc.enable_nasa2";
                boolean boolValue = true;
                parameter.setBool(key, boolValue);
                SampleLogger.log("setBool with value " + boolValue + " and ret:" + ret);
                Thread.sleep(100);
                Out<Boolean> testBool = new Out<>();
                ret = parameter.getBool(key, testBool);
                SampleLogger.log("getBool ret:" + ret + " getBool Result:" + testBool.get());
                if (ret == 0 && testBool.get() == boolValue && testResultPass) {
                    testResultPass = true;
                } else {
                    SampleLogger.error("AgoraParameterTest setBool test fail for channelId:"
                        + argsConfig.getChannelId() + " userId:" + argsConfig.getUserId());
                    return;
                }

                key = "rtc.enable_nasa2";
                int uintValue = 6;
                ret = parameter.setUint(key, uintValue);
                SampleLogger.log("setUint  with value " + uintValue + " and ret:" + ret);
                Thread.sleep(100);
                Out<Integer> testUInt = new Out<>();
                ret = parameter.getUint(key, testUInt);
                SampleLogger.log("getUint ret:" + ret + " getInt Result:" + testUInt.get());
                if (ret == 0 && testUInt.get() == uintValue && testResultPass) {
                    testResultPass = true;
                } else {
                    SampleLogger.error("AgoraParameterTest setUint test fail for channelId:"
                        + argsConfig.getChannelId() + " userId:" + argsConfig.getUserId());
                    return;
                }

                key = "rtc.enable_nasa2";
                double numberValue = 7.0;
                ret = parameter.setNumber(key, numberValue);
                SampleLogger.log("setNumber with value " + numberValue + "  ret:" + ret);
                Thread.sleep(100);
                Out<Double> testDouble = new Out<>();
                ret = parameter.getNumber(key, testDouble);
                SampleLogger.log("getNumber ret:" + ret + " getNumber Result:" + testDouble.get());
                if (ret == 0 && testDouble.get() == numberValue && testResultPass) {
                    testResultPass = true;
                } else {
                    SampleLogger.error("AgoraParameterTest setNumber test fail for channelId:"
                        + argsConfig.getChannelId() + " userId:" + argsConfig.getUserId());
                    return;
                }

                key = "rtc.enable_nasa2";
                String arrayJson = "{\"1\":1}";
                ret = parameter.setArray(key, arrayJson);
                SampleLogger.log("setArray with value " + arrayJson + " and ret:" + ret);
                Thread.sleep(100);

                String parametersJson = "{\"che.audio.custom_bitrate\":128000}";
                ret = parameter.setParameters(parametersJson);
                SampleLogger.log("setParameters with value " + parametersJson + " and ret:" + ret);
                Thread.sleep(100);
                if (ret == 0 && testResultPass) {
                    testResultPass = true;
                } else {
                    SampleLogger.error("AgoraParameterTest setParameters test fail for channelId:"
                        + argsConfig.getChannelId() + " userId:" + argsConfig.getUserId());
                    return;
                }

                key = "rtc.local_domain";
                String stringValue = "ap.250425.agora.local";
                ret = parameter.setString(key, stringValue);
                SampleLogger.log("setString with value " + stringValue + " and ret:" + ret);
                Thread.sleep(100);
                Out<String> testString = new Out<>();
                ret = parameter.getString(key, testString);
                SampleLogger.log("getString ret:" + ret + " getString Result:" + testString.get());
                if (ret == 0 && stringValue.equals(testString.get()) && testResultPass) {
                    testResultPass = true;
                } else {
                    SampleLogger.error("AgoraParameterTest setString fail for channelId:"
                        + argsConfig.getChannelId() + " userId:" + argsConfig.getUserId());
                    return;
                }

                SampleLogger.log("testAgoraParameterTask pass for channelId:"
                    + argsConfig.getChannelId() + " userId:" + argsConfig.getUserId());
                agoraTaskListener.onAllTaskFinished();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            SampleLogger.error("parameter is null");
        }
    }

    public void startStressTask(boolean isSender, String configFileName, ArgsConfig argsConfig,
        AgoraTaskControl.TestTask task) {
        if (argsConfig == null) {
            SampleLogger.error("argsConfig is null");
            return;
        }

        long testTime = argsConfig.getTestTime();
        long testStartTime = System.currentTimeMillis();
        String channelId = argsConfig.getChannelId();
        String userId = argsConfig.getUserId();

        RtcConnConfig ccfg = new RtcConnConfig();
        if (isSender) {
            ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);
        } else {
            ccfg.setClientRoleType(Constants.CLIENT_ROLE_AUDIENCE);
        }
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        for (int i = 0; i < argsConfig.getConnectionCount(); i++) {
            final int threadId = i;
            taskExecutorService.execute(() -> {
                int index = 0;
                while (checkTestTime(testStartTime, testTime)) {
                    int t1 = random.nextInt((int) (argsConfig.getSleepTime()
                                 - argsConfig.getTimeForStressLeave()))
                        + 1;
                    String connChannelId =
                        argsConfig.getConnectionCount() == 1 ? channelId : channelId + threadId;
                    String connUserId =
                        argsConfig.getUserId().equals("0") ? userId : userId + threadId + (index++);
                    argsConfig.setTestTime(t1);
                    argsConfig.setUserId(connUserId);
                    argsConfig.setChannelId(connChannelId);
                    agoraTaskControl.createConnectionAndTest(ccfg, argsConfig, task);
                    agoraTaskListener.onTaskStart(
                        task.name(), connChannelId, connUserId, configFileName);

                    try {
                        Thread.sleep((long) (argsConfig.getSleepTime() * 1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        try {
            Thread.sleep(testTime * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean checkTestTime(long testStartTime, long testTime) {
        long currentTime = System.currentTimeMillis();
        long testCostTime = currentTime - testStartTime;
        if (testCostTime >= testTime * 1000) {
            return false;
        }
        return true;
    }

    public void cleanup() {
        agoraTaskControl.cleanup();
        taskExecutorService.shutdown();
    }

    public void releaseAgoraService() {
        agoraTaskControl.releaseAgoraService();
    }
}
