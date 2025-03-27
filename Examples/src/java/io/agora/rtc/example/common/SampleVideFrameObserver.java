package io.agora.rtc.example.common;

import io.agora.rtc.AgoraVideoFrameObserver2;
import io.agora.rtc.IVideoFrameObserver2;
import io.agora.rtc.VideoFrame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SampleVideFrameObserver extends FileWriter implements IVideoFrameObserver2 {
    protected final ExecutorService writeFileExecutorService = Executors.newSingleThreadExecutor();
    private String outputFilePath = "";

    public SampleVideFrameObserver(String outputFilePath) {
        super(outputFilePath);
        if (new File(outputFilePath).exists()) {
            new File(outputFilePath).delete();
        }
        this.outputFilePath = outputFilePath;
    }

    /**
     * Note: To improve data transmission efficiency, the buffer of the frame object
     * is a DirectByteBuffer.
     * Be sure to extract the byte array value in the callback synchronously and
     * then transfer it to the asynchronous thread for processing.
     * You can refer to {@link io.agora.rtc.utils.Utils#getBytes(ByteBuffer)}.
     * 
     * @param agoraVideoFrameObserver2 the video frame observer
     * @param channelId                the channel id
     * @param remoteUserId             the remote user id
     * @param frame                    the video frame
     */
    @Override
    public void onFrame(AgoraVideoFrameObserver2 agoraVideoFrameObserver2, String channelId, String remoteUserId,
            VideoFrame frame) {
    }

    public void writeVideoFrameToFile(byte[] data) {
        if ("".equals(outputFilePath.trim()) || data == null || data.length == 0) {
            return;
        }
        writeFileExecutorService.execute(() -> {
            File file = new File(outputFilePath);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(file, true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                fileOutputStream.write(data);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
