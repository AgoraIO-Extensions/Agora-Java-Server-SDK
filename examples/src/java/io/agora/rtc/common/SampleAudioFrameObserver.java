package io.agora.rtc.common;

import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.IAudioFrameObserver;
import io.agora.rtc.VadProcessResult;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SampleAudioFrameObserver extends FileWriter implements IAudioFrameObserver {
    protected final ExecutorService writeFileExecutorService = Executors.newSingleThreadExecutor();
    protected final ExecutorService writeVadFileExecutorService = Executors.newSingleThreadExecutor();

    private String outputFilePath = "";

    public SampleAudioFrameObserver(String outputFilePath) {
        super(outputFilePath);
        Utils.deleteAllFile(outputFilePath);
        this.outputFilePath = outputFilePath;
    }

    @Override
    public int onRecordAudioFrame(AgoraLocalUser agora_local_user, String channel_id, AudioFrame frame) {
        return 1;
    }

    @Override
    public int onPlaybackAudioFrame(AgoraLocalUser agora_local_user, String channel_id, AudioFrame frame) {
        return 1;
    }

    @Override
    public int onMixedAudioFrame(AgoraLocalUser agora_local_user, String channel_id, AudioFrame frame) {
        return 1;
    }

    @Override
    public int onEarMonitoringAudioFrame(AgoraLocalUser agora_local_user, AudioFrame frame) {
        return 1;
    }

    @Override
    public int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agora_local_user, String channel_id, String uid,
            AudioFrame frame, VadProcessResult vadResult) {
        return 0;
    }

    @Override
    public int getObservedAudioFramePosition() {
        return 15;
    }

    public void writeAudioFrameToFile(ByteBuffer buffer) {
        if ("".equals(outputFilePath.trim())) {
            return;
        }
        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        buffer.rewind();

        writeFileExecutorService.execute(() -> {
            try {
                writeData(byteArray, byteArray.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void writeAudioFrameToFile(byte[] buffer) {
        if ("".equals(outputFilePath.trim()) || buffer == null || buffer.length == 0) {
            return;
        }

        writeFileExecutorService.execute(() -> {
            try {
                writeData(buffer, buffer.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void writeVadAudioToFile(byte[] byteArray, String file) {
        if (byteArray == null || byteArray.length == 0) {
            return;
        }

        writeVadFileExecutorService.execute(() -> {
            try (FileOutputStream fos = new FileOutputStream(file, true)) {
                fos.write(byteArray);
                fos.flush();
            } catch (Exception e) {
                SampleLogger.log("Open file fail");
            }
        });
    }
}
