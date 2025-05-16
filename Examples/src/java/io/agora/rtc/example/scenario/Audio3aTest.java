package io.agora.rtc.example.scenario;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import io.agora.rtc.audio3a.AgoraAudioProcessor;
import io.agora.rtc.audio3a.AgoraAudioProcessorConfig;
import io.agora.rtc.audio3a.IAgoraAudioProcessorEventHandler;
import io.agora.rtc.audio3a.AgoraAudioFrame;
import io.agora.rtc.Constants;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.utils.Utils;

public class Audio3aTest {
    private static int sampleRate = 16000;
    private static int numOfChannels = 1;
    private static String audioFile = "test_data/nearin_power.pcm";
    private static String audioOutFile = "test_data_out/recv_nearin_power_3a.pcm";
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
        if (parsedArgs.containsKey("audioFile")) {
            audioFile = parsedArgs.get("audioFile");
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
        SampleLogger.log("read appId: " + appId + " license: " + license + " from .keys_3a");

        AgoraAudioProcessor audioProcessor = new AgoraAudioProcessor();
        // use default config
        AgoraAudioProcessorConfig config = new AgoraAudioProcessorConfig();
        // set model path
        config.setModelPath("./resources/model/");
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
        AgoraAudioFrame audioFrame = new AgoraAudioFrame();
        audioFrame.setType(Constants.AudioFrameType.PCM16.getValue());
        audioFrame.setSampleRate(sampleRate);
        audioFrame.setChannels(numOfChannels);
        audioFrame.setSamplesPerChannel(sampleRate / 1000 * INTERVAL);
        audioFrame.setBytesPerSample(Constants.BytesPerSample.TWO_BYTES_PER_SAMPLE.getValue());

        ByteBuffer audioBuffer = ByteBuffer.allocateDirect(bufferSize);

        audioReadExecutor.execute(() -> {
            File file = new File(audioFile);
            if (!file.exists()) {
                SampleLogger.log("Audio file does not exist: " + audioFile);
                taskFinishLatch.countDown();
                return;
            }
            File outFile = new File(audioOutFile);
            if (outFile.exists()) {
                outFile.delete();
            }
            try (FileInputStream fis = new FileInputStream(file);
                    FileOutputStream fos = new FileOutputStream(audioOutFile, true)) {
                byte[] buffer = new byte[bufferSize];
                int readLen;
                while ((readLen = fis.read(buffer)) != -1) {
                    byte[] dataToPut;
                    if (readLen < bufferSize) {
                        dataToPut = new byte[bufferSize];
                        System.arraycopy(buffer, 0, dataToPut, 0, readLen);
                    } else {
                        dataToPut = buffer;
                    }

                    audioBuffer.clear();
                    audioBuffer.put(dataToPut);
                    audioBuffer.flip();
                    audioFrame.setBuffer(audioBuffer);

                    AgoraAudioFrame outFrame = audioProcessor.process(audioFrame);
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

        audioBuffer.clear();

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
    }
}
