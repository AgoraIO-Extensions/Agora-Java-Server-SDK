# AIQoS 版本升级指南

本文档旨在帮助开发者将 Agora Server SDK for Java 升级到支持 AIQoS 的新版本。我们将详细介绍 AIQoS 的概念、相关的接口变更、示例代码的修改方案以及完整的升级步骤。

## 1. AIQoS 概念

AIQoS 是声网专为 AI 场景（如 AIGC、AI 降噪等）打造的特性集，它在 SDK 内部深度优化了网络传输、音频编解码等核心环节。通过将音频场景设置为 `AUDIO_SCENARIO_AI_SERVER`，即可启用 AIQoS。

相比于旧版本推荐的 `CHORUS`（合唱）模式，AIQoS 具有以下优势：

*   **更低延迟**：优化了内部处理流程，显著降低端到端延迟。
*   **更强的抗弱网能力**：在网络条件不佳的情况下，提供更稳定、更清晰的音频体验。
*   **智能场景识别与自动适配**：当 SDK 检测到对端版本不支持 AIQoS 时，能够自动回退到兼容的音频场景，保证通话的连续性。

因此，我们强烈推荐在所有 AI 相关的业务场景中，优先使用 `AUDIO_SCENARIO_AI_SERVER` 模式以获得最佳性能。

## 2. 服务端集成最佳实践

为了确保 SDK 的稳定性和高性能，我们推荐遵循以下服务端集成模式：

### 2.1. Service 的管理

*   **单例模式**：在您的服务进程中，`AgoraService` 应当作为单例存在。即一个进程中只初始化一个 `AgoraService` 实例。
*   **与进程同生命周期**：`AgoraService` 的生命周期应与您的服务进程保持一致。在进程启动时初始化 `AgoraService`，在进程退出前调用 `destroy()` 方法销毁它。

### 2.2. Connection 的管理

*   **对应会话**：在业务逻辑上，一个 `AgoraRtcConn` 实例可以理解为一个独立的通话或会话房间。
*   **多实例共存**：一个 `AgoraService` 实例可以创建和管理多个 `AgoraRtcConn` 实例。
*   **独立生命周期**：不同 `AgoraRtcConn` 实例的生命周期相互独立，可以有不同的配置，其创建和销毁不会影响其他 `Connection`。

### 2.3. Connection 的建立与销毁流程

一个典型的 `Connection` 生命周期如下：

1.  **创建 Connection**: `AgoraRtcConn conn = service.agoraRtcConnCreate(ccfg, publishConfig);`
2.  **注册回调**: `conn.registerObserver(...)`, `conn.registerLocalUserObserver(...)` 等
3.  **加入频道**: `conn.connect(token, channelName, userId);`
4.  **媒体操作**:
    *   **发布音频**: 调用一次 `conn.publishAudio()`，然后循环调用 `conn.pushAudioPcmData()` 或 `conn.pushAudioEncodedData()` 推送数据。
    *   **发布视频**: 调用一次 `conn.publishVideo()`，然后循环调用 `conn.pushVideoFrame()` 或 `conn.pushVideoEncodedData()` 推送数据。
5.  **断开连接**: `conn.disconnect();`
6.  **释放资源**: `conn.destroy();`

> **注意**: 新版 SDK 大大简化了资源管理。开发者不再需要手动管理 `Track` 和 `Sender` 等对象的生命周期。当 `Connection` 被 `destroy` 时，其关联的所有内部资源（包括媒体轨道）都会被自动销毁，从而有效避免了资源泄露的风险。

## 3. 核心接口变更详解

本次升级对 API 进行了较大幅度的重构，旨在简化 API 调用，使其职责更清晰。核心思想是将连接管理、媒体流发布、数据推送等操作统一收敛到 `AgoraRtcConn` 类中。

以下是各个主要类的详细接口变更：

### 3.1. AgoraService

`AgoraService` 作为 SDK 的入口，其职责更加聚焦于服务的初始化和连接对象的创建。

*   **删除的方法**:
    *   `createLocalAudioTrack()`: 不再需要手动创建本地音频轨道对象。
    *   `createCustomAudioTrackPcm()`: 功能被整合，由 `agoraRtcConnCreate` 内部处理。
    *   `createCustomAudioTrackEncoded()`: 同上。
    *   `createCustomVideoTrackFrame()`: 同上。
    *   `createCustomVideoTrackEncoded()`: 同上。
    *   `createMediaNodeFactory()`: `AgoraMediaNodeFactory` 不再作为公开接口使用。

