package io.agora.rtc.example.mediautils;

import io.agora.rtc.Constants;
import io.agora.rtc.example.common.SampleLogger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class H264Reader {
    private String path;
    private long cptr = 0L;
    private final AtomicBoolean isReleased = new AtomicBoolean(false);
    private final ReentrantLock operationLock = new ReentrantLock();

    public static class H264Frame {
        public byte[] data;
        public int width = 0;
        public int height = 0;
        public int framesPerSecond = 0;
        public int frameType = Constants.VIDEO_FRAME_TYPE_UNKNOWN;
        public int rotation = 0;
        public int captureTimeMs = 0;
        public int renderTimeMs = 0;
        public int uid = 0;
        public int streamType = 1;

        public H264Frame(byte[] data, int frameType) {
            this.data = data;
            this.frameType = frameType;
        }
    }

    public H264Reader(String path) {
        this.path = path;
        cptr = init(path);
    }

    public H264Frame readNextFrame() {
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
                    SampleLogger.error("Error releasing H264Reader: " + e.getMessage());
                }
            }
        } finally {
            operationLock.unlock();
        }
    }

    private native long init(String path);

    private native H264Frame getNextFrame(long cptr);

    private native void nativeRelease(long cptr);

    private native void nativeReset(long cptr);
}
