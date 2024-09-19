package io.agora.rtc.common;

import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.IAudioFrameObserver;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SampleAudioFrameObserver extends FileWriter implements IAudioFrameObserver {
    protected final ExecutorService writeFileExecutorService = Executors.newSingleThreadExecutor();
    private String outputFilePath = "";

    public SampleAudioFrameObserver(String outputFilePath) {
        super(outputFilePath);
        if (new File(outputFilePath).exists()) {
            new File(outputFilePath).delete();
        }
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
            AudioFrame frame) {
        return 0;
    }

    @Override
    public int getObservedAudioFramePosition() {
        return 15;
    }

    public void writeAudioFrameToFile(ByteBuffer buffer, int writeBytes) {
        if ("".equals(outputFilePath.trim())) {
            return;
        }
        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        writeFileExecutorService.execute(() -> {
            try {
                writeData(byteArray, writeBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
