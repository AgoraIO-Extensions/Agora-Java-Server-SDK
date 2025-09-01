package io.agora.rtc.example.utils;

import io.agora.rtc.example.common.SampleLogger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * AudioFrameManager (server-side utility).
 * <p>
 * Responsibilities:
 * - Uplink: parse client-generated PTS (3|18|10|1|32), detect session end by
 * LAST_CHUNK (explicit end), TIMEOUT (silence), or INTERRUPT (session id
 * switch).
 * - Downlink: generate PTS for the client (16|4|11|1|32) with internal
 * auto-increment
 * sessionId (4-bit, 1..15) and sentenceId (11-bit, 1..2047).
 * <p>
 * Thread-safety: all public methods are synchronized.
 */
public class AudioFrameManager {
    private static final long PLAYBACK_AUDIO_FRAME_MAX_TIMEOUT_MS = 500; // ms
    private static final long PLAYBACK_AUDIO_FRAME_MIN_TIMEOUT_MS = 200; // ms

    private ICallback mCallback;

    private final ScheduledExecutorService mScheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mFinishFuture;
    private final Object mLock = new Object();

    // Tracking for client uplink PTS (3|18|10|1|32)
    private Integer mLastUplinkSessionId; // 18-bit
    private int mLastSessionEndId = 0; // Deduplicate to avoid repeated callbacks

    // Counters for server-generated downlink PTS (version=2: 4|16|16|10|2|16)
    private int mDownlinkBasePts16 = 0; // 16-bit rolling accumulator [0..65535]
    private int mDownlinkSessionId16 = 1; // 16-bit [1..65535] auto-increment with wrap
    private int mDownlinkSentenceId16 = 1; // 16-bit [1..65535] auto-increment with wrap
    private int mDownlinkChunkId10 = 1; // 10-bit [1..1023] auto-increment with wrap

    private int mReceivedLastChunkDurationMs = 0;

    /**
     * Reasons for uplink session end notification.
     */
    public enum SessionEndReason {
        LAST_CHUNK,
        TIMEOUT,
        INTERRUPT,
        OTHER
    }

    /**
     * Callback interface for notifying session end.
     */
    public interface ICallback {
        /**
         * Invoked when a session ends.
         * 
         * @param sessionId the session id (from uplink PTS, normalized to int)
         * @param reason    the reason of ending (LAST_CHUNK, TIMEOUT, INTERRUPT, OTHER)
         */
        void onSessionEnd(int sessionId, SessionEndReason reason);
    }

    /**
     * Construct the manager with a callback.
     * 
     * @param callback the callback to receive session end events
     */
    public AudioFrameManager(ICallback callback) {
        this.mCallback = callback;
    }

    // ==================== Uplink: parse client PTS (3|18|10|1|32)
    // ====================

    /**
     * Parse client uplink PTS and apply end-of-session detection and timeout
     * scheduling.
     * Layout (MSB->LSB):
     * [3:version][18:sessionId][10:last_chunk_duration_ms][1:isSessionEnd][32:basePts]
     *
     * @param data       raw PCM (16-bit) used to compute frame duration in ms
     * @param sampleRate sample rate in Hz
     * @param channels   number of audio channels
     * @param pts        uplink PTS from client
     */
    public void processUplinkAudioFrame(byte[] data, int sampleRate, int channels, long pts) {
        if (pts == 0L) {
            SampleLogger.log("processUplinkAudioFrame pts is 0 and return");
            return;
        }

        // Parse bit fields (no locking needed)
        int version = (int) ((pts >>> 61) & 0x7L);
        int sessionId18 = (int) ((pts >>> 43) & 0x3FFFFL);
        int lastChunkDurationMs = (int) ((pts >>> 33) & 0x3FFL);
        boolean isSessionEnd = ((pts >>> 32) & 0x1L) != 0L;
        long basePts32 = (pts & 0xFFFF_FFFFL);

        int durationMs = pcmDurationMs(data, sampleRate, channels);

        SampleLogger.log(
                "processUplinkAudioFrame pts:" + pts + " version:" + version + " sessionId18:" + sessionId18
                        + " lastChunkDurationMs:"
                        + lastChunkDurationMs + " isSessionEnd:" + isSessionEnd + " basePts32:" + basePts32
                        + " durationMs:" + durationMs);

        // Determine actions under lock, perform callback outside
        boolean shouldCallback = false;
        int cbSessionId = 0;
        SessionEndReason cbReason = SessionEndReason.OTHER;
        long timeoutMs;

        synchronized (mLock) {
            // Cancel previous timeout
            if (mFinishFuture != null) {
                mFinishFuture.cancel(false);
                mFinishFuture = null;
            }

            // Session switch -> INTERRUPT previous
            if (mLastUplinkSessionId != null && !mLastUplinkSessionId.equals(sessionId18)) {
                // Previous session interrupted -> defer unified callback outside lock
                cbSessionId = mLastUplinkSessionId;
                cbReason = SessionEndReason.INTERRUPT;
                shouldCallback = true;
                // Switch to new session id
                mLastUplinkSessionId = sessionId18;
                mReceivedLastChunkDurationMs = 0;
                // No timeout for old session
                timeoutMs = 0L;
            } else {
                // Same session or first frame
                mLastUplinkSessionId = sessionId18;

                if (isSessionEnd) {
                    mReceivedLastChunkDurationMs += durationMs;
                    if (mReceivedLastChunkDurationMs >= lastChunkDurationMs) {
                        cbSessionId = mLastUplinkSessionId;
                        cbReason = SessionEndReason.LAST_CHUNK;
                        shouldCallback = true;
                        timeoutMs = 0L; // no further scheduling
                    } else {
                        timeoutMs = PLAYBACK_AUDIO_FRAME_MIN_TIMEOUT_MS;
                    }
                } else {
                    timeoutMs = PLAYBACK_AUDIO_FRAME_MAX_TIMEOUT_MS;
                }

                // Schedule timeout if needed
                if (timeoutMs > 0L) {
                    final Integer scheduledSession = mLastUplinkSessionId;
                    mFinishFuture = mScheduler.schedule(() -> {
                        int localSessionId = 0;
                        boolean doCallback = false;
                        synchronized (mLock) {
                            if (scheduledSession != null && scheduledSession.equals(mLastUplinkSessionId)
                                    && mLastSessionEndId != scheduledSession) {
                                localSessionId = scheduledSession;
                                // Defer state reset and callback to unified handler outside lock
                                doCallback = true;
                                mFinishFuture = null;
                            }
                        }
                        if (doCallback) {
                            callbackOnSessionEndInternal(localSessionId, SessionEndReason.TIMEOUT);
                        }
                    }, timeoutMs, TimeUnit.MILLISECONDS);
                }
            }
        }

        if (shouldCallback) {
            callbackOnSessionEndInternal(cbSessionId, cbReason);
        }
    }

