package io.agora.rtc.common;

import io.agora.rtc.AgoraVideoEncodedFrameObserver;
import io.agora.rtc.EncodedVideoFrameInfo;
import io.agora.rtc.IVideoEncodedFrameObserver;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SampleVideoEncodedFrameObserver extends FileWriter implements IVideoEncodedFrameObserver {
    protected final ExecutorService writeFileExecutorService = Executors.newSingleThreadExecutor();
    private String outputFilePath = "";

    public SampleVideoEncodedFrameObserver(String outputFilePath) {
        super(outputFilePath);
        if (new File(outputFilePath).exists()) {
            new File(outputFilePath).delete();
        }
        this.outputFilePath = outputFilePath;
    }

    @Override
    public int onEncodedVideoFrame(AgoraVideoEncodedFrameObserver agora_video_encoded_frame_observer, int uid,
            byte[] image_buffer, long length, EncodedVideoFrameInfo video_encoded_frame_info) {
        return 1;
    }

    public void writeVideoDataToFile(byte[] data, int length) {
        if ("".equals(outputFilePath.trim())) {
            return;
        }
        writeFileExecutorService.execute(() -> {
            try {
                writeData(data, length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
