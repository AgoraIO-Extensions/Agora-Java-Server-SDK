package io.agora.rtc.common;

import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.EncodedAudioFrameReceiverInfo;
import io.agora.rtc.IAudioEncodedFrameObserver;
import io.agora.rtc.IAudioFrameObserver;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SampleAudioEncodedFrameObserver extends FileWriter implements IAudioEncodedFrameObserver {
    protected final ExecutorService writeFileExecutorService = Executors.newSingleThreadExecutor();

    public SampleAudioEncodedFrameObserver(String outputFilePath) {
        super(outputFilePath);
        Utils.deleteAllFile(outputFilePath);
    }

    @Override
    public int onEncodedAudioFrameReceived(String remoteUserId, byte[] packet, EncodedAudioFrameReceiverInfo info) {
        return 0;
    }

    public void writeAudioFrameToFile(byte[] packet, String filePath) {
        if ("".equals(filePath.trim())) {
            return;
        }
        if (packet == null) {
            return;
        }
        writeFileExecutorService.execute(() -> {
            try {
                writeData(packet, packet.length, filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
