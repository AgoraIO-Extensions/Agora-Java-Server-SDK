package io.agora.rtc.common;

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

    @Override
    public void onFrame(AgoraVideoFrameObserver2 agora_video_frame_observer2, String channel_id, String remote_uid,
            VideoFrame frame) {
    }

    public void writeVideoFrameToFile(VideoFrame frame) {
        if ("".equals(outputFilePath.trim())) {
            return;
        }
        writeFileExecutorService.execute(() -> {
            int ylength = frame.getYBuffer().remaining();
            int ulength = frame.getUBuffer().remaining();
            int vlength = frame.getVBuffer().remaining();
            byte[] data = new byte[ylength + ulength + vlength];
            ByteBuffer buffer = ByteBuffer.wrap(data);
            buffer.put(frame.getYBuffer());
            buffer.position(ylength);
            buffer.put(frame.getUBuffer());
            buffer.position(ylength + ulength);
            buffer.put(frame.getVBuffer());
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