package io.agora.rtc.example.mediautils;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class H264Manager {
    static {
        System.loadLibrary("media_utils");
    }
    public static class H264Frame {
        public final byte[] data;
        public final int frameType; // 3: I, 4: P, 0: 其他

        public H264Frame(byte[] data, int frameType) {
            this.data = data;
            this.frameType = frameType;
        }
    }

    private static final List<H264Frame> frames = new ArrayList<>();
    private static volatile boolean loaded = false;
    private static String loadedFilePath = null;
    private static final Object lock = new Object();

    // 只允许静态方法
    private H264Manager() {
    }

    public static void load(String filePath) throws IOException {
        if (loaded && filePath.equals(loadedFilePath))
            return;
        synchronized (lock) {
            if (loaded && filePath.equals(loadedFilePath))
                return;
            frames.clear();
            H264Reader reader = null;
            try {
                reader = new H264Reader(filePath);
                H264Reader.H264Frame nativeFrame;
                while ((nativeFrame = reader.readNextFrame()) != null) {
                    // 复用 H264Reader 的帧类型
                    frames.add(new H264Frame(nativeFrame.data, nativeFrame.frameType));
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

    public static H264Frame getFrame(int index) {
        synchronized (lock) {
            if (index < 0 || index >= frames.size())
                return null;
            return frames.get(index);
        }
    }

    public static List<H264Frame> getAllFrames() {
        synchronized (lock) {
            return new ArrayList<>(frames);
        }
    }
}
