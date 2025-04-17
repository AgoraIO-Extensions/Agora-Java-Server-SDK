# Agora Server SDK Java API 参考

本文档提供了 Agora Server SDK 的 Java API 主要类和方法的参考。

## 目录

- [核心类](#核心类)
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
- [观察者接口](#观察者接口)
  - [IRtcConnObserver](#irtcconnobserver)
  - [ILocalUserObserver](#ilocaluserobserver)
  - [INetworkObserver](#inetworkobserver)
  - [IVideoFrameObserver](#ivideoframeobserver)
  - [IAudioFrameObserver](#iaudioframeobserver)
  - [IAudioEncodedFrameObserver](#iaudioencodedframeobserver)
  - [IVideoEncodedFrameObserver](#ivideoencodedframeobserver)
- [数据结构](#数据结构)
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
- [工具类](#工具类)
  - [AudioConsumerUtils](#audioconsumerutils)
  - [VadDumpUtils](#vaddumputils)

## 核心类

### AgoraService

AgoraService 是 Agora Server SDK 的核心类，负责初始化 SDK，创建媒体轨道和连接等。

```java
public class AgoraService {
    // 构造方法
    public AgoraService()
}
```

#### 方法

##### getSdkVersion

```java
public static String getSdkVersion()
```

获取 SDK 版本号。

**返回值**：

- SDK 版本号字符串。

##### initialize

```java
public int initialize(AgoraServiceConfig config)
```

使用指定的配置初始化 Agora 服务。

**参数**：

- config：Agora 服务的配置。

**返回值**：

- 0：成功
- < 0：失败

##### destroy

```java
public synchronized void destroy()
```

销毁 Agora 服务。

##### setAudioSessionPreset

```java
public int setAudioSessionPreset(int audioScenario)
```

设置音频会话预设。

**参数**：

- audioScenario：要设置的音频场景。

**返回值**：

- 0：成功
- < 0：失败

##### setAudioSessionConfig

```java
public int setAudioSessionConfig(AudioSessionConfig config)
```

设置音频会话配置。

**参数**：

- config：要设置的音频会话配置。

**返回值**：

- 0：成功
- < 0：失败

##### getAudioSessionConfig

```java
public AudioSessionConfig getAudioSessionConfig()
```

获取音频会话配置。

**返回值**：

- 当前的音频会话配置。

##### destroyAudioSessionConfig

```java
public void destroyAudioSessionConfig(AudioSessionConfig config)
```

销毁音频会话配置。

**参数**：

- config：要销毁的音频会话配置。

##### setLogFile

```java
public int setLogFile(String filePath, int fileSize)
```

设置日志文件。

**参数**：

- filePath：日志文件的路径。
- fileSize：日志文件的大小（字节）。

**返回值**：

- 0：成功
- < 0：失败

##### setLogFilter

```java
public int setLogFilter(int filters)
```

设置日志过滤器。

**参数**：

- filters：要设置的日志过滤器。

**返回值**：

- 0：成功
- < 0：失败

##### createLocalAudioTrack

```java
public AgoraLocalAudioTrack createLocalAudioTrack()
```

创建本地音频轨道。

**返回值**：

- 本地音频轨道对象。

##### createCustomAudioTrackPcm

```java
public AgoraLocalAudioTrack createCustomAudioTrackPcm(AgoraAudioPcmDataSender agoraAudioPcmDataSender)
```

使用 PCM 数据发送器创建自定义音频轨道。

**参数**：

- agoraAudioPcmDataSender：PCM 数据发送器。

**返回值**：

- 自定义音频轨道对象。

##### createCustomAudioTrackEncoded

```java
public AgoraLocalAudioTrack createCustomAudioTrackEncoded(AgoraAudioEncodedFrameSender agoraAudioEncodedFrameSender, int mixMode)
```

使用编码帧发送器创建自定义音频轨道。

**参数**：

- agoraAudioEncodedFrameSender：编码帧发送器。
- mixMode：混合模式。

**返回值**：

- 自定义音频轨道对象。

##### createCustomAudioTrackPacket

```java
public AgoraLocalAudioTrack createCustomAudioTrackPacket(AgoraMediaPacketSender agoraMediaPacketSender)
```

使用媒体数据包发送器创建自定义音频轨道。

**参数**：

- agoraMediaPacketSender：媒体数据包发送器。

**返回值**：

- 自定义音频轨道对象。

##### createMediaPlayerAudioTrack

```java
public AgoraLocalAudioTrack createMediaPlayerAudioTrack(AgoraMediaPlayerSource agoraMediaPlayerSource)
```

使用媒体播放器源创建音频轨道。

**参数**：

- agoraMediaPlayerSource：媒体播放器源。

**返回值**：

- 媒体播放器音频轨道对象。

##### createRecordingDeviceAudioTrack

```java
public AgoraLocalAudioTrack createRecordingDeviceAudioTrack(AgoraRecordDevice agoraRecordDevice)
```

使用录音设备创建音频轨道。

**参数**：

- agoraRecordDevice：录音设备。

**返回值**：

- 录音设备音频轨道对象。

##### createAudioDeviceManager

```java
public AgoraAudioDeviceManager createAudioDeviceManager()
```

创建音频设备管理器。

**返回值**：

- 音频设备管理器对象。

##### createMediaNodeFactory

```java
public AgoraMediaNodeFactory createMediaNodeFactory()
```

创建媒体节点工厂。

**返回值**：

- 媒体节点工厂对象。

##### createCameraVideoTrack

```java
public AgoraLocalVideoTrack createCameraVideoTrack(AgoraCameraCapturer agoraCameraCapturer)
```

使用摄像头捕获器创建视频轨道。

**参数**：

- agoraCameraCapturer：摄像头捕获器。

**返回值**：

- 本地视频轨道对象。

##### createCustomVideoTrackFrame

```java
public AgoraLocalVideoTrack createCustomVideoTrackFrame(AgoraVideoFrameSender agoraVideoFrameSender)
```

使用视频帧发送器创建自定义视频轨道。

**参数**：

- agoraVideoFrameSender：视频帧发送器。

**返回值**：

- 自定义视频轨道对象。

##### createScreenVideoTrack

```java
public AgoraLocalVideoTrack createScreenVideoTrack(AgoraScreenCapturer agoraScreenCapturer)
```

使用屏幕捕获器创建视频轨道。

**参数**：

- agoraScreenCapturer：屏幕捕获器。

**返回值**：

- 本地视频轨道对象。

##### createMixedVideoTrack

```java
public AgoraLocalVideoTrack createMixedVideoTrack(AgoraVideoMixer agoraVideoMixer)
```

使用视频混合器创建视频轨道。

**参数**：

- agoraVideoMixer：视频混合器。

**返回值**：

- 本地视频轨道对象。

##### createCustomVideoTrackEncoded

```java
public AgoraLocalVideoTrack createCustomVideoTrackEncoded(AgoraVideoEncodedImageSender agora_video_encoded_image_sender, SenderOptions options)
```

使用编码图像发送器创建自定义视频轨道。

**参数**：

- agora_video_encoded_image_sender：编码图像发送器。
- options：发送器选项。

**返回值**：

- 自定义视频轨道对象。

##### createCustomVideoTrackPacket

```java
public AgoraLocalVideoTrack createCustomVideoTrackPacket(AgoraMediaPacketSender agoraMediaPacketSender)
```

使用媒体数据包发送器创建自定义视频轨道。

**参数**：

- agoraMediaPacketSender：媒体数据包发送器。

**返回值**：

- 自定义视频轨道对象。

##### createMediaPlayerVideoTrack

```java
public AgoraLocalVideoTrack createMediaPlayerVideoTrack(AgoraMediaPlayerSource agoraMediaPlayerSource)
```

使用媒体播放器源创建视频轨道。

**参数**：

- agoraMediaPlayerSource：媒体播放器源。

**返回值**：

- 本地视频轨道对象。

##### agoraRtcConnCreate

```java
public AgoraRtcConn agoraRtcConnCreate(RtcConnConfig rtcConnConfig)
```

创建 RTC 连接。

**参数**：

- rtcConnConfig：RTC 连接配置。

**返回值**：

- RTC 连接对象。

##### getAgoraParameter

```java
public AgoraParameter getAgoraParameter()
```

获取 Agora 参数对象。

**返回值**：

- Agora 参数对象。

##### loadExtensionProvider

```java
public int loadExtensionProvider(String path, boolean unloadAfterUse)
```

加载扩展提供程序。

**参数**：

- path：扩展提供程序的路径。
- unloadAfterUse：使用后是否卸载。

**返回值**：

- 0：成功
- < 0：失败

##### enableExtension

```java
public int enableExtension(String provider, String extension, String trackId, boolean enable)
```

启用或禁用指定的扩展。

**参数**：

- provider：扩展提供程序的名称。
- extension：扩展的名称。
- trackId：轨道 ID。
- enable：是否启用扩展。

**返回值**：

- 0：成功
- < 0：失败

##### disableExtension

```java
public int disableExtension(String provider, String extension, String trackId)
```

禁用指定的扩展。

**参数**：

- provider：扩展提供程序的名称。
- extension：扩展的名称。
- trackId：轨道 ID。

**返回值**：

- 0：成功
- < 0：失败

##### createDataStream

```java
public int createDataStream(Out streamId, int reliable, int ordered)
```

创建数据流。

**参数**：

- streamId：用于存储数据流 ID 的输出参数。
- reliable：是否可靠传输（1 表示可靠，0 表示不可靠）。
- ordered：是否有序传输（1 表示有序，0 表示无序）。

**返回值**：

- 0：成功
- < 0：失败

##### enableEncryption

```java
public int enableEncryption(int enabled, EncryptionConfig config)
```

启用或禁用加密。

**参数**：

- enabled：是否启用加密（1 表示启用，0 表示禁用）。
- config：加密配置。

**返回值**：

- 0：成功
- < 0：失败

##### createRtmpStreamingService

```java
public AgoraRtmpStreamingService createRtmpStreamingService(AgoraRtcConn agoraRtcConn, String appId)
```

创建 RTMP 推流服务。

**参数**：

- agoraRtcConn: RTC 连接对象。
- appId: 应用 ID。

**返回值**：

- RTMP 推流服务对象。

##### createRtmService

```java
public AgoraRtmService createRtmService()
```

创建 RTM 服务。

**返回值**：

- RTM 服务对象。

### AgoraMediaNodeFactory

AgoraMediaNodeFactory 类用于创建各种媒体节点，如发送器、捕获器等。

```java
public class AgoraMediaNodeFactory {
    // 构造方法
    public AgoraMediaNodeFactory(long cptr)
}
```

#### 方法

##### destroy

```java
public synchronized void destroy()
```

销毁媒体节点工厂。
确保释放本地资源。

##### createAudioPcmDataSender

```java
public AgoraAudioPcmDataSender createAudioPcmDataSender()
```

创建音频 PCM 数据发送器实例。

**返回值**：

- AgoraAudioPcmDataSender 实例。

##### createAudioEncodedFrameSender

```java
public AgoraAudioEncodedFrameSender createAudioEncodedFrameSender()
```

创建音频编码帧发送器实例。

**返回值**：

- AgoraAudioEncodedFrameSender 实例。

##### createCameraCapturer

```java
public AgoraCameraCapturer createCameraCapturer()
```

创建摄像头捕获器实例。

**返回值**：

- AgoraCameraCapturer 实例。

##### createScreenCapturer

```java
public AgoraScreenCapturer createScreenCapturer()
```

创建屏幕捕获器实例。

**返回值**：

- AgoraScreenCapturer 实例。

##### createVideoMixer

```java
public AgoraVideoMixer createVideoMixer()
```

创建视频混合器实例。

**返回值**：

- AgoraVideoMixer 实例。

##### createVideoFrameSender

```java
public AgoraVideoFrameSender createVideoFrameSender()
```

创建视频帧发送器实例。

**返回值**：

- AgoraVideoFrameSender 实例。

##### createVideoEncodedImageSender

```java
public AgoraVideoEncodedImageSender createVideoEncodedImageSender()
```

创建视频编码图像发送器实例。

**返回值**：

- AgoraVideoEncodedImageSender 实例。

##### createVideoRenderer

```java
public AgoraVideoRenderer createVideoRenderer()
```

创建视频渲染器实例。

**返回值**：

- AgoraVideoRenderer 实例。

##### createMediaPlayerSource

```java
public AgoraMediaPlayerSource createMediaPlayerSource(int type)
```

创建媒体播放源实例。

**参数**：

- type：要创建的媒体播放源类型。

**返回值**：

- AgoraMediaPlayerSource 实例。

##### createMediaPacketSender

```java
public AgoraMediaPacketSender createMediaPacketSender()
```

创建媒体数据包发送器实例。

**返回值**：

- AgoraMediaPacketSender 实例。

### AgoraRtcConn

AgoraRtcConn 类用于管理 RTC 连接，如加入频道、发送消息等。
**注意：** 部分方法如 `getAgoraParameter`, `createDataStream`, `enableEncryption` 在此文档中错误地归类在 `AgoraMediaNodeFactory` 下，实际应属于 `AgoraRtcConn`。

```java
public class AgoraRtcConn {
    // 构造方法
    public AgoraRtcConn(long cptr)
}
```

#### 方法

##### destroy

```java
public synchronized void destroy()
```

销毁 RTC 连接实例，并释放相关资源。
该方法会先取消注册所有观察者，销毁本地用户和 AgoraParameter 对象，然后调用底层 native 方法销毁连接。

##### registerObserver

```java
public int registerObserver(IRtcConnObserver observer)
```

注册 RTC 连接观察者，用于接收连接相关的事件回调。

**参数**：

- `observer`：要注册的 `IRtcConnObserver` 实现类实例。

**返回值**：

- 0：成功
- < 0：失败

##### unregisterObserver

```java
public int unregisterObserver()
```

取消注册 RTC 连接观察者。

**返回值**：

- 0：成功
- < 0：失败

##### registerNetworkObserver

```java
public int registerNetworkObserver(INetworkObserver observer)
```

注册网络观察者，用于接收网络相关的事件回调。

**参数**：

- `observer`：要注册的 `INetworkObserver` 实现类实例。

**返回值**：

- 0：成功
- < 0：失败

##### unregisterNetworkObserver

```java
public int unregisterNetworkObserver()
```

取消注册网络观察者。

**返回值**：

- 0：成功
- < 0：失败

##### connect

```java
public int connect(String token, String channelId, String userId)
```

连接到指定的 RTC 频道。

**参数**：

- `token`：用于鉴权的 Token。
- `channelId`：要加入的频道 ID。
- `userId`：用户 ID。

**返回值**：

- 0：成功
- < 0：失败

##### disconnect

```java
public int disconnect()
```

断开当前的 RTC 连接。

**返回值**：

- 0：成功
- < 0：失败

##### sendStreamMessage (推荐)

```java
public int sendStreamMessage(int streamId, byte[] messageData)
```

通过指定的数据流 ID 发送数据流消息。

**参数**：

- `streamId`：数据流 ID，通过 `createDataStream` 创建。
- `messageData`：要发送的字节数组消息。

**返回值**：

- 0：成功
- < 0：失败

##### sendStreamMessage (已弃用)

```java
@Deprecated
public int sendStreamMessage(int streamId, String message, int length)
```

**已弃用**：请使用 `sendStreamMessage(int streamId, byte[] messageData)`。
通过指定的数据流 ID 发送字符串消息。

**参数**：

- `streamId`：数据流 ID。
- `message`：要发送的字符串消息。
- `length`：消息长度（此参数在 Java 中通常不必要，因为字节数组自带长度信息，可能是 native 层接口遗留）。

**返回值**：

- 0：成功
- < 0：失败

##### getConnInfo

```java
public RtcConnInfo getConnInfo()
```

获取当前连接的信息。

**返回值**：

- `RtcConnInfo` 对象，包含连接详情。使用后需要调用 `destroyConnInfo` 释放。

##### destroyConnInfo

```java
public void destroyConnInfo(RtcConnInfo info)
```

销毁通过 `getConnInfo` 获取的 `RtcConnInfo` 对象，释放资源。

**参数**：

- `info`：要销毁的 `RtcConnInfo` 对象。

##### getLocalUser

```java
public AgoraLocalUser getLocalUser()
```

获取与此连接关联的本地用户对象。

**返回值**：

- `AgoraLocalUser` 对象。

##### getUserInfo

```java
@Deprecated
public UserInfo getUserInfo(String userId)
```

**已弃用**：请根据需要使用 `getUserInfoByUserAccount` 或 `getUserInfoByUid`。
根据用户 ID 获取用户信息。

**参数**：

- `userId`：用户 ID。

**返回值**：

- `UserInfo` 对象，包含用户详情。使用后需要调用 `destroyUserInfo` 释放。

##### getUserInfoByUserAccount

```java
public UserInfo getUserInfoByUserAccount(String userAccount)
```

根据用户账号获取用户信息。

**参数**：

- `userAccount`：用户账号。

**返回值**：

- `UserInfo` 对象，包含用户详情。使用后需要调用 `destroyUserInfo` 释放。

##### getUserInfoByUid

```java
public UserInfo getUserInfoByUid(int uid)
```

根据 UID 获取用户信息。

**参数**：

- `uid`：用户 UID。

**返回值**：

- `UserInfo` 对象，包含用户详情。使用后需要调用 `destroyUserInfo` 释放。

##### destroyUserInfo

```java
public void destroyUserInfo(UserInfo info)
```

销毁通过 `getUserInfo`, `getUserInfoByUserAccount`, `getUserInfoByUid` 获取的 `UserInfo` 对象，释放资源。

**参数**：

- `info`：要销毁的 `UserInfo` 对象。

##### getConnId

```java
public long getConnId()
```

获取当前连接的 ID。

**返回值**：

- 连接 ID (`long`)。

##### getTransportStats

```java
public RtcStats getTransportStats()
```

获取当前连接的传输统计信息。

**返回值**：

- `RtcStats` 对象，包含统计信息。使用后需要调用 `destroyTransportStats` 释放。

##### destroyTransportStats

```java
public void destroyTransportStats(RtcStats stats)
```

销毁通过 `getTransportStats` 获取的 `RtcStats` 对象，释放资源。

**参数**：

- `stats`：要销毁的 `RtcStats` 对象。

##### sendCustomReportMessage

```java
public int sendCustomReportMessage(String id, String category, String event, String label, int value)
```

发送自定义事件报告消息。

**参数**：

- `id`：报告消息的 ID。
- `category`：事件类别。
- `event`：事件名称。
- `label`：事件标签。
- `value`：事件值。

**返回值**：

- 0：成功
- < 0：失败

##### getAgoraParameter

```java
public AgoraParameter getAgoraParameter()
```

获取用于配置 SDK 参数的 `AgoraParameter` 对象。

**返回值**：

- `AgoraParameter` 实例。

##### createDataStream

```java
public int createDataStream(DataStreamConfig config)
```

创建数据流。

**参数**：

- `config`：数据流配置 (`DataStreamConfig`)。

**返回值**：

- 数据流 ID (`int`)。

##### enableEncryption

```java
public int enableEncryption(boolean enabled, EncryptionConfig config)
```

启用或禁用内置加密。

**参数**：

- `enabled`：是否启用加密 (`boolean`)。
- `config`：加密配置 (`EncryptionConfig`)。

**返回值**：

- 0：成功
- < 0：失败

### AgoraLocalUser

AgoraLocalUser 类表示本地用户，提供发布媒体流、订阅远端媒体流等功能。

```java
public class AgoraLocalUser {
    // 构造方法
    public AgoraLocalUser(long cptr)
}
```

#### 方法

##### destroy

```java
public synchronized void destroy()
```

销毁 `AgoraLocalUser` 实例。该方法会先取消注册所有关联的观察者（音频帧、视频帧、编码帧、本地用户等），然后释放底层 native 资源。

##### registerAudioFrameObserver

```java
public int registerAudioFrameObserver(IAudioFrameObserver observer)
```

注册音频帧观察者，用于接收音频帧数据回调。

**参数**：

- `observer`：要注册的 `IAudioFrameObserver` 实现类实例。

**返回值**：

- 0：成功
- < 0：失败

##### registerAudioFrameObserver (带 VAD)

```java
public int registerAudioFrameObserver(IAudioFrameObserver observer, boolean enableVad, AgoraAudioVadConfigV2 vadConfig)
```

注册带有 VAD（语音活动检测）功能的音频帧观察者。

**参数**：

- `observer`：要注册的 `IAudioFrameObserver` 实现类实例。
- `enableVad`：是否启用 VAD (`boolean`)。
- `vadConfig`：VAD 配置 (`AgoraAudioVadConfigV2`)。

**返回值**：

- 0：成功
- < 0：失败

##### unregisterAudioFrameObserver

```java
public int unregisterAudioFrameObserver()
```

取消注册音频帧观察者。

**返回值**：

- 0：成功
- < 0：失败

##### registerAudioEncodedFrameObserver

```java
public int registerAudioEncodedFrameObserver(IAudioEncodedFrameObserver observer)
```

注册音频编码帧观察者。

**参数**：

- `observer`：要注册的 `IAudioEncodedFrameObserver` 实现类实例。

**返回值**：

- 0：成功
- < 0：失败

##### unregisterAudioEncodedFrameObserver

```java
public int unregisterAudioEncodedFrameObserver(IAudioEncodedFrameObserver observer)
```

取消注册音频编码帧观察者。

**参数**：

- `observer`：要取消注册的 `IAudioEncodedFrameObserver` 实例（应与注册时传入的实例相同）。

**返回值**：

- 0：成功
- < 0：失败

##### registerObserver

```java
public int registerObserver(ILocalUserObserver observer)
```

注册本地用户观察者，用于接收本地用户的事件回调。

**参数**：

- `observer`：要注册的 `ILocalUserObserver` 实现类实例。

**返回值**：

- 0：成功
- < 0：失败

##### unregisterObserver

```java
public int unregisterObserver()
```

取消注册本地用户观察者。

**返回值**：

- 0：成功
- < 0：失败

##### registerVideoFrameObserver

```java
public int registerVideoFrameObserver(AgoraVideoFrameObserver2 agoraVideoFrameObserver2)
```

注册视频帧观察者（V2 版本接口）。

**参数**：

- `agoraVideoFrameObserver2`：要注册的 `AgoraVideoFrameObserver2` 实例。

**返回值**：

- 0：成功
- < 0：失败

##### unregisterVideoFrameObserver

```java
public int unregisterVideoFrameObserver(AgoraVideoFrameObserver2 agoraVideoFrameObserver2)
```

取消注册视频帧观察者。

**参数**：

- `agoraVideoFrameObserver2`：要取消注册的 `AgoraVideoFrameObserver2` 实例（应与注册时传入的实例相同）。

**返回值**：

- 0：成功
- < 0：失败

##### registerVideoEncodedFrameObserver

```java
public int registerVideoEncodedFrameObserver(AgoraVideoEncodedFrameObserver agoraVideoEncodedFrameObserver)
```

注册视频编码帧观察者。

**参数**：

- `agoraVideoEncodedFrameObserver`：要注册的 `AgoraVideoEncodedFrameObserver` 实例。

**返回值**：

- 0：成功
- < 0：失败

##### unregisterVideoEncodedFrameObserver

```java
public int unregisterVideoEncodedFrameObserver(AgoraVideoEncodedFrameObserver agoraVideoEncodedFrameObserver)
```

取消注册视频编码帧观察者。

**参数**：

- `agoraVideoEncodedFrameObserver`：要取消注册的 `AgoraVideoEncodedFrameObserver` 实例（应与注册时传入的实例相同）。

**返回值**：

- 0：成功
- < 0：失败

##### setUserRole

```java
public void setUserRole(int role)
```

设置用户角色（如主播、观众）。使用 `Constants.CLIENT_ROLE_TYPE` 中的常量。

**参数**：

- `role`：要设置的用户角色。

##### getUserRole

```java
public int getUserRole()
```

获取当前用户角色。

**返回值**：

- 当前用户角色 (`int`)，对应 `Constants.CLIENT_ROLE_TYPE`。

##### setAudioEncoderConfig

```java
public int setAudioEncoderConfig(AudioEncoderConfig config)
```

设置音频编码器配置。

**参数**：

- `config`：音频编码器配置 (`AudioEncoderConfig`)。

**返回值**：

- 0：成功
- < 0：失败

##### getLocalAudioStatistics

```java
public LocalAudioDetailedStats getLocalAudioStatistics()
```

获取本地音频详细统计信息。

**返回值**：

- `LocalAudioDetailedStats` 对象。使用后需要调用 `destroyLocalAudioStatistics` 释放。

##### destroyLocalAudioStatistics

```java
public void destroyLocalAudioStatistics(LocalAudioDetailedStats stats)
```

销毁通过 `getLocalAudioStatistics` 获取的 `LocalAudioDetailedStats` 对象，释放资源。

**参数**：

- `stats`：要销毁的 `LocalAudioDetailedStats` 对象。

##### publishAudio

```java
public int publishAudio(AgoraLocalAudioTrack agoraLocalAudioTrack)
```

发布本地音频轨道。

**参数**：

- `agoraLocalAudioTrack`：要发布的本地音频轨道 (`AgoraLocalAudioTrack`)。

**返回值**：

- 0：成功
- < 0：失败

##### unpublishAudio

```java
public int unpublishAudio(AgoraLocalAudioTrack agoraLocalAudioTrack)
```

取消发布本地音频轨道。

**参数**：

- `agoraLocalAudioTrack`：要取消发布的本地音频轨道 (`AgoraLocalAudioTrack`)。

**返回值**：

- 0：成功
- < 0：失败

##### publishVideo

```java
public int publishVideo(AgoraLocalVideoTrack agoraLocalVideoTrack)
```

发布本地视频轨道。

**参数**：

- `agoraLocalVideoTrack`：要发布的本地视频轨道 (`AgoraLocalVideoTrack`)。

**返回值**：

- 0：成功
- < 0：失败

##### unpublishVideo

```java
public int unpublishVideo(AgoraLocalVideoTrack agoraLocalVideoTrack)
```

取消发布本地视频轨道。

**参数**：

- `agoraLocalVideoTrack`：要取消发布的本地视频轨道 (`AgoraLocalVideoTrack`)。

**返回值**：

- 0：成功
- < 0：失败

##### subscribeAudio

```java
public int subscribeAudio(String userId)
```

订阅指定远端用户的音频流。

**参数**：

- `userId`：要订阅的远端用户 ID。

**返回值**：

- 0：成功
- < 0：失败

##### subscribeAllAudio

```java
public int subscribeAllAudio()
```

订阅频道内所有远端用户的音频流。

**返回值**：

- 0：成功
- < 0：失败

##### unsubscribeAudio

```java
public int unsubscribeAudio(String userId)
```

取消订阅指定远端用户的音频流。

**参数**：

- `userId`：要取消订阅的远端用户 ID。

**返回值**：

- 0：成功
- < 0：失败

##### unsubscribeAllAudio

```java
public int unsubscribeAllAudio()
```

取消订阅频道内所有远端用户的音频流。

**返回值**：

- 0：成功
- < 0：失败

##### adjustPlaybackSignalVolume

```java
public int adjustPlaybackSignalVolume(int volume)
```

调整本地播放的所有远端用户混音后的音量。

**参数**：

- `volume`：音量，范围 [0, 100]。0 表示静音，100 表示原始音量。

**返回值**：

- 0：成功
- < 0：失败

##### getPlaybackSignalVolume

```java
public int getPlaybackSignalVolume(Out volume)
```

获取本地播放的所有远端用户混音后的音量。

**参数**：

- `volume`：用于接收音量值的 `Out<Integer>` 对象。

**返回值**：

- 0：成功
- < 0：失败

##### setPlaybackAudioFrameParameters

```java
public int setPlaybackAudioFrameParameters(int channels, int sampleRateHz, int mode, int samplesPerCall)
```

设置播放音频帧参数。

**参数**：

- `channels`：声道数。
- `sampleRateHz`：采样率 (Hz)。
- `mode`：工作模式 (如 `Constants.RAW_AUDIO_FRAME_OP_MODE_TYPE.RAW_AUDIO_FRAME_OP_MODE_READ_ONLY`)。
- `samplesPerCall`：每次回调的样本数。

**返回值**：

- 0：成功
- < 0：失败

##### setRecordingAudioFrameParameters

```java
public int setRecordingAudioFrameParameters(int channels, int sampleRateHz, int mode, int samplesPerCall)
```

设置录制音频帧参数。

**参数**：

- `channels`：声道数。
- `sampleRateHz`：采样率 (Hz)。
- `mode`：工作模式。
- `samplesPerCall`：每次回调的样本数。

**返回值**：

- 0：成功
- < 0：失败

##### setMixedAudioFrameParameters

```java
public int setMixedAudioFrameParameters(int channels, int sampleRateHz, int samplesPerCall)
```

设置混合音频帧参数。

**参数**：

- `channels`：声道数。
- `sampleRateHz`：采样率 (Hz)。
- `samplesPerCall`：每次回调的样本数。

**返回值**：

- 0：成功
- < 0：失败

##### setPlaybackAudioFrameBeforeMixingParameters

```java
public int setPlaybackAudioFrameBeforeMixingParameters(int channels, int sampleRateHz)
```

设置混音前播放音频帧参数。

**参数**：

- `channels`：声道数。
- `sampleRateHz`：采样率 (Hz)。

**返回值**：

- 0：成功
- < 0：失败

##### subscribeVideo

```java
public int subscribeVideo(String userId, VideoSubscriptionOptions options)
```

订阅指定远端用户的视频流。

**参数**：

- `userId`：要订阅的远端用户 ID。
- `options`：视频订阅选项 (`VideoSubscriptionOptions`)。

**返回值**：

- 0：成功
- < 0：失败

##### subscribeAllVideo

```java
public int subscribeAllVideo(VideoSubscriptionOptions options)
```

订阅频道内所有远端用户的视频流。

**参数**：

- `options`：视频订阅选项 (`VideoSubscriptionOptions`)。

**返回值**：

- 0：成功
- < 0：失败

##### unsubscribeVideo

```java
public int unsubscribeVideo(String userId)
```

取消订阅指定远端用户的视频流。

**参数**：

- `userId`：要取消订阅的远端用户 ID。

**返回值**：

- 0：成功
- < 0：失败

##### unsubscribeAllVideo

```java
public int unsubscribeAllVideo()
```

取消订阅频道内所有远端用户的视频流。

**返回值**：

- 0：成功
- < 0：失败

##### setAudioVolumeIndicationParameters

```java
public int setAudioVolumeIndicationParameters(int intervalInMs, int smooth, boolean reportVad)
```

设置音频音量提示参数，用于控制 `onAudioVolumeIndication` 回调。

**参数**：

- `intervalInMs`：回调间隔（毫秒），<= 0 表示禁用。
- `smooth`：平滑系数，建议设置为 3。
- `reportVad`：是否报告语音活动状态 (`boolean`)。

**返回值**：

- 0：成功
- < 0：失败

##### getMediaControlPacketSender

```java
public AgoraMediaCtrlPacketSender getMediaControlPacketSender()
```

获取媒体控制数据包发送器实例，用于发送自定义媒体控制消息。

**返回值**：

- `AgoraMediaCtrlPacketSender` 实例。

##### registerMediaControlPacketReceiver

```java
public int registerMediaControlPacketReceiver(AgoraMediaPacketReceiver agoraMediaPacketReceiver)
```

注册媒体控制数据包接收器，用于接收远端发送的媒体控制消息。

**参数**：

- `agoraMediaPacketReceiver`：要注册的 `AgoraMediaPacketReceiver` 实例。

**返回值**：

- 0：成功
- < 0：失败

##### unregisterMediaControlPacketReceiver

```java
public int unregisterMediaControlPacketReceiver(AgoraMediaPacketReceiver agoraMediaPacketReceiver)
```

取消注册媒体控制数据包接收器。

**参数**：

- `agoraMediaPacketReceiver`：要取消注册的 `AgoraMediaPacketReceiver` 实例。

**返回值**：

- 0：成功
- < 0：失败

##### sendIntraRequest

```java
public int sendIntraRequest(String userId)
```

向指定远端用户请求发送关键帧（I 帧），通常用于视频画面卡顿或花屏时请求刷新。

**参数**：

- `userId`：远端用户 ID。

**返回值**：

- 0：成功
- < 0：失败

##### setAudioScenario

```java
public int setAudioScenario(int scenarioType)
```

设置音频应用场景。不同的场景会应用不同的音频优化策略。

**参数**：

- `scenarioType`：音频场景类型，使用 `Constants.AUDIO_SCENARIO_TYPE` 中的常量。

**返回值**：

- 0：成功
- < 0：失败

##### sendAudioMetaData

```java
public int sendAudioMetaData(byte[] metaData)
```

发送音频元数据。元数据会附加在音频包中发送给远端。

**参数**：

- `metaData`：要发送的元数据字节数组。

**返回值**：

- 0：成功
- < 0：失败

### AgoraLocalAudioTrack

AgoraLocalAudioTrack 类表示本地音频轨道，提供对音频的控制功能。

```java
public class AgoraLocalAudioTrack {
    // 构造方法
    public AgoraLocalAudioTrack(long cptr)
}
```

#### 方法

##### getNativeHandle

```java
public long getNativeHandle()
```

获取本地音频轨道的原生句柄。

**返回值**：

- 本地音频轨道的原生句柄。

##### destroy

```java
public synchronized void destroy()
```

销毁本地音频轨道。
确保释放原生资源。

##### setEnabled

```java
public void setEnabled(int enable)
```

启用或禁用本地音频轨道。

**参数**：

- enable：1 表示启用，0 表示禁用。

##### isEnabled

```java
public int isEnabled()
```

检查本地音频轨道是否已启用。

**返回值**：

- 1：已启用
- 0：已禁用

##### getState

```java
public int getState()
```

获取本地音频轨道的当前状态。

**返回值**：

- 音频轨道的状态。

##### getStats

```java
public LocalAudioTrackStats getStats()
```

获取本地音频轨道的统计信息。

**返回值**：

- 包含统计信息的 LocalAudioTrackStats 实例。

##### destroyStats

```java
public void destroyStats(LocalAudioTrackStats stats)
```

销毁提供的 LocalAudioTrackStats 对象。

**参数**：

- stats：要销毁的 LocalAudioTrackStats 对象。

##### adjustPublishVolume

```java
public int adjustPublishVolume(int volume)
```

调整本地音频轨道的发布音量。

**参数**：

- volume：要设置的音量级别。

**返回值**：

- 0：成功
- < 0：失败

##### getPublishVolume

```java
public int getPublishVolume(Out volume)
```

获取本地音频轨道的当前发布音量。

**参数**：

- volume：用于存储音量级别的 Out 对象。

**返回值**：

- 0：成功
- < 0：失败

##### enableLocalPlayback

```java
public int enableLocalPlayback(int enable)
```

启用或禁用音频轨道的本地回放。

**参数**：

- enable：1 表示启用，0 表示禁用。

**返回值**：

- 0：成功
- < 0：失败

##### enableEarMonitor

```java
public int enableEarMonitor(int enable, int includeAudiFilter)
```

启用或禁用耳机监听。

**参数**：

- enable：1 表示启用，0 表示禁用。
- includeAudiFilter：1 表示包含音频过滤器，0 表示排除。

**返回值**：

- 0：成功
- < 0：失败

##### setMaxBufferedAudioFrameNumber

```java
public void setMaxBufferedAudioFrameNumber(int number)
```

设置缓冲的最大音频帧数。

**参数**：

- number：要缓冲的最大帧数。

##### clearSenderBuffer

```java
public int clearSenderBuffer()
```

清除发送器缓冲区。

**返回值**：

- 0：成功
- < 0：失败

##### setSendDelayMs

```java
public void setSendDelayMs(int delayMs)
```

设置发送延迟（毫秒）。

**参数**：

- delayMs：延迟的毫秒数。

### AgoraLocalVideoTrack

AgoraLocalVideoTrack 类表示本地视频轨道，提供对视频的控制功能。

```java
public class AgoraLocalVideoTrack {
    // 构造方法
    public AgoraLocalVideoTrack(long cptr)
}
```

#### 方法

##### getNativeHandle

```java
public long getNativeHandle()
```

获取本地视频轨道的原生句柄。

**返回值**：

- 本地视频轨道的原生句柄。

##### destroy

```java
public synchronized void destroy()
```

销毁本地视频轨道。
确保释放原生资源。

##### setEnabled

```java
public void setEnabled(int enable)
```

启用或禁用本地视频轨道。

**参数**：

- enable：1 表示启用，0 表示禁用。

##### setVideoEncoderConfig

```java
public int setVideoEncoderConfig(VideoEncoderConfig config)
```

设置视频编码器配置。

**参数**：

- config：要设置的视频编码器配置。

**返回值**：

- 0：成功
- < 0：失败

##### enableSimulcastStream

```java
public int enableSimulcastStream(int enabled, SimulcastStreamConfig config)
```

启用或禁用同时直播流。

**参数**：

- enabled：1 表示启用，0 表示禁用。
- config：同时直播流配置。

**返回值**：

- 0：成功
- < 0：失败

##### getState

```java
public int getState()
```

获取本地视频轨道的当前状态。

**返回值**：

- 视频轨道的状态。

##### getStatistics

```java
public LocalVideoTrackStats getStatistics()
```

获取本地视频轨道的统计信息。

**返回值**：

- 包含统计信息的 LocalVideoTrackStats 实例。

##### destroyStatistics

```java
public void destroyStatistics(LocalVideoTrackStats stats)
```

销毁提供的 LocalVideoTrackStats 对象。

**参数**：

- stats：要销毁的 LocalVideoTrackStats 对象。

### AgoraRemoteAudioTrack

AgoraRemoteAudioTrack 类表示远程音频轨道，提供获取远程音频信息的功能。

```java
public class AgoraRemoteAudioTrack {
    // 构造方法
    public AgoraRemoteAudioTrack(long cptr)
}
```

#### 方法

##### getNativeHandle

```java
public long getNativeHandle()
```

获取远程音频轨道的原生句柄。

**返回值**：

- 远程音频轨道的原生句柄。

##### destroy

```java
public void destroy()
```

销毁远程音频轨道。
确保释放原生资源。

##### getStatistics

```java
public RemoteAudioTrackStats getStatistics()
```

获取远程音频轨道的统计信息。

**返回值**：

- 远程音频轨道统计信息。

##### destroyStatistics

```java
public void destroyStatistics(RemoteAudioTrackStats stats)
```

销毁提供的 RemoteAudioTrackStats 对象。

**参数**：

- stats：要销毁的 RemoteAudioTrackStats 对象。

##### getState

```java
public int getState()
```

获取远程音频轨道的当前状态。

**返回值**：

- 远程音频轨道的状态。

##### registerMediaPacketReceiver

```java
public int registerMediaPacketReceiver(AgoraMediaPacketReceiver agoraMediaPacketReceiver)
```

注册媒体数据包接收器。

**参数**：

- agoraMediaPacketReceiver：要注册的媒体数据包接收器。

**返回值**：

- 0：成功
- < 0：失败

##### unregisterMediaPacketReceiver

```java
public int unregisterMediaPacketReceiver(AgoraMediaPacketReceiver agoraMediaPacketReceiver)
```

取消注册媒体数据包接收器。

**参数**：

- agoraMediaPacketReceiver：要取消注册的媒体数据包接收器。

**返回值**：

- 0：成功
- < 0：失败

### AgoraRemoteVideoTrack

AgoraRemoteVideoTrack 类表示远程视频轨道，提供获取远程视频信息的功能。

```java
public class AgoraRemoteVideoTrack {
    // 构造方法
    public AgoraRemoteVideoTrack(long cptr)
}
```

#### 成员变量

##### mediaPacketReceiver

```java
private IMediaPacketReceiver mediaPacketReceiver
```

媒体数据包接收器。

#### 方法

##### getNativeHandle

```java
public long getNativeHandle()
```

获取远程视频轨道的原生句柄。

**返回值**：

- 远程视频轨道的原生句柄。

##### destroy

```java
public void destroy()
```

销毁远程视频轨道。
确保释放原生资源。

##### getStatistics

```java
public RemoteVideoTrackStats getStatistics()
```

获取远程视频轨道的统计信息。

**返回值**：

- 远程视频轨道统计信息。

##### destroyStatistics

```java
public void destroyStatistics(RemoteVideoTrackStats stats)
```

销毁提供的 RemoteVideoTrackStats 对象。

**参数**：

- stats：要销毁的 RemoteVideoTrackStats 对象。

##### getState

```java
public int getState()
```

获取远程视频轨道的当前状态。

**返回值**：

- 远程视频轨道的状态。

##### getTrackInfo

```java
public VideoTrackInfo getTrackInfo()
```

获取视频轨道信息。

**返回值**：

- 视频轨道信息。

##### destroyTrackInfo

```java
public void destroyTrackInfo(VideoTrackInfo info)
```

销毁提供的 VideoTrackInfo 对象。

**参数**：

- info：要销毁的 VideoTrackInfo 对象。

##### registerVideoEncodedImageReceiver

```java
public int registerVideoEncodedImageReceiver(AgoraVideoEncodedImageReceiver agoraVideoEncodedImageReceiver)
```

注册视频编码图像接收器。

**参数**：

- agoraVideoEncodedImageReceiver：要注册的视频编码图像接收器。

**返回值**：

- 0：成功
- < 0：失败

##### unregisterVideoEncodedImageReceiver

```java
public int unregisterVideoEncodedImageReceiver(AgoraVideoEncodedImageReceiver agoraVideoEncodedImageReceiver)
```

取消注册视频编码图像接收器。

**参数**：

- agoraVideoEncodedImageReceiver：要取消注册的视频编码图像接收器。

**返回值**：

- 0：成功
- < 0：失败

##### registerMediaPacketReceiver

```java
public int registerMediaPacketReceiver(IMediaPacketReceiver agoraMediaPacketReceiver)
```

注册媒体数据包接收器。

**参数**：

- agoraMediaPacketReceiver：要注册的媒体数据包接收器。

**返回值**：

- 0：成功
- < 0：失败

##### unregisterMediaPacketReceiver

```java
public int unregisterMediaPacketReceiver(IMediaPacketReceiver agoraMediaPacketReceiver)
```

取消注册媒体数据包接收器。

**参数**：

- agoraMediaPacketReceiver：要取消注册的媒体数据包接收器。

**返回值**：

- 0：成功
- < 0：失败

### AgoraAudioPcmDataSender

AgoraAudioPcmDataSender 类用于发送 PCM 格式的音频数据。

```java
public class AgoraAudioPcmDataSender {
    // 构造方法
    public AgoraAudioPcmDataSender(long cptr)
}
```

#### 方法

##### send

```java
@Deprecated
public int send(byte[] audioData, int captureTimestamp, int samplesPerChannel, int bytesPerSample, int numberOfChannels, int sampleRate)
```

发送音频数据。

**参数**：

- audioData：音频数据。
- captureTimestamp：捕获时间戳。
- samplesPerChannel：每个通道的样本数。
- bytesPerSample：每个样本的字节数。
- numberOfChannels：通道数。
- sampleRate：采样率。

**返回值**：

- 发送音频数据的结果。

**注意**：此方法已弃用，请使用 sendAudioPcmData(AudioFrame) 代替。

##### sendAudioPcmData

```java
public int sendAudioPcmData(AudioFrame audioFrame)
```

发送音频数据。

**参数**：

- audioFrame：音频帧。

**返回值**：

- 发送音频数据的结果。

##### destroy

```java
public synchronized void destroy()
```

销毁音频数据发送器。

### AgoraVideoFrameSender

AgoraVideoFrameSender 类用于发送视频帧。

```java
public class AgoraVideoFrameSender {
    // 构造方法
    public AgoraVideoFrameSender(long cptr)
}
```

#### 方法

##### sendVideoFrame

```java
public int sendVideoFrame(ExternalVideoFrame frame)
```

将视频帧发送到声网服务器。

**参数**：

- frame：要发送的视频帧。

**返回值**：

- 发送视频帧的结果。

##### destroy

```java
public synchronized void destroy()
```

销毁视频帧发送器。

### AgoraVideoEncodedImageSender

AgoraVideoEncodedImageSender 类用于发送已编码的视频图像。

```java
public class AgoraVideoEncodedImageSender {
    // 构造方法
    public AgoraVideoEncodedImageSender(long cptr)
}
```

#### 方法

##### sendEncodedVideoImage

```java
public int sendEncodedVideoImage(byte[] imageBuffer, EncodedVideoFrameInfo info)
```

将已编码的视频图像发送到声网服务器。

**参数**：

- imageBuffer：编码的视频图像数据。
- info：关于编码视频图像的信息。

**返回值**：

- 发送编码视频图像的结果。

##### destroy

```java
public synchronized void destroy()
```

销毁视频编码图像发送器。

### AgoraAudioEncodedFrameSender

AgoraAudioEncodedFrameSender 类用于发送已编码的音频帧。

```java
public class AgoraAudioEncodedFrameSender {
    // 构造方法
    public AgoraAudioEncodedFrameSender(long cptr)
}
```

#### 方法

##### sendEncodedAudioFrame

```java
public int sendEncodedAudioFrame(byte[] payloadData, EncodedAudioFrameInfo info)
```

将已编码的音频帧发送到声网服务器。

**参数**：

- payloadData：编码的音频数据。
- info：关于编码音频帧的信息。

**返回值**：

- 发送编码音频帧的结果。

##### destroy

```java
public synchronized void destroy()
```

销毁音频编码帧发送器。

### AgoraParameter

AgoraParameter 类用于设置和获取 Agora SDK 的参数。

```java
public class AgoraParameter {
    // 构造方法
    public AgoraParameter(long cptr)
}
```

#### 方法

##### destroy

```java
public void destroy()
```

销毁 AgoraParameter 对象。

##### setInt

```java
public int setInt(String key, int value)
```

设置整数参数。

**参数**：

- key：参数的键。
- value：要设置的整数值。

**返回值**：

- 0：成功
- < 0：失败

##### setBool

```java
public int setBool(String key, boolean value)
```

设置布尔参数。

**参数**：

- key：参数的键。
- value：要设置的布尔值。

**返回值**：

- 0：成功
- < 0：失败

##### setUint

```java
public int setUint(String key, int value)
```

设置无符号整数参数。

**参数**：

- key：参数的键。
- value：要设置的无符号整数值。

**返回值**：

- 0：成功
- < 0：失败

##### setNumber

```java
public int setNumber(String key, double value)
```

设置数值参数。

**参数**：

- key：参数的键。
- value：要设置的数值。

**返回值**：

- 0：成功
- < 0：失败

##### setString

```java
public int setString(String key, String value)
```

设置字符串参数。

**参数**：

- key：参数的键。
- value：要设置的字符串值。

**返回值**：

- 0：成功
- < 0：失败

##### setArray

```java
public int setArray(String key, String json_src)
```

使用 JSON 字符串设置数组参数。

**参数**：

- key：参数的键。
- json_src：表示数组的 JSON 字符串。

**返回值**：

- 0：成功
- < 0：失败

##### setParameters

```java
public int setParameters(String json_src)
```

使用 JSON 字符串设置多个参数。

**参数**：

- json_src：包含多个参数的 JSON 字符串。

**返回值**：

- 0：成功
- < 0：失败

##### getInt

```java
public int getInt(String key, Out value)
```

获取整数参数。

**参数**：

- key：参数的键。
- value：用于存储整数值的 Out 对象。

**返回值**：

- 0：成功
- < 0：失败

##### getBool

```java
public int getBool(String key, Out value)
```

获取布尔参数。

**参数**：

- key：参数的键。
- value：用于存储布尔值的 Out 对象。

**返回值**：

- 0：成功
- < 0：失败

##### getUint

```java
public int getUint(String key, Out value)
```

获取无符号整数参数。

**参数**：

- key：参数的键。
- value：用于存储无符号整数值的 Out 对象。

**返回值**：

- 0：成功
- < 0：失败

##### getNumber

```java
public int getNumber(String key, Out value)
```

获取数值参数。

**参数**：

- key：参数的键。
- value：用于存储数值的 Out 对象。

**返回值**：

- 0：成功
- < 0：失败

##### getString

```java
public int getString(String key, Out value)
```

获取字符串参数。

**参数**：

- key：参数的键。
- value：用于存储字符串值的 Out 对象。

**返回值**：

- 0：成功
- < 0：失败

## 观察者接口

### IRtcConnObserver

IRtcConnObserver 接口用于监听 RTC 连接的状态和事件。

```java
public interface IRtcConnObserver {
    // 方法声明
}
```

#### 方法

##### onConnected

```java
public void onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason)
```

当连接成功建立时触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `connInfo`：连接信息。
- `reason`：连接成功的原因 (`Constants.CONNECTION_CHANGED_REASON_TYPE`)。

##### onDisconnected

```java
public void onDisconnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason)
```

当连接断开时触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `connInfo`：连接信息。
- `reason`：连接断开的原因 (`Constants.CONNECTION_CHANGED_REASON_TYPE`)。

##### onConnecting

```java
public void onConnecting(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason)
```

当 SDK 正在尝试连接到服务器时触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `connInfo`：连接信息。
- `reason`：连接状态改变的原因 (`Constants.CONNECTION_CHANGED_REASON_TYPE`)。

##### onReconnecting

```java
public void onReconnecting(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason)
```

当 SDK 正在尝试重新连接到服务器时触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `connInfo`：连接信息。
- `reason`：连接状态改变的原因 (`Constants.CONNECTION_CHANGED_REASON_TYPE`)。

##### onReconnected

```java
public void onReconnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason)
```

当 SDK 重新连接到服务器成功时触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `connInfo`：连接信息。
- `reason`：连接状态改变的原因 (`Constants.CONNECTION_CHANGED_REASON_TYPE`)。

##### onConnectionLost

```java
public void onConnectionLost(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo)
```

当连接丢失时触发，表示 SDK 与服务器的连接已断开且无法自动恢复。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `connInfo`：连接信息。

##### onLastmileQuality

```java
public void onLastmileQuality(AgoraRtcConn agoraRtcConn, int quality)
```

报告网络最后一英里（客户端到边缘服务器）的质量。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `quality`：网络质量，参考 `Constants.QUALITY_TYPE`。

##### onLastmileProbeResult

```java
public void onLastmileProbeResult(AgoraRtcConn agoraRtcConn, LastmileProbeResult result)
```

报告最后一英里网络探测测试的结果。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `result`：探测结果 (`LastmileProbeResult`)。

##### onTokenPrivilegeWillExpire

```java
public void onTokenPrivilegeWillExpire(AgoraRtcConn agoraRtcConn, String token)
```

当 Token 即将过期时（通常在过期前 30 秒）触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `token`：即将过期的 Token。

##### onTokenPrivilegeDidExpire

```java
public void onTokenPrivilegeDidExpire(AgoraRtcConn agoraRtcConn)
```

当 Token 已经过期时触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。

##### onConnectionFailure

```java
public void onConnectionFailure(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason)
```

当连接失败时触发（区别于 `onDisconnected`，这通常指初始连接失败）。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `connInfo`：连接信息。
- `reason`：连接失败的原因 (`Constants.CONNECTION_CHANGED_REASON_TYPE`)。

##### onConnectionLicenseValidationFailure

```java
public void onConnectionLicenseValidationFailure(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason)
```

当 License 验证失败时触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `connInfo`：连接信息。
- `reason`：失败原因。

##### onUserJoined

```java
public void onUserJoined(AgoraRtcConn agoraRtcConn, String userId)
```

当远端用户加入频道时触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `userId`：加入频道的用户 ID。

##### onUserLeft

```java
public void onUserLeft(AgoraRtcConn agoraRtcConn, String userId, int reason)
```

当远端用户离开频道时触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `userId`：离开频道的用户 ID。
- `reason`：用户离开的原因 (`Constants.USER_OFFLINE_REASON_TYPE`)。

##### onTransportStats

```java
public void onTransportStats(AgoraRtcConn agoraRtcConn, RtcStats stats)
```

报告当前的通话统计信息，通常每 2 秒触发一次。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `stats`：通话统计数据 (`RtcStats`)。

##### onChangeRoleSuccess

```java
public void onChangeRoleSuccess(AgoraRtcConn agoraRtcConn, int oldRole, int newRole)
```

切换用户角色成功时触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `oldRole`：之前的角色。
- `newRole`：切换后的新角色。

##### onChangeRoleFailure

```java
public void onChangeRoleFailure(AgoraRtcConn agoraRtcConn)
```

切换用户角色失败时触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。

##### onUserNetworkQuality

```java
public void onUserNetworkQuality(AgoraRtcConn agoraRtcConn, String userId, int txQuality, int rxQuality)
```

报告指定用户的网络质量。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `userId`：用户 ID。如果是本地用户，则为 null。
- `txQuality`：该用户的上行网络质量 (`Constants.QUALITY_TYPE`)。
- `rxQuality`：该用户的下行网络质量 (`Constants.QUALITY_TYPE`)。

##### onNetworkTypeChanged

```java
public void onNetworkTypeChanged(AgoraRtcConn agoraRtcConn, int type)
```

当网络类型发生改变时触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `type`：新的网络类型 (`Constants.NETWORK_TYPE`)。

##### onApiCallExecuted

```java
public void onApiCallExecuted(AgoraRtcConn agoraRtcConn, int err, String api, String result)
```

当调用 SDK API 执行结束后触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `err`：错误码。
- `api`：被调用的 API 名称。
- `result`：API 执行结果的 JSON 字符串。

##### onContentInspectResult

```java
public void onContentInspectResult(AgoraRtcConn agoraRtcConn, int result)
```

内容审查结果回调。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `result`：审查结果 (`Constants.CONTENT_INSPECT_RESULT`)。

##### onSnapshotTaken

```java
public void onSnapshotTaken(AgoraRtcConn agoraRtcConn, String channel, int userId, String filePath, int width, int height, int errCode)
```

截图操作完成时触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `channel`：频道名。
- `userId`：用户 ID。
- `filePath`：截图文件保存路径。
- `width`：截图宽度。
- `height`：截图高度。
- `errCode`：错误码，0 表示成功。

##### onError

```java
public void onError(AgoraRtcConn agoraRtcConn, int error, String msg)
```

当 SDK 内部发生错误时触发。通常是不可恢复的错误。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `error`：错误码 (`Constants.ERROR_CODE_TYPE`)。
- `msg`：错误描述信息。

##### onWarning

```java
public void onWarning(AgoraRtcConn agoraRtcConn, int warning, String msg)
```

当 SDK 内部发生警告时触发。通常表示一些问题，但可能可以恢复。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `warning`：警告码 (`Constants.WARN_CODE_TYPE`)。
- `msg`：警告描述信息。

##### onChannelMediaRelayStateChanged

```java
public void onChannelMediaRelayStateChanged(AgoraRtcConn agoraRtcConn, int state, int code)
```

跨频道媒体流转发状态发生改变时触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `state`：当前的转发状态 (`Constants.CHANNEL_MEDIA_RELAY_STATE`)。
- `code`：相关的错误码或状态码 (`Constants.CHANNEL_MEDIA_RELAY_ERROR`)。

##### onLocalUserRegistered

```java
public void onLocalUserRegistered(AgoraRtcConn agoraRtcConn, int uid, String userAccount)
```

当本地用户成功注册 User Account 时触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `uid`：用户的 UID。
- `userAccount`：用户的 User Account。

##### onUserAccountUpdated

```java
public void onUserAccountUpdated(AgoraRtcConn agoraRtcConn, int uid, String userAccount)
```

当用户的 User Account 信息更新时触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `uid`：用户的 UID。
- `userAccount`：更新后的 User Account。

##### onStreamMessageError

```java
public void onStreamMessageError(AgoraRtcConn agoraRtcConn, String userId, int streamId, int code, int missed, int cached)
```

当发送数据流消息 (`sendStreamMessage`) 失败时触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `userId`：接收方的用户 ID。
- `streamId`：数据流 ID。
- `code`：错误码。
- `missed`：发送失败的消息数量。
- `cached`：当前缓存的消息数量。

##### onEncryptionError

```java
public void onEncryptionError(AgoraRtcConn agoraRtcConn, int errorType)
```

当发生加密错误时触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `errorType`：加密错误类型 (`Constants.ENCRYPTION_ERROR_TYPE`)。

##### onUploadLogResult

```java
public void onUploadLogResult(AgoraRtcConn agoraRtcConn, String requestId, int success, int reason)
```

当调用 `uploadLogFile` 方法上传日志文件的结果返回时触发。

**参数**：

- `agoraRtcConn`：RTC 连接实例。
- `requestId`：上传请求的 ID。
- `success`：是否上传成功 (`boolean`, 1 为成功, 0 为失败)。
- `reason`：上传结果的原因 (`Constants.UPLOAD_ERROR_REASON`)。

### ILocalUserObserver

ILocalUserObserver 接口用于监听本地用户的状态和事件，如媒体流发布、订阅、统计信息等。

```java
public interface ILocalUserObserver {
    // 方法声明
}
```

#### 方法

##### onAudioTrackPublishSuccess

```java
public void onAudioTrackPublishSuccess(AgoraLocalUser agoraLocalUser, AgoraLocalAudioTrack agoraLocalAudioTrack)
```

当本地音频轨道发布成功时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `agoraLocalAudioTrack`：成功发布的本地音频轨道。

##### onAudioTrackPublicationFailure

```java
public void onAudioTrackPublicationFailure(AgoraLocalUser agoraLocalUser, AgoraLocalAudioTrack agoraLocalAudioTrack, int error)
```

当本地音频轨道发布失败时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `agoraLocalAudioTrack`：发布失败的本地音频轨道。
- `error`：错误码 (`Constants.RTC_ERROR_CODE`)。

##### onLocalAudioTrackStateChanged

```java
public void onLocalAudioTrackStateChanged(AgoraLocalUser agoraLocalUser, AgoraLocalAudioTrack agoraLocalAudioTrack, int state, int error)
```

当本地音频轨道状态发生改变时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `agoraLocalAudioTrack`：状态改变的本地音频轨道。
- `state`：新的状态 (`Constants.LOCAL_AUDIO_STREAM_STATE`)。
- `error`：相关的错误码 (`Constants.LOCAL_AUDIO_STREAM_ERROR`)。

##### onLocalAudioTrackStatistics

```java
public void onLocalAudioTrackStatistics(AgoraLocalUser agoraLocalUser, LocalAudioStats stats)
```

报告本地音频流的统计信息。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `stats`：本地音频统计数据 (`LocalAudioStats`)。

##### onRemoteAudioTrackStatistics

```java
public void onRemoteAudioTrackStatistics(AgoraLocalUser agoraLocalUser, AgoraRemoteAudioTrack agoraRemoteAudioTrack, RemoteAudioTrackStats stats)
```

报告接收到的远端音频流的统计信息。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `agoraRemoteAudioTrack`：远端音频轨道。
- `stats`：远端音频统计数据 (`RemoteAudioTrackStats`)。

##### onUserAudioTrackSubscribed

```java
public void onUserAudioTrackSubscribed(AgoraLocalUser agoraLocalUser, String userId, AgoraRemoteAudioTrack agoraRemoteAudioTrack)
```

当成功订阅远端用户的音频轨道时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `userId`：远端用户 ID。
- `agoraRemoteAudioTrack`：订阅到的远端音频轨道。

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
public void onStreamMessage(AgoraLocalUser agoraLocalUser, String userId, int streamId, String data, long length)
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

INetworkObserver 接口用于监听网络状态的变化。

```java
public interface INetworkObserver {
    // 方法声明
}
```

#### 方法

##### onUplinkNetworkInfoUpdated

```java
public void onUplinkNetworkInfoUpdated(AgoraRtcConn agoraRtcConn, UplinkNetworkInfo info)
```

当上行网络信息更新时触发。

**参数**：

- agoraRtcConn：RTC 连接实例。
- info：上行网络信息。

##### onDownlinkNetworkInfoUpdated

```java
public void onDownlinkNetworkInfoUpdated(AgoraRtcConn agoraRtcConn, DownlinkNetworkInfo info)
```

当下行网络信息更新时触发。

**参数**：

- agoraRtcConn：RTC 连接实例。
- info：下行网络信息。

### IVideoFrameObserver

IVideoFrameObserver 接口用于监听视频帧的捕获、编码前处理和渲染等事件。通过实现此接口，可以获取原始视频数据进行处理。

```java
public interface IVideoFrameObserver {
    // 方法声明
}
```

#### 方法

##### onCaptureVideoFrame

```java
public int onCaptureVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame)
```

当捕获到本地摄像头视频帧时触发。

**参数**：

- `agora_video_frame_observer`：视频帧观察者实例 (通常在 Native 层使用)。
- `frame`：捕获的视频帧 (`VideoFrame`)。

**返回值**：

- SDK 预留参数，目前无实际意义，返回 0 即可。

##### onPreEncodeVideoFrame

```java
public int onPreEncodeVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame)
```

当获取到待编码的本地摄像头视频帧时触发。

**参数**：

- `agora_video_frame_observer`：视频帧观察者实例 (通常在 Native 层使用)。
- `frame`：待编码的视频帧 (`VideoFrame`)。

**返回值**：

- SDK 预留参数，目前无实际意义，返回 0 即可。

##### onSecondaryCameraCaptureVideoFrame

```java
public int onSecondaryCameraCaptureVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame)
```

当捕获到辅助摄像头的视频帧时触发。

**参数**：

- `agora_video_frame_observer`：视频帧观察者实例。
- `frame`：捕获的视频帧 (`VideoFrame`)。

**返回值**：

- SDK 预留参数，返回 0。

##### onSecondaryPreEncodeCameraVideoFrame

```java
public int onSecondaryPreEncodeCameraVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame)
```

当获取到待编码的辅助摄像头视频帧时触发。

**参数**：

- `agora_video_frame_observer`：视频帧观察者实例。
- `frame`：待编码的视频帧 (`VideoFrame`)。

**返回值**：

- SDK 预留参数，返回 0。

##### onScreenCaptureVideoFrame

```java
public int onScreenCaptureVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame)
```

当捕获到屏幕共享视频帧时触发。

**参数**：

- `agora_video_frame_observer`：视频帧观察者实例。
- `frame`：捕获的屏幕视频帧 (`VideoFrame`)。

**返回值**：

- SDK 预留参数，返回 0。

##### onPreEncodeScreenVideoFrame

```java
public int onPreEncodeScreenVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame)
```

当获取到待编码的屏幕共享视频帧时触发。

**参数**：

- `agora_video_frame_observer`：视频帧观察者实例。
- `frame`：待编码的屏幕视频帧 (`VideoFrame`)。

**返回值**：

- SDK 预留参数，返回 0。

##### onMediaPlayerVideoFrame

```java
public int onMediaPlayerVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame, int media_player_id)
```

当获取到媒体播放器的视频帧时触发。

**参数**：

- `agora_video_frame_observer`：视频帧观察者实例。
- `frame`：媒体播放器的视频帧 (`VideoFrame`)。
- `media_player_id`：媒体播放器的 ID。

**返回值**：

- SDK 预留参数，返回 0。

##### onSecondaryScreenCaptureVideoFrame

```java
public int onSecondaryScreenCaptureVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame)
```

当捕获到辅助屏幕共享的视频帧时触发。

**参数**：

- `agora_video_frame_observer`：视频帧观察者实例。
- `frame`：捕获的辅助屏幕视频帧 (`VideoFrame`)。

**返回值**：

- SDK 预留参数，返回 0。

##### onSecondaryPreEncodeScreenVideoFrame

```java
public int onSecondaryPreEncodeScreenVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame)
```

当获取到待编码的辅助屏幕共享视频帧时触发。

**参数**：

- `agora_video_frame_observer`：视频帧观察者实例。
- `frame`：待编码的辅助屏幕视频帧 (`VideoFrame`)。

**返回值**：

- SDK 预留参数，返回 0。

##### onRenderVideoFrame

```java
public int onRenderVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, String channel_id, int uid, VideoFrame frame)
```

当获取到准备渲染的远端用户视频帧时触发。

**参数**：

- `agora_video_frame_observer`：视频帧观察者实例 (通常在 Native 层使用)。
- `channel_id`：频道 ID。
- `uid`：远端用户 ID。
- `frame`：待渲染的视频帧 (`VideoFrame`)。

**返回值**：

- SDK 预留参数，目前无实际意义，返回 0 即可。

##### onTranscodedVideoFrame

```java
public int onTranscodedVideoFrame(AgoraVideoFrameObserver agora_video_frame_observer, VideoFrame frame)
```

当获取到转码后的视频帧时触发（例如，使用了媒体流转发或合流功能）。

**参数**：

- `agora_video_frame_observer`：视频帧观察者实例。
- `frame`：转码后的视频帧 (`VideoFrame`)。

**返回值**：

- SDK 预留参数，返回 0。

### IAudioFrameObserver

IAudioFrameObserver 接口用于监听音频帧的处理事件，如本地录制、远端播放、混音等。通过实现此接口，可以获取原始 PCM 音频数据进行处理。

**重要提示:**
为了提高数据传输效率，回调中 `AudioFrame` 对象的 `buffer` 属性是 `DirectByteBuffer`。
务必在回调方法内**同步**地提取所需的字节数组 (`byte[]`)，然后再将提取出的 `byte[]` 传递给异步线程进行后续处理。
可以直接使用 `io.agora.rtc.utils.Utils.getBytes(audioFrame.getBuffer())` 来获取 `byte[]`。

```java
public interface IAudioFrameObserver {
    // 方法声明
}
```

#### 方法

##### onRecordAudioFrame

```java
public int onRecordAudioFrame(AgoraLocalUser agoraLocalUser, String channelId, AudioFrame frame)
```

当获取到本地录制的音频帧时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `channelId`：频道 ID。
- `frame`：录制的音频帧 (`AudioFrame`)。

**返回值**：

- SDK 预留参数，返回 0 即可。

##### onPlaybackAudioFrame

```java
public int onPlaybackAudioFrame(AgoraLocalUser agoraLocalUser, String channelId, AudioFrame frame)
```

当获取到待播放的远端音频帧时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `channelId`：频道 ID。
- `frame`：待播放的音频帧 (`AudioFrame`)。

**返回值**：

- SDK 预留参数，返回 0 即可。

##### onMixedAudioFrame

```java
public int onMixedAudioFrame(AgoraLocalUser agoraLocalUser, String channelId, AudioFrame frame)
```

当获取到本地和所有远端用户混合后的音频帧时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `channelId`：频道 ID。
- `frame`：混合后的音频帧 (`AudioFrame`)。

**返回值**：

- SDK 预留参数，返回 0 即可。

##### onEarMonitoringAudioFrame

```java
public int onEarMonitoringAudioFrame(AgoraLocalUser agoraLocalUser, AudioFrame frame)
```

当获取到耳返音频帧时触发。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `frame`：耳返音频帧 (`AudioFrame`)。

**返回值**：

- SDK 预留参数，返回 0 即可。

##### onPlaybackAudioFrameBeforeMixing

```java
public int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agoraLocalUser, String channelId, String userId, AudioFrame frame, VadProcessResult vadResult)
```

当获取到指定远端用户混音前的音频帧时触发。如果同时启用了 VAD (语音活动检测)，还会返回 VAD 结果。

**参数**：

- `agoraLocalUser`：本地用户实例。
- `channelId`：频道 ID。
- `userId`：远端用户 ID。
- `frame`：混音前的音频帧 (`AudioFrame`)。
- `vadResult`：语音活动检测结果 (`VadProcessResult`)，如果未启用 VAD 则可能为 null。

**返回值**：

- SDK 预留参数，返回 0 即可。

##### getObservedAudioFramePosition

```java
public int getObservedAudioFramePosition()
```

设置想要获取的音频帧回调位置。例如，可以指定是在录制后、播放前还是混音后获取。
需要返回一个 `Constants.AUDIO_FRAME_POSITION` 常量的组合值（位或运算）。

**返回值**：

- 指定的音频帧位置掩码 (`int`)。

### IAudioEncodedFrameObserver

IAudioEncodedFrameObserver 接口用于监听接收到的远端音频编码帧数据。

**重要提示:**
为了提高数据传输效率，回调中 `ByteBuffer buffer` 参数是 `DirectByteBuffer`。
务必在回调方法内**同步**地提取所需的字节数组 (`byte[]`)，然后再将提取出的 `byte[]` 传递给异步线程进行后续处理。
可以直接使用 `io.agora.rtc.utils.Utils.getBytes(buffer)` 来获取 `byte[]`。

```java
public interface IAudioEncodedFrameObserver {
    // 方法声明
}
```

#### 方法

##### onEncodedAudioFrameReceived

```java
default int onEncodedAudioFrameReceived(String remoteUserId, ByteBuffer buffer, EncodedAudioFrameReceiverInfo info)
```

当接收到远端用户发送的编码后音频帧时触发。

**参数**：

- `remoteUserId`：发送该音频帧的远端用户 ID。
- `buffer`：包含编码后音频数据的 `ByteBuffer` (DirectByteBuffer)。
- `info`：编码音频帧的相关信息 (`EncodedAudioFrameReceiverInfo`)。

**返回值**：

- SDK 预留参数，保持默认实现返回 0 即可。

### IVideoEncodedFrameObserver

IVideoEncodedFrameObserver 接口用于监听接收到的远端视频编码帧数据。

**重要提示:**
为了提高数据传输效率，回调中 `ByteBuffer buffer` 参数是 `DirectByteBuffer`。
务必在回调方法内**同步**地提取所需的字节数组 (`byte[]`)，然后再将提取出的 `byte[]` 传递给异步线程进行后续处理。
可以直接使用 `io.agora.rtc.utils.Utils.getBytes(buffer)` 来获取 `byte[]`。

```java
public interface IVideoEncodedFrameObserver {
    // 方法声明
}
```

#### 方法

##### onEncodedVideoFrame

```java
public int onEncodedVideoFrame(AgoraVideoEncodedFrameObserver observer, int userId, ByteBuffer buffer, EncodedVideoFrameInfo info)
```

当接收到远端用户发送的编码后视频帧时触发。

**参数**：

- `observer`：视频编码帧观察者实例 (通常在 Native 层使用，Java 层可忽略)。
- `userId`：发送该视频帧的远端用户 ID。
- `buffer`：包含编码后视频数据的 `ByteBuffer` (DirectByteBuffer)。
- `info`：编码视频帧的相关信息 (`EncodedVideoFrameInfo`)。

**返回值**：

- SDK 预留参数，返回 0 即可。

## 数据结构

### AgoraServiceConfig

`AgoraServiceConfig` 类用于配置和初始化 Agora 服务实例。

#### 主要属性

- **enableAudioProcessor**：是否启用音频处理模块。默认为 1（启用）。
- **enableAudioDevice**：是否启用音频设备模块。默认为 0（禁用）。
- **enableVideo**：是否启用视频。默认为 0（禁用）。
- **context**：用户上下文对象。
- **appId**：项目的 App ID。
- **areaCode**：支持的区域代码，默认为 AREA_CODE_GLOB。
- **channelProfile**：频道配置文件，默认为 CHANNEL_PROFILE_LIVE_BROADCASTING。
- **audioScenario**：音频场景，默认为 AUDIO_SCENARIO_CHORUS。
- **useStringUid**：是否启用字符串用户 ID。默认为 0（禁用）。
- **logFilePath**：日志文件路径。
- **logFileSize**：日志文件大小，单位为 KB，默认为 2048KB。
- **logFilters**：日志级别，默认为 LOG_FILTER_INFO。
- **domainLimit**：是否启用域限制。默认为 0（无限制）。

### RtcConnConfig

`RtcConnConfig` 类用于配置 RTC 连接的各种参数。

#### 主要属性

- **autoSubscribeAudio**：是否自动订阅所有音频流。默认为 1（启用）。
- **autoSubscribeVideo**：是否自动订阅所有视频流。默认为 1（启用）。
- **enableAudioRecordingOrPlayout**：是否启用音频录制或播放。
- **maxSendBitrate**：最大发送比特率。
- **minPort**：最小端口。
- **maxPort**：最大端口。
- **audioSubsOptions**：音频订阅选项。
- **clientRoleType**：用户角色类型。默认为 CLIENT_ROLE_AUDIENCE。
- **channelProfile**：频道配置文件。
- **audioRecvMediaPacket**：是否接收音频媒体包。
- **audioRecvEncodedFrame**：是否接收音频编码帧。
- **videoRecvMediaPacket**：是否接收视频媒体包。

### RtcConnInfo

`RtcConnInfo` 类包含 RTC 连接的信息。

#### 主要属性

- **id**：连接的唯一标识符。
- **channelId**：频道标识符。
- **state**：连接的当前状态。
- **localUserId**：本地用户标识符。
- **internalUid**：内部用户标识符。

### VideoEncoderConfig

`VideoEncoderConfig` 类包含视频编码器的配置参数。

#### 主要属性

- **codecType**：视频编码器代码类型 (`Constants.VIDEO_CODEC_TYPE`)。
- **dimensions**：视频帧尺寸 (`VideoDimensions`)，指定视频质量，由帧宽度和高度的总像素数衡量。
- **frameRate**：视频帧率 (fps)。`int` 类型，但为兼容旧版本可接受 `Constants.FRAME_RATE` 枚举值。
- **bitrate**：视频编码目标比特率 (Kbps)。SDK 会根据网络情况动态调整，不建议设置得过高。
- **minBitrate**：**[未来使用]** 最小编码比特率 (Kbps)。SDK 会自动调整编码比特率以适应网络条件。除非有特殊图像质量要求，否则不建议更改此值。 **注意：** 此参数仅适用于直播场景 (`CHANNEL_PROFILE_LIVE_BROADCASTING`)。
- **orientationMode**：**[未来使用]** 视频方向模式 (`Constants.ORIENTATION_MODE`)。
- **degradationPreference**：带宽受限时的视频降级偏好 (`Constants.DEGRADATION_PREFERENCE`)。 **注意：** 目前仅支持 `MAINTAIN_QUALITY` (0)。
- **mirrorMode**：镜像模式 (`Constants.VIDEO_MIRROR_MODE_TYPE`)。如果设置为 `VIDEO_MIRROR_MODE_ENABLED`，视频帧将在编码前进行镜像处理。
- **encodeAlpha**：当源视频包含 Alpha 通道数据时，是否对其进行编码并发送。默认为 0 (禁用)。非零值表示启用。

### VideoSubscriptionOptions

`VideoSubscriptionOptions` 类定义了视频订阅的选项。

#### 主要属性

- **type**：视频订阅类型。
- **encodedFrameOnly**：标志，指示是否仅订阅编码帧。

### SimulcastStreamConfig

`SimulcastStreamConfig` 类配置同时直播流的参数。

#### 主要属性

- **dimensions**：视频尺寸。
- **bitrate**：视频比特率。
- **framerate**：视频帧率。

### EncodedVideoFrameInfo

`EncodedVideoFrameInfo` 类包含编码视频帧的相关信息。

#### 主要属性

- **codecType**：编解码器类型 (`Constants.VIDEO_CODEC_TYPE`)。
- **width**：视频帧宽度（像素）。
- **height**：视频帧高度（像素）。
- **framesPerSecond**：视频帧率 (fps)。如果此值为 0，则 SDK 使用原始视频帧的时间戳；否则，SDK 会根据此值调整时间戳。
- **frameType**：视频帧类型 (`Constants.VIDEO_FRAME_TYPE`)，如关键帧 (I 帧)、预测帧 (P 帧) 等。
- **rotation**：视频旋转角度 (`Constants.VIDEO_ORIENTATION`)。
- **trackId**：视频轨道的唯一标识符，用于支持多视频流场景。
- **captureTimeMs**：视频帧的捕获时间戳（毫秒）。这是一个输入参数。
- **decodeTimeMs**：视频帧的解码时间戳（毫秒）。(注意: Java 注释描述为渲染时间戳，此处根据字段名暂定为解码时间戳，建议确认)
- **uid**：发送该视频帧的用户 ID。
- **streamType**：视频流类型 (`Constants.VIDEO_STREAM_TYPE`)。

### EncodedAudioFrameInfo

`EncodedAudioFrameInfo` 类包含编码音频帧的相关信息。

#### 主要属性

- **speech**：指示该帧是否包含语音。
- **codec**：音频编解码器类型。
- **sendEvenIfEmpty**：是否发送空帧。
- **sampleRateHz**：音频采样率（Hz）。
- **samplesPerChannel**：每个声道的样本数。
- **numberOfChannels**：音频通道数。

### EncodedAudioFrameReceiverInfo

`EncodedAudioFrameReceiverInfo` 类包含编码音频帧接收器的相关信息。

#### 主要属性

- **sendTs**：数据包的发送时间。
- **codec**：数据包的编解码器。

### SenderOptions

`SenderOptions` 类包含发送器的选项配置。

#### 主要属性

- **ccMode**：拥塞控制模式。
- **codecType**：编解码器类型。
- **targetBitrate**：目标比特率（bps）。

### VideoFrame

`VideoFrame` 类表示一个视频帧，包含视频像素数据和相关信息。

#### 主要属性

- **type**: 视频帧类型。参考 `Constants.VIDEO_BUFFER_TYPE` 定义。
- **width**：视频帧宽度（像素）。
- **height**：视频帧高度（像素）。
- **yStride**：YUV 数据中 Y 平面的行跨度（像素）。
- **uStride**：YUV 数据中 U 平面的行跨度（像素）。
- **vStride**：YUV 数据中 V 平面的行跨度（像素）。
- **yBuffer**：Y 平面数据缓冲区 (`ByteBuffer`)。
- **uBuffer**：U 平面数据缓冲区 (`ByteBuffer`)。
- **vBuffer**：V 平面数据缓冲区 (`ByteBuffer`)。
- **rotation**：视频帧旋转角度（0, 90, 180, 270）。
- **renderTimeMs**：视频帧的渲染时间戳（毫秒）。此时间戳用于指导视频帧的同步渲染，**并非**捕获时间戳。
- **avsyncType**：音视频同步类型。
- **metadataBuffer**：**[纹理相关]** 元数据缓冲区 (`ByteBuffer`)。默认值为 `null`。
- **sharedContext**：**[纹理相关]** EGL 上下文对象 (`Object`)。
- **textureId**：**[纹理相关]** 视频帧使用的纹理 ID (`int`)。
- **matrix**：**[纹理相关]** 4x4 变换矩阵 (`float[]`)。
- **alphaBuffer**：**[人像分割]** Alpha 通道数据缓冲区 (`ByteBuffer`)。维度与视频帧相同，像素值范围 [0, 255]，0 表示完全背景，255 表示完全前景。默认值为 `null`。
- **alphaMode**：Alpha 通道缓冲区相对于视频帧的位置模式 (`int`)。
  - 0: (默认) 普通帧
  - 1: Alpha 缓冲区在帧上方
  - 2: Alpha 缓冲区在帧下方
  - 3: Alpha 缓冲区在帧左侧
  - 4: Alpha 缓冲区在帧右侧

### AudioFrame

`AudioFrame` 类表示一个音频帧。

#### 主要属性

- **type**：音频帧类型。
- **samplesPerChannel**：每声道的样本数。
- **bytesPerSample**：每个样本的字节数。
- **channels**：声道数。
- **samplesPerSec**：采样率 (Hz)。
- **buffer**：音频数据缓冲区 (ByteBuffer)。
- **renderTimeMs**：渲染时间戳（毫秒）。
- **avsyncType**：音视频同步类型。
- **farFiledFlag**：远场标志 (int)。
- **rms**：音频信号的均方根 (int)。
- **voiceProb**：人声存在的概率 (int)。
- **musicProb**：音乐存在的概率 (int)。
- **pitch**：音频信号的音高 (int)。

### ExternalVideoFrame

`ExternalVideoFrame` 类表示一个外部输入的视频帧，用于将外部视频源的数据推送到 SDK。

#### 主要属性

- **type**：外部视频帧缓冲区类型。参考 `Constants.VIDEO_BUFFER_TYPE` 定义。
- **format**：视频帧像素格式。参考 `Constants.VIDEO_PIXEL_FORMAT` 定义。
- **buffer**：视频帧数据缓冲区 (`ByteBuffer`)。**注意：必须是 DirectByteBuffer**。
- **stride**：视频帧的行跨度（单位：像素）。对于纹理，这是纹理的宽度。
- **height**：视频帧高度（像素）。
- **cropLeft**：**[原始数据相关]** 左边裁剪掉的像素数。默认值为 0。
- **cropTop**：**[原始数据相关]** 顶部裁剪掉的像素数。默认值为 0。
- **cropRight**：**[原始数据相关]** 右边裁剪掉的像素数。默认值为 0。
- **cropBottom**：**[原始数据相关]** 底部裁剪掉的像素数。默认值为 0。
- **rotation**：**[原始数据相关]** 视频帧顺时针旋转角度（0, 90, 180, 270）。默认值为 0。
- **timestamp**：输入视频帧的时间戳（毫秒）。错误的时间戳会导致丢帧或音画不同步。
- **eglContext**：**[纹理相关]** EGL 上下文对象 (`Object`)。
  - 使用 Khronos 定义的 OpenGL 接口 (`javax.microedition.khronos.egl.*`) 时，设置为对应的 `EGLContext`。
  - 使用 Android 定义的 OpenGL 接口 (`android.opengl.*`) 时，设置为对应的 `EGLContext`。
- **eglType**：**[纹理相关]** EGL 类型。参考 `Constants.EGL_CONTEXT_TYPE`。
- **textureId**：**[纹理相关]** 视频帧使用的纹理 ID (`int`)。
- **matrix**：**[纹理相关]** 输入的 4x4 变换矩阵。典型值为单位矩阵。
  **注意:** Java 类中此字段定义为 `float` 类型，与注释描述（4x4 矩阵）不符，请以实际 SDK 行为为准或查阅最新文档。
- **metadataBuffer**：**[纹理相关]** 元数据缓冲区 (`ByteBuffer`)。默认值为 `null`。
- **alphaBuffer**：Alpha 通道数据缓冲区 (`ByteBuffer`)。维度与视频帧相同，像素值范围 [0, 255]，0 表示完全背景，255 表示完全前景。默认值为 `null`。
- **fillAlphaBuffer**：**[仅 BGRA/RGBA]** 是否从 BGRA/RGBA 数据中提取 Alpha 通道数据。如果未显式提供 `alphaBuffer`，可以将此项设为 1 (true)。默认为 0 (false)。
- **alphaMode**：Alpha 通道缓冲区相对于视频帧的位置模式 (`int`)。
  - 0: (默认) 普通帧
  - 1: Alpha 缓冲区在帧上方
  - 2: Alpha 缓冲区在帧下方
  - 3: Alpha 缓冲区在帧左侧
  - 4: Alpha 缓冲区在帧右侧
- **colorSpace**: 色彩空间信息 (`ColorSpace`)。

### VideoDimensions

`VideoDimensions` 类表示视频的尺寸。

#### 主要属性

- **width**：视频宽度。
- **height**：视频高度。

### RtcStats

`RtcStats` 类提供 RTC 统计信息。

#### 主要属性

- **connectionId**: 连接 ID (int)。
- **duration**：通话持续时间（秒）(int)。
- **txBytes**：发送的总字节数 (int)。
- **rxBytes**：接收的总字节数 (int)。
- **txAudioBytes**：发送的音频字节数 (int)。
- **txVideoBytes**：发送的视频字节数 (int)。
- **rxAudioBytes**：接收的音频字节数 (int)。
- **rxVideoBytes**：接收的视频字节数 (int)。
- **txKBitRate**：发送速率（kbps）(short)。
- **rxKBitRate**：接收速率（kbps）(short)。
- **txAudioKBitRate**：音频发送速率（kbps）(short)。
- **rxAudioKBitRate**：音频接收速率（kbps）(short)。
- **txVideoKBitRate**：视频发送速率（kbps）(short)。
- **rxVideoKBitRate**：视频接收速率（kbps）(short)。
- **userCount**：通话中的用户数 (int)。
- **lastmileDelay**：客户端到服务器的延迟（毫秒）(short)。
- **txPacketLossRate**：发送丢包率（%）(int)。
- **rxPacketLossRate**：接收丢包率（%）(int)。
- **cpuTotalUsage**：系统 CPU 使用率（%）(double)。
- **cpuAppUsage**：应用 CPU 使用率（%）(double)。
- **gatewayRtt**：网关 RTT（往返时间，毫秒）(int)。
- **memoryAppUsageRatio**：应用内存使用率（%）(double)。
- **memoryTotalUsageRatio**：系统内存使用率（%）(double)。
- **memoryAppUsageInKbytes**：应用内存使用量（KB）(int)。
- **connectTimeMs**: 连接耗时（毫秒）(int)。
- **firstAudioPacketDuration**: 接收到首个音频包的耗时（毫秒）(int)。
- **firstVideoPacketDuration**: 接收到首个视频包的耗时（毫秒）(int)。
- **firstVideoKeyFramePacketDuration**: 接收到首个视频关键帧的耗时（毫秒）(int)。
- **packetsBeforeFirstKeyFramePacket**: 接收到首个关键帧前丢弃的视频包数量 (int)。
- **firstAudioPacketDurationAfterUnmute**: 取消静音后接收到首个音频包的耗时（毫秒）(int)。
- **firstVideoPacketDurationAfterUnmute**: 取消订阅后接收到首个视频包的耗时（毫秒）(int)。
- **firstVideoKeyFramePacketDurationAfterUnmute**: 取消订阅后接收到首个视频关键帧的耗时（毫秒）(int)。
- **firstVideoKeyFrameDecodedDurationAfterUnmute**: 取消订阅后解码出首个视频关键帧的耗时（毫秒）(int)。
- **firstVideoKeyFrameRenderedDurationAfterUnmute**: 取消订阅后渲染出首个视频关键帧的耗时（毫秒）(int)。

### UserInfo

`UserInfo` 类提供用户的信息。

#### 主要属性

- **userId**：用户标识符 (String)。
- **hasAudio**：用户是否发送音频流 (int, 1 表示是, 0 表示否)。
- **hasVideo**：用户是否发送视频流 (int, 1 表示是, 0 表示否)。

### VadProcessResult

`VadProcessResult` 类提供语音活动检测(VAD)的处理结果。

#### 主要属性

- **outFrame**：处理后的音频帧数据 (byte[])。
- **state**：VAD 状态 (Constants.VadState)。

### AgoraAudioVadConfigV2

`AgoraAudioVadConfigV2` 类配置音频 VAD 参数。

#### 主要属性

- **preStartRecognizeCount**：预启动识别次数 (int)。
- **startRecognizeCount**：开始识别次数 (int)。
- **stopRecognizeCount**：停止识别次数 (int)。
- **activePercent**：活跃百分比 (float)。
- **inactivePercent**：非活跃百分比 (float)。
- **startVoiceProb**：开始语音概率 (int)。
- **stopVoiceProb**：停止语音概率 (int)。
- **startRmsThreshold**：开始 RMS 阈值 (int)。
- **stopRmsThreshold**：停止 RMS 阈值 (int)。

### LocalAudioTrackStats

`LocalAudioTrackStats` 类提供本地音频轨道的统计信息。

#### 主要属性

- **sourceId**：源 ID (int)。
- **bufferedPcmDataListSize**：缓冲的 PCM 数据列表大小 (int)。
- **missedAudioFrames**：丢失的音频帧数 (int)。
- **sentAudioFrames**：发送的音频帧数 (int)。
- **pushedAudioFrames**：推送的音频帧数 (int)。
- **droppedAudioFrames**：丢弃的音频帧数 (int)。
- **enabled**：是否启用 (int)。

### LocalVideoTrackStats

`LocalVideoTrackStats` 类提供本地视频轨道的统计信息。

#### 主要属性

- **numberOfStreams**：流数量 (long)。
- **bytesMajorStream**：主流字节数 (long)。
- **bytesMinorStream**：次流字节数 (long)。
- **framesEncoded**：编码帧数 (int)。
- **ssrcMajorStream**：主流 SSRC (int)。
- **ssrcMinorStream**：次流 SSRC (int)。
- **captureFrameRate**：捕获帧率 (int)。
- **regulatedCaptureFrameRate**：调节后的捕获帧率 (int)。
- **inputFrameRate**：输入帧率 (int)。
- **encodeFrameRate**：编码帧率 (int)。
- **renderFrameRate**：渲染帧率 (int)。
- **targetMediaBitrateBps**：目标媒体比特率（bps）(int)。
- **mediaBitrateBps**：媒体比特率（bps）(int)。
- **totalBitrateBps**：总比特率（bps）(int)。
- **captureWidth**：捕获宽度 (int)。
- **captureHeight**：捕获高度 (int)。
- **regulatedCaptureWidth**：调节后的捕获宽度 (int)。
- **regulatedCaptureHeight**：调节后的捕获高度 (int)。
- **width**：宽度 (int)。
- **height**：高度 (int)。
- **encoderType**：编码器类型 (int)。
- **uplinkCostTimeMs**：上行耗时（毫秒）(int)。
- **qualityAdaptIndication**：质量适应指示 (int)。

### RemoteAudioTrackStats

`RemoteAudioTrackStats` 类提供远程音频轨道的统计信息。

#### 主要属性

- **uid**：用户 ID (int)。
- **quality**：音频质量 (int)。
- **networkTransportDelay**：网络传输延迟 (int)。
- **jitterBufferDelay**：抖动缓冲延迟 (int)。
- **audioLossRate**：音频丢包率 (int)。
- **numChannels**：声道数 (int)。
- **receivedSampleRate**：接收采样率 (int)。
- **receivedBitrate**：接收比特率 (int)。
- **totalFrozenTime**：总冻结时间 (int)。
- **frozenRate**：冻结率 (int)。
- **receivedBytes**：接收字节数 (long)。

### RemoteVideoTrackStats

`RemoteVideoTrackStats` 类提供远程视频轨道的统计信息。

#### 主要属性

- **uid**：用户 ID (int)。
- **delay**：延迟 (int)。**[已弃用]**
- **width**：宽度 (int)。
- **height**：高度 (int)。
- **receivedBitrate**：接收比特率 (int)。
- **decoderOutputFrameRate**：解码器输出帧率 (int)。
- **rendererOutputFrameRate**：渲染器输出帧率 (int)。
- **frameLossRate**：帧丢失率 (int)。
- **packetLossRate**：包丢失率 (int)。
- **rxStreamType**：接收的视频流类型 (int)。
- **totalFrozenTime**：总冻结时间 (int)。
- **frozenRate**：冻结率 (int)。
- **totalDecodedFrames**：解码的总视频帧数 (int)。
- **avSyncTimeMs**：音视频同步偏移（毫秒）(int)。正值表示音频领先视频，负值表示音频落后视频。
- **downlinkProcessTimeMs**：下行处理耗时（毫秒）(int)。从收到组成帧的第一个包到帧准备好渲染的平均偏移时间。
- **frameRenderDelayMs**：帧渲染延迟（毫秒）(int)。渲染器的平均耗时。
- **totalActiveTime**：总活跃时间 (long)。远端用户加入频道后，既未停止发送视频流也未禁用视频模块的总时间（毫秒）。
- **publishDuration**：发布持续时间 (long)。远端视频流的总发布时长（毫秒）。

### EncryptionConfig

`EncryptionConfig` 类配置加密参数。

#### 主要属性

- **encryptionMode**：加密模式 (int)。
- **encryptionKey**：加密密钥 (String)。
- **encryptionKdfSalt**：加密 KDF 盐 (byte[])。

### UplinkNetworkInfo

`UplinkNetworkInfo` 类提供上行网络信息。

#### 主要属性

- **videoEncoderTargetBitrateBps**：视频编码器目标比特率（bps）(int)。

### DownlinkNetworkInfo

`DownlinkNetworkInfo` 类提供下行网络信息。

#### 主要属性

- **lastmileBufferDelayTimeMs**：最后一英里缓冲延迟时间（毫秒）(int)。
- **bandwidthEstimationBps**：带宽估计（bps）(int)。
- **totalDownscaleLevelCount**：总下行级别计数 (int)。
- **peerDownlinkInfo**: 对端下行信息 (`PeerDownlinkInfo`)。
- **totalReceivedVideoCount**: 接收到的视频流总数 (int)。

### PeerDownlinkInfo

`PeerDownlinkInfo` 类提供远程用户下行信息。

#### 主要属性

- **userId**：用户 ID (String)。
- **streamType**：流类型 (int)。
- **currentDownscaleLevel**：当前下采样级别 (int)。
- **expectedBitrateBps**：预期比特率（bps）(int)。

### 工具类

#### AudioConsumerUtils

`AudioConsumerUtils` 类用于消费 PCM 音频数据并将其推送到 RTC 频道。主要用于 AI 场景，如处理 TTS 返回的数据。

**使用模式:**

1.  为每个产生 PCM 数据的用户创建一个 `AudioConsumerUtils` 对象。
2.  当 PCM 数据产生时 (例如 TTS 返回)，调用 `pushPcmData(byte[] data)` 将数据推入内部缓冲区。
3.  通过定时器（推荐间隔 40-80ms）调用 `consume()` 方法，该方法会根据内部状态和时间戳自动将缓冲区中的数据发送到 RTC 频道。
4.  需要中断（例如停止当前 AI 对话）时，调用 `clear()` 方法清空缓冲区。
5.  退出时，调用 `release()` 方法释放资源。

#### 构造方法

```java
public AudioConsumerUtils(AgoraAudioPcmDataSender audioFrameSender, int numOfChannels, int sampleRate)
```

创建 `AudioConsumerUtils` 实例。

**参数**：

- `audioFrameSender`: 用于发送 PCM 数据的 `AgoraAudioPcmDataSender` 实例。
- `numOfChannels`: 音频通道数。
- `sampleRate`: 音频采样率 (Hz)。

#### 方法

##### pushPcmData

```java
public synchronized void pushPcmData(byte[] data)
```

将 PCM 数据推入内部缓冲区。如果数据长度不是内部帧大小的整数倍，会自动补零。

**参数**：

- `data`: PCM 音频数据 (byte[])。

##### consume

```java
public synchronized int consume()
```

从缓冲区消费数据并发送到 RTC 频道。此方法应由定时器定期调用。

**返回值**：

- `>= 0`: 成功消费并发送的音频帧数量。
- `< 0`: 错误码，例如 `Constants.AUDIO_CONSUMER_INVALID_PARAM`, `Constants.AUDIO_CONSUMER_NOT_READY`, `Constants.AUDIO_CONSUMER_PENDING`, `Constants.AUDIO_CONSUMER_FAILED`。

##### getRemainingCacheDurationInMs

```java
public synchronized int getRemainingCacheDurationInMs()
```

获取内部缓冲区中剩余数据的可播放时长（毫秒）。

**返回值**：

- 剩余缓存时长（毫秒）。

##### clear

```java
public synchronized void clear()
```

清空内部缓冲区和状态。

##### release

```java
public synchronized void release()
```

释放资源，包括清空缓冲区和置空内部引用。

#### VadDumpUtils

`VadDumpUtils` 类用于将 VAD (语音活动检测) 处理过程中的音频数据和标签信息转储到文件，方便调试和分析。

##### 构造方法

```java
public VadDumpUtils(String path)
```

创建 `VadDumpUtils` 实例。会在指定的 `path` 目录下创建一个以当前时间戳（`yyyyMMddHHmmss`）命名的子目录来存放转储文件。

**参数**：

- `path`: 转储文件的根目录路径。

##### write

```java
public synchronized void write(AudioFrame frame, byte[] vadResultBytes, Constants.VadState vadResultState)
```

将一帧音频数据及其 VAD 处理结果写入文件。内部使用单独线程进行文件写入。

会生成以下文件：

- `source.pcm`: 包含所有传入的原始 PCM 音频数据。
- `label.txt`: 包含每一帧的标签信息，如帧计数、VAD 状态、远场标志、语音概率、RMS、音高、音乐概率等。
- `vad_X.pcm` (X 为序号): 当 VAD 状态从未说话变为说话 (`START_SPEAKING`) 时创建，记录该段语音的 PCM 数据，直到状态变为 `STOP_SPEAKING` 时关闭。

**参数**：

- `frame`: 包含原始 PCM 数据的 `AudioFrame`。
- `vadResultBytes`: VAD 处理后的音频数据 (byte[])。
- `vadResultState`: 当前帧的 VAD 状态 (`Constants.VadState`)。

##### release

```java
public void release()
```

关闭所有已打开的文件句柄，释放资源，并重置内部计数器。
