package io.agora.rtc.common;

import io.agora.rtc.IAudioEncodedFrameObserver;
import java.util.Arrays;

import io.agora.rtc.AgoraVideoEncodedFrameObserver;
import io.agora.rtc.ILocalUserObserver;
import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AgoraRemoteAudioTrack;
import io.agora.rtc.AgoraRemoteVideoTrack;
import io.agora.rtc.IMediaPacketReceiver;
import io.agora.rtc.IVideoEncodedFrameObserver;
import io.agora.rtc.IAudioFrameObserver;
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
import io.agora.rtc.AgoraVideoFrameObserver2;
import io.agora.rtc.IVideoFrameObserver2;

public class SampleLocalUserObserver extends DefaultLocalUserObserver {
    private AgoraLocalUser localUser;

    private AgoraRemoteAudioTrack remoteAudioTrack;
    private AgoraRemoteVideoTrack remoteVideoTrack;

    private IAudioFrameObserver audioFrameObserver;
    private IAudioEncodedFrameObserver audioEncodedFrameObserver;
    private AgoraVideoFrameObserver2 videoFrameObserver2;
    private AgoraVideoEncodedFrameObserver videoEncodedFrameObserver;

    private boolean isAudioRegistered = false;
    private boolean isVideoRegistered = false;

    public SampleLocalUserObserver(AgoraLocalUser localUser) {
        this.localUser = localUser;
    }

    public AgoraLocalUser GetLocalUser() {
        return localUser;
    }

    public AgoraRemoteAudioTrack GetRemoteAudioTrack() {
        return remoteAudioTrack;
    }

    public AgoraRemoteVideoTrack GetRemoteVideoTrack() {
        return remoteVideoTrack;
    }

    public void setAudioFrameObserver(IAudioFrameObserver observer) {
        audioFrameObserver = observer;
    }

    public void setAudioEncodedFrameObserver(IAudioEncodedFrameObserver observer) {
        audioEncodedFrameObserver = observer;
    }

    public void unsetAudioFrameObserver() {
        if (audioFrameObserver != null) {
            localUser.unregisterAudioFrameObserver();
        }

        if (remoteAudioTrack != null && audioEncodedFrameObserver != null) {
            localUser.unregisterAudioEncodedFrameObserver(audioEncodedFrameObserver);
            audioEncodedFrameObserver = null;
        }

        isAudioRegistered = false;
    }

    public void setVideoFrameObserver(IVideoFrameObserver2 observer) {
        videoFrameObserver2 = new AgoraVideoFrameObserver2(observer);
    }

    public void setVideoEncodedFrameObserver(IVideoEncodedFrameObserver observer) {
        videoEncodedFrameObserver = new AgoraVideoEncodedFrameObserver(observer);
    }

    public void unsetVideoFrameObserver() {
        if (remoteVideoTrack != null && videoFrameObserver2 != null) {
            localUser.unregisterVideoFrameObserver(videoFrameObserver2);
            videoFrameObserver2.destroy();
            videoFrameObserver2 = null;
        }

        if (remoteVideoTrack != null && videoEncodedFrameObserver != null) {
            localUser.unregisterVideoEncodedFrameObserver(videoEncodedFrameObserver);
            videoEncodedFrameObserver.destroy();
            videoEncodedFrameObserver = null;
        }

        isVideoRegistered = false;
    }

    public void onUserAudioTrackStateChanged(AgoraLocalUser agora_local_user, String user_id,
            AgoraRemoteAudioTrack agora_remote_audio_track, int state, int reason, int elapsed) {
        SampleLogger.log("onUserAudioTrackStateChanged success: user_id = " + user_id + ", state = " + state
                + ", reason = " + reason + ", elapsed = " + elapsed);
    }

    public synchronized void onUserAudioTrackSubscribed(AgoraLocalUser agora_local_user, String user_id,
            AgoraRemoteAudioTrack agora_remote_audio_track) {
        // lock
        SampleLogger.log("onUserAudioTrackSubscribed success " + user_id + " " + agora_remote_audio_track);
        remoteAudioTrack = agora_remote_audio_track;
        if (!isAudioRegistered) {
            isAudioRegistered = true;
            if (remoteAudioTrack != null && audioFrameObserver != null) {
                int res = localUser.registerAudioFrameObserver(audioFrameObserver);
                SampleLogger.log("registerAudioFrameObserver success:" + res);
            }

            if (remoteAudioTrack != null && audioEncodedFrameObserver != null) {
                int res = localUser.registerAudioEncodedFrameObserver(audioEncodedFrameObserver);
                SampleLogger.log("registerAudioEncodedFrameObserver success:" + res);
            }
        }
    }

