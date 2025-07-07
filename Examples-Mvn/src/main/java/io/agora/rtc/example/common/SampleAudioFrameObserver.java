package io.agora.rtc.example.common;

import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.IAudioFrameObserver;
import io.agora.rtc.VadProcessResult;
import java.io.FileOutputStream;
import io.agora.rtc.example.utils.Utils;

public class SampleAudioFrameObserver extends FileWriter implements IAudioFrameObserver {

    private String outputFilePath = "";

    public SampleAudioFrameObserver(String outputFilePath) {
        super(outputFilePath);
        Utils.deleteAllFile(outputFilePath);
        this.outputFilePath = outputFilePath;
    }

    /**
     * Note: To improve data transmission efficiency, the buffer of the frame object
     * is a DirectByteBuffer.
     * Be sure to extract the byte array value in the callback synchronously and
     * then transfer it to the asynchronous thread for processing.
     * You can refer to {@link io.agora.rtc.utils.Utils#getBytes(ByteBuffer)}.
     * 
     * @param agoraLocalUser the local user
     * @param channelId      the channel id
     * @param frame          the audio frame
     * @return 0/1, the return value currently has no practical significance
     */
    @Override
    public int onRecordAudioFrame(AgoraLocalUser agoraLocalUser, String channelId, AudioFrame frame) {
        return 1;
    }

    /**
     * Note: To improve data transmission efficiency, the buffer of the frame object
     * is a DirectByteBuffer.
     * Be sure to extract the byte array value in the callback synchronously and
     * then transfer it to the asynchronous thread for processing.
     * You can refer to {@link io.agora.rtc.utils.Utils#getBytes(ByteBuffer)}.
     * 
     * @param agoraLocalUser the local user
     * @param channelId      the channel id
     * @param frame          the audio frame
     * @return 0/1, the return value currently has no practical significance
     */
    @Override
    public int onPlaybackAudioFrame(AgoraLocalUser agoraLocalUser, String channelId, AudioFrame frame) {
        return 1;
    }

    /**
     * Note: To improve data transmission efficiency, the buffer of the frame object
     * is a DirectByteBuffer.
     * Be sure to extract the byte array value in the callback synchronously and
     * then transfer it to the asynchronous thread for processing.
     * You can refer to {@link io.agora.rtc.utils.Utils#getBytes(ByteBuffer)}.
     */
    @Override
    public int onMixedAudioFrame(AgoraLocalUser agoraLocalUser, String channelId, AudioFrame frame) {
        return 1;
    }

    /**
     * Note: To improve data transmission efficiency, the buffer of the frame object
     * is a DirectByteBuffer.
     * Be sure to extract the byte array value in the callback synchronously and
     * then transfer it to the asynchronous thread for processing.
     * You can refer to {@link io.agora.rtc.utils.Utils#getBytes(ByteBuffer)}.
     * 
     * @param agoraLocalUser the local user
     * @param frame          the audio frame
     * @return 0/1, the return value currently has no practical significance
     */
    @Override
    public int onEarMonitoringAudioFrame(AgoraLocalUser agoraLocalUser, AudioFrame frame) {
        return 1;
    }

    /**
     * Note: To improve data transmission efficiency, the buffer of the frame object
     * is a DirectByteBuffer.
     * Be sure to extract the byte array value in the callback synchronously and
     * then transfer it to the asynchronous thread for processing.
     * You can refer to {@link io.agora.rtc.utils.Utils#getBytes(ByteBuffer)}.
     * 
     * @param agoraLocalUser the local user
     * @param channelId      the channel id
     * @param userId         the user id
     * @param frame          the audio frame
     * @param vadResult      the vad result
     * @return 0/1, the return value currently has no practical significance
     */
    @Override
    public int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agoraLocalUser, String channelId, String userId,
            AudioFrame frame, VadProcessResult vadResult) {
        return 0;
    }

    @Override
    public int getObservedAudioFramePosition() {
        return 15;
    }

    public void writeAudioFrameToFile(byte[] buffer) {
        if ("".equals(outputFilePath.trim()) || buffer == null || buffer.length == 0) {
            return;
        }

        try {
            writeData(buffer, buffer.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeAudioFrameToFile(byte[] buffer, String file) {
        if (buffer == null || buffer.length == 0) {
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(file, true)) {
            fos.write(buffer);
            fos.flush();
        } catch (Exception e) {
            SampleLogger.log("Open file fail");
        }
    }
}
