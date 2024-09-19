package io.agora.rtc.common;

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

    private IVideoEncodedFrameObserver videoEncodedReceiver;
    private IAudioFrameObserver audioFrameObserver;
    private IVideoFrameObserver2 videoFrameObserver;

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

    // public synchronized void setMediaPacketReceiver(IMediaPacketReceiver
    // receiver) { // lock
    // mediaPacketReceiver = receiver;
    // if (remoteAudioTrack != null) {
    // remoteAudioTrack.registerMediaPacketReceiver(mediaPacketReceiver);
    // }
    // if (remoteVideoTrack != null) {
    // remoteVideoTrack.registerMediaPacketReceiver(mediaPacketReceiver);
    // }
    // }

    public void setVideoEncodedFrameObserver(IVideoEncodedFrameObserver observer) {
        videoEncodedReceiver = observer;
    }

    public void setAudioFrameObserver(IAudioFrameObserver observer) {
        audioFrameObserver = observer;
    }

    public void unsetAudioFrameObserver() {
        if (audioFrameObserver != null) {
            localUser.unregisterAudioFrameObserver();
        }
    }

    public void setVideoFrameObserver(IVideoFrameObserver2 observer) {
        videoFrameObserver = observer;
    }

    public void unsetVideoFrameObserver() {
        if (remoteVideoTrack != null && videoFrameObserver != null) {
            localUser.unregisterVideoFrameObserver(new AgoraVideoFrameObserver2(videoFrameObserver));
        }
    }

    public void onUserAudioTrackStateChanged(AgoraLocalUser agora_local_user, String user_id,
            AgoraRemoteAudioTrack agora_remote_audio_track, int state, int reason, int elapsed) {

        SampleLogger.log("onUserAudioTrackStateChanged success: user_id = " + user_id + ", state = " + state
                + ", reason = " + reason + ", elapsed = " + elapsed);
    }

    public synchronized void onUserAudioTrackSubscribed(AgoraLocalUser agora_local_user, String user_id,
            AgoraRemoteAudioTrack agora_remote_audio_track) {
        // lock
        SampleLogger.log("onUserAudioTrackSubscribed success " + user_id);
        remoteAudioTrack = agora_remote_audio_track;
        if (remoteAudioTrack != null && audioFrameObserver != null) {
            int res = localUser.registerAudioFrameObserver(audioFrameObserver);
            SampleLogger.log("registerAudioFrameObserver success" + res);
        }
    }

    public void onAudioSubscribeStateChanged(AgoraLocalUser agora_local_user, String channel, String user_id,
            int old_state, int new_state, int elapse_since_last_state) {
        SampleLogger.log("onAudioSubscribeStateChanged success: channel = " + channel + ", user_id = " + user_id
                + ", old_state = " + old_state + ", new_state = " + new_state + ", elapse_since_last_state = "
                + elapse_since_last_state);
    }

    public synchronized void onUserVideoTrackSubscribed(AgoraLocalUser agora_local_user, String user_id,
            VideoTrackInfo info, AgoraRemoteVideoTrack agora_remote_video_track) {
        // lock
        SampleLogger.log("onUserVideoTrackSubscribed success");
        remoteVideoTrack = agora_remote_video_track;
        if (remoteVideoTrack != null && videoEncodedReceiver != null) {
            int res = localUser
                    .registerVideoEncodedFrameObserver(new AgoraVideoEncodedFrameObserver(videoEncodedReceiver));
            SampleLogger.log("registerVideoEncodedImageReceiver success ... " + res);
        }
        // if (remoteVideoTrack != null && mediaPacketReceiver != null) {
        // remoteVideoTrack.registerMediaPacketReceiver(mediaPacketReceiver);
        // }
        if (remoteVideoTrack != null && videoFrameObserver != null) {
            localUser.registerVideoFrameObserver(new AgoraVideoFrameObserver2(videoFrameObserver));
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
}