package io.agora.rtc.example.mediautils;

import io.agora.rtc.Constants;
import io.agora.rtc.example.common.SampleLogger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class H265Reader {
    private String path;
    private long cptr = 0L;
    private final AtomicBoolean isReleased = new AtomicBoolean(false);
    private final ReentrantLock operationLock = new ReentrantLock();

    public static class H265Frame {
        public byte[] data; // 对应 C++ 的 buffer
        public int frameType; // 对应 C++ 的 isKeyFrame (3: 关键帧, 4: 非关键帧)

        public H265Frame(byte[] data, int frameType) {
            this.data = data;
            this.frameType = frameType;
        }

        public boolean isKeyFrame() {
            return frameType == Constants.VIDEO_FRAME_TYPE_KEY_FRAME; // 3
        }

        public int getLength() {
            return data != null ? data.length : 0;
        }
    }

    public H265Reader(String path) {
        this.path = path;
        cptr = init(path);
    }

    public H265Frame readNextFrame() {
        operationLock.lock();
        try {
            if (cptr == 0L || isReleased.get()) {
                return null;
            }
            return getNextFrame(cptr);
        } finally {
            operationLock.unlock();
        }
    }

    public void close() {
        operationLock.lock();
        try {
            release(cptr);
            cptr = 0L;
        } finally {
            operationLock.unlock();
        }
    }

    public void reset() {
        operationLock.lock();
        try {
            if (cptr != 0L && !isReleased.get()) {
                nativeReset(cptr);
            }
        } finally {
            operationLock.unlock();
        }
    }

    public void release(long cptr) {
        operationLock.lock();
        try {
            if (cptr != 0L && isReleased.compareAndSet(false, true)) {
                try {
                    nativeRelease(cptr);
                } catch (Exception e) {
                    SampleLogger.error("Error releasing H265Reader: " + e.getMessage());
                }
            }
        } finally {
            operationLock.unlock();
        }
    }

    private native long init(String path);

    private native H265Frame getNextFrame(long cptr);

    private native void nativeRelease(long cptr);

    private native void nativeReset(long cptr);
}
