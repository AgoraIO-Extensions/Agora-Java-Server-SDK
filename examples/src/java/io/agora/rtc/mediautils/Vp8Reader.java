package io.agora.rtc.mediautils;

public class Vp8Reader {
    private String path;
    private long cptr = 0L;

    public Vp8Reader(String path) {
        this.path = path;
        cptr = init(path);
    }

    public VideoFrame readNextFrame() {
        return getNextFrame(cptr);
    }

    public void close() {
        release(cptr);
        cptr = 0L;
    }

    public void reset() {
        nativeReset(cptr);
    }

    private native long init(String path);

    private native VideoFrame getNextFrame(long cptr);

    private native void release(long cptr);

    private native void nativeReset(long cptr);
}
