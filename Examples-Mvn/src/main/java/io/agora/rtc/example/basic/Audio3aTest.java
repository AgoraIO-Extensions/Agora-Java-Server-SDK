package io.agora.rtc.example.basic;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Audio3aTest {
    private int sampleRate = 48000;
    private int numOfChannels = 1;
    private String nearAudioFile = "test_data/nearin_power.pcm";
    private String farAudioFile = "";
    private String audioOutFile = "test_data_out/recv_near_out_3a";
    // 10ms one frame
    private final int INTERVAL = 10; // ms
    private final int MAX_TASK_COUNT = 2;

    private final ExecutorService taskExecutor = Executors.newCachedThreadPool();
    private final ExecutorService audioWriteExecutor = Executors.newSingleThreadExecutor();
    private final AtomicInteger taskCount = new AtomicInteger(0);

    public void start() {
        String[] keys = Utils.readAppIdAndLicense(".keys_gateway");
        String appId = keys[0];
        String license = keys[1];
        String maskedLicense = license.length() > 5 ? license.substring(0, 5) + "xxxxx" : license;
        SampleLogger.log(
                "read appId: " + appId + " license: " + maskedLicense + " from .keys_gateway");
        SampleLogger.log("AgoraAudioProcessor version: " + AgoraAudioProcessor.getSdkVersion());

        for (int i = 0; i < MAX_TASK_COUNT; i++) {
            taskCount.incrementAndGet();
            final int taskId = i;
            final String audio3aOutFile = this.audioOutFile + "_" + taskId + ".pcm";
            taskExecutor.execute(() -> {
                long totalProcessTime = 0;
                long processCount = 0;
                CountDownLatch taskFinishLatch = new CountDownLatch(1);
                AgoraAudioProcessor audioProcessor = new AgoraAudioProcessor();
                // use default config
                AgoraAudioProcessorConfig config = new AgoraAudioProcessorConfig();
                // set model path
                config.setModelPath("./resources/model/");
                SampleLogger.log("Task " + taskId + " config: " + config);

                int ret = audioProcessor.init(appId, license, new IAgoraAudioProcessorEventHandler() {
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
                nearAudioFrame.setBytesPerSample(
                        Constants.BytesPerSample.TWO_BYTES_PER_SAMPLE.getValue());

                // Check if farAudioFile is provided
                boolean hasFarAudio = farAudioFile != null && !farAudioFile.trim().isEmpty();

                AgoraAudioFrame farAudioFrame = null;
                ByteBuffer farAudioBuffer = null;
                FileInputStream farFis = null;

                if (hasFarAudio) {
                    farAudioFrame = new AgoraAudioFrame();
                    farAudioFrame.setType(Constants.AudioFrameType.PCM16.getValue());
                    farAudioFrame.setSampleRate(sampleRate);
                    farAudioFrame.setChannels(numOfChannels);
                    farAudioFrame.setSamplesPerChannel(sampleRate / 1000 * INTERVAL);
                    farAudioFrame.setBytesPerSample(
                            Constants.BytesPerSample.TWO_BYTES_PER_SAMPLE.getValue());
                    farAudioBuffer = ByteBuffer.allocateDirect(bufferSize);
                }

                ByteBuffer nearAudioBuffer = ByteBuffer.allocateDirect(bufferSize);

                File nearFile = new File(nearAudioFile);
                if (!nearFile.exists()) {
                    SampleLogger.log("Near audio file does not exist: " + nearAudioFile);
                    taskFinishLatch.countDown();
                    return;
                }

                if (hasFarAudio) {
                    File farFile = new File(farAudioFile);
                    if (!farFile.exists()) {
                        SampleLogger.log("Far audio file does not exist: " + farAudioFile);
                        taskFinishLatch.countDown();
                        return;
                    }
                }
                File outFile = new File(audio3aOutFile);
                if (outFile.exists()) {
                    outFile.delete();
                }
                if (!outFile.getParentFile().exists()) {
                    outFile.getParentFile().mkdirs();
                }
                FileOutputStream fos = null;
                try (FileInputStream nearFis = new FileInputStream(nearFile)) {
                    if (hasFarAudio) {
                        farFis = new FileInputStream(new File(farAudioFile));
                    }
                    fos = new FileOutputStream(audio3aOutFile, true);
                    byte[] nearBuffer = new byte[bufferSize];
                    byte[] farBuffer = hasFarAudio ? new byte[bufferSize] : null;
                    int readLen;

                    CountDownLatch writeFinishLatch = new CountDownLatch(1);
                    AtomicInteger writeTaskCount = new AtomicInteger(0);

                    while ((readLen = nearFis.read(nearBuffer)) != -1) {
                        if (hasFarAudio && farFis != null) {
                            farFis.read(farBuffer);
                        }
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

                        if (hasFarAudio && farAudioBuffer != null) {
                            farAudioBuffer.clear();
                            farAudioBuffer.put(farBuffer);
                            farAudioBuffer.flip();
                            farAudioFrame.setBuffer(farAudioBuffer);
                        }

                        long startTime = System.nanoTime();
                        // Pass null for farAudioFrame if farAudioFile is not provided
                        AgoraAudioFrame outFrame = audioProcessor.process(nearAudioFrame,
                                hasFarAudio ? farAudioFrame : null);
                        long estimatedTime = (System.nanoTime() - startTime) / 1000;
                        totalProcessTime += estimatedTime;
                        processCount++;
                        SampleLogger.log(
                                String.format("Task %d process cost: %d us, average cost: %.2f us",
                                        taskId, estimatedTime, (double) totalProcessTime / processCount));
                        if (null != outFrame && outFrame.getBuffer() != null) {
                            // The buffer returned by process() is owned by the SDK and may be
                            // reused. We need to copy the data out of it if we want to use it
                            // asynchronously.
                            final ByteBuffer outBuffer = outFrame.getBuffer();
                            final byte[] outData = new byte[outBuffer.remaining()];
                            outBuffer.get(outData);

                            writeTaskCount.incrementAndGet();
                            final FileOutputStream finalFos = fos;
                            audioWriteExecutor.execute(() -> {
                                try {
                                    finalFos.write(outData);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    SampleLogger.log(
                                            "Exception occurred while writing audio file: " + e);
                                } finally {
                                    if (writeTaskCount.decrementAndGet() == 0) {
                                        writeFinishLatch.countDown();
                                    }
                                }
                            });
                        } else {
                            SampleLogger.log("outFrame is null");
                        }
                    }
                    if (writeTaskCount.get() == 0) {
                        writeFinishLatch.countDown();
                    }
                    writeFinishLatch.await();
                    SampleLogger.log("Audio file reading finished for task " + taskId);
                } catch (Exception e) {
                    e.printStackTrace();
                    SampleLogger.log("Exception occurred while reading audio file: " + e);
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (farFis != null) {
                        try {
                            farFis.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    taskFinishLatch.countDown();
                }

                try {
                    taskFinishLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }

                nearAudioBuffer.clear();
                if (farAudioBuffer != null) {
                    farAudioBuffer.clear();
                }

                try {
                    audioProcessor.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                taskCount.decrementAndGet();
            });
        }
        while (taskCount.get() > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            taskExecutor.shutdown();
            audioWriteExecutor.shutdown();
            taskExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
            audioWriteExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SampleLogger.log("Audio3aTest finished");
        System.exit(0);
    }
}
