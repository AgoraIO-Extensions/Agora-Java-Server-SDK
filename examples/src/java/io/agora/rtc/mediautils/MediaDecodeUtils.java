package io.agora.rtc.mediautils;

import io.agora.rtc.common.SampleLogger;
import java.time.Duration;
import java.time.Instant;

public class MediaDecodeUtils {
    private boolean started = false;
    private MediaDecode mediaDecode;
    private int interval;
    private String filePath;
    private int repeatCount;
    private MediaDecodeCallback callback;

    public MediaDecodeUtils() {
        mediaDecode = new MediaDecode();
    }

    public boolean init(String filePath, int interval, int repeatCount, MediaDecodeCallback callback) {
        boolean ret = false;
        try {
            ret = mediaDecode.open(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ret) {
            this.interval = interval;
            this.filePath = filePath;
            if (repeatCount <= 1) {
                repeatCount = 1;
            } else {
                this.repeatCount = repeatCount;
            }
            this.callback = callback;
        }
        return ret;
    }

    public long getMediaDuration() {
        return mediaDecode.getDuration();
    }

    public void start() {
        started = true;
        long firstPts = 0;
        int decodeCount = 1;
        Instant firstSendTime = Instant.now();
        try {
            SampleLogger.log("MediaDecodeUtils start");
            final long mediaDuration = mediaDecode.getDuration();
            long basePtsForLoop = 0;
            while (started) {
                long totalSendTime = Duration.between(firstSendTime, Instant.now()).toMillis();
                MediaDecode.MediaFrame frame = mediaDecode.getFrame();

                SampleLogger.log("frame:" + frame);

                if (frame == null) {
                    SampleLogger.log("Finished reading file");
                    if (decodeCount >= repeatCount) {
                        break;
                    } else {
                        decodeCount++;
                        mediaDecode.close();
                        mediaDecode.open(filePath);
                        firstPts = 0;
                        basePtsForLoop += mediaDuration;
                        continue;
                    }
                }

                // NOTICE: time stamp must be greater than 0
                // if time stamp is 0, system time will be used in sdk,
                // which will cause frame whose time is less than system time, to be dropped
                if (frame.pts <= 0) {
                    frame.pts = 1;
                }

                if (firstPts == 0) {
                    firstPts = frame.pts;
                    firstSendTime = Instant.now();
                    totalSendTime = 0;
                    Thread.sleep(interval);
                    SampleLogger.log("First pts: " + firstPts);
                }

                if (frame.pts - firstPts > totalSendTime) {
                    Thread.sleep(interval);
                }

                if (frame.frameType == MediaDecode.AVMediaType.AUDIO) {
                    if (frame.format != MediaDecode.AVSampleFormat.S16) {
                        SampleLogger.log("Unsupported audio format");
                        continue;
                    }

                    if (null != callback) {
                        callback.onAudioFrame(frame, basePtsForLoop);
                    }
                } else if (frame.frameType == MediaDecode.AVMediaType.VIDEO) {
                    if (frame.format != MediaDecode.AVPixelFormat.YUV420P) {
                        SampleLogger.log("Unsupported video format");
                        continue;
                    }

                    if (null != callback) {
                        callback.onVideoFrame(frame, basePtsForLoop);
                    }
                }
            }
        } catch (InterruptedException e) {
            SampleLogger.log("Decoding interrupted: " + e.getMessage());
        } finally {
            mediaDecode.close();
        }
        SampleLogger.log("MediaDecodeUtils end");
    }

    public interface MediaDecodeCallback {
        void onAudioFrame(MediaDecode.MediaFrame frame, long basePts);

        void onVideoFrame(MediaDecode.MediaFrame frame, long basePts);
    }

}
