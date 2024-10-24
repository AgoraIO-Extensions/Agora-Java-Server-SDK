package io.agora.rtc.test.vad;

import io.agora.rtc.AgoraAudioVad;
import io.agora.rtc.AgoraAudioVadConfig;
import io.agora.rtc.VadProcessResult;
import io.agora.rtc.common.FileSender;
import io.agora.rtc.common.FileWriter;
import io.agora.rtc.common.SampleLogger;
import io.agora.rtc.common.Utils;
import io.agora.rtc.test.common.AgoraTest;
import java.io.File;
import java.io.FileInputStream;

public class AgoraAudioVadTest extends AgoraTest {
    private FileWriter fileWriter;
    private AgoraAudioVad audioVad;

    public static void main(String[] args) {
        startTest(args, new AgoraAudioVadTest());
    }

    class PcmReader extends FileSender {
        private AgoraAudioVad audioVad;
        private int internal;

        public PcmReader(String filepath, int interval, AgoraAudioVad audioVad) {
            super(filepath, interval, true);
            this.audioVad = audioVad;
            this.internal = interval;
        }

        @Override
        public void sendOneFrame(byte[] data, long timestamp) {
            if (data == null) {
                return;
            }
            VadProcessResult result = audioVad.processPcmFrame(data);
            SampleLogger.log("AudioVadTest sendOneFrame result:" + result);
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
            int samplesPerMs = sampleRate / 1000;
            int samplesFor10Ms = samplesPerMs * this.internal;
            int bufferSize = samplesFor10Ms * (bitDepth / 8) * numOfChannels;
            byte[] pcmData = new byte[bufferSize];
            try {
                int length = fos.read(pcmData);
                if (length == -1) {
                    SampleLogger.log("AudioVadTest readOneFrame end of file");
                    testTaskCount.decrementAndGet();
                    testFinish();
                    release();
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return pcmData;
        }
    }

    @Override
    public void setup() {
        super.setup();
        audioVad = new AgoraAudioVad();
        int ret = audioVad.initialize(new AgoraAudioVadConfig());
        SampleLogger.log("AudioVadTest setup ret:" + ret);
        try {
            File output = new File(audioOutFile);
            if (output.exists()) {
                output.delete();
            }
            testTaskCount.incrementAndGet();
            fileWriter = new FileWriter(audioOutFile);
            PcmReader pcmReader = new PcmReader(audioFile, 10, audioVad);
            pcmReader.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void testFinish() {
        singleExecutorService.execute(() -> {
            File expectedFile = new File(audioExpectedFile);
            File outFile = new File(audioOutFile);
            if (expectedFile.exists() && outFile.exists() && Utils.areFilesIdentical(audioExpectedFile, audioOutFile)) {
                SampleLogger.log("AudioVadTest passed");
                cleanup();
            } else {
                SampleLogger.log("AudioVadTest failed");
            }
        });
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if (null != audioVad) {
            audioVad.destroy();
        }

        if (null != fileWriter) {
            fileWriter.release();
        }
    }
}