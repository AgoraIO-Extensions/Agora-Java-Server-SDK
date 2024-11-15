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
    public int onEncodedAudioFrameReceived(String remoteUserId, ByteBuffer buffer, EncodedAudioFrameReceiverInfo info) {
        return 0;
    }

    public void writeAudioFrameToFile(ByteBuffer buffer, String filePath) {
        if ("".equals(filePath.trim()) || buffer == null || buffer.remaining() == 0) {
            return;
        }

        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        buffer.rewind();

        writeFileExecutorService.execute(() -> {
            try {
                writeData(byteArray, byteArray.length, filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
