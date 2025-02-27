package io.agora.rtc.example.test.parameter;

import io.agora.rtc.AgoraParameter;
import io.agora.rtc.AgoraRtcConn;
import io.agora.rtc.Constants;
import io.agora.rtc.Out;
import io.agora.rtc.RtcConnConfig;
import io.agora.rtc.example.common.AgoraTest;
import io.agora.rtc.example.common.ArgsConfig;
import io.agora.rtc.example.common.SampleLogger;

public class AgoraParameterTest extends AgoraTest {
    public static void main(String[] args) {
        startTest(args, new AgoraParameterTest());
    }

    @Override
    public void setup() {
        super.setup();

        RtcConnConfig ccfg = new RtcConnConfig();
        ccfg.setAutoSubscribeAudio(0);
        ccfg.setAutoSubscribeVideo(0);
        ccfg.setChannelProfile(1);
        ccfg.setClientRoleType(Constants.CLIENT_ROLE_BROADCASTER);

        createConnectionAndTest(ccfg, ArgsConfig.channelId, ArgsConfig.userId, TestTask.NONE, ArgsConfig.testTime);
    }

    @Override
    protected void onConnected(AgoraRtcConn conn, String channelId, String userId, TestTask testTask) {
        SampleLogger.log("onConnected channelId:" + channelId + " userId:" + userId + " testTask:" + testTask);
        AgoraParameter parameter = conn.getAgoraParameter();
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
                SampleLogger.log("getInt ret:" + ret + " getInt Result:" +
                        testInt.get());

                if (ret == 0 && testInt.get() == intValue && !testResultPass) {
                    testResultPass = true;
                } else {
                    SampleLogger.error(
                            "AgoraParameterTest setInt test fail for channelId:" + channelId + " userId:" + userId);
                    return;
                }

                key = "rtc.enable_nasa2";
                boolean boolValue = true;
                parameter.setBool(key, boolValue);
                SampleLogger.log("setBool with value " + boolValue + " and ret:" + ret);
                Thread.sleep(100);
                Out<Boolean> testBool = new Out<>();
                ret = parameter.getBool(key, testBool);
                SampleLogger.log("getBool ret:" + ret + " getBool Result:" +
                        testBool.get());
                if (ret == 0 && testBool.get() == boolValue && testResultPass) {
                    testResultPass = true;
                } else {
                    SampleLogger.error(
                            "AgoraParameterTest setBool test fail for channelId:" + channelId + " userId:" + userId);
                    return;
                }

                key = "rtc.enable_nasa2";
                int uintValue = 6;
                ret = parameter.setUint(key, uintValue);
                SampleLogger.log("setUint  with value " + uintValue + " and ret:" + ret);
                Thread.sleep(100);
                Out<Integer> testUInt = new Out<>();
                ret = parameter.getUint(key, testUInt);
                SampleLogger.log("getUint ret:" + ret + " getInt Result:" +
                        testUInt.get());
                if (ret == 0 && testUInt.get() == uintValue && testResultPass) {
                    testResultPass = true;
                } else {
                    SampleLogger.error(
                            "AgoraParameterTest setUint test fail for channelId:" + channelId + " userId:" + userId);
                    return;
                }

                key = "rtc.enable_nasa2";
                double numberValue = 7.0;
                ret = parameter.setNumber(key, numberValue);
                SampleLogger.log("setNumber with value " + numberValue + "  ret:" + ret);
                Thread.sleep(100);
                Out<Double> testDouble = new Out<>();
                ret = parameter.getNumber(key, testDouble);
                SampleLogger.log("getNumber ret:" + ret + " getNumber Result:" +
                        testDouble.get());
                if (ret == 0 && testDouble.get() == numberValue && testResultPass) {
                    testResultPass = true;
                } else {
                    SampleLogger.error(
                            "AgoraParameterTest setNumber test fail for channelId:" + channelId + " userId:" + userId);
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
                    SampleLogger.error("AgoraParameterTest setParameters test fail for channelId:" + channelId
                            + " userId:" + userId);
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
                    SampleLogger.error(
                            "AgoraParameterTest setString fail for channelId:" + channelId + " userId:" + userId);
                    return;
                }

                SampleLogger.log("AgoraParameterTest test pass for channelId:" + channelId + " userId:" + userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
