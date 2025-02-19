package io.agora.rtc.example.common;

import java.util.Arrays;

import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AgoraRemoteAudioTrack;
import io.agora.rtc.AgoraRemoteVideoTrack;
import io.agora.rtc.AgoraLocalAudioTrack;
import io.agora.rtc.AgoraLocalVideoTrack;
import io.agora.rtc.LocalAudioStats;
import io.agora.rtc.RemoteAudioTrackStats;
import io.agora.rtc.VideoTrackInfo;
import io.agora.rtc.RemoteVideoTrackStats;
import io.agora.rtc.AudioVolumeInfo;
import io.agora.rtc.RemoteVideoStreamInfo;
import io.agora.rtc.LocalVideoTrackStats;
import io.agora.rtc.DefaultLocalUserObserver;

public class SampleLocalUserObserver extends DefaultLocalUserObserver {
    private AgoraLocalUser localUser;

    public SampleLocalUserObserver(AgoraLocalUser localUser) {
        this.localUser = localUser;
    }

    public void onUserAudioTrackStateChanged(AgoraLocalUser agoraLocalUser, String userId,
            AgoraRemoteAudioTrack agoraRemoteAudioTrack, int state, int reason, int elapsed) {
        SampleLogger.log("onUserAudioTrackStateChanged success: userId = " + userId + ", state = " + state
                + ", reason = " + reason + ", elapsed = " + elapsed);
    }

    public void onUserAudioTrackSubscribed(AgoraLocalUser agoraLocalUser, String userId,
            AgoraRemoteAudioTrack agoraRemoteAudioTrack) {
        SampleLogger.log("onUserAudioTrackSubscribed success " + userId + " " + agoraRemoteAudioTrack);
    }

    public void onAudioSubscribeStateChanged(AgoraLocalUser agoraLocalUser, String channel, String userId,
            int oldState, int newState, int elapseSinceLastState) {
        SampleLogger.log("onAudioSubscribeStateChanged success: channel = " + channel + ", userId = " + userId
                + ", oldState = " + oldState + ", newState = " + newState + ", elapseSinceLastState = "
                + elapseSinceLastState);
    }

    @Override
    public void onUserVideoTrackStateChanged(AgoraLocalUser agoraLocalUser, String userId,
            AgoraRemoteVideoTrack agoraRemoteVideoTrack, int state, int reason, int elapsed) {
        SampleLogger
                .log("onUserVideoTrackStateChanged success " + userId + " " + state + " " + reason + " " + elapsed);
    }

    public void onUserVideoTrackSubscribed(AgoraLocalUser agoraLocalUser, String userId,
            VideoTrackInfo info, AgoraRemoteVideoTrack agoraRemoteVideoTrack) {
        SampleLogger.log("onUserVideoTrackSubscribed success agoraRemoteVideoTrack:" + agoraRemoteVideoTrack);
    }

    public void onUserInfoUpdated(AgoraLocalUser agoraLocalUser, String userId, int msg, int val) {
        SampleLogger.log("onUserInfoUpdated success " + userId + "   " + msg + "   " + val);
    }

    public void onIntraRequestReceived(AgoraLocalUser agoraLocalUser) {
        SampleLogger.log("onIntraRequestReceived success");
    }

    public void onStreamMessage(AgoraLocalUser agoraLocalUser, String userId, int streamId, String data,
            long length) {
        SampleLogger.log("onStreamMessage success " + userId + " " + streamId + " " + data + " " + length);
    }

    public void onAudioTrackPublishSuccess(AgoraLocalUser agoraLocalUser,
            AgoraLocalAudioTrack agoraLocalAudioTrack) {
        SampleLogger.log("onAudioTrackPublishSuccess agoraLocalAudioTrack:" + agoraLocalAudioTrack);
    }

    public void onAudioTrackPublishStart(AgoraLocalUser agoraLocalUser,
            AgoraLocalAudioTrack agoraLocalAudioTrack) {
        SampleLogger.log("onAudioTrackPublishStart agoraLocalAudioTrack:" + agoraLocalAudioTrack);
    }