*   **修改的方法**:
    *   `agoraRtcConnCreate(RtcConnConfig ccfg)` -> `agoraRtcConnCreate(RtcConnConfig ccfg, RtcConnPublishConfig publishConfig)`:
        *   **变更**: 增加了 `RtcConnPublishConfig` 参数。
        *   **影响**: 现在创建连接时，需要明确指定媒体发布的类型（如 PCM 音频、YUV 视频等）。这使得连接的意图更加清晰。

### 3.2. AgoraRtcConn

`AgoraRtcConn` 是本次变更的核心，现在是进行所有与连接相关的媒体操作的主要入口。

*   **新增的方法**:
    *   `registerLocalUserObserver(ILocalUserObserver observer)`: 注册本地用户相关的观察者。
    *   `registerAudioFrameObserver(...)`, `registerVideoFrameObserver(...)`, `registerVideoEncodedFrameObserver(...)`: 注册媒体帧数据的观察者，原先这些注册分散在各个 Track 对象上。
    *   `publishAudio()`, `unpublishAudio()`: 发布和取消发布音频。
    *   `publishVideo()`, `unpublishVideo()`: 发布和取消发布视频。
    *   `pushAudioPcmData(...)`, `pushAudioEncodedData(...)`: 推送音频数据。
    *   `pushVideoFrame(...)`, `pushVideoEncodedData(...)`: 推送视频数据。
    *   `setVideoEncoderConfig(...)`, `enableSimulcastStream(...)`: 配置视频编码和大小流。
    *   `sendStreamMessage(byte[] messageData)`: 发送数据流消息，简化了调用，无需开发者管理 `streamId`。
    *   `sendAudioMetaData(byte[] metaData)`: 发送音频元数据。

*   **删除的类**:
    *   `AgoraMediaNodeFactory`, `AgoraLocalAudioTrack`, `AgoraLocalVideoTrack`, `AgoraAudioPcmDataSender` 等类不再作为公开 API 使用。它们的功能已经被整合到 `AgoraRtcConn` 内部，开发者无需再手动管理这些对象的生命周期。

### 3.3. AgoraLocalUser

大部分操作已上移至 `AgoraRtcConn`,具体请参考 `AgoraRtcConn.java` 中的方法。

### 3.4. 观察者接口实现方式变更

本次升级对观察者的实现方式进行了统一。之前通过继承 `Default...Observer` 适配器类的方式，现在统一为直接实现 `I...Observer` 接口。由于回调方法签名保持不变，您只需简单地替换类名/接口名，无需修改已有的回调逻辑。

*   **删除的类**:
    *   `DefaultRtcConnObserver`
    *   `DefaultLocalUserObserver`
    *   以及其他所有 `Default...Observer` 适配器类。

*   **影响**: 这是一个简单的查找和替换操作。例如，将 `class MyObserver extends DefaultRtcConnObserver` 修改为 `class MyObserver implements IRtcConnObserver`。

*   **接口方法变更为 `default`**:
    *   所有 `I...Observer` 接口中的方法都已被声明为 `default` 方法。这意味着您不再需要实现所有方法，只需覆盖您关心的回调即可，这在一定程度上简化了实现。

*   **新增的回调**:
    *   `IRtcConnObserver.onAIQoSCapabilityMissing(AgoraRtcConn agoraRtcConn, int defaultFallbackSenario)`: 当 SDK 检测到对端版本不支持 AIQoS 时触发此回调。您可以实现此回调来决定是自动切换到指定的兼容场景，还是手动处理场景切换。

## 4. 兼容性与升级方案

当您的业务需要同时支持使用旧版本 SDK 的 App 和使用新版本 AIQoS SDK 的 App 时，需要一个完善的兼容性方案来保证互通。

### 4.1. Scenario 互通原则

| 服务端 Scenario       | 客户端支持的 Scenario | 备注                                                                      |
| :-------------------- | :-------------------- | :------------------------------------------------------------------------ |
| AudioScenarioAIServer | AudioScenarioAIClient | 推荐组合，可享受 AIQoS 优化。如果客户端非 AIClient 场景，语音会出现异常。 |
| 非 AIServer           | 任意场景              | 兼容所有非 AIClient 场景的客户端。                                        |