    public void onAudioSubscribeStateChanged(AgoraLocalUser agora_local_user, String channel, String user_id,
            int old_state, int new_state, int elapse_since_last_state) {
        SampleLogger.log("onAudioSubscribeStateChanged success: channel = " + channel + ", user_id = " + user_id
                + ", old_state = " + old_state + ", new_state = " + new_state + ", elapse_since_last_state = "
                + elapse_since_last_state);
    }

    @Override
    public void onUserVideoTrackStateChanged(AgoraLocalUser agora_local_user, String user_id,
            AgoraRemoteVideoTrack agora_remote_video_track, int state, int reason, int elapsed) {
        SampleLogger
                .log("onUserVideoTrackStateChanged success " + user_id + " " + state + " " + reason + " " + elapsed);
    }

    public synchronized void onUserVideoTrackSubscribed(AgoraLocalUser agora_local_user, String user_id,
            VideoTrackInfo info, AgoraRemoteVideoTrack agora_remote_video_track) {
        // lock
        SampleLogger.log("onUserVideoTrackSubscribed success agora_remote_video_track:" + agora_remote_video_track);
        remoteVideoTrack = agora_remote_video_track;
        if (!isVideoRegistered) {
            isVideoRegistered = true;
            if (remoteVideoTrack != null && videoEncodedFrameObserver != null) {
                int res = localUser
                        .registerVideoEncodedFrameObserver(videoEncodedFrameObserver);
                SampleLogger.log("registerVideoEncodedImageReceiver success ... " + res);
            }

            if (remoteVideoTrack != null && videoFrameObserver2 != null) {
                localUser.registerVideoFrameObserver(videoFrameObserver2);
            }
        }
        SampleLogger.log("onUserVideoTrackSubscribed end");
    }

    public void onUserInfoUpdated(AgoraLocalUser agora_local_user, String user_id, int msg, int val) {
        SampleLogger.log("onUserInfoUpdated success " + user_id + "   " + msg + "   " + val);
    }

    public void onIntraRequestReceived(AgoraLocalUser agora_local_user) {
        SampleLogger.log("onIntraRequestReceived success");
    }

    public void onStreamMessage(AgoraLocalUser agora_local_user, String user_id, int stream_id, String data,
            long length) {
        SampleLogger.log("onStreamMessage success");
    }

    public void onAudioTrackPublishSuccess(AgoraLocalUser agora_local_user,
            AgoraLocalAudioTrack agora_local_audio_track) {
        SampleLogger.log("onAudioTrackPublishSuccess success");
    }

    public void onAudioTrackPublishStart(AgoraLocalUser agora_local_user,
            AgoraLocalAudioTrack agora_local_audio_track) {
        SampleLogger.log("onAudioTrackPublishStart success");
    }

    public void onAudioTrackUnpublished(AgoraLocalUser agora_local_user,
            AgoraLocalAudioTrack agora_local_audio_track) {
        SampleLogger.log("onAudioTrackUnpublished success");
    }

    public void onVideoTrackPublishStart(AgoraLocalUser agora_local_user,
            AgoraLocalVideoTrack agora_local_video_track) {
        SampleLogger.log("onVideoTrackPublishStart success");
    }

    public void onVideoTrackUnpublished(AgoraLocalUser agora_local_user,
            AgoraLocalVideoTrack agora_local_video_track) {
        SampleLogger.log("onVideoTrackUnpublished success");
    }

    public void onAudioTrackPublicationFailure(AgoraLocalUser agora_local_user,
            AgoraLocalAudioTrack agora_local_audio_track, int error) {
        SampleLogger.log("onAudioTrackPublicationFailure success");
    }

    public void onLocalAudioTrackStateChanged(AgoraLocalUser agora_local_user,
            AgoraLocalAudioTrack agora_local_audio_track, int state, int error) {
        SampleLogger.log("onLocalAudioTrackStateChanged success");
    }

    public void onLocalAudioTrackStatistics(AgoraLocalUser agora_local_user, LocalAudioStats stats) {
        SampleLogger.log("onLocalAudioTrackStatistics success");
    }

