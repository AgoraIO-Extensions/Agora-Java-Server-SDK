package io.agora.rtc.example.common;

import io.agora.rtc.AgoraVideoEncodedFrameObserver;
import io.agora.rtc.EncodedVideoFrameInfo;
import io.agora.rtc.IVideoEncodedFrameObserver;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import io.agora.rtc.example.utils.Utils;

public class SampleVideoEncodedFrameObserver extends FileWriter implements IVideoEncodedFrameObserver {
    protected final ExecutorService writeFileExecutorService = Executors.newSingleThreadExecutor();
    private String outputFilePath = "";

    public SampleVideoEncodedFrameObserver(String outputFilePath) {
        super(outputFilePath);
        Utils.deleteAllFile(outputFilePath);
        this.outputFilePath = outputFilePath;
    }

    /**
     * Note: To improve data transmission efficiency, the buffer of the frame object
     * is a DirectByteBuffer.
     * Be sure to extract the byte array value in the callback synchronously and
     * then transfer it to the asynchronous thread for processing.
     * You can refer to {@link io.agora.rtc.utils.Utils#getBytes(ByteBuffer)}.
     * 
     * @param observer the video encoded frame observer
     * @param userId   the user id
     * @param buffer   the video encoded frame buffer
     * @param info     the video encoded frame info
     * @return 0/1, the return value currently has no practical significance
     */
    @Override
    public int onEncodedVideoFrame(AgoraVideoEncodedFrameObserver observer, int userId,
            ByteBuffer buffer, EncodedVideoFrameInfo info) {
        return 1;
    }

    public void writeVideoDataToFile(ByteBuffer buffer) {
        if ("".equals(outputFilePath.trim()) || buffer == null || buffer.remaining() == 0) {
            return;
        }
        byte[] byteArray = io.agora.rtc.utils.Utils.getBytes(buffer);

        writeFileExecutorService.execute(() -> {
            try {
                writeData(byteArray, byteArray.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void writeVideoDataToFile(byte[] buffer) {
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

}
