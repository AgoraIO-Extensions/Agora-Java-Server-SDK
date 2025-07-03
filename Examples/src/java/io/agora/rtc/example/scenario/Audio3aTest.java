package io.agora.rtc.example.scenario;

import io.agora.rtc.Constants;
import io.agora.rtc.audio3a.AgoraAudioFrame;
import io.agora.rtc.audio3a.AgoraAudioProcessor;
import io.agora.rtc.audio3a.AgoraAudioProcessorConfig;
import io.agora.rtc.audio3a.IAgoraAudioProcessorEventHandler;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.utils.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Audio3aTest {
    private static int sampleRate = 48000;
    private static int numOfChannels = 1;
    private static String nearAudioFile = "test_data/nearin_power.pcm";
    private static String farAudioFile = "test_data/farin_power.pcm";
    private static String audioOutFile = "test_data_out/recv_near_out_3a.pcm";
    // 10ms one frame
    private static final int INTERVAL = 10; // ms

    private static final ExecutorService audioReadExecutor = Executors.newCachedThreadPool();
    private static final ExecutorService audioWriteExecutor = Executors.newSingleThreadExecutor();

    private static final CountDownLatch taskFinishLatch = new CountDownLatch(1);

    private static void parseArgs(String[] args) {
        SampleLogger.log("parseArgs args:" + Arrays.toString(args));
        if (args == null || args.length == 0) {
            return;
        }

        Map<String, String> parsedArgs = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 < args.length) {
                parsedArgs.put(args[i], args[i + 1]);
            } else {
                SampleLogger.log("Missing value for argument: " + args[i]);
            }
        }

        if (parsedArgs.containsKey("sampleRate")) {
            sampleRate = Integer.parseInt(parsedArgs.get("sampleRate"));
        }
        if (parsedArgs.containsKey("numOfChannels")) {
            numOfChannels = Integer.parseInt(parsedArgs.get("numOfChannels"));
        }
        if (parsedArgs.containsKey("nearAudioFile")) {
            nearAudioFile = parsedArgs.get("nearAudioFile");
        }
        if (parsedArgs.containsKey("farAudioFile")) {
            farAudioFile = parsedArgs.get("farAudioFile");
        }
        if (parsedArgs.containsKey("audioOutFile")) {
            audioOutFile = parsedArgs.get("audioOutFile");
        }
    }

    public static void main(String[] args) {
        parseArgs(args);
        String[] keys = Utils.readAppIdAndLicense(".keys_3a");
        String appId = keys[0];
        String license = keys[1];
        String maskedLicense = license.length() > 5 ? license.substring(0, 5) + "xxxxx" : license;
        SampleLogger.log("read appId: " + appId + " license: " + maskedLicense + " from .keys_3a");
        SampleLogger.log("AgoraAudioProcessor version: " + AgoraAudioProcessor.getSdkVersion());

        AgoraAudioProcessor audioProcessor = new AgoraAudioProcessor();
        // use default config
        AgoraAudioProcessorConfig config = new AgoraAudioProcessorConfig();
        // set model path
        config.setModelPath("./resources/model/");

        SampleLogger.log("config: " + config.toString());

        int ret = audioProcessor.init(appId, license,
                new IAgoraAudioProcessorEventHandler() {
                    @Override
                    public void onEvent(Constants.AgoraAudioProcessorEventType eventType) {
                        SampleLogger.log("onEvent: " + eventType);
                    }

                    @Override
                    public void onError(int errorCode) {
                        SampleLogger.log("onError: " + errorCode);
                    }
                }, config);

        SampleLogger.log("init ret: " + ret);

        final int bufferSize = sampleRate / 1000 * INTERVAL * numOfChannels
                * Constants.BytesPerSample.TWO_BYTES_PER_SAMPLE.getValue();
        AgoraAudioFrame nearAudioFrame = new AgoraAudioFrame();
        nearAudioFrame.setType(Constants.AudioFrameType.PCM16.getValue());
        nearAudioFrame.setSampleRate(sampleRate);
        nearAudioFrame.setChannels(numOfChannels);
        nearAudioFrame.setSamplesPerChannel(sampleRate / 1000 * INTERVAL);
        nearAudioFrame.setBytesPerSample(Constants.BytesPerSample.TWO_BYTES_PER_SAMPLE.getValue());

        AgoraAudioFrame farAudioFrame = new AgoraAudioFrame();
        farAudioFrame.setType(Constants.AudioFrameType.PCM16.getValue());
        farAudioFrame.setSampleRate(sampleRate);
        farAudioFrame.setChannels(numOfChannels);
        farAudioFrame.setSamplesPerChannel(sampleRate / 1000 * INTERVAL);
        farAudioFrame.setBytesPerSample(Constants.BytesPerSample.TWO_BYTES_PER_SAMPLE.getValue());

        ByteBuffer nearAudioBuffer = ByteBuffer.allocateDirect(bufferSize);
        ByteBuffer farAudioBuffer = ByteBuffer.allocateDirect(bufferSize);

        audioReadExecutor.execute(() -> {
            File nearFile = new File(nearAudioFile);
            File farFile = new File(farAudioFile);
            if (!nearFile.exists() || !farFile.exists()) {
                SampleLogger.log("Audio file does not exist: " + nearAudioFile + " or " + farAudioFile);
                taskFinishLatch.countDown();
                return;
            }
            File outFile = new File(audioOutFile);
            if (outFile.exists()) {
                outFile.delete();
            }
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            try (FileInputStream nearFis = new FileInputStream(nearFile);
                    FileInputStream farFis = new FileInputStream(farFile);
                    FileOutputStream fos = new FileOutputStream(audioOutFile, true)) {
                byte[] nearBuffer = new byte[bufferSize];
                byte[] farBuffer = new byte[bufferSize];
                int readLen;
                while ((readLen = nearFis.read(nearBuffer)) != -1) {
                    farFis.read(farBuffer);
                    byte[] dataToPut;
                    if (readLen < bufferSize) {
                        dataToPut = new byte[bufferSize];
                        System.arraycopy(nearBuffer, 0, dataToPut, 0, readLen);
                    } else {
                        dataToPut = nearBuffer;
                    }

                    nearAudioBuffer.clear();
                    nearAudioBuffer.put(dataToPut);
                    nearAudioBuffer.flip();
                    nearAudioFrame.setBuffer(nearAudioBuffer);

                    farAudioBuffer.clear();
                    farAudioBuffer.put(farBuffer);
                    farAudioBuffer.flip();
                    farAudioFrame.setBuffer(farAudioBuffer);

                    AgoraAudioFrame outFrame = audioProcessor.process(nearAudioFrame, farAudioFrame);
                    if (null != outFrame && outFrame.getBuffer() != null) {
                        SampleLogger.log("outFrame: " + Arrays.toString(outFrame.getBuffer().array()));
                        audioWriteExecutor.execute(() -> {
                            try {
                                fos.write(outFrame.getBuffer().array());
                            } catch (Exception e) {
                                e.printStackTrace();
                                SampleLogger.log("Exception occurred while writing audio file: " + e);
                            }
                        });
                    } else {
                        SampleLogger.log("outFrame is null");
                    }

                    try {
                        Thread.sleep(INTERVAL);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                        break;
                    }
                }
                SampleLogger.log("Audio file reading finished");
            } catch (Exception e) {
                e.printStackTrace();
                SampleLogger.log("Exception occurred while reading audio file: " + e);
            } finally {
                taskFinishLatch.countDown();
            }
        });

        try {
            taskFinishLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

        nearAudioBuffer.clear();
        farAudioBuffer.clear();

        try {
            audioProcessor.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            audioReadExecutor.shutdown();
            audioWriteExecutor.shutdown();
            audioReadExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
            audioWriteExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SampleLogger.log("Audio3aTest finished");
        System.exit(0);
    }
}
