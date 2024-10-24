package io.agora.rtc.common;

import java.nio.ByteBuffer;

public class AudioDataCache {
    // Constants
    private static final int START_BY_MAX_FRAME_SIZE = 18;
    private static final int START_BY_MIN_FRAME_SIZE = 6;
    private static final int INTERVAL_ONE_FRAME = 10; // ms
    private static final int INTERVAL_PCM_INTERRUPT = 200; // ms

    // Instance variables
    private final int numOfChannels;
    private final int sampleRate;
    private final int oneFrameSize;
    private int startCacheDataSize;
    private ByteBuffer buffer;
    private long startedTimestamp;
    private long lastSendTimestamp;
    private int consumedFrameCount;

    // Constructor
    public AudioDataCache(int numOfChannels, int sampleRate) {
        this.numOfChannels = numOfChannels;
        this.sampleRate = sampleRate;
        this.oneFrameSize = numOfChannels * (sampleRate / 1000) * INTERVAL_ONE_FRAME * 2;
        this.startCacheDataSize = oneFrameSize * START_BY_MAX_FRAME_SIZE;
        this.buffer = ByteBuffer.allocate(startCacheDataSize * 2);
        this.startedTimestamp = 0;
        this.lastSendTimestamp = 0;
        this.consumedFrameCount = 0;
    }

    public int getOneFrameSize() {
        return oneFrameSize;
    }

    // Calculate samples per channel
    public int getSamplesPerChannel(int dataSize) {
        return sampleRate / 1000 * INTERVAL_ONE_FRAME * (dataSize / oneFrameSize);
    }

    // Push data into the cache
    // data size must be multiple of oneFrameSize
    public synchronized void pushData(byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }
        try {
            int dataLeft = data.length % oneFrameSize;
            if (dataLeft != 0) {
                // If the data size is not a multiple of one frame size, add zeros to the end of
                // the data
                byte[] newData = new byte[data.length + (oneFrameSize - dataLeft)];
                System.arraycopy(data, 0, newData, 0, data.length);
                data = newData;
            }
            // If the buffer space is insufficient, create a larger buffer
            if (buffer.remaining() < data.length) {
                // If the buffer space is insufficient, create a larger buffer
                int newCapacity = Math.max(buffer.capacity() * 2, buffer.position() + data.length);
                ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);

                // Copy the contents of the current buffer to the new buffer
                buffer.flip();
                newBuffer.put(buffer);

                // Switch to the new buffer and ensure it's in the correct write position
                buffer = newBuffer;
            }

            buffer.put(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get data from the cache
    public synchronized byte[] getData() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastSendTimestamp > INTERVAL_PCM_INTERRUPT)) {
            int cacheFrameCount = getBufferSize() / oneFrameSize;

            if (cacheFrameCount < START_BY_MIN_FRAME_SIZE) {
                return null;
            }

            int maxFrameSize = Math.min(cacheFrameCount, START_BY_MAX_FRAME_SIZE);
            startCacheDataSize = oneFrameSize * maxFrameSize;

            startedTimestamp = 0;
            lastSendTimestamp = 0;
            consumedFrameCount = 0;
        }

        try {
            if (startedTimestamp == 0) {
                if (getBufferSize() >= startCacheDataSize) {
                    startedTimestamp = currentTime;
                    return extractData(startCacheDataSize, currentTime);
                }
            } else {
                long elapsedTime = currentTime - startedTimestamp;

                if (elapsedTime < 0) {
                    startedTimestamp = lastSendTimestamp;
                    elapsedTime = currentTime - startedTimestamp;
                }

                int startedAllFrameCount = (int) (elapsedTime / INTERVAL_ONE_FRAME);
                int requiredFrameCount = startedAllFrameCount - consumedFrameCount;
                int wantedFrameCount = Math.min(requiredFrameCount, getBufferSize() / oneFrameSize);
                if (wantedFrameCount > 0) {
                    int requiredFrameSize = wantedFrameCount * oneFrameSize;
                    consumedFrameCount += requiredFrameCount;
                    if (consumedFrameCount < 0) {
                        consumedFrameCount = 0;
                        startedTimestamp = currentTime;
                    }
                    return extractData(requiredFrameSize, currentTime);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the remaining cache duration in milliseconds.
     *
     * @return the remaining cache duration in milliseconds.
     */
    public synchronized int getRemainingCacheDurationInMs() {
        // Calculate the number of frames in the buffer
        int cacheFrameCount = getBufferSize() / oneFrameSize;

        // Calculate and return the remaining cache duration in milliseconds
        return cacheFrameCount * INTERVAL_ONE_FRAME;
    }

    // Clear the cache
    public synchronized void clear() {
        buffer.clear();
        startedTimestamp = 0;
        lastSendTimestamp = 0;
        consumedFrameCount = 0;
    }

    // Helper method to extract data from the buffer
    private byte[] extractData(int size, long currentTime) {
        byte[] data = new byte[size];
        buffer.flip();
        buffer.get(data);
        buffer.compact();
        lastSendTimestamp = currentTime;
        return data;
    }

    private int getBufferSize() {
        // In write mode, position is the write position, and limit is the buffer's
        // capacity
        return buffer.position();
    }
}