    public void onAudioTrackUnpublished(AgoraLocalUser agoraLocalUser,
            AgoraLocalAudioTrack agoraLocalAudioTrack) {
        SampleLogger.log("onAudioTrackUnpublished agoraLocalAudioTrack:" + agoraLocalAudioTrack);
    }

    public void onVideoTrackPublishStart(AgoraLocalUser agoraLocalUser,
            AgoraLocalVideoTrack agoraLocalVideoTrack) {
        SampleLogger.log("onVideoTrackPublishStart agoraLocalVideoTrack:" + agoraLocalVideoTrack);
    }

    public void onVideoTrackUnpublished(AgoraLocalUser agoraLocalUser,
            AgoraLocalVideoTrack agoraLocalVideoTrack) {
        SampleLogger.log("onVideoTrackUnpublished agoraLocalVideoTrack:" + agoraLocalVideoTrack);
    }

    public void onAudioTrackPublicationFailure(AgoraLocalUser agoraLocalUser,
            AgoraLocalAudioTrack agoraLocalAudioTrack, int error) {
        SampleLogger.log("onAudioTrackPublicationFailure agoraLocalAudioTrack:" + agoraLocalAudioTrack
                + " error:" + error);
    }

    public void onLocalAudioTrackStateChanged(AgoraLocalUser agoraLocalUser,
            AgoraLocalAudioTrack agoraLocalAudioTrack, int state, int error) {
        SampleLogger.log("onLocalAudioTrackStateChanged state:" + state + " error:" + error);
    }

    public void onLocalAudioTrackStatistics(AgoraLocalUser agoraLocalUser, LocalAudioStats stats) {
        SampleLogger.log("onLocalAudioTrackStatistics stats:" + stats);
    }

    public void onRemoteAudioTrackStatistics(AgoraLocalUser agoraLocalUser,
            AgoraRemoteAudioTrack agoraRemoteAudioTrack, RemoteAudioTrackStats stats) {
        SampleLogger.log("onRemoteAudioTrackStatistics stats:" + stats);
    }

    public void onAudioPublishStateChanged(AgoraLocalUser agoraLocalUser, String channel, int oldState,
            int newState, int elapseSinceLastState) {
        SampleLogger.log("onAudioPublishStateChanged channel:" + channel + " oldState:" + oldState + " newState:"
                + newState + " elapseSinceLastState:" + elapseSinceLastState);
    }

    public void onFirstRemoteAudioFrame(AgoraLocalUser agoraLocalUser, String userId, int elapsed) {
        SampleLogger.log("onFirstRemoteAudioFrame userId:" + userId + " elapsed:" + elapsed);
    }

    public void onFirstRemoteAudioDecoded(AgoraLocalUser agoraLocalUser, String userId, int elapsed) {
        SampleLogger.log("onFirstRemoteAudioDecoded userId:" + userId + " elapsed:" + elapsed);
    }

    public void onVideoTrackPublishSuccess(AgoraLocalUser agoraLocalUser,
            AgoraLocalVideoTrack agoraLocalVideoTrack) {
        SampleLogger.log("onVideoTrackPublishSuccess agoraLocalVideoTrack:" + agoraLocalVideoTrack);
    }

    public void onVideoTrackPublicationFailure(AgoraLocalUser agoraLocalUser,
            AgoraLocalVideoTrack agoraLocalVideoTrack, int error) {
        SampleLogger.log("onVideoTrackPublicationFailure error:" + error);
    }

    public void onLocalVideoTrackStateChanged(AgoraLocalUser agoraLocalUser,
            AgoraLocalVideoTrack agoraLocalVideoTrack, int state, int error) {
        SampleLogger.log("onLocalVideoTrackStateChanged state:" + state + " error:" + error);
    }

