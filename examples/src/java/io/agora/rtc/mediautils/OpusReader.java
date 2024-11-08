package io.agora.rtc.mediautils;

public class OpusReader {
    private String path;
    private long cptr;

    public OpusReader(String path) {
        this.path = path;
        cptr = init(path);
    }

    public AudioFrame getAudioFrame(int frameSizeDuration) {
        return getOpusFrame(cptr, frameSizeDuration);
    }

    public byte[] getOggSHeader() {
        return nativeGetOggSHeader(cptr);
    }

    public byte[] getOpusHeader() {
        return nativeGetOpusHeader(cptr);
    }

    public byte[] getOggOpusTagsHeader() {
        return nativeOggGetOpusTagsHeader(cptr);
    }

    public byte[] getOpusComments() {
        return nativeGetOpusComments(cptr);
    }

    public byte[] getOggAudioHeader() {
        return nativeGetOggAudioHeader(cptr);
    }

    public void close() {
        if (cptr != 0L) {
            release(cptr);
            cptr = 0L;
        }
    }

    public void reset() {
        nativeReset(cptr);
    }

    private native long init(String path);

    private native void release(long cptr);

    private native AudioFrame getOpusFrame(long cptr, int frameSizeDuration);

    private native void nativeReset(long cptr);

    private native byte[] nativeGetOggSHeader(long cptr);

    private native byte[] nativeGetOpusHeader(long cptr);

    private native byte[] nativeOggGetOpusTagsHeader(long cptr);

    private native byte[] nativeGetOpusComments(long cptr);

    private native byte[] nativeGetOggAudioHeader(long cptr);
}