### 4.2. 兼容性方案流程

核心思想是**由业务服务端来决策每个 RTC 连接应该使用哪种 `scenario`**。

1.  **业务服务器**:
    *   升级 RESTful API，支持 v1（旧）和 v2（新）两个版本的请求。
    *   能根据客户端 App 的版本号，判断其应使用旧场景还是 AIQoS 场景。
    *   在返回给 RTC 服务器的 `{channel, token}` 信息中，增加 `scenario` 字段。
2.  **新版本 App**:
    *   升级到支持 AIQoS 的客户端 SDK 版本。
    *   向业务服务器发起 v2 版本的 RESTful API 请求。
    *   能解析业务服务器返回的 `scenario`，并在加入 RTC 频道时设置。
3.  **RTC 服务器 (使用新版 Go/Java SDK)**:
    *   升级到支持 AIQoS 的版本。
    *   根据业务服务器传来的 `scenario` 参数，为每个 `Connection` 设置对应的音频场景。

通过这种方式，RTC 服务器可以为来自新旧版本 App 的请求创建使用不同 `scenario` 的 `Connection`，从而实现新旧版本的共存与互通。

## 5. Java 代码迁移指南

### 5.1. 完整升级步骤

1.  **替换 SDK**: 将您项目中的 `AgoraServerSDK.jar`（或相应的 Maven/Gradle 依赖）更新到最新版本。
2.  **修改 Service 配置**: 在您的代码中找到初始化 `AgoraServiceConfig` 的地方，将 `setAudioScenario` 的参数从 `Constants.AUDIO_SCENARIO_CHORUS` 修改为 `Constants.AUDIO_SCENARIO_AI_SERVER`。
3.  **重构 Connection 创建和媒体操作**:
    *   检查所有调用 `service.agoraRtcConnCreate()` 的地方，确保传递了 `RtcConnPublishConfig` 参数。
    *   移除所有手动创建和管理 `AgoraLocalAudioTrack`, `AgoraLocalVideoTrack` 等对象的代码。
    *   将媒体发布、数据推送等操作全部改为通过 `AgoraRtcConn` 实例调用。
4.  **修改观察者实现**:
    *   编译您的项目，编译器会报告所有使用 `DefaultRtcConnObserver` 和 `DefaultLocalUserObserver` 的错误。
    *   逐一修改这些错误，将它们替换为对 `IRtcConnObserver` 和 `ILocalUserObserver` 接口的实现。
    *   为 `onAIQoSCapabilityMissing` 回调添加实现，以处理 AIQoS 的兼容性问题。
5.  **重新编译和测试**: 完成代码修改后，请重新编译您的项目，并进行充分的功能测试，特别是音频收发、场景切换等核心功能，以确保升级成功。

### 5.2. 代码迁移示例

本章节提供更细化的场景迁移示例。

#### 场景一：发送 PCM 音频

**Before:**
```java
// 1. 创建 Factory 和 Sender
AgoraMediaNodeFactory factory = service.createMediaNodeFactory();
AgoraAudioPcmDataSender pcmSender = factory.createAudioPcmDataSender();

// 2. 创建自定义音频轨道
AgoraLocalAudioTrack audioTrack = service.createCustomAudioTrackPcm(pcmSender);

// 3. 发布轨道
conn.getLocalUser().publishAudio(audioTrack);

// 4. 循环发送数据
AudioFrame audioFrame = new AudioFrame(...);
pcmSender.sendAudioPcmData(audioFrame);

// 5. 释放资源
// ... a lot of destroy() and release() calls
```

**After:**
```java
// 1. 创建连接时声明发布 PCM 音频
RtcConnPublishConfig publishConfig = new RtcConnPublishConfig();
publishConfig.setAudioScenario(Constants.AUDIO_SCENARIO_AI_SERVER);
publishConfig.setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT);
publishConfig.setAudioPublishType(Constants.AudioPublishType.PCM);
publishConfig.setIsPublishAudio(true);
AgoraRtcConn conn = service.agoraRtcConnCreate(ccfg, publishConfig);

// 2. 发布音频
conn.publishAudio();

// 3. 直接通过 conn 推送数据
conn.pushAudioPcmData(buffer, sampleRate, channels);

// 4. 释放资源
conn.disconnect();
conn.destroy();
```