    public void onLocalVideoTrackStatistics(AgoraLocalUser agoraLocalUser,
            AgoraLocalVideoTrack agoraLocalVideoTrack, LocalVideoTrackStats stats) {
        SampleLogger.log("onLocalVideoTrackStatistics stats:" + stats);
    }

    public void onRemoteVideoTrackStatistics(AgoraLocalUser agoraLocalUser,
            AgoraRemoteVideoTrack agoraRemoteVideoTrack, RemoteVideoTrackStats stats) {
        SampleLogger.log("onRemoteVideoTrackStatistics stats:" + stats);
    }

    public void onAudioVolumeIndication(AgoraLocalUser agoraLocalUser, AudioVolumeInfo[] speakers,
            int totalVolume) {
        SampleLogger.log("onAudioVolumeIndication speakers:" + Arrays.toString(speakers) + " totalVolume:"
                + totalVolume);
    }

    public void onActiveSpeaker(AgoraLocalUser agoraLocalUser, String userId) {
        SampleLogger.log("onActiveSpeaker  " + userId);
    }

    public void onRemoteVideoStreamInfoUpdated(AgoraLocalUser agoraLocalUser, RemoteVideoStreamInfo info) {
        SampleLogger.log("onRemoteVideoStreamInfoUpdated info:" + info);
    }

    public void onVideoSubscribeStateChanged(AgoraLocalUser agoraLocalUser, String channel, String userId,
            int oldState, int newState, int elapseSinceLastState) {
        SampleLogger.log("onVideoSubscribeStateChanged channel:" + channel + " userId:" + userId + " oldState:"
                + oldState + " newState:" + newState + " elapseSinceLastState:" + elapseSinceLastState);
    }

    public void onVideoPublishStateChanged(AgoraLocalUser agoraLocalUser, String channel, int oldState,
            int newState, int elapseSinceLastState) {
        SampleLogger.log("onVideoPublishStateChanged channel:" + channel + " oldState:" + oldState + " newState:"
                + newState + " elapseSinceLastState:" + elapseSinceLastState);
    }

    public void onFirstRemoteVideoFrame(AgoraLocalUser agoraLocalUser, String userId, int width, int height,
            int elapsed) {
        SampleLogger.log("onFirstRemoteVideoFrame userId:" + userId + " width:" + width + " height:" + height
                + " elapsed:" + elapsed);
    }

    public void onFirstRemoteVideoDecoded(AgoraLocalUser agoraLocalUser, String userId, int width, int height,
            int elapsed) {
        SampleLogger.log("onFirstRemoteVideoDecoded userId:" + userId + " width:" + width + " height:"
                + height + " elapsed:" + elapsed);
    }

    public void onFirstRemoteVideoFrameRendered(AgoraLocalUser agoraLocalUser, String userId, int width,
            int height,
            int elapsed) {
        SampleLogger.log("onFirstRemoteVideoFrameRendered userId:" + userId + " width:" + width + " height:"
                + height + " elapsed:" + elapsed);
    }

    public void onVideoSizeChanged(AgoraLocalUser agoraLocalUser, String userId, int width, int height,
            int rotation) {
        SampleLogger.log("onVideoSizeChanged userId:" + userId + " width:" + width + " height:" + height
                + " rotation:" + rotation);
    }

    public void onRemoteSubscribeFallbackToAudioOnly(AgoraLocalUser agoraLocalUser, String userId,
            int isFallbackOrRecover) {
        SampleLogger.log("onRemoteSubscribeFallbackToAudioOnly userId:" + userId + " isFallbackOrRecover:"
                + isFallbackOrRecover);
    }

    public void onUserStateChanged(AgoraLocalUser agoraLocalUser, String userId, int state) {
        SampleLogger.log("onUserStateChanged userId:" + userId + " state:" + state);
    }

    public void onAudioMetaDataReceived(AgoraLocalUser agoraLocalUser, String userId, byte[] metaData) {
        SampleLogger.log("onAudioMetaDataReceived userId:" + userId + " metaData:" + new String(metaData));
    }
}