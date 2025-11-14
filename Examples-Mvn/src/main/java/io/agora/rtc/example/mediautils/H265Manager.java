package io.agora.rtc.example.mediautils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class H265Manager {
    static {
        System.loadLibrary("media_utils");
    }

    public static class H265Frame {
        public final byte[] data; // 对应 C++ 的 buffer
        public final int frameType; // 对应 C++ 的 isKeyFrame (3: 关键帧, 4: 非关键帧)

        public H265Frame(byte[] data, int frameType) {
            this.data = data;
            this.frameType = frameType;
        }

        public boolean isKeyFrame() {
            return frameType == 3; // VIDEO_FRAME_TYPE_KEY_FRAME
        }

        public int getLength() {
            return data != null ? data.length : 0;
        }
    }

    private static final List<H265Frame> frames = new ArrayList<>();
    private static volatile boolean loaded = false;
    private static String loadedFilePath = null;
    private static final Object lock = new Object();

    // 只允许静态方法
    private H265Manager() {
    }

    public static void load(String filePath) throws IOException {
        if (loaded && filePath.equals(loadedFilePath))
            return;
        synchronized (lock) {
            if (loaded && filePath.equals(loadedFilePath))
                return;
            frames.clear();
            H265Reader reader = null;
            try {
                reader = new H265Reader(filePath);
                H265Reader.H265Frame nativeFrame;
                while ((nativeFrame = reader.readNextFrame()) != null) {
                    // 复用 H265Reader 的帧类型
                    frames.add(new H265Frame(nativeFrame.data, nativeFrame.frameType));
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
            loaded = true;
            loadedFilePath = filePath;
        }
    }

    public static int getFrameCount() {
        synchronized (lock) {
            return frames.size();
        }
    }

    public static H265Frame getFrame(int index) {
        synchronized (lock) {
            if (index < 0 || index >= frames.size())
                return null;
            return frames.get(index);
        }
    }

    public static List<H265Frame> getAllFrames() {
        synchronized (lock) {
            return new ArrayList<>(frames);
        }
    }
}