#### 场景二：接收 PCM 音频

**Before:**
```java
// 在 LocalUser 上注册 Observer
conn.getLocalUser().registerAudioFrameObserver(new IAudioFrameObserver() {
    @Override
    public int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agoraLocalUser, String channelId, String userId, AudioFrame frame, VadProcessResult vadResult) {
        // ... process frame
        return 0;
    }
    // ...
});

// 在 LocalUser 上设置参数
conn.getLocalUser().setPlaybackAudioFrameBeforeMixingParameters(channels, sampleRate);
```

**After:**
```java
// 直接在 conn 上注册 Observer
conn.registerAudioFrameObserver(new IAudioFrameObserver() {
    @Override
    public int onPlaybackAudioFrameBeforeMixing(AgoraLocalUser agoraLocalUser, String channelId, String userId, AudioFrame frame, VadProcessResult vadResult) {
        // ... process frame (业务逻辑不变)
        return 0;
    }
    // ...
}, false, null); // 第二、三参数用于 VAD

// 在 LocalUser 上设置参数
conn.getLocalUser().setPlaybackAudioFrameBeforeMixingParameters(channels, sampleRate);
```

#### 场景三：发送 YUV 视频

**Before:**
```java
// 1. 创建 Factory 和 Sender
AgoraMediaNodeFactory factory = service.createMediaNodeFactory();
AgoraVideoFrameSender yuvSender = factory.createVideoFrameSender();

// 2. 创建自定义视频轨道并配置
AgoraLocalVideoTrack videoTrack = service.createCustomVideoTrackFrame(yuvSender);
VideoEncoderConfig videoConfig = new VideoEncoderConfig(...);
videoTrack.setVideoEncoderConfig(videoConfig);
videoTrack.setEnabled(1);

// 3. 发布轨道
conn.getLocalUser().publishVideo(videoTrack);

// 4. 循环发送数据
ExternalVideoFrame videoFrame = new ExternalVideoFrame(...);
yuvSender.sendVideoFrame(videoFrame);

// 5. 释放资源
// ... a lot of destroy() and release() calls
```

**After:**
```java
// 1. 创建连接时声明发布 YUV 视频
RtcConnPublishConfig publishConfig = new RtcConnPublishConfig();
publishConfig.setAudioScenario(Constants.AUDIO_SCENARIO_AI_SERVER);
publishConfig.setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT);
publishConfig.setVideoPublishType(Constants.VideoPublishType.YUV);
publishConfig.setIsPublishVideo(true);
AgoraRtcConn conn = service.agoraRtcConnCreate(ccfg, publishConfig);

// 2. 直接在 conn 上配置和发布
VideoEncoderConfig videoConfig = new VideoEncoderConfig(...);
conn.setVideoEncoderConfig(videoConfig);
conn.publishVideo();

// 3. 直接通过 conn 推送数据
ExternalVideoFrame videoFrame = new ExternalVideoFrame(...);
conn.pushVideoFrame(videoFrame);

// 4. 释放资源
conn.disconnect();
conn.destroy();
```

#### 场景四：接收 YUV 视频

**Before:**
```java
conn.getLocalUser().registerVideoFrameObserver(new AgoraVideoFrameObserver2(new IVideoFrameObserver2() {
    @Override
    public void onFrame(AgoraVideoFrameObserver2 observer, String channelId, String remoteUserId, VideoFrame frame) {
        // ... process frame
    }
}));
```

**After:**
```java
// 直接在 conn 上注册
conn.registerVideoFrameObserver(new AgoraVideoFrameObserver2(new IVideoFrameObserver2() {
    @Override
    public void onFrame(AgoraVideoFrameObserver2 observer, String channelId, String remoteUserId, VideoFrame frame) {
        // ... process frame (业务逻辑不变)
    }
}));
```

#### 场景五：发送 H264 视频

