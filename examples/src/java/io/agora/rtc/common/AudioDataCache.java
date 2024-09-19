package io.agora.rtc.common;

import java.util.LinkedList;
import java.util.Queue;

public class AudioDataCache {
    // Constants
    private static final int START_BY_CACHE_FRAME_SIZE = 18;
    private static final int CONTINUE_BY_CACHE_FRAME_SIZE = 6;
    private static final int INTERVAL_ONE_FRAME = 10; // ms
    private static final int INTERVAL_PCM_INTERRUPT = 200; // ms

    // Instance variables
    private final int numOfChannels;
    private final int sampleRate;
    private final int oneFrameSize;
    private int startCacheDataSize;
    private final Queue<Byte> cacheQueue;
    private long startedTimestamp;
    private long lastSendTimestamp;
    private long lastPushDataTimestamp;
    private int consumedFrameSize;

    // Constructor
    public AudioDataCache(int numOfChannels, int sampleRate) {
        this.numOfChannels = numOfChannels;
        this.sampleRate = sampleRate;
        this.oneFrameSize = numOfChannels * (sampleRate / 1000) * INTERVAL_ONE_FRAME * 2;
        this.startCacheDataSize = oneFrameSize * START_BY_CACHE_FRAME_SIZE;
        this.cacheQueue = new LinkedList<>();
        this.startedTimestamp = 0;
        this.lastSendTimestamp = 0;
        this.lastPushDataTimestamp = 0;
        this.consumedFrameSize = 0;
    }

    public int getOneFrameSize() {
        return oneFrameSize;
    }

    // Calculate samples per channel
    public int getSamplesPerChannel(int dataSize) {
        return sampleRate / 1000 * INTERVAL_ONE_FRAME * (dataSize / oneFrameSize);
    }

    // Push data into the cache
    public synchronized void pushData(byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }
        try {
            if (startedTimestamp != 0
                    && (System.currentTimeMillis() - lastPushDataTimestamp > INTERVAL_PCM_INTERRUPT)) {
                startCacheDataSize = oneFrameSize * CONTINUE_BY_CACHE_FRAME_SIZE;
                startedTimestamp = 0;
                lastSendTimestamp = 0;
                consumedFrameSize = 0;
            }
            for (byte b : data) {
                cacheQueue.offer(b);
            }
            lastPushDataTimestamp = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get data from the cache
    public synchronized byte[] getData() {
        long currentTime = System.currentTimeMillis();
        try {
            if (startedTimestamp == 0) {
                if (cacheQueue.size() >= startCacheDataSize) {
                    startedTimestamp = currentTime;
                    return extractData(startCacheDataSize, currentTime);
                }
            } else {
                long elapsedTime = currentTime - startedTimestamp;

                if (elapsedTime < 0) {
                    startedTimestamp = lastSendTimestamp;
                    elapsedTime = currentTime - startedTimestamp;
                }

                int startedAllFrameSize = (int) (elapsedTime / INTERVAL_ONE_FRAME);
                int requiredFrameSize = startedAllFrameSize - consumedFrameSize;
                if (requiredFrameSize > 0) {
                    int requiredSize = requiredFrameSize * oneFrameSize;
                    if (requiredSize <= cacheQueue.size()) {
                        consumedFrameSize += requiredFrameSize;
                        if (consumedFrameSize < 0) {
                            consumedFrameSize = 0;
                            startedTimestamp = currentTime;
                        }
                        return extractData(requiredSize, currentTime);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Helper method to extract data from the queue
    private byte[] extractData(int size, long currentTime) {
        byte[] data = new byte[size];
        try {
            for (int i = 0; i < size; i++) {
                data[i] = cacheQueue.poll();
            }
            lastSendTimestamp = currentTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
