# Agora-Java-Server-SDK

## 开发环境要求

确保你的服务器满足以下要求：

### 硬件环境

#### 操作系统

- Ubuntu (14.04 或更高版本)
- CentOS (6.6 或更高版本)

#### CPU 架构

- arm64
- x86-64

> 注：如需在其他架构上集成 SDK，请联系 <sales@shengwang.cn>

#### 性能要求

- CPU：8 核 1.8 GHz 或更高配置
- 内存：2 GB（推荐 4 GB 或更高）

#### 网络要求

- 服务器接入公网，有公网 IP
- 服务器允许访问 `.agora.io` 以及 `.agoralab.co` 域名

### 软件环境

- Apache Maven 或其他构建工具（本文以 Apache Maven 为例）
- JDK 8

## 快速开始

### 跑通示例项目

请参考[官方示例文档](https://doc.shengwang.cn/doc/rtc-server-sdk/java/get-started/run-example)。

## 下载SDK

[官网下载](https://doc.shengwang.cn/doc/rtc-server-sdk/java/resources)

[最新工程版本](https://download.agora.io/sdk/release/linux-java-sdk-2.0_04af2fa340.zip)

## 测试API Examples

### 集成SDK

将下载好的 SDK 放到 `examples/libs` 目录下。

### 配置APP_ID和TOKEN

在 `examples` 目录下新建 `.keys` 文件，并在文件中按照以下格式添加 `APP_ID` 和 `TOKEN` 值。

```
APP_ID=XXX
TOKEN=XXX
```

注：如果没有对应值不填即可。

### 测试步骤

以下以 `MultipleConnectionPcmSendTest.sh` 为例，其他测试可替换对应的 `.sh` 文件：

请按照以下步骤进行测试：

```bash
#!/bin/bash
set -e

cd examples
./build.sh
./script/MultipleConnectionPcmSendTest.sh
```

1. **集成 SDK**：确保在 `examples/libs` 目录下集成 SDK。
2. **编译示例**：进入 `examples` 目录并执行 `build.sh` 进行编译。
3. **运行测试**：执行 `/script/MultipleConnectionPcmSendTest.sh` 脚本进行测试。

### 注意事项

- **脚本执行顺序**：确保按照以上顺序执行脚本，以避免依赖问题。
- **测试替换**：若需测试其他功能，只需替换对应的 `.sh` 文件。

## VAD 用法

### 创建和初始化 VAD 实例

1. 创建一个新的 VAD 实例：

   ```java
   AgoraAudioVad audioVad = new AgoraAudioVad();
   ```

2. 初始化 VAD 配置：

   ```java
   AgoraAudioVadConfig config = new AgoraAudioVadConfig();
   audioVad.initialize(config);
   ```

### 处理音频帧

调用 `processPcmFrame` 方法处理一个音频帧。该帧是一个 16 位、16 kHz 和单声道的 PCM 数据：

```java
byte[] frame = // 获取音频 PCM 数据
VadProcessResult result = audioVad.processPcmFrame(frame);
```

### VAD 处理结果

`VadProcessResult` 标识音频 VAD 处理结果：

- `state` 返回值表示当前语音活动检测（VAD）状态：
  - `0` 表示未检测到语音
  - `1` 表示语音开始
  - `2` 表示正在进行语音
  - `3` 表示当前语音段结束

- 如果函数处于状态 `1`、`2` 或 `3`，则 `outFrame` 将包含与 VAD 状态相对应的 PCM 数据。

### 处理 ASR/TTS

当用户想要执行 ASR/TTS 处理时，应将 `outFrame` 的数据发送到 ASR 系统。

### 销毁 VAD 实例

当不再需要 VAD 实例时，调用 `audioVad.destroy()`：

```java
audioVad.destroy();
```

### 注意事项

- 在不再需要 ASR 系统时，应释放 VAD 实例。
- 一个 VAD 实例对应一个音频流。

## API 参考

### 基础API

- [官网API参考](https://doc.shengwang.cn/api-ref/rtc-server-sdk/java/overview)

### AgoraAudioVad 类

`AgoraAudioVad` 是一个用于语音活动检测（VAD）的管理类。通过这个类，你可以实现对音频数据的处理和分析。

#### 使用步骤

1. 调用 `AgoraAudioVad` 构造函数创建 `AgoraAudioVad` 对象。
2. 使用 `AgoraAudioVadConfig` 配置 `AgoraAudioVad` 对象。

#### 成员函数

##### `int initialize(AgoraAudioVadConfig config)`

配置 `AgoraAudioVad` 对象。

- **参数**
  - `config`: 配置参数 (`AgoraAudioVadConfig` 类型)
- **返回值**
  - `0`: 成功
  - 非 `0`: 失败

##### `VadProcessResult processPcmFrame(byte[] frame)`

处理音频 PCM 数据。

- **参数**
  - `frame`: 音频 PCM 数据 (字节数组)
- **返回值**
  - `VadProcessResult` 对象，包含：
    - `state`: 当前语音活动检测（VAD）状态
    - `outFrame`: 与 VAD 状态相对应的 PCM 数据

##### `void destroy()`

销毁 VAD 实例。

#### 示例代码

```java
AgoraAudioVad vad = new AgoraAudioVad();
AgoraAudioVadConfig config = new AgoraAudioVadConfig();
// 设置配置参数
int result = vad.initialize(config);
if (result == 0) {
    byte[] audioFrame = // 获取音频 PCM 数据
    VadProcessResult processResult = vad.processPcmFrame(audioFrame);
    // 处理 VAD 结果
}
vad.destroy();
```

## 常见问题解答

## 更新日志