    /**
     * Compute duration in milliseconds for a 16-bit PCM frame.
     */
    private int pcmDurationMs(byte[] data, int sampleRate, int channels) {
        if (data == null || sampleRate <= 0 || channels <= 0)
            return 0;
        // 16-bit PCM: bytesPerMs = (sampleRate * channels * 2) / 1000
        int bytesPerMs = (sampleRate * channels * 2) / 1000;
        if (bytesPerMs <= 0)
            return 0;
        return data.length / bytesPerMs;
    }

    /**
     * Notify session end with dedup and cleanup.
     */
    private void callbackOnSessionEndInternal(int sessionId, SessionEndReason reason) {
        ICallback cb;
        synchronized (mLock) {
            cb = mCallback;
            if (cb == null)
                return;
            // Deduplicate: ignore if the same session has been ended already
            if (sessionId == mLastSessionEndId) {
                return;
            }
            mLastSessionEndId = sessionId;
            mLastUplinkSessionId = null;
            mReceivedLastChunkDurationMs = 0;
            if (mFinishFuture != null) {
                mFinishFuture.cancel(false);
                mFinishFuture = null;
            }
        }
        // Invoke callback outside the lock to avoid blocking hot path
        cb.onSessionEnd(sessionId, reason);
    }

    // ==================== Downlink: generate client PTS (version=2:
    // 4|16|16|10|2|16)
    // ====================

    /**
     * Generate PTS for the client when version=2.
     * PTS layout (MSB->LSB):
     * [4:version=2][16:sessionId][16:sentenceId][10:chunkId][2:isEnd][16:basePts]
     * sessionId and sentenceId are auto-incremented internally starting from 1.
     * Rules:
     * - sentenceId increments per call and wraps within 1..65535 (back to 1).
     * - chunkId increments per call and wraps within 1..1023 (back to 1).
     * - basePts accumulates duration (ms) in 16-bit and wraps within 0..65535.
     * - When isSessionEnd == true: set isEnd=1; reset basePts to 0, chunkId to 0,
     * sentenceId to 1, and increment sessionId (wrap 1..65535).
     */
    public long generateDownlinkPts(byte[] data, int sampleRate, int channels, boolean isSessionEnd) {
        long pts;
        int durationMs = pcmDurationMs(data, sampleRate, channels);
        synchronized (mLock) {
            // Compose PTS according to version=2 bit layout
            pts = 0L;
            int version4 = 2;
            int isEnd2 = isSessionEnd ? 1 : 0; // encode into 2 bits
            pts |= ((long) (version4 & 0xF)) << 60; // high 4 bits
            pts |= ((long) (mDownlinkSessionId16 & 0xFFFF)) << 44; // next 16 bits
            pts |= ((long) (mDownlinkSentenceId16 & 0xFFFF)) << 28; // next 16 bits
            pts |= ((long) (mDownlinkChunkId10 & 0x3FF)) << 18; // next 10 bits
            pts |= ((long) (isEnd2 & 0x3)) << 16; // next 2 bits
            pts |= (long) (mDownlinkBasePts16 & 0xFFFF); // low 16 bits

            // Update counters
            if (isSessionEnd) {
                mDownlinkBasePts16 = 0;
                mDownlinkChunkId10 = 1;
                mDownlinkSentenceId16 = 1; // reset to 1 on session switch
                mDownlinkSessionId16 = (mDownlinkSessionId16 >= 0xFFFF) ? 1 : (mDownlinkSessionId16 + 1);
            } else {
                mDownlinkBasePts16 = (mDownlinkBasePts16 + (durationMs & 0xFFFF)) & 0xFFFF;
                mDownlinkChunkId10 = (mDownlinkChunkId10 >= 0x3FF) ? 1 : (mDownlinkChunkId10 + 1);
                // mDownlinkSentenceId16 = (mDownlinkSentenceId16 >= 0xFFFF) ? 1 :
                // (mDownlinkSentenceId16 + 1);
            }
        }
        return pts;
    }

    /**
     * Release resources and reset internal state.
     */
    public void release() {
        synchronized (mLock) {
            if (mFinishFuture != null) {
                mFinishFuture.cancel(false);
                mFinishFuture = null;
            }
            mLastUplinkSessionId = null;
            mLastSessionEndId = 0;
            mReceivedLastChunkDurationMs = 0;
            mDownlinkBasePts16 = 0;
            mDownlinkSessionId16 = 1;
            mDownlinkSentenceId16 = 1;
            mDownlinkChunkId10 = 1;
        }
        mScheduler.shutdownNow();
        mCallback = null;
    }
}
