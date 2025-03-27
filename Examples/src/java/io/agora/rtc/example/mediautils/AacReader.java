package io.agora.rtc.example.mediautils;

public class AacReader {
    private String path;
    private long cptr;

    public AacReader(String path) {
        this.path = path;
        cptr = init(path);
    }
    public AacFrame getAudioFrame(int frameSizeDuration){
        return getAacFrame(cptr,frameSizeDuration);
    }
    public void close(){
        if(cptr != 0L) {
            release(cptr);
            cptr = 0L;
        }
    }
    public static class AacFrame {
        public int numberOfChannels;
        public int sampleRate;
        public int codec = 8;
        public int samplesPerChannel;
        public byte[] buffer;
        public int length;

        public AacFrame(int numberOfChannels, int sampleRate, int codec, int samplesPerChannel,int length,byte[] buffer) {
            this.numberOfChannels = numberOfChannels;
            this.sampleRate = sampleRate;
            this.codec = codec;
            this.samplesPerChannel = samplesPerChannel;
            this.buffer = buffer;
            this.length = length;
        }
    }
    public void reset(){
        nativeReset(cptr);
    }
    private native long init(String path);
    private native void release(long cptr);
    private native AacFrame getAacFrame(long cptr, int frameSizeDuration);
    private native void nativeReset(long cptr);
}
