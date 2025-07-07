package io.agora.rtc.example.basic;

import io.agora.rtc.AgoraAudioVad;
import io.agora.rtc.AgoraAudioVadConfig;
import io.agora.rtc.VadProcessResult;
import io.agora.rtc.example.common.FileSender;
import io.agora.rtc.example.common.FileWriter;
import io.agora.rtc.example.common.SampleLogger;
import io.agora.rtc.example.utils.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VadV1Test {
    private String audioFile = "test_data/vad_test.pcm";
    private String audioExpectedFile = "test_data/vad_test_expected.pcm";
    private String audioOutFile = "test_data_out/vad1_test_out.pcm";
    private final int SAMPLE_RATE = 16000;
    private final int NUM_CHANNELS = 1;
    private final int BIT_DEPTH = 16;
    private final int MILLISECONDS = 10;
    private PcmReader pcmReader;
    private FileWriter fileWriter;
    private AgoraAudioVad audioVad;

    private final ExecutorService taskExecutor = Executors.newSingleThreadExecutor();

    private CountDownLatch taskFinishLatch = new CountDownLatch(1);

    // audio thread
    // send audio data every 10 ms;
    class PcmReader extends FileSender {
        private AgoraAudioVad audioVad;

        public PcmReader(String filepath, int interval, AgoraAudioVad audioVad) {
            super(filepath, interval, true);
            this.audioVad = audioVad;
        }

        @Override
        public void sendOneFrame(byte[] data, long timestamp) {
            if (data == null) {
                return;
            }
            VadProcessResult result = audioVad.processPcmFrame(data);
            SampleLogger.log("Vad1Test sendOneFrame result:" + result);
            try {
                if (null != fileWriter && null != result) {
                    if (null != result.getOutFrame() && result.getOutFrame().length > 0) {
                        fileWriter.writeData(result.getOutFrame(), result.getOutFrame().length);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public byte[] readOneFrame(FileInputStream fos) {
            int samplesPerMs = SAMPLE_RATE / 1000;
            int samplesFor10Ms = samplesPerMs * MILLISECONDS;
            int bufferSize = samplesFor10Ms * (BIT_DEPTH / 8) * NUM_CHANNELS;
            byte[] pcmData = new byte[bufferSize];
            try {
                int length = fos.read(pcmData);
                if (length == -1) {
                    release();
                    taskFinishLatch.countDown();
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return pcmData;
        }
    }

    public void start() {
        audioVad = new AgoraAudioVad();
        int ret = audioVad.initialize(new AgoraAudioVadConfig());
        SampleLogger.log("AgoraAudioVad initialize ret:" + ret);
        try {
            File output = new File(audioOutFile);
            if (output.exists()) {
                output.delete();
            }
            pcmReader = new PcmReader(audioFile, MILLISECONDS, audioVad);
            fileWriter = new FileWriter(audioOutFile);
            taskExecutor.execute(pcmReader);
            taskFinishLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        File expectedFile = new File(audioExpectedFile);
        File outFile = new File(audioOutFile);
        if (expectedFile.exists() && outFile.exists()
            && Utils.areFilesIdentical(audioExpectedFile, audioOutFile)) {
            SampleLogger.log("Vad1Test passed");
        } else {
            SampleLogger.log("Vad1Test failed");
        }

        if (pcmReader != null) {
            pcmReader.release();
        }

        if (null != fileWriter) {
            fileWriter.release();
        }

        if (null != audioVad) {
            audioVad.destroy();
        }
        taskExecutor.shutdown();
        System.exit(0);
    }
}