**Before:**
```java
// 1. 创建 Factory 和 Sender
AgoraMediaNodeFactory factory = service.createMediaNodeFactory();
AgoraVideoEncodedImageSender imageSender = factory.createVideoEncodedImageSender();

// 2. 创建自定义视频轨道并配置
SenderOptions options = new SenderOptions();
options.setCcMode(Constants.TCC_ENABLED);
AgoraLocalVideoTrack videoTrack = service.createCustomVideoTrackEncoded(imageSender, options);
VideoEncoderConfig videoConfig = new VideoEncoderConfig(...);
videoConfig.setCodecType(Constants.VIDEO_CODEC_H264);
videoTrack.setVideoEncoderConfig(videoConfig);

// 3. 发布
conn.getLocalUser().publishVideo(videoTrack);

// 4. 循环发送数据
imageSender.sendEncodedVideoImage(buffer, info);

// 5. 释放资源
// ... a lot of destroy() and release() calls
```

**After:**
```java
// 1. 创建连接时声明发布编码视频
RtcConnPublishConfig publishConfig = new RtcConnPublishConfig();
publishConfig.setAudioScenario(Constants.AUDIO_SCENARIO_AI_SERVER);
publishConfig.setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT);
publishConfig.setVideoPublishType(Constants.VideoPublishType.ENCODED_IMAGE);
publishConfig.setIsPublishVideo(true);
SenderOptions options = new SenderOptions();
options.setCcMode(Constants.TCC_ENABLED);
publishConfig.setSenderOptions(options);
AgoraRtcConn conn = service.agoraRtcConnCreate(ccfg, publishConfig);

// 2. 直接在 conn 上配置和发布
VideoEncoderConfig videoConfig = new VideoEncoderConfig(...);
videoConfig.setCodecType(Constants.VIDEO_CODEC_H264);
conn.setVideoEncoderConfig(videoConfig);
conn.publishVideo();

// 3. 直接通过 conn 推送数据
conn.pushVideoEncodedData(buffer, info);

// 4. 释放资源
conn.disconnect();
conn.destroy();
```

#### 场景六：接收 H264 视频

**Before:**
```java
conn.getLocalUser().registerVideoEncodedFrameObserver(new AgoraVideoEncodedFrameObserver(new IVideoEncodedFrameObserver() {
    @Override
    public int onEncodedVideoFrame(AgoraVideoEncodedFrameObserver observer, int userId, ByteBuffer buffer, EncodedVideoFrameInfo info) {
        // ... process encoded frame
        return 0;
    }
}));
```

**After:**
```java
// 直接在 conn 上注册
conn.registerVideoEncodedFrameObserver(new AgoraVideoEncodedFrameObserver(new IVideoEncodedFrameObserver() {
    @Override
    public int onEncodedVideoFrame(AgoraVideoEncodedFrameObserver observer, int userId, ByteBuffer buffer, EncodedVideoFrameInfo info) {
        // ... process encoded frame (业务逻辑不变)
        return 0;
    }
}));
```

#### 场景七：替换 AudioConsumerUtils

旧版本中可能使用 `AudioConsumerUtils` 等工具类来封装数据发送逻辑。在新版 SDK 中，开发者可以直接调用 `conn.pushAudioPcmData()` 来推送数据，从而根据业务场景灵活地实现“实时发送”或“文件一次性发送”。

##### 场景 7.1: 实时发送 PCM 音频流 (如实时麦克风流)

这种场景适用于需要低延迟、连续不断发送音频流的业务，比如将服务端的实时音频转发给客户端。

**Before:**
```java
// 旧方案依赖一个工具类来处理定时发送
AudioConsumerUtils audioConsumerUtils = new AudioConsumerUtils(conn, channels, sampleRate);

// 启动一个线程，周期性地从业务逻辑中获取数据并推入工具类
new Thread(() -> {
    while (true) {
        byte[] pcmData = getPcmDataFromMic(); // 从业务逻辑获取实时音频数据
        audioConsumerUtils.pushPcmData(pcmData);
        Thread.sleep(10);
    }
}).start();

// 另起一个线程，定时消费并发送
new Thread(() -> {
    while (true) {
        audioConsumerUtils.consume();
        Thread.sleep(10);
    }
}).start();
```

