package io.agora.rtc.mediautils;

import java.nio.ByteBuffer;

import io.agora.rtc.Constants;

public class H264Reader {
    private String path;
    private long cptr = 0L;

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

    public H264Frame readNextFrame(){
        return getNextFrame(cptr);
    }
    public void close(){
        release(cptr);
        cptr = 0L;
    }
    public void reset(){
        nativeReset(cptr);
    }
    private native long init(String path);
    private native H264Frame getNextFrame(long cptr);
    private native void release(long cptr);
    private native void nativeReset(long cptr);
}
