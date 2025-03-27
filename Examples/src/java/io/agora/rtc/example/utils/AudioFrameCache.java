package io.agora.rtc.example.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AudioFrameCache {
    // Constants
    private static final int START_BY_MAX_FRAME_SIZE = 18;
    private static final int START_BY_MIN_FRAME_SIZE = 2;
    private static final int INTERVAL_ONE_FRAME = 20; // ms
    private static final int INTERVAL_PCM_INTERRUPT = 200; // ms

    // Instance variables
    private final int numOfChannels;
    private final int sampleRate;
    private final int samplesPerChannel;
    private final int codec;

    private final ConcurrentLinkedQueue<Frame> queue;
    private final AtomicLong startedTimestamp;
    private final AtomicLong lastSendTimestamp;
    private final AtomicInteger consumedFrameCount;

    // Constructor
    public AudioFrameCache(int numOfChannels, int sampleRate, int samplesPerChannel, int codec) {
        this.numOfChannels = numOfChannels;
        this.sampleRate = sampleRate;
        this.samplesPerChannel = samplesPerChannel;
        this.codec = codec;

        this.queue = new ConcurrentLinkedQueue<>();
        this.startedTimestamp = new AtomicLong(0);
        this.lastSendTimestamp = new AtomicLong(0);
        this.consumedFrameCount = new AtomicInteger(0);
    }

    public int getNumOfChannels() {
        return numOfChannels;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getSamplesPerChannel() {
        return samplesPerChannel;
    }

    public int getCodec() {
        return codec;
    }

    // Push frame into the cache
    public void pushFrame(Frame frame) {
        if (isValidFrame(frame)) {
            queue.offer(frame);
        }
    }

    private boolean isValidFrame(Frame frame) {
        return frame != null && frame.getData() != null && frame.getData().length > 0;
    }

    // Get data from the cache
    public synchronized Frame[] getFrames() {
        try {
            long currentTime = System.currentTimeMillis();
            int startCacheFrameCount = 0;
            if ((currentTime - lastSendTimestamp.get() > INTERVAL_PCM_INTERRUPT)) {
                int cacheFrameCount = getQueueSize();

                if (cacheFrameCount < START_BY_MIN_FRAME_SIZE) {
                    return null;
                }

                startCacheFrameCount = Math.min(cacheFrameCount, START_BY_MAX_FRAME_SIZE);

                resetTimestamps();
            }

            if (startedTimestamp.get() == 0) {
                if (getQueueSize() >= startCacheFrameCount) {
                    startedTimestamp.set(currentTime);
                    return extractData(1, currentTime);
                }
            } else {
                long elapsedTime = currentTime - startedTimestamp.get();

                if (elapsedTime < 0) {
                    startedTimestamp.set(lastSendTimestamp.get());
                    elapsedTime = currentTime - startedTimestamp.get();
                }

                int startedAllFrameCount = (int) (elapsedTime / INTERVAL_ONE_FRAME);
                int requiredFrameCount = startedAllFrameCount - consumedFrameCount.get();
                int wantedFrameCount = Math.min(requiredFrameCount, getQueueSize());
                if (wantedFrameCount > 0) {
                    int newCount = consumedFrameCount.addAndGet(requiredFrameCount);
                    if (newCount < 0) {
                        consumedFrameCount.set(0);
                        startedTimestamp.set(currentTime);
                    }
                    return extractData(wantedFrameCount, currentTime);
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
    public int getRemainingCacheFrameCount() {
        return queue.size();
    }

    // Clear the cache
    public synchronized void clear() {
        queue.clear();
        resetTimestamps();
    }

    private void resetTimestamps() {
        startedTimestamp.set(0);
        lastSendTimestamp.set(0);
        consumedFrameCount.set(0);
    }

    // Helper method to extract data from the buffer
    private Frame[] extractData(int frameCount, long currentTime) {
        if (frameCount <= 0 || queue.isEmpty()) {
            return null;
        }

        lastSendTimestamp.set(currentTime);

        List<Frame> result = new ArrayList<>(frameCount);

        for (int i = 0; i < frameCount && !queue.isEmpty(); i++) {
            Frame frame = queue.poll();
            if (frame != null) {
                result.add(frame);
            }
        }

        return result.isEmpty() ? null : result.toArray(new Frame[0]);
    }

    private int getQueueSize() {
        return queue.size();
    }

    public static class Frame {
        private final byte[] data;

        public Frame(byte[] data) {
            this.data = data != null ? data.clone() : null;
        }

        public byte[] getData() {
            return data != null ? data.clone() : null;
        }
    }
}
