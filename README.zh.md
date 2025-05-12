# Agora Linux Server Java SDK

中文 | [English](./README.md)

## 目录

1. [简介](#简介)
2. [开发环境要求](#开发环境要求)
   - [硬件环境](#硬件环境)
   - [软件环境](#软件环境)
3. [SDK 下载](#sdk-下载)
   - [Maven 下载](#maven-下载)
   - [CDN 下载](#cdn-下载)
4. [集成 SDK](#集成-sdk)
   - [1. Maven 集成](#1-maven-集成)
     - [1.1 添加 Maven 依赖](#11-添加-maven-依赖)
     - [1.2 集成 so 库文件](#12-集成-so-库文件)
   - [2. 本地 SDK 集成](#2-本地-sdk-集成)
     - [2.1 SDK 包结构](#21-sdk-包结构)
     - [2.2 集成 JAR 文件](#22-集成-jar-文件)
     - [2.3 集成 so 库文件](#23-集成-so-库文件)
   - [3. 加载原生库 (.so 文件)](#3-加载原生库-so-文件)
     - [3.1 提取 so 库文件](#31-提取-so-库文件)
     - [3.2 配置加载路径](#32-配置加载路径)
5. [快速开始](#快速开始)
   - [官方示例文档](#官方示例文档)
   - [开通服务](#开通服务)
   - [跑通 Examples](#跑通-examples)
     - [环境准备](#环境准备)
     - [项目配置](#项目配置)
     - [编译过程](#编译过程)
     - [运行示例](#运行示例)
     - [测试 case](#测试-case)
     - [常见问题](#常见问题)
6. [API 参考](#api-参考)
   - [API 文档参考](#api-文档参考)
   - [VAD 模块](#vad-模块)
     - [介绍](#介绍)
     - [类和方法](#类和方法)
     - [使用示例](#使用示例)
7. [更新日志](#更新日志)
   - [v4.4.32（2025-05-12）](#v44322025-05-12)
   - [v4.4.31.4（2025-03-21）](#v443142025-03-21)
   - [v4.4.31.3（2025-02-26）](#v443132025-02-26)
   - [v4.4.31.2（2025-02-19）](#v443122025-02-19)
   - [v4.4.31.1（2025-01-06）](#v443112025-01-06)
   - [v4.4.31（2024-12-23）](#v44312024-12-23)
   - [v4.4.30.2（2024-11-20）](#v443022024-11-20)
   - [v4.4.30.1（2024-11-12）](#v443012024-11-12)
   - [v4.4.30（2024-10-24）](#v44302024-10-24)
8. [其他参考](#其他参考)

## 简介

Agora Linux Server Java SDK (v4.4.32) 为您提供了强大的实时音视频通信能力，可无缝集成到 Linux 服务器端 Java 应用程序中。借助此 SDK，您的服务器可以作为数据源或处理节点加入 Agora 频道，实时获取和处理音视频流，从而实现多种业务相关的其他高级功能。

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

## SDK 下载

### Maven 下载

```xml
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-java-sdk</artifactId>
    <version>4.4.32</version>
</dependency>
```

### CDN 下载

[Agora-Linux-Java-SDK-v4.4.31.4-x86_64-491956-341b4be9b9-20250402_171133](https://download.agora.io/sdk/release/Agora-Linux-Java-SDK-v4.4.31.4-x86_64-491956-341b4be9b9-20250402_171133.zip)

## 集成 SDK

SDK 集成有两种方式：通过 Maven 集成和本地 SDK 集成。

### 1. Maven 集成

Maven 集成是最简单的方式，可以自动管理 Java 依赖关系。

#### 1.1 添加 Maven 依赖

在项目的 `pom.xml` 文件中添加以下依赖：

```xml
<!-- x86_64 平台 -->
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-java-sdk</artifactId>
    <version>4.4.32</version>
</dependency>
```

#### 1.2 集成 so 库文件

Maven 依赖包含了所需的 JAR 文件，但仍需手动处理 `.so` 库文件才能运行。请参考下面的 **加载原生库 (.so 文件)** 部分。

### 2. 本地 SDK 集成

本地 SDK 是一个包含所有必要文件的完整包，适合需要更灵活控制的场景。

#### 2.1 SDK 包结构

从官网下载的 SDK 包（zip 格式）包含以下内容：

- **doc/** - JavaDoc 文档，详细的 API 说明
- **examples/** - 示例代码和项目
- **sdk/** - 核心 SDK 文件
  - `agora-sdk.jar` - Java 类库
  - `agora-sdk-javadoc.jar` - JavaDoc 文档

#### 2.2 集成 JAR 文件

你可以通过两种方式集成 JAR 文件：

###### 本地 Maven 仓库方法

方法一：只安装 SDK JAR

```sh
mvn install:install-file \
  -Dfile=sdk/agora-sdk.jar \
  -DgroupId=io.agora.rtc \
  -DartifactId=linux-java-sdk \
  -Dversion=4.4.32 \
  -Dpackaging=jar \
  -DgeneratePom=true
```

方法二：同时安装 SDK JAR 和 JavaDoc JAR

```sh
mvn install:install-file \
  -Dfile=sdk/agora-sdk.jar \
  -DgroupId=io.agora.rtc \
  -DartifactId=linux-java-sdk \
  -Dversion=4.4.32 \
  -Dpackaging=jar \
  -DgeneratePom=true \
  -Djavadoc=sdk/agora-sdk-javadoc.jar
```

安装后，在 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>io.agora.rtc</groupId>
    <artifactId>linux-java-sdk</artifactId>
    <version>4.4.32</version>
</dependency>
```

###### 直接引用方法

1. 将 JAR 文件复制到项目的 `libs` 目录：

   ```sh
   mkdir -p libs
   cp sdk/agora-sdk.jar libs/
   cp sdk/agora-sdk-javadoc.jar libs/  # 可选，用于 IDE 支持
   ```

2. 在 Java 项目中添加 classpath 引用：

   ```sh
   # 使用 SDK JAR
   java -cp .:libs/agora-sdk.jar 你的主类

   # 在 IDE 中配置 JavaDoc（常见的 IDE 如 IntelliJ IDEA 或 Eclipse 支持直接关联 JavaDoc JAR）
   ```

#### 2.3 集成 so 库文件

下载的 SDK 包中已经包含了 `.so` 文件。你需要确保 Java 程序运行时能够找到这些文件。请参考下面的 **加载原生库 (.so 文件)** 部分。

### 3. 加载原生库 (.so 文件)

Agora Linux Server Java SDK 依赖于底层的 C++ 原生库（`.so` 文件）。无论是通过 Maven 集成还是本地集成，都需要确保 Java 虚拟机 (JVM) 在运行时能够找到并加载这些库。

#### 3.1 提取 so 库文件

`.so` 文件包含在 `agora-sdk.jar` 或 `linux-java-sdk-x.x.x.x.jar` 文件内部。你需要先将它们提取出来：

1.  在你的项目或部署目录下创建一个用于存放库文件的目录，例如 `libs`：

    ```sh
    mkdir -p libs
    cd libs
    ```

2.  使用 `jar` 命令从 SDK 的 JAR 文件中提取内容（假设 JAR 文件位于 `libs` 目录下或 Maven 缓存中）：

    ```sh
    # 如果使用本地集成方式，JAR 文件通常在 libs 目录下
    jar xvf agora-sdk.jar

    # 如果使用 Maven 集成方式，JAR 文件在 Maven 缓存中，例如：
    # jar xvf ~/.m2/repository/io/agora/rtc/linux-java-sdk/4.4.32/linux-java-sdk-4.4.32.jar
    ```

3.  提取后，`libs` 目录下会生成 `native/linux/x86_64` 子目录，其中包含所需的 `.so` 文件：

    ```
    libs/
    ├── agora-sdk.jar (或者空的，如果仅用于提取)
    ├── io/          # Java 的 class 类所在，无需关注
    ├── META-INF/    # JAR 文件和应用程序相关的元数据，无需关注
    └── native/      # 对应平台的 so 库文件
        └── linux/
            └── x86_64/   # x86_64 平台 so 库
                ├── libagora_rtc_sdk.so
                ├── libagora-fdkaac.so
                ├── libaosl.so
                └── libbinding.so
    ```

#### 3.2 配置加载路径

有两种主要方法让 JVM 找到 `.so` 文件：

**方法一：通过设置环境变量 `LD_LIBRARY_PATH` (推荐)**

这是最可靠的方式，特别是在 `.so` 文件之间存在依赖关系时。

```sh
# 确定你的 .so 文件所在的目录，假设在 ./libs/native/linux/x86_64
LIB_DIR=$(pwd)/libs/native/linux/x86_64

# 设置 LD_LIBRARY_PATH 环境变量，将库目录添加到现有路径的前面
export LD_LIBRARY_PATH=$LIB_DIR:$LD_LIBRARY_PATH

# 运行你的 Java 应用
java -jar 你的应用.jar
# 或者使用 classpath
# java -cp "你的classpath" 你的主类
```

**方法二：通过 JVM 参数 `-Djava.library.path`**

这种方法直接告诉 JVM 在哪里查找库文件。

```sh
# 确定你的 .so 文件所在的目录，假设在 ./libs/native/linux/x86_64
LIB_DIR=$(pwd)/libs/native/linux/x86_64

# 运行 Java 应用，并通过 -D 参数指定库路径
java -Djava.library.path=$LIB_DIR -jar 你的应用.jar
# 或者使用 classpath
# java -Djava.library.path=$LIB_DIR -cp "你的classpath" 你的主类
```

> **注意**：
>
> - 推荐使用方法一 (`LD_LIBRARY_PATH`)，因为它能更好地处理库之间的依赖。如果仅使用 `-Djava.library.path`，有时可能因为库找不到其依赖的其他库而加载失败。
> - 确保 `$LIB_DIR` 指向包含 `libagora_rtc_sdk.so` 等文件的 **确切目录**。
> - 你可以将设置环境变量的命令放入启动脚本中，以便每次运行应用时自动配置。

参考以下脚本示例，它结合了两种方法，并设置了 classpath：

```sh
#!/bin/bash
# 获取当前脚本所在目录的绝对路径
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# 确定 so 库文件路径 (假设在脚本目录下的 libs/native/linux/x86_64)
LIB_PATH="$SCRIPT_DIR/libs/native/linux/x86_64"
# SDK JAR 路径 (假设在脚本目录下的 libs)
SDK_JAR="$SCRIPT_DIR/libs/agora-sdk.jar"
# 你的应用主类
MAIN_CLASS="你的主类"
# 你的应用的其他依赖 classpath (如果有)
APP_CP="你的其他classpath"

# 设置库路径环境变量
export LD_LIBRARY_PATH=$LIB_PATH:$LD_LIBRARY_PATH

# 组合 classpath
CLASSPATH=".:$SDK_JAR:$APP_CP" # '.' 表示当前目录

# 执行 Java 程序
# 同时使用 LD_LIBRARY_PATH 和 -Djava.library.path 以确保兼容性
java -Djava.library.path=$LIB_PATH -cp "$CLASSPATH" $MAIN_CLASS
```

## 快速开始

### 官方示例文档

参考 [官方示例文档](https://doc.shengwang.cn/doc/rtc-server-sdk/java/get-started/run-example)

### 开通服务

参考 [官网开通服务](https://doc.shengwang.cn/doc/rtc-server-sdk/java/get-started/enable-service)

### 跑通 Examples

#### 环境准备

##### 安装 FFmpeg（可选，用于 MP4 相关测试）

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

#### 项目配置

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

#### 编译过程

执行编译脚本：

```bash
./build.sh [-ffmpegUtils] [-mediaUtils]
```

- 使用 `-ffmpegUtils` 选项编译 FFmpeg 相关库（MP4 测试必需）
- 使用 `-mediaUtils` 选项编译解码音视频相关库（发送编码音视频测试必须）

#### 运行示例

1. 运行测试脚本：

   ```bash
   ./script/TestCaseName.sh
   ```

2. 修改测试参数：直接编辑对应的 `.sh` 文件

#### 测试 case

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

#### 常见问题

- 确保 Java 环境正确安装和配置
- 验证 `agora-sdk.jar` 版本兼容性
- 运行前检查 `APP_ID` 和 `TOKEN` 配置
- 按顺序执行步骤，避免依赖问题

## API 参考

### API 文档参考

完整 API 文档可参考以下资源：

- [API-reference.zh.md](./API-reference.zh.md) 文件（仅供参考）
- 官方文档 [Agora Java Server SDK API 参考](https://doc.shengwang.cn/api-ref/rtc-server-sdk/java/overview)（以官方文档为准）

### VAD 模块

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

### v4.4.32（2025-05-12）

#### API 变更

- `AgoraService` 新增 `getSdkVersion` 方法，用于获取 SDK 版本号
- `AgoraAudioEncodedFrameSender` 移除 `send(byte[] payloadData, int payloadSize, EncodedAudioFrameInfo info)` 方法，新增 `sendEncodedAudioFrame(byte[] payloadData, EncodedAudioFrameInfo info)` 方法替代
- `AgoraAudioPcmDataSender` 的 `send(byte[] audioData, int captureTimestamp, int samplesPerChannel, int bytesPerSample,
int numberOfChannels, int sampleRate) ` 方法标位不推荐，新增 `sendAudioPcmData(AudioFrame audioFrame)` 方法替代
- `AgoraVideoEncodedImageSender` 移除 `send(byte[] imageBuffer, int length, EncodedVideoFrameInfo info` 方法，新增 `sendEncodedVideoImage(byte[] imageBuffer, EncodedVideoFrameInfo info)` 方法替代
- `AgoraVideoFrameSender` 移除 `send(ExternalVideoFrame frame)` 方法，新增 `sendVideoFrame(ExternalVideoFrame frame)` 方法替代

#### 改进与优化

- 修复了 `destroy` 方法可能导致的崩溃问题

### v4.4.31.4（2025-03-21）

#### 改进与优化

- 修复了多线程环境下可能导致的异常崩溃问题
- 改进了错误处理流程，增强了异常情况下的恢复能力

### v4.4.31.3（2025-02-26）

#### 改进与优化

- 修复了由于内存复用可能导致的异常处理问题

### v4.4.31.2（2025-02-19）

#### API 变更

- 新增 `sendStreamMessage(int streamId, byte[] messageData)` 方法，弃用 `sendStreamMessage(int streamId, String message, int length)` 方法

#### 改进与优化

- 优化代码处理，提高系统稳健性

### v4.4.31.1（2025-01-06）

#### 改进与优化

- 优化 VAD 功能配置，现在默认开启 VAD 功能，无需手动配置

### v4.4.31（2024-12-23）

#### API 变更

- 在 `AgoraServiceConfig` 中新增 `DomainLimit` 配置选项，用于域名限制管理
- 新增 `VadDumpUtils` 工具类，支持导出 VAD 处理过程的调试数据
- 新增 `AudioConsumerUtils` 类，提供优化的 PCM 数据传输机制
- 在 `AgoraLocalUser` 中修改 `registerAudioFrameObserver` 方法，支持配置 `AgoraAudioVadConfigV2` 参数
- 在 `IAudioFrameObserver` 中新增 `onPlaybackAudioFrameBeforeMixing` 回调的 `vadResult` 参数
- 在 `AgoraLocalUser` 类中新增 `sendAudioMetaData` 方法，支持发送音频元数据
- 在 `ILocalUserObserver` 类中新增 `onAudioMetaDataReceived` 回调，用于接收音频元数据
- 在 `ExternalVideoFrame` 类中增加 `ColorSpace` 属性，支持自定义颜色空间设置

#### 改进与优化

- 优化代码逻辑架构，显著提升内存使用效率
- 修复多处内存泄露问题，提高系统稳定性
- 增强内存访问安全机制，有效防止内存踩踏问题

### v4.4.30.2（2024-11-20）

#### API 变更

- 增强了 AgoraAudioVadV2 的 `processFrame` 处理，新增 `START_SPEAKING` 和 `STOP_SPEAKING` 状态回调
- 改进了编码帧回调的参数类型，`onEncodedAudioFrameReceived`、`onEncodedVideoImageReceived`、`onEncodedVideoFrame` 现在使用 `ByteBuffer` 替代 `Byte` 数组

#### 改进与优化

- VAD 插件启动优化，`enableExtension` 现在在 SDK 内部实现，应用程序不再需要手动调用此方法
- 修复了 `VideoFrame` 中 `alphaBuffer` 和 `metadataBuffer` 的处理问题

### v4.4.30.1（2024-11-12）

#### API 变更

- 增加 AgoraAudioVad2 相关 `Vad2` 接口，移除 AgoraAudioVad 相关 `Vad` 接口
- 新增接收编码音频回调接口 `IAudioEncodedFrameObserver`

#### 改进与优化

- 修复 `LocalAudioDetailedStats` 相关回调崩溃问题
- 修改 `onAudioVolumeIndication` 回调参数类型

### v4.4.30（2024-10-24）

- 详细更新日志请参考 [发版说明](https://doc.shengwang.cn/doc/rtc-server-sdk/java/overview/release-notes)

## 其他参考

详细参考官网（<https://doc.shengwang.cn/doc/rtc-server-sdk/java/landing-page>）

官网 API 文档 [Agora Server Java SDK API 参考](https://doc.shengwang.cn/api-ref/rtc-server-sdk/java/overview)
