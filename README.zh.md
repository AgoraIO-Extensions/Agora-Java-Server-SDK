# Agora Java Server SDK

## 目录

- [开发环境要求](#开发环境要求)
- [快速开始](#快速开始)
- [SDK 获取](#sdk-获取)
- [API 示例](#api-示例)
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

- [Maven](https://central.sonatype.com/artifact/io.agora.rtc/linux-java-sdk/overview)
- [官方下载页面](https://doc.shengwang.cn/doc/rtc-server-sdk/java/resources)

## API 示例

详细示例请参考 [examples/README.zh.md](examples/README.zh.md)

## API 参考

### 基础API 参考

完整 API 文档请访问 [Agora Java Server SDK API 参考](https://doc.shengwang.cn/api-ref/rtc-server-sdk/java/overview)

### AgoraAudioVadV2

#### 介绍

`AgoraAudioVadV2` 是一个用于处理音频帧的语音活动检测 (VAD) 模块。它可以检测音频流中的语音活动，并根据配置参数进行处理。

#### 类和方法

##### AgoraAudioVadV2

###### 构造方法

```java
public AgoraAudioVadV2(AgoraAudioVadConfigV2 config)
```

- **参数**
  - `config`：`AgoraAudioVadConfigV2` 类型，VAD 配置。

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

### 最新版本：v4.4.30.1（2024-11-12）

- 增加 AgoraAudioVad2 相关 Vad2 接口，移除 AgoraAudioVad 相关 Vad 接口
- 新增接收编码音频回调接口 IAudioEncodedFrameObserver
- 修复 LocalAudioDetailedStats 相关回调崩溃问题
- 修改 onAudioVolumeIndication 回调参数类型

### v4.4.30（2024-10-24）

- 详细更新日志请参考 [发版说明](https://doc.shengwang.cn/doc/rtc-server-sdk/java/overview/release-notes)

## 常见问题

如遇到问题，请先查阅 [文档中心](https://doc.shengwang.cn/) 或在 [GitHub Issues](https://github.com/AgoraIO/Agora-Java-Server-SDK/issues) 中搜索相关问题

## 支持

- 技术支持：<sales@shengwang.cn>
- 商业相关问题：<sales@shengwang.cn>
- 其他架构支持：<sales@shengwang.cn>