**After:**
```java
// 新方案：开发者可以自己实现数据读取和发送的线程，从而获得完全的控制权
// 以下是一个模拟从文件读取并实时发送，并增加了初始数据缓存的示例

// 在连接成功后，启动一个新线程来发送数据
new Thread(() -> {
    try (FileInputStream fos = new FileInputStream("path/to/audio.pcm")) {
        // 假设每次读取并发送10ms的数据
        int interval = 10; // ms
        int sampleRate = 16000;
        int channels = 1;
        int pcmDataSize = channels * (sampleRate / 1000) * interval * 2; // 16bit
        byte[] buffer = new byte[pcmDataSize];

        // 为了避免推流之初因数据不足导致SDK发送静音包，可以先缓存一部分数据（如10帧）
        // 1. 快速读取10帧数据进行缓存
        int cacheFrameCount = 10;
        ByteBuffer cachePcmDataBuffer = ByteBuffer.allocate(pcmDataSize * cacheFrameCount);
        for (int i = 0; i < cacheFrameCount; i++) {
            int bytesRead = fos.read(buffer);
            if (bytesRead > 0) {
                // 将读取的数据放入缓存
                cachePcmDataBuffer.put(buffer, 0, bytesRead);
            }
            // 如果文件提前结束，则跳出缓存循环
            if (bytesRead < pcmDataSize) {
                break;
            }
        }

        // 2. 一次性将缓存的数据全部发送出去
        if (cachePcmDataBuffer.position() > 0) {
            byte[] initialData = new byte[cachePcmDataBuffer.position()];
            cachePcmDataBuffer.flip();
            cachePcmDataBuffer.get(initialData);
            conn.pushAudioPcmData(initialData, sampleRate, channels);
        }

        // 3. 缓存发送完毕后，进入正常的实时发送模式
        // 只要能从源读取到数据，就持续发送
        while (fos.read(buffer) != -1) {
            // 通过 conn 直接推送数据
            conn.pushAudioPcmData(buffer, sampleRate, channels);

            // 控制发送速率，模拟实时发送
            Thread.sleep(interval);
        }
    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
    }
}).start();

// 在断开连接时，需要确保发送线程也已正确终止
```

##### 场景 7.2: 发送完整的 PCM 音频文件 (如 AI TTS 生成的语音)

这种场景适用于一次性生成了完整的音频文件，需要将其完整、快速地发送出去的业务。

**Before:**
```java
// 旧方案同样使用工具类，但一次性将所有数据推入
AudioConsumerUtils audioConsumerUtils = new AudioConsumerUtils(conn, channels, sampleRate);
byte[] pcmData = Utils.readPcmFromFile("path/to/tts_audio.pcm");
audioConsumerUtils.pushPcmData(pcmData);

// 启动一个线程来发送
new Thread(() -> {
    while (audioConsumerUtils.getRemainingCacheDurationInMs() > 0) {
        audioConsumerUtils.consume();
        Thread.sleep(10);
    }
}).start();
```

**After:**
```java
// 新方案：一次性读取文件，一次性发送
byte[] pcmData = Utils.readPcmFromFile("path/to/tts_audio.pcm");

// 可选：为了保证发送的数据是SDK内部处理帧（10ms）的整数倍，可以进行对齐
int bytesPerMs = channels * (sampleRate / 1000) * 2;
if (bytesPerMs > 0 && pcmData.length % bytesPerMs != 0) {
    int newLength = (pcmData.length / bytesPerMs) * bytesPerMs;
    pcmData = Arrays.copyOf(pcmData, newLength);
}

// 直接调用 pushAudioPcmData 一次性发送所有数据
// SDK内部会自动处理分包和发送逻辑
conn.pushAudioPcmData(pcmData, sampleRate, channels);
```

#### 场景八：修改观察者的实现

**Before:**
```java
public class MyRtcConnObserver extends DefaultRtcConnObserver {
    @Override
    public void onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason) {
        // ...
    }
    // ... 需要覆盖很多不关心的方法
}
```

**After:**
```java
public class MyRtcConnObserver implements IRtcConnObserver {
    @Override
    public void onConnected(AgoraRtcConn agoraRtcConn, RtcConnInfo connInfo, int reason) {
        // ... 业务逻辑不变
    }

    // 只需实现关心的方法
    
    @Override
    public int onAIQoSCapabilityMissing(AgoraRtcConn agoraRtcConn, int defaultFallbackSenario) {
        // 新增的回调，处理兼容性
        // 当对端不支持AIQoS时，SDK会建议一个回退场景，这里直接采纳
        System.out.println("AIQoS missing, fallback to scenario: " + defaultFallbackSenario);
        return defaultFallbackSenario;
    }
}
```