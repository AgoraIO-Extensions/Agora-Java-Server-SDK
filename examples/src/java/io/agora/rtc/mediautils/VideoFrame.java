package io.agora.rtc.mediautils;

import io.agora.rtc.Constants;

public class VideoFrame {
    public byte[] data;
    public int width = 0;
    public int height = 0;
    public int frameType = Constants.VIDEO_FRAME_TYPE_UNKNOWN;
    public int rotation = 0;
    public int codec = 0;
    public int length = 0;

    public VideoFrame(int width, int height, int codec, int rotation, int frametype, int len, byte[] data) {
        this.data = data;
        this.frameType = frametype;
        this.width = width;
        this.height = height;
        this.codec = codec;
        this.rotation = rotation;
        this.length = len;
    }
}
