package io.agora.rtc.example.common;

import java.util.Arrays;

import io.agora.rtc.AgoraLocalAudioTrack;
import io.agora.rtc.AgoraLocalUser;
import io.agora.rtc.AgoraLocalVideoTrack;
import io.agora.rtc.AgoraRemoteAudioTrack;
import io.agora.rtc.AgoraRemoteVideoTrack;
import io.agora.rtc.AudioVolumeInfo;
import io.agora.rtc.DefaultLocalUserObserver;
import io.agora.rtc.LocalAudioStats;
import io.agora.rtc.LocalVideoTrackStats;
import io.agora.rtc.RemoteAudioTrackStats;
import io.agora.rtc.RemoteVideoStreamInfo;
import io.agora.rtc.RemoteVideoTrackStats;
import io.agora.rtc.VideoTrackInfo;

public class SampleLocalUserObserver extends DefaultLocalUserObserver {

    public SampleLocalUserObserver() {
    }

    public void onUserAudioTrackStateChanged(AgoraLocalUser agoraLocalUser, String userId,
            AgoraRemoteAudioTrack agoraRemoteAudioTrack, int state, int reason, int elapsed) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onUserAudioTrackStateChanged success: userId = " + userId + ", state = " + state
                    + ", reason = " + reason + ", elapsed = " + elapsed);
        }
    }

    public void onUserAudioTrackSubscribed(AgoraLocalUser agoraLocalUser, String userId,
            AgoraRemoteAudioTrack agoraRemoteAudioTrack) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onUserAudioTrackSubscribed success " + userId + " " + agoraRemoteAudioTrack);
        }
    }

    public void onAudioSubscribeStateChanged(AgoraLocalUser agoraLocalUser, String channel, String userId,
            int oldState, int newState, int elapseSinceLastState) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onAudioSubscribeStateChanged success: channel = " + channel + ", userId = " + userId
                    + ", oldState = " + oldState + ", newState = " + newState + ", elapseSinceLastState = "
                    + elapseSinceLastState);
        }
    }

    @Override
    public void onUserVideoTrackStateChanged(AgoraLocalUser agoraLocalUser, String userId,
            AgoraRemoteVideoTrack agoraRemoteVideoTrack, int state, int reason, int elapsed) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger
                    .log("onUserVideoTrackStateChanged success " + userId + " " + state + " " + reason + " "
                            + elapsed);
        }
    }

    public void onUserVideoTrackSubscribed(AgoraLocalUser agoraLocalUser, String userId,
            VideoTrackInfo info, AgoraRemoteVideoTrack agoraRemoteVideoTrack) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onUserVideoTrackSubscribed success agoraRemoteVideoTrack:"
                    + agoraRemoteVideoTrack);
        }
    }

    public void onUserInfoUpdated(AgoraLocalUser agoraLocalUser, String userId, int msg, int val) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onUserInfoUpdated success " + userId + "   " + msg + "   " + val);
        }
    }

    public void onIntraRequestReceived(AgoraLocalUser agoraLocalUser) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onIntraRequestReceived success");
        }
    }

    public void onStreamMessage(AgoraLocalUser agoraLocalUser, String userId, int streamId, byte[] data) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onStreamMessage success " + userId + " " + streamId + " " + new String(data));
        }
    }

    public void onAudioTrackPublishSuccess(AgoraLocalUser agoraLocalUser,
            AgoraLocalAudioTrack agoraLocalAudioTrack) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onAudioTrackPublishSuccess agoraLocalAudioTrack:" + agoraLocalAudioTrack);
        }
    }

    public void onAudioTrackPublishStart(AgoraLocalUser agoraLocalUser,
            AgoraLocalAudioTrack agoraLocalAudioTrack) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onAudioTrackPublishStart agoraLocalAudioTrack:" + agoraLocalAudioTrack);
        }
    }

    public void onAudioTrackUnpublished(AgoraLocalUser agoraLocalUser,
            AgoraLocalAudioTrack agoraLocalAudioTrack) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onAudioTrackUnpublished agoraLocalAudioTrack:" + agoraLocalAudioTrack);
        }
    }

    public void onVideoTrackPublishStart(AgoraLocalUser agoraLocalUser,
            AgoraLocalVideoTrack agoraLocalVideoTrack) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onVideoTrackPublishStart agoraLocalVideoTrack:" + agoraLocalVideoTrack);
        }
    }

    public void onVideoTrackUnpublished(AgoraLocalUser agoraLocalUser,
            AgoraLocalVideoTrack agoraLocalVideoTrack) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onVideoTrackUnpublished agoraLocalVideoTrack:" + agoraLocalVideoTrack);
        }
    }

    public void onAudioTrackPublicationFailure(AgoraLocalUser agoraLocalUser,
            AgoraLocalAudioTrack agoraLocalAudioTrack, int error) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onAudioTrackPublicationFailure agoraLocalAudioTrack:" + agoraLocalAudioTrack
                    + " error:" + error);
        }
    }

    public void onLocalAudioTrackStateChanged(AgoraLocalUser agoraLocalUser,
            AgoraLocalAudioTrack agoraLocalAudioTrack, int state, int error) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onLocalAudioTrackStateChanged state:" + state + " error:" + error);
        }
    }

    public void onLocalAudioTrackStatistics(AgoraLocalUser agoraLocalUser, LocalAudioStats stats) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onLocalAudioTrackStatistics stats:" + stats);
        }
    }

    public void onRemoteAudioTrackStatistics(AgoraLocalUser agoraLocalUser,
            AgoraRemoteAudioTrack agoraRemoteAudioTrack, RemoteAudioTrackStats stats) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onRemoteAudioTrackStatistics stats:" + stats);
        }
    }

    public void onAudioPublishStateChanged(AgoraLocalUser agoraLocalUser, String channel, int oldState,
            int newState, int elapseSinceLastState) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onAudioPublishStateChanged channel:" + channel + " oldState:" + oldState + " newState:"
                    + newState + " elapseSinceLastState:" + elapseSinceLastState);
        }
    }

    public void onFirstRemoteAudioFrame(AgoraLocalUser agoraLocalUser, String userId, int elapsed) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onFirstRemoteAudioFrame userId:" + userId + " elapsed:" + elapsed);
        }
    }

    public void onFirstRemoteAudioDecoded(AgoraLocalUser agoraLocalUser, String userId, int elapsed) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onFirstRemoteAudioDecoded userId:" + userId + " elapsed:" + elapsed);
        }
    }

    public void onVideoTrackPublishSuccess(AgoraLocalUser agoraLocalUser,
            AgoraLocalVideoTrack agoraLocalVideoTrack) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onVideoTrackPublishSuccess agoraLocalVideoTrack:" + agoraLocalVideoTrack);
        }
    }

    public void onVideoTrackPublicationFailure(AgoraLocalUser agoraLocalUser,
            AgoraLocalVideoTrack agoraLocalVideoTrack, int error) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onVideoTrackPublicationFailure error:" + error);
        }
    }

    public void onLocalVideoTrackStateChanged(AgoraLocalUser agoraLocalUser,
            AgoraLocalVideoTrack agoraLocalVideoTrack, int state, int error) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onLocalVideoTrackStateChanged state:" + state + " error:" + error);
        }
    }

    public void onLocalVideoTrackStatistics(AgoraLocalUser agoraLocalUser,
            AgoraLocalVideoTrack agoraLocalVideoTrack, LocalVideoTrackStats stats) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onLocalVideoTrackStatistics stats:" + stats);
        }
    }

    public void onRemoteVideoTrackStatistics(AgoraLocalUser agoraLocalUser,
            AgoraRemoteVideoTrack agoraRemoteVideoTrack, RemoteVideoTrackStats stats) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onRemoteVideoTrackStatistics stats:" + stats);
        }
    }

    public void onAudioVolumeIndication(AgoraLocalUser agoraLocalUser, AudioVolumeInfo[] speakers,
            int totalVolume) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onAudioVolumeIndication speakers:" + Arrays.toString(speakers) + " totalVolume:"
                    + totalVolume);
        }
    }

    public void onActiveSpeaker(AgoraLocalUser agoraLocalUser, String userId) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onActiveSpeaker  " + userId);
        }
    }

    public void onRemoteVideoStreamInfoUpdated(AgoraLocalUser agoraLocalUser, RemoteVideoStreamInfo info) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onRemoteVideoStreamInfoUpdated info:" + info);
        }
    }

    public void onVideoSubscribeStateChanged(AgoraLocalUser agoraLocalUser, String channel, String userId,
            int oldState, int newState, int elapseSinceLastState) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onVideoSubscribeStateChanged channel:" + channel + " userId:" + userId + " oldState:"
                    + oldState + " newState:" + newState + " elapseSinceLastState:" + elapseSinceLastState);
        }
    }

    public void onVideoPublishStateChanged(AgoraLocalUser agoraLocalUser, String channel, int oldState,
            int newState, int elapseSinceLastState) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onVideoPublishStateChanged channel:" + channel + " oldState:" + oldState + " newState:"
                    + newState + " elapseSinceLastState:" + elapseSinceLastState);
        }
    }

    public void onFirstRemoteVideoFrame(AgoraLocalUser agoraLocalUser, String userId, int width, int height,
            int elapsed) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onFirstRemoteVideoFrame userId:" + userId + " width:" + width + " height:" + height
                    + " elapsed:" + elapsed);
        }
    }

    public void onFirstRemoteVideoDecoded(AgoraLocalUser agoraLocalUser, String userId, int width, int height,
            int elapsed) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onFirstRemoteVideoDecoded userId:" + userId + " width:" + width + " height:"
                    + height + " elapsed:" + elapsed);
        }
    }

    public void onFirstRemoteVideoFrameRendered(AgoraLocalUser agoraLocalUser, String userId, int width,
            int height,
            int elapsed) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onFirstRemoteVideoFrameRendered userId:" + userId + " width:" + width + " height:"
                    + height + " elapsed:" + elapsed);
        }
    }

    public void onVideoSizeChanged(AgoraLocalUser agoraLocalUser, String userId, int width, int height,
            int rotation) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onVideoSizeChanged userId:" + userId + " width:" + width + " height:" + height
                    + " rotation:" + rotation);
        }
    }

    public void onRemoteSubscribeFallbackToAudioOnly(AgoraLocalUser agoraLocalUser, String userId,
            int isFallbackOrRecover) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onRemoteSubscribeFallbackToAudioOnly userId:" + userId + " isFallbackOrRecover:"
                    + isFallbackOrRecover);
        }
    }

    public void onUserStateChanged(AgoraLocalUser agoraLocalUser, String userId, int state) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onUserStateChanged userId:" + userId + " state:" + state);
        }
    }

    public void onAudioMetaDataReceived(AgoraLocalUser agoraLocalUser, String userId, byte[] metaData) {
        if (ArgsConfig.isStressTest == 0) {
            SampleLogger.log("onAudioMetaDataReceived userId:" + userId + " metaData:" + new String(metaData));
        }
    }
}