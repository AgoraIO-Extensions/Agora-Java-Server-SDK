package io.agora.rtc.example.common;

import io.agora.rtc.AgoraVideoEncodedFrameObserver;
import io.agora.rtc.EncodedVideoFrameInfo;
import io.agora.rtc.IVideoEncodedFrameObserver;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SampleVideoEncodedFrameObserver extends FileWriter implements IVideoEncodedFrameObserver {
    protected final ExecutorService writeFileExecutorService = Executors.newSingleThreadExecutor();
    private String outputFilePath = "";

    public SampleVideoEncodedFrameObserver(String outputFilePath) {
        super(outputFilePath);
        Utils.deleteAllFile(outputFilePath);
        this.outputFilePath = outputFilePath;
    }

    @Override
    public int onEncodedVideoFrame(AgoraVideoEncodedFrameObserver observer, int uid,
            ByteBuffer buffer, EncodedVideoFrameInfo info) {
        return 1;
    }

    public void writeVideoDataToFile(ByteBuffer buffer) {
        if ("".equals(outputFilePath.trim()) || buffer == null || buffer.remaining() == 0) {
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
