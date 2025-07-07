package io.agora.rtc.example.common;

import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.EncodedAudioFrameReceiverInfo;
import io.agora.rtc.IAudioEncodedFrameObserver;
import io.agora.rtc.IAudioFrameObserver;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import io.agora.rtc.example.utils.Utils;

public class SampleAudioEncodedFrameObserver extends FileWriter implements IAudioEncodedFrameObserver {
    protected final ExecutorService writeFileExecutorService = Executors.newSingleThreadExecutor();

    public SampleAudioEncodedFrameObserver(String outputFilePath) {
        super(outputFilePath);
        Utils.deleteAllFile(outputFilePath);
    }

    /**
     * Note: To improve data transmission efficiency, the buffer of the frame object
     * is a DirectByteBuffer.
     * Be sure to extract the byte array value in the callback synchronously and
     * then transfer it to the asynchronous thread for processing.
     * You can refer to {@link io.agora.rtc.utils.Utils#getBytes(ByteBuffer)}.
     * 
     * @param remoteUserId the remote user id
     * @param buffer       the audio encoded frame buffer
     * @param info         the audio encoded frame info
     * @return 0/1, the return value currently has no practical significance
     */
    @Override
    public int onEncodedAudioFrameReceived(String remoteUserId, ByteBuffer buffer, EncodedAudioFrameReceiverInfo info) {
        return 0;
    }

    public void writeAudioFrameToFile(ByteBuffer buffer, String filePath) {
        if ("".equals(filePath.trim()) || buffer == null || buffer.remaining() == 0) {
            return;
        }

        byte[] byteArray = io.agora.rtc.utils.Utils.getBytes(buffer);

        writeFileExecutorService.execute(() -> {
            try {
                writeData(byteArray, byteArray.length, filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void writeAudioFrameToFile(byte[] buffer, String filePath) {
        if ("".equals(filePath.trim()) || buffer == null || buffer.length == 0) {
            return;
        }

        writeFileExecutorService.execute(() -> {
            try {
                writeData(buffer, buffer.length, filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
