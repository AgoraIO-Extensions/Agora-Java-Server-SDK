package io.agora.rtc.mediautils;

public class MediaDecode {
    static {
        System.loadLibrary("media_decode");
    }

    private long decoder = 0;

    public static class MediaFrame {
        public int streamIndex;
        public int frameType;
        public long pts;
        public byte[] buffer;
        public int bufferSize;
        // video pixel format or audio sample format
        public int format;

        // video fields
        public int width;
        public int height;
        public int stride;
        public int fps;

        // audio fields
        public int samples;
        public int channels;
        public int sampleRate;
        public int bytesPerSample;

        @Override
        public String toString() {
            return "MediaFrame{" +
                    "streamIndex=" + streamIndex +
                    ", frameType=" + frameType +
                    ", pts=" + pts +
                    ", bufferSize=" + bufferSize +
                    ", format=" + format +
                    ", width=" + width +
                    ", height=" + height +
                    ", stride=" + stride +
                    ", fps=" + fps +
                    ", samples=" + samples +
                    ", channels=" + channels +
                    ", sampleRate=" + sampleRate +
                    ", bytesPerSample=" + bytesPerSample +
                    '}';
        }
    }

    public static class AVMediaType {
        public static final int UNKNOWN = -1; // Usually treated as DATA
        public static final int VIDEO = 0;
        public static final int AUDIO = 1;
        public static final int DATA = 2; // Opaque data information usually continuous
        public static final int SUBTITLE = 3;
        public static final int ATTACHMENT = 4; // Opaque data information usually sparse
        public static final int NB = 5;

        private AVMediaType() {
        } // 私有构造函数防止实例化
    }

    public static class AVSampleFormat {
        public static final int NONE = -1;
        public static final int U8 = 0; // unsigned 8 bits
        public static final int S16 = 1; // signed 16 bits
        public static final int S32 = 2; // signed 32 bits
        public static final int FLT = 3; // float
        public static final int DBL = 4; // double

        public static final int U8P = 5; // unsigned 8 bits, planar
        public static final int S16P = 6; // signed 16 bits, planar
        public static final int S32P = 7; // signed 32 bits, planar
        public static final int FLTP = 8; // float, planar
        public static final int DBLP = 9; // double, planar

        public static final int NB = 10; // Number of sample formats. DO NOT USE if linking dynamically

        private AVSampleFormat() {
        } // 私有构造函数防止实例化
    }

    public static class AVPixelFormat {
        public static final int NONE = -1;
        public static final int YUV420P = 0; // planar YUV 4:2:0, 12bpp, (1 Cr & Cb sample per 2x2 Y samples)
        public static final int YUYV422 = 1; // packed YUV 4:2:2, 16bpp, Y0 Cb Y1 Cr

        private AVPixelFormat() {
        } // 私有构造函数防止实例化
    }

    public boolean open(String fileName) {
        decoder = openMediaFile(fileName);
        return decoder != 0;
    }

    public void close() {
        if (decoder != 0) {
            closeMediaFile(decoder);
            decoder = 0;
        }
    }

    public MediaFrame getFrame() {
        if (decoder == 0) {
            return null;
        }
        return getFrame(decoder);
    }

    public long getDuration() {
        if (decoder == 0) {
            return 0;
        }
        return getMediaDuration(decoder);
    }

    private native long openMediaFile(String fileName);

    private native long getMediaDuration(long decoder);

    private native MediaFrame getFrame(long decoder);

    private native void closeMediaFile(long decoder);

}
