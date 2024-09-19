package io.agora.rtc.mediautils;

public class OpusReader {
    private String path;
    private long cptr;

    public OpusReader(String path) {
        this.path = path;
        cptr = init(path);
    }
    public AudioFrame getAudioFrame(int frameSizeDuration){
        return getOpusFrame(cptr,frameSizeDuration);
    }
    public void close(){
        if(cptr != 0L) {
            release(cptr);
            cptr = 0L;
        }
    }

    public void reset(){
        nativeReset(cptr);
    }
    private native long init(String path);
    private native void release(long cptr);
    private native AudioFrame getOpusFrame(long cptr, int frameSizeDuration);
    private native void nativeReset(long cptr);
}
