# Agora Linux Server Java SDK

## 目录

- [开发环境要求](#开发环境要求)
- [快速开始](#快速开始)
- [SDK 获取](#sdk-获取)
- [Examples](#examples)
- [API 参考](#api-参考)
- [更新日志](#更新日志)
- [常见问题](#常见问题)
- [支持](#支持)

## 开发环境要求

### 硬件环境

- **操作系统**：Ubuntu 18.04+ 或 CentOS 7.0+
- **CPU 架构**：x86-64
- **性能要求**：
  - CPU：8 核 1.8 GHz 或更高
  - 内存：2 GB（推荐 4 GB+）
- **网络要求**：
  - 公网 IP
  - 允许访问 `.agora.io` 和 `.agoralab.co` 域名

### 软件环境

- Apache Maven 或其他构建工具
- JDK 8+

## 快速开始

参考 [官方示例文档](https://doc.shengwang.cn/doc/rtc-server-sdk/java/get-started/run-example)

## SDK 获取

### Maven 下载

```xml
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-java-sdk</artifactId>
    <version>4.4.31.4</version>
</dependency>
```

### CDN 下载

[Agora-Linux-Java-SDK-v4.4.31.4-x86_64-491956-341b4be9b9-20250402_171133](https://download.agora.io/sdk/release/Agora-Linux-Java-SDK-v4.4.31.4-x86_64-491956-341b4be9b9-20250402_171133.zip)

## Examples

### 环境准备

#### 安装 FFmpeg（可选，用于 MP4 相关测试）

1. 更新系统包：

   ```bash
   sudo apt update
   ```

2. 安装 FFmpeg（需要 7.0+）：

   ```bash
   sudo apt install ffmpeg
   ```

3. 安装 FFmpeg 开发库：

   ```bash
   sudo apt-get install libavcodec-dev libavformat-dev libavutil-dev libswscale-dev
   ```

4. 获取库依赖路径：

   ```bash
   pkg-config --cflags --libs libavformat libavcodec libavutil libswresample libswscale
   ```

5. 更新 `build.sh` 中的 `FFMPEG_INCLUDE_DIR` 和 `FFMPEG_LIB_DIR`。

### 项目配置

1. 进入 `Examples` 目录：

   ```bash
   cd Examples
   ```

2. 创建 `.keys` 文件，添加：

   ```
   APP_ID=your_app_id
   TOKEN=your_token
   ```

   _如果未开启证书，TOKEN 值可为空，例如：_

   ```
   APP_ID=abcd1234
   TOKEN=
   ```

3. 准备 SDK 文件：

   - 重命名 JAR 为 `agora-sdk.jar`
   - 放入 `libs/` 目录

4. 提取 SO 文件：

   进入 `Examples/libs` 目录，执行：

   ```bash
   jar xvf agora-sdk.jar
   ```

   提取完成后，确保目录结构如下：

   ```
   libs/
   ├── agora-sdk.jar
   └── native/
       └── linux/
           └── x86_64/
               └── lib*.so (各种动态库文件)
   ```

   注意：确保所有 .so 文件都被正确提取，这些文件是 SDK 的核心组件。

### 编译过程

执行编译脚本：

```bash
./build.sh [-ffmpegUtils] [-mediaUtils]
```

- 使用 `-ffmpegUtils` 选项编译 FFmpeg 相关库（MP4 测试必需）
- 使用 `-mediaUtils` 选项编译解码音视频相关库（发送编码音视频测试必须）

### 运行示例

1. 运行测试脚本：

   ```bash
   ./script/TestCaseName.sh
   ```

2. 修改测试参数：直接编辑对应的 `.sh` 文件

### 测试 case

- 发送 PCM 音频

  参考 `Examples/src/java/io/agora/rtc/example/scenario/SendPcmFileTest.java`,实现循环发送 pcm 文件

  参考 `Examples/src/java/io/agora/rtc/example/scenario/SendPcmRealTimeTest.java`,实现发送流式 pcm 数据

- 发送 YUV 视频

  参考 `Examples/src/java/io/agora/rtc/example/scenario/SendYuvTest.java`,实现流式发送 yuv 数据

- 发送 H264 视频

  参考 `Examples/src/java/io/agora/rtc/example/scenario/SendH264Test.java`,实现流式发送 h264 数据

- 发送 Opus 音频

  参考 `Examples/src/java/io/agora/rtc/example/scenario/SendOpusTest.java`,实现流式发送 opus 数据

- 发送 MP4 音视频

  参考 `Examples/src/java/io/agora/rtc/example/scenario/SendMp4Test.java`,实现发送 MP4 文件

- 接收 PCM 音频

  参考 `Examples/src/java/io/agora/rtc/example/scenario/ReceiverPcmVadTest.java`,实现接收 pcm 数据并携带 VAD 数据

  参考 `Examples/src/java/io/agora/rtc/example/scenario/ReceiverPcmDirectSendTest.java`,实现接收 pcm 数据并直接返回发送

- 接收 PCM&H264 音视频

  参考 `Examples/src/java/io/agora/rtc/example/scenario/ReceiverPcmH264Test.java`,实现接收 pcm&h264 数据

- 接收 PCM&YUV 音视频

  参考 `Examples/src/java/io/agora/rtc/example/scenario/ReceiverPcmYuvTest.java`,实现接收 pcm&yuv 数据

- 发送接收流消息

  参考 `Examples/src/java/io/agora/rtc/example/scenario/SendReceiverStreamMessageTest.java`,实现发送接收流消息

### 常见问题

- 确保 Java 环境正确安装和配置
- 验证 `agora-sdk.jar` 版本兼容性
- 运行前检查 `APP_ID` 和 `TOKEN` 配置
- 按顺序执行步骤，避免依赖问题

## API 参考

### 基础 API 参考

完整 API 文档请访问 [Agora Java Server SDK API 参考](https://doc.shengwang.cn/api-ref/rtc-server-sdk/java/overview)

### AgoraAudioVadV2

#### 介绍

`AgoraAudioVadV2` 是一个用于处理音频帧的语音活动检测 (VAD) 模块。它可以检测音频流中的语音活动，并根据配置参数进行处理。

#### 类和方法

##### AgoraAudioVadV2 类

###### 构造方法

```java
public AgoraAudioVadV2(AgoraAudioVadConfigV2 config)
```

- **参数**
  - `config`：`AgoraAudioVadConfigV2` 类型，VAD 配置。

###### AgoraAudioVadConfigV2 属性

| 属性名                 | 类型  | 描述                                       | 默认值 | 取值范围               |
| ---------------------- | ----- | ------------------------------------------ | ------ | ---------------------- |
| preStartRecognizeCount | int   | 开始说话状态前保存的音频帧数               | 16     | [0, Integer.MAX_VALUE] |
| startRecognizeCount    | int   | 说话状态的音频帧数                         | 30     | [1, Integer.MAX_VALUE] |
| stopRecognizeCount     | int   | 停止说话状态的音频帧数                     | 20     | [1, Integer.MAX_VALUE] |
| activePercent          | float | 在 startRecognizeCount 帧中活跃帧的百分比  | 0.7    | [0.0, 1.0]             |
| inactivePercent        | float | 在 stopRecognizeCount 帧中非活跃帧的百分比 | 0.5    | [0.0, 1.0]             |
| startVoiceProb         | int   | 开始语音检测的概率阈值                     | 70     | [0, 100]               |
| stopVoiceProb          | int   | 停止语音检测的概率阈值                     | 70     | [0, 100]               |
| startRmsThreshold      | int   | 开始语音检测的 RMS 阈值                    | -50    | [-100, 0]              |
| stopRmsThreshold       | int   | 停止语音检测的 RMS 阈值                    | -50    | [-100, 0]              |

###### 注意事项

- `startVoiceProb`: 值越低，帧被判断为活跃的概率越高，开始阶段会更早开始。在需要更敏感的语音检测时可以适当降低。
- `stopVoiceProb`: 值越高，帧被判断为非活跃的概率越高，结束阶段会更早开始。在需要更快结束语音检测时可以适当提高。
- `startRmsThreshold` 和 `stopRmsThreshold`:
  - 值越高，对语音活动越敏感。
  - 在安静环境中推荐使用默认值 -50。
  - 在嘈杂环境中可以调高到 -40 到 -30 之间，以减少误检。
  - 根据实际使用场景和音频特征进行微调可获得最佳效果。

###### 方法

```java
public synchronized VadProcessResult processFrame(AudioFrame frame)
```

- **参数**
  - `frame`：`AudioFrame` 类型，音频帧。
- **返回**
  - `VadProcessResult` 类型，VAD 处理结果。

```java
public synchronized void destroy()
```

- 销毁 VAD 模块，释放资源。

##### VadProcessResult

存储 VAD 处理结果。

###### 构造方法

```java
public VadProcessResult(byte[] result, Constants.VadState state)
```

- **参数**
  - `result`：`byte[]` 类型，处理后的音频数据。
  - `state`：`Constants.VadState` 类型，当前 VAD 状态。

#### 使用示例

下面是一个简单的示例代码，展示如何使用 `AgoraAudioVadV2` 进行音频帧处理：

```java
import io.agora.rtc.AgoraAudioVadV2;
import io.agora.rtc.AgoraAudioVadConfigV2;
import io.agora.rtc.Constants;
import io.agora.rtc.AudioFrame;
import io.agora.rtc.VadProcessResult;

public class Main {
    public static void main(String[] args) {
        // 创建 VAD 配置
        AgoraAudioVadConfigV2 config = new AgoraAudioVadConfigV2();
        config.setPreStartRecognizeCount(16);
        config.setStartRecognizeCount(30);
        config.setStopRecognizeCount(20);
        config.setActivePercent(0.7f);
        config.setInactivePercent(0.5f);
        config.setStartVoiceProb(70);
        config.setStopVoiceProb(70);
        config.setStartRmsThreshold(-50);
        config.setStopRmsThreshold(-50);

        // 创建 VAD 实例
        AgoraAudioVadV2 vad = new AgoraAudioVadV2(config);

        // 模拟音频帧处理
        AudioFrame frame = new AudioFrame();
        // 设置 frame 的属性...

        VadProcessResult result = vad.processFrame(frame);
        if (result != null) {
            System.out.println("VAD State: " + result.getState());
            System.out.println("Processed Data Length: " + result.getResult().length);
        }

        // 销毁 VAD 实例
        vad.destroy();
    }
}
```

## 更新日志

### v4.4.31.4（2025-03-21）

- 修复了多线程环境下可能导致的异常崩溃问题。
- 改进了错误处理流程，增强了异常情况下的恢复能力。

### v4.4.31.3（2025-02-26）

- 修复了由于内存复用可能导致的异常处理问题。

### v4.4.31.2（2025-02-19）

#### 优化

- 优化代码处理，提高系统稳健性。

#### 新增

- 新增 `sendStreamMessage(int streamId, byte[] messageData)` 方法，弃用 `sendStreamMessage(int streamId, String message, int length)` 方法。

### v4.4.31.1（2025-01-06）

#### 功能优化

- 优化 VAD 功能配置,现在默认开启 VAD 功能,无需手动配置

### v4.4.31（2024-12-23）

#### 新增功能

- 在 `AgoraServiceConfig` 中新增 `DomainLimit` 配置选项，用于域名限制管理。
- 新增 `VadDumpUtils` 工具类，支持导出 VAD 处理过程的调试数据，便于问题诊断。
- 新增 `AudioConsumerUtils` 类，提供优化的 PCM 数据传输机制，有效避免音频失真问题。
- 在 `AgoraLocalUser` 中修改 `registerAudioFrameObserver` 方法，支持配置 `AgoraAudioVadConfigV2` 参数。
- 在 `IAudioFrameObserver` 中新增 `onPlaybackAudioFrameBeforeMixing` 回调的 `vadResult` 参数，提供更详细的 VAD 处理结果。
- 在 `AgoraLocalUser` 类中新增 `sendAudioMetaData` 方法，支持发送音频元数据。
- 在 `ILocalUserObserver` 类中新增 `onAudioMetaDataReceived` 回调，用于接收音频元数据。
- 在 `ExternalVideoFrame` 类中增加 `ColorSpace` 属性，支持自定义颜色空间设置。

#### 性能优化

- 优化代码逻辑架构，显著提升内存使用效率。
- 修复多处内存泄露问题，提高系统稳定性。
- 增强内存访问安全机制，有效防止内存踩踏问题。

### v4.4.30.2（2024-11-20）

- 增强了 AgoraAudioVadV2 的 `processFrame` 处理，新增 `START_SPEAKING` 和 `STOP_SPEAKING` 状态回调。
- 改进了编码帧回调的参数类型，`onEncodedAudioFrameReceived`、`onEncodedVideoImageReceived`、`onEncodedVideoFrame` 现在使用 `ByteBuffer` 替代 `Byte` 数组，提高性能和灵活性。
- VAD 插件启动优化，`enableExtension` 现在在 SDK 内部实现，应用程序不再需要手动调用此方法。
- 修复了 `VideoFrame` 中 `alphaBuffer` 和 `metadataBuffer` 的处理问题。

#### 开发者注意事项

- 请更新使用编码帧回调的代码，以适应新的 `ByteBuffer` 参数类型。
- 如之前手动调用了 VAD 插件的 `enableExtension`，现在可以移除该调用。

### v4.4.30.1（2024-11-12）

- 增加 AgoraAudioVad2 相关 `Vad2` 接口，移除 AgoraAudioVad 相关 `Vad` 接口。
- 新增接收编码音频回调接口 `IAudioEncodedFrameObserver`。
- 修复 `LocalAudioDetailedStats` 相关回调崩溃问题。
- 修改 `onAudioVolumeIndication` 回调参数类型。

### v4.4.30（2024-10-24）

- 详细更新日志请参考 [发版说明](https://doc.shengwang.cn/doc/rtc-server-sdk/java/overview/release-notes)。

## 常见问题

如遇到问题，请先查阅 [文档中心](https://doc.shengwang.cn/) 或在 [GitHub Issues](https://github.com/AgoraIO/Agora-Java-Server-SDK/issues) 中搜索相关问题

## 支持

- 技术支持：<sales@shengwang.cn>
- 商业相关问题：<sales@shengwang.cn>
- 其他架构支持：<sales@shengwang.cn>
