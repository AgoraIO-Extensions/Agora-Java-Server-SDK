package io.agora.rtc.example.ffmpegutils;

import io.agora.rtc.example.common.SampleLogger;

import java.time.Duration;
import java.time.Instant;

public class MediaDecodeUtils {
    private boolean started = false;
    private MediaDecode mediaDecode;
    private int interval;
    private String filePath;
    private int repeatCount;
    private MediaDecodeCallback callback;
    private DecodedMediaType decodedMediaType = DecodedMediaType.PCM_YUV;

    public enum DecodedMediaType {
        PCM_YUV,
        PCM_H264
    }

    public MediaDecodeUtils() {
        mediaDecode = new MediaDecode();
    }

    /*
     * Initialize media decode utils
     * 
     * @param filePath: file path
     * 
     * @param interval: interval between frames
     * 
     * @param repeatCount: repeat count, -1 for infinite
     * 
     * @param decodedMediaType: decoded media type
     * 
     * @param callback: media decode callback
     */

    public boolean init(String filePath, int interval, int repeatCount, DecodedMediaType decodedMediaType,
            MediaDecodeCallback callback) {
        boolean ret = false;
        try {
            ret = mediaDecode.open(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ret) {
            this.interval = interval;
            this.filePath = filePath;
            this.repeatCount = repeatCount;
            this.decodedMediaType = decodedMediaType;
            this.callback = callback;
        }
        return ret;
    }

    public long getMediaDuration() {
        return mediaDecode.getDuration();
    }

    public void start() {
        started = true;
        if (decodedMediaType == DecodedMediaType.PCM_YUV) {
            handleDecodedPcmYuv();
        } else if (decodedMediaType == DecodedMediaType.PCM_H264) {
            handleDecodedPcmH264();
        }
    }

    private void handleDecodedPcmYuv() {
        long firstPts = 0;
        int decodeCount = 1;
        Instant firstSendTime = Instant.now();
        try {
            SampleLogger.log("handleDecodedPcmYuv start duration:" + mediaDecode.getDuration());
            long basePts = 0;
            final long duration = mediaDecode.getDuration();
            while (started) {
                long totalSendTime = Duration.between(firstSendTime, Instant.now()).toMillis();
                MediaDecode.MediaFrame frame = mediaDecode.getFrame();
                SampleLogger.log("frame:" + frame);

                if (frame == null) {
                    SampleLogger.log("Finished reading file");
                    if (-1 != repeatCount && decodeCount >= repeatCount) {
                        break;
                    } else {
                        if (mediaDecode.isEndOfFileReached()) {
                            SampleLogger.log("Finished reading file");
                            decodeCount++;
                            mediaDecode.close();
                            mediaDecode.open(filePath);
                            firstPts = 0;
                            basePts += duration;
                            if (basePts < 0) {
                                basePts = 0;
                            }
                        }
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
                        frame.pts += basePts;
                        callback.onAudioFrame(frame);
                    }
                } else if (frame.frameType == MediaDecode.AVMediaType.VIDEO) {
                    if (frame.format != MediaDecode.AVPixelFormat.YUV420P) {
                        SampleLogger.log("Unsupported video format");
                        continue;
                    }

                    if (null != callback) {
                        frame.pts += basePts;
                        callback.onVideoFrame(frame);
                    }
                } else {
                    SampleLogger.log("Unsupported frame type");
                }
            }
        } catch (InterruptedException e) {
            SampleLogger.log("Decoding interrupted: " + e.getMessage());
        } finally {
            mediaDecode.close();
        }
        SampleLogger.log("handleDecodedPcmYuv end");
    }

    private void handleDecodedPcmH264() {
        long firstPts = 0;
        int decodeCount = 1;
        Instant firstSendTime = Instant.now();
        try {
            SampleLogger.log("handleDecodedPcmH264 start duration:" + mediaDecode.getDuration());
            while (started) {
                long totalSendTime = Duration.between(firstSendTime, Instant.now()).toMillis();
                MediaDecode.MediaPacket packet = mediaDecode.getPacket();
                SampleLogger.log("packet:" + packet);

                if (packet == null) {
                    if (-1 != repeatCount && decodeCount >= repeatCount) {
                        mediaDecode.close();
                        break;
                    } else {
                        if (mediaDecode.isEndOfFileReached()) {
                            SampleLogger.log("Finished reading file");
                            decodeCount++;
                            mediaDecode.close();
                            mediaDecode.open(filePath);
                            firstPts = 0;
                        }
                        continue;
                    }
                }

                // NOTICE: time stamp must be greater than 0
                // if time stamp is 0, system time will be used in sdk,
                // which will cause frame whose time is less than system time, to be dropped
                if (packet.pts <= 0) {
                    packet.pts = 1;
                }

                if (firstPts == 0) {
                    firstPts = packet.pts;
                    firstSendTime = Instant.now();
                    totalSendTime = 0;
                    Thread.sleep(interval);
                    SampleLogger.log("First pts: " + firstPts);
                }

                if (packet.pts - firstPts > totalSendTime) {
                    Thread.sleep(interval);
                }

                if (packet.mediaType == MediaDecode.AVMediaType.AUDIO) {
                    MediaDecode.MediaFrame frame = mediaDecode.decodePacket(packet);
                    mediaDecode.freePacket(packet);
                    SampleLogger.log("AUDIO frame:" + frame);
                    if (null != frame) {
                        if (frame.format != MediaDecode.AVSampleFormat.S16) {
                            SampleLogger.log("Unsupported audio format");
                            continue;
                        }

                        if (null != callback) {
                            callback.onAudioFrame(frame);
                        }
                    }
                } else if (packet.mediaType == MediaDecode.AVMediaType.VIDEO) {
                    MediaDecode.MediaPacket annexBPacket = mediaDecode.convertH264ToAnnexB(packet);
                    SampleLogger.log("VIDEO annexBPacket:" + annexBPacket);
                    if (annexBPacket == null) {
                        mediaDecode.freePacket(packet);
                        continue;
                    }
                    MediaDecode.MediaFrame frame = new MediaDecode.MediaFrame();
                    frame.buffer = annexBPacket.buffer;
                    frame.bufferSize = annexBPacket.buffer.length;
                    frame.pts = annexBPacket.pts;
                    frame.width = annexBPacket.width;
                    frame.height = annexBPacket.height;
                    frame.fps = annexBPacket.framerateNum / annexBPacket.framerateDen;
                    frame.isKeyFrame = (annexBPacket.flags & MediaDecode.AVPktFlag.KEY) != 0;

                    mediaDecode.freePacket(packet);

                    if (null != callback) {
                        callback.onVideoFrame(frame);
                    }
                } else {
                    SampleLogger.log("Unsupported frame type");
                    if (null != packet) {
                        mediaDecode.freePacket(packet);
                    }
                }
            }
        } catch (InterruptedException e) {
            SampleLogger.log("Decoding interrupted: " + e.getMessage());
        } finally {
            mediaDecode.close();
        }
        SampleLogger.log("handleDecodedPcmH264 end");
    }

    public interface MediaDecodeCallback {
        void onAudioFrame(MediaDecode.MediaFrame frame);

        void onVideoFrame(MediaDecode.MediaFrame frame);
    }

}
