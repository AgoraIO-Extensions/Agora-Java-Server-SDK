package io.agora.rtc.ffmpegutils;

public class MediaDecode {
    static {
        System.loadLibrary("ffmpeg_utils");
    }

    private long decoder = 0;
    private boolean isEndOfFileReached = false;

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
        public boolean isKeyFrame;

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
                    ", isKeyFrame=" + isKeyFrame +
                    ", samples=" + samples +
                    ", channels=" + channels +
                    ", sampleRate=" + sampleRate +
                    ", bytesPerSample=" + bytesPerSample +
                    '}';
        }
    }

    public static class MediaPacket {
        public byte[] buffer;
        public int mediaType;
        public long pts;
        public int flags;

        public int width;
        public int height;
        public int framerateNum;
        public int framerateDen;

        @Override
        public String toString() {
            return "MediaPacket{" +
                    "mediaType=" + mediaType +
                    ", pts=" + pts +
                    ", flags=" + flags +
                    ", width=" + width +
                    ", height=" + height +
                    ", framerateNum=" + framerateNum +
                    ", framerateDen=" + framerateDen +
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
        }
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
        }
    }

    public static class AVPixelFormat {
        public static final int NONE = -1;
        public static final int YUV420P = 0; // planar YUV 4:2:0, 12bpp, (1 Cr & Cb sample per 2x2 Y samples)
        public static final int YUYV422 = 1; // packed YUV 4:2:2, 16bpp, Y0 Cb Y1 Cr

        private AVPixelFormat() {
        }
    }

    public static class AVPktFlag {
        public static final int KEY = 0x0001;

        private AVPktFlag() {
        }
    }

    public boolean isEndOfFileReached() {
        return isEndOfFileReached;
    }

    public boolean open(String fileName) {
        decoder = openMediaFile(fileName);
        isEndOfFileReached = false;
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

    public MediaPacket getPacket() {
        if (decoder == 0) {
            return null;
        }
        return getPacket(decoder);
    }

    public int freePacket(MediaPacket packet) {
        if (decoder == 0) {
            return -1;
        }
        return freePacket(decoder, packet);
    }

    public MediaPacket convertH264ToAnnexB(MediaPacket packet) {
        if (decoder == 0) {
            return null;
        }
        return convertH264ToAnnexB(decoder, packet);
    }

    public MediaFrame decodePacket(MediaPacket packet) {
        if (decoder == 0) {
            return null;
        }
        return decodePacket(decoder, packet);
    }

    private native long openMediaFile(String fileName);

    private native long getMediaDuration(long decoder);

    private native MediaFrame getFrame(long decoder);

    private native void closeMediaFile(long decoder);

    private native MediaPacket getPacket(long decoder);

    private native int freePacket(long decoder, MediaPacket packet);

    private native MediaPacket convertH264ToAnnexB(long decoder, MediaPacket packet);

    private native MediaFrame decodePacket(long decoder, MediaPacket packet);
}
