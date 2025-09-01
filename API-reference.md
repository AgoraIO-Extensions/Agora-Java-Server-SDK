# Agora Server SDK Java API Reference

This document provides a reference for the main classes and methods of the Agora Server SDK Java API.

## Table of Contents

- [Core Classes](#core-classes)
  - [AgoraService](#agoraservice)
  - [AgoraRtcConn](#agorartcconn)
  - [AgoraLocalUser](#agoralocaluser)
  - [AgoraParameter](#agoraparameter)
  - [AgoraAudioProcessor](#agoraaudioprocessor)
  - [AgoraAudioVad](#agoraaudiovad)
  - [AgoraAudioVadV2](#agoraaudiovadev2)
- [Observer Interfaces](#observer-interfaces)
  - [IRtcConnObserver](#irtcconnobserver)
  - [ILocalUserObserver](#ilocaluserobserver)
  - [INetworkObserver](#inetworkobserver)
  - [IVideoFrameObserver2](#ivideoframeobserver2)
  - [IAudioFrameObserver](#iaudioframeobserver)
  - [IAudioEncodedFrameObserver](#iaudioencodedframeobserver)
  - [IVideoEncodedFrameObserver](#ivideoencodedframeobserver)
  - [IAgoraAudioProcessorEventHandler](#iagoraaudioprocessoreventhandler)
- [Data Structures](#data-structures)
  - [AgoraServiceConfig](#agoraserviceconfig)
  - [RtcConnConfig](#rtcconnconfig)
  - [RtcConnPublishConfig](#rtcconnpublishconfig)
  - [RtcConnInfo](#rtcconninfo)
  - [VideoEncoderConfig](#videoencoderconfig)
  - [VideoSubscriptionOptions](#videosubscriptionoptions)
  - [SimulcastStreamConfig](#simulcaststreamconfig)
  - [EncodedVideoFrameInfo](#encodedvideoframeinfo)
  - [EncodedAudioFrameInfo](#encodedaudioframeinfo)
  - [EncodedAudioFrameReceiverInfo](#encodedaudioframereceiverinfo)
  - [SenderOptions](#senderoptions)
  - [VideoFrame](#videoframe)
  - [AudioFrame](#audioframe)
  - [ExternalVideoFrame](#externalvideoframe)
  - [VideoDimensions](#videodimensions)
  - [RtcStats](#rtcstats)
  - [UserInfo](#userinfo)
  - [VadProcessResult](#vadprocessresult)
  - [AgoraAudioVadConfigV2](#agoraaudiovadconfigv2)
  - [AgoraAudioVadConfig](#agoraaudiovadconfig)
  - [LocalAudioTrackStats](#localaudiotrackstats)
  - [LocalVideoTrackStats](#localvideotrackstats)
  - [RemoteAudioTrackStats](#remoteaudiotrackstats)
  - [RemoteVideoTrackStats](#remotevideotrackstats)
  - [EncryptionConfig](#encryptionconfig)
  - [UplinkNetworkInfo](#uplinknetworkinfo)
  - [DownlinkNetworkInfo](#downlinknetworkinfo)
  - [PeerDownlinkInfo](#peerdownlinkinfo)
  - [AecConfig](#aecconfig)
  - [AnsConfig](#ansconfig)
  - [AgcConfig](#agcconfig)
  - [BghvsConfig](#bghvsconfig)
  - [AgoraAudioProcessorConfig](#agoraaudioprocessorconfig)
  - [AgoraAudioFrame](#agoraaudioframe)
- [Utility Classes](#utility-classes)
  - [VadDumpUtils](#vaddumputils)

---

## Core Classes

### AgoraService
The `AgoraService` class is the entry point of the Agora Server SDK. It provides the main methods for initializing and managing the SDK.

#### Methods

##### `getSdkVersion()`
Gets the SDK version.
- **Returns:** `String` - The SDK version.

##### `initialize(AgoraServiceConfig config)`
Initializes the Agora service with the specified configuration.
- **Parameters:**
  - `config`: `AgoraServiceConfig` - The configuration for the Agora service.
- **Returns:** `int` - 0 for success, < 0 for failure.

##### `destroy()`
Destroys the Agora service and releases all resources.

##### `setLogFile(String filePath, int fileSize)`
Sets the log file path and size.
- **Parameters:**
  - `filePath`: `String` - The path of the log file.
  - `fileSize`: `int` - The maximum size of the log file in bytes.
- **Returns:** `int` - 0 for success, < 0 for failure.

##### `setLogFilter(int filters)`
Sets the log filter level.
- **Parameters:**
  - `filters`: `int` - The log filter level.
- **Returns:** `int` - 0 for success, < 0 for failure.

##### `createRtcConn(RtcConnConfig rtcConnConfig)`
Creates an `AgoraRtcConn` object.
- **Parameters:**
  - `rtcConnConfig`: `RtcConnConfig` - The configuration for the RTC connection.
- **Returns:** `AgoraRtcConn` - The created `AgoraRtcConn` object.

##### `getAgoraParameter()`
Gets the `AgoraParameter` object for setting SDK parameters.
- **Returns:** `AgoraParameter` - The `AgoraParameter` object.

### AgoraRtcConn
The `AgoraRtcConn` class represents an RTC connection.

#### Methods

##### `destroy()`
Destroys the `AgoraRtcConn` instance and releases all associated resources.

##### `registerObserver(IRtcConnObserver observer)`
Registers an `IRtcConnObserver` to receive connection-related events.
- **Parameters:**
  - `observer`: `IRtcConnObserver` - The observer to register.
- **Returns:** `int` - 0 for success, < 0 for failure.

##### `registerNetworkObserver(INetworkObserver observer)`
Registers an `INetworkObserver` to receive network-related events.
- **Parameters:**
  - `observer`: `INetworkObserver` - The observer to register.
- **Returns:** `int` - 0 for success, < 0 for failure.

##### `registerAudioFrameObserver(IAudioFrameObserver observer, boolean enableVad, AgoraAudioVadConfigV2 vadConfig)`
Registers an `IAudioFrameObserver` to receive raw audio data.
- **Parameters:**
  - `observer`: `IAudioFrameObserver` - The observer to register.
  - `enableVad`: `boolean` - Whether to enable Voice Activity Detection (VAD).
  - `vadConfig`: `AgoraAudioVadConfigV2` - The configuration for VAD.
- **Returns:** `int` - 0 for success, < 0 for failure.

##### `registerAudioEncodedFrameObserver(IAudioEncodedFrameObserver observer)`
Registers an `IAudioEncodedFrameObserver` to receive encoded audio frames.
- **Parameters:**
  - `observer`: `IAudioEncodedFrameObserver` - The observer to register.
- **Returns:** `int` - 0 for success, < 0 for failure.

##### `registerLocalUserObserver(ILocalUserObserver observer)`
Registers an `ILocalUserObserver` to receive events for the local user.
- **Parameters:**
  - `observer`: `ILocalUserObserver` - The observer to register.
- **Returns:** `int` - 0 for success, < 0 for failure.

##### `registerVideoFrameObserver(AgoraVideoFrameObserver2 agoraVideoFrameObserver2)`
Registers an `IVideoFrameObserver2` to receive raw video frames.
- **Parameters:**
  - `agoraVideoFrameObserver2`: `AgoraVideoFrameObserver2` - The observer to register.
- **Returns:** `int` - 0 for success, < 0 for failure.

##### `registerVideoEncodedFrameObserver(AgoraVideoEncodedFrameObserver agoraVideoEncodedFrameObserver)`
Registers an `IVideoEncodedFrameObserver` to receive encoded video frames.
- **Parameters:**
  - `agoraVideoEncodedFrameObserver`: `AgoraVideoEncodedFrameObserver` - The observer to register.
- **Returns:** `int` - 0 for success, < 0 for failure.

##### `connect(String token, String channelId, String userId)`
Connects to a channel.
- **Parameters:**
  - `token`: `String` - The token for authentication.
  - `channelId`: `String` - The channel ID.
  - `userId`: `String` - The user ID.
- **Returns:** `int` - 0 for success, < 0 for failure.

##### `disconnect()`
Disconnects from the channel.
- **Returns:** `int` - 0 for success, < 0 for failure.

##### `getLocalUser()`
Gets the `AgoraLocalUser` object associated with this connection.
- **Returns:** `AgoraLocalUser` - The local user object.

### AgoraLocalUser
The `AgoraLocalUser` class provides methods to manage the local user's audio and video.

#### Methods

##### `publishAudio(AgoraLocalAudioTrack agoraLocalAudioTrack)`
Publishes a local audio track.
- **Parameters:**
  - `agoraLocalAudioTrack`: `AgoraLocalAudioTrack` - The local audio track to publish.
- **Returns:** `int` - 0 for success, < 0 for failure.

##### `unpublishAudio(AgoraLocalAudioTrack agoraLocalAudioTrack)`
Unpublishes a local audio track.
- **Parameters:**
  - `agoraLocalAudioTrack`: `AgoraLocalAudioTrack` - The local audio track to unpublish.
- **Returns:** `int` - 0 for success, < 0 for failure.

##### `publishVideo(AgoraLocalVideoTrack agoraLocalVideoTrack)`
Publishes a local video track.
- **Parameters:**
  - `agoraLocalVideoTrack`: `AgoraLocalVideoTrack` - The local video track to publish.
- **Returns:** `int` - 0 for success, < 0 for failure.

##### `unpublishVideo(AgoraLocalVideoTrack agoraLocalVideoTrack)`
Unpublishes a local video track.
- **Parameters:**
  - `agoraLocalVideoTrack`: `AgoraLocalVideoTrack` - The local video track to unpublish.
- **Returns:** `int` - 0 for success, < 0 for failure.

##### `subscribeAudio(String userId)`
Subscribes to a remote user's audio track.
- **Parameters:**
  - `userId`: `String` - The ID of the remote user.
- **Returns:** `int` - 0 for success, < 0 for failure.

##### `unsubscribeAudio(String userId)`
Unsubscribes from a remote user's audio track.
- **Parameters:**
  - `userId`: `String` - The ID of the remote user.
- **Returns:** `int` - 0 for success, < 0 for failure.

##### `subscribeVideo(String userId, VideoSubscriptionOptions options)`
Subscribes to a remote user's video track.
- **Parameters:**
  - `userId`: `String` - The ID of the remote user.
  - `options`: `VideoSubscriptionOptions` - The subscription options.
- **Returns:** `int` - 0 for success, < 0 for failure.

##### `unsubscribeVideo(String userId)`
Unsubscribes from a remote user's video track.
- **Parameters:**
  - `userId`: `String` - The ID of the remote user.
- **Returns:** `int` - 0 for success, < 0 for failure.

### AgoraParameter
The `AgoraParameter` class provides methods to set and get SDK parameters.

#### Methods

##### `destroy()`
Destroys the AgoraParameter object.

##### `setInt(String key, int value)`
Sets an integer parameter.
- **Parameters:**
  - `key`: `String` - The key of the parameter.
  - `value`: `int` - The integer value to set.
- **Returns:** `int` - 0 on success, <0 on failure.

##### `setBool(String key, boolean value)`
Sets a boolean parameter.
- **Parameters:**
  - `key`: `String` - The key of the parameter.
  - `value`: `boolean` - The boolean value to set.
- **Returns:** `int` - 0 on success, <0 on failure.

##### `setUint(String key, int value)`
Sets an unsigned integer parameter.
- **Parameters:**
  - `key`: `String` - The key of the parameter.
  - `value`: `int` - The unsigned integer value to set.
- **Returns:** `int` - 0 on success, <0 on failure.

##### `setNumber(String key, double value)`
Sets a numeric parameter.
- **Parameters:**
  - `key`: `String` - The key of the parameter.
  - `value`: `double` - The numeric value to set.
- **Returns:** `int` - 0 on success, <0 on failure.

##### `setString(String key, String value)`
Sets a string parameter.
- **Parameters:**
  - `key`: `String` - The key of the parameter.
  - `value`: `String` - The string value to set.
- **Returns:** `int` - 0 on success, <0 on failure.

##### `setArray(String key, String json_src)`
Sets an array parameter using a JSON string.
- **Parameters:**
  - `key`: `String` - The key of the parameter.
  - `json_src`: `String` - The JSON string representing the array.
- **Returns:** `int` - 0 on success, <0 on failure.

##### `setParameters(String json_src)`
Sets multiple parameters using a JSON string.
- **Parameters:**
  - `json_src`: `String` - The JSON string containing multiple parameters.
- **Returns:** `int` - 0 on success, <0 on failure.

##### `getInt(String key, Out value)`
Gets an integer parameter.
- **Parameters:**
  - `key`: `String` - The key of the parameter.
  - `value`: `Out` - An Out object to store the integer value.
- **Returns:** `int` - 0 on success, <0 on failure.

##### `getBool(String key, Out value)`
Gets a boolean parameter.
- **Parameters:**
  - `key`: `String` - The key of the parameter.
  - `value`: `Out` - An Out object to store the boolean value.
- **Returns:** `int` - 0 on success, <0 on failure.

##### `getUint(String key, Out value)`
Gets an unsigned integer parameter.
- **Parameters:**
  - `key`: `String` - The key of the parameter.
  - `value`: `Out` - An Out object to store the unsigned integer value.
- **Returns:** `int` - 0 on success, <0 on failure.

##### `getNumber(String key, Out value)`
Gets a numeric parameter.
- **Parameters:**
  - `key`: `String` - The key of the parameter.
  - `value`: `Out` - An Out object to store the numeric value.
- **Returns:** `int` - 0 on success, <0 on failure.

##### `getString(String key, Out value)`
Gets a string parameter.
- **Parameters:**
  - `key`: `String` - The key of the parameter.
  - `value`: `Out` - An Out object to store the string value.
- **Returns:** `int` - 0 on success, <0 on failure.

### AgoraAudioProcessor
The `AgoraAudioProcessor` class is used to process audio frames.

#### Methods

##### `getSdkVersion()`
Gets the SDK version.
- **Returns:** `String` - The SDK version.

##### `init(String appId, String license, IAgoraAudioProcessorEventHandler eventHandler, AgoraAudioProcessorConfig config)`
Initializes the audio processor.
- **Parameters:**
  - `appId`: `String` - The App ID.
  - `license`: `String` - The license for the audio processor.
  - `eventHandler`: `IAgoraAudioProcessorEventHandler` - The event handler.
  - `config`: `AgoraAudioProcessorConfig` - The configuration for the audio processor.
- **Returns:** `int` - 0 for success, or an error code.

##### `process(AgoraAudioFrame nearIn)`
Processes an audio frame.
- **Parameters:**
  - `nearIn`: `AgoraAudioFrame` - The input audio frame.
- **Returns:** `AgoraAudioFrame` - The processed audio frame.

##### `process(AgoraAudioFrame nearIn, AgoraAudioFrame farIn)`
Processes audio frames from near and far ends.
- **Parameters:**
  - `nearIn`: `AgoraAudioFrame` - The near-end input audio frame.
  - `farIn`: `AgoraAudioFrame` - The far-end input audio frame.
- **Returns:** `AgoraAudioFrame` - The processed audio frame.

##### `release()`
Releases the resources used by the audio processor.
- **Returns:** `int` - 0 for success, or an error code.

### AgoraAudioVad
The `AgoraAudioVad` class provides Voice Activity Detection (VAD) functionality. It's managed by `AgoraAudioVadManager`.

#### Methods

##### `getVadInstance(String channelId, String userId)`
Retrieves the VAD instance for a specified channel and user.
- **Parameters:**
  - `channelId`: `String` - Channel ID.
  - `userId`: `String` - User ID.
- **Returns:** `AgoraAudioVadV2` - The VAD instance, or null if not found.

##### `delVadInstance(String channelId, String userId)`
Removes and destroys a specified VAD instance.
- **Parameters:**
  - `channelId`: `String` - Channel ID.
  - `userId`: `String` - User ID.

##### `process(String channelId, String userId, AudioFrame frame)`
Processes an audio frame for VAD.
- **Parameters:**
  - `channelId`: `String` - Channel ID.
  - `userId`: `String` - User ID.
  - `frame`: `AudioFrame` - The audio frame to process.
- **Returns:** `VadProcessResult` - The result of the VAD processing.

##### `destroy()`
Destroys the VAD manager and cleans up all VAD instances.

---

## Observer Interfaces

### IRtcConnObserver
The observer interface for RTC connection events.

#### Methods

##### `onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason)`
Occurs when the SDK connects to the Agora channel.
- **Parameters:**
  - `agoraRtcConn`: `AgoraRtcConn` - The connection object.
  - `connInfo`: `RtcConnInfo` - Connection information.
  - `reason`: `int` - The reason for the state change.

##### `onDisconnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason)`
Occurs when the SDK disconnects from the Agora channel.
- **Parameters:**
  - `agoraRtcConn`: `AgoraRtcConn` - The connection object.
  - `connInfo`: `RtcConnInfo` - Connection information.
  - `reason`: `int` - The reason for the state change.

##### `onUserJoined(AgoraRtcConn agoraRtcConn, String userId)`
Occurs when a remote user joins the channel.
- **Parameters:**
  - `agoraRtcConn`: `AgoraRtcConn` - The connection object.
  - `userId`: `String` - The ID of the remote user.

##### `onUserLeft(AgoraRtcConn agoraRtcConn, String userId, int reason)`
Occurs when a remote user leaves the channel.
- **Parameters:**
  - `agoraRtcConn`: `AgoraRtcConn` - The connection object.
  - `userId`: `String` - The ID of the user who left.
  - `reason`: `int` - The reason the user left.

### ILocalUserObserver
The observer interface for local user events.

#### Methods

##### `onAudioTrackPublishSuccess(AgoraLocalUser agoraLocalUser, AgoraLocalAudioTrack agoraLocalAudioTrack)`
Occurs when a local audio track is published successfully.
- **Parameters:**
  - `agoraLocalUser`: `AgoraLocalUser` - The local user object.
  - `agoraLocalAudioTrack`: `AgoraLocalAudioTrack` - The published audio track.

##### `onVideoTrackPublishSuccess(AgoraLocalUser agoraLocalUser, AgoraLocalVideoTrack agoraLocalVideoTrack)`
Occurs when a local video track is published successfully.
- **Parameters:**
  - `agoraLocalUser`: `AgoraLocalUser` - The local user object.
  - `agoraLocalVideoTrack`: `AgoraLocalVideoTrack` - The published video track.

##### `onUserAudioTrackSubscribed(AgoraLocalUser agoraLocalUser, String userId, AgoraRemoteAudioTrack agoraRemoteAudioTrack)`
Occurs when a remote user's audio track is subscribed.
- **Parameters:**
  - `agoraLocalUser`: `AgoraLocalUser` - The local user object.
  - `userId`: `String` - The ID of the remote user.
  - `agoraRemoteAudioTrack`: `AgoraRemoteAudioTrack` - The remote audio track.

##### `onUserVideoTrackSubscribed(AgoraLocalUser agoraLocalUser, String userId, VideoTrackInfo info, AgoraRemoteVideoTrack agoraRemoteVideoTrack)`
Occurs when a remote user's video track is subscribed.
- **Parameters:**
  - `agoraLocalUser`: `AgoraLocalUser` - The local user object.
  - `userId`: `String` - The ID of the remote user.
  - `info`: `VideoTrackInfo` - Information about the video track.
  - `agoraRemoteVideoTrack`: `AgoraRemoteVideoTrack` - The remote video track.

### INetworkObserver
The observer interface for network events.

#### Methods

##### `onUplinkNetworkInfoUpdated(AgoraRtcConn agoraRtcConn, UplinkNetworkInfo info)`
Occurs when uplink network information is updated.
- **Parameters:**
  - `agoraRtcConn`: `AgoraRtcConn` - The connection object.
  - `info`: `UplinkNetworkInfo` - The uplink network information.

##### `onDownlinkNetworkInfoUpdated(AgoraRtcConn agoraRtcConn, DownlinkNetworkInfo info)`
Occurs when downlink network information is updated.
- **Parameters:**
  - `agoraRtcConn`: `AgoraRtcConn` - The connection object.
  - `info`: `DownlinkNetworkInfo` - The downlink network information.

### IVideoFrameObserver2
The observer interface for raw video frames.

#### Methods

##### `onFrame(AgoraVideoFrameObserver2 agoraVideoFrameObserver2, String channelId, String remoteUserId, VideoFrame frame)`
Occurs when a video frame is received.
- **Parameters:**
  - `agoraVideoFrameObserver2`: `AgoraVideoFrameObserver2` - The observer object.
  - `channelId`: `String` - The channel ID.
  - `remoteUserId`: `String` - The ID of the remote user.
  - `frame`: `VideoFrame` - The video frame.

### IAudioFrameObserver
The observer interface for raw audio frames.

#### Methods

##### `onRecordAudioFrame(AgoraLocalUser agoraLocalUser, String channelId, AudioFrame frame)`
Retrieves the recorded audio frame.
- **Parameters:**
  - `agoraLocalUser`: `AgoraLocalUser` - The local user.
  - `channelId`: `String` - The channel ID.
  - `frame`: `AudioFrame` - The audio frame.
- **Returns:** `int` - 0/1 (no practical significance).

##### `onPlaybackAudioFrame(AgoraLocalUser agoraLocalUser, String channelId, AudioFrame frame)`
Retrieves the playback audio frame.
- **Parameters:**
  - `agoraLocalUser`: `AgoraLocalUser` - The local user.
  - `channelId`: `String` - The channel ID.
  - `frame`: `AudioFrame` - The audio frame.
- **Returns:** `int` - 0/1 (no practical significance).

##### `onMixedAudioFrame(AgoraLocalUser agoraLocalUser, String channelId, AudioFrame frame)`
Retrieves the mixed audio frame.
- **Parameters:**
  - `agoraLocalUser`: `AgoraLocalUser` - The local user.
  - `channelId`: `String` - The channel ID.
  - `frame`: `AudioFrame` - The audio frame.
- **Returns:** `int` - 0/1 (no practical significance).

##### `onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agoraLocalUser, String channelId, String userId, AudioFrame frame, VadProcessResult vadResult)`
Retrieves the playback audio frame of a specific user before mixing.
- **Parameters:**
  - `agoraLocalUser`: `AgoraLocalUser` - The local user.
  - `channelId`: `String` - The channel ID.
  - `userId`: `String` - The user ID.
  - `frame`: `AudioFrame` - The audio frame.
  - `vadResult`: `VadProcessResult` - The VAD result.
- **Returns:** `int` - 0/1 (no practical significance).

### IAudioEncodedFrameObserver
The observer interface for encoded audio frames.

#### Methods

##### `onEncodedAudioFrameReceived(String remoteUserId, ByteBuffer buffer, EncodedAudioFrameReceiverInfo info)`
Occurs when an encoded audio frame is received.
- **Parameters:**
  - `remoteUserId`: `String` - The ID of the remote user.
  - `buffer`: `ByteBuffer` - The encoded audio frame buffer.
  - `info`: `EncodedAudioFrameReceiverInfo` - Information about the encoded audio frame.
- **Returns:** `int` - 0/1 (no practical significance).

### IVideoEncodedFrameObserver
The observer interface for encoded video frames.

#### Methods

##### `onEncodedVideoFrame(AgoraVideoEncodedFrameObserver observer, int userId, ByteBuffer buffer, EncodedVideoFrameInfo info)`
Occurs when an encoded video frame is received.
- **Parameters:**
  - `observer`: `AgoraVideoEncodedFrameObserver` - The observer object.
  - `userId`: `int` - The user ID.
  - `buffer`: `ByteBuffer` - The encoded video frame buffer.
  - `info`: `EncodedVideoFrameInfo` - Information about the encoded video frame.
- **Returns:** `int` - 0/1 (no practical significance).

### IAgoraAudioProcessorEventHandler
The event handler interface for the Agora Audio Processor.

#### Methods

##### `onEvent(Constants.AgoraAudioProcessorEventType eventType)`
Reports an event from the audio processor.
- **Parameters:**
  - `eventType`: `Constants.AgoraAudioProcessorEventType` - The type of the event.

##### `onError(int errorCode)`
Reports an error from the audio processor.
- **Parameters:**
  - `errorCode`: `int` - The error code.

---

## Data Structures

### AgoraServiceConfig
Configuration for the AgoraService.

- **`enableAudioProcessor`**: `int` - Whether to enable the audio processing module. `1` (default) enables, `0` disables.
- **`enableAudioDevice`**: `int` - Whether to enable the audio device module for recording and playback. `1` enables, `0` (default) disables.
- **`enableVideo`**: `int` - Whether to enable video. `1` enables, `0` (default) disables.
- **`context`**: `Object` - The user context. For Windows, it's the window handle; for Android, it's the Activity context.
- **`appId`**: `String` - Your project's App ID.
- **`areaCode`**: `int` - The supported area code. Default is `AREA_CODE_GLOB`.
- **`channelProfile`**: `int` - The channel profile. Default is `CHANNEL_PROFILE_LIVE_BROADCASTING`.
- **`audioScenario`**: `int` - The audio scenario. Default is `AUDIO_SCENARIO_AI_SERVER`.
- **`useStringUid`**: `int` - Whether to enable string user ID. `1` enables, `0` (default) disables.
- **`logFilePath`**: `String` - The path for log files. Default is `NULL`.
- **`logFileSize`**: `int` - The maximum size of the log file in KB. Default is 2048.
- **`logFilters`**: `int` - The log level. Default is `LOG_LEVEL_INFO`.
- **`domainLimit`**: `int` - Whether to enable domain limit. `1` enables, `0` (default) disables.
- **`configDir`**: `String` - The path for configuration files. Default is `NULL`.
- **`dataDir`**: `String` - The path for data files. Default is `NULL`.

### RtcConnConfig
Configuration for the RTC connection.

- **`autoSubscribeAudio`**: `int` - Whether to automatically subscribe to all audio streams. `1` (default) for yes, `0` for no.
- **`autoSubscribeVideo`**: `int` - Whether to automatically subscribe to all video streams. `1` (default) for yes, `0` for no.
- **`enableAudioRecordingOrPlayout`**: `int` - Whether to enable audio recording or playout. `1` for yes, `0` for no.
- **`maxSendBitrate`**: `int` - The maximum sending bitrate.
- **`minPort`**: `int` - The minimum port for the connection.
- **`maxPort`**: `int` - The maximum port for the connection.
- **`audioSubsOptions`**: `AudioSubscriptionOptions` - Options for audio subscription.
- **`clientRoleType`**: `int` - The role of the user. Default is `CLIENT_ROLE_AUDIENCE`.
- **`channelProfile`**: `int` - The channel profile.
- **`audioRecvMediaPacket`**: `int` - Whether to receive audio media packets.
- **`audioRecvEncodedFrame`**: `int` - Whether to receive encoded audio frames.
- **`videoRecvMediaPacket`**: `int` - Whether to receive video media packets.

### RtcConnPublishConfig
Configuration for publishing streams in an RTC connection.

- **`audioProfile`**: `int` - The audio profile. Default is `AUDIO_PROFILE_DEFAULT`.
- **`audioScenario`**: `int` - The audio scenario. Default is `AUDIO_SCENARIO_AI_SERVER`.
- **`isPublishAudio`**: `boolean` - Whether to publish audio. Default is `true`.
- **`isPublishVideo`**: `boolean` - Whether to publish video. Default is `false`.
- **`audioPublishType`**: `Constants.AudioPublishType` - The type of audio to publish. Default is `PCM`.
- **`videoPublishType`**: `Constants.VideoPublishType` - The type of video to publish. Default is `NO_PUBLISH`.
- **`senderOptions`**: `SenderOptions` - Options for the sender.

### RtcConnInfo
Information about the RTC connection.

- **`id`**: `int` - Unique identifier for the connection.
- **`channelId`**: `String` - The channel ID.
- **`state`**: `int` - The current state of the connection.
- **`localUserId`**: `String` - The local user's ID.
- **`internalUid`**: `int` - The internal user ID.

### VideoEncoderConfig
Configuration for the video encoder.

- **`codecType`**: `int` - The video codec type.
- **`dimensions`**: `VideoDimensions` - The dimensions of the video frame.
- **`frameRate`**: `int` - The video frame rate.
- **`bitrate`**: `int` - The video encoding target bitrate in Kbps.
- **`minBitrate`**: `int` - The minimum encoding bitrate in Kbps.
- **`orientationMode`**: `int` - The video orientation mode.
- **`degradationPreference`**: `int` - The video degradation preference under bandwidth constraints.
- **`mirrorMode`**: `int` - The video mirror mode.
- **`encodeAlpha`**: `int` - Whether to encode and send alpha data.

### VideoSubscriptionOptions
Options for video subscription.

- **`type`**: `int` - The type of video subscription.
- **`encodedFrameOnly`**: `int` - Whether to subscribe to encoded frames only.

### SimulcastStreamConfig
Configuration for a simulcast video stream.

- **`dimensions`**: `VideoDimensions` - The dimensions of the video.
- **`bitrate`**: `int` - The bitrate of the video.
- **`framerate`**: `int` - The framerate of the video.

### EncodedVideoFrameInfo
Information about an encoded video frame.

- **`codecType`**: `int` - The video codec type.
- **`width`**: `int` - The width of the video in pixels.
- **`height`**: `int` - The height of the video in pixels.
- **`framesPerSecond`**: `int` - The number of video frames per second.
- **`frameType`**: `int` - The frame type of the encoded video frame.
- **`rotation`**: `int` - The rotation information of the encoded video frame.
- **`trackId`**: `int` - The track ID for multi-track video.
- **`captureTimeMs`**: `long` - The timestamp when the video was captured.
- **`decodeTimeMs`**: `long` - The timestamp for rendering the video.
- **`presentationMs`**: `long` - The presentation timestamp (PTS) of the video frame in ms.
- **`uid`**: `int` - The user ID.
- **`streamType`**: `int` - The stream type of the video frame.

### EncodedAudioFrameInfo
Information about an encoded audio frame.

- **`speech`**: `int` - Indicates whether the frame contains speech.
- **`codec`**: `int` - The codec type of the audio frame.
- **`sendEvenIfEmpty`**: `int` - Whether to send the frame even if it is empty.
- **`sampleRateHz`**: `int` - The sampling rate of the audio frame in Hz.
- **`samplesPerChannel`**: `int` - The number of samples per audio channel.
- **`numberOfChannels`**: `int` - The number of audio channels.
- **`captureTimeMs`**: `long` - The capture timestamp in ms.

### EncodedAudioFrameReceiverInfo
Information for the receiver of an encoded audio frame.

- **`sendTs`**: `long` - The send time of the packet.
- **`codec`**: `int` - The codec of the packet.

### SenderOptions
Options for the sender.

- **`ccMode`**: `int` - The congestion control mode.
- **`codecType`**: `int` - The codec type.
- **`targetBitrate`**: `int` - The target bitrate.

### VideoFrame
Represents a video frame.

- **`type`**: `int` - The type of video frame.
- **`width`**: `int` - The width of the video in pixels.
- **`height`**: `int` - The height of the video in pixels.
- **`yStride`**: `int` - The line span of the Y buffer in YUV data.
- **`uStride`**: `int` - The line span of the U buffer in YUV data.
- **`vStride`**: `int` - The line span of the V buffer in YUV data.
- **`yBuffer`**: `ByteBuffer` - The Y data buffer.
- **`uBuffer`**: `ByteBuffer` - The U data buffer.
- **`vBuffer`**: `ByteBuffer` - The V data buffer.
- **`rotation`**: `int` - The rotation of this frame (0, 90, 180, 270).
- **`renderTimeMs`**: `long` - The timestamp to render the video stream.
- **`avsyncType`**: `int` - The AV sync type.

### ExternalVideoFrame
Represents an external video frame.

- **`type`**: `int` - The buffer type.
- **`format`**: `int` - The pixel format.
- **`buffer`**: `ByteBuffer` - The video buffer.
- **`stride`**: `int` - The line spacing of the video frame in pixels.
- **`height`**: `int` - The height of the video frame.
- **`timestamp`**: `long` - The timestamp of the video frame in ms.

### VideoDimensions
Dimensions of a video.

- **`width`**: `int` - The width of the video.
- **`height`**: `int` - The height of the video.

### RtcStats
Statistics of an RTC connection.

- **`duration`**: `int` - The duration of the call.
- **`txBytes`**: `int` - The total bytes transmitted.
- **`rxBytes`**: `int` - The total bytes received.
- **`txAudioBytes`**: `int` - The total audio bytes transmitted.
- **`txVideoBytes`**: `int` - The total video bytes transmitted.
- **`rxAudioBytes`**: `int` - The total audio bytes received.
- **`rxVideoBytes`**: `int` - The total video bytes received.
- **`txKBitRate`**: `short` - The transmission bitrate in Kbps.
- **`rxKBitRate`**: `short` - The reception bitrate in Kbps.
- **`userCount`**: `int` - The number of users in the channel.

### UserInfo
Information about a user.

- **`userId`**: `String` - The user ID.
- **`hasAudio`**: `int` - Whether the user has audio enabled.
- **`hasVideo`**: `int` - Whether the user has video enabled.

### VadProcessResult
Result of a Voice Activity Detection (VAD) process.

- **`outFrame`**: `byte[]` - The output frame after VAD processing.
- **`state`**: `Constants.VadState` - The state of the VAD process.

### AgoraAudioVadConfig
Configuration for Voice Activity Detection (VAD).

- **`fftSz`**: `int` - FFT size. Default is 1024.
- **`hopSz`**: `int` - FFT Hop Size. Default is 160.
- **`anaWindowSz`**: `int` - FFT window size. Default is 768.
- **`voiceProbThr`**: `float` - Voice probability threshold. Default is 0.8.
- **`rmsThr`**: `float` - RMS threshold in dB. Default is -40.0.

### AgoraAudioVadConfigV2
Configuration for Voice Activity Detection (VAD) version 2.

- **`preStartRecognizeCount`**: `int` - Number of audio frames to save before speech starts. Default is 16.
- **`startRecognizeCount`**: `int` - Number of audio frames to confirm speech state. Default is 30.
- **`stopRecognizeCount`**: `int` - Number of audio frames to confirm silence state. Default is 20.
- **`activePercent`**: `float` - Percentage of active frames to enter speaking state. Default is 0.7.
- **`inactivePercent`**: `float` - Percentage of inactive frames to enter silence state. Default is 0.5.
- **`startVoiceProb`**: `int` - Voice probability gate threshold to start speech. Default is 70.
- **`stopVoiceProb`**: `int` - Voice probability gate threshold to stop speech. Default is 70.
- **`startRmsThreshold`**: `int` - RMS threshold to start speech. Default is -50.
- **`stopRmsThreshold`**: `int` - RMS threshold to stop speech. Default is -50.

### LocalAudioTrackStats
Statistics for a local audio track.

- **`sourceId`**: `int` - The source ID of the audio.
- **`bufferedPcmDataListSize`**: `int` - The size of the buffered PCM data list.
- **`missedAudioFrames`**: `int` - The number of missed audio frames.
- **`sentAudioFrames`**: `int` - The number of sent audio frames.
- **`pushedAudioFrames`**: `int` - The number of pushed audio frames.
- **`droppedAudioFrames`**: `int` - The number of dropped audio frames.

### LocalVideoTrackStats
Statistics for a local video track.

- **`numberOfStreams`**: `long` - The number of video streams.
- **`bytesMajorStream`**: `long` - The bytes of the major stream.
- **`bytesMinorStream`**: `long` - The bytes of the minor stream.
- **`framesEncoded`**: `int` - The number of frames encoded.
- **`captureFrameRate`**: `int` - The capture frame rate in fps.
- **`encodeFrameRate`**: `int` - The encoder output frame rate in fps.
- **`targetMediaBitrateBps`**: `int` - The target bitrate in bps.

### RemoteAudioTrackStats
Statistics for a remote audio track.

- **`uid`**: `int` - The user ID of the remote user.
- **`quality`**: `int` - The audio quality received.
- **`networkTransportDelay`**: `int` - The network delay in ms.
- **`jitterBufferDelay`**: `int` - The jitter buffer delay in ms.
- **`audioLossRate`**: `int` - The audio frame loss rate.
- **`receivedBitrate`**: `int` - The average bitrate of the received audio stream in Kbps.

### RemoteVideoTrackStats
Statistics for a remote video track.

- **`uid`**: `int` - The user ID of the remote user.
- **`width`**: `int` - The width of the video stream in pixels.
- **`height`**: `int` - The height of the video stream in pixels.
- **`receivedBitrate`**: `int` - The received bitrate in Kbps.
- **`decoderOutputFrameRate`**: `int` - The decoder output frame rate in fps.
- **`frameLossRate`**: `int` - The video frame loss rate.

### EncryptionConfig
Configuration for stream encryption.

- **`encryptionMode`**: `int` - The encryption mode.
- **`encryptionKey`**: `String` - The encryption key.
- **`encryptionKdfSalt`**: `byte[]` - The encryption KDF salt.

### UplinkNetworkInfo
Information about the uplink network.

- **`videoEncoderTargetBitrateBps`**: `int` - The target bitrate for the video encoder in bps.

### DownlinkNetworkInfo
Information about the downlink network.

- **`lastmileBufferDelayTimeMs`**: `int` - The delay time in the last-mile buffer in ms.
- **`bandwidthEstimationBps`**: `int` - The estimated bandwidth in bps.
- **`peerDownlinkInfo`**: `PeerDownlinkInfo` - Information about the peer's downlink.

### PeerDownlinkInfo
Information about a peer's downlink.

- **`userId`**: `String` - The user ID.
- **`streamType`**: `int` - The stream type.
- **`currentDownscaleLevel`**: `int` - The current downscale level.
- **`expectedBitrateBps`**: `int` - The expected bitrate in bps.

### AecConfig
Configuration for Acoustic Echo Cancellation (AEC).

- **`enabled`**: `boolean` - Whether to enable AEC.
- **`stereoAecEnabled`**: `boolean` - Whether to enable stereo AEC.
- **`filterLength`**: `Constants.AecFilterLength` - The AEC linear filter length.
- **`aecModelType`**: `Constants.AecModelType` - The AEC model type.
- **`aecSuppressionMode`**: `Constants.AecSuppressionMode` - The AEC suppression level.

### AnsConfig
Configuration for Automatic Noise Suppression (ANS).

- **`enabled`**: `boolean` - Whether to enable ANS.
- **`suppressionMode`**: `Constants.AnsSuppressionMode` - The ANS noise suppression mode.
- **`ansModelType`**: `Constants.AnsModelType` - The ANS model type.

### AgcConfig
Configuration for Automatic Gain Control (AGC).

- **`enabled`**: `boolean` - Whether to enable AGC.
- **`useAnalogMode`**: `boolean` - Whether to use analog AGC mode.
- **`maxDigitalGaindB`**: `int` - The maximum digital AGC gain in dB.
- **`targetleveldB`**: `int` - The target digital AGC level in dB.

### BghvsConfig
Configuration for Background Human Voice Suppression (BGHVS).

- **`enabled`**: `boolean` - Whether to enable BGHVS.
- **`bghvsSosLenInMs`**: `int` - The duration to trigger Start of Speech (SOS) in ms.
- **`bghvsEosLenInMs`**: `int` - The duration to trigger End of Speech (EOS) in ms.
- **`bghvsSppMode`**: `Constants.BghvsSuppressionMode` - The BGHVS aggressive level.

### AgoraAudioProcessorConfig
Configuration for the Agora Audio Processor.

- **`modelPath`**: `String` - The file path to the audio processing model.
- **`aecConfig`**: `AecConfig` - The configuration for AEC.
- **`ansConfig`**: `AnsConfig` - The configuration for ANS.
- **`agcConfig`**: `AgcConfig` - The configuration for AGC.
- **`bghvsConfig`**: `BghvsConfig` - The configuration for BGHVS.

### AgoraAudioFrame
Represents an audio frame.

- **`type`**: `int` - The audio frame type.
- **`sampleRate`**: `int` - The number of samples per second.
- **`channels`**: `int` - The number of audio channels.
- **`samplesPerChannel`**: `int` - The number of samples per channel in this frame.
- **`bytesPerSample`**: `int` - The number of bytes per sample.
- **`buffer`**: `ByteBuffer` - The data buffer of the audio frame.
- **`presentationMs`**: `long` - The presentation timestamp (PTS) of the audio frame in ms.

---

## Utility Classes

### VadDumpUtils
A utility class for dumping VAD (Voice Activity Detection) related data to files for debugging and analysis.

#### Constructors

##### `VadDumpUtils(String path)`
Constructs a `VadDumpUtils` object.
- **Parameters:**
  - `path`: `String` - The directory path where the dump files will be stored. A subdirectory with a timestamp name will be created inside this path.

#### Methods

##### `write(AudioFrame frame, byte[] vadResultBytes, Constants.VadState vadResultState)`
Writes the audio frame and VAD result to the dump files.
- **Parameters:**
  - `frame`: `AudioFrame` - The original audio frame.
  - `vadResultBytes`: `byte[]` - The audio data corresponding to the VAD result.
  - `vadResultState`: `Constants.VadState` - The state of the VAD result.

##### `release()`
Releases all resources and closes the dump files.