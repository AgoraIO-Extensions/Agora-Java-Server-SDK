# 声网服务器 SDK Java API 参考文档

本文档为声网服务器 SDK Java API 的主要类和方法提供参考。

## 目录

- [核心类](#core-classes)
  - [AgoraService](#agoraservice)
  - [AgoraRtcConn](#agorartcconn)
  - [AgoraLocalUser](#agoralocaluser)
  - [AgoraParameter](#agoraparameter)
  - [AgoraAudioProcessor](#agoraaudioprocessor)
  - [AgoraAudioVad](#agoraaudiovad)
  - [AgoraAudioVadV2](#agoraaudiovadev2)
- [观察者接口](#observer-interfaces)
  - [IRtcConnObserver](#irtcconnobserver)
  - [ILocalUserObserver](#ilocaluserobserver)
  - [INetworkObserver](#inetworkobserver)
  - [IVideoFrameObserver2](#ivideoframeobserver2)
  - [IAudioFrameObserver](#iaudioframeobserver)
  - [IAudioEncodedFrameObserver](#iaudioencodedframeobserver)
  - [IVideoEncodedFrameObserver](#ivideoencodedframeobserver)
  - [IAgoraAudioProcessorEventHandler](#iagoraaudioprocessoreventhandler)
- [数据结构](#data-structures)
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
- [工具类](#utility-classes)
  - [VadDumpUtils](#vaddumputils)

---

## 核心类

### AgoraService
`AgoraService` 类是声网服务器 SDK 的入口。它提供了用于初始化和管理 SDK 的主要方法。

#### 方法

##### `getSdkVersion()`
获取 SDK 版本。
- **返回:** `String` - SDK 版本。

##### `initialize(AgoraServiceConfig config)`
使用指定的配置初始化声网服务。
- **参数:**
  - `config`: `AgoraServiceConfig` - 声网服务的配置。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

##### `destroy()`
销毁声网服务并释放所有资源。

##### `setLogFile(String filePath, int fileSize)`
设置日志文件路径和大小。
- **参数:**
  - `filePath`: `String` - 日志文件的路径。
  - `fileSize`: `int` - 日志文件的最大大小（字节）。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

##### `setLogFilter(int filters)`
设置日志过滤器级别。
- **参数:**
  - `filters`: `int` - 日志过滤器级别。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

##### `createRtcConn(RtcConnConfig rtcConnConfig)`
创建一个 `AgoraRtcConn` 对象。
- **参数:**
  - `rtcConnConfig`: `RtcConnConfig` - RTC 连接的配置。
- **返回:** `AgoraRtcConn` - 创建的 `AgoraRtcConn` 对象。

##### `getAgoraParameter()`
获取用于设置 SDK 参数的 `AgoraParameter` 对象。
- **返回:** `AgoraParameter` - `AgoraParameter` 对象。

### AgoraRtcConn
`AgoraRtcConn` 类代表一个 RTC 连接。

#### 方法

##### `destroy()`
销毁 `AgoraRtcConn` 实例并释放所有关联资源。

##### `registerObserver(IRtcConnObserver observer)`
注册一个 `IRtcConnObserver` 以接收连接相关的事件。
- **参数:**
  - `observer`: `IRtcConnObserver` - 要注册的观察者。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

##### `registerNetworkObserver(INetworkObserver observer)`
注册一个 `INetworkObserver` 以接收网络相关的事件。
- **参数:**
  - `observer`: `INetworkObserver` - 要注册的观察者。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

##### `registerAudioFrameObserver(IAudioFrameObserver observer, boolean enableVad, AgoraAudioVadConfigV2 vadConfig)`
注册一个 `IAudioFrameObserver` 以接收原始音频数据。
- **参数:**
  - `observer`: `IAudioFrameObserver` - 要注册的观察者。
  - `enableVad`: `boolean` - 是否启用语音活动检测 (VAD)。
  - `vadConfig`: `AgoraAudioVadConfigV2` - VAD 的配置。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

##### `registerAudioEncodedFrameObserver(IAudioEncodedFrameObserver observer)`
注册一个 `IAudioEncodedFrameObserver` 以接收编码后的音频帧。
- **参数:**
  - `observer`: `IAudioEncodedFrameObserver` - 要注册的观察者。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

##### `registerLocalUserObserver(ILocalUserObserver observer)`
注册一个 `ILocalUserObserver` 以接收本地用户的事件。
- **参数:**
  - `observer`: `ILocalUserObserver` - 要注册的观察者。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

##### `registerVideoFrameObserver(AgoraVideoFrameObserver2 agoraVideoFrameObserver2)`
注册一个 `IVideoFrameObserver2` 以接收原始视频帧。
- **参数:**
  - `agoraVideoFrameObserver2`: `AgoraVideoFrameObserver2` - 要注册的观察者。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

##### `registerVideoEncodedFrameObserver(AgoraVideoEncodedFrameObserver agoraVideoEncodedFrameObserver)`
注册一个 `IVideoEncodedFrameObserver` 以接收编码后的视频帧。
- **参数:**
  - `agoraVideoEncodedFrameObserver`: `AgoraVideoEncodedFrameObserver` - 要注册的观察者。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

##### `connect(String token, String channelId, String userId)`
连接到频道。
- **参数:**
  - `token`: `String` - 用于认证的令牌。
  - `channelId`: `String` - 频道 ID。
  - `userId`: `String` - 用户 ID。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

##### `disconnect()`
断开与频道的连接。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

##### `getLocalUser()`
获取与此连接关联的 `AgoraLocalUser` 对象。
- **返回:** `AgoraLocalUser` - 本地用户对象。

### AgoraLocalUser
`AgoraLocalUser` 类提供了管理本地用户音频和视频的方法。

#### 方法

##### `publishAudio(AgoraLocalAudioTrack agoraLocalAudioTrack)`
发布本地音频轨道。
- **参数:**
  - `agoraLocalAudioTrack`: `AgoraLocalAudioTrack` - 要发布的本地音频轨道。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

##### `unpublishAudio(AgoraLocalAudioTrack agoraLocalAudioTrack)`
取消发布本地音频轨道。
- **参数:**
  - `agoraLocalAudioTrack`: `AgoraLocalAudioTrack` - 要取消发布的本地音频轨道。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

##### `publishVideo(AgoraLocalVideoTrack agoraLocalVideoTrack)`
发布本地视频轨道。
- **参数:**
  - `agoraLocalVideoTrack`: `AgoraLocalVideoTrack` - 要发布的本地视频轨道。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

##### `unpublishVideo(AgoraLocalVideoTrack agoraLocalVideoTrack)`
取消发布本地视频轨道。
- **参数:**
  - `agoraLocalVideoTrack`: `AgoraLocalVideoTrack` - 要取消发布的本地视频轨道。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

##### `subscribeAudio(String userId)`
订阅远程用户的音频轨道。
- **参数:**
  - `userId`: `String` - 远程用户的 ID。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

##### `unsubscribeAudio(String userId)`
取消订阅远程用户的音频轨道。
- **参数:**
  - `userId`: `String` - 远程用户的 ID。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

##### `subscribeVideo(String userId, VideoSubscriptionOptions options)`
订阅远程用户的视频轨道。
- **参数:**
  - `userId`: `String` - 远程用户的 ID。
  - `options`: `VideoSubscriptionOptions` - 订阅选项。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

##### `unsubscribeVideo(String userId)`
取消订阅远程用户的视频轨道。
- **参数:**
  - `userId`: `String` - 远程用户的 ID。
- **返回:** `int` - 0 表示成功, < 0 表示失败。

### AgoraParameter
`AgoraParameter` 类提供了设置和获取 SDK 参数的方法。

#### 方法

##### `destroy()`
销毁 AgoraParameter 对象。

##### `setInt(String key, int value)`
设置一个整型参数。
- **参数:**
  - `key`: `String` - 参数的键。
  - `value`: `int` - 要设置的整数值。
- **返回:** `int` - 0 表示成功, <0 表示失败。

##### `setBool(String key, boolean value)`
设置一个布尔型参数。
- **参数:**
  - `key`: `String` - 参数的键。
  - `value`: `boolean` - 要设置的布尔值。
- **返回:** `int` - 0 表示成功, <0 表示失败。

##### `setUint(String key, int value)`
设置一个无符号整型参数。
- **参数:**
  - `key`: `String` - 参数的键。
  - `value`: `int` - 要设置的无符号整数值。
- **返回:** `int` - 0 表示成功, <0 表示失败。

##### `setNumber(String key, double value)`
设置一个数字参数。
- **参数:**
  - `key`: `String` - 参数的键。
  - `value`: `double` - 要设置的数值。
- **返回:** `int` - 0 表示成功, <0 表示失败。

##### `setString(String key, String value)`
设置一个字符串参数。
- **参数:**
  - `key`: `String` - 参数的键。
  - `value`: `String` - 要设置的字符串值。
- **返回:** `int` - 0 表示成功, <0 表示失败。

##### `setArray(String key, String json_src)`
使用 JSON 字符串设置一个数组参数。
- **参数:**
  - `key`: `String` - 参数的键。
  - `json_src`: `String` - 代表数组的 JSON 字符串。
- **返回:** `int` - 0 表示成功, <0 表示失败。

##### `setParameters(String json_src)`
使用 JSON 字符串设置多个参数。
- **参数:**
  - `json_src`: `String` - 包含多个参数的 JSON 字符串。
- **返回:** `int` - 0 表示成功, <0 表示失败。

##### `getInt(String key, Out value)`
获取一个整型参数。
- **参数:**
  - `key`: `String` - 参数的键。
  - `value`: `Out` - 一个用于存储整数值的 Out 对象。
- **返回:** `int` - 0 表示成功, <0 表示失败。

##### `getBool(String key, Out value)`
获取一个布尔型参数。
- **参数:**
  - `key`: `String` - 参数的键。
  - `value`: `Out` - 一个用于存储布尔值的 Out 对象。
- **返回:** `int` - 0 表示成功, <0 表示失败。

##### `getUint(String key, Out value)`
获取一个无符号整型参数。
- **参数:**
  - `key`: `String` - 参数的键。
  - `value`: `Out` - 一个用于存储无符号整数值的 Out 对象。
- **返回:** `int` - 0 表示成功, <0 表示失败。

##### `getNumber(String key, Out value)`
获取一个数字参数。
- **参数:**
  - `key`: `String` - 参数的键。
  - `value`: `Out` - 一个用于存储数值的 Out 对象。
- **返回:** `int` - 0 表示成功, <0 表示失败。

##### `getString(String key, Out value)`
获取一个字符串参数。
- **参数:**
  - `key`: `String` - 参数的键。
  - `value`: `Out` - 一个用于存储字符串值的 Out 对象。
- **返回:** `int` - 0 表示成功, <0 表示失败。

### AgoraAudioProcessor
`AgoraAudioProcessor` 类用于处理音频帧。

#### 方法

##### `getSdkVersion()`
获取 SDK 版本。
- **返回:** `String` - SDK 版本。

##### `init(String appId, String license, IAgoraAudioProcessorEventHandler eventHandler, AgoraAudioProcessorConfig config)`
初始化音频处理器。
- **参数:**
  - `appId`: `String` - App ID。
  - `license`: `String` - 音频处理器的许可证。
  - `eventHandler`: `IAgoraAudioProcessorEventHandler` - 事件处理器。
  - `config`: `AgoraAudioProcessorConfig` - 音频处理器的配置。
- **返回:** `int` - 0 表示成功，或一个错误码。

##### `process(AgoraAudioFrame nearIn)`
处理一个音频帧。
- **参数:**
  - `nearIn`: `AgoraAudioFrame` - 输入的音频帧。
- **返回:** `AgoraAudioFrame` - 处理后的音频帧。

##### `process(AgoraAudioFrame nearIn, AgoraAudioFrame farIn)`
处理来自近端和远端的音频帧。
- **参数:**
  - `nearIn`: `AgoraAudioFrame` - 近端输入的音频帧。
  - `farIn`: `AgoraAudioFrame` - 远端输入的音频帧。
- **返回:** `AgoraAudioFrame` - 处理后的音频帧。

##### `release()`
释放音频处理器使用的资源。
- **返回:** `int` - 0 表示成功，或一个错误码。

### AgoraAudioVad
`AgoraAudioVad` 类提供语音活动检测 (VAD) 功能。它由 `AgoraAudioVadManager` 管理。

#### 方法

##### `getVadInstance(String channelId, String userId)`
获取指定频道和用户的 VAD 实例。
- **参数:**
  - `channelId`: `String` - 频道 ID。
  - `userId`: `String` - 用户 ID。
- **返回:** `AgoraAudioVadV2` - VAD 实例，如果未找到则为 null。

##### `delVadInstance(String channelId, String userId)`
移除并销毁指定的 VAD 实例。
- **参数:**
  - `channelId`: `String` - 频道 ID。
  - `userId`: `String` - 用户 ID。

##### `process(String channelId, String userId, AudioFrame frame)`
处理一个音频帧以进行 VAD。
- **参数:**
  - `channelId`: `String` - 频道 ID。
  - `userId`: `String` - 用户 ID。
  - `frame`: `AudioFrame` - 要处理的音频帧。
- **返回:** `VadProcessResult` - VAD 处理的结果。

##### `destroy()`
销毁 VAD 管理器并清理所有 VAD 实例。

---

## 观察者接口

### IRtcConnObserver
RTC 连接事件的观察者接口。

#### 方法

##### `onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason)`
当 SDK 连接到声网频道时发生。
- **参数:**
  - `agoraRtcConn`: `AgoraRtcConn` - 连接对象。
  - `connInfo`: `RtcConnInfo` - 连接信息。
  - `reason`: `int` - 状态改变的原因。

##### `onDisconnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason)`
当 SDK 从声网频道断开时发生。
- **参数:**
  - `agoraRtcConn`: `AgoraRtcConn` - 连接对象。
  - `connInfo`: `RtcConnInfo` - 连接信息。
  - `reason`: `int` - 状态改变的原因。

##### `onUserJoined(AgoraRtcConn agoraRtcConn, String userId)`
当远程用户加入频道时发生。
- **参数:**
  - `agoraRtcConn`: `AgoraRtcConn` - 连接对象。
  - `userId`: `String` - 远程用户的 ID。

##### `onUserLeft(AgoraRtcConn agoraRtcConn, String userId, int reason)`
当远程用户离开频道时发生。
- **参数:**
  - `agoraRtcConn`: `AgoraRtcConn` - 连接对象。
  - `userId`: `String` - 离开的用户的 ID。
  - `reason`: `int` - 用户离开的原因。

### ILocalUserObserver
本地用户事件的观察者接口。

#### 方法

##### `onAudioTrackPublishSuccess(AgoraLocalUser agoraLocalUser, AgoraLocalAudioTrack agoraLocalAudioTrack)`
当本地音频轨道成功发布时发生。
- **参数:**
  - `agoraLocalUser`: `AgoraLocalUser` - 本地用户对象。
  - `agoraLocalAudioTrack`: `AgoraLocalAudioTrack` - 发布的音频轨道。

##### `onVideoTrackPublishSuccess(AgoraLocalUser agoraLocalUser, AgoraLocalVideoTrack agoraLocalVideoTrack)`
当本地视频轨道成功发布时发生。
- **参数:**
  - `agoraLocalUser`: `AgoraLocalUser` - 本地用户对象。
  - `agoraLocalVideoTrack`: `AgoraLocalVideoTrack` - 发布的视频轨道。

##### `onUserAudioTrackSubscribed(AgoraLocalUser agoraLocalUser, String userId, AgoraRemoteAudioTrack agoraRemoteAudioTrack)`
当订阅了远程用户的音频轨道时发生。
- **参数:**
  - `agoraLocalUser`: `AgoraLocalUser` - 本地用户对象。
  - `userId`: `String` - 远程用户的 ID。
  - `agoraRemoteAudioTrack`: `AgoraRemoteAudioTrack` - 远程音频轨道。

##### `onUserVideoTrackSubscribed(AgoraLocalUser agoraLocalUser, String userId, VideoTrackInfo info, AgoraRemoteVideoTrack agoraRemoteVideoTrack)`
当订阅了远程用户的视频轨道时发生。
- **参数:**
  - `agoraLocalUser`: `AgoraLocalUser` - 本地用户对象。
  - `userId`: `String` - 远程用户的 ID。
  - `info`: `VideoTrackInfo` - 关于视频轨道的信息。
  - `agoraRemoteVideoTrack`: `AgoraRemoteVideoTrack` - 远程视频轨道。

### INetworkObserver
网络事件的观察者接口。

#### 方法

##### `onUplinkNetworkInfoUpdated(AgoraRtcConn agoraRtcConn, UplinkNetworkInfo info)`
当上行网络信息更新时发生。
- **参数:**
  - `agoraRtcConn`: `AgoraRtcConn` - 连接对象。
  - `info`: `UplinkNetworkInfo` - 上行网络信息。

##### `onDownlinkNetworkInfoUpdated(AgoraRtcConn agoraRtcConn, DownlinkNetworkInfo info)`
当下行网络信息更新时发生。
- **参数:**
  - `agoraRtcConn`: `AgoraRtcConn` - 连接对象。
  - `info`: `DownlinkNetworkInfo` - 下行网络信息。

### IVideoFrameObserver2
原始视频帧的观察者接口。

#### 方法

##### `onFrame(AgoraVideoFrameObserver2 agoraVideoFrameObserver2, String channelId, String remoteUserId, VideoFrame frame)`
当接收到视频帧时发生。
- **参数:**
  - `agoraVideoFrameObserver2`: `AgoraVideoFrameObserver2` - 观察者对象。
  - `channelId`: `String` - 频道 ID。
  - `remoteUserId`: `String` - 远程用户的 ID。
  - `frame`: `VideoFrame` - 视频帧。

### IAudioFrameObserver
原始音频帧的观察者接口。

#### 方法

##### `onRecordAudioFrame(AgoraLocalUser agoraLocalUser, String channelId, AudioFrame frame)`
获取录制的音频帧。
- **参数:**
  - `agoraLocalUser`: `AgoraLocalUser` - 本地用户。
  - `channelId`: `String` - 频道 ID。
  - `frame`: `AudioFrame` - 音频帧。
- **返回:** `int` - 0/1 (无实际意义)。

##### `onPlaybackAudioFrame(AgoraLocalUser agoraLocalUser, String channelId, AudioFrame frame)`
获取播放的音频帧。
- **参数:**
  - `agoraLocalUser`: `AgoraLocalUser` - 本地用户。
  - `channelId`: `String` - 频道 ID。
  - `frame`: `AudioFrame` - 音频帧。
- **返回:** `int` - 0/1 (无实际意义)。

##### `onMixedAudioFrame(AgoraLocalUser agoraLocalUser, String channelId, AudioFrame frame)`
获取混合后的音频帧。
- **参数:**
  - `agoraLocalUser`: `AgoraLocalUser` - 本地用户。
  - `channelId`: `String` - 频道 ID。
  - `frame`: `AudioFrame` - 音频帧。
- **返回:** `int` - 0/1 (无实际意义)。

##### `onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agoraLocalUser, String channelId, String userId, AudioFrame frame, VadProcessResult vadResult)`
获取特定用户混音前的播放音频帧。
- **参数:**
  - `agoraLocalUser`: `AgoraLocalUser` - 本地用户。
  - `channelId`: `String` - 频道 ID。
  - `userId`: `String` - 用户 ID。
  - `frame`: `AudioFrame` - 音频帧。
  - `vadResult`: `VadProcessResult` - VAD 结果。
- **返回:** `int` - 0/1 (无实际意义)。

### IAudioEncodedFrameObserver
编码后音频帧的观察者接口。

#### 方法

##### `onEncodedAudioFrameReceived(String remoteUserId, ByteBuffer buffer, EncodedAudioFrameReceiverInfo info)`
当接收到编码后的音频帧时发生。
- **参数:**
  - `remoteUserId`: `String` - 远程用户的 ID。
  - `buffer`: `ByteBuffer` - 编码后的音频帧缓冲区。
  - `info`: `EncodedAudioFrameReceiverInfo` - 关于编码后音频帧的信息。
- **返回:** `int` - 0/1 (无实际意义)。

### IVideoEncodedFrameObserver
编码后视频帧的观察者接口。

#### 方法

##### `onEncodedVideoFrame(AgoraVideoEncodedFrameObserver observer, int userId, ByteBuffer buffer, EncodedVideoFrameInfo info)`
当接收到编码后的视频帧时发生。
- **参数:**
  - `observer`: `AgoraVideoEncodedFrameObserver` - 观察者对象。
  - `userId`: `int` - 用户 ID。
  - `buffer`: `ByteBuffer` - 编码后的视频帧缓冲区。
  - `info`: `EncodedVideoFrameInfo` - 关于编码后视频帧的信息。
- **返回:** `int` - 0/1 (无实际意义)。

### IAgoraAudioProcessorEventHandler
声网音频处理器事件处理器的接口。

#### 方法

##### `onEvent(Constants.AgoraAudioProcessorEventType eventType)`
报告来自音频处理器的事件。
- **参数:**
  - `eventType`: `Constants.AgoraAudioProcessorEventType` - 事件的类型。

##### `onError(int errorCode)`
报告来自音频处理器的错误。
- **参数:**
  - `errorCode`: `int` - 错误码。

---

## 数据结构

### AgoraServiceConfig
AgoraService 的配置。

- **`enableAudioProcessor`**: `int` - 是否启用音频处理模块。`1` (默认) 启用, `0` 禁用。
- **`enableAudioDevice`**: `int` - 是否为录制和播放启用音频设备模块。`1` 启用, `0` (默认) 禁用。
- **`enableVideo`**: `int` - 是否启用视频。`1` 启用, `0` (默认) 禁用。
- **`context`**: `Object` - 用户上下文。对于 Windows，它是窗口句柄；对于 Android，它是 Activity 上下文。
- **`appId`**: `String` - 你的项目的 App ID。
- **`areaCode`**: `int` - 支持的区域码。默认为 `AREA_CODE_GLOB`。
- **`channelProfile`**: `int` - 频道配置文件。默认为 `CHANNEL_PROFILE_LIVE_BROADCASTING`。
- **`audioScenario`**: `int` - 音频场景。默认为 `AUDIO_SCENARIO_AI_SERVER`。
- **`useStringUid`**: `int` - 是否启用字符串用户 ID。`1` 启用, `0` (默认) 禁用。
- **`logFilePath`**: `String` - 日志文件的路径。默认为 `NULL`。
- **`logFileSize`**: `int` - 日志文件的最大大小（KB）。默认为 2048。
- **`logFilters`**: `int` - 日志级别。默认为 `LOG_LEVEL_INFO`。
- **`domainLimit`**: `int` - 是否启用域名限制。`1` 启用, `0` (默认) 禁用。
- **`configDir`**: `String` - 配置文件的路径。默认为 `NULL`。
- **`dataDir`**: `String` - 数据文件的路径。默认为 `NULL`。

### RtcConnConfig
RTC 连接的配置。

- **`autoSubscribeAudio`**: `int` - 是否自动订阅所有音频流。`1` (默认) 是, `0` 否。
- **`autoSubscribeVideo`**: `int` - 是否自动订阅所有视频流。`1` (默认) 是, `0` 否。
- **`enableAudioRecordingOrPlayout`**: `int` - 是否启用音频录制或播放。`1` 是, `0` 否。
- **`maxSendBitrate`**: `int` - 最大发送比特率。
- **`minPort`**: `int` - 连接的最小端口。
- **`maxPort`**: `int` - 连接的最大端口。
- **`audioSubsOptions`**: `AudioSubscriptionOptions` - 音频订阅选项。
- **`clientRoleType`**: `int` - 用户的角色。默认为 `CLIENT_ROLE_AUDIENCE`。
- **`channelProfile`**: `int` - 频道配置文件。
- **`audioRecvMediaPacket`**: `int` - 是否接收音频媒体包。
- **`audioRecvEncodedFrame`**: `int` - 是否接收编码后的音频帧。
- **`videoRecvMediaPacket`**: `int` - 是否接收视频媒体包。

### RtcConnPublishConfig
RTC 连接中发布流的配置。

- **`audioProfile`**: `int` - 音频配置文件。默认为 `AUDIO_PROFILE_DEFAULT`。
- **`audioScenario`**: `int` - 音频场景。默认为 `AUDIO_SCENARIO_AI_SERVER`。
- **`isPublishAudio`**: `boolean` - 是否发布音频。默认为 `true`。
- **`isPublishVideo`**: `boolean` - 是否发布视频。默认为 `false`。
- **`audioPublishType`**: `Constants.AudioPublishType` - 要发布的音频类型。默认为 `PCM`。
- **`videoPublishType`**: `Constants.VideoPublishType` - 要发布的视频类型。默认为 `NO_PUBLISH`。
- **`senderOptions`**: `SenderOptions` - 发送方选项。

### RtcConnInfo
RTC 连接的信息。

- **`id`**: `int` - 连接的唯一标识符。
- **`channelId`**: `String` - 频道 ID。
- **`state`**: `int` - 连接的当前状态。
- **`localUserId`**: `String` - 本地用户的 ID。
- **`internalUid`**: `int` - 内部用户 ID。

### VideoEncoderConfig
视频编码器的配置。

- **`codecType`**: `int` - 视频编解码器类型。
- **`dimensions`**: `VideoDimensions` - 视频帧的尺寸。
- **`frameRate`**: `int` - 视频帧率。
- **`bitrate`**: `int` - 视频编码目标比特率（Kbps）。
- **`minBitrate`**: `int` - 最小编码比特率（Kbps）。
- **`orientationMode`**: `int` - 视频方向模式。
- **`degradationPreference`**: `int` - 带宽限制下的视频降级偏好。
- **`mirrorMode`**: `int` - 视频镜像模式。
- **`encodeAlpha`**: `int` - 是否编码和发送 alpha 数据。

### VideoSubscriptionOptions
视频订阅的选项。

- **`type`**: `int` - 视频订阅的类型。
- **`encodedFrameOnly`**: `int` - 是否仅订阅编码帧。

### SimulcastStreamConfig
联播视频流的配置。

- **`dimensions`**: `VideoDimensions` - 视频的尺寸。
- **`bitrate`**: `int` - 视频的比特率。
- **`framerate`**: `int` - 视频的帧率。

### EncodedVideoFrameInfo
关于编码视频帧的信息。

- **`codecType`**: `int` - 视频编解码器类型。
- **`width`**: `int` - 视频的宽度（像素）。
- **`height`**: `int` - 视频的高度（像素）。
- **`framesPerSecond`**: `int` - 每秒的视频帧数。
- **`frameType`**: `int` - 编码视频帧的帧类型。
- **`rotation`**: `int` - 编码视频帧的旋转信息。
- **`trackId`**: `int` - 多轨视频的轨道 ID。
- **`captureTimeMs`**: `long` - 视频被捕获时的时间戳。
- **`decodeTimeMs`**: `long` - 渲染视频的时间戳。
- **`presentationMs`**: `long` - 视频帧的显示时间戳（PTS，毫秒）。
- **`uid`**: `int` - 用户 ID。
- **`streamType`**: `int` - 视频帧的流类型。

### EncodedAudioFrameInfo
关于编码音频帧的信息。

- **`speech`**: `int` - 指示帧是否包含语音。
- **`codec`**: `int` - 音频帧的编解码器类型。
- **`sendEvenIfEmpty`**: `int` - 即使帧为空也发送。
- **`sampleRateHz`**: `int` - 音频帧的采样率（Hz）。
- **`samplesPerChannel`**: `int` - 每个音频通道的样本数。
- **`numberOfChannels`**: `int` - 音频通道的数量。
- **`captureTimeMs`**: `long` - 采集时间戳（毫秒）。

### EncodedAudioFrameReceiverInfo
编码音频帧接收者的信息。

- **`sendTs`**: `long` - 包的发送时间。
- **`codec`**: `int` - 包的编解码器。

### SenderOptions
发送方的选项。

- **`ccMode`**: `int` - 拥塞控制模式。
- **`codecType`**: `int` - 编解码器类型。
- **`targetBitrate`**: `int` - 目标比特率。

### VideoFrame
代表一个视频帧。

- **`type`**: `int` - 视频帧的类型。
- **`width`**: `int` - 视频的宽度（像素）。
- **`height`**: `int` - 视频的高度（像素）。
- **`yStride`**: `int` - YUV 数据中 Y 缓冲区的行跨度。
- **`uStride`**: `int` - YUV 数据中 U 缓冲区的行跨度。
- **`vStride`**: `int` - YUV 数据中 V 缓冲区的行跨度。
- **`yBuffer`**: `ByteBuffer` - Y 数据缓冲区。
- **`uBuffer`**: `ByteBuffer` - U 数据缓冲区。
- **`vBuffer`**: `ByteBuffer` - V 数据缓冲区。
- **`rotation`**: `int` - 此帧的旋转角度 (0, 90, 180, 270)。
- **`renderTimeMs`**: `long` - 渲染视频流的时间戳。
- **`avsyncType`**: `int` - AV 同步类型。

### ExternalVideoFrame
代表一个外部视频帧。

- **`type`**: `int` - 缓冲区类型。
- **`format`**: `int` - 像素格式。
- **`buffer`**: `ByteBuffer` - 视频缓冲区。
- **`stride`**: `int` - 视频帧的行间距（像素）。
- **`height`**: `int` - 视频帧的高度。
- **`timestamp`**: `long` - 视频帧的时间戳（毫秒）。

### VideoDimensions
视频的尺寸。

- **`width`**: `int` - 视频的宽度。
- **`height`**: `int` - 视频的高度。

### RtcStats
RTC 连接的统计信息。

- **`duration`**: `int` - 通话时长。
- **`txBytes`**: `int` - 传输的总字节数。
- **`rxBytes`**: `int` - 接收的总字节数。
- **`txAudioBytes`**: `int` - 传输的总音频字节数。
- **`txVideoBytes`**: `int` - 传输的总视频字节数。
- **`rxAudioBytes`**: `int` - 接收的总音频字节数。
- **`rxVideoBytes`**: `int` - 接收的总视频字节数。
- **`txKBitRate`**: `short` - 传输比特率（Kbps）。
- **`rxKBitRate`**: `short` - 接收比特率（Kbps）。
- **`userCount`**: `int` - 频道中的用户数。

### UserInfo
关于用户的信息。

- **`userId`**: `String` - 用户 ID。
- **`hasAudio`**: `int` - 用户是否启用了音频。
- **`hasVideo`**: `int` - 用户是否启用了视频。

### VadProcessResult
语音活动检测 (VAD) 过程的结果。

- **`outFrame`**: `byte[]` - VAD 处理后的输出帧。
- **`state`**: `Constants.VadState` - VAD 过程的状态。

### AgoraAudioVadConfig
语音活动检测 (VAD) 的配置。

- **`fftSz`**: `int` - FFT 大小。默认为 1024。
- **`hopSz`**: `int` - FFT Hop 大小。默认为 160。
- **`anaWindowSz`**: `int` - FFT 窗口大小。默认为 768。
- **`voiceProbThr`**: `float` - 语音概率阈值。默认为 0.8。
- **`rmsThr`**: `float` - RMS 阈值（dB）。默认为 -40.0。

### AgoraAudioVadConfigV2
语音活动检测 (VAD) 版本 2 的配置。

- **`preStartRecognizeCount`**: `int` - 语音开始前要保存的音频帧数。默认为 16。
- **`startRecognizeCount`**: `int` - 确认语音状态的音频帧数。默认为 30。
- **`stopRecognizeCount`**: `int` - 确认静音状态的音频帧数。默认为 20。
- **`activePercent`**: `float` - 进入说话状态的活动帧百分比。默认为 0.7。
- **`inactivePercent`**: `float` - 进入静音状态的非活动帧百分比。默认为 0.5。
- **`startVoiceProb`**: `int` - 开始语音的语音概率门限。默认为 70。
- **`stopVoiceProb`**: `int` - 停止语音的语音概率门限。默认为 70。
- **`startRmsThreshold`**: `int` - 开始语音的 RMS 阈值。默认为 -50。
- **`stopRmsThreshold`**: `int` - 停止语音的 RMS 阈值。默认为 -50。

### LocalAudioTrackStats
本地音频轨道的统计信息。

- **`sourceId`**: `int` - 音频的源 ID。
- **`bufferedPcmDataListSize`**: `int` - 缓冲的 PCM 数据列表大小。
- **`missedAudioFrames`**: `int` - 错过的音频帧数。
- **`sentAudioFrames`**: `int` - 发送的音频帧数。
- **`pushedAudioFrames`**: `int` - 推送的音频帧数。
- **`droppedAudioFrames`**: `int` - 丢弃的音频帧数。

### LocalVideoTrackStats
本地视频轨道的统计信息。

- **`numberOfStreams`**: `long` - 视频流的数量。
- **`bytesMajorStream`**: `long` - 主流的字节数。
- **`bytesMinorStream`**: `long` - 辅流的字节数。
- **`framesEncoded`**: `int` - 编码的帧数。
- **`captureFrameRate`**: `int` - 捕获帧率（fps）。
- **`encodeFrameRate`**: `int` - 编码器输出帧率（fps）。
- **`targetMediaBitrateBps`**: `int` - 目标比特率（bps）。

### RemoteAudioTrackStats
远程音频轨道的统计信息。

- **`uid`**: `int` - 远程用户的用户 ID。
- **`quality`**: `int` - 接收到的音频质量。
- **`networkTransportDelay`**: `int` - 网络延迟（毫秒）。
- **`jitterBufferDelay`**: `int` - 抖动缓冲延迟（毫秒）。
- **`audioLossRate`**: `int` - 音频帧丢失率。
- **`receivedBitrate`**: `int` - 接收到的音频流的平均比特率（Kbps）。

### RemoteVideoTrackStats
远程视频轨道的统计信息。

- **`uid`**: `int` - 远程用户的用户 ID。
- **`width`**: `int` - 视频流的宽度（像素）。
- **`height`**: `int` - 视频流的高度（像素）。
- **`receivedBitrate`**: `int` - 接收到的比特率（Kbps）。
- **`decoderOutputFrameRate`**: `int` - 解码器输出帧率（fps）。
- **`frameLossRate`**: `int` - 视频帧丢失率。

### EncryptionConfig
流加密的配置。

- **`encryptionMode`**: `int` - 加密模式。
- **`encryptionKey`**: `String` - 加密密钥。
- **`encryptionKdfSalt`**: `byte[]` - 加密 KDF盐。

### UplinkNetworkInfo
关于上行网络的信息。

- **`videoEncoderTargetBitrateBps`**: `int` - 视频编码器的目标比特率（bps）。

### DownlinkNetworkInfo
关于下行网络的信息。

- **`lastmileBufferDelayTimeMs`**: `int` - 最后一英里缓冲区的延迟时间（毫秒）。
- **`bandwidthEstimationBps`**: `int` - 估计的带宽（bps）。
- **`peerDownlinkInfo`**: `PeerDownlinkInfo` - 关于对等方下行的信息。

### PeerDownlinkInfo
关于对等方下行的信息。

- **`userId`**: `String` - 用户 ID。
- **`streamType`**: `int` - 流类型。
- **`currentDownscaleLevel`**: `int` - 当前的降级级别。
- **`expectedBitrateBps`**: `int` - 预期的比特率（bps）。

### AecConfig
声学回声消除 (AEC) 的配置。

- **`enabled`**: `boolean` - 是否启用 AEC。
- **`stereoAecEnabled`**: `boolean` - 是否启用立体声 AEC。
- **`filterLength`**: `Constants.AecFilterLength` - AEC 线性滤波器长度。
- **`aecModelType`**: `Constants.AecModelType` - AEC 模型类型。
- **`aecSuppressionMode`**: `Constants.AecSuppressionMode` - AEC 抑制级别。

### AnsConfig
自动噪声抑制 (ANS) 的配置。

- **`enabled`**: `boolean` - 是否启用 ANS。
- **`suppressionMode`**: `Constants.AnsSuppressionMode` - ANS 噪声抑制模式。
- **`ansModelType`**: `Constants.AnsModelType` - ANS 模型类型。

### AgcConfig
自动增益控制 (AGC) 的配置。

- **`enabled`**: `boolean` - 是否启用 AGC。
- **`useAnalogMode`**: `boolean` - 是否使用模拟 AGC 模式。
- **`maxDigitalGaindB`**: `int` - 最大数字 AGC 增益（dB）。
- **`targetleveldB`**: `int` - 目标数字 AGC 级别（dB）。

### BghvsConfig
背景人声抑制 (BGHVS) 的配置。

- **`enabled`**: `boolean` - 是否启用 BGHVS。
- **`bghvsSosLenInMs`**: `int` - 触发语音开始 (SOS) 的持续时间（毫秒）。
- **`bghvsEosLenInMs`**: `int` - 触发语音结束 (EOS) 的持续时间（毫秒）。
- **`bghvsSppMode`**: `Constants.BghvsSuppressionMode` - BGHVS 积极级别。

### AgoraAudioProcessorConfig
声网音频处理器的配置。

- **`modelPath`**: `String` - 音频处理模型的文件路径。
- **`aecConfig`**: `AecConfig` - AEC 的配置。
- **`ansConfig`**: `AnsConfig` - ANS 的配置。
- **`agcConfig`**: `AgcConfig` - AGC 的配置。
- **`bghvsConfig`**: `BghvsConfig` - BGHVS 的配置。

### AgoraAudioFrame
代表一个音频帧。

- **`type`**: `int` - 音频帧类型。
- **`sampleRate`**: `int` - 每秒的样本数。
- **`channels`**: `int` - 音频通道数。
- **`samplesPerChannel`**: `int` - 此帧中每个通道的样本数。
- **`bytesPerSample`**: `int` - 每个样本的字节数。
- **`buffer`**: `ByteBuffer` - 音频帧的数据缓冲区。
- **`presentationMs`**: `long` - 音频帧的显示时间戳（PTS，毫秒）。

---

## 工具类

### VadDumpUtils
一个用于将 VAD（语音活动检测）相关数据转储到文件以进行调试和分析的工具类。

#### 构造函数

##### `VadDumpUtils(String path)`
构造一个 `VadDumpUtils` 对象。
- **参数:**
  - `path`: `String` - 转储文件将存储的目录路径。将在此路径内创建一个带有时间戳名称的子目录。

#### 方法

##### `write(AudioFrame frame, byte[] vadResultBytes, Constants.VadState vadResultState)`
将音频帧和 VAD 结果写入转储文件。
- **参数:**
  - `frame`: `AudioFrame` - 原始音频帧。
  - `vadResultBytes`: `byte[]` - 对应于 VAD 结果的音频数据。
  - `vadResultState`: `Constants.VadState` - VAD 结果的状态。

##### `release()`
释放所有资源并关闭转储文件。