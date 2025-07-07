package io.agora.rtc.example.mediautils;

public class AudioFrame {
    public int numberOfChannels;
    public int sampleRate;
    public int codec = 8;
    public int samplesPerChannel;
    public byte[] buffer;
    public int length;

    public AudioFrame(int numberOfChannels, int sampleRate, int codec, int samplesPerChannel, int length, byte[] buffer) {
        this.numberOfChannels = numberOfChannels;
        this.sampleRate = sampleRate;
        this.codec = codec;
        this.samplesPerChannel = samplesPerChannel;
        this.buffer = buffer;
        this.length = length;
    }
}
