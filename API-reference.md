# Agora Server SDK Java API Reference

This document provides a reference for the main classes and methods of the Agora Server SDK Java API.

## Table of Contents

- [Core Classes](#core-classes)
  - [AgoraService](#agoraservice)
  - [AgoraMediaNodeFactory](#agoramedianodefactory)
  - [AgoraRtcConn](#agorartcconn)
  - [AgoraLocalUser](#agoralocaluser)
  - [AgoraLocalAudioTrack](#agoralocalaudiotrack)
  - [AgoraLocalVideoTrack](#agoralocalvideotrack)
  - [AgoraRemoteAudioTrack](#agoraremoteaudiotrack)
  - [AgoraRemoteVideoTrack](#agoraremotevideotrack)
  - [AgoraAudioPcmDataSender](#agoraaudiopcmdatasender)
  - [AgoraVideoFrameSender](#agoravideoframesender)
  - [AgoraVideoEncodedImageSender](#agoravideoencodedimagesender)
  - [AgoraAudioEncodedFrameSender](#agoraaudioencodedframesender)
  - [AgoraParameter](#agoraparameter)
- [Observer Interfaces](#observer-interfaces)
  - [IRtcConnObserver](#irtcconnobserver)
  - [ILocalUserObserver](#ilocaluserobserver)
  - [INetworkObserver](#inetworkobserver)
  - [IVideoFrameObserver](#ivideoframeobserver)
  - [IAudioFrameObserver](#iaudioframeobserver)
  - [IAudioEncodedFrameObserver](#iaudioencodedframeobserver)
  - [IVideoEncodedFrameObserver](#ivideoencodedframeobserver)
- [Data Structures](#data-structures)
  - [AgoraServiceConfig](#agoraserviceconfig)
  - [RtcConnConfig](#rtcconnconfig)
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
  - [LocalAudioTrackStats](#localaudiotrackstats)
  - [LocalVideoTrackStats](#localvideotrackstats)
  - [RemoteAudioTrackStats](#remoteaudiotrackstats)
  - [RemoteVideoTrackStats](#remotevideotrackstats)
  - [EncryptionConfig](#encryptionconfig)
  - [UplinkNetworkInfo](#uplinknetworkinfo)
  - [DownlinkNetworkInfo](#downlinknetworkinfo)
  - [PeerDownlinkInfo](#peerdownlinkinfo)
- [Utility Classes](#utility-classes)
  - [AudioConsumerUtils](#audioconsumerutils)
  - [VadDumpUtils](#vaddumputils)

## 核心类

### AgoraService

AgoraService is the core class of the Agora Server SDK, responsible for initializing the SDK, creating media tracks, connections, etc.

```java
public class AgoraService {
    // Constructor
    public AgoraService()
}
```

#### Methods

##### getSdkVersion

```java
public static String getSdkVersion()
```

Gets the SDK version.

**Returns**:

- The SDK version string.

##### initialize

```java
public int initialize(AgoraServiceConfig config)
```

Initializes the Agora service with the specified configuration.

**Parameters**:

- `config`: The configuration for the Agora service (`AgoraServiceConfig`).

**Returns**:

- 0: Success
- < 0: Failure

##### destroy

```java
public synchronized void destroy()
```

Destroys the Agora service.

##### setAudioSessionPreset

```java
public int setAudioSessionPreset(int audioScenario)
```

Sets the audio session preset. Refer to `Constants.AUDIO_SCENARIO_TYPE`.

**Parameters**:

- `audioScenario`: The audio scenario to set.

**Returns**:

- 0: Success
- < 0: Failure

##### setAudioSessionConfig

```java
public int setAudioSessionConfig(AudioSessionConfig config)
```

Sets the audio session configuration.

**Parameters**:

- `config`: The audio session configuration (`AudioSessionConfig`) to set.

**Returns**:

- 0: Success
- < 0: Failure

##### getAudioSessionConfig

```java
public AudioSessionConfig getAudioSessionConfig()
```

Gets the audio session configuration.

**Returns**:

- The current audio session configuration (`AudioSessionConfig`).

##### destroyAudioSessionConfig

```java
public void destroyAudioSessionConfig(AudioSessionConfig config)
```

Destroys the audio session configuration object obtained via `getAudioSessionConfig`.

**Parameters**:

- `config`: The audio session configuration (`AudioSessionConfig`) to destroy.

##### setLogFile

```java
public int setLogFile(String filePath, int fileSize)
```

Sets the log file.

**Parameters**:

- `filePath`: The path of the log file.
- `fileSize`: The size of the log file in bytes.

**Returns**:

- 0: Success
- < 0: Failure

##### setLogFilter

```java
public int setLogFilter(int filters)
```

Sets the log filter level. Refer to `Constants.LOG_FILTER_TYPE`.

**Parameters**:

- `filters`: The log filters to set.

**Returns**:

- 0: Success
- < 0: Failure

##### createLocalAudioTrack

```java
public AgoraLocalAudioTrack createLocalAudioTrack()
```

Creates a local audio track.

**Returns**:

- A local audio track object (`AgoraLocalAudioTrack`).

##### createCustomAudioTrackPcm

```java
public AgoraLocalAudioTrack createCustomAudioTrackPcm(AgoraAudioPcmDataSender agoraAudioPcmDataSender)
```

Creates a custom audio track using a PCM data sender.

**Parameters**:

- `agoraAudioPcmDataSender`: The PCM data sender (`AgoraAudioPcmDataSender`).

**Returns**:

- A custom audio track object (`AgoraLocalAudioTrack`).

##### createCustomAudioTrackEncoded

```java
public AgoraLocalAudioTrack createCustomAudioTrackEncoded(AgoraAudioEncodedFrameSender agoraAudioEncodedFrameSender, int mixMode)
```

Creates a custom audio track using an encoded frame sender.

**Parameters**:

- `agoraAudioEncodedFrameSender`: The encoded frame sender (`AgoraAudioEncodedFrameSender`).
- `mixMode`: The mix mode. Refer to `Constants.MIX_MODE_TYPE`.

**Returns**:

- A custom audio track object (`AgoraLocalAudioTrack`).

##### createCustomAudioTrackPacket

```java
public AgoraLocalAudioTrack createCustomAudioTrackPacket(AgoraMediaPacketSender agoraMediaPacketSender)
```

Creates a custom audio track using a media packet sender.

**Parameters**:

- `agoraMediaPacketSender`: The media packet sender (`AgoraMediaPacketSender`).

**Returns**:

- A custom audio track object (`AgoraLocalAudioTrack`).

##### createMediaPlayerAudioTrack

```java
public AgoraLocalAudioTrack createMediaPlayerAudioTrack(AgoraMediaPlayerSource agoraMediaPlayerSource)
```

Creates an audio track using a media player source.

**Parameters**:

- `agoraMediaPlayerSource`: The media player source (`AgoraMediaPlayerSource`).

**Returns**:

- A media player audio track object (`AgoraLocalAudioTrack`).

##### createRecordingDeviceAudioTrack

```java
public AgoraLocalAudioTrack createRecordingDeviceAudioTrack(AgoraRecordDevice agoraRecordDevice)
```

Creates an audio track using a recording device.

**Parameters**:

- `agoraRecordDevice`: The recording device (`AgoraRecordDevice`).

**Returns**:

- A recording device audio track object (`AgoraLocalAudioTrack`).

##### createAudioDeviceManager

```java
public AgoraAudioDeviceManager createAudioDeviceManager()
```

Creates an audio device manager.

**Returns**:

- An audio device manager object (`AgoraAudioDeviceManager`).

##### createMediaNodeFactory

```java
public AgoraMediaNodeFactory createMediaNodeFactory()
```

Creates a media node factory.

**Returns**:

- A media node factory object (`AgoraMediaNodeFactory`).

##### createCameraVideoTrack

```java
public AgoraLocalVideoTrack createCameraVideoTrack(AgoraCameraCapturer agoraCameraCapturer)
```

Creates a video track using a camera capturer.

**Parameters**:

- `agoraCameraCapturer`: The camera capturer (`AgoraCameraCapturer`).

**Returns**:

- A local video track object (`AgoraLocalVideoTrack`).

##### createCustomVideoTrackFrame

```java
public AgoraLocalVideoTrack createCustomVideoTrackFrame(AgoraVideoFrameSender agoraVideoFrameSender)
```

Creates a custom video track using a video frame sender.

**Parameters**:

- `agoraVideoFrameSender`: The video frame sender (`AgoraVideoFrameSender`).

**Returns**:

- A custom video track object (`AgoraLocalVideoTrack`).

##### createScreenVideoTrack

```java
public AgoraLocalVideoTrack createScreenVideoTrack(AgoraScreenCapturer agoraScreenCapturer)
```

Creates a video track using a screen capturer.

**Parameters**:

- `agoraScreenCapturer`: The screen capturer (`AgoraScreenCapturer`).

**Returns**:

- A local video track object (`AgoraLocalVideoTrack`).

##### createMixedVideoTrack

```java
public AgoraLocalVideoTrack createMixedVideoTrack(AgoraVideoMixer agoraVideoMixer)
```

Creates a video track using a video mixer.

**Parameters**:

- `agoraVideoMixer`: The video mixer (`AgoraVideoMixer`).

**Returns**:

- A local video track object (`AgoraLocalVideoTrack`).

##### createCustomVideoTrackEncoded

```java
public AgoraLocalVideoTrack createCustomVideoTrackEncoded(AgoraVideoEncodedImageSender agora_video_encoded_image_sender, SenderOptions options)
```

Creates a custom video track using an encoded image sender.

**Parameters**:

- `agora_video_encoded_image_sender`: The encoded image sender (`AgoraVideoEncodedImageSender`).
- `options`: Sender options (`SenderOptions`).

**Returns**:

- A custom video track object (`AgoraLocalVideoTrack`).

##### createCustomVideoTrackPacket

```java
public AgoraLocalVideoTrack createCustomVideoTrackPacket(AgoraMediaPacketSender agoraMediaPacketSender)
```

Creates a custom video track using a media packet sender.

**Parameters**:

- `agoraMediaPacketSender`: The media packet sender (`AgoraMediaPacketSender`).

**Returns**:

- A custom video track object (`AgoraLocalVideoTrack`).

##### createMediaPlayerVideoTrack

```java
public AgoraLocalVideoTrack createMediaPlayerVideoTrack(AgoraMediaPlayerSource agoraMediaPlayerSource)
```

Creates a video track using a media player source.

**Parameters**:

- `agoraMediaPlayerSource`: The media player source (`AgoraMediaPlayerSource`).

**Returns**:

- A local video track object (`AgoraLocalVideoTrack`).

##### agoraRtcConnCreate

```java
public AgoraRtcConn agoraRtcConnCreate(RtcConnConfig rtcConnConfig)
```

Creates an RTC connection.

**Parameters**:

- `rtcConnConfig`: The RTC connection configuration (`RtcConnConfig`).

**Returns**:

- An RTC connection object (`AgoraRtcConn`).

##### getAgoraParameter

```java
public AgoraParameter getAgoraParameter()
```

Gets the Agora parameter object.

**Returns**:

- The Agora parameter object (`AgoraParameter`).

##### loadExtensionProvider

```java
public int loadExtensionProvider(String path, boolean unloadAfterUse)
```

Loads an extension provider.

**Parameters**:

- `path`: The path of the extension provider.
- `unloadAfterUse`: Whether to unload after use.

**Returns**:

- 0: Success
- < 0: Failure

##### enableExtension

```java
public int enableExtension(String provider, String extension, String trackId, boolean enable)
```

Enables or disables the specified extension.

**Parameters**:

- `provider`: The name of the extension provider.
- `extension`: The name of the extension.
- `trackId`: The track ID.
- `enable`: Whether to enable the extension (`true` or `false`).

**Returns**:

- 0: Success
- < 0: Failure

##### disableExtension

```java
public int disableExtension(String provider, String extension, String trackId)
```

Disables the specified extension.

**Parameters**:

- `provider`: The name of the extension provider.
- `extension`: The name of the extension.
- `trackId`: The track ID.

**Returns**:

- 0: Success
- < 0: Failure

##### createDataStream

```java
public int createDataStream(Out streamId, int reliable, int ordered)
```

**Deprecated**: Use `AgoraRtcConn.createDataStream(DataStreamConfig config)` instead.

Creates a data stream.

**Parameters**:

- `streamId`: An output parameter to store the data stream ID (`Out<Integer>`).
- `reliable`: Whether reliable transmission is required (1 for reliable, 0 for unreliable).
- `ordered`: Whether ordered transmission is required (1 for ordered, 0 for unordered).

**Returns**:

- 0: Success
- < 0: Failure

##### enableEncryption

```java
public int enableEncryption(int enabled, EncryptionConfig config)
```

**Deprecated**: Use `AgoraRtcConn.enableEncryption(boolean enabled, EncryptionConfig config)` instead.

Enables or disables encryption.

**Parameters**:

- `enabled`: Whether to enable encryption (1 to enable, 0 to disable).
- `config`: Encryption configuration (`EncryptionConfig`).

**Returns**:

- 0: Success
- < 0: Failure

##### createRtmpStreamingService

```java
public AgoraRtmpStreamingService createRtmpStreamingService(AgoraRtcConn agoraRtcConn, String appId)
```

Creates an RTMP streaming service.

**Parameters**:

- `agoraRtcConn`: The RTC connection object (`AgoraRtcConn`).
- `appId`: The application ID.

**Returns**:

- An RTMP streaming service object (`AgoraRtmpStreamingService`).

##### createRtmService

```java
public AgoraRtmService createRtmService()
```

Creates an RTM service.

**Returns**:

- An RTM service object (`AgoraRtmService`).

### AgoraMediaNodeFactory

The `AgoraMediaNodeFactory` class is used to create various media nodes, such as senders, capturers, etc.

```java
public class AgoraMediaNodeFactory {
    // Constructor
    public AgoraMediaNodeFactory(long cptr)
}
```

#### Methods

##### destroy

```java
public synchronized void destroy()
```

Destroys the media node factory.
Ensures that the native resources are released.

##### createAudioPcmDataSender

```java
public AgoraAudioPcmDataSender createAudioPcmDataSender()
```

Creates an instance of `AgoraAudioPcmDataSender`.

**Returns**:

- An instance of `AgoraAudioPcmDataSender`.

##### createAudioEncodedFrameSender

```java
public AgoraAudioEncodedFrameSender createAudioEncodedFrameSender()
```

Creates an instance of `AgoraAudioEncodedFrameSender`.

**Returns**:

- An instance of `AgoraAudioEncodedFrameSender`.

##### createCameraCapturer

```java
public AgoraCameraCapturer createCameraCapturer()
```

Creates an instance of `AgoraCameraCapturer`.

**Returns**:

- An instance of `AgoraCameraCapturer`.

##### createScreenCapturer

```java
public AgoraScreenCapturer createScreenCapturer()
```

Creates an instance of `AgoraScreenCapturer`.

**Returns**:

- An instance of `AgoraScreenCapturer`.

##### createVideoMixer

```java
public AgoraVideoMixer createVideoMixer()
```

Creates an instance of `AgoraVideoMixer`.

**Returns**:

- An instance of `AgoraVideoMixer`.

##### createVideoFrameSender

```java
public AgoraVideoFrameSender createVideoFrameSender()
```

Creates an instance of `AgoraVideoFrameSender`.

**Returns**:

- An instance of `AgoraVideoFrameSender`.

##### createVideoEncodedImageSender

```java
public AgoraVideoEncodedImageSender createVideoEncodedImageSender()
```

Creates an instance of `AgoraVideoEncodedImageSender`.

**Returns**:

- An instance of `AgoraVideoEncodedImageSender`.

##### createVideoRenderer

```java
public AgoraVideoRenderer createVideoRenderer()
```

Creates an instance of `AgoraVideoRenderer`.

**Returns**:

- An instance of `AgoraVideoRenderer`.

##### createMediaPlayerSource

```java
public AgoraMediaPlayerSource createMediaPlayerSource(int type)
```

Creates an instance of `AgoraMediaPlayerSource`.

**Parameters**:

- `type`: The type of media player source to create. Refer to `Constants.MEDIA_PLAYER_SOURCE_TYPE`.

**Returns**:

- An instance of `AgoraMediaPlayerSource`.

##### createMediaPacketSender

```java
public AgoraMediaPacketSender createMediaPacketSender()
```

Creates an instance of `AgoraMediaPacketSender`.

**Returns**:

- An instance of `AgoraMediaPacketSender`.

### AgoraRtcConn

The `AgoraRtcConn` class manages the RTC connection, such as joining channels, sending messages, etc.
**Note:** Methods like `getAgoraParameter`, `createDataStream`, and `enableEncryption` were incorrectly listed under `AgoraMediaNodeFactory` in the previous version of this document; they actually belong to `AgoraRtcConn`.

```java
public class AgoraRtcConn {
    // Constructor
    public AgoraRtcConn(long cptr)
}
```

#### Methods

##### destroy

```java
public synchronized void destroy()
```

Destroys the RTC connection instance and releases associated resources.
This method first unregisters all observers, destroys the local user and AgoraParameter objects, and then calls the underlying native method to destroy the connection.

##### registerObserver

```java
public int registerObserver(IRtcConnObserver observer)
```

Registers an RTC connection observer to receive connection-related event callbacks.

**Parameters**:

- `observer`: An instance of the `IRtcConnObserver` implementation class to register.

**Returns**:

- 0: Success
- < 0: Failure

##### unregisterObserver

```java
public int unregisterObserver()
```

Unregisters the RTC connection observer.

**Returns**:

- 0: Success
- < 0: Failure

##### registerNetworkObserver

```java
public int registerNetworkObserver(INetworkObserver observer)
```

Registers a network observer to receive network-related event callbacks.

**Parameters**:

- `observer`: An instance of the `INetworkObserver` implementation class to register.

**Returns**:

- 0: Success
- < 0: Failure

##### unregisterNetworkObserver

```java
public int unregisterNetworkObserver()
```

Unregisters the network observer.

**Returns**:

- 0: Success
- < 0: Failure

##### connect

```java
public int connect(String token, String channelId, String userId)
```

Connects to the specified RTC channel.

**Parameters**:

- `token`: The token used for authentication.
- `channelId`: The ID of the channel to join.
- `userId`: The user ID.

**Returns**:

- 0: Success
- < 0: Failure

##### disconnect

```java
public int disconnect()
```

Disconnects the current RTC connection.

**Returns**:

- 0: Success
- < 0: Failure

##### sendStreamMessage (Recommended)

```java
public int sendStreamMessage(int streamId, byte[] messageData)
```

Sends a data stream message via the specified stream ID.

**Parameters**:

- `streamId`: The data stream ID, created via `createDataStream`.
- `messageData`: The byte array message to send.

**Returns**:

- 0: Success
- < 0: Failure

##### sendStreamMessage (Deprecated)

```java
@Deprecated
public int sendStreamMessage(int streamId, String message, int length)
```

**Deprecated**: Use `sendStreamMessage(int streamId, byte[] messageData)` instead.
Sends a string message via the specified stream ID.

**Parameters**:

- `streamId`: The data stream ID.
- `message`: The string message to send.
- `length`: Message length (this parameter is usually unnecessary in Java as byte arrays have length information; likely a remnant from the native layer interface).

**Returns**:

- 0: Success
- < 0: Failure

##### getConnInfo

```java
public RtcConnInfo getConnInfo()
```

Gets the information of the current connection.

**Returns**:

- An `RtcConnInfo` object containing connection details. You need to call `destroyConnInfo` to release it after use.

##### destroyConnInfo

```java
public void destroyConnInfo(RtcConnInfo info)
```

Destroys the `RtcConnInfo` object obtained via `getConnInfo`, releasing resources.

**Parameters**:

- `info`: The `RtcConnInfo` object to destroy.

##### getLocalUser

```java
public AgoraLocalUser getLocalUser()
```

Gets the local user object associated with this connection.

**Returns**:

- An `AgoraLocalUser` object.

##### getUserInfo

```java
@Deprecated
public UserInfo getUserInfo(String userId)
```

**Deprecated**: Use `getUserInfoByUserAccount` or `getUserInfoByUid` as needed.
Gets user information based on the user ID.

**Parameters**:

- `userId`: The user ID.

**Returns**:

- A `UserInfo` object containing user details. You need to call `destroyUserInfo` to release it after use.

##### getUserInfoByUserAccount

```java
public UserInfo getUserInfoByUserAccount(String userAccount)
```

Gets user information based on the user account.

**Parameters**:

- `userAccount`: The user account.

**Returns**:

- A `UserInfo` object containing user details. You need to call `destroyUserInfo` to release it after use.

##### getUserInfoByUid

```java
public UserInfo getUserInfoByUid(int uid)
```

Gets user information based on the UID.

**Parameters**:

- `uid`: The user UID.

**Returns**:

- A `UserInfo` object containing user details. You need to call `destroyUserInfo` to release it after use.

##### destroyUserInfo

```java
public void destroyUserInfo(UserInfo info)
```

Destroys the `UserInfo` object obtained via `getUserInfo`, `getUserInfoByUserAccount`, or `getUserInfoByUid`, releasing resources. **Note:** Check if `AccountInfo` returned by newer methods needs separate destruction.

**Parameters**:

- `info`: The `UserInfo` object to destroy.

##### getConnId

```java
public int getConnId()
```

Gets the current connection ID.

**Returns**:

- The connection ID (`int`).

##### getTransportStats

```java
public RtcStats getTransportStats()
```

Gets the transport statistics for the current connection.

**Returns**:

- An `RtcStats` object containing the statistics. You need to call `destroyTransportStats` to release it after use.

##### destroyTransportStats

```java
public void destroyTransportStats(RtcStats stats)
```

Destroys the `RtcStats` object obtained via `getTransportStats`, releasing resources.

**Parameters**:

- `stats`: The `RtcStats` object to destroy.

##### sendCustomReportMessage

```java
public int sendCustomReportMessage(String id, String category, String event, String label, int value)
```

Sends a custom event report message.

**Parameters**:

- `id`: The ID of the report message.
- `category`: The event category.
- `event`: The event name.
- `label`: The event label.
- `value`: The event value.

**Returns**:

- 0: Success
- < 0: Failure

##### getAgoraParameter

```java
public AgoraParameter getAgoraParameter()
```

Gets the `AgoraParameter` object used for configuring SDK parameters.

**Returns**:

- An `AgoraParameter` instance.

##### createDataStream

```java
public int createDataStream(DataStreamConfig config)
```

Creates a data stream using configuration object.

**Parameters**:

- `config`: Data stream configuration (`DataStreamConfig`).

**Returns**:

- The data stream ID (`int`), or < 0 on failure.

##### enableEncryption

```java
public int enableEncryption(boolean enabled, EncryptionConfig config)
```

Enables or disables built-in encryption.

**Parameters**:

- `enabled`: Whether to enable encryption (`boolean`).
- `config`: Encryption configuration (`EncryptionConfig`).

**Returns**:

- 0: Success
- < 0: Failure

### AgoraLocalUser

The `AgoraLocalUser` class represents the local user, providing features like publishing media streams, subscribing to remote media streams, etc.

```java
public class AgoraLocalUser {
    // Constructor
    public AgoraLocalUser(long cptr)
}
```

#### Methods

##### destroy

```java
public synchronized void destroy()
```

Destroys the `AgoraLocalUser` instance. This method first unregisters all associated observers (audio frame, video frame, encoded frame, local user, etc.), then releases the underlying native resources.

##### registerAudioFrameObserver

```java
public int registerAudioFrameObserver(IAudioFrameObserver observer)
```

Registers an audio frame observer to receive audio frame data callbacks.

**Parameters**:

- `observer`: An instance of the `IAudioFrameObserver` implementation class to register.

**Returns**:

- 0: Success
- < 0: Failure

##### registerAudioFrameObserver (with VAD)

```java
public int registerAudioFrameObserver(IAudioFrameObserver observer, boolean enableVad, AgoraAudioVadConfigV2 vadConfig)
```

Registers an audio frame observer with VAD (Voice Activity Detection) enabled.

**Parameters**:

- `observer`: An instance of the `IAudioFrameObserver` implementation class to register.
- `enableVad`: Whether to enable VAD (`boolean`).
- `vadConfig`: VAD configuration (`AgoraAudioVadConfigV2`).

**Returns**:

- 0: Success
- < 0: Failure

##### unregisterAudioFrameObserver

```java
public int unregisterAudioFrameObserver()
```

Unregisters the audio frame observer.

**Returns**:

- 0: Success
- < 0: Failure

##### registerAudioEncodedFrameObserver

```java
public int registerAudioEncodedFrameObserver(IAudioEncodedFrameObserver observer)
```

Registers an audio encoded frame observer.

**Parameters**:

- `observer`: An instance of the `IAudioEncodedFrameObserver` implementation class to register.

**Returns**:

- 0: Success
- < 0: Failure

##### unregisterAudioEncodedFrameObserver

```java
public int unregisterAudioEncodedFrameObserver(IAudioEncodedFrameObserver observer)
```

Unregisters the audio encoded frame observer.

**Parameters**:

- `observer`: The `IAudioEncodedFrameObserver` instance to unregister (should be the same instance passed during registration).

**Returns**:

- 0: Success
- < 0: Failure

##### registerObserver

```java
public int registerObserver(ILocalUserObserver observer)
```

Registers a local user observer to receive event callbacks for the local user.

**Parameters**:

- `observer`: An instance of the `ILocalUserObserver` implementation class to register.

**Returns**:

- 0: Success
- < 0: Failure

##### unregisterObserver

```java
public int unregisterObserver()
```

Unregisters the local user observer.

**Returns**:

- 0: Success
- < 0: Failure

##### registerVideoFrameObserver

```java
public int registerVideoFrameObserver(AgoraVideoFrameObserver2 agoraVideoFrameObserver2)
```

Registers a video frame observer (V2 interface).

**Parameters**:

- `agoraVideoFrameObserver2`: The `AgoraVideoFrameObserver2` instance to register.

**Returns**:

- 0: Success
- < 0: Failure

##### unregisterVideoFrameObserver

```java
public int unregisterVideoFrameObserver(AgoraVideoFrameObserver2 agoraVideoFrameObserver2)
```

Unregisters the video frame observer.

**Parameters**:

- `agoraVideoFrameObserver2`: The `AgoraVideoFrameObserver2` instance to unregister (should be the same instance passed during registration).

**Returns**:

- 0: Success
- < 0: Failure

##### registerVideoEncodedFrameObserver

```java
public int registerVideoEncodedFrameObserver(AgoraVideoEncodedFrameObserver agoraVideoEncodedFrameObserver)
```

Registers a video encoded frame observer.

**Parameters**:

- `agoraVideoEncodedFrameObserver`: The `AgoraVideoEncodedFrameObserver` instance to register.

**Returns**:

- 0: Success
- < 0: Failure

##### unregisterVideoEncodedFrameObserver

```java
public int unregisterVideoEncodedFrameObserver(AgoraVideoEncodedFrameObserver agoraVideoEncodedFrameObserver)
```

Unregisters the video encoded frame observer.

**Parameters**:

- `agoraVideoEncodedFrameObserver`: The `AgoraVideoEncodedFrameObserver` instance to unregister (should be the same instance passed during registration).

**Returns**:

- 0: Success
- < 0: Failure

##### setUserRole

```java
public void setUserRole(int role)
```

Sets the user role (e.g., broadcaster, audience). Use constants from `Constants.CLIENT_ROLE_TYPE`.

**Parameters**:

- `role`: The user role to set.

##### getUserRole

```java
public int getUserRole()
```

Gets the current user role.

**Returns**:

- The current user role (`int`), corresponding to `Constants.CLIENT_ROLE_TYPE`.

##### setAudioEncoderConfig

```java
public int setAudioEncoderConfig(AudioEncoderConfig config)
```

Sets the audio encoder configuration.

**Parameters**:

- `config`: Audio encoder configuration (`AudioEncoderConfig`).

**Returns**:

- 0: Success
- < 0: Failure

##### getLocalAudioStatistics

```java
public LocalAudioDetailedStats getLocalAudioStatistics()
```

Gets detailed local audio statistics.

**Returns**:

- A `LocalAudioDetailedStats` object. Call `destroyLocalAudioStatistics` to release it after use.

##### destroyLocalAudioStatistics

```java
public void destroyLocalAudioStatistics(LocalAudioDetailedStats stats)
```

Destroys the `LocalAudioDetailedStats` object obtained via `getLocalAudioStatistics`, releasing resources.

**Parameters**:

- `stats`: The `LocalAudioDetailedStats` object to destroy.

##### publishAudio

```java
public int publishAudio(AgoraLocalAudioTrack agoraLocalAudioTrack)
```

Publishes the local audio track.

**Parameters**:

- `agoraLocalAudioTrack`: The local audio track (`AgoraLocalAudioTrack`) to publish.

**Returns**:

- 0: Success
- < 0: Failure

##### unpublishAudio

```java
public int unpublishAudio(AgoraLocalAudioTrack agoraLocalAudioTrack)
```

Unpublishes the local audio track.

**Parameters**:

- `agoraLocalAudioTrack`: The local audio track (`AgoraLocalAudioTrack`) to unpublish.

**Returns**:

- 0: Success
- < 0: Failure

##### publishVideo

```java
public int publishVideo(AgoraLocalVideoTrack agoraLocalVideoTrack)
```

Publishes the local video track.

**Parameters**:

- `agoraLocalVideoTrack`: The local video track (`AgoraLocalVideoTrack`) to publish.

**Returns**:

- 0: Success
- < 0: Failure

##### unpublishVideo

```java
public int unpublishVideo(AgoraLocalVideoTrack agoraLocalVideoTrack)
```

Unpublishes the local video track.

**Parameters**:

- `agoraLocalVideoTrack`: The local video track (`AgoraLocalVideoTrack`) to unpublish.

**Returns**:

- 0: Success
- < 0: Failure

##### subscribeAudio

```java
public int subscribeAudio(String userId)
```

Subscribes to the audio stream of a specified remote user.

**Parameters**:

- `userId`: The ID of the remote user to subscribe to.

**Returns**:

- 0: Success
- < 0: Failure

##### subscribeAllAudio

```java
public int subscribeAllAudio()
```

Subscribes to the audio streams of all remote users in the channel.

**Returns**:

- 0: Success
- < 0: Failure

##### unsubscribeAudio

```java
public int unsubscribeAudio(String userId)
```

Unsubscribes from the audio stream of a specified remote user.

**Parameters**:

- `userId`: The ID of the remote user to unsubscribe from.

**Returns**:

- 0: Success
- < 0: Failure

##### unsubscribeAllAudio

```java
public int unsubscribeAllAudio()
```

Unsubscribes from the audio streams of all remote users in the channel.

**Returns**:

- 0: Success
- < 0: Failure

##### adjustPlaybackSignalVolume

```java
public int adjustPlaybackSignalVolume(int volume)
```

Adjusts the playback volume of all remote users mixed locally.

**Parameters**:

- `volume`: Volume level, range [0, 100]. 0 means mute, 100 means original volume.

**Returns**:

- 0: Success
- < 0: Failure

##### getPlaybackSignalVolume

```java
public int getPlaybackSignalVolume(Out volume)
```

Gets the playback volume of all remote users mixed locally.

**Parameters**:

- `volume`: An `Out<Integer>` object to receive the volume level.

**Returns**:

- 0: Success
- < 0: Failure

##### setPlaybackAudioFrameParameters

```java
public int setPlaybackAudioFrameParameters(int channels, int sampleRateHz, int mode, int samplesPerCall)
```

Sets the playback audio frame parameters.

**Parameters**:

- `channels`: Number of channels.
- `sampleRateHz`: Sample rate (Hz).
- `mode`: Operation mode (e.g., `Constants.RAW_AUDIO_FRAME_OP_MODE_TYPE.RAW_AUDIO_FRAME_OP_MODE_READ_ONLY`).
- `samplesPerCall`: Number of samples per callback.

**Returns**:

- 0: Success
- < 0: Failure

##### setRecordingAudioFrameParameters

```java
public int setRecordingAudioFrameParameters(int channels, int sampleRateHz, int mode, int samplesPerCall)
```

Sets the recording audio frame parameters.

**Parameters**:

- `channels`: Number of channels.
- `sampleRateHz`: Sample rate (Hz).
- `mode`: Operation mode.
- `samplesPerCall`: Number of samples per callback.

**Returns**:

- 0: Success
- < 0: Failure

##### setMixedAudioFrameParameters

```java
public int setMixedAudioFrameParameters(int channels, int sampleRateHz, int samplesPerCall)
```

Sets the mixed audio frame parameters.

**Parameters**:

- `channels`: Number of channels.
- `sampleRateHz`: Sample rate (Hz).
- `samplesPerCall`: Number of samples per callback.

**Returns**:

- 0: Success
- < 0: Failure

##### setPlaybackAudioFrameBeforeMixingParameters

```java
public int setPlaybackAudioFrameBeforeMixingParameters(int channels, int sampleRateHz)
```

Sets the parameters for the playback audio frame before mixing.

**Parameters**:

- `channels`: Number of channels.
- `sampleRateHz`: Sample rate (Hz).

**Returns**:

- 0: Success
- < 0: Failure

##### subscribeVideo

```java
public int subscribeVideo(String userId, VideoSubscriptionOptions options)
```

Subscribes to the video stream of a specified remote user.

**Parameters**:

- `userId`: The ID of the remote user to subscribe to.
- `options`: Video subscription options (`VideoSubscriptionOptions`).

**Returns**:

- 0: Success
- < 0: Failure

##### subscribeAllVideo

```java
public int subscribeAllVideo(VideoSubscriptionOptions options)
```

Subscribes to the video streams of all remote users in the channel.

**Parameters**:

- `options`: Video subscription options (`VideoSubscriptionOptions`).

**Returns**:

- 0: Success
- < 0: Failure

##### unsubscribeVideo

```java
public int unsubscribeVideo(String userId)
```

Unsubscribes from the video stream of a specified remote user.

**Parameters**:

- `userId`: The ID of the remote user to unsubscribe from.

**Returns**:

- 0: Success
- < 0: Failure

##### unsubscribeAllVideo

```java
public int unsubscribeAllVideo()
```

Unsubscribes from the video streams of all remote users in the channel.

**Returns**:

- 0: Success
- < 0: Failure

##### setAudioVolumeIndicationParameters

```java
public int setAudioVolumeIndicationParameters(int intervalInMs, int smooth, boolean reportVad)
```

Sets the parameters for audio volume indication, controlling the `onAudioVolumeIndication` callback.

**Parameters**:

- `intervalInMs`: Callback interval (milliseconds), <= 0 disables it.
- `smooth`: Smoothing factor, recommended value is 3.
- `reportVad`: Whether to report voice activity status (`boolean`).

**Returns**:

- 0: Success
- < 0: Failure

##### getMediaControlPacketSender

```java
public AgoraMediaCtrlPacketSender getMediaControlPacketSender()
```

Gets the media control packet sender instance, used for sending custom media control messages.

**Returns**:

- An `AgoraMediaCtrlPacketSender` instance.

##### registerMediaControlPacketReceiver

```java
public int registerMediaControlPacketReceiver(AgoraMediaPacketReceiver agoraMediaPacketReceiver)
```

Registers a media control packet receiver to receive media control messages sent by remote users.

**Parameters**:

- `agoraMediaPacketReceiver`: The `AgoraMediaPacketReceiver` instance to register.

**Returns**:

- 0: Success
- < 0: Failure

##### unregisterMediaControlPacketReceiver

```java
public int unregisterMediaControlPacketReceiver(AgoraMediaPacketReceiver agoraMediaPacketReceiver)
```

Unregisters the media control packet receiver.

**Parameters**:

- `agoraMediaPacketReceiver`: The `AgoraMediaPacketReceiver` instance to unregister.

**Returns**:

- 0: Success
- < 0: Failure

##### sendIntraRequest

```java
public int sendIntraRequest(String userId)
```

Requests a key frame (I-frame) from the specified remote user. Typically used to refresh the video display when it freezes or appears corrupted.

**Parameters**:

- `userId`: The remote user ID.

**Returns**:

- 0: Success
- < 0: Failure

##### setAudioScenario

```java
public int setAudioScenario(int scenarioType)
```

Sets the audio application scenario. Different scenarios apply different audio optimization strategies.

**Parameters**:

- `scenarioType`: Audio scenario type, use constants from `Constants.AUDIO_SCENARIO_TYPE`.

**Returns**:

- 0: Success
- < 0: Failure

##### sendAudioMetaData

```java
public int sendAudioMetaData(byte[] metaData)
```

Sends audio metadata. The metadata is attached to audio packets sent to remote users.

**Parameters**:

- `metaData`: The metadata byte array to send.

**Returns**:

- 0: Success
- < 0: Failure

### AgoraLocalAudioTrack

The `AgoraLocalAudioTrack` class represents a local audio track, providing control functions for audio.

```java
public class AgoraLocalAudioTrack {
    // Constructor
    public AgoraLocalAudioTrack(long cptr)
}
```

#### Methods

##### getNativeHandle

```java
public long getNativeHandle()
```

Gets the native handle of the local audio track.

**Returns**:

- The native handle of the local audio track.

##### destroy

```java
public synchronized void destroy()
```

Destroys the local audio track.
Ensures that the native resources are released.

##### setEnabled

```java
public void setEnabled(int enable)
```

Enables or disables the local audio track.

**Parameters**:

- `enable`: 1 to enable, 0 to disable.

##### isEnabled

```java
public int isEnabled()
```

Checks if the local audio track is enabled.

**Returns**:

- 1: Enabled
- 0: Disabled

##### getState

```java
public int getState()
```

Gets the current state of the local audio track. Refer to `Constants.LOCAL_AUDIO_STREAM_STATE`.

**Returns**:

- The state of the audio track.

##### getStats

```java
public LocalAudioTrackStats getStats()
```

Retrieves the statistics of the local audio track.

**Returns**:

- An instance of `LocalAudioTrackStats` containing the statistics. Call `destroyStats` to release it after use.

##### destroyStats

```java
public void destroyStats(LocalAudioTrackStats stats)
```

Destroys the provided `LocalAudioTrackStats` object obtained via `getStats`.

**Parameters**:

- `stats`: The `LocalAudioTrackStats` object to be destroyed.

##### adjustPublishVolume

```java
public int adjustPublishVolume(int volume)
```

Adjusts the publish volume of the local audio track.

**Parameters**:

- `volume`: The volume level to set, range [0, 100].

**Returns**:

- 0: Success
- < 0: Failure

##### getPublishVolume

```java
public int getPublishVolume(Out volume)
```

Gets the current publish volume of the local audio track.

**Parameters**:

- `volume`: An `Out<Integer>` object to store the volume level.

**Returns**:

- 0: Success
- < 0: Failure

##### enableLocalPlayback

```java
public int enableLocalPlayback(int enable)
```

Enables or disables local playback of the audio track.

**Parameters**:

- `enable`: 1 to enable, 0 to disable.

**Returns**:

- 0: Success
- < 0: Failure

##### enableEarMonitor

```java
public int enableEarMonitor(int enable, int includeAudiFilter)
```

Enables or disables ear monitoring (in-ear monitoring).

**Parameters**:

- `enable`: 1 to enable, 0 to disable.
- `includeAudiFilter`: 1 to include audio filters in the monitoring signal, 0 to exclude.

**Returns**:

- 0: Success
- < 0: Failure

##### setMaxBufferedAudioFrameNumber

```java
public void setMaxBufferedAudioFrameNumber(int number)
```

Sets the maximum number of buffered audio frames for custom audio tracks.

**Parameters**:

- `number`: The maximum number of frames to buffer.

##### clearSenderBuffer

```java
public int clearSenderBuffer()
```

Clears the sender buffer for custom audio tracks.

**Returns**:

- 0: Success
- < 0: Failure

##### setSendDelayMs

```java
public void setSendDelayMs(int delayMs)
```

Sets the send delay in milliseconds for custom audio tracks.

**Parameters**:

- `delayMs`: The delay in milliseconds.

### AgoraLocalVideoTrack

The `AgoraLocalVideoTrack` class represents a local video track, providing control functions for video.

```java
public class AgoraLocalVideoTrack {
    // Constructor
    public AgoraLocalVideoTrack(long cptr)
}
```

#### Methods

##### getNativeHandle

```java
public long getNativeHandle()
```

Gets the native handle of the local video track.

**Returns**:

- The native handle of the local video track.

##### destroy

```java
public synchronized void destroy()
```

Destroys the local video track.
Ensures that the native resources are released.

##### setEnabled

```java
public void setEnabled(int enable)
```

Enables or disables the local video track.

**Parameters**:

- `enable`: 1 to enable, 0 to disable.

##### setVideoEncoderConfig

```java
public int setVideoEncoderConfig(VideoEncoderConfig config)
```

Sets the video encoder configuration.

**Parameters**:

- `config`: The video encoder configuration (`VideoEncoderConfig`) to set.

**Returns**:

- 0: Success
- < 0: Failure

##### enableSimulcastStream

```java
public int enableSimulcastStream(int enabled, SimulcastStreamConfig config)
```

Enables or disables the simulcast stream feature.

**Parameters**:

- `enabled`: 1 to enable, 0 to disable.
- `config`: The simulcast stream configuration (`SimulcastStreamConfig`).

**Returns**:

- 0: Success
- < 0: Failure

##### getState

```java
public int getState()
```

Gets the current state of the local video track. Refer to `Constants.LOCAL_VIDEO_STREAM_STATE`.

**Returns**:

- The state of the video track.

##### getStatistics

```java
public LocalVideoTrackStats getStatistics()
```

Retrieves the statistics of the local video track.

**Returns**:

- An instance of `LocalVideoTrackStats` containing the statistics. Call `destroyStatistics` to release it after use.

##### destroyStatistics

```java
public void destroyStatistics(LocalVideoTrackStats stats)
```

Destroys the provided `LocalVideoTrackStats` object obtained via `getStatistics`.

**Parameters**:

- `stats`: The `LocalVideoTrackStats` object to be destroyed.

### AgoraRemoteAudioTrack

The `AgoraRemoteAudioTrack` class represents a remote audio track, providing functions to get information about the remote audio.

```java
public class AgoraRemoteAudioTrack {
    // Constructor
    public AgoraRemoteAudioTrack(long cptr)
}
```

#### Methods

##### getNativeHandle

```java
public long getNativeHandle()
```

Gets the native handle of the remote audio track.

**Returns**:

- The native handle of the remote audio track.

##### destroy

```java
public void destroy()
```

Destroys the remote audio track.
Ensures that the native resources are released.

##### getStatistics

```java
public RemoteAudioTrackStats getStatistics()
```

Retrieves the statistics of the remote audio track.

**Returns**:

- An instance of `RemoteAudioTrackStats` containing the statistics. Call `destroyStatistics` to release it after use.

##### destroyStatistics

```java
public void destroyStatistics(RemoteAudioTrackStats stats)
```

Destroys the provided `RemoteAudioTrackStats` object obtained via `getStatistics`.

**Parameters**:

- `stats`: The `RemoteAudioTrackStats` object to be destroyed.

##### getState

```java
public int getState()
```

Gets the current state of the remote audio track. Refer to `Constants.REMOTE_AUDIO_STATE`.

**Returns**:

- The state of the remote audio track.

##### registerMediaPacketReceiver

```java
public int registerMediaPacketReceiver(AgoraMediaPacketReceiver agoraMediaPacketReceiver)
```

Registers a media packet receiver for this track.

**Parameters**:

- `agoraMediaPacketReceiver`: The media packet receiver (`AgoraMediaPacketReceiver`) to register.

**Returns**:

- 0: Success
- < 0: Failure

##### unregisterMediaPacketReceiver

```java
public int unregisterMediaPacketReceiver(AgoraMediaPacketReceiver agoraMediaPacketReceiver)
```

Unregisters the media packet receiver.

**Parameters**:

- `agoraMediaPacketReceiver`: The media packet receiver (`AgoraMediaPacketReceiver`) to unregister.

**Returns**:

- 0: Success
- < 0: Failure

### AgoraRemoteVideoTrack

The `AgoraRemoteVideoTrack` class represents a remote video track, providing functions to get information about the remote video.

```java
public class AgoraRemoteVideoTrack {
    // Constructor
    public AgoraRemoteVideoTrack(long cptr)
}
```

#### Member Variables

##### mediaPacketReceiver

```java
private IMediaPacketReceiver mediaPacketReceiver
```

The media packet receiver associated with this track.

#### Methods

##### getNativeHandle

```java
public long getNativeHandle()
```

Gets the native handle of the remote video track.

**Returns**:

- The native handle of the remote video track.

##### destroy

```java
public void destroy()
```

Destroys the remote video track.
Ensures that the native resources are released.

##### getStatistics

```java
public RemoteVideoTrackStats getStatistics()
```

Retrieves the statistics of the remote video track.

**Returns**:

- An instance of `RemoteVideoTrackStats` containing the statistics. Call `destroyStatistics` to release it after use.

##### destroyStatistics

```java
public void destroyStatistics(RemoteVideoTrackStats stats)
```

Destroys the provided `RemoteVideoTrackStats` object obtained via `getStatistics`.

**Parameters**:

- `stats`: The `RemoteVideoTrackStats` object to be destroyed.

##### getState

```java
public int getState()
```

Gets the current state of the remote video track. Refer to `Constants.REMOTE_VIDEO_STATE`.

**Returns**:

- The state of the remote video track.

##### getTrackInfo

```java
public VideoTrackInfo getTrackInfo()
```

Gets the video track information.

**Returns**:

- An instance of `VideoTrackInfo`. Call `destroyTrackInfo` to release it after use.

##### destroyTrackInfo

```java
public void destroyTrackInfo(VideoTrackInfo info)
```

Destroys the provided `VideoTrackInfo` object obtained via `getTrackInfo`.

**Parameters**:

- `info`: The `VideoTrackInfo` object to be destroyed.

##### registerVideoEncodedImageReceiver

```java
public int registerVideoEncodedImageReceiver(AgoraVideoEncodedImageReceiver agoraVideoEncodedImageReceiver)
```

Registers a receiver for encoded video images from this track.

**Parameters**:

- `agoraVideoEncodedImageReceiver`: The encoded video image receiver (`AgoraVideoEncodedImageReceiver`) to register.

**Returns**:

- 0: Success
- < 0: Failure

##### unregisterVideoEncodedImageReceiver

```java
public int unregisterVideoEncodedImageReceiver(AgoraVideoEncodedImageReceiver agoraVideoEncodedImageReceiver)
```

Unregisters the encoded video image receiver.

**Parameters**:

- `agoraVideoEncodedImageReceiver`: The encoded video image receiver (`AgoraVideoEncodedImageReceiver`) to unregister.

**Returns**:

- 0: Success
- < 0: Failure

##### registerMediaPacketReceiver

```java
public int registerMediaPacketReceiver(IMediaPacketReceiver agoraMediaPacketReceiver)
```

Registers a media packet receiver for this track.

**Parameters**:

- `agoraMediaPacketReceiver`: The media packet receiver (`IMediaPacketReceiver`) to register.

**Returns**:

- 0: Success
- < 0: Failure

##### unregisterMediaPacketReceiver

```java
public int unregisterMediaPacketReceiver(IMediaPacketReceiver agoraMediaPacketReceiver)
```

Unregisters the media packet receiver.

**Parameters**:

- `agoraMediaPacketReceiver`: The media packet receiver (`IMediaPacketReceiver`) to unregister.

**Returns**:

- 0: Success
- < 0: Failure

### AgoraAudioPcmDataSender

The `AgoraAudioPcmDataSender` class is used for sending PCM audio data.

```java
public class AgoraAudioPcmDataSender {
    // Constructor
    public AgoraAudioPcmDataSender(long cptr)
}
```

#### Methods

##### send (Deprecated)

```java
@Deprecated
public int send(byte[] audioData, int captureTimestamp, int samplesPerChannel, int bytesPerSample, int numberOfChannels, int sampleRate)
```

Sends audio data.

**Parameters**:

- `audioData`: The audio data (`byte[]`).
- `captureTimestamp`: The capture timestamp (ms).
- `samplesPerChannel`: The number of samples per channel.
- `bytesPerSample`: The number of bytes per sample (e.g., 2 for 16-bit).
- `numberOfChannels`: The number of channels.
- `sampleRate`: The sample rate (Hz).

**Returns**:

- The result of sending the audio data (0 for success, < 0 for failure).

**Note**: This method is deprecated. Use `sendAudioPcmData(AudioFrame)` instead.

##### sendAudioPcmData

```java
public int sendAudioPcmData(AudioFrame audioFrame)
```

Sends audio data using an `AudioFrame` object.

**Parameters**:

- `audioFrame`: The audio frame (`AudioFrame`) containing the data and metadata.

**Returns**:

- The result of sending the audio data (0 for success, < 0 for failure).

##### destroy

```java
public synchronized void destroy()
```

Destroys the audio data sender instance and releases associated resources.

### AgoraVideoFrameSender

The `AgoraVideoFrameSender` class is used for sending video frames (typically from an external source).

```java
public class AgoraVideoFrameSender {
    // Constructor
    public AgoraVideoFrameSender(long cptr)
}
```

#### Methods

##### sendVideoFrame

```java
public int sendVideoFrame(ExternalVideoFrame frame)
```

Sends a video frame to the Agora server.

**Parameters**:

- `frame`: The external video frame (`ExternalVideoFrame`) to send.

**Returns**:

- The result of sending the video frame (0 for success, < 0 for failure).

##### destroy

```java
public synchronized void destroy()
```

Destroys the video frame sender instance and releases associated resources.

### AgoraVideoEncodedImageSender

The `AgoraVideoEncodedImageSender` class is used for sending encoded video images.

```java
public class AgoraVideoEncodedImageSender {
    // Constructor
    public AgoraVideoEncodedImageSender(long cptr)
}
```

#### Methods

##### sendEncodedVideoImage

```java
public int sendEncodedVideoImage(byte[] imageBuffer, EncodedVideoFrameInfo info)
```

Sends an encoded video image to the Agora server.

**Parameters**:

- `imageBuffer`: The encoded video image data (`byte[]`).
- `info`: Information about the encoded video image (`EncodedVideoFrameInfo`).

**Returns**:

- The result of sending the encoded video image (0 for success, < 0 for failure).

##### destroy

```java
public synchronized void destroy()
```

Destroys the video encoded image sender instance and releases associated resources.

### AgoraAudioEncodedFrameSender

The `AgoraAudioEncodedFrameSender` class is used for sending encoded audio frames.

```java
public class AgoraAudioEncodedFrameSender {
    // Constructor
    public AgoraAudioEncodedFrameSender(long cptr)
}
```

#### Methods

##### sendEncodedAudioFrame

```java
public int sendEncodedAudioFrame(byte[] payloadData, EncodedAudioFrameInfo info)
```

Sends an encoded audio frame to the Agora server.

**Parameters**:

- `payloadData`: The encoded audio data (`byte[]`).
- `info`: Information about the encoded audio frame (`EncodedAudioFrameInfo`).

**Returns**:

- The result of sending the encoded audio frame (0 for success, < 0 for failure).

##### destroy

```java
public synchronized void destroy()
```

Destroys the audio encoded frame sender instance and releases associated resources.

### AgoraParameter

The `AgoraParameter` class is used to set and get Agora SDK parameters.

```java
public class AgoraParameter {
    // Constructor
    public AgoraParameter(long cptr)
}
```

#### Methods

##### destroy

```java
public void destroy()
```

Destroys the AgoraParameter object.

##### setInt

```java
public int setInt(String key, int value)
```

Sets an integer parameter.

**Parameters**:

- `key`: The key of the parameter.
- `value`: The integer value to set.

**Returns**:

- 0: Success
- < 0: Failure

##### setBool

```java
public int setBool(String key, boolean value)
```

Sets a boolean parameter.

**Parameters**:

- `key`: The key of the parameter.
- `value`: The boolean value to set.

**Returns**:

- 0: Success
- < 0: Failure

##### setUint

```java
public int setUint(String key, int value)
```

Sets an unsigned integer parameter.

**Parameters**:

- `key`: The key of the parameter.
- `value`: The unsigned integer value to set.

**Returns**:

- 0: Success
- < 0: Failure

##### setNumber

```java
public int setNumber(String key, double value)
```

Sets a numeric parameter.

**Parameters**:

- `key`: The key of the parameter.
- `value`: The numeric value to set.

**Returns**:

- 0: Success
- < 0: Failure

##### setString

```java
public int setString(String key, String value)
```

Sets a string parameter.

**Parameters**:

- `key`: The key of the parameter.
- `value`: The string value to set.

**Returns**:

- 0: Success
- < 0: Failure

##### setArray

```java
public int setArray(String key, String json_src)
```

Sets an array parameter using a JSON string.

**Parameters**:

- `key`: The key of the parameter.
- `json_src`: A JSON string representing the array.

**Returns**:

- 0: Success
- < 0: Failure

##### setParameters

```java
public int setParameters(String json_src)
```

Sets multiple parameters using a JSON string.

**Parameters**:

- `json_src`: A JSON string containing multiple parameters.

**Returns**:

- 0: Success
- < 0: Failure

##### getInt

```java
public int getInt(String key, Out value)
```

Gets an integer parameter.

**Parameters**:

- `key`: The key of the parameter.
- `value`: An `Out<Integer>` object to store the parameter value.

**Returns**:

- 0: Success
- < 0: Failure

##### getBool

```java
public int getBool(String key, Out value)
```

Gets a boolean parameter.

**Parameters**:

- `key`: The key of the parameter.
- `value`: An `Out<Boolean>` object to store the parameter value.

**Returns**:

- 0: Success
- < 0: Failure

##### getUint

```java
public int getUint(String key, Out value)
```

Gets an unsigned integer parameter.

**Parameters**:

- `key`: The key of the parameter.
- `value`: An `Out<Integer>` object to store the parameter value.

**Returns**:

- 0: Success
- < 0: Failure

##### getNumber

```java
public int getNumber(String key, Out value)
```

Gets a numeric parameter.

**Parameters**:

- `key`: The key of the parameter.
- `value`: An `Out<Double>` object to store the parameter value.

**Returns**:

- 0: Success
- < 0: Failure

##### getString

```java
public int getString(String key, Out value)
```

Gets a string parameter.

**Parameters**:

- `key`: The key of the parameter.
- `value`: An `Out<String>` object to store the parameter value.

**Returns**:

- 0: Success
- < 0: Failure

## 观察者接口

### IRtcConnObserver

The `IRtcConnObserver` interface is used to listen to RTC connection status and events.

```java
public interface IRtcConnObserver {
    // Method declarations
}
```

#### Methods

##### onConnected

```java
public void onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason)
```

Occurs when the connection state between the SDK and the Agora channel changes to `CONNECTION_STATE_CONNECTED`(3).

**Parameters**:

- `agoraRtcConn`: The connection object.
- `connInfo`: The information of the connection. See [`RtcConnInfo`](#rtcconninfo).
- `reason`: The reason of the connection state change. See [`ConnectionChangedReasonType`](#connectionchangedreasontype).

##### onDisconnected

```java
public void onDisconnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason)
```

Occurs when the connection state between the SDK and the Agora channel changes to `CONNECTION_STATE_DISCONNECTED`(1).

**Parameters**:

- `agoraRtcConn`: The connection object.
- `connInfo`: The information of the connection. See [`RtcConnInfo`](#rtcconninfo).
- `reason`: The reason of the connection state change. See [`ConnectionChangedReasonType`](#connectionchangedreasontype).

##### onConnecting

```java
public void onConnecting(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason)
```

Occurs when the connection state between the SDK and the Agora channel changes to `CONNECTION_STATE_CONNECTING`(2).

**Parameters**:

- `agoraRtcConn`: The connection object.
- `connInfo`: The information of the connection. See [`RtcConnInfo`](#rtcconninfo).
- `reason`: The reason of the connection state change. See [`ConnectionChangedReasonType`](#connectionchangedreasontype).

##### onReconnecting

```java
public void onReconnecting(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason)
```

Occurs when the connection state between the SDK and the Agora channel changes to `CONNECTION_STATE_RECONNECTING`(4).

**Parameters**:

- `agoraRtcConn`: The connection object.
- `connInfo`: The information of the connection. See [`RtcConnInfo`](#rtcconninfo).
- `reason`: The reason of the connection state change. See [`ConnectionChangedReasonType`](#connectionchangedreasontype).

##### onReconnected (Deprecated)

```java
@Deprecated
public void onReconnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason)
```

Occurs when the connection is reestablished after a disconnection.

**Deprecated**: Use `onConnected` with reason `CONNECTION_CHANGED_REJOIN_SUCCESS` instead.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `connInfo`: The information of the connection. See [`RtcConnInfo`](#rtcconninfo).
- `reason`: The reason of the connection state change. See [`ConnectionChangedReasonType`](#connectionchangedreasontype).

##### onConnectionLost

```java
public void onConnectionLost(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo)
```

Occurs when the SDK loses connection with the Agora channel.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `connInfo`: The information of the connection. See [`RtcConnInfo`](#rtcconninfo).

##### onLastmileQuality

```java
public void onLastmileQuality(AgoraRtcConn agoraRtcConn, int quality)
```

Reports the quality of the last-mile network. The SDK triggers this callback within two seconds after the app calls `startLastmileProbeTest`.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `quality`: Quality of the last-mile network. See [`QualityType`](#qualitytype).

##### onLastmileProbeResult

```java
public void onLastmileProbeResult(AgoraRtcConn agoraRtcConn, LastmileProbeResult result)
```

Reports the result of the last-mile network probe test. The SDK triggers this callback within 30 seconds after the app calls `startLastmileProbeTest`.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `result`: The result of the last-mile network probe test. See [`LastmileProbeResult`](#lastmileproberesult).

##### onTokenPrivilegeWillExpire

```java
public void onTokenPrivilegeWillExpire(AgoraRtcConn agoraRtcConn, String token)
```

Occurs when the token expires in 30 seconds. The SDK triggers this callback to remind the app to get a new token before the token privilege expires. Upon receiving this callback, you must generate a new token on your server and call `renewToken` to pass the new token to the SDK.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `token`: The token that expires in 30 seconds.

##### onTokenPrivilegeDidExpire

```java
public void onTokenPrivilegeDidExpire(AgoraRtcConn agoraRtcConn)
```

Occurs when the token has expired. Upon receiving this callback, you must generate a new token on your server and call `renewToken` to pass the new token to the SDK.

**Parameters**:

- `agoraRtcConn`: The connection object.

##### onConnectionFailure

```java
public void onConnectionFailure(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason)
```

Occurs when the connection state between the SDK and the Agora channel changes to `CONNECTION_STATE_FAILED`(5).

**Parameters**:

- `agoraRtcConn`: The connection object.
- `connInfo`: The connection information. See [`RtcConnInfo`](#rtcconninfo).
- `reason`: The reason of the connection state change. See [`ConnectionChangedReasonType`](#connectionchangedreasontype).

##### onConnectionLicenseValidationFailure

```java
public void onConnectionLicenseValidationFailure(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason)
```

Occurs when connection license verification fails.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `connInfo`: The connection information. See [`RtcConnInfo`](#rtcconninfo).
- `reason`: The reason for the license validation failure.

##### onUserJoined

```java
public void onUserJoined(AgoraRtcConn agoraRtcConn, String userId)
```

Occurs when a remote user joins the channel. You can get the ID of the remote user in this callback.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `userId`: The ID of the remote user who joins the channel.

##### onUserLeft

```java
public void onUserLeft(AgoraRtcConn agoraRtcConn, String userId, int reason)
```

Occurs when a remote user leaves the channel. You can know why the user leaves the channel through the `reason` parameter.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `userId`: The ID of the user who leaves the channel.
- `reason`: The reason why the remote user leaves the channel. See [`UserOfflineReasonType`](#userofflinereasontype).

##### onTransportStats

```java
public void onTransportStats(AgoraRtcConn agoraRtcConn, RtcStats stats)
```

Reports the transport statistics of the connection. The SDK triggers this callback once every two seconds when the connection state is `CONNECTION_STATE_CONNECTED`.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `stats`: The transport statistics. See [`RtcStats`](#rtcstats).

##### onChangeRoleSuccess

```java
public void onChangeRoleSuccess(AgoraRtcConn agoraRtcConn, int oldRole, int newRole)
```

Occurs when the role of the local user changes successfully.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `oldRole`: The previous role of the local user. See [`ClientRoleType`](#clientroletype).
- `newRole`: The current role of the local user. See [`ClientRoleType`](#clientroletype).

##### onChangeRoleFailure

```java
public void onChangeRoleFailure(AgoraRtcConn agoraRtcConn)
```

Occurs when the local user fails to change the user role.

**Parameters**:

- `agoraRtcConn`: The connection object.

##### onUserNetworkQuality

```java
public void onUserNetworkQuality(AgoraRtcConn agoraRtcConn, String userId, int txQuality, int rxQuality)
```

Reports the network quality of each user. The SDK triggers this callback once every two seconds to report the uplink and downlink network conditions of each user in the channel, including the local user.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `userId`: The ID of the user. If `userId` is empty, this callback reports the network quality of the local user.
- `txQuality`: The uplink network quality. See [`QualityType`](#qualitytype).
- `rxQuality`: The downlink network quality. See [`QualityType`](#qualitytype).

##### onNetworkTypeChanged

```java
public void onNetworkTypeChanged(AgoraRtcConn agoraRtcConn, int type)
```

Occurs when the network type is changed.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `type`: The current network type. See [`NetworkType`](#networktype).

##### onApiCallExecuted

```java
public void onApiCallExecuted(AgoraRtcConn agoraRtcConn, int err, String api, String result)
```

Reports the result of an API call execution. This is usually used for asynchronous operations.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `err`: The error code returned by the API call. See [`ErrorCodeType`](#errorcodetype).
- `api`: The API name that was called.
- `result`: The result string of the API call.

##### onContentInspectResult

```java
public void onContentInspectResult(AgoraRtcConn agoraRtcConn, int result)
```

Reports the result of content inspection.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `result`: The content inspection result. See [`ContentInspectResult`](#contentinspectresult).

##### onSnapshotTaken

```java
public void onSnapshotTaken(AgoraRtcConn agoraRtcConn, String channel, int userId, String filePath, int width, int height, int errCode)
```

Occurs when a snapshot is successfully taken.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `channel`: The channel name.
- `userId`: The user ID. If the user ID is 0, the snapshot is for the local user.
- `filePath`: The local path of the snapshot file.
- `width`: The width (pixels) of the snapshot.
- `height`: The height (pixels) of the snapshot.
- `errCode`: The error code. 0 means success.

##### onError

```java
public void onError(AgoraRtcConn agoraRtcConn, int error, String msg)
```

Reports an error during SDK runtime.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `error`: The error code. See [`ErrorCodeType`](#errorcodetype).
- `msg`: The error message.

##### onWarning

```java
public void onWarning(AgoraRtcConn agoraRtcConn, int warning, String msg)
```

Reports a warning during SDK runtime.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `warning`: The warning code. See [Warning Codes](#warning-codes).
- `msg`: The warning message.

##### onChannelMediaRelayStateChanged

```java
public void onChannelMediaRelayStateChanged(AgoraRtcConn agoraRtcConn, int state, int code)
```

Occurs when the state of the channel media relay changes.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `state`: The state code. See [Channel Media Relay State](#channel-media-relay-state).
- `code`: The error code. See [Channel Media Relay Error Code](#channel-media-relay-error-code).

##### onLocalUserRegistered

```java
public void onLocalUserRegistered(AgoraRtcConn agoraRtcConn, int uid, String userAccount)
```

Occurs when the local user successfully registers a user account by calling the `joinChannelWithUserAccount` method. This callback reports the user ID and user account of the local user.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `uid`: The ID of the local user.
- `userAccount`: The user account of the local user.

##### onUserAccountUpdated

```java
public void onUserAccountUpdated(AgoraRtcConn agoraRtcConn, int uid, String userAccount)
```

Occurs when the user account information is updated. Technical Preview, please do not depend on this event.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `uid`: The ID of the local user.
- `userAccount`: The user account of the local user.

##### onStreamMessageError

```java
public void onStreamMessageError(AgoraRtcConn agoraRtcConn, String userId, int streamId, int code, int missed, int cached)
```

Reports the error that occurs when receiving data stream messages.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `userId`: The ID of the user sending the data stream.
- `streamId`: The ID of the data stream.
- `code`: The error code. See [`ErrorCodeType`](#errorcodetype).
- `missed`: The number of lost messages.
- `cached`: The number of incoming cached messages when the data stream is interrupted.

##### onEncryptionError

```java
public void onEncryptionError(AgoraRtcConn agoraRtcConn, int errorType)
```

Occurs when an encryption error happens during the transmission.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `errorType`: The type of the encryption error. See [`EncryptionErrorType`](#encryptionerrortype).

##### onUploadLogResult

```java
public void onUploadLogResult(AgoraRtcConn agoraRtcConn, String requestId, int success, int reason)
```

Reports the user log upload result.

**Parameters**:

- `agoraRtcConn`: The connection object.
- `requestId`: Request ID of the upload.
- `success`: Whether the upload was successful (1 for success, 0 for failure).
- `reason`: Reason for the upload result. See [`UploadErrorReason`](#uploaderrorreason).

### ILocalUserObserver

The `ILocalUserObserver` interface is used to listen for status and events related to the local user, such as media stream publishing, subscription, statistics, etc.

```java
public interface ILocalUserObserver {
    // Method declarations
}
```

#### Methods

##### onAudioTrackPublishSuccess

```java
public void onAudioTrackPublishSuccess(AgoraLocalUser agoraLocalUser, AgoraLocalAudioTrack agoraLocalAudioTrack)
```

Triggered when a local audio track is published successfully.

**Parameters**:

- `agoraLocalUser`: The local user instance.
- `agoraLocalAudioTrack`: The successfully published local audio track.

##### onAudioTrackPublicationFailure

```java
public void onAudioTrackPublicationFailure(AgoraLocalUser agoraLocalUser, AgoraLocalAudioTrack agoraLocalAudioTrack, int error)
```

Triggered when publishing a local audio track fails.

**Parameters**:

- `agoraLocalUser`: The local user instance.
- `agoraLocalAudioTrack`: The local audio track that failed to publish.
- `error`: Error code (`Constants.RTC_ERROR_CODE`).

##### onLocalAudioTrackStateChanged

```java
public void onLocalAudioTrackStateChanged(AgoraLocalUser agoraLocalUser, AgoraLocalAudioTrack agoraLocalAudioTrack, int state, int error)
```

Triggered when the state of a local audio track changes.

**Parameters**:

- `agoraLocalUser`: The local user instance.
- `agoraLocalAudioTrack`: The local audio track whose state changed.
- `state`: The new state (`Constants.LOCAL_AUDIO_STREAM_STATE`).
- `error`: Related error code (`Constants.LOCAL_AUDIO_STREAM_ERROR`).

##### onLocalAudioTrackStatistics

```java
public void onLocalAudioTrackStatistics(AgoraLocalUser agoraLocalUser, LocalAudioStats stats)
```

Reports the statistics of the local audio stream.

**Parameters**:

- `agoraLocalUser`: The local user instance.
- `stats`: Local audio statistics data (`LocalAudioStats`).

##### onRemoteAudioTrackStatistics

```java
public void onRemoteAudioTrackStatistics(AgoraLocalUser agoraLocalUser, AgoraRemoteAudioTrack agoraRemoteAudioTrack, RemoteAudioTrackStats stats)
```

Reports the statistics of the received remote audio stream.

**Parameters**:

- `agoraLocalUser`: The local user instance.
- `agoraRemoteAudioTrack`: The remote audio track.
- `stats`: Remote audio statistics data (`RemoteAudioTrackStats`).

##### onUserAudioTrackSubscribed

```java
public void onUserAudioTrackSubscribed(AgoraLocalUser agoraLocalUser, String userId, AgoraRemoteAudioTrack agoraRemoteAudioTrack)
```

Triggered when a remote user's audio track is successfully subscribed to.

**Parameters**:

- `agoraLocalUser`: The local user instance.
- `userId`: The remote user ID.
- `agoraRemoteAudioTrack`: The subscribed remote audio track.

##### onUserAudioTrackStateChanged

```java
public void onUserAudioTrackStateChanged(AgoraLocalUser agoraLocalUser, String userId, AgoraRemoteAudioTrack agoraRemoteAudioTrack, int state, int reason, int elapsed)
```

当远端用户的音频轨道状态发生改变时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `userId`：远端用户 ID。
- `agoraRemoteAudioTrack`：远端音频轨道。
- `state`：新的状态 (`Constants.REMOTE_AUDIO_STATE`)。
- `reason`：状态改变的原因 (`Constants.REMOTE_AUDIO_STATE_REASON`)。
- `elapsed`：从订阅开始到触发此回调的耗时（毫秒）。

##### onAudioSubscribeStateChanged

```java
public void onAudioSubscribeStateChanged(AgoraLocalUser agoraLocalUser, String channel, String userId, int oldState, int newState, int elapseSinceLastState)
```

当音频订阅状态发生改变时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `channel`：频道名。
- `userId`：远端用户 ID。
- `oldState`：之前的订阅状态 (`Constants.STREAM_SUBSCRIBE_STATE`)。
- `newState`：当前的订阅状态 (`Constants.STREAM_SUBSCRIBE_STATE`)。
- `elapseSinceLastState`：距离上次状态改变的耗时（毫秒）。

##### onAudioPublishStateChanged

```java
public void onAudioPublishStateChanged(AgoraLocalUser agoraLocalUser, String channel, int oldState, int newState, int elapseSinceLastState)
```

当音频发布状态发生改变时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `channel`：频道名。
- `oldState`：之前的发布状态 (`Constants.STREAM_PUBLISH_STATE`)。
- `newState`：当前的发布状态 (`Constants.STREAM_PUBLISH_STATE`)。
- `elapseSinceLastState`：距离上次状态改变的耗时（毫秒）。

##### onFirstRemoteAudioFrame

```java
public void onFirstRemoteAudioFrame(AgoraLocalUser agoraLocalUser, String userId, int elapsed)
```

当收到指定远端用户的首帧音频时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `userId`：远端用户 ID。
- `elapsed`：从订阅开始到收到首帧的耗时（毫秒）。

##### onFirstRemoteAudioDecoded

```java
public void onFirstRemoteAudioDecoded(AgoraLocalUser agoraLocalUser, String userId, int elapsed)
```

当成功解码指定远端用户的首帧音频时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `userId`：远端用户 ID。
- `elapsed`：从订阅开始到解码首帧的耗时（毫秒）。

##### onVideoTrackPublishSuccess

```java
public void onVideoTrackPublishSuccess(AgoraLocalUser agoraLocalUser, AgoraLocalVideoTrack agoraLocalVideoTrack)
```

当本地视频轨道发布成功时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `agoraLocalVideoTrack`：成功发布的本地视频轨道。

##### onVideoTrackPublicationFailure

```java
public void onVideoTrackPublicationFailure(AgoraLocalUser agoraLocalUser, AgoraLocalVideoTrack agoraLocalVideoTrack, int error)
```

当本地视频轨道发布失败时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `agoraLocalVideoTrack`：发布失败的本地视频轨道。
- `error`：错误码 (`Constants.RTC_ERROR_CODE`)。

##### onLocalVideoTrackStateChanged

```java
public void onLocalVideoTrackStateChanged(AgoraLocalUser agoraLocalUser, AgoraLocalVideoTrack agoraLocalVideoTrack, int state, int error)
```

当本地视频轨道状态发生改变时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `agoraLocalVideoTrack`：状态改变的本地视频轨道。
- `state`：新的状态 (`Constants.LOCAL_VIDEO_STREAM_STATE`)。
- `error`：相关的错误码 (`Constants.LOCAL_VIDEO_STREAM_ERROR`)。

##### onLocalVideoTrackStatistics

```java
public void onLocalVideoTrackStatistics(AgoraLocalUser agoraLocalUser, AgoraLocalVideoTrack agoraLocalVideoTrack, LocalVideoTrackStats stats)
```

报告本地视频流的统计信息。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `agoraLocalVideoTrack`：本地视频轨道。
- `stats`：本地视频统计数据 (`LocalVideoTrackStats`)。

##### onUserVideoTrackSubscribed

```java
public void onUserVideoTrackSubscribed(AgoraLocalUser agoraLocalUser, String userId, VideoTrackInfo info, AgoraRemoteVideoTrack agoraRemoteVideoTrack)
```

当成功订阅远端用户的视频轨道时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `userId`：远端用户 ID。
- `info`：视频轨道信息 (`VideoTrackInfo`)。
- `agoraRemoteVideoTrack`：订阅到的远端视频轨道。

##### onUserVideoTrackStateChanged

```java
public void onUserVideoTrackStateChanged(AgoraLocalUser agoraLocalUser, String userId, AgoraRemoteVideoTrack agoraRemoteVideoTrack, int state, int reason, int elapsed)
```

当远端用户的视频轨道状态发生改变时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `userId`：远端用户 ID。
- `agoraRemoteVideoTrack`：远端视频轨道。
- `state`：新的状态 (`Constants.REMOTE_VIDEO_STATE`)。
- `reason`：状态改变的原因 (`Constants.REMOTE_VIDEO_STATE_REASON`)。
- `elapsed`：从订阅开始到触发此回调的耗时（毫秒）。

##### onRemoteVideoTrackStatistics

```java
public void onRemoteVideoTrackStatistics(AgoraLocalUser agoraLocalUser, AgoraRemoteVideoTrack agoraRemoteVideoTrack, RemoteVideoTrackStats stats)
```

报告接收到的远端视频流的统计信息。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `agoraRemoteVideoTrack`：远端视频轨道。
- `stats`：远端视频统计数据 (`RemoteVideoTrackStats`)。

##### onAudioVolumeIndication

```java
public void onAudioVolumeIndication(AgoraLocalUser agoraLocalUser, AudioVolumeInfo[] speakers, int totalVolume)
```

报告谁在说话以及说话者的音量。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `speakers`：包含说话者信息的数组 (`AudioVolumeInfo[]`)。如果为空，表示当前没有人说话。
- `totalVolume`：混音后的总音量，范围 [0, 255]。

##### onActiveSpeaker

```java
public void onActiveSpeaker(AgoraLocalUser agoraLocalUser, String userId)
```

当检测到活跃的说话者时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `userId`：当前最活跃的说话者 ID。

##### onRemoteVideoStreamInfoUpdated

```java
public void onRemoteVideoStreamInfoUpdated(AgoraLocalUser agoraLocalUser, RemoteVideoStreamInfo info)
```

当远端视频流的信息更新时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `info`：远端视频流信息 (`RemoteVideoStreamInfo`)。

##### onVideoSubscribeStateChanged

```java
public void onVideoSubscribeStateChanged(AgoraLocalUser agoraLocalUser, String channel, String userId, int oldState, int newState, int elapseSinceLastState)
```

当视频订阅状态发生改变时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `channel`：频道名。
- `userId`：远端用户 ID。
- `oldState`：之前的订阅状态 (`Constants.STREAM_SUBSCRIBE_STATE`)。
- `newState`：当前的订阅状态 (`Constants.STREAM_SUBSCRIBE_STATE`)。
- `elapseSinceLastState`：距离上次状态改变的耗时（毫秒）。

##### onVideoPublishStateChanged

```java
public void onVideoPublishStateChanged(AgoraLocalUser agoraLocalUser, String channel, int oldState, int newState, int elapseSinceLastState)
```

当视频发布状态发生改变时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `channel`：频道名。
- `oldState`：之前的发布状态 (`Constants.STREAM_PUBLISH_STATE`)。
- `newState`：当前的发布状态 (`Constants.STREAM_PUBLISH_STATE`)。
- `elapseSinceLastState`：距离上次状态改变的耗时（毫秒）。

##### onFirstRemoteVideoFrame

```java
public void onFirstRemoteVideoFrame(AgoraLocalUser agoraLocalUser, String userId, int width, int height, int elapsed)
```

当收到指定远端用户的首帧视频时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `userId`：远端用户 ID。
- `width`：视频宽度。
- `height`：视频高度。
- `elapsed`：从订阅开始到收到首帧的耗时（毫秒）。

##### onFirstRemoteVideoDecoded

```java
public void onFirstRemoteVideoDecoded(AgoraLocalUser agoraLocalUser, String userId, int width, int height, int elapsed)
```

当成功解码指定远端用户的首帧视频时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `userId`：远端用户 ID。
- `width`：视频宽度。
- `height`：视频高度。
- `elapsed`：从订阅开始到解码首帧的耗时（毫秒）。

##### onFirstRemoteVideoFrameRendered

```java
public void onFirstRemoteVideoFrameRendered(AgoraLocalUser agoraLocalUser, String userId, int width, int height, int elapsed)
```

当渲染出指定远端用户的首帧视频时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `userId`：远端用户 ID。
- `width`：视频宽度。
- `height`：视频高度。
- `elapsed`：从订阅开始到渲染首帧的耗时（毫秒）。

##### onVideoSizeChanged

```java
public void onVideoSizeChanged(AgoraLocalUser agoraLocalUser, String userId, int width, int height, int rotation)
```

当远端视频的尺寸或旋转角度发生改变时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `userId`：远端用户 ID。
- `width`：新的宽度。
- `height`：新的高度。
- `rotation`：新的旋转角度。

##### onUserInfoUpdated

```java
public void onUserInfoUpdated(AgoraLocalUser agoraLocalUser, String userId, int msg, int val)
```

当用户信息更新时触发（例如，音频或视频状态变化）。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `userId`：远端用户 ID。
- `msg`：更新的消息类型 (`Constants.USER_INFO_UPDATED_MSG`)。
- `val`：更新的值 (通常是 0 或 1)。

##### onIntraRequestReceived

```java
public void onIntraRequestReceived(AgoraLocalUser agoraLocalUser)
```

当收到远端用户发送的 I 帧请求时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。

##### onRemoteSubscribeFallbackToAudioOnly

```java
public void onRemoteSubscribeFallbackToAudioOnly(AgoraLocalUser agoraLocalUser, String userId, int isFallbackOrRecover)
```

当远端订阅的流因网络不佳回退为纯音频流，或从纯音频流恢复时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `userId`：远端用户 ID。
- `isFallbackOrRecover`：1 表示回退到纯音频，0 表示恢复到音视频。

##### onStreamMessage

```java
public void onStreamMessage(AgoraLocalUser agoraLocalUser, String userId, int streamId, byte[] data)
```

当收到远端用户通过数据流 (`sendStreamMessage`) 发送的消息时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `userId`：发送消息的远端用户 ID。
- `streamId`：数据流 ID。
- `data`：收到的消息内容 (String)。
- `length`：消息长度。

##### onUserStateChanged

```java
public void onUserStateChanged(AgoraLocalUser agoraLocalUser, String userId, int state)
```

当远端用户状态发生改变时触发（例如静音/取消静音、启用/禁用视频）。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `userId`：远端用户 ID。
- `state`：新的用户状态（具体含义取决于触发场景）。

##### onAudioTrackPublishStart

```java
public void onAudioTrackPublishStart(AgoraLocalUser agoraLocalUser, AgoraLocalAudioTrack agoraLocalAudioTrack)
```

当开始发布本地音频轨道时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `agoraLocalAudioTrack`：开始发布的本地音频轨道。

##### onAudioTrackUnpublished

```java
public void onAudioTrackUnpublished(AgoraLocalUser agoraLocalUser, AgoraLocalAudioTrack agoraLocalAudioTrack)
```

当取消发布本地音频轨道时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `agoraLocalAudioTrack`：取消发布的本地音频轨道。

##### onVideoTrackPublishStart

```java
public void onVideoTrackPublishStart(AgoraLocalUser agoraLocalUser, AgoraLocalVideoTrack agoraLocalVideoTrack)
```

当开始发布本地视频轨道时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `agoraLocalVideoTrack`：开始发布的本地视频轨道。

##### onVideoTrackUnpublished

```java
public void onVideoTrackUnpublished(AgoraLocalUser agoraLocalUser, AgoraLocalVideoTrack agoraLocalVideoTrack)
```

当取消发布本地视频轨道时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `agoraLocalVideoTrack`：取消发布的本地视频轨道。

##### onAudioMetaDataReceived

```java
public void onAudioMetaDataReceived(AgoraLocalUser agoraLocalUser, String userId, byte[] metaData)
```

当收到远端用户发送的音频元数据时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `userId`：发送元数据的远端用户 ID。
- `metaData`：收到的元数据 (byte[])。

### INetworkObserver

The `INetworkObserver` interface is used to listen for network status changes.

```java
public interface INetworkObserver {
    // Method declarations
}
```

#### Methods

##### onUplinkNetworkInfoUpdated

```java
public void onUplinkNetworkInfoUpdated(AgoraRtcConn agoraRtcConn, UplinkNetworkInfo info)
```

Triggered when uplink network information is updated.

**Parameters**:

- `agoraRtcConn`: The RTC connection instance.
- `info`: Uplink network information (`UplinkNetworkInfo`).

##### onDownlinkNetworkInfoUpdated

```java
public void onDownlinkNetworkInfoUpdated(AgoraRtcConn agoraRtcConn, DownlinkNetworkInfo info)
```

Triggered when downlink network information is updated.

**Parameters**:

- `agoraRtcConn`: The RTC connection instance.
- `info`: Downlink network information (`DownlinkNetworkInfo`).

### IVideoFrameObserver

The `IVideoFrameObserver` interface is used to listen for video frame events such as capture, pre-encoding, and rendering. By implementing this interface, you can obtain raw video data for processing.

```java
public interface IVideoFrameObserver {
    // Method declarations
}
```

#### Methods

##### onCaptureVideoFrame

```java
public int onCaptureVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame)
```

Triggered when a local camera video frame is captured.

**Parameters**:

- `agora_video_frame_observer`: The video frame observer instance (typically used in the native layer).
- `frame`: The captured video frame (`VideoFrame`).

**Returns**:

- Reserved by the SDK. Return 0.

##### onPreEncodeVideoFrame

```java
public int onPreEncodeVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame)
```

Triggered when a local camera video frame is ready to be encoded.

**Parameters**:

- `agora_video_frame_observer`: The video frame observer instance (typically used in the native layer).
- `frame`: The video frame to be encoded (`VideoFrame`).

**Returns**:

- Reserved by the SDK. Return 0.

##### onSecondaryCameraCaptureVideoFrame

```java
public int onSecondaryCameraCaptureVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame)
```

Triggered when a video frame from the secondary camera is captured.

**Parameters**:

- `agora_video_frame_observer`: The video frame observer instance.
- `frame`: The captured video frame (`VideoFrame`).

**Returns**:

- Reserved by the SDK. Return 0.

##### onSecondaryPreEncodeCameraVideoFrame

```java
public int onSecondaryPreEncodeCameraVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame)
```

Triggered when a video frame from the secondary camera is ready to be encoded.

**Parameters**:

- `agora_video_frame_observer`: The video frame observer instance.
- `frame`: The video frame to be encoded (`VideoFrame`).

**Returns**:

- Reserved by the SDK. Return 0.

##### onScreenCaptureVideoFrame

```java
public int onScreenCaptureVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame)
```

Triggered when a screen sharing video frame is captured.

**Parameters**:

- `agora_video_frame_observer`: The video frame observer instance.
- `frame`: The captured screen video frame (`VideoFrame`).

**Returns**:

- Reserved by the SDK. Return 0.

##### onPreEncodeScreenVideoFrame

```java
public int onPreEncodeScreenVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame)
```

Triggered when a screen sharing video frame is ready to be encoded.

**Parameters**:

- `agora_video_frame_observer`: The video frame observer instance.
- `frame`: The screen video frame to be encoded (`VideoFrame`).

**Returns**:

- Reserved by the SDK. Return 0.

##### onMediaPlayerVideoFrame

```java
public int onMediaPlayerVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame, int media_player_id)
```

Triggered when a video frame from the media player is received.

**Parameters**:

- `agora_video_frame_observer`: The video frame observer instance.
- `frame`: The media player video frame (`VideoFrame`).
- `media_player_id`: The ID of the media player.

**Returns**:

- Reserved by the SDK. Return 0.

##### onSecondaryScreenCaptureVideoFrame (Typo in original: onSecondaryScreenSaptureVideoFrame)

```java
// Note: Original name likely a typo, corrected based on pattern.
public int onSecondaryScreenCaptureVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame)
```

Triggered when a video frame from the secondary screen share is captured.

**Parameters**:

- `agora_video_frame_observer`: The video frame observer instance.
- `frame`: The captured secondary screen video frame (`VideoFrame`).

**Returns**:

- Reserved by the SDK. Return 0.

##### onSecondaryPreEncodeScreenVideoFrame

```java
public int onSecondaryPreEncodeScreenVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame)
```

Triggered when a video frame from the secondary screen share is ready to be encoded.

**Parameters**:

- `agora_video_frame_observer`: The video frame observer instance.
- `frame`: The secondary screen video frame to be encoded (`VideoFrame`).

**Returns**:

- Reserved by the SDK. Return 0.

##### onRenderVideoFrame

```java
public int onRenderVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, String channel_id, int uid, VideoFrame frame)
```

Triggered when a remote user's video frame is ready to be rendered.

**Parameters**:

- `agora_video_frame_observer`: The video frame observer instance (typically used in the native layer).
- `channel_id`: Channel ID.
- `uid`: Remote user ID.
- `frame`: The video frame to be rendered (`VideoFrame`).

**Returns**:

- Reserved by the SDK. Return 0.

##### onTranscodedVideoFrame

```java
public int onTranscodedVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame)
```

Triggered when a transcoded video frame is received (e.g., when using media stream relay or stream mixing).

**Parameters**:

- `agora_video_frame_observer`: The video frame observer instance.
- `frame`: The transcoded video frame (`VideoFrame`).

**Returns**:

- Reserved by the SDK. Return 0.

### IAudioFrameObserver

The `IAudioFrameObserver` interface provides callbacks for raw audio data.

#### onRecordAudioFrame

```java
public int onRecordAudioFrame(AgoraLocalUser agoraLocalUser, String channelId, AudioFrame frame);
```

Gets the recorded audio frame.

**Note:** To improve data transmission efficiency, the buffer of the frame object is a DirectByteBuffer. Be sure to extract the byte array value in the callback synchronously and then transfer it to the asynchronous thread for processing. You can refer to `io.agora.rtc.utils.Utils#getBytes(ByteBuffer)`.

**Parameters**

- `agoraLocalUser`: The local user object. See `AgoraLocalUser`.
- `channelId`: The channel ID.
- `frame`: The raw audio data. See `AudioFrame`.

**Returns**

The return value is currently not used.

#### onPlaybackAudioFrame

```java
public int onPlaybackAudioFrame(AgoraLocalUser agoraLocalUser, String channelId, AudioFrame frame);
```

Gets the playback audio frame.

**Note:** To improve data transmission efficiency, the buffer of the frame object is a DirectByteBuffer. Be sure to extract the byte array value in the callback synchronously and then transfer it to the asynchronous thread for processing. You can refer to `io.agora.rtc.utils.Utils#getBytes(ByteBuffer)`.

**Parameters**

- `agoraLocalUser`: The local user object. See `AgoraLocalUser`.
- `channelId`: The channel ID.
- `frame`: The raw audio data. See `AudioFrame`.

**Returns**

The return value is currently not used.

#### onMixedAudioFrame

```java
public int onMixedAudioFrame(AgoraLocalUser agoraLocalUser, String channelId, AudioFrame frame);
```

Gets the mixed audio frame of the local user and all remote users.

**Note:**

- This callback is only triggered when you are in a channel.
- To improve data transmission efficiency, the buffer of the frame object is a DirectByteBuffer. Be sure to extract the byte array value in the callback synchronously and then transfer it to the asynchronous thread for processing. You can refer to `io.agora.rtc.utils.Utils#getBytes(ByteBuffer)`.

**Parameters**

- `agoraLocalUser`: The local user object. See `AgoraLocalUser`.
- `channelId`: The channel ID.
- `frame`: The raw audio data. See `AudioFrame`.

**Returns**

The return value is currently not used.

#### onEarMonitoringAudioFrame

```java
public int onEarMonitoringAudioFrame(AgoraLocalUser agoraLocalUser, AudioFrame frame);
```

Gets the in-ear monitoring audio frame.

**Note:** To improve data transmission efficiency, the buffer of the frame object is a DirectByteBuffer. Be sure to extract the byte array value in the callback synchronously and then transfer it to the asynchronous thread for processing. You can refer to `io.agora.rtc.utils.Utils#getBytes(ByteBuffer)`.

**Parameters**

- `agoraLocalUser`: The local user object. See `AgoraLocalUser`.
- `frame`: The raw audio data. See `AudioFrame`.

**Returns**

The return value is currently not used.

#### onPlaybackAudioFrameBeforeMixing

```java
public int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agoraLocalUser, String channelId, String userId, AudioFrame frame, VadProcessResult vadResult);
```

Gets the audio frame of a specific remote user before mixing.

**Note:**

- This callback is only triggered when you are in a channel.
- To improve data transmission efficiency, the buffer of the frame object is a DirectByteBuffer. Be sure to extract the byte array value in the callback synchronously and then transfer it to the asynchronous thread for processing. You can refer to `io.agora.rtc.utils.Utils#getBytes(ByteBuffer)`.

**Parameters**

- `agoraLocalUser`: The local user object. See `AgoraLocalUser`.
- `channelId`: The channel ID.
- `userId`: The user ID of the remote user.
- `frame`: The raw audio data. See `AudioFrame`.
- `vadResult`: The VAD (Voice Activity Detection) result. See `VadProcessResult`.

**Returns**

The return value is currently not used.

#### getObservedAudioFramePosition

```java
public int getObservedAudioFramePosition();
```

Sets the observation position for the audio frames.

You can use this method to determine whether to observe the recorded audio frame, the playback audio frame, or the mixed audio frame. The SDK triggers the corresponding callbacks based on your setting.

**Note:**

- The functions returning the C++ interface are not supported. Call this method before joining a channel.
- If you want to observe multiple positions, use the bitwise OR operator `|`.

**Returns**

A bitmask that specifies the observation positions. See `AudioFramePosition`. The default value is `AUDIO_FRAME_POSITION_PLAYBACK (1 << 0) | AUDIO_FRAME_POSITION_RECORD (1 << 1) | AUDIO_FRAME_POSITION_MIXED (1 << 2) | AUDIO_FRAME_POSITION_BEFORE_MIXING (1 << 3)`.

### IAudioEncodedFrameObserver

The `IAudioEncodedFrameObserver` interface is used to listen for received remote encoded audio frame data.

**Important:**
To improve data transmission efficiency, the `ByteBuffer buffer` parameter in the callback is a `DirectByteBuffer`.
You **must synchronously** extract the required byte array (`byte[]`) within the callback method before passing the extracted `byte[]` to an asynchronous thread for further processing.
You can use `io.agora.rtc.utils.Utils.getBytes(buffer)` to get the `byte[]`.

```java
public interface IAudioEncodedFrameObserver {
    // Method declarations
}
```

#### Methods

##### onEncodedAudioFrameReceived

```java
default int onEncodedAudioFrameReceived(String remoteUserId, ByteBuffer buffer, EncodedAudioFrameReceiverInfo info)
```

Triggered when an encoded audio frame is received from a remote user.

**Parameters**:

- `remoteUserId`: The ID of the remote user who sent this audio frame.
- `buffer`: A `ByteBuffer` (DirectByteBuffer) containing the encoded audio data.
- `info`: Information about the encoded audio frame (`EncodedAudioFrameReceiverInfo`).

**Returns**:

- Reserved by the SDK. The default implementation returns 0.

### IVideoEncodedFrameObserver

The `IVideoEncodedFrameObserver` interface is used to listen for received remote encoded video frame data.

**Important:**
To improve data transmission efficiency, the `ByteBuffer buffer` parameter in the callback is a `DirectByteBuffer`.
You **must synchronously** extract the required byte array (`byte[]`) within the callback method before passing the extracted `byte[]` to an asynchronous thread for further processing.
You can use `io.agora.rtc.utils.Utils.getBytes(buffer)` to get the `byte[]`.

```java
public interface IVideoEncodedFrameObserver {
    // Method declarations
}
```

#### Methods

##### onEncodedVideoFrame

```java
public int onEncodedVideoFrame(AgoraVideoEncodedFrameObserver observer, int userId, ByteBuffer buffer, EncodedVideoFrameInfo info)
```

Triggered when an encoded video frame is received from a remote user.

**Parameters**:

- `observer`: The video encoded frame observer instance (typically used in the native layer, can be ignored in Java).
- `userId`: The ID of the remote user who sent this video frame.
- `buffer`: A `ByteBuffer` (DirectByteBuffer) containing the encoded video data.
- `info`: Information about the encoded video frame (`EncodedVideoFrameInfo`).

**Returns**:

- Reserved by the SDK. Return 0.

## 数据结构

### AgoraServiceConfig

The `AgoraServiceConfig` class is used to configure and initialize the Agora service instance.

#### Main Properties

- **enableAudioProcessor**: Whether to enable the audio processing module.
  - `1`: (Default) Enable the audio processing module.
  - `0`: Disable the audio processing module. If disabled, you cannot create audio tracks.
- **enableAudioDevice**: Whether to enable the audio device module (manages recording and playback).
  - `1`: Enable the audio device module. Audio recording and playback are available.
  - `0`: (Default) Disable the audio device module. Audio recording and playback are unavailable.
    **Important:** If `enableAudioDevice` is `false` and `enableAudioProcessor` is `true`, you cannot use audio devices, but you can push PCM audio data.
- **enableVideo**: Whether to enable video.
  - `1`: Enable video.
  - `0`: (Default) Disable video.
- **context**: User context object.
  - For Windows: Handle of the window loading the video. Specify to support hot-plugging video devices.
  - For Android: Context of the activity.
- **appId**: The App ID of your project.
- **areaCode**: Supported area code. Default is `Constants.AREA_CODE_GLOB`.
- **channelProfile**: Channel profile. Refer to `Constants.CHANNEL_PROFILE_TYPE`. Default is `Constants.CHANNEL_PROFILE_LIVE_BROADCASTING`.
- **audioScenario**: Audio scenario. Refer to `Constants.AUDIO_SCENARIO_TYPE`. Default is `Constants.AUDIO_SCENARIO_CHORUS`.
- **useStringUid**: Whether to enable string user IDs.
  - `1`: Enable string user IDs.
  - `0`: (Default) Disable string user IDs (use integer UIDs).
- **logFilePath**: Log file path. `null` for the default log path.
- **logFileSize**: Log file size in KB. Default is 2048 KB.
- **logFilters**: Log level filter. Refer to `Constants.LOG_FILTER_TYPE`. Default is `Constants.LOG_FILTER_INFO`.
- **domainLimit**: Whether to enable domain limit.
  - `1`: Only connect to servers already parsed by DNS.
  - `0`: (Default) Connect to servers with no limit.

### RtcConnConfig

The `RtcConnConfig` class is used to configure various parameters for an RTC connection.

#### Main Properties

- **autoSubscribeAudio**: Whether to automatically subscribe to all audio streams.
  - `1`: (Default) Subscribe to all audio streams automatically.
  - `0`: Do not subscribe to any audio stream automatically.
- **autoSubscribeVideo**: Whether to automatically subscribe to all video streams.
  - `1`: (Default) Subscribe to all video streams automatically.
  - `0`: Do not subscribe to any video stream automatically.
- **enableAudioRecordingOrPlayout**: Whether to enable audio recording or playout.
  - `1`: Used for publishing audio and mixing microphone, or subscribing to audio and playing it out.
  - `0`: Used for publishing external audio frames only without mixing microphone, or when no audio device is needed for playout.
- **maxSendBitrate**: Maximum sending bitrate (bps).
- **minPort**: Minimum port number for UDP connection.
- **maxPort**: Maximum port number for UDP connection.
- **audioSubsOptions**: Audio subscription options (`AudioSubscriptionOptions`).
- **clientRoleType**: User role type. Refer to `Constants.CLIENT_ROLE_TYPE`. Default is `Constants.CLIENT_ROLE_AUDIENCE`.
- **channelProfile**: Channel profile. Refer to `Constants.CHANNEL_PROFILE_TYPE`. Inherited from `AgoraServiceConfig` if not set here.
- **audioRecvMediaPacket**: Whether to receive audio media packets. `1` to receive, `0` otherwise.
- **audioRecvEncodedFrame**: Whether to receive encoded audio frames. `1` to receive, `0` otherwise.
- **videoRecvMediaPacket**: Whether to receive video media packets. `1` to receive, `0` otherwise.

### RtcConnInfo

The `RtcConnInfo` class contains information about an RTC connection.

#### Main Properties

- **id**: Unique identifier for the connection (`int`).
- **channelId**: Channel identifier (`String`).
- **state**: Current state of the connection (`int`). Refer to `Constants.CONNECTION_STATE_TYPE`.
- **localUserId**: Local user identifier (`String`).
- **internalUid**: Internal user identifier (`int`).

### VideoEncoderConfig

The `VideoEncoderConfig` class contains configuration parameters for the video encoder.

#### Main Properties

- **codecType**: Video encoder codec type (`Constants.VIDEO_CODEC_TYPE`).
- **dimensions**: Video frame dimensions (`VideoDimensions`), specifying video quality measured by the total number of pixels (width \* height).
- **frameRate**: Video frame rate (fps). `int` type, accepts `Constants.FRAME_RATE` enum values for backward compatibility.
- **bitrate**: Target bitrate for video encoding (Kbps). The SDK adjusts this dynamically based on network conditions; setting it too high is not recommended.
- **minBitrate**: **[For future use]** Minimum encoding bitrate (Kbps). The SDK automatically adjusts the encoding bitrate to adapt to network conditions. Unless you have specific image quality requirements, changing this value is not recommended. **Note:** This parameter applies only to the Live Broadcast profile (`CHANNEL_PROFILE_LIVE_BROADCASTING`).
- **orientationMode**: **[For future use]** Video orientation mode (`Constants.ORIENTATION_MODE`).
- **degradationPreference**: Video degradation preference under limited bandwidth (`Constants.DEGRADATION_PREFERENCE`). **Note:** Currently, only `MAINTAIN_QUALITY` (0) is supported.
- **mirrorMode**: Mirror mode (`Constants.VIDEO_MIRROR_MODE_TYPE`). If set to `VIDEO_MIRROR_MODE_ENABLED`, the video frame is mirrored before encoding.
- **encodeAlpha**: Whether to encode and send alpha channel data when present in the source video. Default is `0` (disabled). A non-zero value enables it.

### VideoSubscriptionOptions

The `VideoSubscriptionOptions` class defines the options for video subscription.

#### Main Properties

- **type**: Video stream type to subscribe to (`Constants.VIDEO_STREAM_TYPE`). Default is `VIDEO_STREAM_HIGH`.
- **encodedFrameOnly**: Whether to only subscribe to encoded video frames (`int`).
  - `1`: Only receive encoded video frames, raw video data callbacks will be disabled.
  - `0`: (Default) Receive both encoded video frames and raw video data.

### SimulcastStreamConfig

The `SimulcastStreamConfig` class configures parameters for simultaneous live streaming.

#### Main Properties

- **dimensions**: Video dimensions.
- **bitrate**: Video bitrate.
- **framerate**: Video frame rate.

### EncodedVideoFrameInfo

The `EncodedVideoFrameInfo` class contains information about encoded video frames.

#### Main Properties

- **codecType**: Codec type (`Constants.VIDEO_CODEC_TYPE`).
- **width**: Video frame width (pixels).
- **height**: Video frame height (pixels).
- **framesPerSecond**: Video frame rate (fps). If this value is 0, the SDK uses the timestamp of the original video frame; otherwise, the SDK adjusts the timestamp based on this value.
- **frameType**: Video frame type (`Constants.VIDEO_FRAME_TYPE`), such as keyframes (I frames), predicted frames (P frames), etc.
- **rotation**: Video rotation angle (`Constants.VIDEO_ORIENTATION`).
- **trackId**: Unique identifier for the video stream, used for multi-video stream scenarios.
- **captureTimeMs**: Timestamp of when the video frame was captured (milliseconds). This is an input parameter.
- **decodeTimeMs**: Timestamp of when the video frame was decoded (milliseconds). (Note: Java annotation describes it as a render timestamp, but here it's tentatively defined as a decode timestamp based on the field name, so please confirm.)
- **uid**: User ID of the user who sent this video frame.
- **streamType**: Video stream type (`Constants.VIDEO_STREAM_TYPE`).

### EncodedAudioFrameInfo

The `EncodedAudioFrameInfo` class contains information about encoded audio frames.

#### Main Properties

- **speech**: Indicates whether the frame contains speech (`int`, 1 for yes, 0 for no).
- **codec**: Audio codec type (`Constants.AUDIO_CODEC_TYPE`).
- **sendEvenIfEmpty**: Whether to send the frame even if it contains no audio data (`int`, 1 to send, 0 otherwise).
- **sampleRateHz**: Audio sampling rate (Hz).
- **samplesPerChannel**: Number of samples per audio channel.
- **numberOfChannels**: Number of audio channels.

### EncodedAudioFrameReceiverInfo

The `EncodedAudioFrameReceiverInfo` class contains information about a received encoded audio frame.

#### Main Properties

- **sendTs**: The send timestamp of the packet (`long`).
- **codec**: The codec type of the packet (`Constants.AUDIO_CODEC_TYPE`).

### SenderOptions

The `SenderOptions` class contains configuration options for senders (e.g., custom encoded video track sender).

#### Main Properties

- **ccMode**: Congestion control mode (`Constants.CC_MODE`). Determines how the SDK handles network congestion. Default is usually `CC_ENABLED`.
- **codecType**: Codec type (`Constants.VIDEO_CODEC_TYPE` or `Constants.AUDIO_CODEC_TYPE`). Specifies the codec to be used for encoding.
- **targetBitrate**: Target bitrate (bps). Recommended target bitrate for the sender.

### VideoFrame

The `VideoFrame` class represents a video frame, containing video pixel data and related information.

#### Main Properties

- **type**: Video frame type. Refer to `Constants.VIDEO_BUFFER_TYPE`.
- **width**: Video frame width (pixels).
- **height**: Video frame height (pixels).
- **yStride**: Line span (stride) of the Y buffer in YUV data (pixels).
- **uStride**: Line span (stride) of the U buffer in YUV data (pixels).
- **vStride**: Line span (stride) of the V buffer in YUV data (pixels).
- **yBuffer**: Y data buffer (`ByteBuffer`).
- **uBuffer**: U data buffer (`ByteBuffer`).
- **vBuffer**: V data buffer (`ByteBuffer`).
- **rotation**: Rotation of this frame (0, 90, 180, 270 degrees).
- **renderTimeMs**: Timestamp for rendering the video stream (milliseconds). This timestamp is used to synchronize video rendering, **not** the capture timestamp.
- **avsyncType**: Audio-video synchronization type (`int`).
- **metadataBuffer**: **[Texture related]** Metadata buffer (`ByteBuffer`). Default is `null`.
- **sharedContext**: **[Texture related]** EGL context object (`Object`).
- **textureId**: **[Texture related]** Texture ID used by the video frame (`int`).
- **matrix**: **[Texture related]** Incoming 4x4 transformation matrix (`float[]`).
- **alphaBuffer**: **[Portrait Segmentation]** Alpha channel data buffer (`ByteBuffer`). Dimensions are the same as the `VideoFrame`. Pixel values range from 0 (completely background) to 255 (completely foreground). Default is `null`.
- **alphaMode**: Relative position between `alphaBuffer` and the frame (`int`).
  - `0`: (Default) Normal frame.
  - `1`: `alphaBuffer` is above the frame.
  - `2`: `alphaBuffer` is below the frame.
  - `3`: `alphaBuffer` is to the left of the frame.
  - `4`: `alphaBuffer` is to the right of the frame.

### AudioFrame

The `AudioFrame` class represents an audio frame.

#### Main Properties

- **type**: Audio frame type (`Constants.AUDIO_FRAME_TYPE`).
- **samplesPerChannel**: Number of samples per channel.
- **bytesPerSample**: Number of bytes per sample (`Constants.BYTES_PER_SAMPLE`).
- **channels**: Number of audio channels.
- **samplesPerSec**: Sampling rate (Hz).
- **buffer**: Audio data buffer (`ByteBuffer`). **Note:** For efficiency, this is often a `DirectByteBuffer` in callbacks. You must extract the byte array synchronously within the callback.
- **renderTimeMs**: Render timestamp (milliseconds).
- **avsyncType**: Audio-video sync type (`int`).
- **farFiledFlag**: Far-field flag (`int`). Indicates if the audio is likely from a distant source.
- **rms**: Root mean square (RMS) of the audio signal (`int`). Represents the volume level.
- **voiceProb**: Probability of voice presence (`int`). Estimation of the likelihood that the frame contains human speech.
- **musicProb**: Probability of music presence (`int`). Estimation of the likelihood that the frame contains music.
- **pitch**: Pitch of the audio signal (`int`). Estimation of the fundamental frequency of the audio.

### ExternalVideoFrame

The `ExternalVideoFrame` class represents an external input video frame, used to push data from an external video source to the SDK.

#### Main Properties

- **type**: External video frame buffer type. Refer to `Constants.VIDEO_BUFFER_TYPE`.
- **format**: Pixel format of the video frame. Refer to `Constants.VIDEO_PIXEL_FORMAT`.
- **buffer**: Video frame data buffer (`ByteBuffer`). **Note:** Must be a `DirectByteBuffer`.
- **stride**: Line spacing (stride) of the incoming video frame (pixels). For textures, this is the width of the texture.
- **height**: Height of the incoming video frame (pixels).
- **cropLeft**: **[Raw data related]** Number of pixels trimmed from the left. Default is 0.
- **cropTop**: **[Raw data related]** Number of pixels trimmed from the top. Default is 0.
- **cropRight**: **[Raw data related]** Number of pixels trimmed from the right. Default is 0.
- **cropBottom**: **[Raw data related]** Number of pixels trimmed from the bottom. Default is 0.
- **rotation**: **[Raw data related]** Clockwise rotation of the video frame (0, 90, 180, 270 degrees). Default is 0.
- **timestamp**: Timestamp of the incoming video frame (milliseconds). Incorrect timestamps can result in frame loss or unsynchronized audio/video.
- **eglContext**: **[Texture related]** EGL context object (`Object`).
  - For Khronos OpenGL interfaces (`javax.microedition.khronos.egl.*`), set this to the corresponding `EGLContext`.
  - For Android OpenGL interfaces (`android.opengl.*`), set this to the corresponding `EGLContext`.
- **eglType**: **[Texture related]** EGL type. Refer to `Constants.EGL_CONTEXT_TYPE`.
- **textureId**: **[Texture related]** Texture ID used by the video frame (`int`).
- **matrix**: **[Texture related]** Incoming 4x4 transformation matrix (`float[]`). Typically an identity matrix. (Note: Javadoc for this field says `float`, not `float[]`. Verify actual usage).
- **metadataBuffer**: **[Texture related]** Metadata buffer (`ByteBuffer`). Default is `null`.
- **alphaBuffer**: **[Portrait Segmentation]** Alpha channel data buffer (`ByteBuffer`). Dimensions are the same as the `VideoFrame`. Pixel values range from 0 (completely background) to 255 (completely foreground). Default is `null`.
- **fillAlphaBuffer**: **[BGRA/RGBA only]** Whether to extract `alphaBuffer` from BGRA/RGBA data (`int`). Set to `1` (true) if you don't explicitly provide `alphaBuffer`. Default is `0` (false).
- **alphaMode**: Relative position between `alphaBuffer` and the frame (`int`).
  - `0`: (Default) Normal frame
  - 1: Alpha buffer above frame
  - 2: Alpha buffer below frame
  - 3: Alpha buffer to the left of frame
  - 4: Alpha buffer to the right of frame
- **colorSpace**: Color space information (`ColorSpace`).

### VideoDimensions

The `VideoDimensions` class represents the dimensions (width and height) of a video frame.

#### Main Properties

- **width**: Video width (pixels).
- **height**: Video height (pixels).

### RtcStats

The `RtcStats` class provides statistics related to the RTC connection.

#### Main Properties

- **connectionId**: Connection ID (`int`).
- **duration**: Call duration (seconds) (`int`).
- **txBytes**: Total bytes sent (`int`).
- **rxBytes**: Total bytes received (`int`).
- **txAudioBytes**: Total audio bytes sent (`int`).
- **txVideoBytes**: Total video bytes sent (`int`).
- **rxAudioBytes**: Total audio bytes received (`int`).
- **rxVideoBytes**: Total video bytes received (`int`).
- **txKBitRate**: Transmission bitrate (Kbps) (`short`).
- **rxKBitRate**: Reception bitrate (Kbps) (`short`).
- **txAudioKBitRate**: Audio transmission bitrate (Kbps) (`short`).
- **rxAudioKBitRate**: Audio reception bitrate (Kbps) (`short`).
- **txVideoKBitRate**: Video transmission bitrate (Kbps) (`short`).
- **rxVideoKBitRate**: Video reception bitrate (Kbps) (`short`).
- **lastmileDelay**: Latency from the client to the Agora edge server (ms) (`short`).
- **txPacketLossRate**: Uplink packet loss rate (%) (`int`).
- **rxPacketLossRate**: Downlink packet loss rate (%) (`int`).
- **userCount**: Number of users in the channel (`int`).
- **cpuAppUsage**: CPU usage (%) of the application (`double`).
- **cpuTotalUsage**: Total CPU usage (%) of the system (`double`).
- **gatewayRtt**: Round-trip time (RTT) from the client to the gateway (ms) (`int`).
- **memoryAppUsageRatio**: Application memory usage ratio (%) (`double`).
- **memoryTotalUsageRatio**: System memory usage ratio (%) (`double`).
- **memoryAppUsageInKbytes**: Application memory usage (KB) (`int`).
- **connectTimeMs**: Time elapsed from connection start to established state (ms) (`int`).
- **firstAudioPacketDuration**: Duration from connection start to receiving the first audio packet (ms) (`int`).
- **firstVideoPacketDuration**: Duration from connection start to receiving the first video packet (ms) (`int`).
- **firstVideoKeyFramePacketDuration**: Duration from connection start to receiving the first video key frame packet (ms) (`int`).
- **packetsBeforeFirstKeyFramePacket**: Number of video packets received before the first key frame (`int`).
- **firstAudioPacketDurationAfterUnmute**: Duration from unmuting audio to receiving the first audio packet (ms) (`int`).
- **firstVideoPacketDurationAfterUnmute**: Duration from unmuting video to receiving the first video packet (ms) (`int`).
- **firstVideoKeyFramePacketDurationAfterUnmute**: Duration from unmuting video to receiving the first video key frame packet (ms) (`int`).
- **firstVideoKeyFrameDecodedDurationAfterUnmute**: Duration from unmuting video to decoding the first video key frame (ms) (`int`).
- **firstVideoKeyFrameRenderedDurationAfterUnmute**: Duration from unmuting video to rendering the first video key frame (ms) (`int`).

### UserInfo

The `UserInfo` class provides information about a user.

#### Main Properties

- **userId**: User identifier (`String`).
- **hasAudio**: Whether the user is sending an audio stream (`int`).
  - `1`: Yes.
  - `0`: No.
- **hasVideo**: Whether the user is sending a video stream (`int`).
  - `1`: Yes.
  - `0`: No.

### VadProcessResult

The `VadProcessResult` class provides the result of a Voice Activity Detection (VAD) process. This is typically used in the `onPlaybackAudioFrameBeforeMixing` callback.

#### Main Properties

- **outFrame**: Processed audio frame data (`byte[]`) after VAD. The specific content depends on the VAD implementation (might be original or modified).
- **state**: VAD state (`Constants.VadState`). Indicates whether speech is detected (`START_SPEAKING`, `SPEAKING`) or not (`STOP_SPEAKING`).

### AgoraAudioVadConfigV2

Configuration class for Agora Audio Voice Activity Detection (VAD) V2. Used when registering `IAudioFrameObserver` with VAD enabled.

#### Main Properties

- **preStartRecognizeCount**: Number of audio frames to save before entering the `START_SPEAKING` state. Default: 16 frames. Range: [0, ∞).
- **startRecognizeCount**: Number of consecutive audio frames required to confirm the `SPEAKING` state. Default: 30 frames. Range: [1, ∞).
- **stopRecognizeCount**: Number of consecutive audio frames required to confirm the `STOP_SPEAKING` state. Default: 20 frames. Range: [1, ∞).
- **activePercent**: Percentage of active frames required within `startRecognizeCount` frames to enter the `SPEAKING` state. Default: 0.7 (70%). Range: [0.0, 1.0].
- **inactivePercent**: Percentage of inactive frames required within `stopRecognizeCount` frames to enter the `STOP_SPEAKING` state. Default: 0.5 (50%). Range: [0.0, 1.0].
- **startVoiceProb**: Voice probability threshold to trigger the start phase (transition towards `SPEAKING`). A lower value makes it easier to start. Default: 70. Range: [0, 100].
- **stopVoiceProb**: Voice probability threshold to trigger the end phase (transition towards `STOP_SPEAKING`). A higher value makes it easier to end. Default: 70. Range: [0, 100].
- **startRmsThreshold**: RMS threshold (dBFS) to trigger the start phase. A higher value is more sensitive (starts earlier). Default: -50. Range: [-100, 0]. In quiet environments, -50 might be suitable. In noisy environments, consider -40 to -30.
- **stopRmsThreshold**: RMS threshold (dBFS) to trigger the end phase. A higher value is more sensitive (ends later). Default: -50. Range: [-100, 0].

### LocalAudioTrackStats

The `LocalAudioTrackStats` class provides statistics for a local audio track.

#### Main Properties

- **sourceId**: Source ID of the track (`int`). Usually identifies the source type (e.g., microphone, custom PCM).
- **bufferedPcmDataListSize**: Size of the buffered PCM data list (`int`). Relevant for custom PCM tracks.
- **missedAudioFrames**: Number of missed audio frames (`int`). Audio frames that were expected but not received/processed.
- **sentAudioFrames**: Number of audio frames sent (`int`).
- **pushedAudioFrames**: Number of audio frames pushed (`int`). Relevant for custom audio tracks where data is pushed via API.
- **droppedAudioFrames**: Number of dropped audio frames (`int`). Frames discarded, possibly due to buffer overflow or processing issues.
- **enabled**: Whether the track is enabled (`int`). `1` for enabled, `0` for disabled.

### LocalVideoTrackStats

The `LocalVideoTrackStats` class provides statistics for a local video track.

#### Main Properties

- **numberOfStreams**: Number of video streams published (`long`). Usually 1, unless simulcast is enabled.
- **bytesMajorStream**: Total bytes sent for the major (high-resolution) stream (`long`).
- **bytesMinorStream**: Total bytes sent for the minor (low-resolution) stream (`long`). Applicable only if simulcast is enabled.
- **framesEncoded**: Number of video frames encoded (`int`).
- **ssrcMajorStream**: SSRC (Synchronization Source Identifier) of the major stream (`int`).
- **ssrcMinorStream**: SSRC (Synchronization Source Identifier) of the minor stream (`int`). Applicable only if simulcast is enabled.
- **captureFrameRate**: Video capture frame rate (fps) (`int`).
- **regulatedCaptureFrameRate**: Capture frame rate after regulation based on video encoder configuration (fps) (`int`).
- **inputFrameRate**: Input frame rate to the encoder (fps) (`int`).
- **encodeFrameRate**: Encoder output frame rate (fps) (`int`).
- **renderFrameRate**: Renderer output frame rate (fps) (`int`). (Note: Typically for local rendering, might be 0 if not rendered locally).
- **targetMediaBitrateBps**: Target media bitrate set by the SDK (bps) (`int`).
- **mediaBitrateBps**: Actual media bitrate (excluding FEC) sent (bps) (`int`).
- **totalBitrateBps**: Total bitrate sent, including FEC (Forward Error Correction) overhead (bps) (`int`).
- **captureWidth**: Video capture width (pixels) (`int`).
- **captureHeight**: Video capture height (pixels) (`int`).
- **regulatedCaptureWidth**: Capture width after regulation (pixels) (`int`).
- **regulatedCaptureHeight**: Capture height after regulation (pixels) (`int`).
- **width**: Encoded video width (pixels) (`int`).
- **height**: Encoded video height (pixels) (`int`).
- **encoderType**: Video encoder type (`Constants.VIDEO_CODEC_TYPE` or other internal identifiers) (`int`).
- **uplinkCostTimeMs**: Average time difference between frame capture and encoding completion (ms) (`int`).
- **qualityAdaptIndication**: Quality adaptation indication (`Constants.QUALITY_ADAPT_INDICATION`). Shows how the video quality adapted in the last interval (e.g., due to bandwidth, CPU).

### RemoteAudioTrackStats

The `RemoteAudioTrackStats` class provides statistics for a remote audio track.

#### Main Properties

- **uid**: User ID of the remote user sending the audio stream (`int`).
- **quality**: Audio quality received by the user (`Constants.QUALITY_TYPE`). 0: Unknown, 1: Excellent, 2: Good, 3: Poor, 4: Bad, 5: Very Bad, 6: Down.
- **networkTransportDelay**: Network delay (ms) from the sender to the receiver (`int`).
- **jitterBufferDelay**: Delay (ms) introduced by the jitter buffer on the receiver side (`int`).
- **audioLossRate**: Audio frame loss rate (%) in the reported interval (`int`).
- **numChannels**: Number of audio channels (`int`).
- **receivedSampleRate**: Sample rate (Hz) of the received audio stream in the reported interval (`int`).
- **receivedBitrate**: Average bitrate (Kbps) of the received audio stream in the reported interval (`int`).
- **totalFrozenTime**: Total freeze time (ms) of the remote audio stream since the user joined. Audio freeze occurs when the audio frame loss rate reaches 4%. Agora calculates freeze time in 2-second units. Total freeze time = (freeze count) \* 2000 ms.
- **frozenRate**: Total audio freeze time as a percentage (%) of the total time the audio was available (`int`). (frozenRate = totalFrozenTime / totalActiveTime).
- **receivedBytes**: Total number of audio bytes received (`long`).

### RemoteVideoTrackStats

The `RemoteVideoTrackStats` class provides statistics for a remote video track.

#### Main Properties

- **uid**: User ID of the remote user sending the video stream (`int`).
- **delay**: **[DEPRECATED]** Time delay (ms). This field is deprecated.
- **width**: Width (pixels) of the video stream (`int`).
- **height**: Height (pixels) of the video stream (`int`).
- **receivedBitrate**: Bitrate (Kbps) received since the last report interval (`int`).
- **decoderOutputFrameRate**: Decoder output frame rate (fps) of the remote video (`int`).
- **rendererOutputFrameRate**: Renderer output frame rate (fps) of the remote video (`int`).
- **frameLossRate**: Video frame loss rate (%) of the remote video stream in the reported interval (`int`).
- **packetLossRate**: Packet loss rate (%) of the remote video stream after applying anti-packet-loss measures (`int`).
- **rxStreamType**: Type of the received remote video stream (`Constants.VIDEO_STREAM_TYPE`).
- **totalFrozenTime**: Total freeze time (ms) of the remote video stream since the user joined. For video sessions with frame rates >= 5 fps, freeze occurs if the interval between two adjacent renderable frames exceeds 500 ms.
- **frozenRate**: Total video freeze time as a percentage (%) of the total time the video was available (`int`). (frozenRate = totalFrozenTime / totalActiveTime).
- **totalDecodedFrames**: Total number of video frames decoded (`int`).
- **avSyncTimeMs**: Offset (ms) between audio and video streams (`int`). Positive value means audio leads video; negative value means audio lags video.
- **downlinkProcessTimeMs**: Average processing time (ms) on the downlink (`int`). Calculated as the time from receiving the first packet of a frame to the frame being ready for rendering.
- **frameRenderDelayMs**: Average time cost (ms) in the renderer (`int`).
- **totalActiveTime**: Total time (ms) the remote user was sending video (`long`). This is the time since the user joined, excluding periods where they stopped sending video or disabled the video module.
- **publishDuration**: Total duration (ms) the remote video stream was published (`long`).

### EncryptionConfig

The `EncryptionConfig` class configures encryption parameters.

#### Main Properties

- **encryptionMode**: Encryption mode (`int`).
- **encryptionKey**: Encryption key (`String`).
- **encryptionKdfSalt**: Encryption KDF salt (`byte[]`).

### UplinkNetworkInfo

The `UplinkNetworkInfo` class provides information about the uplink network.

#### Main Properties

- **videoEncoderTargetBitrateBps**: The target bitrate (bps) for the video encoder (`int`). This value is determined by the SDK based on the current network conditions and quality settings.

### DownlinkNetworkInfo

The `DownlinkNetworkInfo` class provides information about the downlink network.

#### Main Properties

- **lastmileBufferDelayTimeMs**: Last mile buffer delay (ms) (`int`). Delay caused by the jitter buffer on the receiving end.
- **bandwidthEstimationBps**: Estimated downlink bandwidth (bps) (`int`).
- **totalDownscaleLevelCount**: Total number of video stream downscale levels (`int`). Counts how many times the received video streams were switched to a lower resolution due to poor network conditions.
- **peerDownlinkInfo**: Downlink information for each remote user (`PeerDownlinkInfo[]`). (Note: The Java class shows a single `PeerDownlinkInfo` object, but logically this might represent an array or collection in the callback context. Clarification needed based on `onDownlinkNetworkInfoUpdated` usage).
- **totalReceivedVideoCount**: Total number of received video streams (`int`).

### PeerDownlinkInfo

The `PeerDownlinkInfo` class provides downlink information for a specific remote user's video stream.

#### Main Properties

- **userId**: User ID of the remote user (`String`). (Note: getter is `getUid()`, but field is `userId`).
- **streamType**: Type of the video stream (`Constants.VIDEO_STREAM_TYPE`). High or low stream.
- **currentDownscaleLevel**: Current downscale level of the video stream (`int`). 0 means original resolution, 1 means downscaled once, etc.
- **expectedBitrateBps**: Expected bitrate (bps) of this video stream (`int`). The bitrate the receiver expects based on network conditions and subscription settings.

### 工具类

#### AudioConsumerUtils

The `AudioConsumerUtils` class is used to consume PCM audio data and push it to the RTC channel. It's mainly used for AI scenarios, such as processing data returned by TTS.

**Usage mode:**

1. Create an `AudioConsumerUtils` object for each user generating PCM data.
2. When PCM data is generated (e.g., from TTS), call `pushPcmData(byte[] data)` to push the data into the internal buffer.
3. Call the `consume()` method periodically via a timer (recommended interval is 40-80ms). This method will automatically send the data from the buffer to the RTC channel based on internal state and timestamp.
4. Call the `clear()` method when you need to interrupt (e.g., stop the current AI conversation).
5. Call the `release()` method when exiting to release resources.

#### Constructor

```java
public AudioConsumerUtils(AgoraAudioPcmDataSender audioFrameSender, int numOfChannels, int sampleRate)
```

Create an instance of `AudioConsumerUtils`.

**Parameters:**

- `audioFrameSender`: Instance of `AgoraAudioPcmDataSender` for sending PCM data.
- `numOfChannels`: Number of audio channels.
- `sampleRate`: Audio sampling rate (Hz).

#### Methods

##### pushPcmData

```java
public synchronized void pushPcmData(byte[] data)
```

Push PCM data into the internal buffer. If the data length is not a multiple of the internal frame size, it will be padded with zeros automatically.

**Parameters:**

- `data`: PCM audio data (`byte[]`).

##### consume

```java
public synchronized int consume()
```

Consume data from the buffer and send it to the RTC channel. This method should be called periodically by a timer.

**Return value:**

- `>= 0`: Number of successfully consumed and sent audio frames.
- `< 0`: Error code, e.g., `Constants.AUDIO_CONSUMER_INVALID_PARAM`, `Constants.AUDIO_CONSUMER_NOT_READY`, `Constants.AUDIO_CONSUMER_PENDING`, `Constants.AUDIO_CONSUMER_FAILED`.

##### getRemainingCacheDurationInMs

```java
public synchronized int getRemainingCacheDurationInMs()
```

Get the remaining playable duration of the internal buffer (milliseconds).

**Return value:**

- Remaining cache duration (milliseconds).

##### clear

```java
public synchronized void clear()
```

Clear the internal buffer and state.

##### release

```java
public synchronized void release()
```

Release resources, including clearing the buffer and setting internal references to null.

#### VadDumpUtils

The `VadDumpUtils` class is used to dump audio data and label information from the VAD (Voice Activity Detection) processing pipeline to files, facilitating debugging and analysis.

##### Constructor

```java
public VadDumpUtils(String path)
```

Creates a `VadDumpUtils` instance. It will create a subdirectory under the specified `path` named with the current timestamp (`yyyyMMddHHmmss`) to store the dump files.

**Parameters**:

- `path`: The root directory path for the dump files.

##### write

```java
public synchronized void write(AudioFrame frame, byte[] vadResultBytes, Constants.VadState vadResultState)
```

Writes one frame of audio data and its corresponding VAD processing result to files. File writing is performed on a separate internal thread.

The following files are generated within the timestamped subdirectory:

- `source.pcm`: Contains all the raw PCM audio data passed to the `write` method.
- `label.txt`: Contains label information for each frame, including frame count, VAD state, far-field flag, voice probability, RMS, pitch, music probability, etc.
- `vad_X.pcm` (where X is a sequential number): A new file is created when the VAD state transitions to `START_SPEAKING`. It records the PCM data (`vadResultBytes`) for that speech segment until the state transitions to `STOP_SPEAKING`, at which point the file is closed.

**Parameters**:

- `frame`: The `AudioFrame` containing the original PCM data and metadata (RMS, pitch, etc.).
- `vadResultBytes`: The audio data (`byte[]`) resulting from the VAD process (this might be identical to the original or modified depending on the VAD implementation).
- `vadResultState`: The VAD state (`Constants.VadState`) determined for this frame.

##### release

```java
public void release()
```

Closes all open file handles, releases resources, and resets internal counters. Call this when you are finished dumping data.