    public void onRemoteAudioTrackStatistics(AgoraLocalUser agora_local_user,
            AgoraRemoteAudioTrack agora_remote_audio_track, RemoteAudioTrackStats stats) {
        SampleLogger.log("onRemoteAudioTrackStatistics success");
    }

    public void onAudioPublishStateChanged(AgoraLocalUser agora_local_user, String channel, int old_state,
            int new_state, int elapse_since_last_state) {
        SampleLogger.log("onAudioPublishStateChanged success");
    }

    public void onFirstRemoteAudioFrame(AgoraLocalUser agora_local_user, String user_id, int elapsed) {
        SampleLogger.log("onFirstRemoteAudioFrame success");
    }

    public void onFirstRemoteAudioDecoded(AgoraLocalUser agora_local_user, String user_id, int elapsed) {
        SampleLogger.log("onFirstRemoteAudioDecoded success");
    }

    public void onVideoTrackPublishSuccess(AgoraLocalUser agora_local_user,
            AgoraLocalVideoTrack agora_local_video_track) {
        SampleLogger.log("onVideoTrackPublishSuccess success");
    }

    public void onVideoTrackPublicationFailure(AgoraLocalUser agora_local_user,
            AgoraLocalVideoTrack agora_local_video_track, int error) {
        SampleLogger.log("onVideoTrackPublicationFailure success");
    }

    public void onLocalVideoTrackStateChanged(AgoraLocalUser agora_local_user,
            AgoraLocalVideoTrack agora_local_video_track, int state, int error) {
        SampleLogger.log("onLocalVideoTrackStateChanged success");
    }

    public void onLocalVideoTrackStatistics(AgoraLocalUser agora_local_user,
            AgoraLocalVideoTrack agora_local_video_track, LocalVideoTrackStats stats) {
        SampleLogger.log("onLocalVideoTrackStatistics success");
    }

    public void onRemoteVideoTrackStatistics(AgoraLocalUser agora_local_user,
            AgoraRemoteVideoTrack agora_remote_video_track, RemoteVideoTrackStats stats) {
        SampleLogger.log("onRemoteVideoTrackStatistics success");
    }

    public void onAudioVolumeIndication(AgoraLocalUser agora_local_user, AudioVolumeInfo[] speakers,
            int total_volume) {
        SampleLogger.log("onAudioVolumeIndication success");
    }

    public void onActiveSpeaker(AgoraLocalUser agora_local_user, String userId) {
        SampleLogger.log("onActiveSpeaker success");
    }

    public void onRemoteVideoStreamInfoUpdated(AgoraLocalUser agora_local_user, RemoteVideoStreamInfo info) {
        SampleLogger.log("onRemoteVideoStreamInfoUpdated success");
    }

    public void onVideoSubscribeStateChanged(AgoraLocalUser agora_local_user, String channel, String user_id,
            int old_state, int new_state, int elapse_since_last_state) {
        SampleLogger.log("onVideoSubscribeStateChanged success");
    }

    public void onVideoPublishStateChanged(AgoraLocalUser agora_local_user, String channel, int old_state,
            int new_state, int elapse_since_last_state) {
        SampleLogger.log("onVideoPublishStateChanged success");
    }

    public void onFirstRemoteVideoFrame(AgoraLocalUser agora_local_user, String user_id, int width, int height,
            int elapsed) {
        SampleLogger.log("onFirstRemoteVideoFrame success");
    }

    public void onFirstRemoteVideoDecoded(AgoraLocalUser agora_local_user, String user_id, int width, int height,
            int elapsed) {
        SampleLogger.log("onFirstRemoteVideoDecoded success");
    }

    public void onFirstRemoteVideoFrameRendered(AgoraLocalUser agora_local_user, String user_id, int width,
            int height,
            int elapsed) {
        SampleLogger.log("onFirstRemoteVideoFrameRendered success");
    }

    public void onVideoSizeChanged(AgoraLocalUser agora_local_user, String user_id, int width, int height,
            int rotation) {
        SampleLogger.log("onVideoSizeChanged success");
    }

    public void onRemoteSubscribeFallbackToAudioOnly(AgoraLocalUser agora_local_user, String user_id,
            int is_fallback_or_recover) {
        SampleLogger.log("onRemoteSubscribeFallbackToAudioOnly success");
    }

    public void onUserStateChanged(AgoraLocalUser agora_local_user, String user_id, int state) {
        SampleLogger.log("onUserStateChanged success");
    }
}